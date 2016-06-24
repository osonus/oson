package ca.oson.json.numeric;

import java.math.BigInteger;
import org.junit.Test;

import ca.oson.json.Oson.ClassMapper;
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
		   
		   oson.setClassMappers(new ClassMapper(Long.class).setSerializer((Long p) -> "# " + p));

		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   

	   @Test
	   public void testDeserializeLongWithFunctionObject2Object() {
		   String value = "# 7";
		   Long expected = 7l;

		   oson.setClassMappers(new ClassMapper(Long.class)
		   	.setDeserializer((Object p) -> {
				   if (p.toString().startsWith("# ")) {
					   return p.toString().substring(2);
				   } else {
					   return p;
				   }
			   }));

		   Long result = oson.deserialize(value, Long.class);

		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testDeserializeLongWithFunctionString2Long() {
		   String value = "# 7";
		   Long expected = 7l;

		   oson.setClassMappers(new ClassMapper(Long.class)
		   	.setDeserializer((String p) -> {
				   if (p.startsWith("# ")) {
					   p = p.substring(2);
				   }
				return Long.parseLong(p);
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
	   public void testDeserializeLongWithFunctionObject2BigInteger() {
		   String value = "10000";
		   Long expected = 10000l;
		   
		   oson.setClassMappers(new ClassMapper(Long.class)
		   	.setDeserializer((Object p) -> BigInteger.valueOf(Long.parseLong(p.toString()))));
		   
		   Long result = oson.deserialize(value, Long.class);

		   assertEquals(expected, result);
	   }

	   @Test
	   public void testSerializeLongWithMin() {
		   Long value = 1l;
		   Integer min = 10;
		   String expected = min + "";
		   
		   oson.setClassMappers(new ClassMapper(Long.class).setMin(min));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeLongWithMax() {
		   Long value = 1000000000l;
		   Integer max = 2000;
		   String expected = max + "";
		   
		   oson.setClassMappers(new ClassMapper(Long.class).setMax(max));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeLongWithMin() {
		   String value = "1";
		   Integer min = 10;
		   Long expected = min.longValue();
		   
		   oson.setClassMappers(new ClassMapper(Long.class).setMin(min));
		   
		   Long result = oson.deserialize(value, Long.class);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeLongWithMax() {
		   String value = "19999999";
		   Integer max = 10000;
		   Long expected = max.longValue();
		   
		   oson.setClassMappers(new ClassMapper(Long.class).setMax(max));
		   
		   Long result = oson.deserialize(value, Long.class);

		   assertEquals(expected, result);
	   }
}