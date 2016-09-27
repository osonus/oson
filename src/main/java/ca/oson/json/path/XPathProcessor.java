package ca.oson.json.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.oson.json.path.Step.Type;
import ca.oson.json.util.ArrayToJsonMap;

public class XPathProcessor extends PathProcessor {

	public XPathProcessor(String xpath) {
		super(xpath);
	}
	
	
	/*
	 * Major predicate processing
	 */
	private Filter processFilter(String raw) {
		Filter filter = new Filter(raw);
		
		
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
				idx1 = idx2;
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
