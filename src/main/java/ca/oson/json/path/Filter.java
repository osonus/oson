package ca.oson.json.path;

import java.util.ArrayList;
import java.util.List;

public class Filter {
	String raw;
	SELECTOR selector;
	List<Filter> predicates = null;
	
	
	public Filter(String raw) {
		this.raw = raw;
	}
	
}
