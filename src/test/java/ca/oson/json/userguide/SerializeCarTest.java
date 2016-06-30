package ca.oson.json.userguide;

import java.util.Date;

import org.junit.Test;

import ca.oson.json.Oson;
import ca.oson.json.support.TestCaseBase;

public class SerializeCarTest extends TestCaseBase {

	@Test
	public void testSerializationCar() {
		Oson oson = new Oson();
		Car car = new Car("Chevron", 6);
		String json = oson.serialize(car);
		//System.out.println(json);

		String expected = "{\"doors\":6,\"date\":null,\"brand\":\"Chevron\"}";

		assertEquals(expected, json);
	}
	
	@Test
	public void testSerializationCarOneLine() {
		String json = new Oson().serialize(new Car("Chevron", 6));

		String expected = "{\"doors\":6,\"date\":null,\"brand\":\"Chevron\"}";

		assertEquals(expected, expected);
	}
	
	@Test
	public void testSerializationAsGson() {
		String json = oson.asGson().serialize(new Car("Chevron", 6));

		String expected = "{\"brand\":\"Chevron\",\"doors\":6,\"date\":null}";

		//System.out.println(json);
		
		assertEquals(expected, json);
	}
	
	@Test
	public void testSerializationAsJackson() {
		String json = oson.asJackson().serialize(new Car("Chevron", 6));

		String expected = "{\"brand\":\"Chevron\"}";
		
		System.out.println(json);

		assertEquals(expected, json);
	}
	
	@Test
	public void testSerializationCarOneLinetoJson() {
		String json = oson.toJson(new Car("Chevron", 6));

		String expected = "{\"doors\":6,\"date\":null,\"brand\":\"Chevron\"}";

		assertEquals(expected, expected);
	}
	
	@Test
	public void testSerializationCarOneLinewriteValueAsString() {
		String json = oson.writeValueAsString(new Car("Chevron", 6));

		String expected = "{\"doors\":6,\"date\":null,\"brand\":\"Chevron\"}";

		assertEquals(expected, expected);
	}
}


class Car {
    String brand;
    public String getBrand() {
		return brand;
	}

	int doors = 4;
    Date date;

    public Car() {}
    
    public Car(String brand, int doors) {
        this.brand = brand;
        this.doors = doors;
    }
}