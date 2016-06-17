package com.tobox.totalk.controllers.esindex;

import org.apache.thrift.TException;
import org.everthrift.appserver.controller.ThriftController;
import org.everthrift.appserver.transport.jms.RpcJms;
import org.everthrift.rabbit.RpcRabbit;
import org.springframework.beans.factory.annotation.Autowired;

import com.tobox.totalk.models.Indexer;
import com.tobox.totalk.thrift.rpc.EsIndexService;
import com.tobox.totalk.thrift.rpc.EsIndexService.index_args;

@RpcJms
@RpcRabbit
public class IndexController extends ThriftController<EsIndexService.index_args, Void> {

	@Autowired
	private Indexer indexer;
	
	@Override
	public void setup(index_args args) {
		
	}

	@Override
	protected Void handle() throws TException {
		indexer.runIndexTasks(args.getTasks());
		return null;
	}

}
