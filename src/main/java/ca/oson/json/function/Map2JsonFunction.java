package ca.oson.json.function;

import java.util.Map;

@FunctionalInterface
public interface Map2JsonFunction extends OsonFunction {
	public String apply(Map t);
}
