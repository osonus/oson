package ca.oson.json.listarraymap;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

import org.junit.Test;

import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.OsonAssert;
import ca.oson.json.OsonConvert;
import ca.oson.json.OsonAssert.MODE;
import ca.oson.json.domain.Car;
import ca.oson.json.function.Map2JsonFunction;
import ca.oson.json.support.TestCaseBase;
import ca.oson.json.util.StringUtil;
import ca.oson.json.ComponentType;

public class MapTest extends TestCaseBase {

	   @Test
	   public void testSerializeMap() {
		   Map<String, Double> value = new HashMap<>();
		   value.put("Alabama", 1529d);
		   value.put("Wyoming", 1541d);
		   value.put("Arizona", 1222d);

		   String expected = "{\"Wyoming\":1541,\"Alabama\":1529,\"Arizona\":1222}";
		   
		   String result = oson.setAppendingFloatingZero(false).serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeMapSort() {
		   Map<String, Double> value = new HashMap<>();
		   value.put("Alabama", 1529d);
		   value.put("Wyoming", 1541d);
		   value.put("Arizona", 1222d);

		   String expected = "{\"Alabama\":1529,\"Arizona\":1222,\"Wyoming\":1541}";
		   
		   String result = oson.setAppendingFloatingZero(false).sort().serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeMapWithObject() {
		   Map<String, Car> value = new HashMap<>();
		   Car mycar = new Car("Chevrolet Malibu", 4);
		   mycar.years = new int[]{1990,2001,2009};
		   value.put("Alabama", mycar);
		   value.put("Wyoming", new Car("Ford Mondeo",5));
		   value.put("Arizona", new Car("Toyota Camry",6));

		   String result = oson.serialize(value);

		   assertTrue(result.contains("\"years\":[1990,2001,2009]"));
	   }
	   
	   @Test
	   public void testSerializeMapWithDifferentObjects() {
		   Map<String, Object> value = new HashMap<>();
		   Car mycar = new Car("Chevrolet Malibu", 4);
		   mycar.years = new int[]{1990,2001,2009};
		   value.put("Alabama", mycar);
		   value.put("Wyoming", 123456789);
		   value.put("Arizona", "This is a testing");

		   String result = oson.serialize(value);

		   assertTrue(result.contains("\"Wyoming\":123456789"));
		   assertTrue(result.contains("\"Arizona\":\"This is a testing\""));
	   }
	   
	   @Test
	   public void testDeserializeMap() {
		   Map<String, Double> expected = new HashMap<>();
		   expected.put("Alabama", 1529d);
		   expected.put("Wyoming", 1541d);
		   expected.put("Arizona", 1222d);

		   String value = "{\"Wyoming\":1541,\"Alabama\":1529,\"Arizona\":1222}";
		   
		   // new ComponentType("Map<String, Double>")
		   Map<String, Number> result = oson.deserialize(value, expected.getClass());

		   OsonAssert.assertEquals(expected, result, MODE.EXACT);
	   }
	   
	   @Test
	   public void testDeserializeMapWithObject() {
		   Map<String, Car> value = new HashMap<>();
		   Car mycar = new Car("Chevrolet Malibu", 4);
		   value.put("Wyoming", new Car("Ford Mondeo",5));
		   
		   String text = "{\"Wyoming\":{\"doors\":5,\"year\":2016,\"brand\":\"Ford Mondeo\"}}";
		   
		   ComponentType type = new ComponentType(HashMap.class, Car.class);

		   Map<String, Car> result = oson.clearAll().setDefaultType(JSON_INCLUDE.NON_NULL).deserialize(text, type);

		   for (String key: result.keySet()) {
			   Object obj = result.get(key);
			   assertTrue(obj instanceof Car);
		   }
		   
		   assertTrue(result.containsKey("Wyoming"));
	   }
	   
	   
		@Test
		public void testMapToList() {
			String jsonString = "[{\"user_name\":\"Azim\",\"zip_code\":67890},{\"user_name\":\"Smith\",\"zip_code\":12345}]";

			String[][] users = oson.des(String[].class, (Object p) -> StringUtil.list2Array(((Map)p).values()))
					.deserialize(jsonString, String[][].class);
			
			String json = oson.serialize(users);
			String expected = "[[\"Azim\",\"67890\"],[\"Smith\",\"12345\"]]";
			assertEquals(expected, json);
		}
		
}
