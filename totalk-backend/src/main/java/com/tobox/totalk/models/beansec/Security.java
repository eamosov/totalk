package com.tobox.totalk.models.beansec;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.thrift.TBase;
import org.everthrift.appserver.model.lazy.LazyLoadManager;
import org.everthrift.appserver.utils.thrift.ThriftTraversal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tobox.entity.user.User;
import com.tobox.totalk.models.UserIdAwareIF;

@Component
public class Security implements InitializingBean{
	
	private static final Logger log = LoggerFactory.getLogger(Security.class);
		
	@Autowired
	private ApplicationContext applicationContext;
	
	private static class SecurityHandlers{
		final Class cls;
		final Map<SecurityHandler.Type, List<Method>> methods;
		final Map<SecurityPostHandler.Type, List<Method>> postMethods;
		
		public SecurityHandlers(Class cls) {
			super();
			this.cls = cls;
			this.methods = Maps.newEnumMap(SecurityHandler.Type.class);
			this.postMethods = Maps.newEnumMap(SecurityPostHandler.Type.class);
			
			for (SecurityHandler.Type t :SecurityHandler.Type.values()){
				methods.put(t, Lists.newArrayList());
			}
			
			for (SecurityPostHandler.Type t: SecurityPostHandler.Type.values()){
				postMethods.put(t, Lists.newArrayList());
			}
		}
		
		void put(SecurityHandler.Type t, Method m){
			methods.get(t).add(m);
		}

		void put(SecurityPostHandler.Type t, Method m){
			postMethods.get(t).add(m);
		}

		@Override
		public String toString() {
			return "SecurityHandlers [cls=" + cls + ", methods=" + methods + "]";
		}
		
		void call(SecurityHandler.Type type, Object obj, User observer){
			
			final List<Method> _methods = methods.get(type);
			if (!_methods.isEmpty())
				_methods.forEach((m) -> {call(m, type, obj, observer);});
			
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		void call(Method m, SecurityHandler.Type type, Object obj, User observer){
				
			if (log.isDebugEnabled())
				log.debug("call security handler {}", m);
			
			try{
				if (m.getParameterCount() > 0){
					
					final Class<?>[] paramTypes = m.getParameterTypes();
					final Object args[] = new Object[paramTypes.length];
						
					for (int i=0; i<paramTypes.length; i++){
						final Class paramCls = paramTypes[i];
						
						if (paramCls.isAssignableFrom(User.class)){
							args[i] = observer;
						}else if (paramCls.isAssignableFrom(SecurityHandler.Type.class)){
							args[i] = type;
						}else{
							log.error("Coudn't find argument of class {} for method {}", paramCls.getCanonicalName(), m);
							args[i] = null;
						}
					}
					m.invoke(obj, args);
				}else{
					m.invoke(obj);
				}			
			}catch(Exception e){
				throw Throwables.propagate(e);
			}		
		}
		
		<T> void call(SecurityPostHandler.Type type, Set<T> objects, User observer){
			
			final List<Method> _methods = postMethods.get(type);
			if (!_methods.isEmpty())
				_methods.forEach((m) -> {call(m, type, objects, observer);});
			
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		<T> void call(Method m, SecurityPostHandler.Type type, Set<T> objects, User observer){
			
			if (log.isDebugEnabled())
				log.debug("call post security handler {}", m);
			
			try{					
																
				final Class<?>[] paramTypes = m.getParameterTypes();
				final Object []args = new Object[paramTypes.length];
				
				for (int i=0; i< paramTypes.length; i++){
					final Class paramCls = paramTypes[i];
					
					if (paramCls.isAssignableFrom(Set.class)){
						args[i] = objects;
					}else if (paramCls.isAssignableFrom(User.class)){
						args[i] = observer;
					}else if (paramCls.isAssignableFrom(SecurityPostHandler.Type.class)){
						args[i] = type;
					}else{
						log.error("Coudn't find argument of class {} for method {}", paramCls.getCanonicalName(), m);
						args[i] = null;
					}
				}
				m.invoke(null, args);
			}catch(Exception e){
				throw Throwables.propagate(e);
			}		
		}
	}
	
	private final List<SecurityHandlers> securityHandlers = Lists.newArrayList();

	public Security() {		
		
	}

	public void loadAndCallSecurityHandlers(final User clientAccount, Object result){
		LazyLoadManager.load(LazyLoadManager.SCENARIO_DEFAULT, LazyLoadManager.MAX_LOAD_ITERATIONS, result, clientAccount);
		callSecurityHandlers(clientAccount, false, result);
	}
	
	/**
	 * Персонифицировать result с точки зрения clientAccount
	 * 
	 * @param clientAccount
	 * @param result
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void callSecurityHandlers(final User clientAccount,  boolean isAdmin, Object result){
		
		if (result == null)
			return;
		
		final long start = System.currentTimeMillis();
		
				
		for (SecurityHandlers h:securityHandlers){
			
			if (log.isDebugEnabled())
				log.debug("ThriftTraversal.visitChildsOfType: result.getClass()={}, cls={}", result.getClass().getSimpleName(), h.cls.getSimpleName());
			
			final Set<TBase> objects = ThriftTraversal.visitChildsOfType(result,
					h.cls,
					new Function<TBase, Void>(){
						@Override
						public Void apply(TBase input) {
							
							if (log.isDebugEnabled())
								log.debug("find object of class {}", input.getClass().getSimpleName());
							
							h.call(SecurityHandler.Type.ALL, input, clientAccount);
							
							if (isAdmin){
								h.call(SecurityHandler.Type.ADMIN, input, clientAccount);
							}else{
								h.call(SecurityHandler.Type.CLIENTS, input, clientAccount);
								
								if (clientAccount!=null && (input instanceof UserIdAwareIF) && clientAccount.getId().equals(((UserIdAwareIF)input).getUserId())){
									h.call(SecurityHandler.Type.ME, input, clientAccount);
								}else{
									h.call(SecurityHandler.Type.FOREIGN, input, clientAccount);
								}								
							}
																					
							return null;
						}
					});
			
			h.call(SecurityPostHandler.Type.ALL, objects, clientAccount);
			
			if (isAdmin)
				h.call(SecurityPostHandler.Type.ADMIN, objects, clientAccount);
			else
				h.call(SecurityPostHandler.Type.CLIENTS, objects, clientAccount);
		}
		
		if (log.isDebugEnabled()){
			final long end = System.currentTimeMillis();
			log.debug("setAnonymous(id={}, result={}) took {}ms", clientAccount !=null ? clientAccount.getId() : null, result.getClass().getSimpleName(), end - start);			
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void callJsonSecurityHandlers(Object result){
		
		if (result == null)
			return;
		
		for (SecurityHandlers h:securityHandlers){
						
			ThriftTraversal.visitChildsOfType(result,
					h.cls,
					new Function<TBase, Void>(){
						@Override
						public Void apply(TBase input) {
							
							if (log.isDebugEnabled())
								log.debug("find object of class {}", input.getClass().getSimpleName());
							
							h.call(SecurityHandler.Type.JSON, input, null);
							
							return null;
						}
					});			
		}		
	}

	
	@Override
	public void afterPropertiesSet() throws Exception {
		final ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		
		scanner.addIncludeFilter(new AnnotationTypeFilter(Secured.class));
		
		final List<String> l = applicationContext.getEnvironment().getProperty("thrift.scan", List.class);

		for (String basePath:l){			
			for (BeanDefinition b : scanner.findCandidateComponents(basePath)){
				final Class cls = ClassUtils.resolveClassName(b.getBeanClassName(), ClassUtils.getDefaultClassLoader());
				
				final SecurityHandlers sh = new SecurityHandlers(cls);
				for (Method m : cls.getMethods()){					
					final SecurityHandler h = m.getAnnotation(SecurityHandler.class);
					
					if (h!=null){
						for (SecurityHandler.Type t : h.value()){
							
							if ((t == SecurityHandler.Type.ME || t == SecurityHandler.Type.FOREIGN) && !UserIdAwareIF.class.isAssignableFrom(cls))
								throw new RuntimeException("Class " + cls.getCanonicalName() + " must implements AccountIdAwareIF");
							
							sh.put(t,m);
						}
					}
					
					final SecurityPostHandler p = m.getAnnotation(SecurityPostHandler.class);
					if (p!=null){
						if (!Modifier.isStatic(m.getModifiers()))
							throw new RuntimeException("Method " + m.getName() + " must be static in class " + cls.getCanonicalName());
						
						for (SecurityPostHandler.Type t : p.value())
							sh.put(t, m);
					}											
				}
				
				securityHandlers.add(sh);
				log.debug("Add SecurityHandler: {}", sh);
			}
		}
	}

}
