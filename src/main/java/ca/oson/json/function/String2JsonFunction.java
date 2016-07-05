package ca.oson.json.function;

@FunctionalInterface
public interface String2JsonFunction extends OsonFunction {
	public String apply(String t);
}
