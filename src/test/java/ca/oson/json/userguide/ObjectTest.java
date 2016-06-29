package ca.oson.json.userguide;

import org.junit.Test;

import com.google.gson.GsonBuilder;

import ca.oson.json.support.TestCaseBase;

public class ObjectTest extends TestCaseBase {

	@Test
	public void testSerializationObject() {
		BagOfPrimitives obj = new BagOfPrimitives();
		String json = oson.serialize(obj);

		String expected = "{\"value2\":\"abc\",\"value1\":1}";

		assertEquals(expected, json);
	}

	@Test
	public void testSerializationObjectSort() {
		BagOfPrimitives obj = new BagOfPrimitives();
		String json = oson.sort().serialize(obj);

		String expected = "{\"value1\":1,\"value2\":\"abc\"}";

		assertEquals(expected, json);
	}

	@Test
	public void testDeserializationObject() {
		BagOfPrimitives obj = new BagOfPrimitives();
		String json = oson.serialize(obj);

		BagOfPrimitives obj2 = oson.deserialize(json, BagOfPrimitives.class);

		String json2 = oson.serialize(obj2);

		assertEquals(json, json2);

		// System.out.println(json);
	}

	@Test
	public void testSerializationGsonBuilder() {

		GsonBuilder gbuilder = new GsonBuilder();

		String gson = oson.setLevel(1).pretty(true).includeClassTypeInJson(true).serialize(gbuilder);

		GsonBuilder gbuilder2 = oson.deserialize(gson);
		String gson2 = oson.serialize(gbuilder2);

		GsonBuilder gbuilder3 = oson.deserialize(gson2);
		String gson3 = oson.serialize(gbuilder3);
		assertEquals(gson2, gson3);
		
		//System.out.println(gson3);
	}

}


class BagOfPrimitives {
	private int value1 = 1;
	private String value2 = "abc";
	private transient int value3 = 3;

	BagOfPrimitives() {
		// no-args constructor
	}
}