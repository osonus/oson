package ca.oson.json.object;

import java.util.HashSet;
import java.util.TreeSet;

import java.util.Set;
import ca.oson.json.support.TestCaseBase;

public class SetTest extends TestCaseBase {

	public void testTreeSetSerialize() {
		TreeSet<Object> set = new TreeSet<>();
		
		set.add("123");
		set.add("This is a test");

		String json = oson.serialize(set);

		assertEquals("[\"123\",\"This is a test\"]", json);
	}

	public void testTreeSetDeserialize() {
		String json = "[\"123\",\"This is a test\"]";
		
		Set set = oson.deserialize(json, TreeSet.class);

		assertEquals(TreeSet.class, set.getClass());

		assertEquals("[123, This is a test]", set.toString());
	}
	
	public void testHashSetSerialize() {
		HashSet<Object> set = new HashSet<>();
		
		set.add("123");
		set.add("This is a test");

		String json = oson.serialize(set);

		assertEquals("[\"123\",\"This is a test\"]", json);
	}

	public void testHashSetDeserialize() {
		String json = "[\"123\",\"This is a test\"]";
		
		Set set = oson.deserialize(json, HashSet.class);
		
		//System.err.println(set);
		assertEquals(HashSet.class, set.getClass());

		assertEquals("[123, This is a test]", set.toString());
	}
}
