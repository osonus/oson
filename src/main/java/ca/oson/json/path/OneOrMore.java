package ca.oson.json.path;

/*
 * Any, recursive descent, or Deep scan. Available anywhere a name is required.
 */
public class OneOrMore extends Step {
	private static OneOrMore step = new OneOrMore();
	
	private OneOrMore() {
		super("//", "OneOrMore");
	}
	
	public static OneOrMore getInstance() {
		return step;
	}
}
