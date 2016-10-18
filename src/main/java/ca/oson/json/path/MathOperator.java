package ca.oson.json.path;

public enum MathOperator {
	SUBTRACTION ("-"), // Subtraction,
	ADDITION ("+"), // Addition,
	MODULUS ("%", "mod"), // Modulus (division remainder)
	MULTIPLICATION ("*"), // Multiplication
	DIVISION ("div"); // Division
	
	String[] ops;

	MathOperator(String... ops) {
		this.ops = ops;
	}
}
