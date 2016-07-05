package ca.oson.json.function;

import java.math.BigInteger;

@FunctionalInterface
public interface BigInteger2JsonFunction extends OsonFunction {
	public String apply(BigInteger t);
}
