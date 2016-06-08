package com.tobox.totalk.controllers;

import org.springframework.beans.factory.annotation.Autowired;

import com.knockchat.appserver.controller.ConnectionStateHandler;
import com.knockchat.appserver.transport.websocket.RpcWebsocket;
import com.tobox.totalk.models.ClientsRegistry;
import com.tobox.totalk.models.Session;

@RpcWebsocket
public class OnWebsocketOpen extends ConnectionStateHandler {

	@Autowired
	private ClientsRegistry clientsRegistry;
	
	public OnWebsocketOpen() {
		
	}

	@Override
	public boolean onOpen() {
		log.debug("onOpen, attr:{}", attributes);
				
		String userId = null;
		String deviceId = null;
		try{
			userId = this.attributes.getHttpRequestParams().get("userId")[0];
			deviceId = this.attributes.getHttpRequestParams().get("deviceId")[0];
			final Session session = new Session(userId, deviceId);
			if (thriftClient !=null)
				clientsRegistry.onAuth(session, thriftClient);
		}catch (RuntimeException e){				
		}						

		return true;
	}

}
