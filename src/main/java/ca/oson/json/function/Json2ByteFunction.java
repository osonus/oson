package ca.oson.json.function;

@FunctionalInterface
public interface Json2ByteFunction extends OsonFunction {
	public Byte apply(String t);
}
