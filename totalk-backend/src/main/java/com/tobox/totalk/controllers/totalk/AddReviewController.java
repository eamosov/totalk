package com.tobox.totalk.controllers.totalk;

import java.util.UUID;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;

import com.knockchat.appserver.jgroups.RpcJGroups;
import com.knockchat.appserver.transport.http.RpcHttp;
import com.knockchat.appserver.transport.websocket.RpcWebsocket;
import com.tobox.totalk.controllers.AppThriftController;
import com.tobox.totalk.models.ReviewModel;
import com.tobox.totalk.models.ReviewModelFactory;
import com.tobox.totalk.thrift.TotalkService;
import com.tobox.totalk.thrift.TotalkService.addReview_args;
import com.tobox.totalk.thrift.types.Review;
import com.tobox.totalk.thrift.types.Review._Fields;

@RpcJGroups
@RpcHttp
@RpcWebsocket
public class AddReviewController extends AppThriftController<TotalkService.addReview_args, ReviewModel> {
	
	private static final _Fields [] userFields =
			new _Fields[]{_Fields.TYPE, _Fields.ENTITY_TYPE, _Fields.ENTITY_ID, _Fields.CATEGORY_ID, _Fields.COUNTRY, _Fields.CREATOR_ID, _Fields.COMMENTS_ALLOWED, _Fields.TITLE, _Fields.BODY}; 

	@Autowired
	private ReviewModelFactory reviewModelFactory;

	@Override
	public void setup(addReview_args args) {
		isSecured = false;
	}
	
	@Override
	protected ReviewModel processRequest() throws TException {
		ReviewModel r = new ReviewModel();
		final Review _r = args.getReview();
		if (_r == null)
			throw new TApplicationException("invalid arguments");
		
		for (_Fields f: userFields){
			if (_r.isSet(f))
				r.setFieldValue(f, _r.getFieldValue(f));
		}
		
		r.setId(UUID.randomUUID().toString());
		
		return reviewModelFactory.fastInsert(r).afterUpdate;
	}

}
