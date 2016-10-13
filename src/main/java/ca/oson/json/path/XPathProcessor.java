package ca.oson.json.path;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import ca.oson.json.OsonPath;
import ca.oson.json.path.Index.RANGE;
import ca.oson.json.util.NumberUtil;
import ca.oson.json.util.StringUtil;

public class XPathProcessor extends PathProcessor {

	public XPathProcessor(String xpath) {
		super(xpath);
	}
	

	@Override
	protected Field getField(String raw, String node) {
		Field field = new Field(raw);

		int idx = raw.indexOf("::");
		Axis axis = Axis.NONE;
		String raw2 = raw;
		if (idx != -1) {
			axis = getAxis(raw.substring(0, idx));
			
			raw = raw.substring(idx+2).trim();

		} else if (xpath.startsWith("../")) {
			axis = Axis.PARENT;
			raw2 = raw2.substring(idx+3).trim();
			
		} else if (xpath.startsWith(".//")) {
			axis = Axis.SELF;
			raw2 = raw2.substring(idx+3).trim();
			
		} else if (xpath.startsWith(".")) {
			axis = Axis.SELF;
			raw2 = raw2.substring(idx+1).trim();
			
		}

		field.axis = axis;
		field.type = getType(raw2);
		
		int idx2 = raw2.indexOf("/");
		
		if (idx2 != -1) {
			// either starts with root, or invovles more than 1 steps
			field.steps = this.process(raw);
			
		} else {
			raw = raw2;
			
			// if it is separated by |
			idx = raw.indexOf("|");
			idx2 = raw.indexOf(",");
			int idx3 = raw.indexOf(" ");
			
			if (idx != -1) {
				field.names = StringUtil.toArray(raw, "|");
			} else if (idx2 != -1) {
				field.names = StringUtil.toArray(raw, ",");
			} else if (idx3 != -1) {
				field.names = StringUtil.toArray(raw, " ");
				
			} else if (raw.equals(".")) {
				idx = node.indexOf(raw);
				
				if (idx != -1) {
					node = node.substring(0, idx);
					idx = node.indexOf(raw);
					String[] names = node.split("[^a-zA-Z0-9_]");
					node = names[0].trim();
					if (node.length() > 0) {
						field.name = node;
					}
				}
			}
			
			if (field.names != null && field.names.length > 1) {
				for (idx = 1; idx < field.names.length; idx++) {
					idx2 = field.names[idx].indexOf("::");
					if (idx2 != -1) {
						field.names[idx] = field.names[idx].substring(idx2 + 2).trim();
					}
				}
			}
			
		}

		return field;
	}

	@Override
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

		raw = StringUtil.unwrap(raw, "(", ")");
		
		rawLc = raw.toLowerCase();
		
		int idx, idx2;

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
		
		left = left.trim();
		rawLc = left.toLowerCase();

		idx = rawLc.indexOf("position()");
		idx2 = rawLc.indexOf("last()");

		if (idx != -1 || idx2 != -1 || NumberUtil.isInt(left)) {
			Index index = new Index(predicate);
			
			if (idx != -1) {
				left = left.substring(idx + 10).trim();
				rawLc = left.toLowerCase();
			}
			
			
			idx2 = rawLc.indexOf("last()");
			if (idx2 != -1) {
				index.range = RANGE.LAST;
				left = left.substring(idx2 + 6).trim();
			}

			if (left.length() > 0) {
				index.index = (Integer) cleanUpNumber(left, Integer.class);
			}
			
			
			if (right != null && right.length() > 0) {
				rawLc = right.toLowerCase();
				idx2 = rawLc.indexOf("last()");
				if (idx2 != -1) {
					index.range = RANGE.LAST;
					right = right.substring(idx2 + 6).trim();
				}

				if (right.length() > 0) {
					index.index = (Integer) cleanUpNumber(right, Integer.class);
				}
			}
			
			// fix indexing
			if (index.index != null && index.index > 0) {
				index.index = index.index - 1;
			}

			return index;
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
			
			if (right.contains("/")) {
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
			
		} else {
			node = node.toLowerCase();
			
			if (node.startsWith("text") && node.substring(4, node.indexOf("(", 4)).trim().length() == 0) {
				type = Type.TEXT;
				
			} else if (node.startsWith("node") && node.substring(4, node.indexOf("(", 4)).trim().length() == 0) {
				type = Type.NODE;
				
			}
		}
		
		return type;
	}
	
	protected Axis getAxis(String axisName) {
		axisName = axisName.toLowerCase();
		for (Axis ax: Axis.values()) {
			if (axisName.endsWith(ax.toString())) {
				return ax;
			}
		}
		
		return Axis.NONE;
	}

	@Override
	protected Step processStep(String raw, String node) {
		int idx = node.indexOf("::");
		Axis axis = null;
		if (idx != -1) {
			axis = getAxis(node.substring(0, idx));
			
			node = node.substring(idx+2).trim();
			
		} else if (node.equals(".")) {
			axis = Axis.SELF;
		
		} else if (node.equals("../")) {
			axis = Axis.PARENT;
			
		} else if (node.equals(".//")) {
			axis = Axis.SELF;
			
		}
		
		
		
		Step step = new Step(raw, node);
		step.axis = axis;
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
		
		String stepDelimiter = "/";
		String oneOrMore = "//";
		String any = "*";
		
		if (xpath.startsWith(oneOrMore)) {
			current = Step.getInstance(Type.ONE_OR_MORE);
			steps.add(current);
			currentPos += 2;
		} else if (xpath.startsWith(stepDelimiter)) {
			current = Step.getInstance(Type.ROOT);
			steps.add(current);
			currentPos++;

		} else if (xpath.startsWith("../")) {
			String raw = "../";
			current = processStep(raw, raw);
			steps.add(current);
			currentPos += 3;
			
		} else if (xpath.startsWith(".//")) {
			String raw = ".//";
			current = processStep(raw, raw);
			steps.add(current);
			currentPos += 3;
			
		} else if (xpath.startsWith(".")) {
			String raw = ".";
			current = processStep(raw, raw);
			steps.add(current);
			currentPos++;
			
		}

		String xpath2 = xpath.substring(currentPos);
		
		List<String> parts = new ArrayList<>(); 
		
		int idx1 = 0;
		int idx2 = xpath2.indexOf(stepDelimiter, idx1);
		int idx = xpath2.indexOf(oneOrMore, idx1);
		int idx3, idx4;
		String previous;

		while (idx2 != -1) {
			previous = xpath2.substring(idx1, idx2);
			idx3 = previous.lastIndexOf("[");
			idx4 = previous.lastIndexOf("]");
			if ((idx3 == -1 || idx4 > idx3) && StringUtil.isParenthesisBalanced(previous)) {
				// make sure previous is a separate step
				parts.add(previous);

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
