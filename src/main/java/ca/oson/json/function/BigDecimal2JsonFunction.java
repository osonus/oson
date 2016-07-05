package ca.oson.json.function;

import java.math.BigDecimal;

@FunctionalInterface
public interface BigDecimal2JsonFunction extends OsonFunction {
	public String apply(BigDecimal t);
}
