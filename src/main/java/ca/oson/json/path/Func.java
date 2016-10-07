package ca.oson.json.path;

public enum Func {
	CONCAT, // concat()
	SUBSTRING, // substring()
	CONTAINS, // contains()
	SUBSTRING_BEFORE, // substring-before
	SUBSTRING_AFTER, // substring-after()
	TRANSLATE, // translate()
	NORMALIZE_SPACE, // normalize-space()
	STARTS_WITH, // starts-with
	STRING_LENGTH, // //Participant[string-length(FirstName)>=8]
	SUM, // sum()
	ROUND, // round()
	FLOOR, // floor()
	CEILING, // ceiling()
	POSITION, // position()
	MIN, // min()
	MAX, // max()
	AVG, // avg()
	STDDEV, // stddev()
	LENGTH; // length()
	
	public String toString() {
		return this.name().toLowerCase().replaceAll("_", "-"); //  + "()"
	}
}
