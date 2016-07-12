package ca.oson.json.object;

import ca.oson.json.support.TestCaseBase;

public class StringBuilderBufferTest extends TestCaseBase {

	public void testStringBuilderSerialize() {
		StringBuilder sb = new StringBuilder();
		sb.append("This is a test.");
		
		String json = oson.serialize(sb);

		assertEquals("\"This is a test.\"", json);
	}

	public void testStringBuilderDeserialize() {
		String json = "\"This is a test.\"";
		
		StringBuilder sb = oson.deserialize(json, StringBuilder.class);
		
		// System.err.println(sb);

		assertEquals("This is a test.", sb.toString());
	}
	
	public void testStringBufferSerialize() {
		StringBuffer sb = new StringBuffer();
		sb.append("This is a test.");
		
		String json = oson.serialize(sb);

		assertEquals("\"This is a test.\"", json);
	}

	public void testStringBufferDeserialize() {
		String json = "\"This is a test.\"";
		
		StringBuffer sb = oson.deserialize(json, StringBuffer.class);
		
		// System.err.println(sb);

		assertEquals("This is a test.", sb.toString());
	}
	
}
