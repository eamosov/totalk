package com.tobox.totalk.models;

import com.knockchat.utils.thrift.SessionIF;

public class Session implements SessionIF{

	private final String userId;
	private final String deviceId;		

	public Session(String userId, String deviceId) {
		super();
		this.userId = userId;
		this.deviceId = deviceId;
	}

	@Override
	public String getCredentials() {
		return userId + ":" + deviceId;
	}

	public String getUserId() {
		return userId;
	}

	public String getDeviceId() {
		return deviceId;
	}

}
