namespace java com.tobox.totalk.thrift.rpc

include "types/types.thrift"

service RpcService {

	list<string> sendPing(1:string userId, 2:list<string> deviceIds, 3:string value);
	
	void setValue(1:string key, 2:string value);
	
	string getValue(1:string key);
}