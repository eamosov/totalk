package com.tobox.totalk.models;

import org.apache.thrift.TException;
import org.elasticsearch.client.Client;
import org.everthrift.appserver.AppserverApplication;
import org.everthrift.appserver.model.LocalEventBus;
import org.everthrift.cassandra.model.CassandraFactories;
import org.everthrift.cassandra.model.Statements;
import org.everthrift.clustering.jgroups.ClusterThriftClientIF;
import org.everthrift.thrift.TFunction;
import org.everthrift.thrift.TVoidFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.tobox.totalk.models.beansec.Security;

import net.sf.ehcache.CacheManager;

/**
 * Created by knockchat on 7/21/14.
 */
@Component
public class CF {

  private static volatile CF INSTANCE;

  public static final CF i() {
	  
	  CF _INSTANCE = INSTANCE;
	  if (_INSTANCE == null){
		  synchronized(CF.class){
			  if (_INSTANCE == null){
				  _INSTANCE = INSTANCE = AppserverApplication.INSTANCE.context.getBean(CF.class);
			  }
		  }
	  }
	  return _INSTANCE;
  }
  
  @Autowired
  public ApplicationContext applicationContext;

  @Autowired
  public CacheManager cacheManager;

  @Autowired
  public Indexer esIndexer;
  
  @Qualifier("listeningCallerRunsBoundQueueExecutor")
  @Autowired
  public  ListeningExecutorService callerRunsBoundQueueExecutor;
  
  @Qualifier("unboundQueueExecutor")
  @Autowired
  public  ThreadPoolTaskExecutor unboundQueueExecutor;
    
  @Autowired
  public CassandraFactories cassandraFactories;
  
  @Autowired
  public Security security;
  
  @Autowired
  public LocalEventBus localEventBus;
  
  @Autowired
  public Client esClient;
  
  @Autowired
  public ClusterThriftClientIF multicastThriftTransport;
  
  public static Statements autoCommit(){
	  return i().cassandraFactories.begin().setAutoCommit(true).setBatch(false);
  }

  public static Statements begin(){
	  return i().cassandraFactories.begin().setBatch(false);
  }

  public static Statements batch(){
	  return i().cassandraFactories.begin();
  }

  public static void batch(TVoidFunction<Statements> f) throws TException{
	  i().cassandraFactories.batch(f);
  }

  public static <E> E batchr(TFunction<Statements, E> f) throws TException{
	  return i().cassandraFactories.batch(f);
  }

  public static void execute(TVoidFunction<Statements> f) throws TException{
	  i().cassandraFactories.execute(f);
  }

  public static <E> E executer(TFunction<Statements, E> f) throws TException{
	  return i().cassandraFactories.execute(f);
  }

}
