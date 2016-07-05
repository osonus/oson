package ca.oson.json.function;

import java.util.Map;

@FunctionalInterface
public interface Json2MapFunction extends OsonFunction {
	public Map apply(Map t);
}