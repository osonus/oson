package ca.oson.json.listarraymap;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.google.gson.reflect.TypeToken;

import ca.oson.json.domain.Car;
import ca.oson.json.support.TestCaseBase;

public class ListTest extends TestCaseBase {

   @Test
   public void testSerializeList() {
	   List<String> value = Arrays.asList("Chevrolet Malibu", "Ford Mondeo", "Toyota Camry");
	   String expected = "[\"Chevrolet Malibu\",\"Ford Mondeo\",\"Toyota Camry\"]";
	   
	   String result = oson.serialize(value);

	   assertEquals(expected, result);
   }


   @Test
   public void testDeserializeList() {
	   String value = "[\"Chevrolet Malibu\",\"Ford Mondeo\",\"Toyota Camry\"]";
	   
	   //Type type = new TypeToken<List<String>>(){}.getType();
	   List<String> result = oson.deserialize(value, List.class);
	   
	   List<String> expected = Arrays.asList("Chevrolet Malibu", "Ford Mondeo", "Toyota Camry");

	   for (int i = 0; i < expected.size(); i++) {
		   assertTrue(expected.get(i).equals(result.get(i)));
	   }
   }
}
