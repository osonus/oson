package ca.oson.json.numeric;

import org.junit.Test;

import ca.oson.json.support.TestCaseBase;
import static org.junit.Assert.assertNotEquals;

public class ScaleTest extends TestCaseBase {

	@Test
	public void testSerializationScaleFloatGlobal() {
		float value = 12.34567890f;

		String json = oson.serialize(value);

		String expected = "12.34567890";

		assertNotEquals(expected, json);

		
		oson.setScale(2).setPrecision(null);
		
		expected = "12.35";
		
		json = oson.serialize(value);
		
		assertEquals(expected, json);
		
		
		oson.setScale(1);
		
		expected = "12.3";
		
		json = oson.serialize(value);
		
		assertEquals(expected, json);
		
		
		oson.setScale(3);
		
		expected = "12.346";
		
		json = oson.serialize(value);
		
		assertEquals(expected, json);
		
		
		oson.setScale(4);
		
		expected = "12.3457";
		
		json = oson.serialize(value);
		
		assertEquals(expected, json);
		
		
		oson.setScale(5);
		
		expected = "12.34568";
		
		json = oson.serialize(value);
		
		assertEquals(expected, json);
	}
	
	
	@Test
	public void testSerializationScaleDoubleGlobal() {
		Double value = 12.34567890d;

		String json = oson.serialize(value);

		String expected = "12.34567890";

		assertNotEquals(expected, json);

		
		oson.setScale(2).setPrecision(null);
		
		expected = "12.35";
		
		json = oson.serialize(value);
		
		assertEquals(expected, json);
		
		
		oson.setScale(1);
		
		expected = "12.3";
		
		json = oson.serialize(value);
		
		assertEquals(expected, json);
		
		
		oson.setScale(3);
		
		expected = "12.346";
		
		json = oson.serialize(value);
		
		assertEquals(expected, json);
		
		
		oson.setScale(4);
		
		expected = "12.3457";
		
		json = oson.serialize(value);
		
		assertEquals(expected, json);
		
		
		oson.setScale(5);
		
		expected = "12.34568";
		
		json = oson.serialize(value);
		
		assertEquals(expected, json);
	}
}
