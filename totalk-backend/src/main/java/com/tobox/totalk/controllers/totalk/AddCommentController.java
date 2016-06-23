package com.tobox.totalk.controllers.totalk;

import java.util.UUID;

import org.apache.thrift.TException;
import org.everthrift.appserver.model.OptResult;
import org.everthrift.appserver.transport.http.RpcHttp;
import org.everthrift.appserver.transport.websocket.RpcWebsocket;
import org.springframework.beans.factory.annotation.Autowired;

import com.tobox.totalk.controllers.AppThriftController;
import com.tobox.totalk.models.review.CommentModel;
import com.tobox.totalk.models.review.CommentModelFactory;
import com.tobox.totalk.models.review.ReviewModel;
import com.tobox.totalk.models.review.ReviewModelFactory;
import com.tobox.totalk.thrift.TotalkService;
import com.tobox.totalk.thrift.TotalkService.addComment_args;
import com.tobox.totalk.thrift.exceptions.DeletedException;
import com.tobox.totalk.thrift.exceptions.NoReviewException;
import com.tobox.totalk.thrift.types.Comment;

@RpcHttp
@RpcWebsocket
public class AddCommentController extends AppThriftController<TotalkService.addComment_args, Comment> {

	@Autowired
	private ReviewModelFactory reviewModelFactory;

	@Autowired
	private CommentModelFactory commentModelFactory;

	@Override
	protected Comment processRequest() throws TException {

		assertNotNull(addComment_args._Fields.COMMENT);
		assertNotNullUuid("comment.reviewId", args.getComment().getReviewId());

		final Comment _comment = args.getComment();		
		final ReviewModel review = reviewModelFactory.findEntityById(_comment.getReviewId());
		
		if (review == null)
			throw new NoReviewException(_comment.getReviewId());

		if (review.isDeleted())
			throw new DeletedException(review.getId());
		
		//TODO проверить commentsAllowed
		
		final CommentModel comment = new CommentModel();
		comment.setId(UUID.randomUUID().toString());
		comment.setReviewId(review.getId());
		comment.setCreatorId(session.getUserId());
		
		//TODO валидировать body
		comment.setBody(_comment.getBody());
		
		final OptResult<CommentModel> r = commentModelFactory.fastInsert(comment);
		
		return r.afterUpdate;
	}

	@Override
	public void setup(addComment_args args) {
		
	}

}
