package ca.oson.json.object;

import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import ca.oson.json.support.TestCaseBase;

public class MapTest extends TestCaseBase {

	public void testPropertiesSerialize() {
		Properties map = new Properties();
		
		map.put("number", 123);
		map.put("string", "This is a test");

		String json = oson.serialize(map);
		
		//System.err.println(json);

		assertEquals("{\"string\":\"This is a test\",\"number\":123}", json);
	}
	
	public void testPropertiesDeserialize() {
		String json = "{\"string\":\"This is a test\",\"number\":123}";
		
		Map map = oson.deserialize(json, Properties.class);

		assertEquals(Properties.class, map.getClass());

		assertEquals(map.get("number"), 123);
		assertEquals(map.get("string"), "This is a test");
	}
	
	public void testHashtableSerialize() {
		Hashtable map = new Hashtable();
		
		map.put("number", 123);
		map.put("string", "This is a test");

		String json = oson.serialize(map);
		
		//System.err.println(json);

		assertEquals("{\"string\":\"This is a test\",\"number\":123}", json);
	}
	
	public void testHashtableDeserialize() {
		String json = "{\"string\":\"This is a test\",\"number\":123}";
		
		Map map = oson.deserialize(json, Hashtable.class);

		assertEquals(Hashtable.class, map.getClass());

		assertEquals(map.get("number"), 123);
		assertEquals(map.get("string"), "This is a test");
	}
	
}
