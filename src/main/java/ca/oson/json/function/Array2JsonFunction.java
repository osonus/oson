package ca.oson.json.function;

@FunctionalInterface
public interface Array2JsonFunction<E> extends OsonFunction {
	public String apply(E[] t);
}
