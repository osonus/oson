package ca.oson.json.path;

import java.util.ArrayList;
import java.util.Arrays;
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
		if (rawLc.startsWith("contains(") && raw.endsWith(")")) {
			int i = rawLc.lastIndexOf(",");
			if (i > 9) {
				raw = raw.substring(9, i).trim();
			} else {
				raw = raw.substring(9, raw.length()-1).trim();
			}
			
			if ( (raw.startsWith("'") && raw.endsWith("'"))
					|| (raw.startsWith("\"") && raw.endsWith("\""))) {
				raw = raw.substring(1, raw.length()-1).trim();
			}

			predicate.value = StringUtil.array2List(raw.split(" "));
			predicate.selector = SELECTOR.CONTAINS;
			
			return predicate;
		}
		
		
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
			predicate.selector = SELECTOR.CONTAINS;
			
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
		for (Func func: Func.values()) {
			idx = rawLc.indexOf(func.toString());
			if (idx != -1) {
				predicate.func = func;
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

			return index;
		}
		
		predicate.field = left;

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
		int idx3, idx4;
		String previous;
		
		while (idx2 != -1) {
			previous = xpath2.substring(idx1, idx2);
			idx3 = previous.lastIndexOf("[");
			idx4 = previous.lastIndexOf("]");
			if (idx3 == -1 || idx4 > idx3) {
				// make sure previous is a separate step
				parts.add(previous);
				idx1 = idx2+1;
			}
			
			idx2 = xpath2.indexOf(stepDelimiter, idx2 + 1);
		}
		previous = xpath2.substring(idx1);
		parts.add(previous);
		
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
