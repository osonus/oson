package ca.oson.json.function;

@FunctionalInterface
public interface Json2BooleanFunction extends OsonFunction {
	public Boolean apply(String t);
}
