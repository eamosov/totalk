package com.tobox.test.suites;

import static org.hamcrest.MatcherAssert.assertThat;

import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.THttpClient;
import org.hamcrest.Matchers;
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

import com.google.common.collect.Collections2;
import com.tobox.auth.protocol.UserDataRequest;
import com.tobox.auth.protocol.UserIdResponse;
import com.tobox.test.TestApplication;
import com.tobox.totalk.thrift.TotalkService;
import com.tobox.totalk.thrift.exceptions.DeletedException;
import com.tobox.totalk.thrift.exceptions.NoAdvException;
import com.tobox.totalk.thrift.exceptions.WrappedException;
import com.tobox.totalk.thrift.types.Comment;
import com.tobox.totalk.thrift.types.Comments;
import com.tobox.totalk.thrift.types.EntityType;
import com.tobox.totalk.thrift.types.Review;
import com.tobox.totalk.thrift.types.ReviewType;
import com.tobox.totalk.thrift.types.Reviews;

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
	private String testAdvId = "18fd7de2-4de8-4dbb-8eed-a03d4067a84d";
	
	private UserIdResponse user;
	
	@BeforeClass
	public void beforeSuite() throws Exception{
		httpClient = HttpClientBuilder.create().setDefaultCookieStore(cookies).build();
		
		httpRequestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		restTemplate = new RestTemplate(httpRequestFactory);
		
		final THttpClient tr = new THttpClient("http://localhost:11000/TBINARY", httpClient);
		totalkService = new TotalkService.Client(new TBinaryProtocol(tr));
		
		
		final ResponseEntity<UserIdResponse> r = restTemplate.postForEntity(path("/api/beta/auth/anonymous"), new UserDataRequest(), UserIdResponse.class);
		user = r.getBody();
		log.info("authAnonymous: {}", user.getId());
	}
	
	@AfterClass(alwaysRun = true)
	public void afterSuite(){
		
		
	}
	
	private String path(String path){
		return String.format("http://%s:%d/%s",  backendHost, backendPort, path);
	}
	
	@Test
	public void testAddReview() throws WrappedException, TException, InterruptedException{
		
		final Reviews reviewsBefore = totalkService.getByEntity(EntityType.ADV, testAdvId, ReviewType.OPINION, 0, 100);
				
		Review review = new Review();
		review.setEntityId(testAdvId);
		review.setEntityType(EntityType.ADV);
		review.setTitle("title1");
		review.setBody("boidy1");
		review.setType(ReviewType.OPINION);
		
		review = totalkService.addReview(review);
		log.info("review:{}", review);
		assertThat(review.getId(), Matchers.notNullValue());
		
		Review review2  = totalkService.getReviewById(review.getId());
		assertThat(review2, Matchers.equalTo(review));
		
		Thread.sleep(1000);// ES index
		
		final Reviews reviewsAfter = totalkService.getByEntity(EntityType.ADV, review.getEntityId(), ReviewType.OPINION, 0, 100);
		assertThat(reviewsAfter.getTotal(), Matchers.equalTo(reviewsBefore.getTotal() +1));
		assertThat(Collections2.transform(reviewsAfter.getReviews(), Review::getId), Matchers.hasItems(review.getId()));
	}
	
	@Test
	public void testAddComment() throws WrappedException, NoAdvException, DeletedException, TException, InterruptedException{

		Review review = new Review();
		review.setEntityId(testAdvId);
		review.setEntityType(EntityType.ADV);
		review.setTitle("title1");
		review.setBody("boidy1");
		review.setType(ReviewType.OPINION);
		
		review = totalkService.addReview(review);
		
		final Comments beforeAdd = totalkService.getComments(review.getId(), 0, 100);
		
		Comment comment = new Comment();
		comment.setReviewId(review.getId());
		comment.setBody("asdaqweqwe");
		comment = totalkService.addComment(comment);
		assertThat(comment.getId(), Matchers.notNullValue());
		assertThat(comment.getBody(), Matchers.notNullValue());
		assertThat(comment.getReviewId(), Matchers.equalTo(review.getId()));
		
		Comment comment2  = totalkService.getCommentById(comment.getId());
		assertThat(comment2, Matchers.equalTo(comment));
		
		Thread.sleep(1000);// ES index
		final Comments afterAdd = totalkService.getComments(review.getId(), 0, 100);
		assertThat(afterAdd.getTotal(), Matchers.equalTo(beforeAdd.getTotal() +1));
		assertThat(Collections2.transform(afterAdd.getComments(), Comment::getId), Matchers.hasItems(comment.getId()));

	}
	
}
