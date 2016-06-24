package ca.oson.json.numeric;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import ca.oson.json.Oson.ClassMapper;
import ca.oson.json.support.TestCaseBase;

public class DoubleTest extends TestCaseBase {
	   
	   @Test
	   public void testSerializeDouble() {
		   Double value = 7899990d;
		   String expected = "" + value;
		   
		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testDeserializeDouble() {
		   double value = 0;
		   String text = oson.serialize(value);
		   
		   double result = oson.deserialize(text, Double.class);
		   
		   assertEquals(value, result);
	   }
	   
	   @Test
	   public void testSerializeDoubleWithFunction() {
		   double value = 60000.0;
		   String expected = "# " + value;
		   
		   oson.setClassMappers(new ClassMapper(Double.class).setSerializer((Double p) -> "# " + p));

		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   

	   @Test
	   public void testDeserializeDoubleWithFunctionObject2Object() {
		   String value = "# 7";
		   Double expected = 7d;

		   oson.setClassMappers(new ClassMapper(Double.class)
		   	.setDeserializer((Object p) -> {
				   if (p.toString().startsWith("# ")) {
					   return p.toString().substring(2);
				   } else {
					   return p;
				   }
			   }));

		   Double result = oson.deserialize(value, Double.class);

		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testDeserializeDoubleWithFunctionString2Double() {
		   String value = "# 7";
		   Double expected = 7d;

		   oson.setClassMappers(new ClassMapper(Double.class)
		   	.setDeserializer((String p) -> {
				   if (p.startsWith("# ")) {
					   p = p.substring(2);
				   }
				return Double.parseDouble(p);
			   }));

		   Double result = oson.deserialize(value, Double.class);

		   assertEquals(expected, result);
	   }

	   @Test
	   public void testSerializeDoubleWithFunctionAtomicInteger() {
		   // a minimum value of -128 and a maximum value of 127 (inclusive)
		   double value = 12789d;
		   String expected = "12789.0";
		   
		   oson.setClassMappers(new ClassMapper(Double.class)
		   	.setSerializer((Object p) -> new AtomicInteger(Integer.parseInt(p.toString()))));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeDoubleWithFunctionObject2AtomicLong() {
		   String value = "100";
		   Double expected = 100d;
		   
		   oson.setClassMappers(new ClassMapper(Double.class)
		   	.setDeserializer((Object p) -> new AtomicLong(Long.parseLong(p.toString()))));
		   
		   Double result = oson.deserialize(value, Double.class);

		   assertEquals(expected, result);
	   }

	   @Test
	   public void testSerializeDoubleWithMin() {
		   Double value = 1d;
		   Integer min = 10009;
		   String expected = "10009.0";
		   
		   oson.setClassMappers(new ClassMapper(Double.class).setMin(min));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeDoubleWithMax() {
		   Double value = 1009999d;
		   Integer max = 300;
		   String expected = "300.0";
		   
		   oson.setClassMappers(new ClassMapper(Double.class).setMax(max));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeDoubleWithMin() {
		   String value = "1";
		   Integer min = 10;
		   Double expected = min.doubleValue();
		   
		   oson.setClassMappers(new ClassMapper(Double.class).setMin(min));
		   
		   Double result = oson.deserialize(value, Double.class);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeDoubleWithMax() {
		   String value = "1999";
		   Integer max = 99;
		   Double expected = max.doubleValue();
		   
		   oson.setClassMappers(new ClassMapper(Double.class).setMax(max));
		   
		   Double result = oson.deserialize(value, Double.class);

		   assertEquals(expected, result);
	   }
}