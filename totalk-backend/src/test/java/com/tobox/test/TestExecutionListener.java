package com.tobox.test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Created by pavel on 7.10.14.
 */
public class TestExecutionListener implements ITestListener {
  private static final Logger log = LoggerFactory.getLogger(TestExecutionListener.class);

  @Override
  public void onTestFailure(ITestResult testResult) {
    log.error("ERROR: {}", testResult.getThrowable());
    log.error("Error in test: {}\n", getCaseName(testResult));
  }

  @Override
  public void onTestSkipped(ITestResult result) {

  }

  @Override
  public void onTestFailedButWithinSuccessPercentage(ITestResult result) {

  }

  @Override
  public void onStart(ITestContext context) {

  }

  @Override
  public void onFinish(ITestContext context) {

  }


  @Override
  public void onTestStart(ITestResult result) {
    log.info("Test method start: {}", getCaseName(result));
    log.info("Test method description: {}", result.getMethod().getDescription());
  }

  @Override
  public void onTestSuccess(ITestResult result) {
    log.info("Test method successfully finished: {}\n", getCaseName(result));
  }

  protected String getCaseName(ITestResult result) {
    return result.getTestClass().getRealClass().getSimpleName() + "." + result.getMethod().getMethodName();
  }
}
