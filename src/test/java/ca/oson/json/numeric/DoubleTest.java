package ca.oson.json.numeric;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import ca.oson.json.Oson.ClassMapper;
import ca.oson.json.support.TestCaseNumeric;

public class DoubleTest extends TestCaseNumeric {
	   
	   @Test
	   public void testSerializeDouble() {
		   Double value = 7899990d;
		   String expected = "7899990";
		   
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
		   double value = 12789d;
		   String expected = "12789";
		   
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
		   Long min = 10009l;
		   String expected = "10009";
		   
		   oson.setClassMappers(new ClassMapper(Double.class).setMin(min));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeDoubleWithMax() {
		   Double value = 1009999d;
		   Long max = 300l;
		   String expected = "300";
		   
		   oson.setClassMappers(new ClassMapper(Double.class).setMax(max));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeDoubleWithMin() {
		   String value = "1";
		   Long min = 10l;
		   Double expected = min.doubleValue();
		   
		   oson.setClassMappers(new ClassMapper(Double.class).setMin(min));
		   
		   Double result = oson.deserialize(value, Double.class);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeDoubleWithMax() {
		   String value = "1999";
		   Long max = 99l;
		   Double expected = max.doubleValue();
		   
		   oson.setClassMappers(new ClassMapper(Double.class).setMax(max));
		   
		   Double result = oson.deserialize(value, Double.class);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeDoubleWithPrecision() {
		   Double value = 1234567891.98765;
		   Integer precision = 4;
		   String expected = "1234000000";
		   
		   oson.setPrecision(precision);
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeDoubleWithScale() {
		   Double value = 1234567891.98765;
		   Integer scale = 3;
		   String expected = "1234567891.988";
		   
		   oson.setClassMappers(new ClassMapper(Double.class).setScale(scale));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testSerializeDoubleWithPrecisionScale() {
		   Double value = 1234567891.98765;
		   Integer scale = 3;
		   Integer precision = 3;
		   String expected = "1230000000.000";
		   
		   oson.setClassMappers(new ClassMapper(Double.class).setPrecision(precision).setScale(scale));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
}