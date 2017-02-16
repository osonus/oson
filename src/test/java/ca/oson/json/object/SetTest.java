package ca.oson.json.object;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.TreeSet;
import java.util.Set;
import java.util.SortedSet;

import ca.oson.json.Oson;
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
		Oson oson = new Oson();
		
		String json = "[\"Hello World\",\"\"]";
		String expected = "[\"\",\"Hello World\"]";
		
		Set set = oson.deserialize(json, SortedSet.class);
		assertEquals(TreeSet.class, set.getClass());
		assertEquals(expected, oson.serialize(set));
	}
	
	public void testLinkedHashSetDeserialize() {
		Oson oson = new Oson();
		
		String json = "[\"Hello World\",\"\"]";

		Set set = oson.deserialize(json, LinkedHashSet.class);
		assertEquals(LinkedHashSet.class, set.getClass());
		assertEquals(json, oson.serialize(set));
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
