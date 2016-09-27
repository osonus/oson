package ca.oson.json.path;

public class Step {
	String raw;
	String name;
	Filter filter;
	
	public Step(String raw, String name) {
		this.raw = raw;
		this.name = name;
	}
}
