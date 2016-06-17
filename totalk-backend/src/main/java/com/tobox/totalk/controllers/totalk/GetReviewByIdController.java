package com.tobox.totalk.controllers.totalk;

import java.util.Optional;

import org.apache.thrift.TException;
import org.everthrift.appserver.transport.http.RpcHttp;
import org.everthrift.appserver.transport.websocket.RpcWebsocket;
import org.springframework.beans.factory.annotation.Autowired;

import com.tobox.totalk.controllers.AppThriftController;
import com.tobox.totalk.models.review.ReviewModelFactory;
import com.tobox.totalk.thrift.TotalkService;
import com.tobox.totalk.thrift.TotalkService.getReviewById_args;
import com.tobox.totalk.thrift.exceptions.NoReviewException;
import com.tobox.totalk.thrift.types.Review;

@RpcHttp
@RpcWebsocket
public class GetReviewByIdController extends AppThriftController<TotalkService.getReviewById_args, Review> {

	@Autowired
	private ReviewModelFactory reviewModelFactory;

	@Override
	protected Review processRequest() throws TException {
		return Optional.ofNullable(reviewModelFactory.findEntityById(args.getId())).orElseThrow(() -> new NoReviewException(args.getId()));
	}

	@Override
	public void setup(getReviewById_args args) {
		this.isSecured = false;
	}

}
