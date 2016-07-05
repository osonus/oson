package ca.oson.json.function;

@FunctionalInterface
public interface Enum2JsonFunction extends OsonFunction {
	public String apply(Enum t);
}
