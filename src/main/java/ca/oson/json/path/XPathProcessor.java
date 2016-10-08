package ca.oson.json.path;

import java.util.ArrayList;
import java.util.Arrays;
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
		
		while (raw.startsWith("(") && raw.endsWith(")")) {
			raw = raw.substring(1, raw.length() - 1).trim();
		}
		
		rawLc = raw.toLowerCase();
		
		// contains('X Y Z', local-name())
//		if (rawLc.startsWith("contains(") && raw.endsWith(")")) {
//			int i = rawLc.lastIndexOf(",");
//			if (i > 9) {
//				raw = raw.substring(9, i).trim();
//			} else {
//				raw = raw.substring(9, raw.length()-1).trim();
//			}
//			
//			if ( (raw.startsWith("'") && raw.endsWith("'"))
//					|| (raw.startsWith("\"") && raw.endsWith("\""))) {
//				raw = raw.substring(1, raw.length()-1).trim();
//			}
//
//			predicate.value = StringUtil.array2List(raw.split(" "));
//			predicate.selector = SELECTOR.HAS;
//			
//			return predicate;
//		}
		
		
		rawLc = raw.toLowerCase();
		
		// handling ::
		int idx = raw.indexOf("::");
		int idx2;
		if (idx != -1) {
			rawLc = raw.substring(0, idx);
			
			for (Axis axis: Axis.values()) {
				if (rawLc.endsWith(axis.toString())) {
					predicate.axis = axis;

					rawLc = rawLc.substring(0, rawLc.length() - axis.toString().length());
					
					rawLc = rawLc.trim().toLowerCase();
					
					if (rawLc.contains("!") || rawLc.contains("not")) {
						predicate.not = true;
					}
					
					break;
				}
			}

			raw = raw.substring(idx + 2);
			
			raw = raw.replaceAll("self::", "");
			
			
		} else {
			idx = raw.indexOf("..");
			idx2 = raw.indexOf(".");
			
			if (idx != -1) {
				predicate.axis = Axis.PARENT;
				rawLc = raw.substring(0, idx).toLowerCase();
				
				if (rawLc.contains("!") || rawLc.contains("not")) {
					predicate.not = true;
				}
				
				raw = raw.substring(idx + 2);
				 
			} else if (idx2 != -1) {
				predicate.axis = Axis.SELF;
				rawLc = raw.substring(0, idx2).toLowerCase();
				
				if (rawLc.contains("!") || rawLc.contains("not")) {
					predicate.not = true;
				}
				
				raw = raw.substring(idx2 + 2); 
			}
		}
		
		String[] splitted = raw.split("\\|");
		if (splitted.length > 1) {
			predicate.value = StringUtil.array2List(splitted);
			predicate.selector = SELECTOR.HAS;
			
			return predicate;
		}
		
		
		raw = raw.trim();
		rawLc = raw.toLowerCase();
		
		idx = rawLc.indexOf("node()");
		idx2 = rawLc.indexOf("text()");
		int idx3 = rawLc.indexOf("*");
			
		if (idx != -1) {
			predicate.type = Type.NODE;
			raw = raw.substring(idx + 6);
		} else if (idx2 != -1) {
			predicate.type = Type.TEXT;
			raw = raw.substring(idx2 + 6);
		} else if (idx3 == 0) {
			predicate.type = Type.ANY;
			raw = raw.substring(idx3 + 1);
		}
		
		raw = raw.trim();
		rawLc = raw.toLowerCase();

		
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
					if (idx != -1 && !op.matches("[a-z]+") && !op.equals("-")) {
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

		// processing func
		String name;
		for (Func func: Func.values()) {
			name = func.toString();
			if (containsFunction(rawLc, name)) {
				predicate.func = func;
				idx = rawLc.indexOf(name);
				idx2 = rawLc.indexOf("(", idx);
				String[] params = null;
				if (idx2 > idx) {
					params = getParameters(left.substring(idx2 + 1));
				}
				
				switch (func) {
				case STRING_LENGTH:
				case NUMBER:
				case COUNT:
				case SUM:
				case ROUND:
					if (params != null && params.length > 0) {
						predicate.field = params[0];
					}
					break;

				case STARTS_WITH:
				case CONTAINS:
					if (params != null && params.length > 1) {
						predicate.field = params[0];
						predicate.value = params[1];
					}
					
					break;

				case TRANSLATE:
					if (params != null && params.length > 2) {
						predicate.field = params[0];
						List<Object> list = new ArrayList<>();
						list.add(params[1]);
						list.add(params[2]);
						
						predicate.value = list;
					}
					
					break;
				}
				
				
				break;
			}
		}
		

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
		
		
		if (predicate.field == null) {
			predicate.field = left;
		}

		if (right != null) {
			right = right.trim();
			
			if (right.contains("/")) {
				predicate.value = this.process(right);
			}
			
			if (predicate.value == null) {
				predicate.value = OsonPath.oson.deserialize(right);
			}
		}
		
		return predicate;
	}



	@Override
	public List<Step> process() {
		// first clean up unncessary phrases
//		Map<String, Object> cleanupMap = ArrayToJsonMap.array2Map(new String[] {
//			"child::", "",
//			"descendant-or-self::node()", "",
//			"self::node()", ".",
//			"parent::node()", ".."
//		});
//		for (String key: cleanupMap.keySet()) {
//			String replacement = cleanupMap.get(key).toString();
//			xpath.replaceAll(key, replacement);
//		}

		
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
