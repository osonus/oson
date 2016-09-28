package ca.oson.json.path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import ca.oson.json.path.Step.Type;
import ca.oson.json.util.ArrayToJsonMap;

public class XPathProcessor extends PathProcessor {

	public XPathProcessor(String xpath) {
		super(xpath);
	}
	
	
	private Predicate processPredicate(String raw) {
		raw = raw.trim();
		
		
		
		
		Predicate predicate = new Predicate(raw);
		
		
		
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
				List<Filter> predicates = new ArrayList<>();

				Map<Integer, Integer> counts = new LinkedHashMap<>();

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
				i = 0;
				k = 0;
				Stack<Character> stack = new Stack<>();
				while (i <  andors[k]) {
					
				}
				
				
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
		
		List<Filter> predicates = new ArrayList<>();
		

		while (i > -1) {
			raw = raw.substring(i+1).trim();
			
			i = raw.indexOf("]");

			Filter filter = processFilter(raw.substring(0, i).trim());
			
			predicates.add(filter);
			
			i = raw.indexOf("[", i+1);
		}
		
		return step;
	}

	
	@Override
	public List<Step> process() {
		// first clean up unncessary phrases
		Map<String, Object> cleanupMap = ArrayToJsonMap.array2Map(new String[] {
			"child::", "",
			"descendant-or-self::node()", "",
			"self::node()", ".",
			"parent::node()", ".."
		});
		
		for (String key: cleanupMap.keySet()) {
			String replacement = cleanupMap.get(key).toString();
			xpath.replaceAll(key, replacement);
		}

		
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
		int length = xpath2.length();
		int idx3, idx4;
		String previous;
		
		while (idx2 != -1) {
			previous = xpath2.substring(idx1, idx2);
			idx3 = previous.lastIndexOf("[");
			idx4 = previous.lastIndexOf("]");
			if (idx3 == -1 || idx4 > idx3) {
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
