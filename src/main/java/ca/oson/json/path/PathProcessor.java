package ca.oson.json.path;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import ca.oson.json.util.NumberUtil;
import ca.oson.json.util.StringUtil;

public abstract class PathProcessor {
	protected String xpath;
	
	public static Number cleanUpNumber(String raw, Class <? extends Number> type) {
		raw = cleanUpParenthesis(raw);
		raw = raw.replaceAll(" ", "");
		
		return NumberUtil.getNumber(raw, type);
	}
	
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
			return StringUtil.unwrap(raw, "[", "]");
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
		
		return StringUtil.unwrap(raw, "[", "]");
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
	
	
	
	protected abstract Filter processPredicate(String raw);
	
	
	 protected List<Filter> processPredicates(String raw, String andOr) {
		 List<Filter> predicates = new ArrayList<>();
		 
		 int idx = 0;
		 int idx2 = raw.indexOf(andOr, idx);
		 String condition = null;
		 while (idx2 != -1) {
			 condition = raw.substring(idx, idx2);
			 
			 predicates.add(processPredicate(condition));
		 
			 idx = idx2 + 2;
			 
			 idx2 = raw.indexOf(andOr, idx);
		 }
		 
		 condition = raw.substring(idx);
		 predicates.add(processPredicate(condition));

		 return predicates;
	 }
	
	
	/*
	 * Major predicate processing
	 */
	protected Filter processFilter(String raw) {
		Filter filter = new Filter(raw);

		raw = raw.replaceAll("\\&\\&", " &&  ");
		raw = raw.replaceAll("\\|\\|", " || ");
		
		String rawLc = raw.toLowerCase();
		
		rawLc = rawLc.replaceAll(" and ", " &&  ");
		rawLc = rawLc.replaceAll(" or ", " || ");		
		

		int idx = rawLc.indexOf("&&");
		int idx2 = rawLc.indexOf("||");
		
		List<Integer> andList = new ArrayList<>();
		while (idx != -1) {
			andList.add(idx);
			raw = raw.substring(0, idx) + "&&  " + raw.substring(idx + 4);
			idx = rawLc.indexOf("&&", idx + 4);
		}
		
		List<Integer> orList = new ArrayList<>();
		while (idx2 != -1) {
			orList.add(idx2);
			raw = raw.substring(0, idx2) + "|| " + raw.substring(idx2 + 3);
			idx2 = rawLc.indexOf("||", idx2 + 3);
		}
		
		if (andList.size() > 0) {
			if (orList.size() > 0) {
				int length = andList.size() + orList.size();
				int[] andors = new int[length];
				
				int i = 0, j = 0, k = 0, a, b;
				while (i < andList.size() && j < orList.size()) {
					a = andList.get(i);
					b = orList.get(j);
					if (a < b) {
						andors[k] = a;
						i++;
					} else {
						andors[k] = b;
						j++;
					}
					k++;
				}
				
				while (i < andList.size()) {
					andors[k++] = andList.get(i++);
				}
				
				while (j < orList.size()) {
					andors[k++] = orList.get(j++);
				}
				
				// now counting parenthesis
				idx = -1;
				Map<Integer, Integer> levelMap = new LinkedHashMap<>();
				String[] raws = new String[length + 1];
				int last = 0;
				i = 0;
				k = 0;
				Stack<Character> stack = new Stack<>();
				while (i < raw.length()) {
					char c = raw.charAt(i);
					if (c == '(') {
						stack.push(c);
						
					} else if (c == ')') {
						if (stack.isEmpty()) {
							throw new RuntimeException("Unbalanced parenthsis");
						} else {
							stack.pop();
						}
					}
					
					i++;
					if (k < andors.length && i == andors[k]) {
						idx2 = stack.size();
						levelMap.put(k, idx2);
						raws[k] = raw.substring(last, i);
						last = i + 2;
						k++;

						if (idx2 > idx) {
							idx = idx2;
						}
					}
				}
				idx2 = stack.size();
				levelMap.put(k, new Integer(idx2));
				raws[k] = raw.substring(last);

				Filter[] filters = new Filter[length + 1];
				Filter currentFilter = null;
				// now combining conditions
				while (idx >= 0) {
					for (k = 0; k < length; k++) {
						
						if (levelMap.get(k) == idx) {
							idx2 = andors[k];
							SELECTOR selector = null;
							if (andList.contains(idx2)) {
								selector = SELECTOR.AND;
							} else if (orList.contains(idx2)) {
								selector = SELECTOR.OR;
							}
							
							// left side
							Filter leftFilter = filters[k];
							Filter rightFilter = filters[k+1];
							String leftRaw = raws[k];
							String rightRaw = raws[k+1];
							
							if (leftFilter == null) {
								leftFilter = processPredicate(leftRaw);
								filters[k] = leftFilter;
							} else {
								while (leftFilter.parent != null) {
									leftFilter = leftFilter.parent;
								}
							}
							
							if (rightFilter == null) {
								rightFilter = processPredicate(rightRaw);
								filters[k+1] = rightFilter;
							} else {
								while (rightFilter.parent != null) {
									rightFilter = rightFilter.parent;
								}
							}

							if (leftFilter.selector != null && leftFilter.selector == selector && leftFilter.predicates != null) {
								leftFilter.predicates.add(rightFilter);
								currentFilter = leftFilter;
								rightFilter.parent = leftFilter;
								
							} else if (rightFilter.selector != null && rightFilter.selector == selector && rightFilter.predicates != null) {
								rightFilter.predicates.add(leftFilter);
								currentFilter = rightFilter;
								leftFilter.parent = currentFilter;
								
							} else {
								List<Filter> predicates = new ArrayList<>();
								predicates.add(leftFilter);
								predicates.add(rightFilter);
								
								currentFilter = new Filter(selector);
								currentFilter.predicates = predicates;
								rightFilter.parent = currentFilter;
								leftFilter.parent = currentFilter;
							}
						}
					}

					idx--;
				}
				
				filter.selector = currentFilter.selector;
				filter.predicates = currentFilter.predicates;
				
				
			} else {
				filter.selector = SELECTOR.AND;
				filter.predicates = processPredicates(raw, "&&");
			}

			
		} else if (orList.size() > 0) {
			filter.selector = SELECTOR.OR;
			filter.predicates = processPredicates(raw, "||");
			
		} else {
			List<Filter> predicates = new ArrayList<>();
			predicates.add(processPredicate(raw));
			filter.predicates = predicates;
		}

		return filter;
	}
	
	
	protected Step processStep(String raw) {
		// String name = "";
		
		if (raw.startsWith("(") && raw.endsWith(")") && raw.contains("|")) {
			raw = "[" + raw.substring(1, raw.length() - 1) + "]";
		}
		
		int i = raw.indexOf("[");
		
		Step step = null;
		if (i > -1) {
			step = new Step(raw, raw.substring(0, i).trim());
			
		} else {
			step = new Step(raw, raw);
			raw = "";
		}
		
		List<Filter> filters = new ArrayList<>();
		
		int idx = i;
		while (i > -1) {
			i = raw.indexOf("]", i);
			
			if (i > -1) {
				String part = raw.substring(idx, i+1).trim();
	
				if (StringUtil.isParenthesisBalanced(part)) {
					Filter filter = processFilter(part);
					filters.add(filter);
					raw = raw.substring(i+1).trim();
					
					i = raw.indexOf("[", i+1);
					idx = i;
	
				} else {
					i++;
				}
			}
		}
		
		if (raw.length() > 0) {
			Filter filter = processFilter(raw);
			filters.add(filter);
		}

		if (filters.size() > 0) {
			step.filters = filters;
		}
		
		return step;
	}
	

	public abstract List<Step> process();
	
	public List<Step> process(String xpath) {
		this.xpath = xpath;
		return process();
	}
}
