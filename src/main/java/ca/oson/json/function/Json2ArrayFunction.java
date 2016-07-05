package ca.oson.json.function;

@FunctionalInterface
public interface Json2ArrayFunction<E> extends OsonFunction {
	public E[] apply(String t);
}
