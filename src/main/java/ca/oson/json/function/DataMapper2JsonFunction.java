package ca.oson.json.function;

import ca.oson.json.DataMapper;

@FunctionalInterface
public interface DataMapper2JsonFunction extends OsonFunction {
	public String apply(DataMapper classData);
}
