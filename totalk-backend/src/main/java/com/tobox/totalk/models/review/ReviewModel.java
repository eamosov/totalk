package com.tobox.totalk.models.review;

import java.io.Serializable;

import org.everthrift.appserver.model.CreatedAtIF;
import org.everthrift.appserver.model.DaoEntityIF;
import org.everthrift.appserver.model.UpdatedAtIF;
import org.everthrift.cassandra.com.datastax.driver.mapping.annotations.Table;

import com.tobox.totalk.thrift.types.Review;

@Table(name = "reviews", version="updatedAt", readConsistency="LOCAL_QUORUM", writeConsistency="LOCAL_QUORUM")
public class ReviewModel extends Review implements DaoEntityIF, CreatedAtIF, UpdatedAtIF {

	private static final long serialVersionUID = 1L;

	public ReviewModel() {
		super();
	}

	public ReviewModel(ReviewModel other) {
		super();
		deepCopyFields(other);
	}

	@Override
	public Serializable getPk() {
		return getId();
	}

	@Override
	public void setPk(Serializable identifier) {
		setId((String)identifier);
	}

}
