package ca.oson.json.listarraymap;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.google.gson.reflect.TypeToken;

import ca.oson.json.ComponentType;
import ca.oson.json.domain.Car;
import ca.oson.json.support.TestCaseBase;

public class ListObjectTest extends TestCaseBase {
	   @Before 
	   public void setUp() {
		   super.setUp();
		   oson.asOson();//.asJackson();//.asGson();//
	   }
	   
	   @Test
	   public void testSerializeListOfObjects() {
		   List<Car> value = Arrays.asList(new Car("Chevrolet Malibu", 6), new Car("Ford Mondeo", 2), new Car("Toyota Camry", 4));
		   String expected = "[{\"doors\":6,\"year\":2016,\"brand\":\"Chevrolet Malibu\"},{\"doors\":2,\"year\":2016,\"brand\":\"Ford Mondeo\"},{\"doors\":4,\"year\":2016,\"brand\":\"Toyota Camry\"}]";
		   
		   String result = oson.serialize(value);
		   
//		   System.out.println("testSerializeListOfObjects:");
//		   System.out.println(result);

		   assertTrue(result.contains("\"brand\":\"Ford Mondeo\""));
	   }
	   
	   @Test
	   public void testSerializeListOfObjectsAttributesOrdered() {
		   List<Car> value = Arrays.asList(new Car("Chevrolet Malibu", 6), new Car("Ford Mondeo", 2), new Car("Toyota Camry", 4));
		   String expected = "[{\"brand\":\"Chevrolet Malibu\",\"doors\":6,\"year\":2016,\"years\":null},{\"brand\":\"Ford Mondeo\",\"doors\":2,\"year\":2016,\"years\":null},{\"brand\":\"Toyota Camry\",\"doors\":4,\"year\":2016,\"years\":null}]";
		   
		   String result = oson.sort().serialize(value);
		   
//		   System.out.println("testSerializeListOfObjectsAttributesOrdered:");
//		   System.out.println(result);

//		   assertTrue(result.contains("\"brand\":\"Chevrolet Malibu\""));
		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeListOfObjects() {
		   String value = "[{\"doors\":6,\"year\":2016,\"brand\":\"Chevrolet Malibu\"},{\"doors\":2,\"year\":2016,\"brand\":\"Ford Mondeo\"},{\"doors\":4,\"year\":2016,\"brand\":\"Toyota Camry\"}]";
		   List<Car> expected = Arrays.asList(new Car("Chevrolet Malibu", 6), new Car("Ford Mondeo", 2), new Car("Toyota Camry", 4));

		   Type type = new TypeToken<List<Car>>(){}.getType();
		   
		   List<Car> result = oson.deserialize(value, type);

		   for (int i = 0; i < expected.size(); i++) {
			   assertTrue((expected.get(i).brand).equals((result.get(i)).brand));
			   //assertEquals(expected.get(i).doors, result.get(i).doors);
		   }
	   }
	   
	   
	   
	   @Test
	   public void testDeserializeListOfObjectsWithComponentType() {
		   String value = "[{\"doors\":6,\"year\":2016,\"brand\":\"Chevrolet Malibu\"},{\"doors\":2,\"year\":2016,\"brand\":\"Ford Mondeo\"},{\"doors\":4,\"year\":2016,\"brand\":\"Toyota Camry\"}]";
		   List<Car> expected = Arrays.asList(new Car("Chevrolet Malibu", 6), new Car("Ford Mondeo", 2), new Car("Toyota Camry", 4));

		   ComponentType ComponentType = new ComponentType("java.util.List<ca.oson.json.domain.Car>");
		   
		   List<Car> result = oson.deserialize(value, ComponentType);

		   for (int i = 0; i < expected.size(); i++) {
			   assertTrue((expected.get(i).brand).equals((result.get(i)).brand));
			   //assertEquals(expected.get(i).doors, result.get(i).doors);
		   }
	   }

	   @Test
	   public void testDeserializeListOfObjectsWithComponentType2() {
		   String value = "[{\"doors\":6,\"year\":2016,\"brand\":\"Chevrolet Malibu\"},{\"doors\":2,\"year\":2016,\"brand\":\"Ford Mondeo\"},{\"doors\":4,\"year\":2016,\"brand\":\"Toyota Camry\"}]";
		   List<Car> expected = Arrays.asList(new Car("Chevrolet Malibu", 6), new Car("Ford Mondeo", 2), new Car("Toyota Camry", 4));

		   ComponentType ComponentType = new ComponentType(List.class, Car.class);
		   
		   List<Car> result = oson.deserialize(value, ComponentType);

		   for (int i = 0; i < expected.size(); i++) {
			   assertTrue((expected.get(i).brand).equals((result.get(i)).brand));
			   //assertEquals(expected.get(i).doors, result.get(i).doors);
		   }
	   }
	   
	   @Test
	   public void testDeserializeListOfObjectsWithComponentType3() {
		   String value = "[{\"doors\":6,\"year\":2016,\"brand\":\"Chevrolet Malibu\"},{\"doors\":2,\"year\":2016,\"brand\":\"Ford Mondeo\"},{\"doors\":4,\"year\":2016,\"brand\":\"Toyota Camry\"}]";
		   List<Car> expected = Arrays.asList(new Car("Chevrolet Malibu", 6), new Car("Ford Mondeo", 2), new Car("Toyota Camry", 4));

		   Type type = new TypeToken<List<Car>>(){}.getType();
		   ComponentType ComponentType = new ComponentType(type);
		   
		   List<Car> result = oson.deserialize(value, ComponentType);

		   for (int i = 0; i < expected.size(); i++) {
			   assertTrue((expected.get(i).brand).equals((result.get(i)).brand));
			   //assertEquals(expected.get(i).doors, result.get(i).doors);
		   }
	   }
}
