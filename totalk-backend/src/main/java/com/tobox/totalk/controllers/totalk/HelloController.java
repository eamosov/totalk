package com.tobox.totalk.controllers.totalk;

import org.apache.thrift.TException;

import com.knockchat.appserver.jgroups.RpcJGroups;
import com.knockchat.appserver.jms.RpcJms;
import com.knockchat.appserver.transport.http.RpcHttp;
import com.knockchat.appserver.transport.websocket.RpcWebsocket;
import com.tobox.totalk.controllers.AppThriftController;
import com.tobox.totalk.thrift.TotalkService;
import com.tobox.totalk.thrift.TotalkService.hello_args;

@RpcHttp
@RpcWebsocket
@RpcJms
@RpcJGroups
public class HelloController extends AppThriftController<TotalkService.hello_args, String> {
	
	
	@Override
	public void setup(hello_args args) {
		this.isSecured = false;
	}

	@Override
	protected String processRequest() throws TException {
		

		log.info("Executing handle with args: {}", args);
		
		return "Hello " + args.getArg();
	}

}
