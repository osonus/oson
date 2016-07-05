package ca.oson.json.function;

@FunctionalInterface
public interface Character2JsonFunction extends OsonFunction {
	public String apply(Character t);
}
