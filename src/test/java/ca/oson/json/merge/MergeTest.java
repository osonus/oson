package ca.oson.json.merge;

import org.junit.Before;
import org.junit.Test;

import ca.oson.json.OsonIO;
import ca.oson.json.OsonMerge;
import ca.oson.json.OsonMerge.OTHER_VALUE;
import ca.oson.json.support.TestCaseBase;
import ca.oson.json.util.ObjectUtil;

public class MergeTest extends TestCaseBase {
	OsonMerge merge;
	
	   @Before 
	   protected void setUp() {
		   super.setUp();
		   merge = new OsonMerge();
	   }

	@Test
	public void testSimpleMerge() {
		String str1 = "{\"id\":1, \"username\":\"osonus\", \"webSite\":\"google.com\"}";
		String str2 = "{\"id\":2, \"email\":\"osonus@gmail.com\", \"web_site\":\"oson.ca\"}";
		
		String expected = "{\"id\":2,\"email\":\"osonus@gmail.com\",\"web_site\":\"oson.ca\",\"username\":\"osonus\"}";

		String json = merge.merge(str1, str2);
		assertEquals(expected, json);
	}
	
	
	@Test
	public void testListMerge() {
		String list = "[{\"foo\":\"foo1\"},{\"bar\":\"bar2\"},{\"foo\":\"foo1\"},{\"bar\":\"bar1\"},{\"foo\":\"foo2\"},{\"bar\":\"bar3\"}]";;
		
		String json = merge.merge(list);
		String expected = "{\"bar\":\"bar3\",\"foo\":\"foo2\"}";
		assertEquals(expected, json);
		
		
		merge.getConfig().otherValue = OTHER_VALUE.KEEP_OLD;
		json = merge.merge(list);
		expected = "{\"bar\":\"bar2\",\"foo\":\"foo1\"}";
		assertEquals(expected, json);
		
		
		merge.getConfig().otherValue = OTHER_VALUE.FREQUENT;
		json = merge.merge(list);
		expected = "{\"bar\":\"bar1\",\"foo\":\"foo1\"}";
		assertEquals(expected, json);
	}
}
