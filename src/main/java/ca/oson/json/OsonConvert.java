package ca.oson.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import ca.oson.json.Oson.FIELD_NAMING;
import ca.oson.json.util.ConvertUtil;
import ca.oson.json.util.StringUtil;

public class OsonConvert {
	private static String DELIMITER = "_";
	private static FIELD_NAMING naming = ConvertUtil.naming;
	private static Oson oson = new Oson()
				.includeClassTypeInJson(false)
				.setFieldNaming(naming);
	
	// 
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
	
	/*
	 * Filter the Json string based on the filters criteria
	 * @param json     the Json string to filter
	 * @param filters  the filtering rule: filter out any attributes with values to be null or empty, rename any other attribute with value
	 *
	 * @return the filtered Json string
	 */
	public static String filter(String json, Map<String, Object> filters) {
		return filter(json, filters, null);
	}
	
	public static String filter(String json, Map<String, Object> filters, String root) {
		if (!StringUtil.isEmpty(root)) {
			json = OsonSearch.search(json, root, true);
			
			String rootPath = root.toLowerCase() + ".";
			Map<String, Object> map = new HashMap<>();
			for (String k: filters.keySet()) {
				String key = k.toLowerCase();
				int idx = key.indexOf(rootPath);
				if (idx == 0) {
					key = k.substring(rootPath.length());
				} else if (idx > 0 && key.charAt(idx -1) == '.') {
					key = k.substring(idx + rootPath.length());
				} else {
					key = k;
				}
				
				map.put(key, filters.get(k));
			}
			
			filters = map;
		}
		
		
		Object object = oson.deserialize(json);
		
		object = filtering (object, filters);
		
		return oson.serialize(object);
	}
	
	static Object filtering (Object object, Map<String, Object> filters) {
		return filtering (object, filters, null);
	}
	
	static Object filtering (Object object, Map<String, Object> filters, String path) {
		if (object == null) {
			return null;
		}
		
		if (Map.class.isInstance(object)) {
			Map<String, Object> map = (Map)object;
			
			if (path != null) {
				path += ".";
			} else {
				path = "";
			}
			
			Map<String, Object> newMap = new LinkedHashMap<>();
			
			for (String key: map.keySet()) {
				String name = StringUtil.formatName(key, naming);
				Object obj = filtering (map.get(key), filters, path + name);
				
				if (filters.containsKey(path + name)) {
					ConvertUtil.processNameMap(newMap, path + name, obj, filters.get(path + name));
					
				} else if (filters.containsKey(name)) {
					ConvertUtil.processNameMap(newMap, name, obj, filters.get(name));
					
				} else {
					newMap.put(key, obj);
				}
				
			}
			
			return newMap;
			
		} else if (List.class.isInstance(object)) {
			List list = (List)object;
			
			for (int i = 0; i < list.size(); i++) {
				list.set(i, filtering (list.get(i), filters, path));
			}
			
			return list;
	
		} else {
			return object;
		}
	}
	
	
	
	/*
	 * Flatten a Json data into 1 level data structure
	 * 
	 * @param json   Json string to flatten
	 * @delimiter    connect attribute or key name to form a new attribute name
	 *               If it is empty or null, then "_" will be used to make attribute name unique
	 *               in case the attribute exists
	 * 
	 * @return flattened Json string
	 */
	public static String flatten (String json, String delimiter) {
		Object obj = oson.deserialize(json);
		
		obj = flat(obj, delimiter);
		
		return oson.serialize(obj);
	}
	
	/*
	 * Flatten a Json data into 1 level data structure
	 * "_" will be used to make attribute name unique
	 * in case the attribute exists
	 * 
	 * @param json   Json string to flatten
	 * 
	 * @return flattened Json string
	 */
	public static String flatten (String json) {
		return flatten (json, null);
	}
	
	
	private static Object flat (Object obj, String delimiter) {
		if (obj == null) {
			return obj;
		}
		
		boolean emptydelimiter = StringUtil.isEmpty(delimiter);
		Map holder = new LinkedHashMap();
		
		if (Map.class.isInstance(obj)) {
			Map<String, Object> map = (Map)obj;
			
			for (Map.Entry<String, Object> entry: map.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
				
				if (value != null && Map.class.isAssignableFrom(value.getClass())) {
					Map<String, Object> values = (Map)flat (value, delimiter);
					
					for (String k: values.keySet()) {
						Object o = values.get(k);
						if (emptydelimiter) {
							if (holder.containsKey(k)) {
								holder.put(key + DELIMITER + k, o);
							} else {
								holder.put(k, o);
							}
							
						} else {
							holder.put(key + delimiter + k, o);
						}
					}
					
				} else {
					if (value != null) {
						holder.put(key, value);
					} else if (!holder.containsKey(key)) {
						holder.put(key, value);
					}
				}
			}
			
			
		} else if (List.class.isInstance(obj)) {
			List list = (List)obj;
			boolean hasMap = false;
			
			for (Object value: list) {
				if (value != null) {
					if (Map.class.isInstance(value)) {
						Map<String, Object> values = (Map)flat (value, delimiter);
						
						for (String key: values.keySet()) {
							Object object = values.get(key);
							if (!holder.containsKey(key)) {
								holder.put(key, object);
							} else {
								Object v = holder.get(key);
								if (v != null && List.class.isInstance(v)) {
									((List)v).add(object);
								} else {
									List l = new ArrayList();
									l.add(v);
									l.add(object);
									holder.put(key, l);
								}
							}
						}
						
						hasMap = true;
					}
					
				}
			}
			
			if (!hasMap) {
				return obj;
			}
			
		} else {
		
			return obj;
		}
		
		return holder;
	}
	
	
	
}
