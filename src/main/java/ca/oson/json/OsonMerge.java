package ca.oson.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONObject;
import org.json.JSONArray;

import ca.oson.json.Oson.FIELD_NAMING;
import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.util.NumberUtil;
import ca.oson.json.util.ObjectUtil;
import ca.oson.json.util.StringUtil;

public class OsonMerge {
	private static FIELD_NAMING naming = FIELD_NAMING.CAMELCASE;
	
	public static enum NUMERIC_VALUE {
		KEEP_NEW, // additional parameter values take precedence over previous ones
		KEEP_OLD, // keep previous values
		KEEP_MAX, // keep the maximum value, in case numeric
		KEEP_MIN, // keep the smallest value
		AVERAGE, // take an average value of all numeric values
		FREQUENT // keep the most frequent value
	}
	
	public static enum OTHER_VALUE {
		KEEP_NEW, // additional parameter values take precedence over previous ones
		KEEP_OLD, // keep previous values
		FREQUENT // keep the most frequent value
	}
	
	public static enum NON_OVERLAP_VALUE {
		KEEP_NEW, // additional parameter values take precedence over previous ones
		KEEP_OLD, // keep previous values
		KEEP_BOTH // keep both of the Non-overlapping values
	}
	
	
	public static enum LIST_VALUE {
		KEEP_NEW, // additional parameter values take precedence over previous ones
		KEEP_OLD, // keep previous values
		APPENDING, // concate arrays/lists with the same name
		KEEP_UNIQUE, // keeps unique set of values
		MERGE // merge each items of lists into 1, using specific merging rules for specific values, 
		// either NUMERIC_VALUE or OTHER_VALUE rules
	}
	
	public static class Config
	{
		/*
		 * How to use values, the default is to ignore null and empty values
		 */
		public JSON_INCLUDE defaultType = JSON_INCLUDE.NON_EMPTY;
		
		/*
		 * How to keep numeric attribute values, the default is 
		 * to use the average value
		 */
		public NUMERIC_VALUE numericValue = NUMERIC_VALUE.AVERAGE;
		
		/*
		 * when averaging numeric values, the biggest difference allowed between the average value and the other values
		 * Say the values are 1, 10, 13, 15, 10000, if the the error threshold is 20, then the largest number 10000 
		 * is removed from the rest to calculate the average.
		 */
		public double errorThreshold = 0;
		
		/*
		 * How to keep non-numeric attribute values, the default is 
		 * to use the most frequent value
		 */
		public OTHER_VALUE otherValue = OTHER_VALUE.FREQUENT;
		
		/*
		 * How to handle the cases that some attributes only exist in the old object, 
		 * or the other way. The default is to keep both of them in the returning object
		 */
		public NON_OVERLAP_VALUE nonOverlapValue = NON_OVERLAP_VALUE.KEEP_BOTH;
		
		/*
		 * How to keep values of arrays, the default is to append or concatenate values
		 */
		public LIST_VALUE listValue = LIST_VALUE.APPENDING;
		
		/*
		 * Oson.FIELD_NAMING rules for attribute names in the result
		 * unless specified by names map
		 */
		public FIELD_NAMING namingPolicy = FIELD_NAMING.FIELD;

	}

	private Config config;
	
	/*
	 * key: attribute or key names, or path of them
	 * this is used to allow specific attribute or path to have specific configuration.
	 * Each of this configuration entry in the map is a specialized copy of the global config. 
	 * This settings make the merge to be very flexible in the way it can be used.
	 * 
	 */
	private Map<String,Config> configs = new HashMap<>();

	/*
	 * Old names are replaced by new names in the result:
	 * {"oldName": "newName"}
	 */
	private Map<String,String> replacedByNames = new HashMap<>();

	private Oson oson;
	private Map<String, Map<Object, Integer>> cachedValues = new ConcurrentHashMap<>();
	private Map<String, List<Number>> cachedListValues = new ConcurrentHashMap<>();
	
	/*
	 * format returned Json output
	 */
	public boolean pretty = false;
	
	public OsonMerge() {
		this(new Config());
	}

	public OsonMerge(Config config) {
		ObjectUtil.getJSONObject(null);
		this.config = config;
		oson = new Oson()
			.setDefaultType(config.defaultType)
			.setFieldNaming(config.namingPolicy)
			.includeClassTypeInJson(false);
		
		if (config.namingPolicy != FIELD_NAMING.FIELD) {
			naming = config.namingPolicy;
		}
	}
	
	
	public Config getConfig() {
		return config;
	}

	public void setConfig(Config config) {
		this.config = config;
	}
	
	private Map<String, String> getNames() {
		return replacedByNames;
	}

	public void setNames(Map<String, String> names) {
		if (names != null && names.size() > 0) {
			for (String key: names.keySet()) {
				this.replacedByNames.put(StringUtil.formatName(key, naming), names.get(key));
			}
		} else {
			this.replacedByNames.clear();
		}
	}
	
	
	private void clear() {
		cachedListValues.clear();
		cachedValues.clear();
	}
	
	
	private Object mergeObjects(Object object, Object obj) {
		return mergeObjects(object, obj, null);
	}
	private Object mergeObjects(Object object, Object obj, String path) {
		if (StringUtil.isNull(object)) {
			return obj;
		} else if (StringUtil.isNull(obj)) {
			return object;
		}
		
		Config config;
		if (path != null && configs.containsKey(path)) {
			config = configs.get(path);
		} else {
			config = this.config;
		}
		
		if (Map.class.isInstance(object) && Map.class.isInstance(obj)) {
			Map<String, Object> objectMap = (Map)object;
			Map<String, Object> objMap = (Map)obj;
			
			Map<String,String> nameKeys = new HashMap<>();

			for (String key : objectMap.keySet()) {
				String name = StringUtil.formatName(key, naming);
				nameKeys.put(name, key);
			}
			
			Map<String, Object> newMap = new LinkedHashMap<>();
			
			boolean keepNew = false;
			boolean keepOld = false;
			
			if (config.nonOverlapValue == NON_OVERLAP_VALUE.KEEP_BOTH) {
				keepNew = true;
				keepOld = true;
			} else if (config.nonOverlapValue == NON_OVERLAP_VALUE.KEEP_NEW) {
				keepNew = true;
			} else if (config.nonOverlapValue == NON_OVERLAP_VALUE.KEEP_OLD) {
				keepOld = true;
			}
			
			
			if (path != null) {
				path += ".";
			} else {
				path = "";
			}

			for (String key : objMap.keySet()) {
				String name = StringUtil.formatName(key, naming);
				
				Object o = objMap.get(key);
				
				Object ob = null;
				
				if (nameKeys.containsKey(name)) {
					ob = mergeObjects(objectMap.get(nameKeys.get(name)), o, path + name);
					nameKeys.remove(name);

				} else if (keepNew) {
					ob = o;
				}
				
				if (ob != null) {
					boolean ignore = false;
					switch (config.defaultType) {
					case NON_NULL:
						if (StringUtil.isNull(ob)) {
							ignore = true;
						}
						break;
						
					case NON_EMPTY:
						if (StringUtil.isEmpty(ob)) {
							ignore = true;
						}
						break;
						
					case NON_DEFAULT:
						if (DefaultValue.isDefault(ob)) {
							ignore = true;
						}
						break;
					}
					
					if (ignore) {
						continue;
					}
					
					
					if (replacedByNames.containsKey(path + name)) {
						if (!StringUtil.isEmpty(replacedByNames.get(path + name))) {
							newMap.put(replacedByNames.get(path + name), ob);
						}
						
					} else if (replacedByNames.containsKey(name)) {
						if (!StringUtil.isEmpty(replacedByNames.get(name))) {
							newMap.put(replacedByNames.get(name), ob);
						}
							
					} else {
						newMap.put(key, ob);
					}
				}
				
			}
			
			
			if (keepOld && nameKeys.size() > 0) {
				for (String name: nameKeys.keySet()) {
					Object ob = objectMap.get(nameKeys.get(name));
					boolean ignore = false;
					switch (config.defaultType) {
					case NON_NULL:
						if (StringUtil.isNull(ob)) {
							ignore = true;
						}
						break;
						
					case NON_EMPTY:
						if (StringUtil.isEmpty(ob)) {
							ignore = true;
						}
						break;
						
					case NON_DEFAULT:
						if (DefaultValue.isDefault(ob)) {
							ignore = true;
						}
						break;
					}
					
					if (ignore) {
						continue;
					}
					
					if (replacedByNames.containsKey(path + name)) {
						if (!StringUtil.isEmpty(replacedByNames
								.get(path + name))) {
							newMap.put(replacedByNames.get(path + name), ob);
						}
					} else if (replacedByNames.containsKey(name)) {
						if (!StringUtil.isEmpty(replacedByNames.get(name))) {
							newMap.put(replacedByNames.get(name), ob);
						}

					} else {
						newMap.put(nameKeys.get(name), ob);
					}
				}
			}
			
			return newMap;
			
		} else if (List.class.isInstance(object) || List.class.isInstance(obj)) {
			List list = null;
			switch (config.listValue) {
			case KEEP_NEW:
				return obj;

			case KEEP_OLD:
				return object;

			case APPENDING:
				if (List.class.isInstance(object)) {
					if (List.class.isInstance(obj)) {
						((List)object).addAll((List)obj);
						return object;
					} else {
						((List)object).add(obj);
						return object;
					}
					
				} else {
					((List)obj).add(object);
					return obj;
				}
				
			case KEEP_UNIQUE:
				Set set = new LinkedHashSet();
				if (List.class.isInstance(object)) {
					set.addAll((List)object);
				} else {
					set.add(object);
				}
				
				if (List.class.isInstance(obj)) {
					set.addAll((List)obj);
				} else {
					set.add(obj);
				}
				
				return new ArrayList(set);

			case MERGE:

				Object head;
				if (List.class.isInstance(object)) {
					list = (List) object;
					head = list.get(0);
					int size = list.size();

					for (int i = 1; i < size; i++) {
						head = mergeObjects(head, list.get(i), path);
					}

				} else {
					head = object;
				}
				
				if (List.class.isInstance(obj)) {
					list = (List) obj;
					int size = list.size();

					for (int i = 0; i < size; i++) {
						head = mergeObjects(head, list.get(i), path);
					}

				} else {
					head = mergeObjects(head, obj, path);
				}
				
				return head;
			}
			
			
			return object;
		}
		
		
		switch (config.defaultType) {
		case NON_NULL:
			if (StringUtil.isNull(obj)) {
				return object;
			}
			if (StringUtil.isNull(object)) {
				return obj;
			}
			break;
			
		case NON_EMPTY:
			if (StringUtil.isEmpty(obj)) {
				return object;
			}
			if (StringUtil.isEmpty(object)) {
				return obj;
			}
			break;
			
		case NON_DEFAULT:
			if (DefaultValue.isDefault(obj)) {
				return object;
			}
			if (DefaultValue.isDefault(object)) {
				return obj;
			}
			break;
			
		}

		String myroot;
		if (path != null) {
			myroot = path;
		} else {
			myroot = "ROOT";
		}
		
		if ( obj != null && object != null && 
				Number.class.isAssignableFrom(obj.getClass()) &&
				Number.class.isAssignableFrom(object.getClass()) ) {
			switch (config.numericValue) {
			case KEEP_NEW:
				return obj;
			case KEEP_OLD:
				return object;
			case KEEP_MAX:
				return NumberUtil.max((Number)object, (Number)obj);
			case KEEP_MIN:
				return NumberUtil.min((Number)object, (Number)obj);
			case AVERAGE:
				List<Number> values = cachedListValues.get(myroot);
				if (values == null) {
					values = new ArrayList();
					values.add((Number)object);
					cachedListValues.put(myroot, values);
				}
				values.add((Number)obj);
				
				return NumberUtil.avg(values, config.errorThreshold);
				
			case FREQUENT:
				
			}
			
		} else {
			switch (config.otherValue) {
			case KEEP_NEW:
				return obj;
			case KEEP_OLD:
				return object;
			case FREQUENT:
			}
		}

		// most FREQUENT
		Map<Object, Integer> map = cachedValues.get(myroot);
		if (map == null) {
			map = new HashMap<>();
			map.put(object, 1);
			cachedValues.put(myroot, map);
		}
		if (map.containsKey(obj)) {
			map.put(obj, map.get(obj) + 1);
		} else {
			map.put(obj, 1);
		}

		int max = 0;
		for (Map.Entry<Object, Integer> entry: map.entrySet()) {
			if (entry.getValue() > max) {
				max = entry.getValue();
				object = entry.getKey();
			}
		}

		return object;
	}

	
	/*
	 * Merge Json string data, return a Json result
	 */
	public String merge(String json, String... jsons) {
		Object object = oson.deserialize(json);
		 clear();

		if ((jsons == null || jsons.length == 0) && List.class.isInstance(object)) {
			object = merge((List)object);

		} else {
			Object obj;
			for (String jsn: jsons) {
				obj = oson.deserialize(jsn);
				object = mergeObjects(object, obj);
			}
		}
		
		return oson.pretty(pretty).serialize(object);
	}
	
	/*
	 * Merge JSONObject objects, by creating a new JSONObject object
	 * to hold the resulting data
	 */
	public JSONObject merge(JSONObject source, JSONObject... sources) {
		Object object = oson.deserialize(source);
		clear();
		
		Object obj;
		for (JSONObject src: sources) {
			obj = oson.deserialize(src);
			object = mergeObjects(object, obj);
		}
		
		return ObjectUtil.getJSONObject(oson.serialize(object));
	}
	
	private Object merge(List list) {
		Object object = null;
		int size = list.size();
		if (size > 0) {
			object = list.get(0);
			Object obj;
			for (int i = 1; i < size; i++) {
				obj= list.get(i);
				object = mergeObjects(object, obj);
			}
		}
		
		return object;
	}
	
	
	/*
	 * Merge JSONObject objects, by creating a new JSONObject object
	 * to hold the resulting data
	 */
	public JSONObject merge(JSONArray source, JSONArray... sources) {
		Object object = oson.deserialize(source.toString());
		 clear();

		if ((sources == null || sources.length == 0) && List.class.isInstance(object)) {
			object = merge((List)object);
			
		} else {
			Object obj;
			for (JSONArray src: sources) {
				obj = oson.deserialize(src.toString());
				object = mergeObjects(object, obj);
			}
		}
		
		return ObjectUtil.getJSONObject(oson.serialize(object));
	}
	
	
	/*
	 * Merge Java objects, by dynamically creating a new Java class 
	 * to hold the resulting data, using javassist library
	 */
	public Object merge(Object ob, Object... obs) {
		Object object = oson.deserialize(oson.serialize(ob));
		 clear();
		
		Object obj;
		if ((obs == null || obs.length == 0) && List.class.isInstance(object)) {
			object = merge((List)object);
			
		} else {
			for (Object objt: obs) {
				obj = oson.deserialize(oson.serialize(objt));
				object = mergeObjects(object, obj);
			}
		}
		
		// to modify later
		return object;
	}

}
