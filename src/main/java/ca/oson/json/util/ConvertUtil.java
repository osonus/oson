package ca.oson.json.util;

import java.util.Map;
import java.util.function.Function;

import ca.oson.json.MapData;
import ca.oson.json.Oson;
import ca.oson.json.Oson.FIELD_NAMING;
import ca.oson.json.function.ConvertFunction;

public class ConvertUtil {
	public static FIELD_NAMING naming = FIELD_NAMING.CAMELCASE;
	
	/*
	 * Change the values and keys of a map object.
	 * 
	 * @param mapObject     the map object to modify
	 * @param key           the key of the map to modify
	 * @param value         the value of the key in the map
	 * @param replacer      this parameter determines how the key and value would be modified
	 *                      if it is a null or empty string or object, this key will be removed from the map object if it exists
	 *                      if it is a normal text string, it will become the new key for value in the map
	 *                      if it is a Json string, the Json string will be deserialized into a List or Map, and used as the value for key
	 *                      if it is ConvertFunction lambda expression, mapObject would be modified inside that function any way you like,
	 *                           and if the returned value from this ConvertFunction's execution is not empty or not a Map, then this 
	 *                           returned value would be used as a new value for key in mapObject
	 *                      if it is a normal Function lambda expression, then this function will be executed and its returned value 
	 *                           will be used as the value for key, unless it is null or empty
	 *                      if it is any other object type, then it will be used as the new value for the key in mapObject
	 *                           
	 * @return the modified object map, which is actually unnecessary as any update here occurs to the original map object.
	 */
	public static Map<String, Object> processNameMap(Map<String, Object> mapObject, String key, Object value, Object replacer) {
		if (StringUtil.isEmpty(replacer)) {
			try {
				mapObject.remove(key);
			} catch (Exception e) {}
			return mapObject;
		}

		Class type = replacer.getClass();
		
		if (type == String.class) {
			String newKeyValue = replacer.toString();
			if (StringUtil.parenthesized(newKeyValue)) {
				mapObject.put(key, Oson.getListMapObject(newKeyValue));
				
			} else {
				mapObject.put(newKeyValue, value);
			}
			
		} else if (ConvertFunction.class.isInstance(replacer)) { //Function.class.isAssignableFrom(type)) {
			ConvertFunction function = (ConvertFunction)replacer;
			
			MapData data = new MapData(mapObject, key, value);
			Object obj = function.apply(data);
			if (!StringUtil.isEmpty(obj) && !Map.class.isInstance(obj)) {
				mapObject.put(key, obj);
			}
			
		} else if (Function.class.isInstance(replacer)) {
			try {
				Function function = (Function)replacer;
				Object obj = function.apply(value);
				if (!StringUtil.isEmpty(obj)) {
					mapObject.put(key, obj);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		} else {
			mapObject.put(key, replacer);
		}
		
		return mapObject;
	}
	
	
}
