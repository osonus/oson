package ca.oson.json.numeric;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import ca.oson.json.Oson.ClassMapper;
import ca.oson.json.support.TestCaseBase;

public class ByteTest extends TestCaseBase {
	   
	   @Test
	   public void testSerializeByte() {
		   Byte value = 10;
		   String expected = "10";
		   
		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testDeserializeByte() {
		   byte value = 0;
		   String text = oson.serialize(value);
		   
		   byte result = oson.deserialize(text, Byte.class);
		   
		   assertEquals(value, result);
	   }
	   
	   @Test
	   public void testSerializeByteWithFunction() {
		   byte value = 6;
		   String expected = "# 6";
		   
		   oson.setClassMappers(new ClassMapper(Byte.class).setSerializer((Byte p) -> "# " + p));

		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   

	   @Test
	   public void testDeserializeByteWithFunctionObject2Object() {
		   String value = "# 7";
		   Byte expected = 7;

		   oson.setClassMappers(new ClassMapper(Byte.class)
		   	.setDeserializer((Object p) -> {
				   if (p.toString().startsWith("# ")) {
					   return p.toString().substring(2);
				   } else {
					   return p;
				   }
			   }));

		   Byte result = oson.deserialize(value, Byte.class);

		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testDeserializeByteWithFunctionString2Byte() {
		   String value = "# 7";
		   Byte expected = 7;

		   oson.setClassMappers(new ClassMapper(Byte.class)
		   	.setDeserializer((String p) -> {
				   if (p.startsWith("# ")) {
					   p = p.substring(2);
				   }
				return Byte.parseByte(p);
			   }));

		   Byte result = oson.deserialize(value, Byte.class);

		   assertEquals(expected, result);
	   }

	   @Test
	   public void testSerializeByteWithFunctionAtomicInteger() {
		   // a minimum value of -128 and a maximum value of 127 (inclusive)
		   byte value = 127;
		   String expected = "127";
		   
		   oson.setClassMappers(new ClassMapper(Byte.class)
		   	.setSerializer((Object p) -> new AtomicInteger(Integer.parseInt(p.toString()))));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeByteWithFunctionObject2AtomicLong() {
		   String value = "100";
		   Byte expected = 100;
		   
		   oson.setClassMappers(new ClassMapper(Byte.class)
		   	.setDeserializer((Object p) -> new AtomicLong(Long.parseLong(p.toString()))));
		   
		   Byte result = oson.deserialize(value, Byte.class);

		   assertEquals(expected, result);
	   }

	   @Test
	   public void testSerializeByteWithMin() {
		   Byte value = 1;
		   Integer min = 10;
		   String expected = min + "";
		   
		   oson.setClassMappers(new ClassMapper(Byte.class).setMin(min));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeByteWithMax() {
		   Byte value = 127;
		   Integer max = 125;
		   String expected = max + "";
		   
		   oson.setClassMappers(new ClassMapper(Byte.class).setMax(max));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeByteWithMin() {
		   String value = "1";
		   Integer min = 10;
		   Byte expected = min.byteValue();
		   
		   oson.setClassMappers(new ClassMapper(Byte.class).setMin(min));
		   
		   Byte result = oson.deserialize(value, Byte.class);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeByteWithMax() {
		   String value = "1999";
		   Integer max = 99;
		   Byte expected = max.byteValue();
		   
		   oson.setClassMappers(new ClassMapper(Byte.class).setMax(max));
		   
		   Byte result = oson.deserialize(value, Byte.class);

		   assertEquals(expected, result);
	   }
}