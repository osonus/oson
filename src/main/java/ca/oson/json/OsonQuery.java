package ca.oson.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import ca.oson.json.Oson.FIELD_NAMING;
import ca.oson.json.util.StringUtil;

public class OsonQuery {
	private static FIELD_NAMING naming = FIELD_NAMING.UNDERSCORE_LOWER;

	public static String getAttribute(String source, String attr) {
		Oson oson = new Oson();

		source = oson.setFieldNaming(naming).serialize(oson.getListMapObject(source));
		
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
