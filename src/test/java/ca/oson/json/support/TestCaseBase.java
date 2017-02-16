package ca.oson.json.support;

import org.junit.Before;

import ca.oson.json.OsonIO;
import ca.oson.json.util.ObjectUtil;
import junit.framework.TestCase;

public abstract class TestCaseBase extends TestCase {
	protected OsonIO oson;

   @Before 
   protected void setUp() {
	   ObjectUtil.getJSONObject(null);
	   oson = new OsonIO();
   }
   
   
   //tearDown used to close the connection or clean up activities
   protected void tearDown() {
   }
}
