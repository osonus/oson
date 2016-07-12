package ca.oson.json.support;

import org.junit.Before;

import ca.oson.json.Oson.JSON_PROCESSOR;
import ca.oson.json.OsonIO;
import junit.framework.TestCase;

public abstract class TestCaseBase extends TestCase {
	protected OsonIO oson;
	public static JSON_PROCESSOR processor = JSON_PROCESSOR.OSON;
	
   @Before 
   protected void setUp() {
	   oson = new OsonIO();
	   oson.setJsonProcessor(processor).setPrintErrorUseOsonInFailure(true);
   }
   
   
   //tearDown used to close the connection or clean up activities
   protected void tearDown() {
   }
}
