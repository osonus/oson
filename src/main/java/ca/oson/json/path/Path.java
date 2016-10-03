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
		
		String[] xpaths = xpath.split("\\|");
		
		boolean isXpath = false;
		
		for (String p: xpaths) {
			if (PathProcessor.isXpath(xpaths[0])) {
				isXpath = true;
				break;
			}
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
