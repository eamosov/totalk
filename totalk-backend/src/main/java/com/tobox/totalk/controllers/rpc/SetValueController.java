package com.tobox.totalk.controllers.rpc;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;

import com.knockchat.appserver.controller.ThriftController;
import com.knockchat.appserver.jgroups.RpcJGroups;
import com.knockchat.appserver.jms.RpcJms;
import com.tobox.totalk.models.ValueService;
import com.tobox.totalk.thrift.rpc.RpcService;
import com.tobox.totalk.thrift.rpc.RpcService.setValue_args;

@RpcJGroups
@RpcJms
public class SetValueController extends ThriftController<RpcService.setValue_args, Void> {

	@Autowired
	private ValueService valueService;
	
	@Override
	public void setup(setValue_args args) {
		
	}

	@Override
	protected Void handle() throws TException {
		valueService.set(args.getKey(), args.getValue());
		return null;
	}

}
