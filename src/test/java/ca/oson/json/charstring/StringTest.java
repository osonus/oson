package ca.oson.json.charstring;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import org.junit.Test;

import ca.oson.json.support.TestCaseBase;
import ca.oson.json.ClassMapper;
import ca.oson.json.FieldMapper;
import ca.oson.json.domain.Car;
import ca.oson.json.domain.DateTime;

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
	   
	   
	   @Test
	   public void testSerializeTextWithLength() {
		   String value = "This is my new car. Do not drive without my permission!";

		   int length = 18;
		   String result = oson.setClassMappers(new ClassMapper(String.class).setLength(length))
				   .serialize(value);

		   assertTrue((result.length() == length));
	   }
	   
	   @Test
	   public void testSerializeStringClass() {
		   StringClass obj = new StringClass();
		   
		   obj.value = "This is a test of length of String.";

		   String json = oson.serialize(obj);
		   String expected = "{\"value\":\"This\"}";
		   assertEquals(expected, json);
		   
		   oson.setAnnotationSupport(false);
		   expected = "{\"value\":\"This is a test of length of String.\"}";
		   json = oson.serialize(obj);
		   assertEquals(expected, json);
		   
		   oson.setLength(14);
		   expected = "{\"value\":\"This is a test\"}";
		   json = oson.serialize(obj);
		   assertEquals(expected, json);
		   
		   oson.setAnnotationSupport(true)
		   		.setFieldMappers(new FieldMapper[] {
					new FieldMapper("value", StringClass.class).setLength(9)
			});
		   expected = "{\"value\":\"This is a\"}";
		   json = oson.serialize(obj);
		   assertEquals(expected, json);
	   }
}

@ca.oson.json.annotation.ClassMapper(length=10)
class StringClass {
	@ca.oson.json.annotation.FieldMapper(length=4)
	public String value;
	
}
