package com.tobox.totalk.models.review;

import java.util.Iterator;

import org.everthrift.cassandra.model.OptLockCassandraModelFactory;
import org.springframework.stereotype.Component;

import com.tobox.totalk.models.EsProviderIF;
import com.tobox.totalk.thrift.exceptions.NoCommentException;

@Component("CommentModelFactory")
public class CommentModelFactory extends OptLockCassandraModelFactory<String, CommentModel, NoCommentException>  implements EsProviderIF<CommentModel>{

	public CommentModelFactory() {
		super((String)null, CommentModel.class);
	}

	@Override
	protected NoCommentException createNotFoundException(String id) {
		return new NoCommentException(id);
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
		return "comments";
	}

}
