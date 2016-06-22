package com.tobox.totalk.models.review;

import java.util.Iterator;

import org.everthrift.cassandra.model.OptLockCassandraModelFactory;
import org.springframework.stereotype.Component;

import com.tobox.totalk.models.EsProviderIF;
import com.tobox.totalk.thrift.exceptions.NoReviewException;

@Component("ReviewModelFactory")
public class ReviewModelFactory extends OptLockCassandraModelFactory<String, ReviewModel, NoReviewException> implements EsProviderIF<ReviewModel> {

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

}
