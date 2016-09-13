package ca.oson.json.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class NumberUtil {

	public static String removeTrailingDecimalZeros(Object value) {
		String str = value.toString().trim();
		str = str.replaceFirst("\\.0*$", "");
		
		return str.replaceFirst("^(.*\\.[0-9]*[1-9])0*$", "$1");
	}
	
	public static boolean isFloatingNumber(Number number) {
		switch (number.getClass().getName()) {
		case "java.lang.Integer": return false;
		case "int": return false;
		case "java.lang.Long": return false;
		case "long": return false;
		case "java.lang.Byte": return false;
		case "byte": return false;
		case "java.lang.Double": return true;
		case "double": return true;
		case "java.lang.Short": return false;
		case "short": return false;
		case "java.lang.Float": return true;
		case "float": return true;
		case "java.math.BigDecimal": return true;
		case "java.math.BigInteger": return false;
		case "java.util.concurrent.atomic.AtomicInteger": return false;
		case "java.util.concurrent.atomic.AtomicLong": return false;
		case "java.lang.Number": return true;
		default: return true;
		}
	}
	
	public static String toPlainString(Number number, boolean appendingFloatingZero) {
		String str = toPlainString(number);

		if (appendingFloatingZero) {
			int idx = str.indexOf(".");
			if (idx == -1 && isFloatingNumber(number)) {
				return str + ".0";
			}
		}
		
		return str;
	}
	
	public static String toPlainString(Number number) {
//		if (number == null) {
//			return null;
//		}
		
		if (number instanceof BigDecimal) {
			return ((BigDecimal)number).toPlainString();
		}
		if (number instanceof BigInteger) {
			return ((BigInteger)number).toString();
		}
		
		if (isFloatingNumber(number)) {
			double value = number.doubleValue();

			String str = new BigDecimal(value).toPlainString();
			int idx = str.indexOf(".");
			if (idx > -1) {
				str = str.replaceFirst("^(.*\\.[0-9]*[1-9])0{5,}[0-9]*$", "$1");
				str = str.replaceFirst("\\.?0*$", "");
				
				String str2 = str.replaceFirst("^(.*\\.[0-9]*[0-8])9{5,}[0-9]*$", "$1");
				if (!str2.equals(str)) {
					String last = str2.substring(str2.length()-1);
					str = str2.substring(0, str2.length()-1) + (Integer.parseInt(last) + 1);
				}
			}
			
//			String str2 = value + "";
//			idx = str2.indexOf("E");
//			if (idx > -1) {
//				str2 = str2.substring(0, idx);
//			}
//			if (!str.equals(str2)) {
//				idx = str.indexOf(".");
//				int idx2 = str2.indexOf(".");
//				if (str.length() - idx > str2.length() - idx2) {
//					System.err.println(str + ":" + str2);
//				}
//			}
			
			return str;
		}
		
		return number.longValue()+"";
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

		str = precision2Json(number, precision, roundingMode);
		
		return getNumber(str, number.getClass());
	}
	
	public static <E> Number getNumber(E number, Class valueType) {
		try {
			Class type = number.getClass();
			if (Number.class.isAssignableFrom(type)) {
				Number num = (Number)number;
				switch (valueType.getName()) {
				case "java.lang.Integer": return num.intValue();
				case "int": return num.intValue();
				case "java.lang.Long": return num.longValue();
				case "long": return num.longValue();
				case "java.lang.Byte": return num.byteValue();
				case "byte": return num.byteValue();
				case "java.lang.Double": return num.doubleValue();
				case "double": return num.doubleValue();
				case "java.lang.Short": return num.shortValue();
				case "short": return num.shortValue();
				case "java.lang.Float": num.floatValue();
				case "float": return num.floatValue();
				case "java.math.BigDecimal": return new BigDecimal(num.longValue());
				case "java.math.BigInteger": return new BigInteger(num.toString());
				case "java.util.concurrent.atomic.AtomicInteger": return new AtomicInteger(num.intValue());
				case "java.util.concurrent.atomic.AtomicLong": return new AtomicLong(num.longValue());
				case "java.lang.Number": return num;
				default: return num;
				}
			}
			
			String str = removeTrailingDecimalZeros(StringUtil.unquote(number, true));
			
			switch (valueType.getName()) {
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
			case "java.util.concurrent.atomic.AtomicLong": return new AtomicLong(Integer.parseInt(str));
			case "java.lang.Number": return Double.parseDouble(str);
			default: return Double.parseDouble(str);
			}
		} catch (Exception e) {}
		
		return null;
	}
}

