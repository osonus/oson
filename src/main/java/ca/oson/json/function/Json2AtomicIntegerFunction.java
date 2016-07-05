package ca.oson.json.function;

import java.util.concurrent.atomic.AtomicInteger;

@FunctionalInterface
public interface Json2AtomicIntegerFunction extends OsonFunction {
	public AtomicInteger apply(String t);
}
