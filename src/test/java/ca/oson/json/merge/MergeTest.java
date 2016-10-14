package ca.oson.json.merge;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ca.oson.json.OsonConvert;
import ca.oson.json.OsonIO;
import ca.oson.json.OsonMerge;
import ca.oson.json.OsonMerge.Config;
import ca.oson.json.OsonMerge.LIST_VALUE;
import ca.oson.json.OsonSearch;
import ca.oson.json.OsonMerge.NUMERIC_VALUE;
import ca.oson.json.OsonMerge.NONNUMERICAL_VALUE;
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
		
		String expected = "{\"id\":1,\"email\":\"osonus@gmail.com\",\"web_site\":\"google.com\",\"username\":\"osonus\"}";

		String json = merge.merge(str1, str2);
		assertEquals(expected, json);
	}
	
	
	@Test
	public void testListMerge() {
		String list = "[{\"foo\":\"foo1\"},{\"bar\":\"bar2\"},{\"foo\":\"foo1\"},{\"bar\":\"bar1\"},{\"foo\":\"foo2\"},{\"bar\":\"bar3\"}]";;
		
		String json = merge.merge(list);
		String expected = "{\"bar\":\"bar1\",\"foo\":\"foo1\"}";
		assertEquals(expected, json);
		
		
		merge.getConfig().nonnumericalValue = NONNUMERICAL_VALUE.KEEP_OLD;
		json = merge.merge(list);
		expected = "{\"bar\":\"bar2\",\"foo\":\"foo1\"}";
		assertEquals(expected, json);
		
		
		merge.getConfig().nonnumericalValue = NONNUMERICAL_VALUE.FREQUENT;
		json = merge.merge(list);
		expected = "{\"bar\":\"bar1\",\"foo\":\"foo1\"}";
		assertEquals(expected, json);
	}
	
	@Test
	public void testNumericMerge() {
		String json = "[15, 10, 1, 10, 10000, 13]";
		OsonMerge merge = new OsonMerge();
		
		merge.getConfig().numericValue = NUMERIC_VALUE.AVERAGE;
		String result = merge.merge(json);
		assertEquals("1674", result);

		merge.getConfig().errorThreshold = 20;
		result = merge.merge(json);
		assertEquals("9", result);
		
		merge.getConfig().numericValue = NUMERIC_VALUE.FREQUENT;
		result = merge.merge(json);
		assertEquals("10", result);
		
		merge.getConfig().numericValue = NUMERIC_VALUE.KEEP_MAX;
		result = merge.merge(json);
		assertEquals("10000", result);
		
		merge.getConfig().numericValue = NUMERIC_VALUE.KEEP_MIN;
		result = merge.merge(json);
		assertEquals("1", result);
		
		merge.getConfig().numericValue = NUMERIC_VALUE.KEEP_NEW;
		result = merge.merge(json);
		assertEquals("13", result);
		
		merge.getConfig().numericValue = NUMERIC_VALUE.KEEP_OLD;
		result = merge.merge(json);
		assertEquals("15", result);
	}
	
	
	@Test
	public void testTableMerge() {
		Object obj = oson.readValue("rows2Columns.txt");

		Config config = merge.getConfig();
		config.numericValue = NUMERIC_VALUE.MERGE_UNIQUE;
		config.nonnumericalValue = NONNUMERICAL_VALUE.MERGE_UNIQUE;
		config.listValue = LIST_VALUE.MERGE_UNIQUE;

		Object merged = merge.merge(obj);
		
		String expected = "{\"PM\":[\"Jane\",\"Jill\"],\"e\":\"j@nunya.com\",\"h\":[\"15.00\",\"11.00\",\"21.00\",\"12.00\"],\"w\":[\"10/30/2016 12:00:00 AM\",\"11/06/2016 12:00:00 AM\"],\"c\":\"John\",\"p\":[\"Happy Town USA\",\"Sad Town USA\"]}";

		assertEquals(expected, oson.serialize(merged));
	}

	
	@Test
	public void testTableMerge2() {
		List<Map> list = oson.readValue("rows2Columns.txt");
		
		Map<String, List> collected = new HashMap<>();

		for (Map map: list) {
			String key = (String) map.get("PM");
			List obj = collected.get(key);
			
			if (obj == null) {
				obj = new ArrayList();
				
				collected.put(key, obj);
			}
			
			obj.add(map);
		}

		Config config = merge.getConfig();
		config.numericValue = NUMERIC_VALUE.MERGE_UNIQUE;
		config.nonnumericalValue = NONNUMERICAL_VALUE.MERGE_UNIQUE;
		config.listValue = LIST_VALUE.MERGE_UNIQUE;

		Map<String, Object> results = new HashMap<>();
		
		for (String key: collected.keySet()) {
			results.put(key, merge.merge(collected.get(key)));
		}
		
		Collection result = results.values();
		
		// oson.pretty();
		String json = oson.serialize(result);
		
		String expected = "[{\"PM\":\"Jill\",\"e\":\"j@nunya.com\",\"h\":[\"21.00\",\"12.00\"],\"w\":[\"10/30/2016 12:00:00 AM\",\"11/06/2016 12:00:00 AM\"],\"c\":\"John\",\"p\":\"Sad Town USA\"},{\"PM\":\"Jane\",\"e\":\"j@nunya.com\",\"h\":[\"15.00\",\"11.00\"],\"w\":[\"10/30/2016 12:00:00 AM\",\"11/06/2016 12:00:00 AM\"],\"c\":\"John\",\"p\":\"Happy Town USA\"}]";

		assertEquals(expected, json);
	}
	
	
	@Test
	public void testTableMerge3() {
		List<Map> list = oson.readValue("rows2Columns.txt");
		
		Config config = merge.getConfig();
		config.groupBy = "PM";
		List result = (List)merge.merge(list);
		
		// oson.pretty();
		String json = oson.serialize(result);
		
		String expected = "[{\"PM\":\"Jane\",\"e\":\"j@nunya.com\",\"h\":[\"15.00\",\"11.00\"],\"w\":[\"10/30/2016 12:00:00 AM\",\"11/06/2016 12:00:00 AM\"],\"c\":\"John\",\"p\":\"Happy Town USA\"},{\"PM\":\"Jill\",\"e\":\"j@nunya.com\",\"h\":[\"21.00\",\"12.00\"],\"w\":[\"10/30/2016 12:00:00 AM\",\"11/06/2016 12:00:00 AM\"],\"c\":\"John\",\"p\":\"Sad Town USA\"}]";
		
		assertEquals(expected, json);
	}
}
