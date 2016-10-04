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
					stack.push(c);
					
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
		
		i = 0;
		for (char c: stack) {
			if (c == '(') {
				i = raw.indexOf(c, i) + 1;
				raw = raw.substring(i);
			} else {
				if (i == 0) {
					i = raw.length();
				}
				i = raw.lastIndexOf(c, i);
				raw = raw.substring(0, i);
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
		
		if (xpath.contains("/") || xpath.contains("last()") || xpath.contains("@")) {
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
