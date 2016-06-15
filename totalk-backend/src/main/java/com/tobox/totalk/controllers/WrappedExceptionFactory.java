package com.tobox.totalk.controllers;

import org.apache.thrift.TException;

import com.tobox.totalk.thrift.exceptions.AuthenticationRequiredException;
import com.tobox.totalk.thrift.exceptions.InvalidSessionTokenException;
import com.tobox.totalk.thrift.exceptions.WrappedException;

public class WrappedExceptionFactory  {
	
	public static WrappedException create(TException e){
		final WrappedException w = new WrappedException();
		
		if (e instanceof AuthenticationRequiredException)
			w.setAuthenticationRequiredException((AuthenticationRequiredException)e);
		else if (e instanceof InvalidSessionTokenException)
			w.setInvalidSessionTokenException((InvalidSessionTokenException)e);
		else
			return null;
		
		return w;
	}

}
