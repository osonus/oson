package ca.oson.json.support;

import junit.framework.TestCase;

import org.junit.Before;

import ca.oson.json.OsonIO;
import ca.oson.json.Oson.JSON_PROCESSOR;

public abstract class TestCaseNumeric extends TestCase {
	protected OsonIO oson;
	public static JSON_PROCESSOR processor = JSON_PROCESSOR.OSON;
	
   @Before 
   public void setUp() {
	   oson = new OsonIO();
	   oson.setJsonProcessor(processor);
	   oson.setCommentPatterns(new String[] {"//[^\n\r]*\n?", "/\\*[^\\*/]*\\*/"});
   }
   
   
   //tearDown used to close the connection or clean up activities
   public void tearDown() {
   }
}