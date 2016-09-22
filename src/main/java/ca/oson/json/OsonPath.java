package ca.oson.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import ca.oson.json.Oson.FIELD_NAMING;
import ca.oson.json.util.StringUtil;

public class OsonPath {
	private static FIELD_NAMING naming = FIELD_NAMING.CAMELCASE;
	
	/*
	 * Search for attribute or key's values in the Json source. The search follows Json document structure
	 * from the root to children through attr, with dot notation.
	 * 
	 * @param source       the Json source document, can be object or list of objects
	 * @param attr         the attribute for the values to search. It can be one single attribut name,
	 *                     or a list of attributes, linked by dot ".", such as "enclosing_attr.enclosed_attr".
	 * @param strict       if strict is true, then the search path will match attr exactly, once the search starts
	 * @return             a Json item, or list of items found, or null if no matching attributes and values.
	 * 
	 */
	public static String search(String source, String attr, boolean strict) {
		Oson oson = new Oson()
				.setFieldNaming(naming)
				.includeClassTypeInJson(false);
		Object obj = oson.deserialize(source);
		
		String[] attrs = attr.split("\\.");
		
		Set found = find(obj, attrs, strict, false);
		
		if (found == null || found.size() == 0) {
			return null;
		}
		
		if (found.size() == 1) {
			return oson.serialize(found.iterator().next()); //.toArray()[0]
		}
		
		return oson.serialize(found);
	}
	
	/*
	 * Search for attribute or key's values in the Json source. The search follows Json document structure
	 * from the root to children through attr, with dot notation.
	 * 
	 * @param source       the Json source document, can be object or list of objects
	 * @param attr         the attribute for the values to search. It can be one single attribut name,
	 *                     or a list of attributes, linked by dot ".", such as "enclosing_attr.enclosed_attr".
	 * @return             a Json item, or list of items found, or null if no matching attributes and values.
	 * 
	 */
	public static String search(String source, String attr) {
		return search(source, attr, false);
	}
	
	
	private static Set find(Object obj, String[] attrs, boolean strict, boolean started) {
		if (obj == null || attrs == null) {
			return null;
		}
		
		int len = attrs.length;
		if (len < 1) {
			return null;
		}
		
		String[] attrs2 = null;
		if (len > 1) {
			attrs2 = Arrays.copyOfRange(attrs, 1, len);
		}
		
		String attr = attrs[0];
		String attr2 = StringUtil.formatName(attr, naming);
		
		Set founds = new LinkedHashSet();
		
		if (Map.class.isInstance(obj)) {
			Map<String, Object> map = (Map)obj;
			
			boolean existed = (map.containsKey(attr) || map.containsKey(attr2));
			
			if (existed) {
				started = true;
			}

			for (String key : map.keySet()) {
				Object value = map.get(key);
				
				if (!StringUtil.isEmpty(value)) {
					if (key.equals(attr) || key.equals(attr2)) {
						started = true;
						if (len == 1) {
							founds.add(value);
						} else {
							Set found = find(value, attrs2, strict, started);
							if (found != null) {
								founds.addAll(found);
							}
						}
						
					} else if (!started || !strict) {
						Set found = find(value, attrs, strict, started);
						if (found != null) {
							founds.addAll(found);
						}
					}
				}
			}
			
		} else if (List.class.isInstance(obj)) {
			for (Object value : (List)obj) {
				Set found = find(value, attrs, strict, started);
				if (found != null) {
					founds.addAll(found);
				}
			}
			
		}
		
		if (founds.size() > 0) {
			return founds;
		}
		
		return null;
	}
	

	/*
	 * Query for attribute or key's values in the Json source. The query is a pure text one, as it searches
	 * values using attribute and key names, with dot notation. So this search does not necessary follow
	 * Json document structures precisely, and can cross Json object boundaries.
	 * 
	 * @param source       the Json source document, can be object or list of objects
	 * @param attr         the attribute for the values to search. It can be one single attribut name,
	 *                     or a list of attributes, linked by dot ".", such as "enclosing_attr.enclosed_attr".
	 * @return             a Json item, or list of items found, or null if no matching attributes and values.
	 * 
	 */
	public static String query(String source, String attr) {
		Oson oson = new Oson().includeClassTypeInJson(false).setFieldNaming(naming);

		source = oson.serialize(oson.deserialize(source));
		
		String[] attrs = attr.split("\\.");
		
		int size = attrs.length;
		int max = 10;
		
		int[][] array = new int[size][max];

		int len = source.length();

		int startIdx = 0, idx, idx2;
		for (int i = 0; i < size; i++) {
			String att = attrs[i];
			
			String att1 = StringUtil.doublequote(att);
			String att2 = StringUtil.doublequote(StringUtil.formatName(att, naming));

			int start = startIdx;
			int j = 0;
			while (start < len) {
				idx = source.indexOf(att1, start);
				idx2 = source.indexOf(att2, start);
				
				if (idx != -1) {
					array[i][j++] = idx;
					start = idx;
					
				} else if (idx2 != -1) {
					array[i][j++] = idx2;
					start = idx2;
				} else {
					break;
				}
				start += att1.length();
			}

			startIdx = array[i][0] + 1;
		}
		
		for (int i = 0; i < 10; i++) {
			int m = array[size - 1][i];
			if (m > 0) {
				for (int j = 0; j < size - 1; j++) {
					boolean hasValue = false;
					for (int k = 0; k < 10; k++) {
						if (array[j][k] < m) {
							if (array[j][k] > 0) {
								hasValue = true;
								array[j][k] = 0;
							}
						} else {
							break;
						}
					}
					
					if (!hasValue) {
						array[size - 1][i] = 0;
					}
				}
			} else {
				break;
			}

		}
		
		List<String> results = new ArrayList<>();
		
		for (int i = 0; i < 10; i++) {
			int m = array[size - 1][i];
			if (m > 0) {
				String found = find(source, m);
				
				if (found != null && !found.equals("null")) {
					results.add(found);
				}
			}
		}
		size = results.size();
		if (size == 0) {
			return null;
		} else if (size == 1) {
			return results.get(0);
			
		} else {
			return oson.serialize(results);
			
		}
	}
	
	
	private static String find(String source, int startIdx) {
		int idx;
		startIdx = source.indexOf(":", startIdx);
		if (startIdx == -1) {
			return null;
		}
		startIdx++;
		
		int len = source.length();
		
		Stack<Character> stack = new Stack<>();
		boolean done = false;
		for (idx = startIdx; idx < len; idx++) {
			char c = source.charAt(idx);
			switch (c) {
			case '}':
				if (stack.isEmpty()) {
					done = true;

				} else {

					char l = stack.peek();
					if (l == '{') {
						idx++;
						stack.pop();
						
						if (stack.isEmpty()) {
							done = true;
							break;
						}
						
					} else {
						// unbalanced
						return null;
					}
					
				}
				break;

			case ']':
				if (stack.isEmpty()) {
					done = true;

				} else {

					char l = stack.peek();
					if (l == '[') {
						idx++;
						stack.pop();
						
						if (stack.isEmpty()) {
							done = true;
							break;
						}
						
					} else {
						// unbalanced
						return null;
					}
					
				}
				break;
				
			case '{':
			case '[':
				stack.push(c);
			}
			
			if (done) {
				break;
			}
		}
		
		source = source.substring(startIdx, idx).trim();
		
		if (StringUtil.parenthesized(source)) {
			return source;
		}
		
		idx = source.indexOf(":");
		
		if (idx != -1) {
			source = source.substring(0, idx).trim();
			idx = source.lastIndexOf(",\"");
			source = source.substring(0, idx).trim();
		}

		return source;
	}
	
	
}
