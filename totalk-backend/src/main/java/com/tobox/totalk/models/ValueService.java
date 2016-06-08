package com.tobox.totalk.models;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;


@Component
public class ValueService {

	private final Map<String, String> map = Maps.newHashMap();
	
	public String get(String key){
		return map.get(key);
	}
	
	public void set(String key, String value){
		map.put(key, value);
	}
}
