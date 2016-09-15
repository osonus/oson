package ca.oson.json.util;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import ca.oson.json.JSONObject;

public class ArrayToJsonMap {
	public static Map<String, Object> array2Map(Object[] array) {
		Map<String, Object> map = new HashMap<>();

		int length = array.length;

		for (int i = 0; i < length; i++) {
			map.put(array[i].toString(), array[++i]);
		}

		return map;
	}

	public static JSONObject array2Json(Object[] array) {
		JSONObject obj = new JSONObject();

		int length = array.length;

		for (int i = 0; i < length; i++) {
			obj.put(array[i].toString(), array[++i]);
		}

		return obj;
	}

	public static Map<String, Object> json2Map(JSONObject json) {
		Map<String, Object> names = new HashMap<>();
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

