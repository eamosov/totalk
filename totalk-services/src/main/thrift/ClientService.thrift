namespace java com.tobox.totalk.thrift
namespace cocoa TH
namespace js totalk.thrift

include "types/types.thrift"
include "types/exceptions.thrift"

service ClientService {

	bool onPing(1:string arg);
}
