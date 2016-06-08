package com.tobox.totalk.models;

import org.springframework.stereotype.Component;

import com.knockchat.cassandra.model.OptLockCassandraModelFactory;
import com.tobox.totalk.thrift.exceptions.NoReviewException;

@Component
public class ReviewModelFactory extends OptLockCassandraModelFactory<String, ReviewModel, NoReviewException> {

	public ReviewModelFactory() {
		super((String)null, ReviewModel.class);
	}

	@Override
	protected NoReviewException createNotFoundException(String id) {
		return new NoReviewException(id);
	}

}
