package ca.oson.json.listarraymap;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.google.gson.reflect.TypeToken;

import ca.oson.json.domain.Car;
import ca.oson.json.support.TestCaseBase;

public class ListObjectTest extends TestCaseBase {
	   
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
		   String expected = "[{\"brand\":\"Chevrolet Malibu\",\"doors\":6,\"year\":2016},{\"brand\":\"Ford Mondeo\",\"doors\":2,\"year\":2016},{\"brand\":\"Toyota Camry\",\"doors\":4,\"year\":2016}]";
		   
		   String result = oson.orderByKeyAndProperties(true).serialize(value);
		   
//		   System.out.println("testSerializeListOfObjectsAttributesOrdered:");
//		   System.out.println(result);

		   assertTrue(result.contains("\"brand\":\"Chevrolet Malibu\""));
	   }
	   
	   @Test
	   public void testDeserializeListOfObjects() {
		   String value = "[{\"doors\":6,\"year\":2016,\"brand\":\"Chevrolet Malibu\"},{\"doors\":2,\"year\":2016,\"brand\":\"Ford Mondeo\"},{\"doors\":4,\"year\":2016,\"brand\":\"Toyota Camry\"}]";
		   List<Car> expected = Arrays.asList(new Car("Chevrolet Malibu", 6), new Car("Ford Mondeo", 2), new Car("Toyota Camry", 4));

		   Type type = new TypeToken<List<Car>>(){}.getType();
		   
		   List<Car> result = oson.deserialize(value, type);

		   for (int i = 0; i < expected.size(); i++) {
			   Car car1 = expected.get(i);
			   Car car2 = result.get(i);
			   assertTrue((car1.brand).equals((car2).brand));
			   //assertEquals(expected.get(i).doors, result.get(i).doors);
		   }
	   }

}
