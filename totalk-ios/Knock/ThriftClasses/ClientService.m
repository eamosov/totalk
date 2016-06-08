/**
 * Autogenerated by Thrift Compiler (1.0.0-dev)
 *
 * DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
 *  @generated
 */

#import <Foundation/Foundation.h>

#import "TProtocol.h"
#import "TApplicationException.h"
#import "TProtocolException.h"
#import "TProtocolUtil.h"
#import "TProcessor.h"
#import "TObjective-C.h"
#import "TBase.h"
#import "TProtocolFactory.h"
#import "TBaseClient.h"
#import "TAsyncRegister.h"
#import "TMemoryBuffer.h"
#import "TBinaryProtocol.h"
#import "TExceptionRegister.h"

#import "types.h"
#import "exceptions.h"

#import "ClientService.h"


@implementation THClientServiceConstants
+ (void) initialize {
}
@end

@interface THClientServiceonPing_args : NSObject <TBase, NSCoding> {
  NSString * __arg;

  BOOL __arg_isset;
}

#if TARGET_OS_IPHONE || (MAC_OS_X_VERSION_MAX_ALLOWED >= MAC_OS_X_VERSION_10_5)
@property (nonatomic, retain, getter=arg, setter=setArg:) NSString * arg;
#endif

- (id) init;
- (id) initWithArg: (NSString *) arg;

- (void) read: (id <TProtocol>) inProtocol;
- (void) write: (id <TProtocol>) outProtocol;

- (void) validate;

#if !__has_feature(objc_arc)
- (NSString *) arg;
- (void) setArg: (NSString *) arg;
#endif
- (BOOL) argIsSet;

@end

@implementation THClientServiceonPing_args

- (id) init
{
  self = [super init];
#if TARGET_OS_IPHONE || (MAC_OS_X_VERSION_MAX_ALLOWED >= MAC_OS_X_VERSION_10_5)
#endif
  return self;
}

- (id) initWithArg: (NSString *) arg
{
  self = [super init];
  __arg = [arg retain_stub];
  __arg_isset = YES;
  return self;
}

- (id) initWithCoder: (NSCoder *) decoder
{
  self = [super init];
  if ([decoder containsValueForKey: @"arg"])
  {
    __arg = [[decoder decodeObjectForKey: @"arg"] retain_stub];
    __arg_isset = YES;
  }
  return self;
}

- (void) encodeWithCoder: (NSCoder *) encoder
{
  if (__arg_isset)
  {
    [encoder encodeObject: __arg forKey: @"arg"];
  }
}

- (NSUInteger) hash
{
  NSUInteger hash = 17;
  hash = (hash * 31) ^ __arg_isset ? 2654435761 : 0;
  if (__arg_isset)
  {
    hash = (hash * 31) ^ [__arg hash];
  }
  return hash;
}

- (BOOL) isEqual: (id) anObject
{
  if (self == anObject) {
    return YES;
  }
  if (![anObject isKindOfClass:[THClientServiceonPing_args class]]) {
    return NO;
  }
  THClientServiceonPing_args *other = (THClientServiceonPing_args *)anObject;
  if ((__arg_isset != other->__arg_isset) ||
      (__arg_isset && ((__arg || other->__arg) && ![__arg isEqual:other->__arg]))) {
    return NO;
  }
  return YES;
}

- (void) dealloc
{
  [__arg release_stub];
  [super dealloc_stub];
}

- (NSString *) arg {
  return [[__arg retain_stub] autorelease_stub];
}

- (void) setArg: (NSString *) arg {
  [arg retain_stub];
  [__arg release_stub];
  __arg = arg;
  __arg_isset = YES;
}

- (BOOL) argIsSet {
  return __arg_isset;
}

- (void) unsetArg {
  [__arg release_stub];
  __arg = nil;
  __arg_isset = NO;
}

- (void) read: (id <TProtocol>) inProtocol
{
  NSString * fieldName;
  int fieldType;
  int fieldID;

  [inProtocol readStructBeginReturningName: NULL];
  while (true)
  {
    [inProtocol readFieldBeginReturningName: &fieldName type: &fieldType fieldID: &fieldID];
    if (fieldType == TType_STOP) { 
      break;
    }
    switch (fieldID)
    {
      case 1:
        if (fieldType == TType_STRING) {
          NSString * fieldValue = [inProtocol readString];
          [self setArg: fieldValue];
        } else { 
          [TProtocolUtil skipType: fieldType onProtocol: inProtocol];
        }
        break;
      default:
        [TProtocolUtil skipType: fieldType onProtocol: inProtocol];
        break;
    }
    [inProtocol readFieldEnd];
  }
  [inProtocol readStructEnd];
}

- (void) write: (id <TProtocol>) outProtocol {
  [outProtocol writeStructBeginWithName: @"onPing_args"];
  if (__arg_isset) {
    if (__arg != nil) {
      [outProtocol writeFieldBeginWithName: @"arg" type: TType_STRING fieldID: 1];
      [outProtocol writeString: __arg];
      [outProtocol writeFieldEnd];
    }
  }
  [outProtocol writeFieldStop];
  [outProtocol writeStructEnd];
}

- (void) validate {
  // check for required fields
}

- (NSString *) description {
  NSMutableString * ms = [NSMutableString stringWithString: @"THonPing_args("];
  [ms appendString: @"arg:"];
  [ms appendFormat: @"\"%@\"", __arg];
  [ms appendString: @")"];
  return [NSString stringWithString: ms];
}

@end

@interface THClientServiceOnPing_result : NSObject <TBase, NSCoding> {
  BOOL __success;

  BOOL __success_isset;
}

#if TARGET_OS_IPHONE || (MAC_OS_X_VERSION_MAX_ALLOWED >= MAC_OS_X_VERSION_10_5)
@property (nonatomic, getter=success, setter=setSuccess:) BOOL success;
#endif

- (id) init;
- (id) initWithSuccess: (BOOL) success;

- (void) read: (id <TProtocol>) inProtocol;
- (void) write: (id <TProtocol>) outProtocol;

- (void) validate;

#if !__has_feature(objc_arc)
- (BOOL) success;
- (void) setSuccess: (BOOL) success;
#endif
- (BOOL) successIsSet;

@end

@implementation THClientServiceOnPing_result

- (id) init
{
  self = [super init];
#if TARGET_OS_IPHONE || (MAC_OS_X_VERSION_MAX_ALLOWED >= MAC_OS_X_VERSION_10_5)
#endif
  return self;
}

- (id) initWithSuccess: (BOOL) success
{
  self = [super init];
  __success = success;
  __success_isset = YES;
  return self;
}

- (id) initWithCoder: (NSCoder *) decoder
{
  self = [super init];
  if ([decoder containsValueForKey: @"success"])
  {
    __success = [decoder decodeBoolForKey: @"success"];
    __success_isset = YES;
  }
  return self;
}

- (void) encodeWithCoder: (NSCoder *) encoder
{
  if (__success_isset)
  {
    [encoder encodeBool: __success forKey: @"success"];
  }
}

- (NSUInteger) hash
{
  NSUInteger hash = 17;
  hash = (hash * 31) ^ __success_isset ? 2654435761 : 0;
  if (__success_isset)
  {
    hash = (hash * 31) ^ [@(__success) hash];
  }
  return hash;
}

- (BOOL) isEqual: (id) anObject
{
  if (self == anObject) {
    return YES;
  }
  if (![anObject isKindOfClass:[THClientServiceOnPing_result class]]) {
    return NO;
  }
  THClientServiceOnPing_result *other = (THClientServiceOnPing_result *)anObject;
  if ((__success_isset != other->__success_isset) ||
      (__success_isset && (__success != other->__success))) {
    return NO;
  }
  return YES;
}

- (void) dealloc
{
  [super dealloc_stub];
}

- (BOOL) success {
  return __success;
}

- (void) setSuccess: (BOOL) success {
  __success = success;
  __success_isset = YES;
}

- (BOOL) successIsSet {
  return __success_isset;
}

- (void) unsetSuccess {
  __success_isset = NO;
}

- (void) read: (id <TProtocol>) inProtocol
{
  NSString * fieldName;
  int fieldType;
  int fieldID;

  [inProtocol readStructBeginReturningName: NULL];
  while (true)
  {
    [inProtocol readFieldBeginReturningName: &fieldName type: &fieldType fieldID: &fieldID];
    if (fieldType == TType_STOP) { 
      break;
    }
    switch (fieldID)
    {
      case 0:
        if (fieldType == TType_BOOL) {
          BOOL fieldValue = [inProtocol readBool];
          [self setSuccess: fieldValue];
        } else { 
          [TProtocolUtil skipType: fieldType onProtocol: inProtocol];
        }
        break;
      default:
        [TProtocolUtil skipType: fieldType onProtocol: inProtocol];
        break;
    }
    [inProtocol readFieldEnd];
  }
  [inProtocol readStructEnd];
}

- (void) write: (id <TProtocol>) outProtocol {
  [outProtocol writeStructBeginWithName: @"OnPing_result"];

  if (__success_isset) {
    [outProtocol writeFieldBeginWithName: @"success" type: TType_BOOL fieldID: 0];
    [outProtocol writeBool: __success];
    [outProtocol writeFieldEnd];
  }
  [outProtocol writeFieldStop];
  [outProtocol writeStructEnd];
}

- (void) validate {
  // check for required fields
}

- (NSString *) description {
  NSMutableString * ms = [NSMutableString stringWithString: @"THOnPing_result("];
  [ms appendString: @"success:"];
  [ms appendFormat: @"%i", __success];
  [ms appendString: @")"];
  return [NSString stringWithString: ms];
}

@end

@implementation THClientServiceClient
- (id) initWithProtocol: (id <TProtocol>) protocol
{
  return [self initWithInProtocol: protocol outProtocol: protocol];
}

- (id) initWithInProtocol: (id <TProtocol>) anInProtocol outProtocol: (id <TProtocol>) anOutProtocol
{
  self = [super init];
  inProtocol = [anInProtocol retain_stub];
  outProtocol = [anOutProtocol retain_stub];
  return self;
}

- (void) send_onPing: (NSString *) arg
{
  [outProtocol writeMessageBeginWithName: @"ClientService:onPing" type: TMessageType_CALL sequenceID: 0];
  [outProtocol writeStructBeginWithName: @"onPing_args"];
  if (arg != nil)  {
    [outProtocol writeFieldBeginWithName: @"arg" type: TType_STRING fieldID: 1];
    [outProtocol writeString: arg];
    [outProtocol writeFieldEnd];
  }
  [outProtocol writeFieldStop];
  [outProtocol writeStructEnd];
  [outProtocol writeMessageEnd];
}

- (BOOL) recv_onPing
{
  TApplicationException * x = [self checkIncomingMessageException];
  if (x != nil)  {
    @throw x;
  }
  THClientServiceOnPing_result * result = [[[THClientServiceOnPing_result alloc] init] autorelease_stub];
  [result read: inProtocol];
  [inProtocol readMessageEnd];
  if ([result successIsSet]) {
    return [result success];
  }
  @throw [TApplicationException exceptionWithType: TApplicationException_MISSING_RESULT
                                           reason: @"onPing failed: unknown result"];
}

- (BOOL) onPing: (NSString *) arg
{
  [self send_onPing : arg];
  [[outProtocol transport] flush];
  return [self recv_onPing];
}

@end

@implementation THClientServiceProcessor

- (id) initWithClientService: (id <THClientService>) service
{
self = [super init];
if (!self) {
  return nil;
}
mService = [service retain_stub];
mMethodMap = [[NSMutableDictionary dictionary] retain_stub];
{
  SEL s = @selector(process_onPing_withSequenceID:inProtocol:outProtocol:);
  NSMethodSignature * sig = [self methodSignatureForSelector: s];
  NSInvocation * invocation = [NSInvocation invocationWithMethodSignature: sig];
  [invocation setSelector: s];
  [invocation retainArguments];
  [mMethodMap setValue: invocation forKey: @"ClientService:onPing"];
}
return self;
}

- (id<THClientService>) service
{
  return [[mService retain_stub] autorelease_stub];
}

- (BOOL) processOnInputProtocol: (id <TProtocol>) inProtocol
                 outputProtocol: (id <TProtocol>) outProtocol
{
  NSString * messageName;
  int messageType;
  int seqID;
  [inProtocol readMessageBeginReturningName: &messageName
                                       type: &messageType
                                 sequenceID: &seqID];
  NSInvocation * invocation = [mMethodMap valueForKey: messageName];
  if (invocation == nil) {
    [TProtocolUtil skipType: TType_STRUCT onProtocol: inProtocol];
    [inProtocol readMessageEnd];
    TApplicationException * x = [TApplicationException exceptionWithType: TApplicationException_UNKNOWN_METHOD reason: [NSString stringWithFormat: @"Invalid method name: '%@'", messageName]];
    [outProtocol writeMessageBeginWithName: messageName
                                      type: TMessageType_EXCEPTION
                                sequenceID: seqID];
    [x write: outProtocol];
    [outProtocol writeMessageEnd];
    [[outProtocol transport] flush];
    return YES;
  }
  // NSInvocation does not conform to NSCopying protocol
  NSInvocation * i = [NSInvocation invocationWithMethodSignature: [invocation methodSignature]];
  [i setSelector: [invocation selector]];
  [i setArgument: &seqID atIndex: 2];
  [i setArgument: &inProtocol atIndex: 3];
  [i setArgument: &outProtocol atIndex: 4];
  [i setTarget: self];
  [i invoke];
  return YES;
}

- (void) process_onPing_withSequenceID: (int32_t) seqID inProtocol: (id<TProtocol>) inProtocol outProtocol: (id<TProtocol>) outProtocol
{
THClientServiceonPing_args * args = [[THClientServiceonPing_args alloc] init];
[args read: inProtocol];
[inProtocol readMessageEnd];
THClientServiceOnPing_result * result = [[THClientServiceOnPing_result alloc] init];
[result setSuccess: [mService onPing: [args arg]]];
[outProtocol writeMessageBeginWithName: @"ClientService:onPing"
                                  type: TMessageType_REPLY
                            sequenceID: seqID];
[result write: outProtocol];
[outProtocol writeMessageEnd];
[[outProtocol transport] flush];
[result release_stub];
[args release_stub];
}

- (void) dealloc
{
[mService release_stub];
[mMethodMap release_stub];
[super dealloc_stub];
}

@end

@implementation THClientServiceAsyncClient
- (id)initWithOutProtocol:(id<TProtocol>)anOutProtocol inProtocolFactory:(id<TProtocolFactory>)anInProtocolFactory
{
self = [super init];
outProtocol = [anOutProtocol retain_stub];
inProtocolFactory = [anInProtocolFactory retain_stub];
return self;
}

- (void) dealloc
{
[outProtocol release_stub];
[super dealloc_stub];
}

- (void) send_onPing: (NSString *) arg success: (void (^)(BOOL result)) success error: (void (^)(NSException *exception)) error
{
int seqID = [[TAsyncRegister sharedRegister] getNextSeqID];
[outProtocol writeMessageBeginWithName: @"ClientService:onPing" type: TMessageType_CALL sequenceID: seqID];
[outProtocol writeStructBeginWithName: @"onPing_args"];
if (arg != nil){
  [outProtocol writeFieldBeginWithName: @"arg" type: TType_STRING fieldID: 1];
  [outProtocol writeString: arg];
  [outProtocol writeFieldEnd];
}
[outProtocol writeFieldStop];
[outProtocol writeStructEnd];
[outProtocol writeMessageEnd];
[[TAsyncRegister sharedRegister] registerSeqID:seqID handler:^(NSData *data, NSException *exception)
{
  id <TProtocol> protocol = [inProtocolFactory newProtocolOnTransport:[[TMemoryBuffer alloc] initWithData:data]];
  if (exception)
  {
    if(error) error(exception);
  }
  else
  {
    @try {
      BOOL result = [self recv_onPing:protocol];
      if(success) success(result);
    }
    @catch (NSException *e) {
      [[TExceptionRegister sharedRegister] throwException:e];
      if(error) error(e);
    }
  }
}
];
[[outProtocol transport] flush:seqID];
}

- (BOOL) recv_onPing:(id <TProtocol>)inProtocol
{
int msgType = 0;
[inProtocol readMessageBeginReturningName: nil type: &msgType sequenceID: NULL];
if (msgType == TMessageType_EXCEPTION) {
  TApplicationException * x = [TApplicationException read: inProtocol];
  [inProtocol readMessageEnd];
  @throw x;
}
THClientServiceOnPing_result * result = [[[THClientServiceOnPing_result alloc] init] autorelease_stub];
[result read: inProtocol];
[inProtocol readMessageEnd];
if ([result successIsSet]) {
  return [result success];
}
@throw [TApplicationException exceptionWithType: TApplicationException_MISSING_RESULT
                                         reason: @"onPing failed: unknown result"];
}

- (void) onPing: (NSString *) arg success: (void (^)(BOOL result)) success error: (void (^)(NSException *exception)) error
{
dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
  @try {
    [self send_onPing : arg success: success error:error];
  }
  @catch (NSException *exception) {
    if(error) error(exception);
  }
});
}

@end
