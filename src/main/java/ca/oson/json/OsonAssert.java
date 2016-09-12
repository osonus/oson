package ca.oson.json;

import junit.framework.Assert;

import org.json.JSONObject;

public class OsonAssert extends Assert {
	public static enum MODE {
		EXACT, // exact match
		ORDERED, // ordered match: class attributes, Map keys, and List and Array values are sorted
		VALUE, // ignore name: type and values matches, but ignore attribute names
		SUBSET // parent-child relationship, one is a subset of the other
	};
	
	private static Oson oson = null;
	private static Oson oson() {
		if (oson == null) {
			oson = new Oson();
		}
		
		return oson;
	}

	public static void assertEquals(String expectedStr, JSONObject actual, MODE mode) {
		if (actual == null) {
			if (actual == null) {
				return;
			}
			assertEquals(null, expectedStr, actual);
			
			return;
		}
		
		assertEquals(expectedStr, actual.toString(), mode);
		
	}
	
	public static void assertEquals(String expectedStr, String actual, MODE mode) {
		Object expected = oson().deserialize(expectedStr);
		Object obj = oson().deserialize(actual.toString());
		
		
		
	}
}
