package ca.oson.json;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import ca.oson.json.numeric.NumericTestSuite;

public class TestRunner {
	   public static void main(String[] args) {
		      Result result = JUnitCore.runClasses(NumericTestSuite.class);
		      for (Failure failure : result.getFailures()) {
		         System.err.println(failure.toString());
		      }
		      System.out.println(result.wasSuccessful());
		   }
}
