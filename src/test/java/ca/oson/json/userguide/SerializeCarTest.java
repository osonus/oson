package ca.oson.json.userguide;

import java.util.Date;

import org.junit.Test;

import ca.oson.json.Oson;
import ca.oson.json.OsonIO;
import ca.oson.json.domain.Car;
import ca.oson.json.support.TestCaseBase;

public class SerializeCarTest extends TestCaseBase {

	@Test
	public void testSerializationCar() {
		Oson oson = new Oson();
		Car car = new Car("Chevron", 6);
		String json = oson.serialize(car);
		//System.out.println(json);

		String expected = "{\"doors\":6,\"year\":2016,\"brand\":\"Chevron\",\"years\":null}";

		assertEquals(expected, json);
	}
	
	@Test
	public void testSerializationCarOneLine() {
		String json = new Oson().serialize(new Car("Chevron", 6));

		String expected = "{\"doors\":6,\"date\":null,\"brand\":\"Chevron\"}";

		assertEquals(expected, expected);
	}
	
	@Test
	public void testSerializationCarOneLine2() {
		String json = new OsonIO().serialize(new Car("Chevron", 6));

		String expected = "{\"doors\":6,\"date\":null,\"brand\":\"Chevron\"}";

		assertEquals(expected, expected);
	}
	
	@Test
	public void testSerializationAsGson() {
		String json = oson.asGson().serialize(new Car("Chevron", 6));

		String expected = "{\"brand\":\"Chevron\",\"doors\":6,\"year\":2016,\"years\":null}";

		//System.out.println(json);
		
		assertEquals(expected, json);
	}
	
	@Test
	public void testSerializationAsJackson() {
		String json = oson.asJackson().serialize(new Car("Chevron", 6));

		String expected = "{\"brand\":\"Chevron\",\"doors\":6,\"year\":2016}";
		
		//System.out.println(json);

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