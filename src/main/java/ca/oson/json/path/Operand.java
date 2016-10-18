package ca.oson.json.path;

import java.util.List;

public class Operand <E> {
	MathOperator op;
	List<Operand> children;
	
	// data
	//boolean not = false;
	String raw;

	Func func;
	Field field;
	E value;
	
	public Operand (String raw) {
		this.raw = raw;
		// this.field = raw;
	}
	
	public Operand (E value) {
		this.value = value;
	}
	
	public Operand (MathOperator op) {
		this.op = op;
	}
	
	public Operand () {
	}
}
