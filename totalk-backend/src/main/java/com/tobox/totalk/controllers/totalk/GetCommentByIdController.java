package com.tobox.totalk.controllers.totalk;

import java.util.Optional;

import org.apache.thrift.TException;
import org.everthrift.appserver.transport.http.RpcHttp;
import org.everthrift.appserver.transport.websocket.RpcWebsocket;
import org.springframework.beans.factory.annotation.Autowired;

import com.tobox.totalk.controllers.AppThriftController;
import com.tobox.totalk.models.review.CommentModel;
import com.tobox.totalk.models.review.CommentModelFactory;
import com.tobox.totalk.thrift.TotalkService;
import com.tobox.totalk.thrift.TotalkService.getCommentById_args;
import com.tobox.totalk.thrift.exceptions.DeletedException;
import com.tobox.totalk.thrift.exceptions.NoCommentException;
import com.tobox.totalk.thrift.types.Comment;

@RpcHttp
@RpcWebsocket
public class GetCommentByIdController extends AppThriftController<TotalkService.getCommentById_args, Comment> {


	@Autowired
	private CommentModelFactory commentModelFactory;

	@Override
	protected Comment processRequest() throws TException {
		
		assertNotNull(getCommentById_args._Fields.ID);
		
		final CommentModel comment = Optional.ofNullable(commentModelFactory.findEntityById(args.getId())).orElseThrow(() -> new NoCommentException(args.getId()));
		
		if (comment.isDeleted())
			throw new DeletedException(comment.getId());
		
		return comment;		
	}

	@Override
	public void setup(getCommentById_args args) {
		this.isSecured = false;
	}


}
