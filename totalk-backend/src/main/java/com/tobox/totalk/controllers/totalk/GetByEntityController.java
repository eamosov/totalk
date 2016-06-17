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
import com.tobox.totalk.models.review.ReviewModel;
import com.tobox.totalk.thrift.TotalkService;
import com.tobox.totalk.thrift.TotalkService.getByEntity_args;

@RpcHttp
@RpcWebsocket
public class GetByEntityController extends AppThriftController<TotalkService.getByEntity_args, List<ReviewModel>> {

	@Autowired
	private EsService esService;
	
	@Override
	protected List<ReviewModel> processRequest() throws TException {		
		return waitForAnswer(Futures.transformAsync(esService.findReviewsByEntity(args.getEntityId(), args.getEntityType(), args.getReviewType(), args.getLimit(), args.getOffset()), ESearchResult::loadEntitiesAsync));		
	}

	@Override
	public void setup(getByEntity_args args) {
		this.isSecured = false;
	}

}
