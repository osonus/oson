package ca.oson.json.function;

@FunctionalInterface
public interface Double2JsonFunction extends OsonFunction {
	public String apply(Double t);
}