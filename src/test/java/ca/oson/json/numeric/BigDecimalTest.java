package ca.oson.json.numeric;

import java.math.BigDecimal;

import org.junit.Test;

import ca.oson.json.Oson.*;
import ca.oson.json.support.TestCaseBase;

public class BigDecimalTest extends TestCaseBase {
	   
	   @Test
	   public void testSerializeBigDecimal() {
		   BigDecimal value = new BigDecimal(5);
		   String expected = "5";
		   
		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testDeserializeBigDecimal() {
		   BigDecimal value = new BigDecimal(5);
		   String text = oson.serialize(value);
		   
		   BigDecimal result = oson.deserialize(text, BigDecimal.class);
		   
		   assertEquals(value, result);
	   }
	   
	   @Test
	   public void testSerializeBigDecimalWithFunction() {
		   BigDecimal value = new BigDecimal(1);
		   String expected = "One";

		   oson.setClassMappers(new ClassMapper(BigDecimal.class).setSerializer((BigDecimal p) -> {
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
			   default: return p.toPlainString();
			   }
		   }));
		   
		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testSerializeBigDecimalWithBigDecimal2JsonFunction() {
		   BigDecimal value = new BigDecimal(6);
		   String expected = "Six";

		   oson.setClassMappers(new ClassMapper(BigDecimal.class).setSerializer((BigDecimal p) -> {
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
	   public void testSerializeBigDecimalWithBigDecimal2JsonFunctionDirect() {
		   BigDecimal value = new BigDecimal(8);
		   String expected = "Eight";

		   oson.setClassMappers(new ClassMapper(BigDecimal.class).setSerializer(
				   (BigDecimal p) -> {
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
			   default: return p.toPlainString();
			   }
		   }));
		   
		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testDeserializeBigDecimalWithFunction() {
		   String value = "Seven";
		   BigDecimal expected = new BigDecimal (7);

		   oson.setClassMappers(new ClassMapper(BigDecimal.class)
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
				   return new BigDecimal(val);
			   }));

		   BigDecimal result = oson.deserialize(value, BigDecimal.class);
		   
		   assertEquals(expected, result);
	   }

	   @Test
	   public void testSerializeBigDecimalWithFunctionEnum() {
		   BigDecimal value = new BigDecimal (1);
		   String expected = "UNDERSCORE_UPPER_CAMELCASE";
		   
		   oson.setClassMappers(new ClassMapper(BigDecimal.class)
		   	.setSerializer((Object p) -> FIELD_NAMING.UNDERSCORE_UPPER_CAMELCASE));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   } 
	   
	   @Test
	   public void testDeserializeBigDecimalWithFunctionEnum() {
		   String value = "1";
		   BigDecimal expected = new BigDecimal(4);
		   
		   oson.setClassMappers(new ClassMapper(BigDecimal.class)
		   	.setDeserializer((Object p) -> FIELD_NAMING.UNDERSCORE_UPPER_CAMELCASE));
		   
		   BigDecimal result = oson.deserialize(value, BigDecimal.class);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeBigDecimalWithMin() {
		   BigDecimal value = new BigDecimal(1);
		   Integer min = 10;
		   String expected = min + "";
		   
		   oson.setClassMappers(new ClassMapper(BigDecimal.class).setMin(min));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeBigDecimalWithMax() {
		   BigDecimal value = new BigDecimal(1000000000);
		   Integer max = 2000;
		   String expected = max + "";
		   
		   oson.setClassMappers(new ClassMapper(BigDecimal.class).setMax(max));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeBigDecimalWithMin() {
		   String value = "1";
		   Integer min = 10;
		   BigDecimal expected = new BigDecimal(min);
		   
		   oson.setClassMappers(new ClassMapper(BigDecimal.class).setMin(min));
		   
		   BigDecimal result = oson.deserialize(value, BigDecimal.class);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeBigDecimalWithMax() {
		   String value = "19999999";
		   Integer max = 10000;
		   BigDecimal expected = new BigDecimal(max);
		   
		   oson.setClassMappers(new ClassMapper(BigDecimal.class).setMax(max));
		   
		   BigDecimal result = oson.deserialize(value, BigDecimal.class);

		   assertEquals(expected, result);
	   }
	   
}
