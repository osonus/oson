package ca.oson.json.path;

public enum Func {
	CONCAT, // concat()
	SUBSTRING, // substring()
	CONTAINS, // contains(string, string), returns TRUE if the second string is part of the first
	SUBSTRING_BEFORE, // substring-before
	SUBSTRING_AFTER, // substring-after()
	TRANSLATE, // translate(string1, string2, string3): translates string1 by substituting string2 elements with string3 elements
	NORMALIZE_SPACE, // normalize-space()
	STARTS_WITH, // starts-with(string, string): returns TRUE if the second string is part of the first and starts off the first
	// //Participant[starts-with(Firstname,'Berna')]"
	STRING_LENGTH, // string-length(string): returns the length of a string, //Participant[string-length(FirstName)>=8]
	SUM, // sum(node-set): computes the sum of a given set of nodes. If necessary, does string conversion with number()
	ROUND, // round(number): round a number, e.g. 1.4 becomes 1 and 1.7 becomes 2
	FLOOR, // floor()
	CEILING, // ceiling()
	POSITION, // position() returns the position of an element with respect to other children in the same parent
	MIN, // min()
	MAX, // max()
	AVG, // avg()
	STDDEV, // stddev()
	LENGTH, // length()
	COUNT, // count(node-set): gives the number of nodes in a node set, //problems[count(//problem) >= 2]
	NUMBER; // number(string): transforms a string into a number
	//last() gives the number or nodes within a context
	
	public String toString() {
		return this.name().toLowerCase().replaceAll("_", "-"); //  + "()"
	}
}
