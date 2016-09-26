package ca.oson.json.util;

import java.util.Map;

import ca.oson.json.MapData;
import ca.oson.json.Oson.FIELD_NAMING;
import ca.oson.json.function.ConvertFunction;

import com.google.common.base.Function;

public class ConvertUtil {
	public static FIELD_NAMING naming = FIELD_NAMING.CAMELCASE;
	
	public static void processNameMap(Map<String, Object> newMap, String key, Object value, Object replacer) {
		if (StringUtil.isEmpty(replacer)) {
			return;
		}

		Class type = replacer.getClass();
		
		if (type == String.class) {
			newMap.put(replacer.toString(), value);
			
		} else if (ConvertFunction.class.isInstance(replacer)) { //Function.class.isAssignableFrom(type)) {
			ConvertFunction function = (ConvertFunction)replacer;
			
			MapData data = new MapData(newMap, key, value);
			Object obj = function.apply(data);
			if (!StringUtil.isEmpty(obj) && !Map.class.isInstance(obj)) {
				newMap.put(key, obj);
			}
			
		} else {
			try {
				Function function = (Function)replacer;
				Object obj = function.apply(value);
				if (!StringUtil.isEmpty(obj)) {
					newMap.put(key, obj);
				}
			} catch (Exception e) {}
		}
	}
	
	
}
