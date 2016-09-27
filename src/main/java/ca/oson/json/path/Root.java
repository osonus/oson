package ca.oson.json.path;

/*
 * The root element to query. This starts all path expressions.
 * can be $ or //, depending on the xpath or jsonpath convention to use
 */
public class Root extends Step {
	private static Root step = new Root();
	
	private Root() {
		super("/", "Root");
	}
	
	public static Root getInstance() {
		return step;
	}
}
