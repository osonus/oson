package ca.oson.json.function;

@FunctionalInterface
public interface Json2DoubleFunction extends OsonFunction {
	public Double apply(String t);
}
