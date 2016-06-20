package com.tobox.totalk.models;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.SearchHit;
import org.everthrift.appserver.model.AsyncRoModelFactoryIF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

public class BasicEsService {

	private final Logger log = LoggerFactory.getLogger(this.getClass());

	@Autowired
	protected Client esClient;
	
	@Autowired
	private ApplicationContext ctx;
		
	public BasicEsService() {
		
	}
	
	public static class ESearchResult<ENTITY extends EsIndexableIF>{		
		public final int total;		
		public final List<ENTITY> sources;
		
		public final List<SearchHit> hits;
		public final List<SearchHit> innerHits;
		public final SearchResponse response;
		private final EsProviderIF<ENTITY> factory;
		public final List<ENTITY> loaded;
		
		ESearchResult(EsProviderIF<ENTITY> factory, SearchResponse response, int total, List<SearchHit> hits, List<SearchHit> innerHits, List<ENTITY> sources) {
			super();
			this.total = total;
			this.hits = hits;
			this.innerHits = innerHits;
			this.sources = sources;
			this.response = response;
			this.factory = factory;
			this.loaded =  null;
		}
		
		ESearchResult(ESearchResult<ENTITY> other, List<ENTITY> loaded){
			this.total = other.total;
			this.hits = other.hits;
			this.innerHits = other.innerHits;
			this.sources = other.sources;
			this.response = other.response;
			this.factory = other.factory;
			this.loaded =  loaded;			
		}
		
		public static <ENTITY extends EsIndexableIF> ESearchResult<ENTITY> empty(EsProviderIF<ENTITY> factory){
			return new ESearchResult<ENTITY>(factory, null, 0, Collections.emptyList(), Collections.emptyList(), Collections.emptyList());
		}
		
		public List<String> getIds(){
			return Lists.transform(hits, SearchHit::id);
		}
				
		public List<ENTITY> loadEntities(){
			return factory.findEntityByIdsInOrder(getIds());			
		}
				
		@SuppressWarnings("unchecked")
		public ListenableFuture<ESearchResult<ENTITY>> loadEntitiesAsync(){
			if (!(factory instanceof AsyncRoModelFactoryIF))
				throw new NotImplementedException("Factory " + factory.getClass().getCanonicalName() + " must implement AsyncRoModelFactoryIF");
			
			return Futures.transform(((AsyncRoModelFactoryIF<String, ENTITY>)factory).findEntityByIdsInOrderAsync(getIds()), (List<ENTITY> loaded) -> 
				new ESearchResult<ENTITY>(ESearchResult.this, loaded)
			);			
		}
		
	}

	public <PK, ENTITY extends EsIndexableIF> ESearchResult<ENTITY> searchQuery(EsProviderIF<ENTITY> factory, SearchType searchType, boolean scroll, String query) throws ElasticsearchException {
		return searchQuery(factory, searchType, scroll, null, query, false);
	}
	
	public <PK, ENTITY extends EsIndexableIF> ESearchResult<ENTITY> searchQuery(EsProviderIF<ENTITY> factory, SearchType searchType, boolean scroll, String query, boolean parseSource) throws ElasticsearchException {
		return searchQuery(factory, searchType, scroll, null, query, parseSource);
	}
	
	/**
	 * 
	 * @param vars
	 * @param jsVarName
	 * @return <total, list<accountId>>
	 * @throws ElasticsearchException
	 */
	public <PK, ENTITY extends EsIndexableIF> ESearchResult<ENTITY> searchQuery(EsProviderIF<ENTITY> factory, SearchType searchType, boolean scroll, String innerName, String query, boolean parseSource) throws ElasticsearchException {

		try {
			return searchQueryAsync(factory, searchType, scroll, innerName, query, parseSource).get();
		} catch (ExecutionException  e) {
			Throwables.propagateIfPossible(e.getCause(), ElasticsearchException.class);
			throw Throwables.propagate(e);
		} catch (InterruptedException e) {
			throw Throwables.propagate(e);
		}
		
	}
	
	private <ENTITY extends EsIndexableIF> void proccessHits(final ESearchResult<ENTITY> ret, SearchResponse response, String innerName, Class<ENTITY> entityClass){
		for (SearchHit h: response.getHits().getHits()){
			ret.hits.add(h);
			
			if (innerName !=null){
				for (SearchHit i : h.getInnerHits().get(innerName)){
					ret.innerHits.add(i);
				}							
			}
			
			if (entityClass !=null)
				ret.sources.add(GsonBuilderHolder.gson.fromJson(h.getSourceAsString(), entityClass));
		}		
	}
	
	private <ENTITY extends EsIndexableIF> ListenableFuture<ESearchResult<ENTITY>> scroll(final ESearchResult<ENTITY> ret, final String scrollId, String innerName, Class<ENTITY> entityClass){
		
		final SettableFuture<ESearchResult<ENTITY>> f = SettableFuture.create();
		
		esClient.searchScroll(esClient.prepareSearchScroll(scrollId).setScroll(TimeValue.timeValueMinutes(1)).request(), new ActionListener<SearchResponse>(){

			@Override
			public void onResponse(SearchResponse response) {
				
				log.trace("Scroll response: {}", response);

				if (response.getHits().getHits().length ==0){
					f.set(ret);
					esClient.clearScroll(esClient.prepareClearScroll().addScrollId(scrollId).request());
					return;
				}
				
				proccessHits(ret, response, innerName, entityClass);
				f.setFuture(scroll(ret, response.getScrollId(), innerName, entityClass));
			}

			@Override
			public void onFailure(Throwable e) {
				esClient.clearScroll(esClient.prepareClearScroll().addScrollId(scrollId).request());
				f.setException(e);				
			}});
			
		return f;
	}

	/**
	 * 
	 * @param vars
	 * @param jsVarName
	 * @return <total, list<accountId>>
	 * @throws ElasticsearchException
	 */
	public <PK, ENTITY extends EsIndexableIF> ListenableFuture<ESearchResult<ENTITY>> searchQueryAsync(EsProviderIF<ENTITY> factory, SearchType searchType, boolean scroll, String innerName, String query, boolean parseSource) throws ElasticsearchException {
				
		log.trace("query: {}", query);
				
		final SearchRequest sr = new SearchRequest();
		
		sr.indices(factory.getIndexName());
		sr.types(factory.getMappingName());
		sr.source(query);
		sr.searchType(searchType);
		
		if (scroll){
			sr.scroll(TimeValue.timeValueMinutes(1));
		}
		
		final SettableFuture<ESearchResult<ENTITY>> f = SettableFuture.create();
		
		esClient.search(sr, new ActionListener<SearchResponse>(){

			@Override
			public void onResponse(SearchResponse response) {
				
				log.trace("Response: {}", response);				
				
				final int total = (int)response.getHits().getTotalHits();
								
				final ESearchResult<ENTITY> ret = new ESearchResult<ENTITY>(factory, response, total, Lists.newArrayList(), innerName == null ? null : Lists.newArrayList(), parseSource ? Lists.newArrayList() : null);
				
				if (total == 0){
					f.set(ret);
					return;
				}

				proccessHits(ret, response, innerName, parseSource ? factory.getEntityClass() : null);
				
				if (scroll && response.getScrollId() !=null){
					f.setFuture(scroll(ret, response.getScrollId(), innerName, parseSource ? factory.getEntityClass() : null));
				}else{
					f.set(ret);
				}				
			}

			@Override
			public void onFailure(Throwable e) {
				f.setException(e);
			}});
		
		return f;
	}


	public void deleteIndex(String indexName){
		
		log.info("Delete index: {}", indexName);
		try {
			esClient.admin().indices().prepareDelete(indexName).execute().actionGet();
		}catch (Exception e){
			log.error("deleteIndex", e);
		}
	}
	    
    private String resourceContent(String name) throws IOException{
    	return IOUtils.toString(ctx.getResource(name).getInputStream());
    }
	
    public void createIndex(String indexName) throws IOException{
    	createIndex(indexName, resourceContent(String.format("classpath:es/%s/_settings.json", indexName)));
    }

	public void createIndex(String indexName, String settings){
		
		log.info("Create index: {}", indexName);
		
		try {
			esClient.admin().indices().prepareCreate(indexName).setSource(settings).execute().actionGet();
		} catch (Exception e) {
			log.error("createIndex", e);
		}								
    }
	
	public void putMapping(String indexName, String typeName) throws IOException{
		putMapping(indexName, typeName, resourceContent(String.format("classpath:es/%s/%s.json", indexName, typeName)));
	}

	public void putMapping(String indexName, String typeName, String mapping){
		
		log.info("Put mapping: {}.{}", indexName, typeName);
		
		try {
			esClient.admin().indices().preparePutMapping(indexName).setType(typeName).setSource(mapping).execute().actionGet();
		} catch (Exception e) {
			log.error("putMapping", e);
		}								
    }

}
