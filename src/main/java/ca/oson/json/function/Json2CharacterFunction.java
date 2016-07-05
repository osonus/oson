package ca.oson.json.function;

@FunctionalInterface
public interface Json2CharacterFunction extends OsonFunction {
	public Character apply(String t);
}
