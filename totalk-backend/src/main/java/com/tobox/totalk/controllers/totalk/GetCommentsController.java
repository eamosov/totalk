package com.tobox.totalk.controllers.totalk;

import java.util.List;

import org.apache.thrift.TException;
import org.everthrift.appserver.transport.http.RpcHttp;
import org.everthrift.appserver.transport.websocket.RpcWebsocket;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.util.concurrent.Futures;
import com.tobox.totalk.controllers.AppThriftController;
import com.tobox.totalk.models.BasicEsService.ESearchResult;
import com.tobox.totalk.models.EsService;
import com.tobox.totalk.models.review.CommentModel;
import com.tobox.totalk.thrift.TotalkService;
import com.tobox.totalk.thrift.TotalkService.getComments_args;
import com.tobox.totalk.thrift.types.Comments;

@RpcHttp
@RpcWebsocket
public class GetCommentsController extends AppThriftController<TotalkService.getComments_args, Comments> {

	@Autowired
	private EsService esService;

	@Override
	protected Comments processRequest() throws TException {

		assertNotNullUuid(getComments_args._Fields.REVIEW_ID);
		checkLimitOffset(100, 5000);
		
		return waitForAnswer(Futures.transform(Futures.transformAsync(esService.findCommentsByReview(args.getReviewId(), args.getLimit(), args.getOffset()), r-> r.loadEntitiesAsync()), (ESearchResult<CommentModel> sr) -> {
			return new Comments(sr.total, args.getLimit(), args.getOffset(), (List)sr.loaded);
		}));		
	}

	@Override
	public void setup(getComments_args args) {
		this.isSecured = false;		
	}

}
