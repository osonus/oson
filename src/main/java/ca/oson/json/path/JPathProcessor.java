package ca.oson.json.path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import ca.oson.json.OsonPath;
import ca.oson.json.path.Index.RANGE;
import ca.oson.json.util.ArrayToJsonMap;
import ca.oson.json.util.NumberUtil;

public class JPathProcessor extends PathProcessor {
	public Map<String, List<String>> cached = new HashMap<>();

	public JPathProcessor(String xpath) {
		super(xpath);
		// TODO Auto-generated constructor stub
	}


	private Filter processPredicate(String raw) {
		raw = raw.trim();
		
		raw = cleanUpParenthesis(raw);
		
		Predicate predicate = new Predicate(raw);
		
		String rawLc = raw.toLowerCase();

		if (rawLc.startsWith("not ")) {
			predicate.not = true;
			raw = raw.substring(4).trim();
		} else if (rawLc.startsWith("!")) {
			predicate.not = true;
			raw = raw.substring(1).trim();
		}
		
		// parent, self, child
		if (raw.startsWith("@...")) {
			raw = raw.substring(4).trim();
			predicate.axis = Axis.DESCENDANT;
			
		} else if (raw.startsWith("@..")) {
			raw = raw.substring(3).trim();
			predicate.axis = Axis.CHILD;
			
		} else if (raw.startsWith("..@.")) {
			raw = raw.substring(4).trim();
			predicate.axis = Axis.ANCESTOR;
			
		} else if (raw.startsWith(".@.")) {
			raw = raw.substring(3).trim();
			predicate.axis = Axis.PARENT;
		}
		
		raw = raw.replaceAll("@.", "").trim();

		List<String> list = new ArrayList<>();

		if (raw.startsWith("+(") && raw.endsWith(")")) {
			list.addAll(OsonPath.oson.deserialize("[" + raw.substring(2, raw.length()-1) + "]", list.getClass()));
			predicate.value = list;
			predicate.selector = SELECTOR.EXPOSE;
			return predicate;

		} else if (raw.startsWith("-(") && raw.endsWith(")")) {
			list.addAll(OsonPath.oson.deserialize("[" + raw.substring(2, raw.length()-1) + "]", list.getClass()));
			predicate.value = list;
			predicate.selector = SELECTOR.IGNORE;
			return predicate;
		}
		
		
		while (raw.startsWith("(") && raw.endsWith(")")) {
			raw = raw.substring(1, raw.length() - 1).trim();
		}

		raw = raw.trim();
		rawLc = raw.toLowerCase();
		
		int idx = rawLc.indexOf("*");
			
		if (idx != -1) {
			predicate.type = Type.ANY;
			raw = raw.substring(idx + 1);
		}
		
		raw = raw.trim();
		rawLc = raw.toLowerCase();

		if (cached.containsKey(raw)) {
			predicate.value = cached.get(raw);
			predicate.selector = SELECTOR.CONTAINS;
			
			return predicate;
		}

		
		idx = rawLc.indexOf("length()");
		int idx2 = rawLc.indexOf("length");

		if (idx != -1 || idx2 != -1 || rawLc.matches("^[-0-9 ,:]+$")) {
			Index index = new Index(predicate);
			
			if (idx != -1) {
				rawLc = rawLc.substring(idx + 8).trim();
				rawLc = rawLc.toLowerCase();
				index.range = RANGE.LAST;
			}

			idx2 = rawLc.indexOf("length");
			if (idx2 != -1) {
				index.range = RANGE.LAST;
				rawLc = rawLc.substring(idx2 + 6).trim();
			}
			
			if (rawLc.length() > 0) {
				idx = rawLc.indexOf(",");
				idx2 = rawLc.indexOf(":");
				if (idx != -1) {
					Set<Integer> numbers = new LinkedHashSet<>();
					index.set = OsonPath.oson.deserialize("[" + rawLc + "]", numbers.getClass());
					index.range = RANGE.SET;
					
				} else if (idx2 != -1) {
					String[] numbers = rawLc.split(":");
					idx = numbers.length;
					
					index.start = Index.getValue(numbers[0]);
					if (idx > 1) {
						index.end = Index.getValue(numbers[1]);
						if (idx == 3) {
							index.step = Index.getValue(numbers[2]);
						}
					}

					index.range = RANGE.SLICE;
					
				} else {
					index.index = (int) NumberUtil.getNumber(rawLc, Integer.class);
				}
			}

			return index;
		}
		
		String left = null, right = null;
		for (Operator operator: Operator.values()) {
			for (String op: operator.ops) {
				idx = rawLc.indexOf(" " + op + " ");
				if (idx != -1) {
					left = raw.substring(0, idx);
					right = raw.substring(idx + op.length() + 2);
					predicate.op = operator;
					break;
				}
			}
		}
		
		if (predicate.op == null && left == null && right == null) {
			for (Operator operator: Operator.values()) {
				for (String op: operator.ops) {
					idx = rawLc.indexOf(op);
					if (idx != -1 && !op.matches("[a-z]+")) {
						left = raw.substring(0, idx);
						right = raw.substring(idx + op.length());
						predicate.op = operator;
						break;
					}
				}
			}
		}
		
		
		if (left == null) {
			left = raw;
		}
		
		left = left.trim();
		rawLc = left.toLowerCase();

		// processing func
		for (Func func: Func.values()) {
			idx = rawLc.indexOf(func.toString());
			if (idx != -1) {
				predicate.func = func;
				break;
			}
		}
		
		if (right != null) {
			right = right.trim();
		}


		
		predicate.field = left;

		if (right != null) {
			if (xpath.startsWith("$")) {
				predicate.value = this.process(right);
			}
			
			if (predicate.value == null) {
				predicate.value = OsonPath.oson.deserialize(right);
			}
		}
		
		return predicate;
	}
	
	
	 private List<Filter> processPredicates(String raw, String andOr) {
		 List<Filter> predicates = new ArrayList<>();
		 
		 int idx = 0;
		 int idx2 = raw.indexOf(andOr, idx);
		 
		 while (idx2 != -1) {
			 String condition = raw.substring(idx, idx2);
			 
			 predicates.add(processPredicate(condition));
		 
			 idx = idx2 + 2;
			 
			 idx2 = raw.indexOf(andOr, idx);
		 }

		 return predicates;
	 }
	
	
	/*
	 * Major predicate processing
	 */
	private Filter processFilter(String raw) {
		Filter filter = new Filter(raw);

		String rawLc = raw.toLowerCase();
		
		rawLc.replaceAll(" and ", " && ");
		rawLc.replaceAll(" or ", " || ");

		int idx = rawLc.indexOf("&&");
		int idx2 = rawLc.indexOf("||");
		
		if (idx != -1) {
			if (idx2 != -1) {
				List<Integer> andList = new ArrayList<>();
				while (idx != -1) {
					andList.add(idx);
					idx = raw.indexOf("&&", idx + 2);
				}
				
				List<Integer> orList = new ArrayList<>();
				while (idx2 != -1) {
					andList.add(idx2);
					idx2 = raw.indexOf("||", idx2 + 2);
				}
				
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
				
				while (j < andList.size()) {
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
					if (i == andors[k]) {
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
						idx = andors[k];
						if (levelMap.get(idx) == k) {
							SELECTOR selector = null;
							if (andList.contains(idx)) {
								selector = SELECTOR.AND;
							} else {
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
							}
							
							if (rightFilter == null) {
								rightFilter = processPredicate(rightRaw);
							}

							if (leftFilter.selector != null && leftFilter.selector == selector && leftFilter.predicates != null) {
								leftFilter.predicates.add(rightFilter);
								currentFilter = leftFilter;
								rightFilter.parent = currentFilter;
								
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

			
		} else if (idx2 != -1) {
			filter.selector = SELECTOR.OR;
			filter.predicates = processPredicates(raw, "||");
			
		} else {
			List<Filter> predicates = new ArrayList<>();
			predicates.add(processPredicate(raw));
			filter.predicates = predicates;
		}

		return filter;
	}

	
	private Step processStep(String raw) {
		String name = "";
		
		int i = raw.indexOf("[");
		
		if (i > -1) {
			name = raw.substring(0, i).trim();
			
		} else {
			name = raw;
		}

		Step step = new Step(raw, name);
		
		List<Filter> filters = new ArrayList<>();

		while (i > -1) {
			raw = raw.substring(i+1).trim();
			
			i = raw.indexOf("]");

			Filter filter = processFilter(raw.substring(0, i).trim());
			
			filters.add(filter);
			
			i = raw.indexOf("[", i+1);
		}

		if (filters.size() > 0) {
			step.filters = filters;
		}
		
		return step;
	}


	@Override
	public List<Step> process() {
		int currentPos = 0;
		
		Step current = null;
		List<Step> steps = new ArrayList<>();
		
		String stepDelimiter = ".";
		String oneOrMore = "$..";
		String any = "*";
		String rootDelimiter = "$.";
		
		// dot–notation: $.store.book[0].title
		// the bracket–notation: $['store']['book'][0]['title']
		boolean dotNotation = true;
		
		if (xpath.startsWith(oneOrMore)) {
			current = Step.getInstance(Type.ONE_OR_MORE);
			steps.add(current);
			currentPos += 3;
		} else if (xpath.startsWith(rootDelimiter)) {
			current = Step.getInstance(Type.ROOT);
			steps.add(current);
			currentPos += 2;
			
		} else if (xpath.startsWith("$[")) {
			current = Step.getInstance(Type.ROOT);
			dotNotation = false;
			currentPos += 1;
		}

		String xpath2 = xpath.substring(currentPos);
		
		List<String> parts = new ArrayList<>(); 

		int idx2, idx3, idx4;
		String previous;
		
		int idx1 = 0;
		if (dotNotation) {
			idx2 = xpath2.indexOf(stepDelimiter, idx1);
			while (idx2 != -1) {
				previous = xpath2.substring(idx1, idx2);
				idx3 = previous.lastIndexOf("[");
				idx4 = previous.lastIndexOf("]");
				if (idx3 == -1 || idx4 > idx3) {
					
					if (idx4 > idx3) {
						String str = previous.substring(idx3 + 1, idx4);
						
						if ((str.startsWith("'") && str.endsWith("'"))
								|| (str.startsWith("\"") && str.endsWith("\""))) {
							
							List<String> list = new ArrayList<>();
							list.addAll(OsonPath.oson.deserialize("[" + str + "]", list.getClass()));
							
							if (list.size() > 1) {
								cached.put(str, list);
								parts.add(previous.substring(0, idx3) + "[" + str + "]");
								
							} else {
								parts.add(previous.substring(0, idx3) + "[" + str.substring(1, str.length()-1) + "]");
							}
							
						} else {
							parts.add(previous);
						}
						
					} else {
						parts.add(previous);
					}
					
					idx1 = idx2+1;
				}
				
				idx2 = xpath2.indexOf(stepDelimiter, idx2 + 1);
			}
			previous = xpath2.substring(idx1);
			parts.add(previous);
			
		} else {
			idx3 = xpath2.indexOf("[", idx1);
			idx4 = xpath2.indexOf("]", idx1);
			
			while (idx3 != -1 && idx4 > idx3) {
				previous = xpath2.substring(idx3 + 1, idx4).trim();
				
				if ((previous.startsWith("'") && previous.endsWith("'"))
						|| (previous.startsWith("\"") && previous.endsWith("\""))) {
					
					List<String> list = new ArrayList<>();
					list.addAll(OsonPath.oson.deserialize("[" + previous + "]", list.getClass()));
					
					if (list.size() > 1) {
						cached.put(previous, list);
						parts.add(parts.remove(parts.size()-1) + "[" + previous + "]");
						
					} else {
						parts.add(previous.substring(1, previous.length()-1));
					}
					
				} else if (previous.matches("^[-0-9 ,:]+$")) {
					parts.add(parts.remove(parts.size()-1) + "[" + previous + "]");
					
				} else {
					parts.add(previous);
				}
				
				idx3 = xpath2.indexOf("[", idx4+1);
				idx4 = xpath2.indexOf("]", idx4+1);
			}
			
		}
		
		
		String[] splitted = parts.toArray(new String[0]); // xpath.substring(currentPos).split(stepDelimiter);

		for (int i = 0; i < splitted.length; i++) {
			String raw = splitted[i].trim();
			
			if (raw.equals("")) {
				current = Step.getInstance(Type.ONE_OR_MORE);
				
			} else if (raw.equals(any)) {
				current = Step.getInstance(Type.ANY);
				
			} else {
				current = processStep(raw);
			}
			
			steps.add(current);
		}

		return steps;
	}

}
