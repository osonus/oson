package ca.oson.json;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import ca.oson.json.Oson.FIELD_NAMING;
import ca.oson.json.util.ObjectUtil;

public class OsonMerge {
	public static class Config
	{
		/*
		 * additional options take precedence over previous ones
		 */
		public boolean overwritePrevious = true;
		
		/*
		 * overwrite or concate arrays/lists with the same name
		 */
		public boolean concatArrays = true;
		
		/*
		 * attribute and key names following any of the Oson.FIELD_NAMING rules are considered to be equivalent
		 */
		public boolean naming = true;
		
		/*
		 * Oson.FIELD_NAMING rules for attribute names in the result
		 * unless specified by names map
		 */
		public FIELD_NAMING namingPolicy = FIELD_NAMING.FIELD;
		
		/*
		 * Old names are replaced by new names in the result:
		 * {"oldName": "newName"}
		 */
		public Map names = new HashMap();
		
	}

	private Config config;
	
	public OsonMerge() {
		this(new Config());
	}

	public OsonMerge(Config config) {
		ObjectUtil.getJSONObject(null);
		this.config = config;
	}
	
	/*
	 * Merge Json string data, return a Json result
	 */
	public String merge(String json, String... jsons) {
		
		
		return json;
	}
	
	/*
	 * Merge JSONObject objects, by creating a new JSONObject object
	 * to hold the resulting data
	 */
	public JSONObject merge(JSONObject obj, JSONObject... objs) {
		
		
		return obj;
	}
	
	
	/*
	 * Merge Java objects, by dynamically creating a new Java class 
	 * to hold the resulting data, using javassist library
	 */
	public Object merge(Object obj, Object... objs) {
		
		
		return obj;
	}
	
	/*
	 * Create a Java object, based on a JSONObject object
	 */
	public static Object json2Object (JSONObject obj) {
		
		return obj;
	}
	
	/*
	 * Create a custom Java object, based on a Json string
	 */
	public static Object json2Object (String json) {
		
		return null;
	}
}
