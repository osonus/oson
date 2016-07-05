package ca.oson.json.function;

@FunctionalInterface
public interface Json2IntegerFunction extends OsonFunction {
	public Integer apply(String t);
}
