package ca.oson.json.function;

import ca.oson.json.MapData;

public interface ConvertFunction extends OsonFunction {
	public Object apply(MapData mapData);
}
