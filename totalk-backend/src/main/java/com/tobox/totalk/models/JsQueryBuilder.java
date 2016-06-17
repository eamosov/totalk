package com.tobox.totalk.models;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.IOUtils;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.Scriptable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;

@Component
public class JsQueryBuilder {
	
	private static final Logger log = LoggerFactory.getLogger(JsQueryBuilder.class);
	
	private final JSONUtil ju = new JSONUtil();
	
	private volatile Scriptable scope;
		
	@Autowired
	public JsQueryBuilder(ApplicationContext ctx) throws IOException {
		initScope(ctx.getResources("classpath:queries/*.js"));
	}

	public static JsQueryBuilder create(String jsSrc){
		return new JsQueryBuilder(jsSrc);
	}

	public static JsQueryBuilder create(String []jsSrc){
		return new JsQueryBuilder(jsSrc);
	}

	private JsQueryBuilder(String jsSrc){
		initScope(new Resource[]{new FileSystemResource(jsSrc)});
	}

	private JsQueryBuilder(String []jsSrc){
		final Resource[] r = new Resource[jsSrc.length];
		for (int i=0; i< jsSrc.length; i++)
			r[i] = new FileSystemResource(jsSrc[i]);
		
		initScope(r);
	}

	public synchronized void initScope(Resource src[]){
		final Context cx = Context.enter();
		try{
			scope = cx.initStandardObjects();
			for (Resource r: src){
				try(final InputStream is = r.getInputStream()){
					cx.evaluateString(scope, IOUtils.toString(is), r.getFilename(), 1, null);
				} catch (IOException e) {
					throw Throwables.propagate(e);
				}
			}
			
			final Map gv = Maps.newHashMap();
			gv.put("explain", (Object)log.isDebugEnabled());
			
			setGlobalVars(gv);			
		}finally{
			Context.exit();
		}		
	}
	
	public synchronized void setGlobalVars(Map<String, Object> vars){
		Context.enter();
		try{
			for (Entry<String, Object> e:vars.entrySet())
				scope.put(e.getKey(), scope, e.getValue());			
		}finally{
			Context.exit();
		}				
	}
	
	public <T> Object array(List<T> list){
		return Context.javaToJS(list.toArray(), scope);
	}
			
	public synchronized String getQuery(String jsVarName, Object ... args){
		
		final Context cx = Context.enter();
		try{						
			final Function fct = (Function)scope.get(jsVarName, scope);
			
			final Object []jsArgs = new Object[args.length];
			for (int i=0; i<args.length; i++){
				
				if (args[i] instanceof List)
					jsArgs[i] = Context.javaToJS(((List)args[i]).toArray(), scope);
				else
					jsArgs[i] = Context.javaToJS(args[i], scope);
			}
			
			final Object o = fct.call(cx, scope, scope, jsArgs);
			return ju.SimpleNativeObjectToJson((NativeObject)o);
		}finally{
			Context.exit();
		}		
	}
	
}
