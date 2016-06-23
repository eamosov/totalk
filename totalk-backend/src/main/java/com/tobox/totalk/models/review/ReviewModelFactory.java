package com.tobox.totalk.models.review;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.everthrift.appserver.model.lazy.AsyncLazyLoader;
import org.everthrift.cassandra.model.OptLockCassandraModelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.tobox.totalk.models.EsProviderIF;
import com.tobox.totalk.models.EsService;
import com.tobox.totalk.thrift.exceptions.NoReviewException;
import com.tobox.totalk.thrift.types.Comments;

@Component("ReviewModelFactory")
public class ReviewModelFactory extends OptLockCassandraModelFactory<String, ReviewModel, NoReviewException> implements EsProviderIF<ReviewModel> {
	
	@Autowired
	private EsService esService;

	public ReviewModelFactory() {
		super((String)null, ReviewModel.class);
	}

	@Override
	protected NoReviewException createNotFoundException(String id) {
		return new NoReviewException();
	}

	@Override
	public Iterator<String> fetchAllIds() {
		return fetchAll("id");
	}

	@Override
	public String getIndexName() {
		return "reviews_index";
	}

	@Override
	public String getMappingName() {
		return "reviews";
	}
	
	private class CommentsLazyLoader implements AsyncLazyLoader<ReviewModel>{

		@Override
		public ListenableFuture<Integer> processAsync(List<ReviewModel> entities) {
			
			if (CollectionUtils.isEmpty(entities))
				return Futures.immediateFuture(0);
			
			return Futures.transform(esService.getCommentsCount(Lists.transform(entities, ReviewModel::getId)), (Map<String, Long> comments) -> {
				for (ReviewModel r:entities){
					final Comments c = new Comments();
					c.setTotal(comments.getOrDefault(r.getId(), 0L).intValue());
					r.setComments(c);
				}
				return comments.size();
			});
		}

	}
	
	public final CommentsLazyLoader commentsCountLoader = new CommentsLazyLoader();

}
