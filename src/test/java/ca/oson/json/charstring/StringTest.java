package ca.oson.json.charstring;

import org.junit.Test;

import ca.oson.json.support.TestCaseBase;
import ca.oson.json.Oson.ClassMapper;
import ca.oson.json.domain.Car;

public class StringTest extends TestCaseBase {
	
	   @Test
	   public void testSerializeTextWithFunction() {
		   String value = "THIS IS A TEST.";
		   String expected = "this is a test.";
		   
		   String result = oson.setClassMappers(new ClassMapper(String.class)
		   		.setSerializer((String p) -> p.toLowerCase()))
				.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testSerializeTextWithFunctionReturningObject() {
		   String value = "This is my new car";

		   String result = oson.setClassMappers(new ClassMapper(String.class)
		   		.setSerializer((Object p) -> {
		   			Car car = new Car(p.toString());
		   			// only once used for this string
		   			oson.setClassMappers((ClassMapper)null);
		   			return car;
		   		})).pretty(true)
				.serialize(value);
		   
		   // System.err.println(result);

		   assertTrue(result.contains(value));
	   }
}
