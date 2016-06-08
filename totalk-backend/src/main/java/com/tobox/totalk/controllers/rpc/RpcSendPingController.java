package com.tobox.totalk.controllers.rpc;

import java.util.Collections;
import java.util.List;

import org.apache.thrift.TException;

import com.knockchat.appserver.jgroups.RpcJGroups;
import com.knockchat.appserver.transport.asynctcp.RpcAsyncTcp;
import com.tobox.totalk.models.ClientsRegistry;
import com.tobox.totalk.thrift.rpc.RpcService;
import com.tobox.totalk.thrift.rpc.RpcService.sendPing_args;

@RpcJGroups
@RpcAsyncTcp
public class RpcSendPingController extends ClientServiceController<RpcService.sendPing_args, List<String>> {
	
	@Override
	public void setup(sendPing_args args) {
		
	}

	@Override
	protected List<String> handle() throws TException {
				
		try {
			return this.waitForAnswer(clientsRegistry.send(args.getUserId(), args.getDeviceIds(), getClientTimeoutMs(), ClientsRegistry.clientService().onPing(args.getValue())));
		} catch (final TException e) {
			log.error("Error sendPing to accountId:" + args.getUserId(), e);
			return Collections.emptyList();
		}		
	}

}
