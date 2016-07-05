package ca.oson.json.function;

import java.util.concurrent.atomic.AtomicInteger;

@FunctionalInterface
public interface AtomicInteger2JsonFunction extends OsonFunction {
	public String apply(AtomicInteger t);
}
