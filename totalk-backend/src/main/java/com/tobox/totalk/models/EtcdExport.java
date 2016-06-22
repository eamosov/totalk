package com.tobox.totalk.models;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.everthrift.jetty.JettyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import mousio.client.retry.RetryOnce;
import mousio.etcd4j.EtcdClient;
import mousio.etcd4j.responses.EtcdKeysResponse;

@Component
public class EtcdExport implements SmartLifecycle{
	
	private static final Logger log = LoggerFactory.getLogger(EtcdExport.class);
	
	@Autowired(required=false)
	private JettyServer jettyServer;
	
	@Value("${etcd.url}")
	private String etcdUrl;
	
	@Value("${etcd.key}")
	private String etcdKey;	

	@Value("${etcd.key.ttl:60}")
	private String etcdKeyTtl;
	
	@Value("${docker.host.ip}")
	private String hostIp;
	
	private volatile boolean isRunning = false;
	
	private EtcdClient etcd;
	
	@PostConstruct
	public void init(){		
		etcd = new EtcdClient(Lists.transform(ImmutableList.copyOf(etcdUrl.split(",")), URI::create).toArray(new URI[0]));
	}
	
	@PreDestroy
	public synchronized void destroy(){
		try {
			if (etcd !=null){
				etcd.close();
				etcd = null;
			}
		} catch (IOException e) {
			log.error("ETCD close error", e);
		}
	}
	
	private synchronized EtcdClient getClient(){
		if (etcd !=null)
			return etcd;
		
		etcd = new EtcdClient(Lists.transform(ImmutableList.copyOf(etcdUrl.split(",")), URI::create).toArray(new URI[0]));
		etcd.setRetryHandler(new RetryOnce(100));

		return etcd;
	}
	
	private void put(){
		
		final EtcdClient client = getClient();
		
		if (jettyServer !=null){
			try {
				client.put(etcdKey + "/http", hostIp + ":" + jettyServer.getJettyPort()).ttl(Integer.parseInt(etcdKeyTtl)).timeout(1, TimeUnit.SECONDS).send().addListener( resp -> {
					try {
						resp.get();
					} catch (Exception e) {
						log.error("Error setting ETCD key", e);
					}
				});
			} catch (Exception e) {
				log.error("Error setting ETCD key", e);
			}					
		}
	}
	
	private void delete(){
		try {
			EtcdKeysResponse response = getClient().deleteDir(etcdKey).recursive().timeout(1, TimeUnit.SECONDS).send().getNow();
		} catch (Exception e) {
			log.error("Error deleting ETCD key", e);
		}					
	}
	
	@Scheduled(fixedRate=5000)
	public void refresh(){
		if (isRunning)
			put();
	}	

	@Override
	public void start() {
		log.debug("starting EtcdExport");
		isRunning = true;
		put();
	}

	@Override
	public void stop() {
		log.debug("stopping EtcdExport");
		isRunning = false;
		delete();
	}

	@Override
	public int getPhase() {
		return 0;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(Runnable callback) {
		stop();
		callback.run();
	}

	@Override
	public boolean isRunning() {
		return isRunning;
	}

}
