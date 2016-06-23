package com.tobox.totalk.models;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.everthrift.utils.LongTimestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.tobox.totalk.models.review.CommentModel;
import com.tobox.totalk.models.review.CommentModelFactory;
import com.tobox.totalk.models.review.ReviewModel;
import com.tobox.totalk.models.review.ReviewModelFactory;
import com.tobox.totalk.thrift.types.ReviewType;

@Component("esService")
public class EsService extends BasicEsService{
	
	private static final Logger log = LoggerFactory.getLogger(EsService.class);

	@Autowired
	private JsQueryBuilder jsQueryBuilder;
	
	@Autowired
	private ReviewModelFactory reviewModelFactory;

	@Autowired
	private CommentModelFactory commentModelFactory;

	public EsService() {
		
	}
		
	public long getNow(){
		return LongTimestamp.round100sec(System.currentTimeMillis());		
	}

	public ListenableFuture<ESearchResult<ReviewModel>> findReviewsByEntity(String entityId, ReviewType type, int limit, int offset){		
		return searchQueryAsync(reviewModelFactory, SearchType.DFS_QUERY_THEN_FETCH, false, null, jsQueryBuilder.getQuery("findReviewsByEntity", entityId, type.name(), limit, offset), false);
	}

	public ListenableFuture<ESearchResult<CommentModel>> findCommentsByReview(String reviewId, int limit, int offset){		
		return searchQueryAsync(commentModelFactory, SearchType.DFS_QUERY_THEN_FETCH, false, null, jsQueryBuilder.getQuery("findCommentsByReview", reviewId, limit, offset), false);
	}
	
	public ListenableFuture<Map<String, Long>> getCommentsCount(List<String> reviewIds){
		final ListenableFuture<ESearchResult<CommentModel>> srf = searchQueryAsync(commentModelFactory, SearchType.COUNT, false, null, jsQueryBuilder.getQuery("getCommentsCount", reviewIds), false);
		
		return Futures.transform(srf, (ESearchResult<CommentModel> sr) -> {
			final Map<String, Long> result = new HashMap<>(reviewIds.size());
			final Terms comments = sr.response.getAggregations().get("comments");
			for (Bucket b: comments.getBuckets()){
				result.put(b.getKey(), b.getDocCount());
			}
			return result;			
		});		
	}

	public ListenableFuture<Map<String, Map<ReviewType, Long>>> getReviewsCount(List<String> entityIds){
		final ListenableFuture<ESearchResult<ReviewModel>> srf =  searchQueryAsync(reviewModelFactory, SearchType.COUNT, false, null, jsQueryBuilder.getQuery("getReviewsCount", entityIds), false);
		
		return Futures.transform(srf, (ESearchResult<ReviewModel> sr) -> {
			final Map<String, Map<ReviewType, Long>> result = new HashMap<>(entityIds.size());
			final Terms comments = sr.response.getAggregations().get("entities");
			for (Bucket b: comments.getBuckets()){			
				final Terms reviewTypes = b.getAggregations().get("reviewTypes");
				final Map<ReviewType, Long> byType = new HashMap<>(ReviewType.values().length);
				result.put(b.getKey(), byType);
				for (Bucket t: reviewTypes.getBuckets()){
					byType.put(ReviewType.valueOf(t.getKey()), t.getDocCount());				
				}			
			}
			return result;
		});
	}

}
