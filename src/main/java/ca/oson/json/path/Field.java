package ca.oson.json.path;

import java.util.List;

public class Field {
	String raw;
	
	Axis axis;// = Axis.NONE;
	Type type;
	
	String name;
	String[] names;
	List<Step> steps;
	
	public Field (String raw) {
		this.raw = raw;
	}
}
