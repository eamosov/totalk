namespace java com.tobox.totalk.thrift.types
namespace cocoa TH
namespace js totalk.thrift

enum ReviewType {
	OPINION,
	REVIEW
}

enum EntityType {
	ADV
}

enum Country {
    RU,
    CN
}

struct Comment {
	1:string id
	2:string reviewId
	3:bool deleted
	4:i64 deletedAt
	
	5:string creatorId			//UUID
	6:string createdAt
} 

struct Review {
	1:string id					//UUID
	2:ReviewType type
	3:bool deleted
	4:i64 deletedAt
	
	5:EntityType entityType
	6:string entityId			//UUID товара
	7:i32 categoryId			//Категоря товара  ( Нужно учеть, что категория может быть изменена)
	8:Country country			//Страна товара
	
	9:string creatorId			//UUID
	10:i64 createdAt
	11:i64 updatedAt
	
	12:bool commentsAllowed
	
	13:string title
	14:string body
	//15:list<string> photos	??
	
	16:i32 votesYes				//Полезно ДА
	17:i32 votesNo				//Полезно НЕТ
	
	18:list<Comment> comments	//Первая страница комментариев
	19:i32 reviewsCount			//сколько всего отзывов об этом товаре
	20:bool myVote		//"Полезно", поставленный текущим пользователем
	
}
