package ca.oson.json.function;

import ca.oson.json.Oson.FieldData;

@FunctionalInterface
public interface Json2FieldDataFunction extends OsonFunction {
	public Object apply(FieldData fieldData);
}
