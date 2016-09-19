package ca.oson.json.merge;

import org.junit.Test;

import ca.oson.json.OsonMerge;
import ca.oson.json.support.TestCaseBase;

public class MergeTest extends TestCaseBase {

	@Test
	public void testSimpleMerge() {
		String str1 = "{\"id\":1, \"username\":\"osonus\", \"webSite\":\"google.com\"}";
		String str2 = "{\"id\":2, \"email\":\"osonus@gmail.com\", \"web_site\":\"oson.ca\"}";
		
		String expected = "{\"id\":2,\"email\":\"osonus@gmail.com\",\"web_site\":\"oson.ca\",\"username\":\"osonus\"}";
		
		OsonMerge merge = new OsonMerge();
		String json = merge.merge(str1, str2);
		// System.err.println(json);
		assertEquals(expected, json);
	}
	
}
