package ca.oson.json.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicInteger;

public class NumberUtil {
	
	public static String toPlainString(Number number) {
		if (number instanceof BigDecimal) {
			return ((BigDecimal)number).toPlainString();
		}
		return new BigDecimal(number.doubleValue()).toPlainString();
	}
	
	
	public static String precision2Json(Number number, int precision, RoundingMode roundingMode) {
		String str = toPlainString(number);
		int length = str.length();
		if (length <= precision) {
			return str;
		}
		
		Class type = number.getClass();
		
		String first = str.substring(0, precision);
		int idx = first.indexOf(".");
		if (idx > -1) {
			//precision++;
			// return str.substring(0, precision);
			
			int digits = precision - idx;
			BigDecimal b = new BigDecimal(str).setScale(digits, roundingMode);
					
			return b.toPlainString();
		}
		if (first.contains("-")) {
			precision++;
			first = str.substring(0, precision);
		}
		
		String last = str.substring(precision);
		StringBuffer sb = new StringBuffer();
		length = last.length();
		
		for (int i = 0; i < length; i++) {
			char c = last.charAt(i);
			if (c == '0' || c == '-') {
				sb.append(c);
			} else if (c == '.') {
					break;
			} else {
				sb.append('0');
			}
		}
		
		return first + sb.toString();
	}
	
	
	// assume valid inputs
	public static Number setPrecision(Number number, int precision, RoundingMode roundingMode) {
		String str = toPlainString(number);
		int length = str.length();
		if (length <= precision) {
			return number;
		}
		
		Class type = number.getClass();
		
		str = precision2Json(number, precision, roundingMode);
		
		switch (type.getName()) {
		case "java.lang.Integer": return Integer.parseInt(str);
		case "int": return Integer.parseInt(str);
		case "java.lang.Long": return Long.parseLong(str);
		case "long": return Long.parseLong(str);
		case "java.lang.Byte": return Byte.parseByte(str);
		case "byte": return Byte.parseByte(str);
		case "java.lang.Double": return Double.parseDouble(str);
		case "double": return Double.parseDouble(str);
		case "java.lang.Short": return Short.parseShort(str);
		case "short": return Short.parseShort(str);
		case "java.lang.Float": return Float.parseFloat(str);
		case "float": return Float.parseFloat(str);
		case "java.math.BigDecimal": return new BigDecimal(str);
		case "java.math.BigInteger": return new BigInteger(str);
		case "java.util.concurrent.atomic.AtomicInteger": return new AtomicInteger(Integer.parseInt(str));
		case "java.util.concurrent.atomic.AtomicLong": return new AtomicInteger(Integer.parseInt(str));
		case "java.lang.Number": return Double.parseDouble(str);
		default: return Double.parseDouble(str);
		}
	}
}

