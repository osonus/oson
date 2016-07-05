package ca.oson.json.numeric;

import java.math.BigInteger;
import org.junit.Test;

import ca.oson.json.Oson.ClassMapper;
import ca.oson.json.support.TestCaseNumeric;

public class ShortTest extends TestCaseNumeric {
	   
	   @Test
	   public void testSerializeShort() {
		   Short value = 10;
		   String expected = "10";
		   
		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testDeserializeShort() {
		   short value = 0;
		   String text = oson.serialize(value);
		   
		   short result = oson.deserialize(text, Short.class);
		   
		   assertEquals(value, result);
	   }
	   
	   @Test
	   public void testSerializeShortWithFunction() {
		   short value = 6;
		   String expected = "# 6";
		   
		   oson.setClassMappers(new ClassMapper(Short.class).setSerializer((Short p) -> "# " + p));

		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   

	   @Test
	   public void testDeserializeShortWithFunctionObject2Object() {
		   String value = "# 7";
		   Short expected = 7;

		   oson.setClassMappers(new ClassMapper(Short.class)
		   	.setDeserializer((Object p) -> {
				   if (p.toString().startsWith("# ")) {
					   return p.toString().substring(2);
				   } else {
					   return p;
				   }
			   }));

		   Short result = oson.deserialize(value, Short.class);

		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testDeserializeShortWithFunctionString2Short() {
		   String value = "# 7";
		   Short expected = 7;

		   oson.setClassMappers(new ClassMapper(Short.class)
		   	.setDeserializer((String p) -> {
				   if (p.startsWith("# ")) {
					   p = p.substring(2);
				   }
				return Short.parseShort(p);
			   }));

		   Short result = oson.deserialize(value, Short.class);

		   assertEquals(expected, result);
	   }

	   @Test
	   public void testSerializeShortWithFunctionBigDecimal() {
		   short value = 10000;
		   String expected = "10000";
		   
		   oson.setClassMappers(new ClassMapper(Short.class)
		   	.setSerializer((Object p) -> new java.math.BigDecimal(p.toString())));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeShortWithFunctionObject2BigInteger() {
		   String value = "10000";
		   Short expected = 10000;
		   
		   oson.setClassMappers(new ClassMapper(Short.class)
		   	.setDeserializer((Object p) -> BigInteger.valueOf(Short.parseShort(p.toString()))));
		   
		   Short result = oson.deserialize(value, Short.class);

		   assertEquals(expected, result);
	   }

	   @Test
	   public void testSerializeShortWithMin() {
		   Short value = 1;
		   Long min = 10l;
		   String expected = min + "";
		   
		   oson.setClassMappers(new ClassMapper(Short.class).setMin(min));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeShortWithMax() {
		   Short value = 10000;
		   Long max = 2000l;
		   String expected = max + "";
		   
		   oson.setClassMappers(new ClassMapper(Short.class).setMax(max));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeShortWithMin() {
		   String value = "1";
		   Long min = 10l;
		   Short expected = min.shortValue();
		   
		   oson.setClassMappers(new ClassMapper(Short.class).setMin(min));
		   
		   Short result = oson.deserialize(value, Short.class);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeShortWithMax() {
		   String value = "1999";
		   Long max = 200l;
		   Short expected = max.shortValue();
		   
		   oson.setClassMappers(new ClassMapper(Short.class).setMax(max));
		   
		   Short result = oson.deserialize(value, Short.class);

		   assertEquals(expected, result);
	   }
}