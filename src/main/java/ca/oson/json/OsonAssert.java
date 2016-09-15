package ca.oson.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import junit.framework.Assert;

import org.json.JSONObject;

import ca.oson.json.Oson.FIELD_NAMING;

public class OsonAssert extends Assert {
	public static enum MODE {
		EXACT, // exact match
		NAMING, // any of the Oson.FIELD_NAMING is considered to be equivalent
		KEY_SORT, // unordered match, with NAMING: sorted by class attributes, Map keys
		LIST_SORT, // unordered match, with NAMING: sorted by list and array values
		SORTED, // unordered match, with NAMING: using both KEY_SORT and LIST_SORT
		SUBSET, // parent-child relationship, one is a subset of the other
		VALUE // ignore name: type and values matches, but ignore attribute names, ignore case
	};
	
	private static Oson oson = null;
	private static Oson oson() {
		if (oson == null) {
			oson = new Oson();
		}
		
		
		return oson;
	}
	
	private static void setUpOson(MODE mode) {
		oson().clear();
		
		switch (mode) {
		case EXACT:
			break;
			
		case NAMING:
			break;
			
		case KEY_SORT:
			oson.orderByKeyAndProperties(true);
			break;
			
		case LIST_SORT:
			oson.orderArrayAndList(true);
			break;
			
		case SORTED:
			oson.sort();
			break;
			
		case SUBSET:
			oson.sort();
			break;
			
		case VALUE:
			oson.sort();
			break;
		}
	}
	
	private static Boolean hasNulls(Object expected, Object actual) {
		if (expected == null) {
			if (actual == null) {
				return true;
			}
			assertEquals(null, expected, actual);
			
			return true;
			
		} else if (actual == null) {
			assertEquals(null, expected, actual);
			
			return true;
		}
		
		return false;
	}


	
	
	private static String getValuesOnly(Object obj) {
		if (obj == null) {
			return null;
		}
		
		List<String> values = new ArrayList<>();
		
		if (Map.class.isInstance(obj)) {
			Map map = (Map)obj;

			for (Object value : map.values()) {
				String valuesOnly = getValuesOnly(value);
				if (valuesOnly != null) {
					values.add(valuesOnly);
				}
			}
			
			Collections.sort(values);
			
			return oson.serialize(values);
			
		} else if (List.class.isInstance(obj)) {
			List list = (List)obj;

			for (Object value : list) {
				String valuesOnly = getValuesOnly(value);
				if (valuesOnly != null) {
					values.add(valuesOnly);
				}
			}
			
			Collections.sort(values);
			
			return oson.serialize(values);
			
		} else {
			return oson.serialize(obj);
		}
	}
	
	
	private static <K, E> void assertSameValue(String aJson, String bJson) {
		if (hasNulls(aJson, bJson)) {
			return;
		}
		
		if (aJson.equals(bJson)) {
			return;
		}
		
		Object a = oson.deserialize(aJson);
		Object b = oson.deserialize(bJson);
		
		if (hasNulls(a, b)) {
			return;
		}
		
		aJson = getValuesOnly(a);
		bJson = getValuesOnly(b);
		
		if (hasNulls(aJson, bJson)) {
			return;
		}
		
		if (aJson.equalsIgnoreCase(bJson)) {
			return;
		}
		
		fail(a.getClass().getSimpleName() + " and " + b.getClass().getSimpleName() + " do not have the same value.");
	}
	
	
	private static <K, E> void assertSubset(String parentJson, String childJson) {
		if (parentJson == null) {
			return;
		}
		
		if (parentJson.equals(childJson)) {
			return;
		}
		
		Object parent = oson.deserialize(parentJson);
		Object child = oson.deserialize(childJson);
		
		if (parent == null) {
			return;
		}
		
		if (Map.class.isInstance(parent)) {
			if (Map.class.isInstance(child)) {
				Map<K, E> pMap = (Map)parent;
				Map<K, E> cMap = (Map)child;
				
				for (K key: pMap.keySet()) {
					if (!cMap.containsKey(key)) {
						fail("No subset relationship for attribute " + key + " between " + parent.getClass().getSimpleName() + " and " + child.getClass().getSimpleName());
						return;
					}
					
					E pValue = pMap.get(key);
					E cValue = cMap.get(key);
					
					String pString = oson.serialize(pValue);
					String cString = oson.serialize(cValue);
					assertSubset(pString, cString);
				}
				
			} else {
				fail("No subset (Parent-Child) inheritance between " + parent.getClass().getSimpleName() + " and " + child.getClass().getSimpleName());
			}
			
		} else {
			fail("No subset (Parent-Child) inheritance between " + parent.getClass().getSimpleName() + " and " + child.getClass().getSimpleName());
		}
	}
	
	public static void assertEquals(Object expected, Object actual, MODE mode) {
		if (hasNulls(expected, actual)) {
			return;
		}
		
		setUpOson(mode);
		
		String expectedJson = oson.serialize(expected);
		String actualJson = oson.serialize(actual);
		
		if (hasNulls(expectedJson, actualJson)) {
			return;
		}
		
		Class expectedType = expected.getClass();
		Class actualType = actual.getClass();
		
		
		switch (mode) {
		case EXACT:
			if (expectedType != actualType) {
				assertEquals("Not the same class type", expectedType, actualType);
				return;
			}
			
			if (!expectedJson.equals(actualJson)) {
				assertEquals("Not the same Json string", expectedJson, actualJson);

				return;
			}
			
			break;
			
		case NAMING:
		case KEY_SORT:
		case LIST_SORT:	
		case SORTED:
			if (expectedType != actualType) {
				assertEquals("Not the same class type", expectedType, actualType);
				return;
			}
			
			if (!expectedJson.equals(actualJson)) {
				Object expectedObj = Oson.getListMapObject (expectedJson, FIELD_NAMING.UNDERSCORE_UPPER);
				Object actualObj = Oson.getListMapObject (actualJson, FIELD_NAMING.UNDERSCORE_UPPER);
				
				expectedJson = oson.serialize(expectedObj);
				actualJson = oson.serialize(actualObj);
				
				if (!expectedJson.equals(actualJson)) {
					assertEquals("Not the same Json string", expectedJson, actualJson);
				}
				
				return;
			}
			
			break;
			
		case SUBSET:
			if (!(expectedType.isAssignableFrom(actualType) || actualType.isAssignableFrom(expectedType))) {
				fail("No subset (Parent-Child) inheritance between " + expectedType.getSimpleName() + " and " + actualType.getSimpleName());
				return;
			}
			
			if (expectedType.isAssignableFrom(actualType) && expectedJson.length() <= actualJson.length()) {
				assertSubset(expectedJson, actualJson);
			} else {
				assertSubset(actualJson, expectedJson);
			}
			break;
			
		case VALUE:
			assertSameValue(expectedJson, actualJson);
		}
	}
	
	public static void assertEquals(String expectedStr, Object actual, MODE mode) {
		if (hasNulls(expectedStr, actual)) {
			return;
		}
		
		setUpOson(mode);

		assertEquals(expectedStr, oson.serialize(actual), mode);
		
	}
	
	
	public static void assertEquals(Object actual, String expectedStr, MODE mode) {
		if (hasNulls(expectedStr, actual)) {
			return;
		}
		
		setUpOson(mode);

		assertEquals(expectedStr, oson.serialize(actual), mode);
		
	}
	
	public static void assertEquals(String expectedStr, JSONObject actual, MODE mode) {
		if (hasNulls(expectedStr, actual)) {
			return;
		}
		
		assertEquals(expectedStr, actual.toString(), mode);
		
	}
	
	public static void assertEquals(JSONObject actual, String expectedStr, MODE mode) {
		if (hasNulls(expectedStr, actual)) {
			return;
		}
		
		assertEquals(expectedStr, actual.toString(), mode);
	}
	
	
	public static void assertEquals(String expectedStr, String actualStr, MODE mode) {
		if (hasNulls(expectedStr, actualStr)) {
			return;
		}
		
		setUpOson(mode);
		
		Object expected = oson.deserialize(expectedStr);
		Object actual = oson.deserialize(actualStr);
		
		assertEquals(expected, actual, mode);
	}
}
