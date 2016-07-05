package ca.oson.json.function;

@FunctionalInterface
public interface Long2JsonFunction extends OsonFunction {
	public String apply(Long t);
}
