package com.tobox.test.suites;

import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.THttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.web.client.RestTemplate;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.tobox.auth.protocol.UserDataRequest;
import com.tobox.auth.protocol.UserIdResponse;
import com.tobox.test.TestApplication;
import com.tobox.totalk.thrift.TotalkService;
import com.tobox.totalk.thrift.exceptions.WrappedException;
import com.tobox.totalk.thrift.types.Review;

@ContextConfiguration(classes = TestApplication.class)
public class TotalkServiceTest extends AbstractTestNGSpringContextTests {
	
	private static final Logger log = LoggerFactory.getLogger(TotalkServiceTest.class);

	private CloseableHttpClient httpClient;
	private HttpComponentsClientHttpRequestFactory httpRequestFactory;
	private RestTemplate restTemplate;
	
	private BasicCookieStore cookies = new BasicCookieStore();
	private TotalkService.Iface totalkService; 
	
	private String backendHost = "localhost";
	private int backendPort = 8080;
	
	@BeforeClass
	public void beforeSuite() throws Exception{
		httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookies).build();
		
		httpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		restTemplate = new RestTemplate(httpRequestFactory);
		
		final THttpClient tr = new THttpClient("http://localhost:11000/TBINARY", httpClient);
		totalkService = new TotalkService.Client(new TBinaryProtocol(tr));
	}
	
	@AfterClass(alwaysRun = true)
	public void afterSuite(){
		
		
	}
	
	private String path(String path){
		return String.format("http://%s:%d/%s",  backendHost, backendPort, path);
	}
	
	@Test
	public void authAnonymous() throws WrappedException, TException{
		
		final ResponseEntity<UserIdResponse> r = restTemplate.postForEntity(path("/api/beta/auth/anonymous"), new UserDataRequest(), UserIdResponse.class);
		final UserIdResponse resp = r.getBody();
		log.info("authAnonymous: {}", resp.getId());
		
		Review review = new Review();
		review.setEntityId("18fd7de2-4de8-4dbb-8eed-a03d4067a84d");
		review.setTitle("title1");
		review.setBody("boidy1");
		
		review = totalkService.addReview(review);
		log.info("review:{}", review);
	}
	
}
