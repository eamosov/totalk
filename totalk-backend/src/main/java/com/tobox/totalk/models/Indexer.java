package com.tobox.totalk.models;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.collections.map.LazyMap;
import org.apache.thrift.TException;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.index.VersionType;
import org.everthrift.appserver.model.LocalEventBus;
import org.everthrift.appserver.model.events.InsertEntityEvent;
import org.everthrift.appserver.model.events.UpdateEntityEvent;
import org.everthrift.appserver.model.lazy.LazyLoadManager;
import org.everthrift.clustering.rabbit.RabbitThriftClientIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.eventbus.Subscribe;
import com.tobox.totalk.models.beansec.Security;
import com.tobox.totalk.thrift.rpc.EsIndexService;
import com.tobox.totalk.thrift.rpc.EsOp;
import com.tobox.totalk.thrift.rpc.IndexTask;

//TODO сделать удаление из индекса
//TODO сделать обновление индексируемых сущностей по сравнению индексируемых полей
@Component
public class Indexer implements InitializingBean, DisposableBean{
	
	private static final Logger log = LoggerFactory.getLogger(Indexer.class);
	
	@Autowired
	private LocalEventBus localEventBus;
	
	@Autowired
	protected ApplicationContext context;
	
	@Autowired
	private Security security;
	
	@Autowired
	protected Client esClient;
	
	@Autowired
	private RabbitThriftClientIF rabbit;
	
	private Thread thread;
	
	// factoryName -> (id -> version)
	@SuppressWarnings("unchecked")	
	private Map<String, Map<String, EsIndexableIF>> batch = LazyMap.decorate(Maps.newHashMap(), () -> Maps.newHashMap());
	
	private synchronized void batchPut(String factoryName, List<EsIndexableIF> ids){
		final Map<String, EsIndexableIF> f = batch.get(factoryName); 
		for (EsIndexableIF id: ids){
			final EsIndexableIF old = f.get(id.getId());
			if (old == null || old.getVersion() < id.getVersion())
				f.put(id.getId(),  id);
		}
	}
	
	private synchronized Map<String, List<EsIndexableIF>> batchGet(){
		
		if (batch.isEmpty())
			return Collections.emptyMap();
		
		final Map<String, List<EsIndexableIF>> r =  ImmutableMap.copyOf(Maps.transformValues(batch, v -> (ImmutableList.copyOf(v.values()))));
		batch.clear();
		return r;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		localEventBus.register(this);
		
		thread = new Thread(new Runnable(){

			@Override
			public void run() {
				do{					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						processBatch();
						return;
					}
					processBatch();
				}while(true);				
			}});
		
		thread.start();
	}		
	
	@Subscribe
	public void onIndexEvent(InsertEntityEvent event){
		
		if (event.factory instanceof EsProviderIF){
			
			if (!(event.entity instanceof EsIndexableIF)){
				log.error("Entity {} must be instanceof EsIndexableIF", event.entity.getClass().getCanonicalName());
			}
			
			scheduleIndex(((EsProviderIF)event.factory).getBeanName(), (EsIndexableIF)event.entity);			
		}		
	}
	
	@Subscribe
	public void onProperyUpdateEvent(UpdateEntityEvent event) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException{
		
		if (event.factory instanceof EsProviderIF){
	    	final Set<String> triggers = ((EsProviderIF)event.factory).getIndexTriggers();
	    	
	    	if (triggers == null){
	    		if (!event.afterUpdate.equals(event.beforeUpdate)){
	    			if (log.isDebugEnabled())
	    				log.debug("Detected changed(not equals) for entity of class {} pk:{}", event.factory.getEntityClass().getSimpleName(), event.afterUpdate.getPk());
	    			
	    			scheduleIndex(((EsProviderIF)event.factory).getBeanName(), (EsIndexableIF)event.afterUpdate);	    			
	    		}
	    	}else {
	    		for (String propertyName: triggers){
		    		final PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(event.factory.getEntityClass(), propertyName);
		    		final Object before = event.beforeUpdate == null ? null : pd.getReadMethod().invoke(event.beforeUpdate);
		    		final Object after = event.afterUpdate == null ? null : pd.getReadMethod().invoke(event.afterUpdate);
		    		
		    		if (!Objects.equals(before, after)){
		    			
		    			if (log.isDebugEnabled())
		    				log.debug("Detected changed property '{}' for entity of class {} pk:{}", propertyName, event.factory.getEntityClass().getSimpleName(), event.afterUpdate.getPk());
		    			
		    			scheduleIndex(((EsProviderIF)event.factory).getBeanName(), (EsIndexableIF)event.afterUpdate);
		    		}
	    		}
	    	}			
		}		
	}
	
	public void scheduleIndex(String factoryName, EsIndexableIF e){
		scheduleIndex(factoryName, Collections.singletonList(e));
	}

//	public void scheduleIndex0(String factoryName, List<String> ids){
//		scheduleIndex(factoryName, Lists.transform(ids, id -> (new EsIdVer(id, 0))));
//	}

	public void scheduleIndex(String factoryName, List<EsIndexableIF> ids){
		
		log.debug("scheduleIndex: factory={}, ids={}", factoryName, ids);
		
		if (!ids.isEmpty())
			batchPut(factoryName, ids);
	}
	
	private void processBatch(){
		
		final Map<String, List<EsIndexableIF>> batch = batchGet();

		if (!batch.isEmpty()){
			log.debug("processBatch: {}", batch);
			
			batch.forEach((k,v) -> {
				try{					
					final List<IndexTask> tasks = buildIndexTasks(k, v);
					
					if (!tasks.isEmpty())					
						rabbit.onIface(EsIndexService.Iface.class).index(tasks);
					
				}catch(TException e){
					throw new RuntimeException(e);
				}				
			});
		}
	}
	
	public List<IndexTask> buildIndexTasks(String factoryName, List<EsIndexableIF> ids){
		log.debug("runIndex: factory={}, ids={}", factoryName, ids);
		
		if (CollectionUtils.isEmpty(ids))
			return Collections.emptyList();		
		
    	final EsProviderIF<EsIndexableIF> factory = (EsProviderIF)context.getBean(factoryName);
    	
    	if (factory == null){
    		log.error("Couldn't find factory: {}", factoryName);
    		return Collections.emptyList();
    	}
    	    	
		final Map<String, EsIndexableIF> loaded = Maps.newHashMap(factory.findEntityByIdAsMap(Collections2.transform(ids, EsIndexableIF::getId)));		
		
		final List<IndexTask> tasks = Lists.newArrayListWithCapacity(loaded.size());
		final List<EsIndexableIF> toIndex = Lists.newArrayListWithCapacity(loaded.size());
		
		for (EsIndexableIF id: ids){
			
			EsIndexableIF o = loaded.get(id.getId());
			if (o !=null && o.getVersion() >= id.getVersion()){
				toIndex.add(o);
			}else if (o == null){
				tasks.add(new IndexTask(EsOp.DELETE, factory.getIndexName(), factory.getMappingName(), factory.getVersionType().getValue(), 0, id.getId(), null, null));				
			}else {									
				log.debug("o.version={}, id.version={}, id.id={}, factory={}", o.getVersion(), id.getVersion(), id.getId(), factory.getClass().getSimpleName());
				
				factory.invalidateLocal(id.getId());					
				o = factory.findEntityById(id.getId());
				
				if (o == null){						
					log.error("coudn't load object with id={}", id.getId());
				}else if (o.getVersion() < id.getVersion()){
					log.error("loaded o.version={} < id.version={}, id={}, factory={}", o.getVersion(), id.getVersion(), id.getId(), factory.getClass().getSimpleName());
				}else{
					toIndex.add(o);
				}
			}						
		}
		
		LazyLoadManager.loadForJson(toIndex);
		security.callJsonSecurityHandlers(toIndex);
		
		tasks.addAll(Collections2.transform(toIndex, o -> new IndexTask(EsOp.INDEX, factory.getIndexName(), factory.getMappingName(), factory.getVersionType().getValue(), o.getVersion(), o.getId(), GsonBuilderHolder.gson.toJson(o), o instanceof EsParentAwareIF ? ((EsParentAwareIF)o).getEsParent() : null)));
		
		if (tasks.isEmpty())
			return Collections.emptyList();
		
		return tasks;
	}
	
	public void runIndexTasks(List<IndexTask> tasks){
		
		if (CollectionUtils.isEmpty(tasks))
			return;
		
		final long startMillis = System.currentTimeMillis();

		final BulkRequestBuilder bulkRequest = esClient.prepareBulk();
		
		for (IndexTask t: tasks){

			switch(t.getOperation()){
			case INDEX:
				final IndexRequestBuilder rb = esClient.prepareIndex(t.getIndexName(), t.getMappingName(), t.getId())
				.setSource(t.getSource())
				.setVersionType(VersionType.fromValue((byte)t.getVersionType()))
				.setVersion(t.getVersion());
		
				if (t.isSetParentId())
					rb.setParent(t.getParentId());
		
				bulkRequest.add(rb);				
				break;
			case DELETE:
				bulkRequest.add(esClient.prepareDelete(t.getIndexName(), t.getMappingName(), t.getId()));
				break;
			}				
		}
		
		final BulkResponse response = bulkRequest.get();
		
		log.debug("index took {} millis for indexing {} entities", System.currentTimeMillis() - startMillis, tasks.size());
				
		if (response.hasFailures()){
			
			boolean hasUnknownErrors = false;
			
			for (int i=0; i< response.getItems().length; i++){
				final BulkItemResponse resp = response.getItems()[i];
				if (resp.isFailed()){							
					if (resp.getFailureMessage().contains("VersionConflictEngineException")){
						log.warn("Version conflict while index:{}", resp.getFailureMessage());
					}else{
						hasUnknownErrors = true;
					}
				}
			}
			
			if (hasUnknownErrors)
				log.error("failures: {}", response.buildFailureMessage());					
		}		
	}
	
	@Override
	public void destroy() throws Exception {
		thread.interrupt();
		thread.join();
	}

}
