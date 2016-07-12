package ca.oson.json;

import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import ca.oson.json.Oson.JSON_PROCESSOR;
import ca.oson.json.support.TestCaseBase;

public class TestRunner {
	public static void main(String[] args) {
		//TestCaseBase.processor = JSON_PROCESSOR.GSON;//.GSON;
		Class[] testSuites = new Class[] {NumericTestSuite.class, EnumBooleanDateTestSuite.class,
				CharacterStringTestSuite.class, ListArrayMapTestSuite.class, UserGuideTestSuite.class,
				AnnotationTestSuite.class, GsonTestSuite.class, ObjectTestSuite.class};
		
		int i = 1;
		int total_success = 0;
		int total_fails = 0;
		long runTime = 0;
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
			runTime += result.getRunTime();
			
			total_fails += result.getFailureCount();
			total_success += result.getRunCount() - result.getFailureCount();
	
			// ((OsonIO) new OsonIO().pretty(true).setDefaultType(JSON_INCLUDE.NON_DEFAULT).setLevel(2)).print(result);
			i++;
		}
		
		System.out.println("Total success count: " + total_success);
		System.out.println("Total failure count: " + total_fails);
		System.out.println("Total RunTime: " + runTime);
	}
}
