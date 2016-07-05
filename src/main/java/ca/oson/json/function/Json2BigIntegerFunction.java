package ca.oson.json.function;

import java.math.BigInteger;

@FunctionalInterface
public interface Json2BigIntegerFunction extends OsonFunction {
	public BigInteger apply(String t);
}
