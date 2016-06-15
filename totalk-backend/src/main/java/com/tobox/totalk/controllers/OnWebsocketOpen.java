package com.tobox.totalk.controllers;

import javax.servlet.http.Cookie;

import org.everthrift.appserver.controller.ConnectionStateHandler;
import org.everthrift.appserver.transport.websocket.RpcWebsocket;
import org.everthrift.clustering.MessageWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.tobox.session.Session;
import com.tobox.totalk.models.ClientsRegistry;
import com.tobox.totalk.models.TotalkSession;

@RpcWebsocket
public class OnWebsocketOpen extends ConnectionStateHandler {

	@Autowired
	private ClientsRegistry clientsRegistry;
	
	@Autowired
	private ApplicationContext context;
	
	public OnWebsocketOpen() {
		
	}

	@Override
	public boolean onOpen() {
		log.debug("onOpen, attr:{}", attributes);
				
		try{
			final Session toboxSession = AppThriftController.getSession(getHttpCookies(), attributes.getHttpHeaders(), context);
			
			if (toboxSession !=null){
				final TotalkSession session = new TotalkSession(AppThriftController.getSession(getHttpCookies(), attributes.getHttpHeaders(), context));
				
				if (thriftClient !=null)
					clientsRegistry.onAuth(session, thriftClient);				
			}			
		}catch (Exception e){				
		}						

		return true;
	}

	protected Cookie[] getHttpCookies(){
		return (Cookie[])this.attributes.getAttributes().get(MessageWrapper.HTTP_COOKIES);
	}	
	
}
