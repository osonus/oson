package ca.oson.json;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.numeric.NumericTestSuite;

public class TestRunner {
	   public static void main(String[] args) {
		      Result result = JUnitCore.runClasses(NumericTestSuite.class);
		      for (Failure failure : result.getFailures()) {
		         System.err.println(failure.toString());
		      }
		      System.out.println("RunCount: " + result.getRunCount());
		      System.out.println("RunTime: " + result.getRunTime());
		      System.out.println("wasSuccessful: " + result.wasSuccessful());
		      
		      //((OsonIO) new OsonIO().pretty(true).setDefaultType(JSON_INCLUDE.NON_DEFAULT).setLevel(2)).print(result);
		   }
}
