package ca.oson.json.domain;

import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.annotation.FieldMapper;

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
