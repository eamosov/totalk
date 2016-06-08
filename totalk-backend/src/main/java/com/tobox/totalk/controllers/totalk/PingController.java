package com.tobox.totalk.controllers.totalk;

import static com.knockchat.clustering.thrift.ThriftProxyFactory.on;

import java.util.List;

import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.knockchat.appserver.transport.http.RpcHttp;
import com.knockchat.appserver.transport.websocket.RpcWebsocket;
import com.tobox.totalk.controllers.AppThriftController;
import com.tobox.totalk.models.ClientsRegistry;
import com.tobox.totalk.thrift.TotalkService;
import com.tobox.totalk.thrift.TotalkService.ping_args;
import com.tobox.totalk.thrift.rpc.RpcService;

@RpcHttp
@RpcWebsocket
public class PingController extends AppThriftController<TotalkService.ping_args, Void> {

	@Autowired
	private ClientsRegistry clientsRegistry;

	@Override
	protected Void processRequest() throws TException {
		
		ListenableFuture<List<String>> f  = clientsRegistry.call(session.getUserId(), null,  (userId, deviceIds) -> {
			return on(RpcService.Iface.class).sendPing(userId, deviceIds, args.getArg());
		});
		
		Futures.addCallback(f, new FutureCallback<List<String>>(){

			@Override
			public void onSuccess(List<String> result) {
				log.info("onSuccess: {}", result);				
			}

			@Override
			public void onFailure(Throwable t) {
				log.error("onFailure", t);				
			}
			
		});
		
		
		return null;
	}

	@Override
	public void setup(ping_args args) {
		
	}

}
