package ca.oson.json.path;

import java.util.Set;

public class Index extends Filter {
	public static enum RANGE {
		INDEX,
		SLICE,
		LAST,
		SET,
		ALL
	}
	
	static int NONE = -1000;
	
	RANGE range;
	
	int index = NONE;
	
	int start = NONE;
	int end = NONE;
	int step = 1;
	
	Set<Integer> set = null;
	
	
	
}
