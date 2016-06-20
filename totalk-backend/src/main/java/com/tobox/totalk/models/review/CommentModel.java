package com.tobox.totalk.models.review;

import java.io.Serializable;

import org.everthrift.appserver.model.CreatedAtIF;
import org.everthrift.appserver.model.DaoEntityIF;
import org.everthrift.appserver.model.UpdatedAtIF;
import org.everthrift.cassandra.com.datastax.driver.mapping.annotations.Table;

import com.tobox.totalk.models.EsIndexableIF;
import com.tobox.totalk.models.EsParentAwareIF;
import com.tobox.totalk.thrift.types.Comment;

@Table(name = "review_comments", version="updatedAt", readConsistency="LOCAL_QUORUM", writeConsistency="LOCAL_QUORUM")
public class CommentModel extends Comment implements DaoEntityIF, CreatedAtIF, UpdatedAtIF, EsIndexableIF, EsParentAwareIF{

	private static final long serialVersionUID = 1L;

	public CommentModel() {
		super();
	}

	public CommentModel(CommentModel other) {
		super();
		super.deepCopyFields(other);
	}

	public CommentModel(String id, String reviewId, boolean deleted, long deletedAt, String creatorId, long createdAt,
			long updatedAt, String body) {
		super(id, reviewId, deleted, deletedAt, creatorId, createdAt, updatedAt, body);
		// TODO Auto-generated constructor stub
	}

	@Override
	public long getVersion() {
		return getUpdatedAt();
	}

	@Override
	public Serializable getPk() {
		return getId();
	}

	@Override
	public void setPk(Serializable identifier) {
		setId((String)identifier);		
	}


	@Override
	public String getEsParent() {
		return getReviewId();
	}

}
