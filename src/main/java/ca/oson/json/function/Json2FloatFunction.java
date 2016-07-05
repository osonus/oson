package ca.oson.json.function;

@FunctionalInterface
public interface Json2FloatFunction extends OsonFunction {
	public Float apply(String t);
}
