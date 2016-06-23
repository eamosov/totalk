package com.tobox.totalk.controllers.totalk;

import java.util.Collections;
import java.util.Map;

import org.apache.thrift.TException;
import org.everthrift.appserver.transport.http.RpcHttp;
import org.everthrift.appserver.transport.websocket.RpcWebsocket;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.util.concurrent.Futures;
import com.tobox.totalk.controllers.AppThriftController;
import com.tobox.totalk.models.EsService;
import com.tobox.totalk.thrift.TotalkService;
import com.tobox.totalk.thrift.TotalkService.getEntityById_args;
import com.tobox.totalk.thrift.types.ReviewType;
import com.tobox.totalk.thrift.types.Reviews;
import com.tobox.totalk.thrift.types.TotalkEntity;

@RpcHttp
@RpcWebsocket
public class GetEntityByIdController extends AppThriftController<TotalkService.getEntityById_args, TotalkEntity> {

	@Autowired
	private EsService esService;

	@Override
	protected TotalkEntity processRequest() throws TException {
		
		assertNotNullUuid(getEntityById_args._Fields.ID);
		
		return waitForAnswer(Futures.transform(esService.getReviewsCount(Collections.singletonList(args.getId())), (Map<String, Map<ReviewType, Long>> rc) -> {
			final TotalkEntity e = new TotalkEntity();
			e.setId(args.getId());
			
			final Map<ReviewType, Long> reviewsCounts = rc.getOrDefault(args.getId(), Collections.emptyMap());
			
			final Reviews reviews = new Reviews();
			reviews.setTotal(reviewsCounts.getOrDefault(ReviewType.REVIEW, 0L).intValue());
			e.setReviews(reviews);

			final Reviews opinions = new Reviews();
			opinions.setTotal(reviewsCounts.getOrDefault(ReviewType.OPINION, 0L).intValue());
			e.setOpinions(opinions);
			
			return e;
		}));
	}

	@Override
	public void setup(getEntityById_args args) {
		this.isSecured = false;
	}

}
