package ca.oson.json.path;

public enum Axis {
	ANCESTOR, // ....
	ANCESTOR_OR_SELF, // ...
	PARENT, // ..
	SELF, // .
	CHILD, // 
	PRECEDING_SIBLING,
	PRECEDING,
	DESCENDANT_OR_SELF,
	DESCENDANT,
	FOLLOWING,
	FOLLOWING_SIBLING,
	ATTIRBUTE,
	NAMESPACE,
	NONE;

	public String toString() {
		return this.name().toLowerCase().replaceAll("_", "-");
	}
}
