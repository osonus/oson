package ca.oson.json.charstring;

import org.junit.Test;

import ca.oson.json.support.TestCaseBase;
import ca.oson.json.util.StringUtil;

public class AsciiTest extends TestCaseBase {
	
	@Test
	public void testSerializeAndDeserializeAscii() {
		byte[] bytes = new byte[128];
		char[] chars = new char[128];

		for (int b = 0; b < 128; b++) {
			bytes[b] = (byte)b;
			chars[b] = (char) b;
		}

		String json = oson.serialize(chars);

		//System.err.println(json);

		char[] results = oson.deserialize(json, char[].class);
		
		for (int b = 0; b < 128; b++) {
			assertEquals(chars[b], results[b]);
			
			assertEquals(bytes[b], results[b]);
		}
	}
	

	@Test
	public void testSerializeAndDeserializeExtendedAscii() {
		int max = 256;
		processCharset(max);
	}
	
	@Test
	public void testSerializeAndDeserializeUtf8() {
		int max = 1112064; // U+10FFFF;
		processCharset(max);
	}
	
	
	@Test
	public void testSerializeAndDeserializeMaxCodePoint() {
		int max = Character.MAX_CODE_POINT;
		processCharset(max);
	}
	
	private void processCharset(int max) {
		char[] chars = new char[max];

		for (int b = 0; b < max; b++) {
			chars[b] = (char) b;
		}

		String json = oson.serialize(chars);

		char[] results = oson.deserialize(json, char[].class);
		
		for (int b = 0; b < max; b++) {
			assertEquals(chars[b], results[b]);
		}
	}
	
	
}
