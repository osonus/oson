package ca.oson.json;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONObject;

import ca.oson.json.Oson.FIELD_NAMING;
import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.util.NumberUtil;
import ca.oson.json.util.ObjectUtil;
import ca.oson.json.util.StringUtil;

public class OsonMerge {
	private static FIELD_NAMING naming = FIELD_NAMING.UNDERSCORE_LOWER;
	
	public static enum NUMERIC_VALUE {
		KEEP_NEW, // additional parameter values take precedence over previous ones
		KEEP_OLD, // keep previous values
		KEEP_MAX, // keep the maximum value, in case numeric
		KEEP_MIN, // keep the smallest value
		AVERAGE // take an average value of all numeric values
		// FREQUENT // keep the most frequent value: too expensive to implement
		// need to hold it to the end of loops, might implement in the future
	}
	
	public static enum OTHER_VALUE {
		KEEP_NEW, // additional parameter values take precedence over previous ones
		KEEP_OLD // keep previous values
		// FREQUENT // keep the most frequent value: too expensive to implement
		// need to hold it to the end of loops, might implement in the future
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
		KEEP_UNIQUE // keeps unique set of values
	}
	
	public static class Config
	{
		/*
		 * How to use values, the default is to ignore null and empty values
		 */
		public JSON_INCLUDE defaultType = JSON_INCLUDE.NON_EMPTY;
		
		/*
		 * How to keep numeric attribute values, the default is 
		 * to use a new value to replace the earlier value
		 */
		public NUMERIC_VALUE numericValue = NUMERIC_VALUE.KEEP_NEW;
		
		/*
		 * How to keep non-numeric attribute values, the default is 
		 * to use a new value to replace the earlier value
		 */
		public OTHER_VALUE otherValue = OTHER_VALUE.KEEP_NEW;
		
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
		
		/*
		 * Old names are replaced by new names in the result:
		 * {"oldName": "newName"}
		 */
		public Map<String,String> names = new HashMap<>();
		
	}

	private Config config;
	private Oson oson;
	
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
		
		if (this.config.names != null && this.config.names.size() > 0) {
			for (String key: this.config.names.keySet()) {
				this.config.names.put(StringUtil.formatName(key, naming), this.config.names.get(key));
			}
		}
	}
	
	
	private Object mergeObjects(Object object, Object obj) {
		if (object == null) {
			return obj;
		} else if (obj == null) {
			return object;
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
			
			if (this.config.nonOverlapValue == NON_OVERLAP_VALUE.KEEP_BOTH) {
				keepNew = true;
				keepOld = true;
			} else if (this.config.nonOverlapValue == NON_OVERLAP_VALUE.KEEP_NEW) {
				keepNew = true;
			} else if (this.config.nonOverlapValue == NON_OVERLAP_VALUE.KEEP_OLD) {
				keepOld = true;
			}
			

			for (String key : objMap.keySet()) {
				String name = StringUtil.formatName(key, naming);
				
				Object o = objMap.get(key);
				
				Object ob = null;
				
				if (nameKeys.containsKey(name)) {
					ob = mergeObjects(objectMap.get(nameKeys.get(name)), o);
					nameKeys.remove(name);
					
				} else if (keepNew) {
					ob = o;
				}
				
				if (ob != null) {
					if (this.config.names.containsKey(name)) {
						newMap.put(this.config.names.get(name), ob);
					} else {
						newMap.put(key, ob);
					}
				}
				
			}
			
			
			if (keepOld && nameKeys.size() > 0) {
				for (String name: nameKeys.keySet()) {
					if (this.config.names.containsKey(name)) {
						newMap.put(this.config.names.get(name), objectMap.get(nameKeys.get(name)));
					} else {
						newMap.put(nameKeys.get(name), objectMap.get(nameKeys.get(name)));
					}
				}
			}
			
			return newMap;
			
		} else if (List.class.isInstance(object) && List.class.isInstance(obj)) {
			switch (this.config.listValue) {
			case KEEP_NEW:
				return obj;
			case KEEP_OLD:
				return object;
			case APPENDING:
				((List)object).addAll((List)obj);
				return object;
			case KEEP_UNIQUE:
				Set set = new LinkedHashSet((List)object);
				set.addAll((List)obj);
				return new ArrayList(set);
			}
		}
		
		
		switch (this.config.defaultType) {
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

		if ( obj != null && object != null && 
				Number.class.isAssignableFrom(obj.getClass()) &&
				Number.class.isAssignableFrom(object.getClass()) ) {
			switch (this.config.numericValue) {
			case KEEP_NEW:
				return obj;
			case KEEP_OLD:
				return object;
			case KEEP_MAX:
				return NumberUtil.max((Number)object, (Number)obj);
			case KEEP_MIN:
				return NumberUtil.min((Number)object, (Number)obj);
			case AVERAGE:
				return NumberUtil.avg((Number)object, (Number)obj);
			}
			
		} else {
			switch (this.config.otherValue) {
			case KEEP_NEW:
				return obj;
			case KEEP_OLD:
				return object;
			}
		}
		
		
		return object;
	}
	
	/*
	 * Merge Json string data, return a Json result
	 */
	public String merge(String json, String... jsons) {
		Object object = oson.deserialize(json);
		
		Object obj;
		for (String jsn: jsons) {
			obj = oson.deserialize(jsn);
			object = mergeObjects(object, obj);
		}
		
		return oson.serialize(object);
	}
	
	/*
	 * Merge JSONObject objects, by creating a new JSONObject object
	 * to hold the resulting data
	 */
	public JSONObject merge(JSONObject source, JSONObject... sources) {
		Object object = oson.deserialize(source);
		
		Object obj;
		for (JSONObject src: sources) {
			obj = oson.deserialize(src);
			object = mergeObjects(object, obj);
		}
		
		return ObjectUtil.getJSONObject(oson.serialize(object));
	}
	
	
	/*
	 * Merge Java objects, by dynamically creating a new Java class 
	 * to hold the resulting data, using javassist library
	 */
	public Object merge(Object ob, Object... obs) {
		Object object = oson.deserialize(oson.serialize(ob));
		
		Object obj;
		for (Object objt: obs) {
			obj = oson.deserialize(oson.serialize(objt));
			object = mergeObjects(object, obj);
		}
		
		// to modify later
		return object;
	}

}
