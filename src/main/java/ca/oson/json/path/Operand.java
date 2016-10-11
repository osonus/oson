package ca.oson.json.path;

public class Operand <E> {
	Operand parent;
	Operand left;
	MathOperator op;
	Operand right;
	
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
	
	public Operand () {
	}
}
