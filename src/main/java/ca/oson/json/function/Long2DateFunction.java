package ca.oson.json.function;

import java.util.Date;

@FunctionalInterface
public interface Long2DateFunction extends OsonFunction {
	public Date apply(Long t);
}
