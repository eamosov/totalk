package com.tobox.test.suites;

import static com.knockchat.clustering.thrift.ThriftProxyFactory.on;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.thrift.TException;
import org.hamcrest.Matchers;
import org.jgroups.Address;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.google.common.collect.Maps;
import com.google.common.util.concurrent.ListenableFuture;
import com.knockchat.clustering.jgroups.ClusterThriftClientIF.Reply;
import com.knockchat.clustering.jgroups.JGroupsThriftClientImpl;
import com.knockchat.clustering.jms.JmsThriftClientImpl;
import com.tobox.test.TestApplication;
import com.tobox.totalk.thrift.TotalkService;
import com.tobox.totalk.thrift.exceptions.AuthenticationRequiredException;
import com.tobox.totalk.thrift.rpc.RpcService;

@ContextConfiguration(classes = TestApplication.class)
public class TotalkServiceTest extends AbstractTestNGSpringContextTests {
	
	private static final Logger log = LoggerFactory.getLogger(TotalkServiceTest.class);
	
	private JmsThriftClientImpl jmsClient;
	private JGroupsThriftClientImpl jGroupsClient;
	
	@BeforeClass
	public void beforeSuite() throws Exception{
		System.setProperty("java.net.preferIPv4Stack", "true");
		System.setProperty("jgroups.multicast.bind_addr", "127.0.0.1");
				
		jGroupsClient = new JGroupsThriftClientImpl("jgroups.xml", "totalk");
		jGroupsClient.connect();
		Thread.sleep(100);
		
		jmsClient = new JmsThriftClientImpl(new ActiveMQConnectionFactory("failover:tcp://localhost:61616"));
	}
	
	@AfterClass(alwaysRun = true)
	public void afterSuite(){
		
		if (jGroupsClient !=null){
			jGroupsClient.destroy();
			jGroupsClient = null;
		}
	}
		
	@Test
	public void testJmsTransport() throws AuthenticationRequiredException, TException, InterruptedException, ExecutionException{
		
		final ListenableFuture<Map<Address, Reply<String>>> f = jGroupsClient.<String>call(on(RpcService.Iface.class).getValue("k1"));
		
		final Map<Address, String> resultBefore = Maps.transformValues(f.get(),  Reply<String>::getUnchecked);
		log.info("Result before setValue:{}", resultBefore);
		
		final String rndValue = Long.toString(new Random().nextLong());
		jmsClient.on(RpcService.Iface.class).setValue("k1", rndValue);
		
		Thread.sleep(1000);
		
		final Map<Address, String> resultAfter = Maps.transformValues(jGroupsClient.<String>call(on(RpcService.Iface.class).getValue("k1")).get(),  Reply::getUnchecked);
		log.info("Result after setValue:{}", resultAfter);
		
		//Значение ключа k1 должно измениться только на одной ноде
		assertThat(Maps.difference(resultBefore, resultAfter).entriesDiffering().size(), Matchers.equalTo(1));
		assertThat(Maps.difference(resultBefore, resultAfter).entriesDiffering().entrySet().iterator().next().getValue().rightValue(), Matchers.equalTo(rndValue));

	}
	
	@Test
	public void testMulticastJGroups() throws Exception{

		final ListenableFuture<Map<Address,Reply<String>>> f2 = jGroupsClient.call(on(TotalkService.Iface.class).hello("world"));
			
		final Map<Address,Reply<String>> results = f2.get();			
		log.info("multicast answer: {}", results);
			
		//check result from every node
		for (Address a: jGroupsClient.getCluster().getView().getMembers()){
			if (!a.equals(jGroupsClient.getCluster().getAddress()))
				assertThat(results.get(a).get(), Matchers.equalTo("Hello world"));
		}						
	}
	
	@Test
	public void testUnicastJgroups() throws Exception{
		
		final ListenableFuture<String> f = jGroupsClient.callOne(on(TotalkService.Iface.class).hello("world"));			
		final String results = f.get();			
		log.info("unicast answer: {}", results);
			
		assertThat(results, Matchers.equalTo("Hello world"));		
	}
	
	@Test
	public void testUnicastJgroups2() throws Exception{
		final ListenableFuture<Map<Address, Reply<String>>> f = jGroupsClient.<String>call(on(RpcService.Iface.class).getValue("k1"));
		
		final Map<Address, String> resultBefore = Maps.transformValues(f.get(),  Reply<String>::getUnchecked);
		log.info("Result before setValue:{}", resultBefore);
		
		final String rndValue = Long.toString(new Random().nextLong());
				
		on(RpcService.Iface.class).setValue("k1", rndValue);
		jGroupsClient.callOne((Void)null).get();// нельзя передать аргумент типа void, поэтому две строки
				
		final Map<Address, String> resultAfter = Maps.transformValues(jGroupsClient.<String>call(on(RpcService.Iface.class).getValue("k1")).get(),  Reply::getUnchecked);
		log.info("Result after setValue:{}", resultAfter);
		
		//Значение ключа k1 должно измениться только на одной ноде
		assertThat(Maps.difference(resultBefore, resultAfter).entriesDiffering().size(), Matchers.equalTo(1));
		assertThat(Maps.difference(resultBefore, resultAfter).entriesDiffering().entrySet().iterator().next().getValue().rightValue(), Matchers.equalTo(rndValue));		
	}

}
