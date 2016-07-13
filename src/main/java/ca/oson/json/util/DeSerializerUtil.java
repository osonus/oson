package ca.oson.json.util;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import ca.oson.json.DataMapper;
import ca.oson.json.Oson;
import ca.oson.json.Oson.FieldData;
import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.function.DataMapper2JsonFunction;
import ca.oson.json.function.Json2DataMapperFunction;

/*
 * provides some default creator, method, serializer, and deserializer for special data types
 */
public class DeSerializerUtil {
	
	/*
	 * some abstract class or interface, nowhere can know its implementation
	 */
	public static Class implementingClass(String className) {
		switch (className) {
		case "com.google.gson.JsonElement": return com.google.gson.JsonPrimitive.class;


		}
		
		return null;
	}
	

	public static String getJsonValueFieldName(String className) {
		switch (className) {
		case "java.net.URL": return "toString";
		case "java.lang.StringBuilder": return "toString";
		case "java.lang.StringBuffer": return "toString";
		case "java.util.UUID": return "toString";
		case "java.util.Locale": return "toString";
		case "com.google.gson.JsonElement": return "toString";
		case "com.google.gson.JsonPrimitive": return "toString";
		

		case "java.net.Inet4Address": return "hostAddress";
		case "java.net.URI": return "toURL";

		}
		
		return null;
	}
	
	
	public static Function getDeserializer(String className) {
		Json2DataMapperFunction function = null;
		switch (className) {
		case "java.util.BitSet": 
			function = (DataMapper dataMapper) -> {
				BitSet bitSet = (BitSet)dataMapper.getObj();
				
				String json = (String)dataMapper.getValueObject();
				
				if (!(json.startsWith("[") && json.endsWith("]"))) {
					return bitSet;
				}

				Oson oson = new Oson();
				
				Boolean[] bits = oson.deserialize(json, Boolean[].class);
				
				for (int i = 0; i < bits.length; i++) {
					if (bits[i]) {
						bitSet.set(i);
					}
				}
				
				return bitSet;
			};
			break;
			
		case "java.util.Calendar":
		case "java.util.GregorianCalendar":
			function = (DataMapper dataMapper) -> {
				Calendar calendar = (Calendar)dataMapper.getObj();
				
				Map<String, Object> map = dataMapper.getMap();
				
				Map<String, Integer> fields = new HashMap<>();
				fields.put("year", Calendar.YEAR);
				fields.put("month", Calendar.MONTH);
				fields.put("dayofmonth", Calendar.DAY_OF_MONTH);
				fields.put("hourofday", Calendar.HOUR_OF_DAY);
				fields.put("minute", Calendar.MINUTE);
				fields.put("second", Calendar.SECOND);
				
				//{year:2009,month:2,dayOfMonth:11,hourOfDay:14,minute:29,second:23}
				for (Entry<String, Object> entry: map.entrySet()) {
					String key = entry.getKey().replace("_", "").toLowerCase();
					Integer field = fields.get(key);
					if (field != null) {
						int value = Integer.parseInt(entry.getValue().toString());
						calendar.set(field, value);
					}
				}
				
				return calendar;
			};
			break;

			
		}
		
		return function;
	}
	
	public static Function getSerializer(String className) {
		DataMapper2JsonFunction function = null;
		switch (className) {
		case "java.util.BitSet": 
			function = (DataMapper dataMapper) -> {
				BitSet bitSet = (BitSet)dataMapper.getObj();
				int level = dataMapper.getLevel();
				int indentation = dataMapper.getPrettyIndentation();
				String repeated = StringUtil.getPrettyIndentationln(level, indentation);
				String repeatedItem = StringUtil.getPrettyIndentationln(level + 1, indentation);
				int length = bitSet.length();
				if (length > 0) {
					StringBuilder sb = new StringBuilder();
					sb.append("[" + repeatedItem);
					sb.append(bitSet.get(0) ? '1' : '0');
					
					for (int i = 1; i < bitSet.length(); i++) {
						sb.append(',');
						sb.append(repeatedItem);
						sb.append(bitSet.get(i) ? '1' : '0');
					}
					sb.append(repeated + "]");
					
					return sb.toString();
				
				} else {
					return "";
				}
			};
			break;
			
		case "java.util.Calendar":
		case "java.util.GregorianCalendar":
			function = (DataMapper dataMapper) -> {
				Calendar calendar = (Calendar)dataMapper.getObj();
				if (calendar == null) {
					return null;
				}

				Map<String, Integer> fields = new LinkedHashMap<>();
				fields.put("year", Calendar.YEAR);
				fields.put("month", Calendar.MONTH);
				fields.put("dayOfMonth", Calendar.DAY_OF_MONTH);
				fields.put("hourOfDay", Calendar.HOUR_OF_DAY);
				fields.put("minute", Calendar.MINUTE);
				fields.put("second", Calendar.SECOND);
				
				//{year:2009,month:2,dayOfMonth:11,hourOfDay:14,minute:29,second:23}
				int level = dataMapper.getLevel();
				int indentation = dataMapper.getPrettyIndentation();
				String repeated = StringUtil.getPrettyIndentationln(level, indentation);
				String repeatedItem = StringUtil.getPrettyIndentationln(level + 1, indentation);
				String pretty = StringUtil.getPrettySpace(indentation);
				
				StringBuilder sb = new StringBuilder();
				sb.append("{");
				for (Entry<String, Integer> entry: fields.entrySet()) {
					String key = entry.getKey();
					Integer field = fields.get(key);
					int value = calendar.get(field);
					
					sb.append(repeatedItem + "\"" + key + "\":" + pretty);
					sb.append(value);
					sb.append(",");
				}
				int length = sb.length();
				sb.replace(length - 1, length, repeated + "}");
				
				return sb.toString();
			};
			break;
			
		}
		
		return function;
	}
	
	
}
