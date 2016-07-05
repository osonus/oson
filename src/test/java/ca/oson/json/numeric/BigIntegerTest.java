package ca.oson.json.numeric;

import java.math.BigInteger;
import java.util.function.Function;

import org.junit.Test;

import ca.oson.json.Oson.*;
import ca.oson.json.support.TestCaseBase;
import ca.oson.json.domain.Car;

public class BigIntegerTest extends TestCaseBase {
	   
	   @Test
	   public void testSerializeBigInteger() {
		   BigInteger value = BigInteger.valueOf(5);
		   String expected = "5";
		   
		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testDeserializeBigInteger() {
		   BigInteger value = BigInteger.valueOf(5);
		   String text = oson.serialize(value);
		   
		   BigInteger result = oson.deserialize(text, BigInteger.class);
		   
		   assertEquals(value, result);
	   }
	   
	   @Test
	   public void testSerializeBigIntegerWithFunction() {
		   BigInteger value = BigInteger.valueOf(1);
		   String expected = "One";

		   oson.setClassMappers(new ClassMapper(BigInteger.class).setSerializer((BigInteger p) -> {
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
	   public void testSerializeBigIntegerWithBigInteger2JsonFunction() {
		   BigInteger value = BigInteger.valueOf(6);
		   String expected = "Six";

		   oson.setClassMappers(new ClassMapper(BigInteger.class).setSerializer((BigInteger p) -> {
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
	   public void testSerializeBigIntegerWithBigInteger2JsonFunctionDirect() {
		   BigInteger value = BigInteger.valueOf(8);
		   String expected = "Eight";

		   oson.setClassMappers(new ClassMapper(BigInteger.class).setSerializer(
				   (BigInteger p) -> {
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
	   public void testSerializeBigIntegerWithDataMapper2JsonFunction() {
		   BigInteger value = BigInteger.valueOf(8);
		   String expected = "Eight";
		   
		   DataMapper2JsonFunction function = (DataMapper p) -> {
			   BigInteger bint = (BigInteger) p.getObj();
			   
			   switch (bint.intValue()) {
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
		   };

		   oson.setSerializer(BigInteger.class, function);
		   
		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeBigIntegerWithGenericFunction() {
		   BigInteger value = BigInteger.valueOf(8);
		   String expected = "{\"@class\":\"ca.oson.json.domain.Car\",\"doors\":4,\"year\":2016,\"brand\":\"Eight\",\"years\":null}";
		   
		   Function function = (Object p) -> {
			   BigInteger bint = (BigInteger) p;
			   
			   switch (bint.intValue()) {
			   case 1: return new Car("One");
			   case 2: return new Car("Two");
			   case 3: return new Car("Three");
			   case 4: return new Car("Four");
			   case 5: return new Car("Five");
			   case 6: return new Car("Six");
			   case 7: return new Car("Seven");
			   case 8: return new Car("Eight");
			   case 9: return new Car("Nine");
			   case 10: return new Car("Ten");
			   default: return new Car(p.toString());
			   }
		   };

		   oson.setSerializer(BigInteger.class, function);
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testDeserializeBigIntegerWithFunction() {
		   String value = "Seven";
		   BigInteger expected = BigInteger.valueOf (7);

		   oson.setClassMappers(new ClassMapper(BigInteger.class)
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
				   return BigInteger.valueOf(val);
			   }));

		   BigInteger result = oson.deserialize(value, BigInteger.class);
		   
		   assertEquals(expected, result);
	   }

	   @Test
	   public void testSerializeBigIntegerWithFunctionEnum() {
		   BigInteger value = BigInteger.valueOf (1);
		   String expected = "UNDERSCORE_UPPER_CAMELCASE";
		   
		   oson.setClassMappers(new ClassMapper(BigInteger.class)
		   	.setSerializer((Object p) -> FIELD_NAMING.UNDERSCORE_UPPER_CAMELCASE));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   } 
	   
	   @Test
	   public void testDeserializeBigIntegerWithFunctionEnum() {
		   String value = "1";
		   BigInteger expected = BigInteger.valueOf(4);
		   
		   oson.setClassMappers(new ClassMapper(BigInteger.class)
		   	.setDeserializer((Object p) -> FIELD_NAMING.UNDERSCORE_UPPER_CAMELCASE));
		   
		   BigInteger result = oson.deserialize(value, BigInteger.class);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeBigIntegerWithMin() {
		   BigInteger value = BigInteger.valueOf(1);
		   Long min = 10l;
		   String expected = min + "";
		   
		   oson.setClassMappers(new ClassMapper(BigInteger.class).setMin(min));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeBigIntegerWithMax() {
		   BigInteger value = BigInteger.valueOf(1000000000);
		   Long max = 2000l;
		   String expected = max + "";
		   
		   oson.setClassMappers(new ClassMapper(BigInteger.class).setMax(max));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeBigIntegerWithMin() {
		   String value = "1";
		   Long min = 10l;
		   BigInteger expected = BigInteger.valueOf(min);
		   
		   oson.setClassMappers(new ClassMapper(BigInteger.class).setMin(min));
		   
		   BigInteger result = oson.deserialize(value, BigInteger.class);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeBigIntegerWithMax() {
		   String value = "19999999";
		   Long max = 10000l;
		   BigInteger expected = BigInteger.valueOf(max);
		   
		   oson.setClassMappers(new ClassMapper(BigInteger.class).setMax(max));
		   
		   BigInteger result = oson.deserialize(value, BigInteger.class);

		   assertEquals(expected, result);
	   }
	   
}
