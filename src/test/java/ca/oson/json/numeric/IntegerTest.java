package ca.oson.json.numeric;

import org.junit.Test;

import ca.oson.json.ClassMapper;
import ca.oson.json.Oson.*;
import ca.oson.json.support.TestCaseBase;

public class IntegerTest extends TestCaseBase {
	   
	   @Test
	   public void testSerializeInt() {
		   int value = 5;
		   String expected = "5";
		   
		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testDeserializeInt() {
		   int value = 5;
		   String text = oson.serialize(value);
		   
		   int result = oson.deserialize(text, Integer.class);
		   
		   assertEquals(value, result);
	   }
	   
	   @Test
	   public void testSerializeIntWithFunction() {
		   int value = 6;
		   String expected = "Six";

		   oson.setClassMappers(new ClassMapper(Integer.class).setSerializer((Integer p) -> {
			   switch (p) {
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
			   default: return p + "";
			   }
		   }));
		   
		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testSerializeIntWithInteger2JsonFunction() {
		   int value = 6;
		   String expected = "Six";

		   oson.setClassMappers(new ClassMapper(Integer.class).setSerializer((Integer p) -> {
			   switch (p) {
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
			   default: return p + "";
			   }
		   }));
		   
		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testSerializeIntWithInteger2JsonFunctionDirect() {
		   int value = 6;
		   String expected = "Six";

		   oson.setClassMappers(new ClassMapper(Integer.class).setSerializer(
				   (Integer p) -> {
			   switch (p) {
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
			   default: return p + "";
			   }
		   }));
		   
		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testDeserializeIntWithFunction() {
		   String value = "Seven";
		   Integer expected = 7;

		   oson.setClassMappers(new ClassMapper(Integer.class)
		   	.setDeserializer((String p) -> {
				   switch (p) {
				   case "One": return 1;
				   case "Two": return 2;
				   case "Three": return 3;
				   case "Four": return 4;
				   case "Five": return 5;
				   case "Six": return 6;
				   case "Seven": return 7;
				   case "Eight": return 8;
				   case "Nine": return 9;
				   case "Ten": return 10;
				   default: return Integer.parseInt(p);
				   }
			   }));

		   Integer result = oson.deserialize(value, Integer.class);
		   
		   assertEquals(expected, result);
	   }

	   @Test
	   public void testSerializeIntWithFunctionEnum() {
		   int value = 1;
		   String expected = "UNDERSCORE_UPPER_CAMELCASE";
		   
		   oson.setClassMappers(new ClassMapper(Integer.class)
		   	.setSerializer((Object p) -> FIELD_NAMING.UNDERSCORE_UPPER_CAMELCASE));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   } 
	   
	   @Test
	   public void testDeserializeIntWithFunctionEnum() {
		   String value = "1";
		   Integer expected = 6;
		   
		   oson.setClassMappers(new ClassMapper(Integer.class)
		   	.setDeserializer((Object p) -> FIELD_NAMING.UNDERSCORE_UPPER_CAMELCASE));
		   
		   Integer result = oson.deserialize(value, Integer.class);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeIntWithMin() {
		   Integer value = 1;
		   Long min = 10l;
		   String expected = min + "";
		   
		   oson.setClassMappers(new ClassMapper(Integer.class).setMin(min));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeIntWithMax() {
		   Integer value = 1000000000;
		   Long max = 2000l;
		   String expected = max + "";
		   
		   oson.setClassMappers(new ClassMapper(Integer.class).setMax(max));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeIntWithMin() {
		   String value = "1";
		   Long min = 10l;
		   Integer expected = min.intValue();
		   
		   oson.setClassMappers(new ClassMapper(Integer.class).setMin(min));
		   
		   Integer result = oson.deserialize(value, Integer.class);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeIntWithMax() {
		   String value = "19999999";
		   Long max = 10000l;
		   Integer expected = max.intValue();
		   
		   oson.setClassMappers(new ClassMapper(Integer.class).setMax(max));
		   
		   Integer result = oson.deserialize(value, Integer.class);

		   assertEquals(expected, result);
	   }
	   
}
