package ca.oson.json.function;

import java.util.concurrent.atomic.AtomicLong;

@FunctionalInterface
public interface Json2AtomicLongFunction extends OsonFunction {
	public AtomicLong apply(String t);
}
