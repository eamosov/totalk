package com.tobox.totalk.models;

import java.io.Serializable;

import com.datastax.driver.mapping.annotations.Table;
import com.knockchat.appserver.model.CreatedAtIF;
import com.knockchat.appserver.model.DaoEntityIF;
import com.knockchat.appserver.model.UpdatedAtIF;
import com.tobox.totalk.thrift.types.Review;

@Table(name = "reviews", version="updatedAt", readConsistency="LOCAL_QUORUM", writeConsistency="LOCAL_QUORUM")
public class ReviewModel extends Review implements DaoEntityIF, CreatedAtIF, UpdatedAtIF{

	private static final long serialVersionUID = 1L;

	public ReviewModel() {
		super();
	}

	public ReviewModel(ReviewModel other) {
		super(other);
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
