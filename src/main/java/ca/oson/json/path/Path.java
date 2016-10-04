package ca.oson.json.path;

import java.util.ArrayList;
import java.util.List;

import ca.oson.json.util.StringUtil;

public class Path {
	List<List<Step>> steps;

	/*
	 * the position of the current step in a path
	 */
	int[] positions;
	
	public Path(String xpath) {
		if (StringUtil.isEmpty(xpath)) {
			xpath = "//*";
		} else {
			xpath = xpath.trim();
		}
		processPath(xpath);
	}
	
	
	private void processPath(String xpath) {
		PathProcessor processor;

		boolean isXpath = false;
		
		String xp;
		List<String> xpaths = new ArrayList<>();

		int idx = 0;
		int idx2 = xpath.indexOf("|", idx);
		while (idx2 != -1) {
			xp = xpath.substring(idx, idx2);

			if (StringUtil.isParenthesisBalanced(xp)) {
				if (!isXpath && PathProcessor.isXpath(xp)) {
					isXpath = true;
				}
				
				xpaths.add(xp.trim());
				
				idx = idx2 + 1;
			}

			idx2 = xpath.indexOf("|", idx2 + 1);
		}
		
		xp = xpath.substring(idx);
		xpaths.add(xp.trim());
		if (!isXpath && PathProcessor.isXpath(xp)) {
			isXpath = true;
		}
		
		if (isXpath) {
			processor = new XPathProcessor(xpath);
		} else {
			processor = new JPathProcessor(xpath);
		}
		
		steps = new ArrayList<>();
		
		for (String p: xpaths) {
			steps.add(processor.process(p));
		}
		
		positions = new int[steps.size()];
	}

	
	public List<List<Step>> getSteps() {
		return steps;
	}
}
