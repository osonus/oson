package ca.oson.json.function;

import java.util.Collection;

@FunctionalInterface
public interface Collection2JsonFunction extends OsonFunction {
	public String apply(Collection collection);
}