package ca.oson.json.userguide;

import org.junit.Test;

import ca.oson.json.support.TestCaseBase;

public class PrimitivesTest extends TestCaseBase {

	   @Test
	   public void testSerializationPrimitives() {
		   assertEquals("1", oson.serialize(1));
		   
		   assertEquals("abcd", oson.serialize("abcd"));
		   
		   assertEquals("10", oson.serialize(new Long(10)));

		   assertEquals("10", oson.serialize(new Long(10)));
		   
		   int[] values = { 1 };
		   
		   assertEquals("[1]", oson.serialize(values));
	   }
	
	
	   @Test
	   public void testDeserializationPrimitives() {
		   int one = oson.deserialize("1", int.class);
		   assertEquals(1, one);
		   
		   Integer two = oson.deserialize("2", Integer.class);
		   assertEquals(new Integer(2), two);

		   Long three = oson.deserialize("3", Long.class);
		   assertEquals(new Long(3), three);

		   Boolean four = oson.deserialize("false", Boolean.class);
		   assertFalse(four);

		   String str = oson.deserialize("\"abc\"", String.class);
		   assertEquals("abc", str);

		   String[] anotherStr = oson.deserialize("[\"abc\"]", String[].class);
		   
		   assertEquals("abc", anotherStr[0]);
	   }
	   
}
