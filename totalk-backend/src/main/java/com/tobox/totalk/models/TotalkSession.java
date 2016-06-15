package com.tobox.totalk.models;

import org.everthrift.appserver.utils.thrift.SessionIF;
import org.springframework.util.Assert;

import com.tobox.session.Session;

public class TotalkSession implements SessionIF{

	private final Session session;

	public TotalkSession(Session session) {
		super();
		Assert.notNull(session);
		Assert.notNull(session.getUser());
		this.session = session;
	}

	@Override
	public String getCredentials() {
		return String.format("%s:%s", session.getUser().getId(), session.getUser().getDeviceId());
	}
	
	public String getUserId(){
		return session.getUser().getId().toString();
	}
	
	public String getDeviceId(){
		return "";
	}
}
