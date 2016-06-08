package com.tobox.test;

import static com.knockchat.clustering.thrift.ThriftProxyFactory.on;

import java.util.Map;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.jgroups.Address;

import com.google.common.util.concurrent.ListenableFuture;
import com.knockchat.clustering.jgroups.ClusterThriftClientIF.Reply;
import com.knockchat.clustering.jgroups.JGroupsThriftClientImpl;
import com.knockchat.clustering.jms.JmsThriftClientImpl;
import com.tobox.totalk.thrift.TotalkService;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

public class Test {

	public static class Dummy extends ClassicConverter{

		@Override
		public String convert(ILoggingEvent event) {			
			return "";
		}
		
	}

	static {
		PatternLayout.defaultConverterMap.put("coloron", Dummy.class.getName());
		PatternLayout.defaultConverterMap.put("coloroff", Dummy.class.getName());
	}

	public static void main(String[] args) throws Exception {
						
		final JmsThriftClientImpl jmsClient = new JmsThriftClientImpl(new ActiveMQConnectionFactory("failover:tcp://localhost:61616"));
		
		
		final JGroupsThriftClientImpl jGroupsClient = new JGroupsThriftClientImpl("jgroups.xml", "totalk");		
		jGroupsClient.connect();
		Thread.sleep(200);
		
		System.out.println("Using JMS for calling TotalkService.hello");
		jmsClient.on(TotalkService.Iface.class).hello("===============hello from jms!=================");		
		
		
		System.out.println("Using JGroups \"callOne\" for calling TotalkService.hello");
		ListenableFuture<String> f = jGroupsClient.callOne(on(TotalkService.Iface.class).hello("world"));
		System.out.println("Answer callOne: " + f.get());

		
				
		System.out.println("Using JGroups \"call\" for calling TotalkService.hello");
		ListenableFuture<Map<Address,Reply<String>>> f2 = jGroupsClient.call(on(TotalkService.Iface.class).hello("world"));
		System.out.println("Answer call: " + f2.get());

		jGroupsClient.destroy();
	}

}
