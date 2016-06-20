package com.tobox.totalk.controllers.totalk;

import java.util.UUID;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.everthrift.appserver.model.OptResult;
import org.everthrift.appserver.transport.http.RpcHttp;
import org.everthrift.appserver.transport.websocket.RpcWebsocket;
import org.springframework.beans.factory.annotation.Autowired;

import com.tobox.entity.Adv;
import com.tobox.services.dao.AdvDAO;
import com.tobox.totalk.controllers.AppThriftController;
import com.tobox.totalk.models.review.ReviewModel;
import com.tobox.totalk.models.review.ReviewModelFactory;
import com.tobox.totalk.thrift.TotalkService;
import com.tobox.totalk.thrift.TotalkService.addReview_args;
import com.tobox.totalk.thrift.exceptions.DeletedException;
import com.tobox.totalk.thrift.exceptions.NoAdvException;
import com.tobox.totalk.thrift.types.Country;
import com.tobox.totalk.thrift.types.EntityType;
import com.tobox.totalk.thrift.types.Review;

@RpcHttp
@RpcWebsocket
public class AddReviewController extends AppThriftController<TotalkService.addReview_args, Review> {
	
	@Autowired
	private AdvDAO advDAO;
	
	@Autowired
	private ReviewModelFactory reviewModelFactory;

	@Override
	protected Review processRequest() throws TException {
		
		//TODO  проверить, что пользователь не анонимус
		
		final Review _review = args.getReview();
		if (_review == null)
			throw new TApplicationException("invalid arguments");
		
		final ReviewModel review = new ReviewModel();
		review.setEntityType(EntityType.ADV);
		
		if (_review.getEntityId() == null)
			throw new TApplicationException("invalid arguments: review.entityId");
		
		final Adv adv = advDAO.findById(UUID.fromString(_review.getEntityId()));
		
		if (adv == null)
			throw new NoAdvException(_review.getEntityId());
		
		if (adv.isDeleted())
			throw new DeletedException(adv.getId().toString());
		
		//TODO проверить свойства товара (не удален, не скрыт, ...)
		
		review.setEntityId(_review.getEntityId());
		
		if (adv.getCategoryId() !=null)
			review.setCategoryId(adv.getCategoryId());
		
		if (adv.getCountry() !=null)
			review.setCountry(Country.valueOf(adv.getCountry().name()));
		
		review.setCreatorId(session.getUserId());
		
		review.setCommentsAllowed(_review.isSetCommentsAllowed() ? _review.isCommentsAllowed(): true);
		
		if (!_review.isSetType())
			throw new TApplicationException("invalid arguments: review.type");
		
		//TODO какие ограничение на type?
		review.setType(_review.getType());
		
		//TODO валидировать title
		review.setTitle(_review.getTitle());

		//TODO валидировать body
		review.setBody(_review.getBody());
		review.setId(UUID.randomUUID().toString());
		
		final OptResult<ReviewModel> or = reviewModelFactory.fastInsert(review);
		
		return or.afterUpdate;
	}

	@Override
	public void setup(addReview_args args) {
		
	}

}
