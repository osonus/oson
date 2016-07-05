package ca.oson.json.function;

import java.util.Date;

@FunctionalInterface
public interface Date2LongFunction extends OsonFunction {
	public Long apply(Date t);
}
