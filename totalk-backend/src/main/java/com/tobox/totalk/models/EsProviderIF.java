package com.tobox.totalk.models;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.elasticsearch.index.VersionType;
import org.everthrift.appserver.model.RoModelFactoryIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

public interface EsProviderIF<ENTITY extends EsIndexableIF> extends RoModelFactoryIF<String, ENTITY>{
	
	final static Logger _log = LoggerFactory.getLogger(EsProviderIF.class); 

	Iterator<String> fetchAllIds();
	
	void fetchAll(final int batchSize, Consumer<List<ENTITY>> consumer);
	
	void invalidateLocal(String id);

	String getIndexName();
	String getMappingName();
	
	default Set<String> getIndexTriggers(){
		return null;
	}
		
	default String getBeanName() {
		return this.getClass().getSimpleName();
	}
			    
	default VersionType getVersionType() {
		return VersionType.EXTERNAL_GTE;
	}	
		
	default public void indexInES(ENTITY e){
		CF.i().esIndexer.scheduleIndex(getBeanName(), e);
	}	

	default public void indexInES(String pk, long version){
		CF.i().esIndexer.scheduleIndex(getBeanName(), new EsIndexableIF(){

			@Override
			public String getId() {
				return pk;
			}

			@Override
			public long getVersion() {
				return version;
			}});
	}	

	//Only for dev
	default public void fetchIndexAllInES(final int batchSize){
		CF.i().callerRunsBoundQueueExecutor.submit( () -> {
			
			try{
				fetchAll(batchSize, entities -> {
					CF.i().esIndexer.runIndexTasks(CF.i().esIndexer.buildIndexTasks(getBeanName(), (List)entities));
				});							
			}catch (Exception e){
				_log.error("Exception in fetchIndexAllInES", e);
				throw Throwables.propagate(e);
			}			
		});		
    }    	
}
