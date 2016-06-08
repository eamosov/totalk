package com.tobox.totalk.controllers.rpc;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;

import com.knockchat.appserver.controller.ThriftController;
import com.knockchat.appserver.jgroups.RpcJGroups;
import com.knockchat.appserver.jms.RpcJms;
import com.tobox.totalk.models.ValueService;
import com.tobox.totalk.thrift.rpc.RpcService;
import com.tobox.totalk.thrift.rpc.RpcService.getValue_args;

@RpcJGroups
@RpcJms
public class GetValueController extends ThriftController<RpcService.getValue_args, String> {

	@Autowired
	private ValueService valueService;

	@Override
	protected String handle() throws TException {
		final String v = valueService.get(args.getKey());
		return v == null ? "" : v;
	}

	@Override
	public void setup(getValue_args args) {
		
	}

}
