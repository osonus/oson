package ca.oson.json.function;

@FunctionalInterface
public interface Json2EnumFunction extends OsonFunction {
	public Enum apply(String t);
}
