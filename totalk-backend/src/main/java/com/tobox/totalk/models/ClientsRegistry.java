package com.tobox.totalk.models;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.thrift.TException;
import org.jgroups.Address;
import org.jgroups.blocks.ResponseMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.knockchat.clustering.jgroups.ClusterThriftClientIF;
import com.knockchat.clustering.jgroups.ClusterThriftClientIF.Options;
import com.knockchat.clustering.jgroups.ClusterThriftClientIF.Reply;
import com.knockchat.clustering.thrift.InvocationInfo;
import com.knockchat.clustering.thrift.InvocationInfoThreadHolder;
import com.knockchat.clustering.thrift.ThriftProxyFactory;
import com.knockchat.utils.Pair;
import com.knockchat.utils.thrift.ThriftClient;
import com.tobox.totalk.thrift.ClientService;
import com.tobox.totalk.utils.BiMultimap;

@Component
@SuppressWarnings("rawtypes")
public class ClientsRegistry {
	
	private static final Logger log = LoggerFactory.getLogger(ClientsRegistry.class);
	
	@Autowired
	private ClusterThriftClientIF clusterThriftClient;

	private final BiMultimap<String, ThriftClient> clients  = BiMultimap.create();

	public synchronized void addClient(String userId, ThriftClient client) {
		
		log.debug("Add ThriftClient: userId={} sessionId={} session={}", userId, client.getSessionId(), client.getSession());
		
		clients.removeByValue(client);
		clients.put(userId, client);
	}
	
	public synchronized List<ThriftClient> getByUserId(String userId){
		return Lists.newArrayList(clients.values(userId));
	}
	
	public synchronized boolean removeClient(ThriftClient client){
		
		log.debug("Remove clients with sessionId={}", client.getSessionId());		
		return !clients.removeByValue(client).isEmpty();
	}
		
	public static ClientService.Iface clientService(){
		return ThriftProxyFactory.onIfaceAsAsync(ClientService.Iface.class);	
	}
	
	/**
	 * 
	 * @param accountId
	 * @param deviceIds
	 * @param timeout
	 * @param unused
	 * @return
	 * 		Pair.first - идентификатор устройста(devideId)
	 * 		Pair.second - возвращенное от клиента значение
	 */
	private <R> ListenableFuture<List<Pair<String,R>>> doSend(final String userId, List<String> deviceIds, final int timeout, final R unused){
		
		final InvocationInfo ii = InvocationInfoThreadHolder.getInvocationInfo();
		
		final List<ThriftClient> clients = getByUserId(userId);
		
		final List<ListenableFuture<Pair<String,R>>> futures = Lists.newArrayList();
		
		final Set<String> _deviceIds = deviceIds == null ? null : Sets.newHashSet(deviceIds);
		
		for (final ThriftClient c : clients){
			
			final Session session = (Session)c.getSession();
			
			if (session == null){
				log.error("No session for sessionId: {}", c.getSessionId());
				continue;
			}
			
			log.debug("try session: {}", c.getSession());
						
			if (_deviceIds == null || _deviceIds.contains(session.getDeviceId())){
				
				log.debug("try call clientService");
				
				try {
					futures.add(Futures.transform(c.<R>thriftCallByInfo(timeout, new InvocationInfo(ii)), new Function<R, Pair<String,R>>(){

						@Override
						public Pair<String, R> apply(R input) {
							return Pair.create(session.getDeviceId(), input);
						}}));
				} catch (final TException e) {
					log.debug("TException", e);
				}								
			}else{
				log.debug("skip: not in device list");
			}
		}

		return Futures.successfulAsList(futures);
	}
	
	/**
	 * 
	 * @param clientId
	 * @param deviceIds
	 * @param clientTimeOut
	 * @param unused
	 * @return  список идентификаторов устройств, куда отправлено сообщение и вернулся успешный ответ (true)
	 */
	public ListenableFuture<List<String>> send(final String userId, List<String> deviceIds, int clientTimeOut, final boolean unused){
		final ListenableFuture<List<Pair<String, Boolean>>> ret = doSend(userId, deviceIds, clientTimeOut, unused);
		
		return Futures.transform(ret, new Function<List<Pair<String, Boolean>>, List<String>>(){

			@Override
			public List<String> apply(List<Pair<String, Boolean>> input) {
				log.debug("ClientService success list (accId={}) : {}", userId, input);
				
				final Set<String> success = Sets.newHashSet();
				
				for (final Pair<String, Boolean> b: input){
					if (b!=null && b.second.booleanValue())
						success.add(b.first);
				}
				return Lists.newArrayList(success);
			}});				
	}
		
	public void onAuth(Session ses, final ThriftClient thriftClient){
		
		Session old = (Session)thriftClient.getSession();
		
		if (old == null || !Objects.equals(old.getUserId(), ses.getUserId()) || !Objects.equals(old.getDeviceId(), ses.getDeviceId()))		
			thriftClient.setSession(ses);
		
		if (thriftClient.isThriftCallEnabled()){
			addClient(ses.getUserId(), thriftClient);
			
			thriftClient.addCloseCallback(new FutureCallback<Void>(){

				@Override
				public void onSuccess(Void result) {
					removeClient(thriftClient);					
				}

				@Override
				public void onFailure(Throwable t) {
					
				}});					
		}
		
	}
	
	/**
	 * 
	 * @param thriftClient
	 * @return old Session object
	 */
	public Session onLogout(final ThriftClient thriftClient){
        removeClient(thriftClient);
        final Session s = (Session)thriftClient.getSession();        
        thriftClient.setSession(null);
		return s;
	}
	
    public static interface RpcServiceCall{
    	List<String> call(String userId, List<String> deviceIds) throws TException;
    }

    /**
     * 
     * @param userId
     * @param deviceIds
     * @param serviceCall
     * @return Список deviceId, на которые сообщение не доставлено
     */
    public ListenableFuture<List<String>> call(String userId, List<String> deviceIds, final RpcServiceCall serviceCall){
    	
		if (deviceIds !=null && deviceIds.isEmpty())
			return Futures.immediateFuture(null);
				
		try {
			serviceCall.call(userId, deviceIds);
		} catch (TException e) {
			return Futures.immediateFailedFuture(e);
		}
		
		final List<String> _deviceIds = deviceIds == null ? Lists.newArrayList() : Lists.newArrayList(deviceIds);
			
		final InvocationInfo ii = InvocationInfoThreadHolder.getInvocationInfo();
		
		final ListenableFuture<Map<Address, Reply<List<String>>>> f;
		try {
			f = clusterThriftClient.call(ii, Options.responseMode(ResponseMode.GET_ALL), Options.loopback(true));
		} catch (TException e) {
			return Futures.immediateFailedFuture(e);
		}
		
		final SettableFuture<List<String>> r = SettableFuture.create();
			
		Futures.addCallback(f, new FutureCallback<Map<Address, Reply<List<String>>>>(){

			@Override
			public void onSuccess(Map<Address, Reply<List<String>>> ret) {
				log.debug("{}({}): {}", ii.methodName, ii.args.toString(), ret);
					
				for(Reply<List<String>> _ids: ret.values()){
					try {
						final List<String> ids = _ids.get();
						if (!CollectionUtils.isEmpty(ids)){
							for (String deviceId: ids){
								_deviceIds.remove(deviceId);
							}								
						}						
					} catch (TException e) {
					}
				}
				
				r.set(_deviceIds);
			}

			@Override
			public void onFailure(Throwable t) {
				r.set(_deviceIds);
			}});
			
		return r;			
    }
	
}
