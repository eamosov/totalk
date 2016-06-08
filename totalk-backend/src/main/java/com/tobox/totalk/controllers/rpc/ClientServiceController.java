package com.tobox.totalk.controllers.rpc;

import org.apache.thrift.TBase;
import org.springframework.beans.factory.annotation.Autowired;

import com.knockchat.appserver.controller.ThriftController;
import com.tobox.totalk.models.ClientsRegistry;

public abstract class ClientServiceController<ArgsType extends TBase, ResultType> extends ThriftController<ArgsType, ResultType>{

	@Autowired
	protected ClientsRegistry clientsRegistry;
	
	protected int getClientTimeoutMs(){
		return 5000;
	}
}
