package com.tobox.totalk.utils;

import com.google.common.base.Functions;

public class BiMultimap<K,V> extends BiMultimap2<K,V,V>{
		
	public static <K,V> BiMultimap<K,V> create(){
		return new BiMultimap<K,V>();
	}	
	
	public BiMultimap() {
		super(Functions.<V>identity());
	}
}
