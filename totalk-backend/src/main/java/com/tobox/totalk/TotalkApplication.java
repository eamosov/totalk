package com.tobox.totalk;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.management.MBeanServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.datastax.driver.core.Session;
import com.datastax.driver.mapping.MappingManager;
import com.knockchat.appserver.AppserverApplication;
import com.knockchat.cassandra.DbMetadataParser;
import com.knockchat.sql.migration.logging.ColorOffConverter;
import com.knockchat.sql.migration.logging.ColorOnConverter;

import ch.qos.logback.classic.PatternLayout;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.config.ConfigurationFactory;
import net.sf.ehcache.management.ManagementService;


@Configuration
@ImportResource("classpath:context.xml")
@ComponentScan(value="com.tobox.totalk")
@EnableTransactionManagement(mode=AdviceMode.ASPECTJ)
public class TotalkApplication {

    @Autowired
    private AnnotationConfigApplicationContext context;

    static {
        PatternLayout.defaultConverterMap.put("coloron", ColorOnConverter.class.getName());
        PatternLayout.defaultConverterMap.put("coloroff", ColorOffConverter.class.getName());
    }

    private static final Logger log = LoggerFactory.getLogger(TotalkApplication.class);

         
    @Bean
    public MappingManager mappingManager(Session session){
		final MappingManager mm = new MappingManager(session, DbMetadataParser.INSTANCE);
    	return mm;
    }
        
    @Bean
    public CacheManager ehCache(ApplicationContext context) throws IOException{
    	try(InputStream inputStream = TotalkApplication.class.getResourceAsStream("/ehcache.xml")){
    		net.sf.ehcache.config.Configuration config = ConfigurationFactory.parseConfiguration(inputStream);    		
    		
    		if (!AppserverApplication.isJGroupsEnabled(context.getEnvironment())){
        		config.getCacheManagerPeerProviderFactoryConfiguration().clear();
        		
        		for (CacheConfiguration cc: config.getCacheConfigurations().values()){
        			cc.getCacheEventListenerConfigurations().clear();
        		}
        		
        		config.getDefaultCacheConfiguration().getCacheEventListenerConfigurations().clear();    			
    		}
    		
    		final CacheManager cm = CacheManager.create(config);
    		return cm;
    	}
    }
    
	private static void initEhCacheMbeans(){
		final CacheManager manager = AppserverApplication.INSTANCE.context.getBean(CacheManager.class);
		final MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		ManagementService.registerMBeans(manager, mBeanServer, true, true, true, true);		
	}

    /**
     * @param args
     * @throws Exception
     */
    public static void main(final String[] args) throws Exception {

		//Хак, разрешающий конфликт elasticsearch с bigmemory-go
		final ClassLoader cl = new ClassLoader(Thread.currentThread().getContextClassLoader()){
			
			@Override
			 public Enumeration<URL> getResources(String name) throws IOException {
				
				if (!name.equals("META-INF/services/org.apache.lucene.codecs.Codec"))
					return super.getResources(name);
				
				
				final List<URL> urls = new ArrayList<URL>();
				final Enumeration<URL> e = super.getResources(name);
				while(e.hasMoreElements()){
					final URL u = e.nextElement();
					
					if (u.toExternalForm().contains("bigmemory-go"))
						continue;
					
					urls.add(u);
				}
				 
				return new Vector(urls).elements();
			 }
		};
				
		Thread.currentThread().setContextClassLoader(cl);
		
		AppserverApplication.INSTANCE.addScanPath("com.tobox.totalk");		
		AppserverApplication.INSTANCE.addPropertySource("classpath:totalk.properties");
		
		AppserverApplication.INSTANCE.registerAnnotatedClasses(TotalkApplication.class);
		
		try{
			AppserverApplication.INSTANCE.addPropertySource("classpath:totalk-local.properties");
			log.info("totalk-local.properties is loaded");
		}catch (final IOException e){
			log.info("totalk-local.properties not found");
		}
				
		AppserverApplication.INSTANCE.init(args, TotalkApplication.class.getPackage().getImplementationVersion());
		
		initEhCacheMbeans();		

		AppserverApplication.INSTANCE.start();

        AppserverApplication.INSTANCE.waitExit();
    }


}
