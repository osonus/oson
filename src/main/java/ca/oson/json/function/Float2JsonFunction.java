package ca.oson.json.function;

@FunctionalInterface
public interface Float2JsonFunction extends OsonFunction {
	public String apply(Float t);
}