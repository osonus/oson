package ca.oson.json.function;

import ca.oson.json.DataMapper;

@FunctionalInterface
public interface Json2DataMapperFunction extends OsonFunction {
	public Object apply(DataMapper classData);
}
