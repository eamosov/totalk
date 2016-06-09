package com.tobox.totalk.controllers.rpc;

import java.util.concurrent.Callable;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
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
	
	@Qualifier("listeningCallerRunsBoundQueueExecutor")
	@Autowired
	private  ListeningExecutorService executor;

	@Override
	protected String handle() throws TException {
		
		//Контроллер может быть асинхронным!
		
		final ListenableFuture<String> f = 
				executor.submit(new Callable<String>(){
					@Override
					public String call() throws Exception {
						final String v = valueService.get(args.getKey());
						return v == (String)null ? "" : v;			
					}
				});
		
		
		//Выход из контроллера без блокировки!
		return waitForAnswer(f);		
	}

	@Override
	public void setup(getValue_args args) {
		
	}

}
