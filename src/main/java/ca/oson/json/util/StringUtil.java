package ca.oson.json.util;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Optional;
import java.util.regex.Pattern;

import ca.oson.json.Oson.FIELD_NAMING;

public class StringUtil {
	public static final char SPACE = ' ';
	
	public static String applyPatterns(String content, Pattern[] patterns) {
		if (patterns == null || patterns.length == 0) {
			return content;
		}
	    
	    for (Pattern pattern: patterns) {
	    	content = pattern.matcher(content).replaceAll("");
	    }
	    
	    return content;
	}
	
	public static Pattern[] compilePatterns(String[] strpatterns) {
		if (strpatterns == null || strpatterns.length == 0) {
			return null;
		}
		Pattern[] patterns = new Pattern[strpatterns.length];
	    
	    for (int i = 0; i < strpatterns.length; i++) {
	    	patterns[i] = Pattern.compile(strpatterns[i],
		            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	    }
	    
	    return patterns;
	}
	
	
	public static <T> boolean isNull(T obj) {
		if (obj == null) return true;
		
		if (obj instanceof Optional) {
			Optional<T> opt = Optional.ofNullable(obj);
			
			if (!opt.isPresent()) {
				return true;
			}
		}
		
		return false;
	}
	
	public static <T> boolean isEmpty(T obj) {
		if (isNull(obj)) {
			return true;
		}
		
		String str = obj.toString().trim();
		return (str.length() == 0 || str.equalsIgnoreCase("null") || str.equals("\"\"") || str.equals("''") || obj.getClass().getName().equals("org.json.JSONObject$Null"));
	}

	public static boolean isEmpty(String str) {
		if (str == null) {
			return true;
		}

		return (str.length() == 0 || str.equals("\"\"") || str.equals("''"));
	}
	
	public static String getPrettyIndentationln(int level, int indentation) {
		if (indentation > 0) {
			return "\n" + StringUtil.repeatSpace(level * indentation);
		}

		return "";
	}
	
	public static String repeatSpace(int repeat) {
		return repeatChar(SPACE, repeat);
	}

	public static String repeatChar(char c, int repeat) {
		if (repeat < 1) {
			return "";
		}
		return (new String(new char[repeat]).replace('\0', c));
	}

	public static String underscore2CamelCase(String name) {
		int idx = name.indexOf('_');
		if (idx == -1) {
			return name;
		}

		String[] parts = name.split("_");
		String camelCaseString = parts[0];

		for (int i = 1; i < parts.length; i++) {
			camelCaseString = camelCaseString + capitalize(parts[i]);
		}
		return camelCaseString;
	}


	private static String camelCase2Delimiter(String name, char delimiter) {
		if (name == null || !name.matches(".*[A-Z].*"))
			return name;

		String regex = "([a-z])([A-Z])";
		String replacement = "$1" + delimiter + "$2";

		return name.replaceAll(regex, replacement);
	}

	public static String camelCase2Underscore(String name) {
		return camelCase2Delimiter(name, '_');
	}

	public static String camelCase2Space(String name) {
		return camelCase2Delimiter(name, ' ');
	}

	public static String camelCase2Dash(String name) {
		return camelCase2Delimiter(name, '-');
	}

	//
	public static String camelCase(String name) {
		if (name == null) {
			return null;
		}
		String regex = "[_ -]([a-zA-Z])";
		String replacement = "_$1";

		return uncapitalize(underscore2CamelCase(name.replaceAll(regex, replacement)));
	}


	public static String camelCase2UnderscoreLowercase(String name) {
		return camelCase2Underscore(name).toLowerCase().replace('-', '_');
	}


	public static String camelCase2UnderscoreUppercase(String name) {
		return camelCase2Underscore(name).toUpperCase().replace('-', '_');
	}



	public static String capitalize(String name) {
		if (name == null || name.length() == 0) {
			return name;
		}

		return name.substring(0, 1).toUpperCase() + name.substring(1); //.toLowerCase();
	}

	public static String uncapitalize(String name) {
		if (name == null || name.length() == 0) {
			return name;
		}

		char first = name.charAt(0);

		if (first >= 65 && first <= 90) { // uppercase + 32
			return Character.toLowerCase(first) + name.substring(1);
		} else {
			return name;
		}
	}

	public static String escapeDoublequote(String str) {
		if (str == null) {
			return null;
		}
		
		if (str.startsWith("\"") && str.endsWith("\"")) {
			return str.substring(1, str.length()-1);
		}
		
		return str.replaceAll("\"", "\\\\\"");
	}
	public static String escapeDoublequote(Object obj) {
		return escapeDoublequote(obj.toString());
	}
	public static String doublequote(String str) {
		if (str == null) {
			return null;
		}
		
		return quote(str);
		
//		if (str.startsWith("\"") && str.endsWith("\"")) {
//			return str;
//		}
//		return "\"" + str.replaceAll("\"", "\\\\\"").replaceAll("\n", "").replaceAll("\r", "") + "\"";
	}
	public static String doublequote(Object obj) {
		return doublequote(obj.toString());
	}
	
	// modified From Jettison
	 public static String quote(String string) {
		 int length = string.length();
         if (string == null || length == 0) {
             return "\"\"";
         }
         
		if (length > 1 && (string.startsWith("\"") && string.endsWith("\""))) {
			return string;
		}

         char         c = 0;
         int          i;
         int          len = string.length();
         StringBuilder sb = new StringBuilder(len + 10);
         String       t;

         sb.append('"');
         for (i = 0; i < len; i += 1) {
             c = string.charAt(i);
             switch (c) {
             case '\\':
             case '"':
                 sb.append('\\');
                 sb.append(c);
                 break;
//             case '/':
//                 if (b == '<') {
//                     sb.append('\\');
//                 }
//                 sb.append(c);
//                 break;
             case '\b':
                 sb.append("\\b");
                 break;
             case '\t':
                 sb.append("\\t");
                 break;
             case '\n':
                 sb.append("\\\\n");
                 break;
             case '\f':
                 sb.append("\\f");
                 break;
             case '\r':
                sb.append("\\\\r");
                break;
             case ',':
                 sb.append(",");
                 break;
             default:
                 if (c < ' ') {
                     t = "000" + Integer.toHexString(c);
                     sb.append("\\u" + t.substring(t.length() - 4));
                 } else {
                     sb.append(c);
                 }
             }
         }
         sb.append('"');
         return sb.toString();
     }
	 
	 
	 public static String unquote(Object str) {
		if (isEmpty(str)) {
			return null;
		}
		return unquote(str.toString());
	 }
	 
	 
	public static String unquote(String str) {
		if (isEmpty(str)) {
			return null;
		}
		int length = str.length();
		if (length > 1) {
			if (str.startsWith("\"") && str.endsWith("\"")) {
				str = str.substring(1, length-1);
			} else if (str.startsWith("'") && str.endsWith("'")) {
				str = str.substring(1, length-1);
			}
		}

		String [][] filters = new String[][] { 
                {"\\\\n", "\n"},
                {"\\\\r", "\r"},
                {"\\\"", "\""},
                {"\\'", "'"},
                {"\\\\/", "/"}
                };
		
		for (String [] filter: filters) {
			str = str.replaceAll(filter[0], filter[1]);
		}
		
		return str;
	}
		
	public static String unquote2(String str) {
		if (str == null || str.equals("null")) {
			return null;
		}
		StreamTokenizer parser = new StreamTokenizer(new StringReader(str));
		String result;
		try {
			parser.nextToken();
			if (parser.ttype == '"') {
				result = parser.sval;
			} else {
				result = unquote(str);
			}
		} catch (IOException e) {
			result = unquote(str);
		}
		
		return result;
	}
	

	public static String formatName(String name, FIELD_NAMING format) {
		if (name == null) {
			return name;
		}
		
		switch(format) {
		case FIELD: // someField_name
			return name;

		case CAMELCASE: // someFieldName
			return camelCase(name);

		case UPPER_CAMELCASE: // SomeFieldName
			return capitalize(camelCase(name));

		case UNDERSCORE_CAMELCASE: // some_Field_Name
			return camelCase2Underscore(camelCase(name));

		case UNDERSCORE_UPPER_CAMELCASE: // Some_Field_Name
			return capitalize(camelCase2Underscore(camelCase(name)));

		case UNDERSCORE_LOWER: // some_field_name
			return camelCase2Underscore(camelCase(name)).toLowerCase();

		case UNDERSCORE_UPPER: // SOME_FIELD_NAME
			return camelCase2Underscore(camelCase(name)).toUpperCase();

		case SPACE_CAMELCASE: // some Field Name
			return camelCase2Space(camelCase(name));

		case SPACE_UPPER_CAMELCASE: // Some Field Name
			return capitalize(camelCase2Space(camelCase(name)));

		case SPACE_LOWER: // some field name
			return camelCase2Space(camelCase(name)).toLowerCase();

		case SPACE_UPPER: // SOME FIELD NAME
			return camelCase2Space(camelCase(name)).toUpperCase();

		case DASH_CAMELCASE: // some-Field-Name
			return camelCase2Dash(camelCase(name));

		case DASH_UPPER_CAMELCASE: // Some-Field-Name
			return capitalize(camelCase2Dash(camelCase(name)));

		case DASH_LOWER: // some-field-name
			return camelCase2Dash(camelCase(name)).toLowerCase();

		case DASH_UPPER: // SOME-FIELD-NAME
			return camelCase2Dash(camelCase(name)).toUpperCase();
		}

		return name;
	}
	
	
	public static boolean isNumeric(String str) {
		try {
			double d = Double.parseDouble(str);
		} catch (NumberFormatException nfe) {
			//nfe.printStackTrace();
			return str.matches("-?\\d+(\\.\\d+)?(E-?\\d+)?");
		}
		return true;
	}
}
