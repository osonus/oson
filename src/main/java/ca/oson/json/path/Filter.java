package ca.oson.json.path;

import java.util.List;

public class Filter {
	Filter parent = null;
	String raw;
	SELECTOR selector;
	List<Filter> predicates = null;
	
	
	public Filter(String raw) {
		this.raw = raw;
	}
	
	public Filter(SELECTOR selector) {
		this.selector = selector;
	}
}
