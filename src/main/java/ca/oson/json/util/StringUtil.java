package ca.oson.json.util;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.Optional;
import java.util.regex.Pattern;

import ca.oson.json.Oson.FIELD_NAMING;

public class StringUtil {
	public static final char SPACE = ' ';
	
	public static String removeComments(String content, Pattern[] patterns) {
		if (patterns == null || patterns.length == 0) {
			return content;
		}
		
	    Pattern p = Pattern.compile("<script[^>]*>(.*?)</script>",
	            Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	    
	    for (Pattern pattern: patterns) {
	    	content = pattern.matcher(content).replaceAll("");
	    }
	    
	    return content;
	}
	
	public static Pattern[] compilePatterns(String[] comments) {
		if (comments == null || comments.length == 0) {
			return null;
		}
		Pattern[] patterns = new Pattern[comments.length];
	    
	    for (int i = 0; i < comments.length; i++) {
	    	patterns[i] = Pattern.compile(comments[i],
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
		return (str.length() == 0 || str.equals("\"\"") || str.equals("''") || obj.getClass().getName().equals("org.json.JSONObject$Null"));
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
		if (!name.matches(".*[A-Z].*"))
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
		
		if (str.startsWith("\"") && str.endsWith("\"")) {
			return str;
		}
		return "\"" + str.replaceAll("\"", "\\\\\"").replaceAll("\n", "").replaceAll("\r", "") + "\"";
	}
	public static String doublequote(Object obj) {
		return doublequote(obj.toString());
	}
	
	public static String unquote(String str) {
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
				if (str.startsWith("\"") && str.endsWith("\"")) {
					str = str.substring(1, str.length()-1);
				}
				result = str.replaceAll("\\\\\"", "\"");
			}
		} catch (IOException e) {
			if (str.startsWith("\"") && str.endsWith("\"")) {
				str = str.substring(1, str.length()-1);
			}
			result = str.replaceAll("\\\\\"", "\"");
		}
		return result;
	}
	

	public static String formatName(String name, FIELD_NAMING format) {
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
