package ca.oson.json.function;

import java.util.function.Function;

public interface OsonFunction extends Function {
	@Override
	public default Object apply(Object t) {
		return t;
	}
}
