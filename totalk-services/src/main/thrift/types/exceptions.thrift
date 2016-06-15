namespace java com.tobox.totalk.thrift.exceptions
namespace js totalk.thrift

exception NoReviewException{
	1:string id
}

exception NoAdvException {
	1:string id
}

exception AuthenticationRequiredException {

}

exception InvalidSessionTokenException {
	1:string msg
}

exception WrappedException {
	1:AuthenticationRequiredException authenticationRequiredException
	2:InvalidSessionTokenException invalidSessionTokenException
}