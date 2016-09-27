package ca.oson.json.path;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ca.oson.json.util.ArrayToJsonMap;

public class XPathProcessor extends PathProcessor {

	public XPathProcessor(String xpath) {
		super(xpath);
	}

	@Override
	public List<Step> process() {
		// first clean up unncessary phrases
		Map<String, Object> cleanupMap = ArrayToJsonMap.array2Map(new String[] {
			"child::", "",
			"descendant-or-self::node()", "",
			"self::node()", "."
		});
		
		for (String key: cleanupMap.keySet()) {
			String replacement = cleanupMap.get(key).toString();
			xpath.replaceAll(key, replacement);
		}
		
		boolean done = false;
		
		int length = xpath.length();
		int currentPos = 0, setpStart;
		String remaining;
		
		Step current = null;
		List<Step> steps = new ArrayList<>();
		
		String delimiter = "/";
		
		if (xpath.startsWith("//")) {
			current = OneOrMore.getInstance();
			steps.add(current);
			currentPos += 2;
		} else if (xpath.startsWith(delimiter)) {
			current = Root.getInstance();
			steps.add(current);
			currentPos++;
		}
		
		// in case rules are not respected
		while (xpath.charAt(currentPos) == ' ' && currentPos < length) {
			currentPos++;
		}
		
		while (currentPos < length) {
			setpStart = currentPos;
			
			if (xpath.charAt(currentPos) == '*') {
				current = Any.getInstance();
				steps.add(current);
				currentPos++;
				
				while (xpath.charAt(currentPos) == ' ' && currentPos < length) {
					currentPos++;
				}
			}
			
			if (currentPos < length) {
			
				int idx = xpath.indexOf(delimiter);
		
				
		
			}
			
		}
		
		return null;
	}

}
