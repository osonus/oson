package ca.oson.json.domain;

import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.annotation.FieldMapper;

public class Point {
	double x;
	double y;

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
}