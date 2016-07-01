package ca.oson.json.domain;

import ca.oson.json.FieldMapper;
import ca.oson.json.Oson.BOOLEAN;

public class AnyPoint {
    private final Point point;

    @FieldMapper(jsonCreator = BOOLEAN.TRUE)
    public AnyPoint(@FieldMapper(name = "point") Point point)
    {
      this.point = point;
    }

    @FieldMapper(ignore = BOOLEAN.TRUE)
    public String toString() {
    	return "{\"x\":" + point.x + ",\"y\":" + point.y + "}";
    }
}
