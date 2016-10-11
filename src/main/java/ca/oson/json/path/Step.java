package ca.oson.json.path;

import java.util.List;

public class Step {
	private static Step oneOrMore = null;
	private static Step root = null;
	private static Step any = null;

	Axis axis = null;
	Type type = null;
	Operand operand = null;
	
	String raw;
	String name;
	
	
	public void setName(String name) {
		this.name = PathProcessor.cleanUpParenthesis(name);
	}

	List<Filter> filters = null;
	
	
	
	public Step(String raw, String name) {
		
		this.type = Type.REGULAR;
		this.raw = raw;
		this.setName(name);
	}
	
	private Step(Type type, String raw, String name) {
		this.type = type;
		this.raw = raw;
		this.setName(name);
	}
	private Step(Type type) {
		this.type = type;
	}
	
	public static Step getInstance(Type type) {
		switch(type) {
		case ROOT:
			if (root == null) {
				root = new Step(type, "$|/", "Root");
			}
			return root;
			
		case ANY:
			if (any == null) {
				any = new Step(type, "*", "Any");
			}
			return any;
			
		case ONE_OR_MORE:
			if (oneOrMore == null) {
				oneOrMore = new Step(type, "..|//", "One or more");
			}
			return oneOrMore;
			
		}

		return new Step(type);
	}
	
}
