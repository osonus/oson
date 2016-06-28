package ca.oson.json.numeric;

import java.util.concurrent.atomic.AtomicLong;

import org.junit.Test;

import ca.oson.json.Oson.*;
import ca.oson.json.support.TestCaseBase;

public class AtomicLongTest extends TestCaseBase {
	   
	   @Test
	   public void testSerializeAtomicLong() {
		   AtomicLong value = new AtomicLong(5);
		   String expected = "5";
		   
		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testDeserializeAtomicLong() {
		   AtomicLong value = new AtomicLong(5);
		   String text = oson.serialize(value);
		   
		   AtomicLong result = oson.deserialize(text, AtomicLong.class);
		   
		   assertEquals(value.longValue(), result.longValue());
	   }
	   
	   @Test
	   public void testSerializeAtomicLongWithFunction() {
		   AtomicLong value = new AtomicLong(1);
		   String expected = "One";

		   oson.setClassMappers(new ClassMapper(AtomicLong.class).setSerializer((AtomicLong p) -> {
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
	   public void testSerializeAtomicLongWithAtomicLong2JsonFunction() {
		   AtomicLong value = new AtomicLong(6);
		   String expected = "Six";

		   oson.setClassMappers(new ClassMapper(AtomicLong.class).setSerializer((AtomicLong p) -> {
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
	   public void testSerializeAtomicLongWithAtomicLong2JsonFunctionDirect() {
		   AtomicLong value = new AtomicLong(8);
		   String expected = "Eight";

		   oson.setClassMappers(new ClassMapper(AtomicLong.class).setSerializer(
				   (AtomicLong p) -> {
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
	   public void testDeserializeAtomicLongWithFunction() {
		   String value = "Seven";
		   AtomicLong expected = new AtomicLong (7);

		   oson.setClassMappers(new ClassMapper(AtomicLong.class)
		   	.setDeserializer((String p) -> {
		   			long val;
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
				   default: val = Long.parseLong(p); break;
				   }
				   return new AtomicLong(val);
			   }));

		   AtomicLong result = oson.deserialize(value, AtomicLong.class);
		   
		   assertEquals(expected.longValue(), result.longValue());
	   }

	   @Test
	   public void testSerializeAtomicLongWithFunctionEnum() {
		   AtomicLong value = new AtomicLong (1);
		   String expected = "UNDERSCORE_UPPER_CAMELCASE";
		   
		   oson.setClassMappers(new ClassMapper(AtomicLong.class)
		   	.setSerializer((Object p) -> FIELD_NAMING.UNDERSCORE_UPPER_CAMELCASE));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   } 
	   
	   @Test
	   public void testDeserializeAtomicLongWithFunctionEnum() {
		   String value = "1";
		   AtomicLong expected = new AtomicLong(4);
		   
		   oson.setClassMappers(new ClassMapper(AtomicLong.class)
		   	.setDeserializer((Object p) -> FIELD_NAMING.UNDERSCORE_UPPER_CAMELCASE));
		   
		   AtomicLong result = oson.deserialize(value, AtomicLong.class);

		   assertEquals(expected.longValue(), result.longValue());
	   }
	   
	   @Test
	   public void testSerializeAtomicLongWithMin() {
		   AtomicLong value = new AtomicLong(1);
		   Long min = 10l;
		   String expected = min + "";
		   
		   oson.setClassMappers(new ClassMapper(AtomicLong.class).setMin(min));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeAtomicLongWithMax() {
		   AtomicLong value = new AtomicLong(1000000000);
		   Long max = 2000l;
		   String expected = max + "";
		   
		   oson.setClassMappers(new ClassMapper(AtomicLong.class).setMax(max));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeAtomicLongWithMin() {
		   String value = "1";
		   Long min = 10l;
		   AtomicLong expected = new AtomicLong(min);
		   
		   oson.setClassMappers(new ClassMapper(AtomicLong.class).setMin(min));
		   
		   AtomicLong result = oson.deserialize(value, AtomicLong.class);

		   assertEquals(expected.longValue(), result.longValue());
	   }
	   
	   @Test
	   public void testDeserializeAtomicLongWithMax() {
		   String value = "19999999";
		   Long max = 10000l;
		   AtomicLong expected = new AtomicLong(max);
		   
		   oson.setClassMappers(new ClassMapper(AtomicLong.class).setMax(max));
		   
		   AtomicLong result = oson.deserialize(value, AtomicLong.class);

		   assertEquals(expected.longValue(), result.longValue());
	   }
	   
}
