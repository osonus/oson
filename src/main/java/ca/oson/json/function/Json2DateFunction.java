package ca.oson.json.function;

import java.util.Date;

@FunctionalInterface
public interface Json2DateFunction extends OsonFunction {
	public Date apply(String t);
}
