package ca.oson.json.enumbooleandate;

import org.junit.Test;

import ca.oson.json.ClassMapper;
import ca.oson.json.support.TestCaseBase;

public class BooleanTest extends TestCaseBase {

	   @Test
	   public void testSerializeBoolean() {
		   Boolean value = true;
		   String expected = "true";
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	
	   @Test
	   public void testDeserializeBoolean() {
		   String value = "false";

		   Boolean result = oson.deserialize(value, Boolean.class);

		   assertFalse(result);
	   }
	   
	   @Test
	   public void testDeserializeBooleanTRUE() {
		   String value = "TRUE";

		   Boolean result = oson.deserialize(value, Boolean.class);

		   assertTrue(result);
	   }
	   
	   @Test
	   public void testDeserializeBooleanT() {
		   String value = "T";

		   Boolean result = oson.deserialize(value, Boolean.class);

		   assertTrue(result);
	   }
	   
	   @Test
	   public void testDeserializeBoolean1() {
		   String value = "1";

		   Boolean result = oson.deserialize(value, Boolean.class);

		   assertTrue(result);
	   }
	   
	   @Test
	   public void testDeserializeBoolean0() {
		   String value = "0";

		   Boolean result = oson.deserialize(value, Boolean.class);

		   assertFalse(result);
	   }
	   
	   
	   @Test
	   public void testDeserializeBooleannull() {
		   String value = null;

		   Boolean result = oson.deserialize(value, Boolean.class);

		   assertNull(result);
	   }
	   
	   @Test
	   public void testDeserializeBooleanWithnull() {
		   String value = "null";

		   Boolean result = oson.deserialize(value, Boolean.class);

		   assertTrue(result == null);
	   }
	   
	   @Test
	   public void testDeserializeBooleanWithOther() {
		   String value = "other";

		   Boolean result = oson.deserialize(value, Boolean.class);

		   assertNull(result);
	   }
	   
	   @Test
	   public void testSerializeBooleanWithFunction() {
		   Boolean value = false;
		   String expected = "true";

		   oson.setClassMappers(new ClassMapper(Boolean.class).setSerializer((Boolean p) -> (!p) + ""));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeBooleanWithFunction() {
		   String value = "false";
		   Boolean expected = true;

		   oson.setClassMappers(new ClassMapper(Boolean.class).setDeserializer((String p) -> !(Boolean.parseBoolean(p))));
		   
		   Boolean result = oson.deserialize(value, Boolean.class);

		   assertEquals(expected, result);
	   }
	   
}
	   
	   
