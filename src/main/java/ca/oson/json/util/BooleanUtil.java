package ca.oson.json.util;

public class BooleanUtil {
	
	public static <E> boolean object2Boolean (E obj) {
		try {
			if (obj == null) {
				return false;
			}
			
			Class cls = obj.getClass();
			
			if (cls == boolean.class || cls == Boolean.class) {
				return (boolean)obj;
				
			} else if (cls == String.class) {
				return string2Boolean((String)obj);
				
			} else if (cls == Character.class || cls == char.class) {
				return char2Boolean((char)obj);
			
			} else if (cls == Character.class || cls == char.class) {
				return char2Boolean((char)obj);
				
			}
		
		} catch (Exception ex) {}
		
		return false;
	}
	
	public static boolean string2Boolean(String str) {
		if (str == null) {
			return false;
		}
		
		str = str.trim().toLowerCase();
	
		if (str.equals("true") || str.equals("t") ||  str.equals("1")) {
			return true;
		}
		
		try {
			return Boolean.parseBoolean(str);
		} catch (Exception ex) {}
	
		return false;
	}

	public static boolean char2Boolean(char c) {
		if (c == '1' || c == 't' || c == 'T') {
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean number2Boolean(Number n) {
		if (n == null) {
			return false;
		}
		if (n.intValue() == 1) {
			return true;
		}
		
		return false;
	}
}
