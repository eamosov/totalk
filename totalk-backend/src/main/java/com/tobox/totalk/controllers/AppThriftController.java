package com.tobox.totalk.controllers;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;

import com.knockchat.appserver.controller.ThriftController;
import com.knockchat.appserver.transport.http.RpcHttp;
import com.knockchat.appserver.transport.websocket.RpcWebsocket;
import com.tobox.totalk.models.ClientsRegistry;
import com.tobox.totalk.models.Session;
import com.tobox.totalk.thrift.exceptions.AuthenticationRequiredException;

public abstract class AppThriftController<ArgsType extends TBase, ResultType> extends ThriftController<ArgsType, ResultType> {
	
	
	protected boolean isSecured = true;
		
	protected Session session;
		
	protected abstract ResultType processRequest() throws TException;

	@Autowired
	private ClientsRegistry clientsRegistry;

	
	@Override
	protected final ResultType handle() throws TException {
		return _handle();
	}

	private final ResultType _handle() throws TException {

		//Попытка авторизоваться через persistent данные соединения
		if (this.thriftClient !=null){			
			session = (Session)thriftClient.getSession();
		}
		
		//Авторизация через ключ, переданный в HTTP GET параметры
		if (session == null && (this.registryAnn == RpcHttp.class || this.registryAnn == RpcWebsocket.class)){
			
			String userId = null;
			String deviceId = null;
			try{
				userId = this.attributes.getHttpRequestParams().get("userId")[0];
				deviceId = this.attributes.getHttpRequestParams().get("deviceId")[0];
				session = new Session(userId, deviceId);
				if (thriftClient !=null)
					clientsRegistry.onAuth(session, thriftClient);
			}catch (RuntimeException e){				
			}						
		}
		
		if (isSecured && session==null)
			throw new AuthenticationRequiredException();
								
		return processRequest();
	}	
}
