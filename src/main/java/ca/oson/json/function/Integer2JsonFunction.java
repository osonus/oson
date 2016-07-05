package ca.oson.json.function;

@FunctionalInterface
public interface Integer2JsonFunction extends OsonFunction {
	public String apply(Integer t);
}
