package ca.oson.json.userguide;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.gson.reflect.TypeToken;

import ca.oson.json.Oson;
import ca.oson.json.ComponentType;
import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.domain.Car;
import ca.oson.json.domain.Customer;
import ca.oson.json.domain.Event;
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

		String expected = "[\"hello\",6,{\"@class\":\"ca.oson.json.domain.Event\",\"name\":\"GREETINGS\",\"source\":\"guest\"}]";
		
		assertEquals(expected, json);
	}
	
	@Test
	public void testDeserializationCollectionArbitraryObjectIncludeType() {
		List<Object> expected = new ArrayList<>();
		expected.add("hello");
		expected.add(6);
		expected.add(new Event("GREETINGS", "guest"));
		
		String json = "[\"hello\",6,{\"@class\":\"ca.oson.json.domain.Event\",\"name\":\"GREETINGS\",\"source\":\"guest\"}]";

		List<Object> result = oson.deserialize(json, List.class);

		assertEquals(expected.get(0), result.get(0));
		assertEquals(expected.get(1), result.get(1));
		Event event1 = (Event) expected.get(2);
		Event event2 = (Event) result.get(2);
		assertEquals(event1.name, event2.name);
		assertEquals(event1.source, event2.source);
	}
	
	
	@Test
	public void testDeserializationCollectionArbitraryObjectMultipleComponentTypes() {
		List<Object> expected = new ArrayList<>();
		expected.add(new Car("Toyota", 4));
		expected.add("hello");
		expected.add(6);
		expected.add(new Event("GREETINGS", "guest"));

		String json = "[{\"doors\":4,\"year\":2016,\"brand\":\"Toyota\",\"years\":null},\"hello\",6,{\"name\":\"GREETINGS\",\"source\":\"guest\"}]";

		ComponentType type = new ComponentType(List.class, Event.class, Car.class);
		
		List<Object> result = oson.deserialize(json, type);

		Car car1 = (Car) expected.get(0);
		Car car2 = (Car) result.get(0);
		assertEquals(car1.brand, car2.brand);
		assertEquals(car1.doors, car2.doors);
		assertEquals(expected.get(1), result.get(1));
		assertEquals(expected.get(2), result.get(2));
		Event event1 = (Event) expected.get(3);
		Event event2 = (Event) result.get(3);
		assertEquals(event1.name, event2.name);
		assertEquals(event1.source, event2.source);
	}
	
	
	@Test
	public void testDeserializationCollectionArbitraryObjectComponentType() {
		List<Object> expected = new ArrayList<>();
		expected.add("hello");
		expected.add(6);
		expected.add(new Event("GREETINGS", "guest"));
		
		String json = "[\"hello\",6,{\"name\":\"GREETINGS\",\"source\":\"guest\"}]";

		ComponentType type = new ComponentType(List.class, Event.class);
		
		List<Object> result = oson.deserialize(json, type);

		assertEquals(expected.get(0), result.get(0));
		assertEquals(expected.get(1), result.get(1));
		Event event1 = (Event) expected.get(2);
		Event event2 = (Event) result.get(2);
		assertEquals(event1.name, event2.name);
		assertEquals(event1.source, event2.source);
	}

	
	@Test
	public void testDeserializeListOfCustomers() {
		Customer customer = new Customer();
		List<Customer> expected = new ArrayList<>();
		expected.add(customer);
		
		String json = null; // oson.setDefaultType(JSON_INCLUDE.NON_NULL).serialize(list);

        json = "[{\"vehicles\":[{\"doors\":4,\"year\":2016,\"brand\":\"Audi\"},{\"doors\":4,\"year\":2016,\"brand\":\"Mercedes\"}],\"carList\":[{\"doors\":4,\"year\":2016,\"brand\":\"BMW\"},{\"doors\":4,\"year\":2016,\"brand\":\"Chevy\"}]}]";
        
        ComponentType type = new ComponentType(List.class, Customer.class);
        
        List<Customer> result = oson.deserialize(json, type);

        for (int i = 0; i < result.size(); i++) {
        	assertEquals(expected.get(i).toString(), result.get(i).toString());
        }
	}
	
	
	@Test
	public void testDeserializeListOfMultipleObjects() {
		Customer customer = new Customer();
		List<Object> expected = new ArrayList<>();
		expected.add(customer);
		Event event = new Event("GREETINGS", "guest");
		expected.add(event);
		int[][][] ints = {{{1, 2}, {3, 24}}, {{5, 6}, {7, 8}}, {{9, 10}, {11, 12}}};
		expected.add(ints);
		expected.add(999876);
		expected.add("This is a testing.");
		Car car = new Car("Ford", 4);
		expected.add(car);
		
		String json = oson.setDefaultType(JSON_INCLUDE.NON_NULL).serialize(expected);
		
		//System.err.println(json);

        json = "[{\"vehicles\":[{\"doors\":4,\"year\":2016,\"brand\":\"Audi\"},{\"doors\":4,\"year\":2016,\"brand\":\"Mercedes\"}],\"carList\":[{\"doors\":4,\"year\":2016,\"brand\":\"BMW\"},{\"doors\":4,\"year\":2016,\"brand\":\"Chevy\"}]},{\"name\":\"GREETINGS\",\"source\":\"guest\"},[[[1,2],[3,24]],[[5,6],[7,8]],[[9,10],[11,12]]],999876,\"This is a testing.\",{\"doors\":4,\"year\":2016,\"brand\":\"Ford\"}]";
        
        ComponentType type = new ComponentType(List.class, Customer.class, Event.class, Car.class, int[][][].class);
        
        List<Object> result = oson.deserialize(json, type);

        for (int i = 0; i < result.size(); i++) {
        	Object obj = result.get(i);
        	
        	if (obj instanceof Customer) {
        		Customer cobj = (Customer)obj;
        		assertEquals(cobj.toString(), customer.toString());
        		
        	} else if (obj instanceof Event) {
        		Event cEvent = (Event)obj;
            	assertEquals(cEvent.toString(), event.toString());
        		
        	} else if (obj instanceof Car) {
        		Car cCar = (Car)obj;
            	assertEquals(cCar.toString(), car.toString());
            	
        	} else if (i == 2) {
        		int[][][] ints3 = (int[][][])result.get(i);
        		int[][][] intsexpected = (int[][][])expected.get(i);
        		
        		for (int p = 0; p < ints3.length; p++) {
        			for (int j = 0; j < ints3[0].length; j++) {
        				for (int k = 0; k < ints3[0][0].length; k++) {
        					assertEquals(intsexpected[p][j][k], ints3[p][j][k]);
        				}
        			}
        		}
        		
        	} else {
        		assertEquals(expected.get(i).toString(), result.get(i).toString());
        	}
        }
	}
	
	
	
	@Test
	public void testDeserializeListOfMapListMap() {
		List<Object> expected = new ArrayList<>();

		Map<String, Object> map = new HashMap<>();
		Event event = new Event("GREETINGS", "guest");
		map.put("event", event);
		Customer customer = new Customer();
		map.put("customer", customer);
		Boolean[] bools = new Boolean[] { true, false, true };
		map.put("integer", 12345);
		map.put("string", "I am a string.");
		map.put("bools", bools);
		expected.add(map);

		int[][][] ints = { { { 1, 2 }, { 3, 24 } }, { { 5, 6 }, { 7, 8 } },
				{ { 9, 10 }, { 11, 12 } } };
		expected.add(ints);
		expected.add(999876);
		expected.add("This is a testing.");

		List<Object> list2 = new ArrayList<>();
		Car car = new Car("Ford", 4);
		list2.add(car);
		list2.add(1);
		Map<String, Object> map2 = new HashMap<>();
		Car car2 = new Car("Toyota", 2);
		map2.put("toyota", car2);
		Event event2 = new Event("HELLO", "hostess");
		map2.put("new_event", event2);
		list2.add(map2);
		expected.add(list2);

		Oson oson = new Oson();

		String json = oson.setDefaultType(JSON_INCLUDE.NON_NULL).serialize(
				expected);

		String myjson = "[{\"bools\":[true,false,true],\"string\":\"I am a string.\",\"integer\":12345,\"event\":{\"name\":\"GREETINGS\",\"source\":\"guest\"},\"customer\":{\"vehicles\":[{\"doors\":4,\"year\":2016,\"brand\":\"Audi\"},{\"doors\":4,\"year\":2016,\"brand\":\"Mercedes\"}],\"carList\":[{\"doors\":4,\"year\":2016,\"brand\":\"BMW\"},{\"doors\":4,\"year\":2016,\"brand\":\"Chevy\"}]}},[[[1,2],[3,24]],[[5,6],[7,8]],[[9,10],[11,12]]],999876,\"This is a testing.\",[{\"doors\":4,\"year\":2016,\"brand\":\"Ford\"},1,{\"toyota\":{\"doors\":2,\"year\":2016,\"brand\":\"Toyota\"},\"new_event\":{\"name\":\"HELLO\",\"source\":\"hostess\"}}]]";

		assertEquals(json, myjson);

		ComponentType type = new ComponentType(List.class, Customer.class,
				Event.class, Car.class, int[][][].class, Boolean[].class,
				HashMap.class, ArrayList.class);

		List<Object> result = oson.deserialize(myjson, type);

		for (int i = 0; i < result.size(); i++) {
			Object obj = result.get(i);
			if (i == 0) {
				Map<String, Object> mymap = (Map) obj;
				for (Map.Entry<String, Object> entry : mymap.entrySet()) {
					String key = entry.getKey();
					Object value = entry.getValue();

					if (value instanceof Event) {
						Event myevent = (Event) value;
						assertEquals(key, "event");
						assertEquals(event.toString(), myevent.toString());

					} else if (value instanceof Customer) {
						Customer mycustomer = (Customer) value;
						assertEquals(key, "customer");
						assertEquals(mycustomer.toString(), customer.toString());

					} else if (value instanceof Boolean[]) {
						Boolean[] mybools = (Boolean[]) value;
						assertEquals(key, "bools");
						String myboolstr = oson.serialize(mybools);
						String boolstr = oson.serialize(bools);
						assertEquals(myboolstr, boolstr);

					} else {
						assertEquals(value.toString(), map.get(key).toString());
					}
				}

			} else if (obj instanceof int[][][]) {
				int[][][] ints3 = (int[][][]) result.get(i);
				int[][][] intsexpected = (int[][][]) expected.get(i);

				for (int p = 0; p < ints3.length; p++) {
					for (int j = 0; j < ints3[0].length; j++) {
						for (int k = 0; k < ints3[0][0].length; k++) {
							assertEquals(intsexpected[p][j][k], ints3[p][j][k]);
						}
					}
				}

			} else if (i == 4) {
				List<Object> mylist2 = (List) obj;

				int j = 0;
				for (Object object : mylist2) {
					if (object instanceof Car) {
						Car cCar = (Car) object;
						assertEquals(cCar.toString(), car.toString());

					} else if (Map.class.isAssignableFrom(object.getClass())) {
						Map<String, Object> mymap2 = (Map) object;

						for (String key : mymap2.keySet()) {
							Object val = mymap2.get(key);

							if (obj instanceof Car) {
								Car mycar2 = (Car) val;
								assertEquals(key, "toyota");
								assertEquals(mycar2.toString(), car2.toString());

							} else if (obj instanceof Event) {
								Event myevent2 = (Event) obj;
								assertEquals(key, "new_event");
								assertEquals(myevent2.toString(),
										event2.toString());

							}
						}

					} else {
						assertEquals(object.toString(), list2.get(j).toString());
					}

					j++;
				}

			} else {
				assertEquals(expected.get(i).toString(), result.get(i)
						.toString());
			}
		}
	}

}
