package com.fpinjava.errorhandling.exercise06_03;

import static org.junit.Assert.*;

import org.junit.Test;

public class InsuranceTest {

  @Test
  public void testParseInsuranceRateQuote() {
    assertEquals("Some(11.5)", Insurance.parseInsuranceRateQuote("25", "2").toString());
  }

  @Test
  public void testParseInsuranceRateQuoteLeftError() {
    assertEquals("None", Insurance.parseInsuranceRateQuote("25,2", "2").toString());
  }

  @Test
  public void testParseInsuranceRateQuoteRightError() {
    assertEquals("None", Insurance.parseInsuranceRateQuote("30", "3,4").toString());
  }

}
