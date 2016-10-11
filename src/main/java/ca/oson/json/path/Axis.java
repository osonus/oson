package ca.oson.json.path;

public enum Axis {
	ANCESTOR, // .... select all parent node of the context node until the root node
	ANCESTOR_OR_SELF, // ... select all parent node of the context node until the root node. also select context node to itself
	PARENT, // .. select the parent node of the context node
	SELF, // . select the context node
	CHILD, // select child of the context node
	PRECEDING_SIBLING,
	PRECEDING, // select all nodes before the context node, excluding attributes node or namespace node
	DESCENDANT_OR_SELF, // select all descendants of the context node, child in any level depth also select context node to itself
	DESCENDANT, // select all descendants of the context node, child in any level depth
	FOLLOWING, // select all nodes after the context node, excluding attributes node or namespaces node
	FOLLOWING_SIBLING, // select all following sibling of the context node
	// select none, If context node is attributes node or namespace node following sibling empty
	ATTIRBUTE, // select attributes of the context node
	NAMESPACE, // select all namespace node of the context node
	NONE;

	public String toString() {
		return this.name().toLowerCase().replaceAll("_", "-");
	}
}
