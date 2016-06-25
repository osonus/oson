package ca.oson.json.enumbooleandate;

import javax.persistence.EnumType;

import org.junit.Test;

import ca.oson.json.Oson.*;
import ca.oson.json.support.TestCaseBase;

public class EnumTest extends TestCaseBase {

	   @Test
	   public void testSerializeEnum() {
		   Enum value = MODIFIER.Public;
		   String expected = "Public";
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeEnum() {
		   String value = "DASH_UPPER_CAMELCASE";
		   Enum expected = FIELD_NAMING.DASH_UPPER_CAMELCASE;
		   
		   Enum result = oson.deserialize(value, expected.getClass());

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeEnumWithEnumType() {
		   Enum value = MODIFIER.Synchronized;
		   String expected = "10";
		   
		   oson.setEnumType(EnumType.ORDINAL);
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeEnumWithFunction() {
		   Enum value = MODIFIER.Public;
		   String expected = "Final";
		   
		   Enum2JsonFunction function = p -> MODIFIER.Final.name();
		   
		   oson.setClassMappers(new ClassMapper(value.getClass()).setSerializer(function));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }

}
