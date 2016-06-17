package com.tobox.totalk.models;

import org.elasticsearch.action.search.SearchType;
import org.everthrift.utils.LongTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.ListenableFuture;
import com.tobox.totalk.models.review.ReviewModel;
import com.tobox.totalk.models.review.ReviewModelFactory;
import com.tobox.totalk.thrift.types.EntityType;
import com.tobox.totalk.thrift.types.ReviewType;

@Component("esService")
public class EsService extends BasicEsService{
	
	private static final Logger log = LoggerFactory.getLogger(EsService.class);

	@Autowired
	private JsQueryBuilder jsQueryBuilder;
	
	@Autowired
	private ReviewModelFactory reviewModelFactory;
       
	public EsService() {
		
	}
		
	public long getNow(){
		return LongTimestamp.round100sec(System.currentTimeMillis());		
	}

	public ListenableFuture<ESearchResult<ReviewModel>> findReviewsByEntity(String entityId, EntityType entityType, ReviewType type, int limit, int offset){		
		return searchQueryAsync(reviewModelFactory, SearchType.DFS_QUERY_THEN_FETCH, false, null, jsQueryBuilder.getQuery("findReviewsByEntity", entityId, entityType.name(), type.name(), limit, offset), false);
	}

}
