package ca.oson.json.function;

import java.util.Collection;

@FunctionalInterface
public interface Json2CollectionFunction extends OsonFunction {
	public Collection apply(Collection collection);
}
