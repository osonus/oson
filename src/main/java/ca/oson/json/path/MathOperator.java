package ca.oson.json.path;

public enum MathOperator {
	DIVISION ("div"), // Division
	MULTIPLICATION ("*"), // Multiplication
	MODULUS ("%", "mod"), // Modulus (division remainder)
	ADDITION ("+"), // Addition,
	SUBTRACTION ("-"); // Subtraction,
	
	String[] ops;

	MathOperator(String... ops) {
		this.ops = ops;
	}
}
