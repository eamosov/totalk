package com.tobox.totalk.utils;

import java.util.Collections;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.LinkedHashMultimap;

public class BiMultimap2<K,V,R> {
	
	private final Function<V,R> reverseKey;
	
	private LinkedHashMultimap<K,V> forward = LinkedHashMultimap.create();
	private LinkedHashMultimap<R, K> inverse = LinkedHashMultimap.create();
	
	public static <K,V> BiMultimap2<K,V,V> create(){
		return new BiMultimap2<K,V,V>(Functions.<V>identity());
	}	
	
	public static <K,V,R> BiMultimap2<K,V,R> create(Function<V,R> reverseKey){
		return new BiMultimap2<K,V,R>(reverseKey);
	}

	public BiMultimap2(Function<V,R> reverseKey) {
		this.reverseKey = reverseKey;
	}

	public void put(K key, V value){
		forward.put(key, value);
		inverse.put(reverseKey.apply(value), key);
	}
	
	public Set<V> remove(K key){
		final Set<V> values = forward.removeAll(key);
		
		if (values !=null){
			for (V v: values)
				inverse.remove(reverseKey.apply(v), key);
		}
		
		return values == null ? Collections.<V>emptySet() : values;
	}
	
	public Set<K> removeByValue(V value){
		final Set<K> keys = inverse.removeAll(reverseKey.apply(value));
		
		if (keys !=null){
			for (K k: keys)
				forward.remove(k, value);
		}
		
		return keys == null ? Collections.<K>emptySet() : keys;
	}
	
	public Set<K> keys(V value){
		return inverse.get(reverseKey.apply(value));
	}
	
	public Set<V> values(K key){
		return forward.get(key);
	}
	
	@Override
	public String toString() {
		return "BiMultimap " + forward;
	}	
	
//	public static void main(String[] args) {
//		
//		final BiMultimap<Integer, String, String> map = BiMultimap.create();
//		
//		map.put(1, "aaa");
//		map.put(2, "aaa");
//		map.put(2, "bbb");
//		map.put(2, "ccc");
//		map.put(2, "ccc");
//		
//		System.out.println(map.toString());
//		System.out.println(map.values(2));
//		System.out.println(map.keys("aaa"));
//		System.out.println(map.keys("ccc"));
//		
//		map.removeByValue("ccc");
//		
//		System.out.println(map.toString());
//		System.out.println(map.values(2));
//		System.out.println(map.keys("aaa"));
//		System.out.println(map.keys("ccc"));
//	}

}
