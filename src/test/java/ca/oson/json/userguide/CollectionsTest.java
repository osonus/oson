package ca.oson.json.userguide;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;

import com.google.gson.reflect.TypeToken;

import ca.oson.json.Oson.ComponentType;
import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.domain.Car;
import ca.oson.json.support.TestCaseBase;

public class CollectionsTest extends TestCaseBase {

	@Test
	public void testSerializationCollectionInt() {
		Collection<Integer> ints = Arrays.asList(1,2,3,4,5);

		String json = oson.serialize(ints);

		String expected = "[1,2,3,4,5]";

		assertEquals(expected, json);
	}
	
	@Test
	public void testDeserializationCollectionInt() {
		String json = "[1,2,3,4,5]";
		
		List<Integer> expected = Arrays.asList(1,2,3,4,5);

		Type collectionType = new TypeToken<Collection<Integer>>(){}.getType();
		
		List<Integer> ints2 = oson.deserialize(json, collectionType);

		
		assertEquals(expected.size(), ints2.size());
		
		for (int i = 0; i < ints2.size(); i++) {
			assertEquals(expected.get(i), ints2.get(i));
		}
	}
	
	@Test
	public void testDeserializationCollectionIntComponentType() {
		String json = "[1,2,3,4,5]";
		
		List<Integer> expected = Arrays.asList(1,2,3,4,5);

		ComponentType collectionType = new ComponentType(expected.getClass(), Integer.class);
		
		List<Integer> ints2 = oson.deserialize(json, collectionType);

		
		assertEquals(expected.size(), ints2.size());
		
		for (int i = 0; i < ints2.size(); i++) {
			assertEquals(expected.get(i), ints2.get(i));
		}
	}
	
	@Test
	public void testSerializationCollectionObject() {
		List<Car> cars = Arrays.asList(new Car("Ford", 4),new Car("Chrysler", 6),new Car("Hundai", 2));

		String json = oson.setDefaultType(JSON_INCLUDE.NON_NULL).serialize(cars);

		String expected = "[{\"doors\":4,\"year\":2016,\"brand\":\"Ford\"},{\"doors\":6,\"year\":2016,\"brand\":\"Chrysler\"},{\"doors\":2,\"year\":2016,\"brand\":\"Hundai\"}]";
		
		assertEquals(expected, json);
	}
	
	@Test
	public void testDeserializationCollectionObject() {
		List<Car> expected = Arrays.asList(new Car("Ford", 4),new Car("Chrysler", 6),new Car("Hundai", 2));

		String json = "[{\"doors\":4,\"year\":2016,\"brand\":\"Ford\"},{\"doors\":6,\"year\":2016,\"brand\":\"Chrysler\"},{\"doors\":2,\"year\":2016,\"brand\":\"Hundai\"}]";
		
		ComponentType collectionType = new ComponentType(List.class, Car.class);
		
		List<Car> cars = oson.deserialize(json, collectionType);
		
		assertEquals(expected.size(), cars.size());
		
		for (int i = 0; i < cars.size(); i++) {
			Car car1 = cars.get(i);
			Car car2 = expected.get(i);
			assertEquals(car1.brand, car2.brand);
			assertEquals(car1.doors, car2.doors);
			assertEquals(car1.year, car2.year);
		}
	}
	
	@Test
	public void testSerializationCollectionObjectIncludeType() {
		List<Car> cars = Arrays.asList(new Car("Ford", 4),new Car("Chrysler", 6),new Car("Hundai", 2));

		String json = oson.setDefaultType(JSON_INCLUDE.NON_NULL).includeClassTypeInJson(true).serialize(cars);
		
		//System.err.println(json);

		String expected = "[{\"@class\":\"ca.oson.json.domain.Car\",\"doors\":4,\"year\":2016,\"brand\":\"Ford\"},{\"@class\":\"ca.oson.json.domain.Car\",\"doors\":6,\"year\":2016,\"brand\":\"Chrysler\"},{\"@class\":\"ca.oson.json.domain.Car\",\"doors\":2,\"year\":2016,\"brand\":\"Hundai\"}]";
		
		assertEquals(expected, json);
	}

	@Test
	public void testDeserializationCollectionObjectIncludeType() {
		List<Car> expected = Arrays.asList(new Car("Ford", 4),new Car("Chrysler", 6),new Car("Hundai", 2));

		String json = "[{\"@class\":\"ca.oson.json.domain.Car\",\"doors\":4,\"year\":2016,\"brand\":\"Ford\"},{\"@class\":\"ca.oson.json.domain.Car\",\"doors\":6,\"year\":2016,\"brand\":\"Chrysler\"},{\"@class\":\"ca.oson.json.domain.Car\",\"doors\":2,\"year\":2016,\"brand\":\"Hundai\"}]";
		
		List<Car> cars = oson.deserialize(json, List.class);
		
		assertEquals(expected.size(), cars.size());
		
		for (int i = 0; i < cars.size(); i++) {
			Car car1 = cars.get(i);
			Car car2 = expected.get(i);
			assertEquals(car1.brand, car2.brand);
			assertEquals(car1.doors, car2.doors);
			assertEquals(car1.year, car2.year);
		}
	}
	
	
	@Test
	public void testSerializationCollectionArbitraryObjectIncludeType() {
		List<Object> collection = new ArrayList<>();
		collection.add("hello");
		collection.add(6);
		collection.add(new Event("GREETINGS", "guest"));

		String json = oson.setDefaultType(JSON_INCLUDE.NON_NULL).includeClassTypeInJson(true).pretty(false).serialize(collection);
		
		// System.err.println(json);

		String expected = "[\"hello\",6,{\"@class\":\"ca.oson.json.userguide.Event\",\"name\":\"GREETINGS\",\"source\":\"guest\"}]";
		
		assertEquals(expected, json);
	}
	
	@Test
	public void testDeserializationCollectionArbitraryObjectIncludeType() {
		List<Object> expected = new ArrayList<>();
		expected.add("hello");
		expected.add(6);
		expected.add(new Event("GREETINGS", "guest"));
		
		String json = "[\"hello\",6,{\"@class\":\"ca.oson.json.userguide.Event\",\"name\":\"GREETINGS\",\"source\":\"guest\"}]";

		List<Object> result = oson.deserialize(json, List.class);

		assertEquals(expected.get(0), result.get(0));
		assertEquals(expected.get(1), result.get(1));
		Event event1 = (Event) expected.get(2);
		Event event2 = (Event) result.get(2);
		assertEquals(event1.name, event2.name);
		assertEquals(event1.source, event2.source);
	}
}


class Event {
	public String name;
	public String source;

	public Event(String name, String source) {
		this.name = name;
		this.source = source;
	}
}
