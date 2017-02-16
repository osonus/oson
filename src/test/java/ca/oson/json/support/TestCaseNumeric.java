package ca.oson.json.support;

import junit.framework.TestCase;

import org.junit.Before;

import ca.oson.json.OsonIO;

public abstract class TestCaseNumeric extends TestCaseBase {
   @Before 
   public void setUp() {
	   super.setUp();
	   oson.setCommentPatterns(new String[] {"//[^\n\r]*\n?", "/\\*[^\\*/]*\\*/"});
   }
   
   
   //tearDown used to close the connection or clean up activities
   public void tearDown() {
   }
}