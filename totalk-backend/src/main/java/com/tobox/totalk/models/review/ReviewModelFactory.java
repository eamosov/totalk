package com.tobox.totalk.models.review;

import org.everthrift.cassandra.model.OptLockCassandraModelFactory;
import org.springframework.stereotype.Component;

import com.tobox.totalk.thrift.exceptions.NoReviewException;

@Component
public class ReviewModelFactory extends OptLockCassandraModelFactory<String, ReviewModel, NoReviewException> {

	public ReviewModelFactory() {
		super((String)null, ReviewModel.class);
	}

	@Override
	protected NoReviewException createNotFoundException(String id) {
		return new NoReviewException();
	}

}
