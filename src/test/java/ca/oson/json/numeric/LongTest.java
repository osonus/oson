package ca.oson.json.numeric;

import java.math.BigInteger;
import java.util.function.Function;

import org.junit.Test;

import ca.oson.json.Oson.ClassMapper;
import ca.oson.json.Oson.FIELD_NAMING;
import ca.oson.json.support.TestCaseBase;

public class LongTest extends TestCaseBase {
	   
	   @Test
	   public void testSerializeLong() {
		   Long value = 10l;
		   String expected = "10";
		   
		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testDeserializeLong() {
		   long value = 0;
		   String text = oson.serialize(value);
		   
		   long result = oson.deserialize(text, Long.class);
		   
		   assertEquals(value, result);
	   }
	   
	   @Test
	   public void testSerializeLongWithFunction() {
		   long value = 6;
		   String expected = "# 6";
		   
		   oson.setClassMappers(new ClassMapper(Long.class).setSerializer(p -> "# " + p));

		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testDeserializeLongWithFunction() {
		   String value = "# 7";
		   Long expected = 7l;

		   oson.setClassMappers(new ClassMapper(Long.class)
		   	.setDeserializer(p -> {
		   			String str = p.toString();
				   if (str.startsWith("# ")) {
					   return str.substring(2);
				   } else {
					   return Long.parseLong(p.toString());
				   }
			   }));

		   Long result = oson.deserialize(value, Long.class);

		   assertEquals(expected, result);
	   }

	   @Test
	   public void testSerializeLongWithFunctionBigDecimal() {
		   long value = 10000;
		   String expected = "10000";
		   
		   oson.setClassMappers(new ClassMapper(Long.class)
		   	.setSerializer((Object p) -> new java.math.BigDecimal(p.toString())));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   } 
	   
	   @Test
	   public void testDeserializeLongWithFunctionBigInteger() {
		   String value = "10000";
		   Long expected = 10000l;
		   
		   oson.setClassMappers(new ClassMapper(Long.class)
		   	.setDeserializer(p -> BigInteger.valueOf(Long.parseLong(p.toString()))));
		   
		   Long result = oson.deserialize(value, Long.class);

		   assertEquals(expected, result);
	   }
}