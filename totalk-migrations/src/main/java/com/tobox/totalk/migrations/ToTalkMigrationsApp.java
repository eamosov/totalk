package com.tobox.totalk.migrations;

import org.everthrift.cassandra.CassandraClusterFactoryBean;
import org.everthrift.cassandra.migrator.CMigrationProcessor;
import org.everthrift.utils.logging.ColorOffConverter;
import org.everthrift.utils.logging.ColorOnConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.SimpleCommandLinePropertySource;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import ch.qos.logback.classic.PatternLayout;

@Configuration
@PropertySource({"totalk.properties", "totalk-local.properties"})
public class ToTalkMigrationsApp {
	
    static {
        PatternLayout.defaultConverterMap.put("coloron", ColorOnConverter.class.getName());
        PatternLayout.defaultConverterMap.put("coloroff", ColorOffConverter.class.getName());
    }
    
	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}    
	
	@Value("${cassandra.contactpoints}")
	private String contactpoints;

	@Value("${cassandra.port}")
	private Integer port;
	
	@Value("${cassandra.keyspace}")
	private String keyspace;

	@Value("${cassandra.migrator.login}")
	private String login;

	@Value("${cassandra.migrator.password}")
	private String password;
		
	@Value("${cassandra.migrations.basePackage}")
	private String basePackage;
	
	@Bean
	public Cluster cluster() throws Exception{
		final CassandraClusterFactoryBean f = new CassandraClusterFactoryBean();
		f.setContactPoints(contactpoints);
		f.setPort(port);
		f.setLogin(login);
		f.setPassword(password);
		return f.getObject();
	}
	
	@Bean
	public Session session(Cluster cluster) throws Exception{
		return cluster.connect(keyspace);
	}
	
	@Bean
	public CMigrationProcessor migrator(Session session){
		final CMigrationProcessor p = new CMigrationProcessor(basePackage);
		p.setSession(session);
		p.setSchemaVersionCf("schema_version_totalk");
		return p;
	}
	
	public static void main(final String[] args) throws Exception {
								
		final AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();
		
		ctx.register(ToTalkMigrationsApp.class);
		ctx.getEnvironment().getPropertySources().addFirst(new SimpleCommandLinePropertySource(args));
		ctx.refresh();
		
		CMigrationProcessor processor = ctx.getBean(CMigrationProcessor.class);
        
        try{
        	processor.call();
        }finally{
        	ctx.close();
        }        
	}
}
