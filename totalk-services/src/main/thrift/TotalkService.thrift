namespace java com.tobox.totalk.thrift
namespace cocoa TH
namespace js totalk.thrift

include "types/types.thrift"
include "types/exceptions.thrift"

service TotalkService {
		
	types.Review getReviewById(1:string id) throws (1:exceptions.WrappedException wrappedException);
	
	/**
	*	Показать ленту отзывов по дате создания
	*/
	list<types.Review> getByCreatedAt(1:types.Country country, 2:i32 categoryId, 3:i32 offset, 4:i32 limit) throws (1:exceptions.WrappedException wrappedException);
	
	/**
	*	Показать ленту отзывов по популярности
	*/
	list<types.Review> getByPopularity(1:types.Country country, 2:i32 categoryId, 3:i32 offset, 4:i32 limit, 5:i64 arg3) throws (1:exceptions.WrappedException wrappedException);
				
	/**
	*	Показать ленту подписок пользователя userId
	*/
	list<types.Review> getSubscription(1:string userId, 2:i32 categoryId) throws (1:exceptions.WrappedException wrappedException);
	
	/**
	*	Получить список комментариев к отзыву
	*/
	list<types.Comment> getComments(1:string reviewId, 2:i32 offset, 3:i32 limit) throws (1:exceptions.WrappedException wrappedException, 2:exceptions.NoReviewException noReviewException);
	
	/**
	*	Получить список отзывов о товаре
	*/
	list<types.Review> getByEntity(1:types.EntityType entityType, 2:string entityId, 3:types.ReviewType reviewType, 4:i32 offset, 5:i32 limit) throws (1:exceptions.WrappedException wrappedException);
	
	/**
	*	Отзыв полезный: ДА/НЕТ ?
	*/
	void setVote(1:string reviewId, 2:bool yes) throws (1:exceptions.WrappedException wrappedException, 2:exceptions.NoReviewException noReviewException);
		
	/**
	*	Добавить обзор
	*/
	types.Review addReview(1:types.Review review) throws (1:exceptions.WrappedException wrappedException, 2:exceptions.NoAdvException noAdvException);
	
	/**
	*	Добавить комментарий
	*/
	types.Comment addComment(1:types.Comment comment) throws (1:exceptions.WrappedException wrappedException);	 
}
