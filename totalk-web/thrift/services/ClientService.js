//
// Autogenerated by Thrift Compiler (1.0.0-dev)
//
// DO NOT EDIT UNLESS YOU ARE SURE THAT YOU KNOW WHAT YOU ARE DOING
//


//HELPER FUNCTIONS AND STRUCTURES

totalk.thrift.ClientService_onPing_args = function(args) {
  this.arg = null;
  if (args) {
    if (args.arg !== undefined && args.arg !== null) {
      this.arg = args.arg;
    }
  }
};
totalk.thrift.ClientService_onPing_args.prototype = {};
totalk.thrift.ClientService_onPing_args.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 1:
      if (ftype == Thrift.Type.STRING) {
        this.arg = input.readString().value;
      } else {
        input.skip(ftype);
      }
      break;
      case 0:
        input.skip(ftype);
        break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

totalk.thrift.ClientService_onPing_args.prototype.write = function(output) {
  output.writeStructBegin('ClientService_onPing_args');
  if (this.arg !== null && this.arg !== undefined) {
    output.writeFieldBegin('arg', Thrift.Type.STRING, 1);
    output.writeString(this.arg);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

totalk.thrift.ClientService_onPing_result = function(args) {
  this.success = null;
  if (args) {
    if (args.success !== undefined && args.success !== null) {
      this.success = args.success;
    }
  }
};
totalk.thrift.ClientService_onPing_result.prototype = {};
totalk.thrift.ClientService_onPing_result.prototype.read = function(input) {
  input.readStructBegin();
  while (true)
  {
    var ret = input.readFieldBegin();
    var fname = ret.fname;
    var ftype = ret.ftype;
    var fid = ret.fid;
    if (ftype == Thrift.Type.STOP) {
      break;
    }
    switch (fid)
    {
      case 0:
      if (ftype == Thrift.Type.BOOL) {
        this.success = input.readBool().value;
      } else {
        input.skip(ftype);
      }
      break;
      case 0:
        input.skip(ftype);
        break;
      default:
        input.skip(ftype);
    }
    input.readFieldEnd();
  }
  input.readStructEnd();
  return;
};

totalk.thrift.ClientService_onPing_result.prototype.write = function(output) {
  output.writeStructBegin('ClientService_onPing_result');
  if (this.success !== null && this.success !== undefined) {
    output.writeFieldBegin('success', Thrift.Type.BOOL, 0);
    output.writeBool(this.success);
    output.writeFieldEnd();
  }
  output.writeFieldStop();
  output.writeStructEnd();
  return;
};

totalk.thrift.ClientServiceClient = function(input, output) {
    this.input = input;
    this.output = (!output) ? input : output;
    this.seqid = 0;
};
totalk.thrift.ClientServiceClient.prototype = {};
totalk.thrift.ClientServiceClient.prototype.onPing = function(arg, callback) {
  this.send_onPing(arg, callback); 
};

totalk.thrift.ClientServiceClient.prototype.send_onPing = function(arg, callback) {
  var seqId = Thrift.AsyncManager.getInstance().getNextSeqId();
  var outTransport = new Thrift.TMemoryOutputTransport();
  var outProtocol = new Thrift.Protocol(outTransport);
  outProtocol.writeMessageBegin('ClientService:onPing', Thrift.MessageType.CALL, seqId);
  var args = new totalk.thrift.ClientService_onPing_args();
  args.arg = arg;
  args.write(outProtocol);
  outProtocol.writeMessageEnd();
  var packet = outTransport.getPayload();
  var self = this;
  if (callback) {
    Thrift.AsyncManager.getInstance().put(seqId, function (responsePacket) {
      var inputTransport = new Thrift.TMemoryInputTransport(responsePacket);
      var inputProtocol = new Thrift.Protocol(inputTransport);
      var result = null;
      try {
        result = self.recv_onPing(inputProtocol);
      } catch (e) {
        result = e;
      }
      callback(result);
    });
  }
  this.output.getTransport().write(packet);
  this.output.getTransport().flush();
};

totalk.thrift.ClientServiceClient.prototype.recv_onPing = function(inputProtocol) {
  var ret = inputProtocol.readMessageBegin();
  var fname = ret.fname;
  var mtype = ret.mtype;
  var rseqid = ret.rseqid;
  if (mtype == Thrift.MessageType.EXCEPTION) {
    var x = new Thrift.TApplicationException();
    x.read(inputProtocol);
    inputProtocol.readMessageEnd();
    throw x;
  }
  var result = new totalk.thrift.ClientService_onPing_result();
  result.read(inputProtocol);
  inputProtocol.readMessageEnd();

  if (null !== result.success) {
    return result.success;
  }
  throw 'onPing failed: unknown result';
};
