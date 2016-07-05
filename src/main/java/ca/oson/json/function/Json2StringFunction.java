package ca.oson.json.function;

@FunctionalInterface
public interface Json2StringFunction extends OsonFunction {
	public String apply(String t);
}
