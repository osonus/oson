package ca.oson.json.path;

import java.util.List;
import java.util.Stack;

public abstract class PathProcessor {
	protected String xpath;
	
	public static String cleanUpParenthesis(String raw) {
		int i = 0;
		Stack<Character> stack = new Stack<>();
		while (i < raw.length()) {
			char c = raw.charAt(i);
			if (c == '(') {
				stack.push(c);
				
			} else if (c == ')') {
				if (stack.isEmpty()) {
					// 
				} else {
					stack.pop();
				}
			}
			
			i++;
		}
		
		int size = stack.size();
		if (size == 0) {
			return raw;
		}
		
		String parenthesis = stack.toString();
		if (parenthesis.startsWith("(")) {
			i = 0;
			while (size > 0) {
				i = raw.indexOf("(", i);
				if (i != -1) {
					raw = raw.substring(i+1);
				} else {
					i = raw.indexOf(")", i);
					if (i != -1) {
						raw = raw.substring(i+1);
					} else {
						throw new RuntimeException("Unbalanced parenthsis");
					}
				}
				size--;
			}

		} else {
			i = raw.length() - 1;
			while (size > 0) {
				i = raw.lastIndexOf(")", i);
				if (i != -1) {
					raw = raw.substring(0, i);
				} else {
					i = raw.indexOf("(", i);
					if (i != -1) {
						raw = raw.substring(0, i);
					} else {
						throw new RuntimeException("Unbalanced parenthsis");
					}
				}
				size--;
			}
		}
		
		return raw.trim();
	}
	
	
	public static boolean isXpath(String xpath) {
		if (xpath.startsWith("/")) {
			return true;
		}

		if (xpath.startsWith("$.")) {
			return false;
		}
		
		if (xpath.contains("::")) {
			return true;
		}
		
		if (xpath.contains("@.")) {
			return false;
		}
		
		if (xpath.contains("/") || xpath.contains("last()")) {
			return true;
		}
		
		return false;
	}
	
	public PathProcessor(String xpath) {
		this.xpath = xpath.trim();
	}
	
	public abstract List<Step> process();
	
	public List<Step> process(String xpath) {
		this.xpath = xpath;
		return process();
	}
	
}
