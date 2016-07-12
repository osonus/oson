package ca.oson.json.util;

import java.util.Arrays;
import java.util.BitSet;
import java.util.function.Function;

import ca.oson.json.DataMapper;
import ca.oson.json.Oson;
import ca.oson.json.function.DataMapper2JsonFunction;
import ca.oson.json.function.Json2DataMapperFunction;

public class ClassMapperUtil {
	
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
			

			
		}
		
		return function;
	}
	
	
}
