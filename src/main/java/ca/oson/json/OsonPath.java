package ca.oson.json;

import java.lang.reflect.Type;
import java.util.Map;

import ca.oson.json.Oson.FIELD_NAMING;
import ca.oson.json.path.*;
import ca.oson.json.util.ConvertUtil;

public class OsonPath {
	private static FIELD_NAMING naming = ConvertUtil.naming;
	private static Oson oson = new Oson()
				.includeClassTypeInJson(false)
				.setFieldNaming(naming);
	
	/*
	 * Core functionality implemented here. Select nodes based on xpath query string
	 * 
	 * @param object    the list-map Java object to query
	 * @param xpath     the xpath query string, can be of xpath convention from xml, or jsonpath convention
	 *                  this implementation combine notations from multiple sources, including current jsonpath
	 *                  convention, sql query convention, jpa convention, xml xpath convention
	 * @return          the list of values found.
	 */
	private static Object select(Object object, String xpath) {
		Path path = new Path(xpath);
		
		
		
		return object;
	}

	public static String evaluate(String json, String xpath) {
		Object object = oson.deserialize(json);
		
		object = select(object, xpath);

		return oson.serialize(object);
	}
	
	
	public static String evaluate(String json, String xpath, Map<String, Object> filters) {
		Object object = oson.deserialize(json);
		
		object = select(object, xpath);
		
		object = OsonConvert.filtering(object, filters);
		
		return oson.serialize(object);
	}
	

	public static <T> T evaluate(String json, String xpath, Class<T> valueType) {
		json = evaluate(json, xpath);

		return oson.deserialize(json, valueType);
	}
	
	
	public static <T> T evaluate(String json, String xpath, Map<String, Object> filters, Class<T> valueType) {
		json = evaluate(json, xpath, filters);

		return oson.deserialize(json, valueType);
	}
	
	public <T> T evaluate(String json, String xpath, T obj) {
		json = evaluate(json, xpath);

		return oson.deserialize(json, obj);
	}
	
	public <T> T evaluate(String json, String xpath, Map<String, Object> filters, T obj) {
		json = evaluate(json, xpath, filters);

		return oson.deserialize(json, obj);
	}
	
	public <T> T evaluate(String json, String xpath, Type type) {
		json = evaluate(json, xpath);

		return oson.deserialize(json, type);
	}

	public <T> T evaluate(String json, String xpath, Map<String, Object> filters, Type type) {
		json = evaluate(json, xpath, filters);

		return oson.deserialize(json, type);
	}
}
