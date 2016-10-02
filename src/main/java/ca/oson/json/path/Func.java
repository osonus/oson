package ca.oson.json.path;

public enum Func {
	CONCAT, // concat()
	SUBSTRING, // substring()
	CONTAINS, // contains()
	SUBSTRING_BEFORE, // substring-before
	SUBSTRING_AFTER, // substring-after()
	TRANSLATE, // translate()
	NORMALIZE_SPACE, // normalize-space()
	STRING_LENGTH, // string-length()
	SUM, // sum()
	ROUND, // round()
	FLOOR, // floor()
	CEILING, // ceiling()
	POSITION; // position()
	
	public String toString() {
		return this.name().toLowerCase().replaceAll("_", "-") + "()";
	}
}
