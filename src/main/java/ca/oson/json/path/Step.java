package ca.oson.json.path;

public class Step {
	public enum Type {
		ROOT, // The root element to query. This starts all path expressions. can be $ or //, depending on the xpath or jsonpath convention to use
		ONE_OR_MORE, // recursive descent, or Deep scan. Available anywhere a name is required.
		ANY, // *
		REGULAR // all the other types of step
	}
	
	private static Step oneOrMore = null;
	private static Step root = null;
	private static Step any = null;
	
	private Type type;
	
	String raw;
	String name;
	Filter filter;
	
	public Step(String raw, String name) {
		this.type = Type.REGULAR;
		this.raw = raw;
		this.name = name;
	}
	
	private Step(Type type, String raw, String name) {
		this.type = type;
		this.raw = raw;
		this.name = name;
	}
	private Step(Type type) {
		this.type = type;
	}
	
	public static Step getInstance(Type type) {
		switch(type) {
		case ROOT:
			if (root == null) {
				root = new Step(type, "/", "Root");
			}
			return root;
			
		case ANY:
			if (any == null) {
				any = new Step(type, "*", "Any");
			}
			return any;
			
		case ONE_OR_MORE:
			if (oneOrMore == null) {
				oneOrMore = new Step(type, "//", "One or more");
			}
			return oneOrMore;
			
		}

		return new Step(type);
	}
	
	
	
}
