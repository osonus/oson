package ca.oson.json.path;

import java.util.Set;

import ca.oson.json.util.NumberUtil;
import ca.oson.json.util.StringUtil;

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
	
	public static int getValue(String value) {
		if (StringUtil.isEmpty(value)) {
			return NONE;
		}
		
		return (int) NumberUtil.getNumber(value, Integer.class);
	}
	
}
