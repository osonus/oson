package ca.oson.json.function;

import java.util.Date;

@FunctionalInterface
public interface Date2JsonFunction extends OsonFunction {
	public String apply(Date t);
}
