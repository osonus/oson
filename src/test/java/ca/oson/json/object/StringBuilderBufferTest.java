package ca.oson.json.object;

import ca.oson.json.support.TestCaseBase;

public class StringBuilderBufferTest extends TestCaseBase {

	public void testStringBuilderSerialize() {
		StringBuilder sb = new StringBuilder();
		sb.append("This is a test.");
		
		String json = oson.serialize(sb);
		
		System.err.println(json);

		assertEquals("\"This is a test.\"", json);
	}

	public void testStringBuilderDeserialize() {
		String json = "\"This is a test.\"";
		
		StringBuilder sb = oson.deserialize(json, StringBuilder.class);
		
		System.err.println(sb);

		assertEquals("\"This is a test.\"", json);
	}
	
}
