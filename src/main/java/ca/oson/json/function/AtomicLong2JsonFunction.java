package ca.oson.json.function;

import java.util.concurrent.atomic.AtomicLong;

@FunctionalInterface
public interface AtomicLong2JsonFunction extends OsonFunction {
	public String apply(AtomicLong t);
}
