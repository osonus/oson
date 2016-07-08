package ca.oson.json.function;

import ca.oson.json.Oson.FieldData;

@FunctionalInterface
public interface FieldData2JsonFunction extends OsonFunction {
	public Object apply(FieldData fieldData);
}