package ca.oson.json.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import ca.oson.json.util.NumberUtil;
import ca.oson.json.util.StringUtil;

public abstract class PathProcessor {
	protected String xpath;
	
	
	protected Field getField(String raw) {
		return getField(raw, raw);
	}
	
	protected abstract Field getField(String node, String raw);

	protected Operand processOperand(String raw) {
		Operand operand = new Operand(raw);

		String rawLc = raw.toLowerCase();
		
		// processing func
		int idx, idx2;
		String name;
		for (Func func: Func.values()) {
			name = func.toString();
			if (containsFunction(rawLc, name)) {
				operand.func = func;
				idx = rawLc.indexOf(name);
				idx2 = rawLc.indexOf("(", idx);
				String[] params = null;
				if (idx2 > idx) {
					params = getParameters(raw.substring(idx2 + 1));
				}
				
				switch (func) {
				case STRING_LENGTH:
				case NUMBER:
				case COUNT:
				case SUM:
				case ROUND:
					if (params != null && params.length > 0) {
						operand.field = getField(params[0], raw);
					}
					break;

				case STARTS_WITH:
				case CONTAINS:
					if (params != null && params.length > 1) {
						operand.field = getField(params[0], raw);
						operand.value = params[1];
					}
					
					break;

				case TRANSLATE:
					if (params != null && params.length > 2) {
						operand.field = getField(params[0], raw);

						operand.value = Arrays.copyOfRange(params, 1, 3);
					}
					
					break;
				}
				
				
				break;
			}
		}

		// operand.func == null && 
		if (operand.field == null) {
			operand.field = getField(raw);
		}
		
		return operand;
	}
	
	
	
	public static String getNextPar(String raw) {
		int i = 0;
		String par = raw;
		Stack<Character> stack = new Stack<>();
		while (i < raw.length()) {
			char c = raw.charAt(i);
			if (c == '(') {
				stack.push(c);
				
			} else if (c == ')') {
				if (stack.isEmpty()) {
					raw = raw.substring(0, i);
					break;
					
				} else {
					stack.pop();
				}
			}
			
			i++;
		}

		return raw; // StringUtil.unwrap(raw, "(", ")");
	}
	
	
	public static String getPrevPar(String raw) {
		int i = raw.length() - 1;
		Stack<Character> stack = new Stack<>();
		String par = raw;
		while (i > 0) {
			char c = raw.charAt(i);
			if (c == ')') {
				stack.push(c);
				
			} else if (c == '(') {
				if (stack.isEmpty()) {
					par = raw.substring(i);
					break;
					
				} else {
					stack.pop();
				}
			}
			
			i--;
		}

		return StringUtil.unwrap(raw, "(", ")");
	}
	
	
	protected Operand processLeftOperand(String raw) {
		String par = getPrevPar(raw);

		return processOperand(par);
	}
	
	protected Operand processRightOperand(String raw) {
		String par = getNextPar(raw);

		return processOperand(par);
	}
	
	
	protected boolean containsMathOperator(String raw) {
		return containsMathOperator(raw, 0);
	}
	
	protected boolean containsMathOperator(String raw, int startIndx) {
		String rawLc = raw.toLowerCase();

		MathOperator[] operators = MathOperator.values();
		int len = operators.length;
		for (int i = startIndx; i < len; i++) {
			MathOperator operator = operators[i];
			for (String op: operator.ops) {
				if (rawLc.contains(op)) {
					boolean isOpText = StringUtil.isAlpha(op);
					if (isOpText) {
						int idx = rawLc.indexOf(" " + op + " ");
						if (idx != -1) {
							return true;
						}
						
					} else {
						return true;
					}
					
				}
			}
		}
		
		return false;
	}
	
	

	// to re-write, using different approach
	protected Operand processOperand(String raw, boolean hasMathOp) {
		raw = StringUtil.unwrap(raw, "(", ")");

		if (!hasMathOp) {
			return processOperand(raw);
		}

		List<Object> ops = new ArrayList<>();

		// first round: parenthesis
		int idx = raw.indexOf("(");
		
		while (idx != -1) {
			String nextPar = getNextPar(raw.substring(idx + 1));
			
			if (containsMathOperator(nextPar)) {
				
				String prev = raw.substring(0, idx);
				
				ops.add(prev);

				Operand currentOperand = processOperand(nextPar, true);
				
				ops.add(currentOperand);
				
				raw = raw.substring(idx + nextPar.length() + 2);
				
				idx = raw.indexOf("(");
				
			} else {
				idx = raw.indexOf("(", idx + 1);
			}
		}

		
		raw = raw.trim();

		if (raw.length() > 0) {
			ops.add(raw);
		}
		
		if (ops.size() == 0) {
			return null;
		}
		
		
		return processOperand(ops, 0);
	}
	
	protected Operand processOperand(List<Object> ops, int startIndex) {
		Operand operand = null;
		
		// handling math operations
		MathOperator[] operators = MathOperator.values();
		
		for (int i = startIndex; i < operators.length; i++) {
			MathOperator operator = operators[i];

			String nextOperator = null;
			for (String op: operator.ops) {
				boolean isOpText = StringUtil.isAlpha(op);

				for (Object o: ops) {
					if (String.class.isInstance(o)) {
						String text = (String)o;
						text = text.toLowerCase();

						if (text.contains(op)) {
							if (isOpText) {
								if (text.startsWith(op + " ") || text.endsWith(" " + op) ||
										text.contains(" " + op + " ") || text.equals(op)
										) {
									
									nextOperator = op;
									
									break;
								}
								
							} else {
								nextOperator = op;
								break;
							}
							
						}
					}
				}
				
				if (nextOperator != null) {
					break;
				}
			}

			
			if (nextOperator != null) {
				operand = new Operand(operator);
				operand.children = new ArrayList<>();

				List<Object> previous = null; // new ArrayList<>();
				for (Object o: ops) {
					
					if (String.class.isInstance(o)) {
						String[] parts = StringUtil.toArray((String)o, nextOperator);

						if (parts.length > 1) {
							int j = 0;
							if (previous != null) {
								if (!StringUtil.isEmpty(parts[0])) {
									previous.add(parts[0]);
								}
								Operand opr = processOperand(previous, i);
								if (opr != null) {
									operand.children.add(opr);
								}
								
								j++;
							}
							
							for (; j < parts.length - 1; j++) {
								String ob = parts[j];
							
								if (!StringUtil.isEmpty(ob)) {
									boolean hasMOp = false;
									if (startIndex + 1 < operators.length) {
										hasMOp = containsMathOperator(ob);
									}
									
									Operand opr = null;
									
									if (hasMOp) {
										previous = new ArrayList<>();
										previous.add(ob);
										opr = processOperand(previous, i+1);
										previous = null;
									} else {
										opr = processOperand(ob);
									}
									
									if (opr != null) {
										operand.children.add(opr);
									}
								}
							}
							
							previous = new ArrayList<>();
							previous.add(parts[j]);
							
						} else if (!StringUtil.isEmpty(o)) {
							if (previous == null) {
								previous = new ArrayList<>();
							}
							previous.add(o);
							
						}
						
					} else if (Operand.class.isInstance(o)) {
						Operand opr = (Operand)o;
						
						if (operator == opr.op && operator != MathOperator.SUBTRACTION && operator != MathOperator.DIVISION) {
							operand.children.addAll(opr.children);
						} else {
							if (previous == null) {
								previous = new ArrayList<>();
							}
							previous.add(o);
						}
						
						
					} else {
						if (previous == null) {
							previous = new ArrayList<>();
						}
						previous.add(o);
					}
				}
				
				if (previous != null) {
					Operand opr = null;
					
					if (previous.size() == 1) {
						Object o = previous.get(0);
						
						if (String.class.isInstance(o)) {
							String ob = (String)o;
							
							if (!StringUtil.isEmpty(ob)) {
								boolean hasMOp = false;
								if (startIndex + 1 < operators.length) {
									hasMOp = containsMathOperator(ob);
								}

								if (hasMOp) {
									opr = processOperand(previous, i+1);
								} else {
									opr = processOperand(ob);
								}
							}
							
						} else {
							opr = processOperand(previous, i+1);
						}
						
					} else{
						opr = processOperand(previous, i+1);
					}
					
					if (opr != null) {
						operand.children.add(opr);
					}
				}
				
				return operand;
			}
		}
		
		for (Object o: ops) {
			if (Operand.class.isInstance(o)) {
				return (Operand)o;
			}
		}

		return operand;
	}	

	
	public static String[] getParameters(String raw) {
		return getParameters(raw, ',');
	}
	
	public static String[] getParameters(String raw, char character) {
		int i = 0, last = 0;
		Stack<Character> stack = new Stack<>();
		List<String> list = new ArrayList<>();
		while (i < raw.length()) {
			char c = raw.charAt(i);
			if (c == '(') {
				stack.push(c);
				
			} else if (c == ')') {
				if (stack.isEmpty()) {
					String part = raw.substring(last, i);
					part = StringUtil.unwrap(part, "\"");
					part = StringUtil.unwrap(part, "'");
					part = StringUtil.unwrap(part, "(", ")");
					if (part.length() > 0 && StringUtil.isParenthesisBalanced(part)) {
						list.add(part);
					}
					break;
					
				} else {
					stack.pop();
				}
				
			} else if (c == character) {
				String part = raw.substring(last, i).trim();
				boolean balanced = true;
				if (part.startsWith("\"")) {
					if (!part.endsWith("\"")) {
						balanced = false;
					} else {
						part = StringUtil.unwrap(part, "\"");
					}
				}
				if (balanced && part.startsWith("'")) {
					if (!part.endsWith("'")) {
						balanced = false;
					} else {
						part = StringUtil.unwrap(part, "'");
					}
				}
				if (balanced) {
					part = StringUtil.unwrap(part, "(", ")");
					if (part.length() > 0 && StringUtil.isParenthesisBalanced(part)) {
						list.add(part);
					}
					last = i + 1;
				}
			}
			
			i++;
		}
		
//		String str = raw.substring(0, i);
//		return str.split(",");
		
		return list.toArray(new String[0]);
	}
	
	public static boolean containsFunction(String str, String funcName) {
		int idx = str.indexOf(funcName);
		if (idx != -1) {
			int i = str.indexOf("(", idx);
			if (i > idx && str.substring(idx + funcName.length(), i).trim().length() == 0) {
				return true;
			}
		}
		
		return false;
	}
	
	
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

		raw = raw.replaceAll("\\?\\s*\\(", "(");

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
	
	
	protected abstract Step processStep(String raw, String node);
	
	
	protected Step processStep(String raw) {
		// String name = "";
		
		if (raw.startsWith("(") && raw.endsWith(")") && raw.contains("|")) {
			raw = "[" + raw.substring(1, raw.length() - 1) + "]";
		}
		
		int i = raw.indexOf("[");
		
		Step step = null;
		if (i > -1) {
			step = processStep(raw, raw.substring(0, i).trim());
			
		} else {
			step = processStep(raw, raw);
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
