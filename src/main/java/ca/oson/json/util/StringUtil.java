package ca.oson.json.util;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ca.oson.json.JSONObject;

import ca.oson.json.Oson.FIELD_NAMING;

public class StringUtil {
	/*
	 * The white space character for Json indentation
	 */
	public static char SPACE = ' ';
	
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
		if (obj == null || obj.equals(JSONObject.NULL) || obj.toString().equalsIgnoreCase("null")) return true;
		
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
		return (str.length() == 0 || str.equals("\"\"") || str.equals("''") || obj.getClass().getName().equals("org.json.JSONObject$Null"));
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
	
	public static String getPrettySpace(int indentation) {
		if (indentation > 0) {
			return String.valueOf(StringUtil.SPACE);
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
	
	public static String repeatString(String str, int repeat) {
		if (repeat < 1) {
			return "";
		}
		
		int length = str.length();
		
		StringBuffer sb = new StringBuffer(length * repeat);
		while (repeat > 0) {
			repeat--;
			sb.append(str);
		}
		
		return sb.toString();
	}
	
	private static boolean sameCase(char first, char last) {
		if (first < 91 && last < 91) {
			return true;
		}
		if (first > 96 && last > 96) {
			return true;
		}
		return false;
	}

	public static String underscore2CamelCase(String name) {
		int idx = name.indexOf('_');
		if (idx == -1) {
			return name;
		}
		
		if (!name.matches(".*[a-z].*")) {
			name = name.toLowerCase();
		}

		StringBuilder sb = new StringBuilder();
		// no empty string
		String[] parts = Arrays.asList(name.split("_")).stream().filter(str -> !str.isEmpty()).collect(Collectors.toList()).toArray(new String[0]);

		for (int i = 0; i < parts.length; i++) {
			if (parts[i].length() > 1 && !parts[i].matches(".*[a-z].*")) {
				parts[i] = parts[i].toLowerCase();
			}
			
			sb.append(capitalize(parts[i]));
		}

		return sb.toString();
	}


	private static String camelCase2Delimiter(String name, char delimiter) {
		if (name == null || !name.matches(".*[A-Z].*") || !name.matches(".*[a-z].*"))
			return name;

		String regex = "([a-zA-Z])([A-Z])";
		String replacement = "$1" + delimiter + "$2";

		return name.replaceAll(regex, replacement).replaceAll(regex, replacement);
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
		if (name == null || !name.matches(".*[_ -].*")) {
			return name;
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
	
	public static boolean parenthesized(String str) {
		if (isArrayOrList(str)) {
			return true;
		}
		if (isObjectOrMap(str)) {
			return true;
		}
		
		return false;
	}
	
	public static boolean isArrayOrList(String str) {
		if (str == null) {
			return false;
		}
		
		if (str.startsWith("[") && str.endsWith("]")) {
			return true;
		}

		return false;
	}
	
	public static boolean isObjectOrMap(String str) {
		if (str == null) {
			return false;
		}
		if (str.startsWith("{") && str.endsWith("}")) {
			return true;
		}
		
		return false;
	}
	
	public static boolean quoted(String str) {
		if (str.startsWith("\"") && str.endsWith("\"")) {
			return true;
		}
		if (str.startsWith("'") && str.endsWith("'")) {
			return true;
		}
		
		return false;
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
	public static String doublequote(String str, boolean escapeHtml) {
		if (str == null) {
			return null;
		}
		
		return quote(str, escapeHtml);
	}
	public static String doublequote(Object obj, boolean escapeHtml) {
		return doublequote(obj.toString(), escapeHtml);
	}
	public static String doublequote(Object obj) {
		return doublequote(obj, true);
	}
	
	// modified From Jettison
	 public static String quote(String string, boolean escapeHtml) {
		 int length = string.length();
         if (string == null || length == 0) {
             return "\"\"";
         }
         
		if (length > 1) {
			if (quoted(string)) {
				return string;
			}
			if (parenthesized(string)) {
				return string;
			}
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
             case '<':
            	 if (escapeHtml) {
            		 sb.append("\\u003c");
            	 } else {
            		 sb.append(c);
            	 }
                 break;
             case '>':
            	 if (escapeHtml) {
            		 sb.append("\\u003e");
            	 } else {
            		 sb.append(c);
            	 }
                 break;
             case '=':
            	 if (escapeHtml) {
            		 sb.append("\\u003d");
            	 } else {
            		 sb.append(c);
            	 }
                 break;
             case '&':
            	 if (escapeHtml) {
            		 sb.append("\\u0026");
            	 } else {
            		 sb.append(c);
            	 }
                 break;
             case '\'':
            	 if (escapeHtml) {
            		 sb.append("\\u0027");
            	 } else {
            		 sb.append(c);
            	 }
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
		 return unquote(str, true);
	 }
	 
	 
	 public static String unquote(Object str, boolean escapeHtml) {
		if (str == null) {
			return null;
		}
		return unquote(str.toString(), escapeHtml);
	 }
	 
	 
	public static String unquote(String str, boolean escapeHtml) {
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
		
		if (escapeHtml) {
			filters = new String[][] { 
	                {"\\u003c", "<"},
	                {"\\u003e", ">"},
	                {"\\u003d", "="},
	                {"\\u0026", "&"},
	                {"\\u0027", "'"}
	                };
			
			for (String [] filter: filters) {
				str = str.replaceAll(filter[0], filter[1]);
			}
		}
		
		str = unescapeJava(str);
		
		return str.trim();
	}

	/* 
	 * ssuukk
	 */
	public static String unescapeJava(String escaped) {
	    if(escaped.indexOf("\\u")==-1)
	        return escaped;

	    String processed="";

	    int position=escaped.indexOf("\\u");
	    while(position!=-1) {
	        if(position!=0)
	            processed+=escaped.substring(0,position);
	        String token=escaped.substring(position+2,position+6);
	        escaped=escaped.substring(position+6);
	        processed+=(char)Integer.parseInt(token,16);
	        position=escaped.indexOf("\\u");
	    }
	    processed+=escaped;

	    return processed;
	}
		
	public static String unquote2(String str, boolean escapeHtml) {
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
				result = unquote(str, escapeHtml);
			}
		} catch (IOException e) {
			result = unquote(str, escapeHtml);
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
