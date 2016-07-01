package ca.oson.json.listarraymap;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import ca.oson.json.support.TestCaseBase;
import ca.oson.json.Oson.*;

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
   
   @Test
   public void testDeserializeListWithClassData() {
	   String value = "[\"Chevrolet Malibu\",\"Ford Mondeo\",\"Toyota Camry\"]";
	   List<String> expected = Arrays.asList("Chevrolet Malibu", "Ford Mondeo", "Toyota Camry");

	   Json2ClassDataFunction function = (ClassData p) -> expected;
	   List<String> result = oson.setClassMappers(new ClassMapper(List.class).setDeserializer(
			   function
			   ))
			   .deserialize(value, List.class);

	   assertEquals(expected, result);
   }
   
   @Test
   public void testDeserializeListWithClassData2() {
	   String value = "[\"Chevrolet Malibu\",\"Ford Mondeo\",\"Toyota Camry\"]";
	   List<String> expected = Arrays.asList("Chevrolet Malibu", "Ford Mondeo", "Toyota Camry");

	   // Json2ClassDataFunction fucntion = (ClassData p) -> (Collection)(((Collection)p.getObj()).stream().map(item -> item.toString().toUpperCase()).collect(Collectors.toList()));
	   List<String> result = oson.setClassMappers(new ClassMapper(List.class).setDeserializer(
			   (ClassData p) -> (Collection)(((Collection)p.getObj()).stream().map(item -> item.toString().toUpperCase()).collect(Collectors.toList()))
			   ))
			   .deserialize(value, List.class);

	   for (int i = 0; i < expected.size(); i++) {
		   assertTrue(expected.get(i).toUpperCase().equals(result.get(i)));
	   }
   }
   
   @Test
   public void testDeserializeListWithCollection() {
	   String value = "[\"Chevrolet Malibu\",\"Ford Mondeo\",\"Toyota Camry\"]";
	   List<String> expected = Arrays.asList("Chevrolet Malibu", "Ford Mondeo", "Toyota Camry");

	   // Json2CollectionFunction fucntion = (Collection p) -> expected;
	   List<String> result = oson.setClassMappers(new ClassMapper(List.class).setDeserializer(
			   (Collection p) -> expected
			   ))
			   .deserialize(value, List.class);

	   assertEquals(expected, result);
   }
 
   @Test
   public void testDeserializeListWithCollection2() {
	   String value = "[\"Chevrolet Malibu\",\"Ford Mondeo\",\"Toyota Camry\"]";
	   List<String> expected = Arrays.asList("Chevrolet Malibu", "Ford Mondeo", "Toyota Camry");
	   
	   Json2CollectionFunction fucntion = (Collection p) -> (Collection)p.stream().map(item -> item.toString().toUpperCase()).collect(Collectors.toList());

	   List<String> result = oson.setClassMappers(new ClassMapper(List.class).setDeserializer(
			   fucntion
			   ))
			   .deserialize(value, List.class);

	   for (int i = 0; i < expected.size(); i++) {
		   assertTrue(expected.get(i).toUpperCase().equals(result.get(i)));
	   }
   }
}
