package com.tobox.totalk.models.beansec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *  Possible method params:
 *	 
 *  AccountModel			- observer (authenticated client)
 *	Point					- observer's point of view
 *  SecurityHandler.Type	- type of handler
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SecurityHandler {
	
	public static enum Type{
		ALL,	 // CLIENTS || FOREIGN || ME || ADMIN 
		CLIENTS,
		FOREIGN,
		ME,
		ADMIN,
		JSON	// for ES index
	}

	Type[] value() default Type.ALL;
}
