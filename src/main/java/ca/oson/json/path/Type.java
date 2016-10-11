package ca.oson.json.path;

public enum Type {
	ROOT ("/", "$"), // The root element to query. This starts all path expressions. can be $ or //, depending on the xpath or jsonpath convention to use
	ONE_OR_MORE ("//", ".."), // recursive descent, or Deep scan. Available anywhere a name is required.
	ANY ("*"), // *
	TEXT ("text()"), // 
	NODE ("node()"), // node()
	REGULAR ("regular()"); // all the other types of step
	
	String[] types;
	
	Type(String... types) {
		this.types = types;
	}
}