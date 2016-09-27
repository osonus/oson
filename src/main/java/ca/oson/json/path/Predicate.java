package ca.oson.json.path;

public class Predicate <E> extends Filter {
	public Predicate(String raw) {
		super(raw);
		// TODO Auto-generated constructor stub
	}
	String field;
	Operator op;
	E value;
	
}
