package ca.oson.json.path;

import java.util.Set;

public class Index extends Predicate {
	public static enum RANGE {
		INDEX,
		SLICE,
		LAST,
		SET,
		ALL
	}
	
	public Index(String raw) {
		super(raw);
	}
	
	
	public Index(Predicate predicate) {
		super(predicate.raw);
		this.op = predicate.op;
		this.func = predicate.func;
		this.value = predicate.value;
	}


	
	static int NONE = -1000;
	
	RANGE range = null;
	
	int index = NONE;
	
	int start = NONE;
	int end = NONE;
	int step = 1;
	
	Set<Integer> set = null;
	
	
	
}
