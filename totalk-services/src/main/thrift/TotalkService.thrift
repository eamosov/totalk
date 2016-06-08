namespace java com.tobox.totalk.thrift
namespace cocoa TH
namespace js totalk.thrift

include "types/types.thrift"
include "types/exceptions.thrift"

service TotalkService {

	string hello(1:string arg) throws (1:exceptions.AuthenticationRequiredException authenticationRequiredException);
	
	void ping(1:string arg) throws (1:exceptions.AuthenticationRequiredException authenticationRequiredException);
		
	types.Review getReviewById(1:string id) throws (1:exceptions.NoReviewException noReviewException);
	
	/**
	*	Показать ленту отзывов по дате создания
	*/
	list<types.Review> getByCreatedAt(1:types.Country country, 2:i32 categoryId, 3:i32 offset, 4:i32 limit);
	
	/**
	*	Показать ленту отзывов по популярности
	*/
	list<types.Review> getByPopularity(1:types.Country country, 2:i32 categoryId, 3:i32 offset, 4:i32 limit, 5:i64 arg3);
				
	/**
	*	Показать ленту подписок пользователя userId
	*/
	list<types.Review> getSubscription(1:string userId, 2:i32 categoryId);
	
	/**
	*	Получить список комментариев к отзыву
	*/
	list<types.Comment> getComments(1:string reviewId, 2:i32 offset, 3:i32 limit) throws (1:exceptions.NoReviewException noReviewException);
	
	/**
	*	Получить список отзывов о товаре
	*/
	list<types.Review> getByEntity(1:types.EntityType entityType, 2:string entityId, 3:types.ReviewType reviewType, 4:i32 offset, 5:i32 limit);
	
	/**
	*	Отзыв полезный: ДА/НЕТ ?
	*/
	void setVote(1:string reviewId, 2:bool yes) throws (1:exceptions.NoReviewException noReviewException);
		
	/**
	*	Добавить обзор
	*/
	types.Review addReview(1:types.Review review)
	
	/**
	*	Добавить комментарий
	*/
	types.Comment addComment(1:types.Comment comment)	 
}
