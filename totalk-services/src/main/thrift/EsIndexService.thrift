namespace java com.tobox.totalk.thrift.rpc

include "types/types.thrift"

enum EsOp {
	INDEX,
	DELETE
}

struct IndexTask{
	1:EsOp operation
	2:string indexName
	3:string mappingName
	4:i32 versionType
	5:i64 version	
	6:string id		
	7:string source
	8:string parentId
}

service EsIndexService {

	void index(1:list<IndexTask> tasks);
		
}