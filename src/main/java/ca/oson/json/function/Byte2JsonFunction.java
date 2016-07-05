package ca.oson.json.function;

@FunctionalInterface
public interface Byte2JsonFunction extends OsonFunction {
	public String apply(Byte t);
}
