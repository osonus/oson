package ca.oson.json.function;

@FunctionalInterface
public interface Short2JsonFunction extends OsonFunction {
	public String apply(Short t);
}