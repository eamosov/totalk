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
import com.tobox.totalk.thrift.types.Reviews;

@RpcHttp
@RpcWebsocket
public class GetByEntityController extends AppThriftController<TotalkService.getByEntity_args, Reviews> {

	@Autowired
	private EsService esService;
	
	@Override
	protected Reviews processRequest() throws TException {
		
		assertNotNullUuid(getByEntity_args._Fields.ENTITY_ID);
		assertNotNull(getByEntity_args._Fields.REVIEW_TYPE);

		checkLimitOffset(100, 5000);		
				
		return waitForAnswer(Futures.transform(Futures.transformAsync(esService.findReviewsByEntity(args.getEntityId(), args.getReviewType(), args.getLimit(), args.getOffset()), r-> r.loadEntitiesAsync()), (ESearchResult<ReviewModel> sr) -> {
			return new Reviews(sr.total, args.getLimit(), args.getOffset(), (List)sr.loaded);
		}));		
	}

	@Override
	public void setup(getByEntity_args args) {
		this.isSecured = false;
	}

}
