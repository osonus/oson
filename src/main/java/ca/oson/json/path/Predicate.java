package ca.oson.json.path;

import ca.oson.json.path.Type;

public class Predicate <E> extends Filter {
	public Predicate(String raw) {
		super(raw);
	}
	boolean not = false;
	Axis axis = Axis.NONE;
	Type type;
	
	String field;
	Operator op;
	E value;
	
	Func func;
}
