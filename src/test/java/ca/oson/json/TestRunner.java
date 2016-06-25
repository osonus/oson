package ca.oson.json;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.enumbooleandate.EnumBooleanDateTestSuite;
import ca.oson.json.numeric.NumericTestSuite;

public class TestRunner {
	public static void main(String[] args) {
		Class[] testSuites = new Class[] {NumericTestSuite.class, EnumBooleanDateTestSuite.class};
		
		int i = 1;
		for (Class cl: testSuites) {
			System.out.println(i + ". " + cl.getSimpleName() + ":\n");
			
			Result result = JUnitCore.runClasses(cl);
			for (Failure failure : result.getFailures()) {
				System.out.println(failure.toString());
			}
			System.out.println("RunCount: " + result.getRunCount());
			System.out.println("RunTime: " + result.getRunTime());
			System.out.println("wasSuccessful: " + result.wasSuccessful());
			System.out.println("\n");
	
			// ((OsonIO) new OsonIO().pretty(true).setDefaultType(JSON_INCLUDE.NON_DEFAULT).setLevel(2)).print(result);
			i++;
		}
	}
}
