package ca.oson.json.path;

public enum Operator {
	ADDITION ("+"), // Addition,
	SUBTRACTION ("-"), // Subtraction,
	MULTIPLICATION ("*"), // Multiplication
	DIVISION ("div"), // Division
	MODULUS ("%", "mod"), // Modulus (division remainder)
	IN ("in"), // in a list of values
	NOT_IN ("! in", "not in", "nin"), // not in a set
	BETWEEN ("between"), // Between an inclusive range
	LIKE, // Search for a pattern, matching regex
	GREATER_THAN_EQUAL (">=", "ge"), // Greater than or equal to
	GREATER_THAN (">", "gt"), // Greater than
	LESS_THAN_EQUAL ("<=", "le"), // Less than or equal to
	LESS_THAN ("<", "lt"), // Less than, price<9.80
	EQUAL ("=", "==", "eq"), // is equal to
	NOT_EQUAL ("!=", "ne"), // Not equal
	REGEX ("=~");
	
	String[] ops;
	
	
	Operator(String... ops) {
		this.ops = ops;
	}
	
}
