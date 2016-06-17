package com.tobox.totalk.models.beansec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface SecurityPostHandler {
	
	public static enum Type{
		ALL,
		CLIENTS,
		ADMIN		
	}

	Type[] value() default Type.ALL;
}
