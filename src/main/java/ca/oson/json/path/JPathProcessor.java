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
import ca.oson.json.util.StringUtil;

public class JPathProcessor extends PathProcessor {
	public Map<String, List<String>> cached = new HashMap<>();

	public JPathProcessor(String xpath) {
		super(xpath);
		// TODO Auto-generated constructor stub
	}
	
	
	@Override
	protected Field getField(String raw, String node) {
		Field field = new Field(raw);
		
		Axis axis = Axis.NONE;
	
		// parent, self, child
		if (raw.startsWith("@...")) {
			raw = raw.substring(4).trim();
			axis = Axis.DESCENDANT;
			
		} else if (raw.startsWith("@..")) {
			raw = raw.substring(3).trim();
			axis = Axis.CHILD;
			
		} else if (raw.startsWith("..@.")) {
			raw = raw.substring(4).trim();
			axis = Axis.ANCESTOR;
			
		} else if (raw.startsWith(".@.")) {
			raw = raw.substring(3).trim();
			axis = Axis.PARENT;
		}

		field.axis = axis;
		field.type = getType(raw);
		
		int idx = raw.indexOf(".");
		
		if (raw.startsWith("$") || idx != -1) {
			field.steps = this.process(raw);
			
		} else {
			// if it is separated by |
			idx = raw.indexOf("|");
			int idx2 = raw.indexOf(",");
			int idx3 = raw.indexOf(" ");
			
			if (idx != -1) {
				field.names = StringUtil.toArray(raw, "|");
			} else if (idx2 != -1) {
				field.names = StringUtil.toArray(raw, ",");
			} else if (idx3 != -1) {
				field.names = StringUtil.toArray(raw, " ");
			}
		}
		
		return field;
	}


	protected Filter processPredicate(String raw) {
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
		
		// idx2 != -1 
		int idx2 = rawLc.indexOf("@.length");
		
		raw = raw.replaceAll("@.", "").trim();

		List<String> list = new ArrayList<>();
		
		raw = StringUtil.unwrap(raw, "(", ")");

		if (raw.startsWith("+(") && raw.endsWith(")") && StringUtil.isParenthesisBalanced(raw.substring(2, raw.length()-1))) {
			list.addAll(OsonPath.oson.deserialize("[" + raw.substring(2, raw.length()-1) + "]", list.getClass()));
			predicate.right.value = list;
			predicate.selector = SELECTOR.EXPOSE;
			
			return predicate;

		} else if (raw.startsWith("-(") && raw.endsWith(")") && StringUtil.isParenthesisBalanced(raw.substring(2, raw.length()-1))) {
			list.addAll(OsonPath.oson.deserialize("[" + raw.substring(2, raw.length()-1) + "]", list.getClass()));
			predicate.right.value = list;
			predicate.selector = SELECTOR.IGNORE;
			
			return predicate;
		}
		
		raw = StringUtil.unwrap(raw, "(", ")");

		raw = raw.trim();
		rawLc = raw.toLowerCase();
		
		int idx; // = rawLc.indexOf("*");
//		if (idx != -1) {
//			//predicate.left.type = Type.ANY;
//			raw = raw.substring(idx + 1);
//		}
//		raw = raw.trim();

		if (cached.containsKey(raw)) {
			predicate.right.value = cached.get(raw);
			predicate.selector = SELECTOR.HAS;
			
			return predicate;
		}

		rawLc = raw.toLowerCase();
		idx = rawLc.indexOf("length()");
		

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
			Map<String, Operator> map = new HashMap<>();
			for (Operator operator: Operator.values()) {
				for (String op: operator.ops) {
					idx = rawLc.indexOf(op);
					if (idx != -1 && !op.matches("[a-z]+")) { // && !op.equals("-")
						left = raw.substring(0, idx);
						right = raw.substring(idx + op.length());
						predicate.op = operator;
						map.put(op, operator);
					}
				}
			}
			
			if (map.size() > 0) {
				int max = 0;
				String op = "";
				for (String key: map.keySet()) {
					if (key.length() > max) {
						max = key.length();
						op = key;
					}
				}
				idx = rawLc.indexOf(op);
				if (idx != -1) {
					left = raw.substring(0, idx);
					right = raw.substring(idx + op.length());
					predicate.op = map.get(op);
				}
			}
		}
		
		
		if (left == null) {
			left = raw;
			
		} else {
			rawLc = right.toLowerCase(); 
			if (rawLc.contains("position()")) {
				rawLc = right;
				right = left;
				left = rawLc;
				
				switch (predicate.op) {
				case GREATER_THAN_EQUAL:
					predicate.op = Operator.LESS_THAN_EQUAL;
					break;
				case LESS_THAN_EQUAL:
					predicate.op = Operator.GREATER_THAN_EQUAL;
					break;
				case GREATER_THAN:
					predicate.op = Operator.LESS_THAN;
					break;
				case LESS_THAN:
					predicate.op = Operator.GREATER_THAN;
					break;
				}
			}
		}
		
		
		boolean hasMathOp = false;
		for (MathOperator operator: MathOperator.values()) {
			for (String op: operator.ops) {
				idx = rawLc.indexOf(" " + op + " ");
				if (idx != -1) {
					hasMathOp = true;
					break;
				}
			}
		}
		
		if (!hasMathOp && right != null) {
			rawLc = right.toLowerCase();
			for (MathOperator operator: MathOperator.values()) {
				for (String op: operator.ops) {
					idx = rawLc.indexOf(" " + op + " ");
					if (idx != -1) {
						hasMathOp = true;
						rawLc = right;
						right = left;
						left = rawLc;
						
						break;
					}
				}
			}
		}
		
		
		predicate.left = processOperand(left, hasMathOp);
		

		if (right != null) {
			right = right.trim();

			if (right.startsWith("$")) {
				predicate.right.value = this.process(right);
			}
			
			if (predicate.right.value == null) {
				predicate.right.value = OsonPath.oson.deserialize(right);
			}
		}
		
		return predicate;
	}
	
	
	protected Type getType(String node) {
		Type type = Type.REGULAR;
		
		if (node.length() == 0 || node.equals("*")) {
			type = Type.ANY;
		}
		
		return type;
	}

	@Override
	protected Step processStep(String raw, String node) {
		Step step = new Step(raw, node);
		step.type = getType(node);

		node = node.toLowerCase();
		for (Func func: Func.values()) {
			if (containsFunction(node, func.toString())) {
				step.operand = processOperand(node);
				break;
			}
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
			steps.add(current);
			dotNotation = false;
			currentPos += 1;
		}

		String xpath2 = xpath.substring(currentPos);
		
		List<String> parts = new ArrayList<>(); 

		int idx2, idx3, idx4, idx;
		String previous;
		
		int idx1 = 0;
		if (dotNotation) {
			oneOrMore = "..";
			idx2 = xpath2.indexOf(stepDelimiter, idx1);
			idx = xpath2.indexOf(oneOrMore, idx1);
			while (idx2 != -1) {
				previous = xpath2.substring(idx1, idx2);
				idx3 = previous.lastIndexOf("[");
				idx4 = previous.lastIndexOf("]");
				if ((idx3 == -1 || idx4 > idx3) && StringUtil.isParenthesisBalanced(previous)) {
					
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
					
					if (idx2 == idx) {
						parts.add(oneOrMore);
						idx1 = idx2+2;
					} else {
						idx1 = idx2+1;
					}
					
					idx2 = idx1;
					
				} else {
					idx2 = idx2+1;
					
				}
				
				idx = xpath2.indexOf(oneOrMore, idx2);
				idx2 = xpath2.indexOf(stepDelimiter, idx2);
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
			
			if (raw.equals(oneOrMore)) {
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
