package ca.oson.json.path;

import ca.oson.json.path.Type;

public class Predicate extends Filter {
	
	boolean not = false;
	
	Operand left;
	Operator op;
	Operand right;
	
	public Predicate(String raw) {
		super(raw);
		left = new Operand();
		right = new Operand();
	}

}
