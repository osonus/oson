package ca.oson.json.path;

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
		
		
		
		
		return null;
	}

}
