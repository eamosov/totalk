package com.tobox.totalk.controllers.totalk;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;

import com.knockchat.appserver.jgroups.RpcJGroups;
import com.knockchat.appserver.transport.http.RpcHttp;
import com.knockchat.appserver.transport.websocket.RpcWebsocket;
import com.tobox.totalk.controllers.AppThriftController;
import com.tobox.totalk.models.ReviewModel;
import com.tobox.totalk.models.ReviewModelFactory;
import com.tobox.totalk.thrift.TotalkService;
import com.tobox.totalk.thrift.TotalkService.getReviewById_args;
import com.tobox.totalk.thrift.exceptions.NoReviewException;

@RpcJGroups
@RpcHttp
@RpcWebsocket
public class GetReviewByIdController extends AppThriftController<TotalkService.getReviewById_args, ReviewModel> {

	@Autowired
	private ReviewModelFactory reviewModelFactory;

	@Override
	public void setup(getReviewById_args args) {
		isSecured = false;
	}

	@Override
	protected ReviewModel processRequest() throws TException {
		
		final ReviewModel r = reviewModelFactory.findEntityById(args.getId());
		if (r == null)
			throw new NoReviewException(args.getId());
		
		return r;			
	}

}
