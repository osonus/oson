package ca.oson.json.function;

import java.math.BigDecimal;

@FunctionalInterface
public interface Json2BigDecimalFunction extends OsonFunction {
	public BigDecimal apply(String t);
}
