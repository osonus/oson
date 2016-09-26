package ca.oson.json.path;

import java.util.ArrayList;
import java.util.List;

import ca.oson.json.util.StringUtil;

public class Path {
	List<Step> steps;

	/*
	 * the position of the current step in a path
	 */
	int pos = 0;
	
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
		if (PathProcessor.isXpath(xpath)) {
			processor = new XPathProcessor(xpath);
		} else {
			processor = new XPathProcessor(xpath);
		}
		steps = processor.process();
	}
	
	public List<Step> getSteps() {
		return steps;
	}
}
