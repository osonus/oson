package ca.oson.json.numeric;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import ca.oson.json.Oson.ClassMapper;
import ca.oson.json.support.TestCaseBase;

public class FloatTest extends TestCaseBase {
	   
	   @Test
	   public void testSerializeFloat() {
		   Float value = 7899990f;
		   String expected = "7899990";
		   
		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testDeserializeFloat() {
		   float value = 0;
		   String text = oson.serialize(value);
		   
		   float result = oson.deserialize(text, Float.class);
		   
		   assertEquals(value, result);
	   }
	   
	   @Test
	   public void testSerializeFloatWithFunction() {
		   float value = 60000.0f;
		   String expected = "# " + value;
		   
		   oson.setClassMappers(new ClassMapper(Float.class).setSerializer((Float p) -> "# " + p));

		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   

	   @Test
	   public void testDeserializeFloatWithFunctionObject2Object() {
		   String value = "# 7";
		   Float expected = 7f;

		   oson.setClassMappers(new ClassMapper(Float.class)
		   	.setDeserializer((Object p) -> {
				   if (p.toString().startsWith("# ")) {
					   return p.toString().substring(2);
				   } else {
					   return p;
				   }
			   }));

		   Float result = oson.deserialize(value, Float.class);

		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testDeserializeFloatWithFunctionString2Float() {
		   String value = "# 7";
		   Float expected = 7f;

		   oson.setClassMappers(new ClassMapper(Float.class)
		   	.setDeserializer((String p) -> {
				   if (p.startsWith("# ")) {
					   p = p.substring(2);
				   }
				return Float.parseFloat(p);
			   }));

		   Float result = oson.deserialize(value, Float.class);

		   assertEquals(expected, result);
	   }

	   @Test
	   public void testSerializeFloatWithFunctionAtomicInteger() {
		   // a minimum value of -128 and a maximum value of 127 (inclusive)
		   float value = 12789f;
		   String expected = "12789";
		   
		   oson.setClassMappers(new ClassMapper(Float.class)
		   	.setSerializer((Object p) -> new AtomicInteger(Integer.parseInt(p.toString()))));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeFloatWithFunctionObject2AtomicLong() {
		   String value = "100";
		   Float expected = 100f;
		   
		   oson.setClassMappers(new ClassMapper(Float.class)
		   	.setDeserializer((Object p) -> new AtomicLong(Long.parseLong(p.toString()))));
		   
		   Float result = oson.deserialize(value, Float.class);

		   assertEquals(expected, result);
	   }

	   @Test
	   public void testSerializeFloatWithMin() {
		   Float value = 1f;
		   Long min = 10009l;
		   String expected = "10009";
		   
		   oson.setClassMappers(new ClassMapper(Float.class).setMin(min));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeFloatWithMax() {
		   Float value = 1009999f;
		   Long max = 300l;
		   String expected = "300";
		   
		   oson.setClassMappers(new ClassMapper(Float.class).setMax(max));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeFloatWithMin() {
		   String value = "1";
		   Long min = 10l;
		   Float expected = min.floatValue();
		   
		   oson.setClassMappers(new ClassMapper(Float.class).setMin(min));
		   
		   Float result = oson.deserialize(value, Float.class);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeFloatWithMax() {
		   String value = "1999";
		   Long max = 99l;
		   Float expected = max.floatValue();
		   
		   oson.setClassMappers(new ClassMapper(Float.class).setMax(max));
		   
		   Float result = oson.deserialize(value, Float.class);

		   assertEquals(expected, result);
	   }
}