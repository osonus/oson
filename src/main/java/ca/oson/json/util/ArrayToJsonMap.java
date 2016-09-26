package ca.oson.json.util;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public class ArrayToJsonMap {
	public static Map<String, Object> array2Map(Object[] array) {
		Map<String, Object> map = new LinkedHashMap<>();

		int length = array.length - 1;

		for (int i = 0; i < length; i++) {
			map.put(array[i].toString(), array[++i]);
		}

		return map;
	}
	public static Map<String, Object> array2Map(String[] array) {
		Map<String, Object> map = new LinkedHashMap<>();

		int length = array.length - 1;

		for (int i = 0; i < length; i++) {
			map.put(array[i], array[++i]);
		}

		return map;
	}
	
	public static Map<Object, Object> list2Map(List list) {
		Map<Object, Object> map = new LinkedHashMap<>();

		int length = list.size();
		for (int i = 0; i < length; i++) {
			Object obj = list.get(i);
			
			if (Map.class.isAssignableFrom(obj.getClass())) {
				map.put(obj, list.get(++i));
				
			} else if (List.class.isAssignableFrom(obj.getClass())) {
				List l = (List)obj;
				if (l.size() == 2) {
					map.put(l.get(0), l.get(1));
				}
			}
		}

		return map;
	}

	public static JSONObject array2Json(Object[] array) {
		JSONObject obj = new JSONObject();

		int length = array.length - 1;

		for (int i = 0; i < length; i++) {
			obj.put(array[i].toString(), array[++i]);
		}

		return obj;
	}

	public static Map<String, Object> json2Map(JSONObject json) {
		Map<String, Object> names = new LinkedHashMap<>();
		JSONArray array = json.names();
		int length = array.length();
		for (int i = 0; i < length; i++) {
			String key = array.get(i).toString();
			if (key != null && key.length() > 0) {
				names.put(key.trim(), json.get(key));
			}
		}

		return names;
	}
}

