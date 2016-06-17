package com.tobox.totalk.models;

import org.apache.thrift.TBase;
import org.everthrift.appserver.utils.GsonSerializer.TBaseSerializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonBuilderHolder {

	public static final Gson gson =
			new GsonBuilder().
			setPrettyPrinting().
			disableHtmlEscaping().
			registerTypeHierarchyAdapter(TBase.class, new TBaseSerializer()).
			create();
	
	
}
