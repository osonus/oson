package ca.oson.json.function;

@FunctionalInterface
public interface Json2LongFunction extends OsonFunction {
	public Long apply(String t);
}
