package com.tobox.totalk.controllers;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.Cookie;

import org.apache.thrift.TApplicationException;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.everthrift.appserver.controller.ThriftController;
import org.everthrift.appserver.transport.http.RpcHttp;
import org.everthrift.appserver.transport.websocket.RpcWebsocket;
import org.everthrift.clustering.MessageWrapper;
import org.everthrift.utils.ClassUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.tobox.entity.user.Language;
import com.tobox.session.Session;
import com.tobox.session.web.filters.SessionFilter;
import com.tobox.totalk.models.ClientsRegistry;
import com.tobox.totalk.models.TotalkSession;
import com.tobox.totalk.thrift.exceptions.AuthenticationRequiredException;
import com.tobox.totalk.thrift.exceptions.InvalidSessionTokenException;
import com.tobox.totalk.thrift.exceptions.WrappedException;

public abstract class AppThriftController<ArgsType extends TBase, ResultType> extends ThriftController<ArgsType, ResultType> {
	
	
	protected boolean isSecured = true;
		
	protected TotalkSession session;
		
	protected abstract ResultType processRequest() throws TException;

	@Autowired
	private ClientsRegistry clientsRegistry;

	
	@Override
	protected final ResultType handle() throws TException {
		try{
			return _handle();
		}catch(TException e){
			final WrappedException w =  WrappedExceptionFactory.create(e);
			throw w !=null ? w : e;
		}
	}
	
    private static Cookie getCookieKey(Cookie[] cookies, String keyName) {
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (keyName.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }
    
    static String getSecretKey(ApplicationContext context){
    	return context.getEnvironment().getProperty("session.secretKey", SessionFilter.DEFAULT_SECRET_KEY);
    }
	
	static Session getSession(Cookie[] cookies, Map<String, String> headers, ApplicationContext context) throws InvalidSessionTokenException{
				
		final Cookie toboxKey = getCookieKey(cookies, SessionFilter.TOBOX_KEY);
		if (toboxKey == null)
			return null;
		
        final Language userLanguage = Language.of(Optional.ofNullable(headers.get(SessionFilter.LANGUAGE_HEADER)).orElse(SessionFilter.DEFAULT_LANGUAGE));
        
        try{
        	return Session.of(toboxKey.getValue(), getSecretKey(context), userLanguage);
        }catch (com.tobox.session.InvalidSessionTokenException e){
        	throw new InvalidSessionTokenException(e.getMessage());
        }
	}

	private final ResultType _handle() throws TException {

		//Попытка авторизоваться через persistent данные соединения
		if (this.thriftClient !=null){			
			session = (TotalkSession)thriftClient.getSession();
		}
		
		//Авторизация через ключ, переданный в HTTP GET параметры
		if (session == null && (this.registryAnn == RpcHttp.class || this.registryAnn == RpcWebsocket.class)){
			
			final Session toboxSession = getSession(getHttpCookies(), getHttpHeaders(), context);
			
			if (toboxSession !=null){
				session = new TotalkSession(toboxSession);
				
				if (thriftClient !=null)
					clientsRegistry.onAuth(session, thriftClient);				
			}			
		}
		
		if (isSecured && session==null)
			throw new AuthenticationRequiredException();
								
		return processRequest();
	}
	
	protected Cookie[] getHttpCookies(){
		return (Cookie[])tps.getAttributes().get(MessageWrapper.HTTP_COOKIES);
	}
	
	protected void checkLimitOffset(int maxLimit, int maxOffset) throws TApplicationException{
		
		final Map<String, PropertyDescriptor> entityProps = ClassUtils.getPropertyDescriptors(args.getClass());
		final PropertyDescriptor limitProp = entityProps.get("limit");
		final PropertyDescriptor offsetProp = entityProps.get("offset");
		
		if (limitProp == null)
			throw new TApplicationException("unknown arg 'limit'");
		
		final Integer limit;
		try {
			limit = (Integer)limitProp.getReadMethod().invoke(args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new TApplicationException("unknown arg 'limit'");
		}
		
		if (limit == null || limit <=0 || limit > maxLimit)
			throw new TApplicationException("anvalid limit, must be (0, " + maxLimit + "]");
		
		if (offsetProp == null)
			throw new TApplicationException("unknown arg 'offset'");
				
		final Integer offset;
		try {
			offset = (Integer)offsetProp.getReadMethod().invoke(args);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new TApplicationException("unknown arg 'offset'");
		}
		
		if (offset == null || offset <0 || offset > maxOffset)
			throw new TApplicationException("anvalid offset, must be [0, " + maxOffset + "]");
	}
}
