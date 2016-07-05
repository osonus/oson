package ca.oson.json.function;

@FunctionalInterface
public interface Boolean2JsonFunction extends OsonFunction {
	public String apply(Boolean t);
}
