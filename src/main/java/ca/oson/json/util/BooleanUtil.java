package ca.oson.json.util;

public class BooleanUtil {
	
	public static <E> Boolean object2Boolean (E obj) {
		try {
			if (obj == null) {
				return null;
			}
			
			Class cls = obj.getClass();
			
			if (cls == boolean.class || cls == Boolean.class) {
				return (Boolean)obj;
				
			} else if (cls == String.class) {
				return string2Boolean((String)obj);
				
			} else if (cls == Character.class || cls == char.class) {
				return char2Boolean((Character)obj);
				
			}
		
		} catch (Exception ex) {}
		
		return null;
	}
	
	public static boolean isBoolean(String str) {
		if (str != null && (str.equals("true") || str.equals("false"))) {
			return true;
		}
		
		return false;
	}
	
	public static Boolean string2Boolean(String str) {
		if (str == null || str.equalsIgnoreCase("null")) {
			return null;
		}
		
		str = str.trim().toLowerCase();
	
		if (str.equals("true") || str.equals("t") ||  str.equals("1")) {
			return true;
		} else if (str.equals("false") || str.equals("f") ||  str.equals("0")) {
			return false;
		}
		
//		try {
//			return Boolean.parseBoolean(str);
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
	
		return null;
	}

	public static Boolean char2Boolean(char c) {
		if (c == '1' || c == 't' || c == 'T') {
			return true;
		} else if (c == '0' || c == 'f' || c == 'F') {
			return false;
		} else {
			return null;
		}
	}
	
	public static Boolean number2Boolean(Number n) {
		if (n == null) {
			return null;
		}
		int intValue = n.intValue();
		if (intValue == 1) {
			return true;
		} else if (intValue == 0) {
			return false;
		}
		
		return null;
	}
}
