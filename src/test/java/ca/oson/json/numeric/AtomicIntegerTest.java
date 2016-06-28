package ca.oson.json.numeric;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import ca.oson.json.Oson.*;
import ca.oson.json.support.TestCaseBase;

public class AtomicIntegerTest extends TestCaseBase {
	   
	   @Test
	   public void testSerializeAtomicInteger() {
		   AtomicInteger value = new AtomicInteger(5);
		   String expected = "5";
		   
		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testDeserializeAtomicInteger() {
		   AtomicInteger value = new AtomicInteger(5);
		   String text = oson.serialize(value);
		   
		   AtomicInteger result = oson.deserialize(text, AtomicInteger.class);
		   
		   assertEquals(value.intValue(), result.intValue());
	   }
	   
	   @Test
	   public void testSerializeAtomicIntegerWithFunction() {
		   AtomicInteger value = new AtomicInteger(1);
		   String expected = "One";

		   oson.setClassMappers(new ClassMapper(AtomicInteger.class).setSerializer((AtomicInteger p) -> {
			   switch (p.intValue()) {
			   case 1: return "One";
			   case 2: return "Two";
			   case 3: return "Three";
			   case 4: return "Four";
			   case 5: return "Five";
			   case 6: return "Six";
			   case 7: return "Seven";
			   case 8: return "Eight";
			   case 9: return "Nine";
			   case 10: return "Ten";
			   default: return p.toString();
			   }
		   }));
		   
		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testSerializeAtomicIntegerWithAtomicInteger2JsonFunction() {
		   AtomicInteger value = new AtomicInteger(6);
		   String expected = "Six";

		   oson.setClassMappers(new ClassMapper(AtomicInteger.class).setSerializer((AtomicInteger p) -> {
			   switch (p.intValue()) {
			   case 1: return "One";
			   case 2: return "Two";
			   case 3: return "Three";
			   case 4: return "Four";
			   case 5: return "Five";
			   case 6: return "Six";
			   case 7: return "Seven";
			   case 8: return "Eight";
			   case 9: return "Nine";
			   case 10: return "Ten";
			   default: return p.toString();
			   }
		   }));
		   
		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testSerializeAtomicIntegerWithAtomicInteger2JsonFunctionDirect() {
		   AtomicInteger value = new AtomicInteger(8);
		   String expected = "Eight";

		   oson.setClassMappers(new ClassMapper(AtomicInteger.class).setSerializer(
				   (AtomicInteger p) -> {
			   switch (p.intValue()) {
			   case 1: return "One";
			   case 2: return "Two";
			   case 3: return "Three";
			   case 4: return "Four";
			   case 5: return "Five";
			   case 6: return "Six";
			   case 7: return "Seven";
			   case 8: return "Eight";
			   case 9: return "Nine";
			   case 10: return "Ten";
			   default: return p.toString();
			   }
		   }));
		   
		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testDeserializeAtomicIntegerWithFunction() {
		   String value = "Seven";
		   AtomicInteger expected = new AtomicInteger (7);

		   oson.setClassMappers(new ClassMapper(AtomicInteger.class)
		   	.setDeserializer((String p) -> {
		   			int val;
				   switch (p) {
				   case "One": val = 1; break;
				   case "Two": val = 2; break;
				   case "Three": val = 3; break;
				   case "Four": val = 4; break;
				   case "Five": val = 5; break;
				   case "Six": val = 6; break;
				   case "Seven": val = 7; break;
				   case "Eight": val = 8; break;
				   case "Nine": val = 9; break;
				   case "Ten": val = 10; break;
				   default: val = Integer.parseInt(p); break;
				   }
				   return new AtomicInteger(val);
			   }));

		   AtomicInteger result = oson.deserialize(value, AtomicInteger.class);
		   
		   assertEquals(expected.intValue(), result.intValue());
	   }

	   @Test
	   public void testSerializeAtomicIntegerWithFunctionEnum() {
		   AtomicInteger value = new AtomicInteger (1);
		   String expected = "UNDERSCORE_UPPER_CAMELCASE";
		   
		   oson.setClassMappers(new ClassMapper(AtomicInteger.class)
		   	.setSerializer((Object p) -> FIELD_NAMING.UNDERSCORE_UPPER_CAMELCASE));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   } 
	   
	   @Test
	   public void testDeserializeAtomicIntegerWithFunctionEnum() {
		   String value = "1";
		   AtomicInteger expected = new AtomicInteger(4);
		   
		   oson.setClassMappers(new ClassMapper(AtomicInteger.class)
		   	.setDeserializer((Object p) -> FIELD_NAMING.UNDERSCORE_UPPER_CAMELCASE));
		   
		   AtomicInteger result = oson.deserialize(value, AtomicInteger.class);

		   assertEquals(expected.intValue(), result.intValue());
	   }
	   
	   @Test
	   public void testSerializeAtomicIntegerWithMin() {
		   AtomicInteger value = new AtomicInteger(1);
		   Long min = 10l;
		   String expected = min + "";
		   
		   oson.setClassMappers(new ClassMapper(AtomicInteger.class).setMin(min));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeAtomicIntegerWithMax() {
		   AtomicInteger value = new AtomicInteger(1000000000);
		   Long max = 2000l;
		   String expected = max + "";
		   
		   oson.setClassMappers(new ClassMapper(AtomicInteger.class).setMax(max));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeAtomicIntegerWithMin() {
		   String value = "1";
		   Long min = 10l;
		   AtomicInteger expected = new AtomicInteger(min.intValue());
		   
		   oson.setClassMappers(new ClassMapper(AtomicInteger.class).setMin(min));
		   
		   AtomicInteger result = oson.deserialize(value, AtomicInteger.class);

		   assertEquals(expected.intValue(), result.intValue());
	   }
	   
	   @Test
	   public void testDeserializeAtomicIntegerWithMax() {
		   String value = "19999999";
		   Long max = 10000l;
		   AtomicInteger expected = new AtomicInteger(max.intValue());
		   
		   oson.setClassMappers(new ClassMapper(AtomicInteger.class).setMax(max));
		   
		   AtomicInteger result = oson.deserialize(value, AtomicInteger.class);

		   assertEquals(expected.intValue(), result.intValue());
	   }
	   
}
