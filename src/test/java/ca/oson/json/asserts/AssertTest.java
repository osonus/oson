package ca.oson.json.asserts;

import org.junit.Test;

import ca.oson.json.OsonAssert;
import ca.oson.json.OsonAssert.MODE;
import ca.oson.json.domain.Car;
import ca.oson.json.support.TestCaseBase;

public class AssertTest extends TestCaseBase {

	@Test
	public void testAssertValueObjectEquals() {
		Car car = new Car();
		House house = new House();

		OsonAssert.assertEquals(car, house, MODE.VALUE);
		
		car.years = new int[]{1,9,81,45,3};
		house.many = new int[]{45,3,81,1,9};
		
		car.year = 1990;
		house.current = 1990;
				
		OsonAssert.assertEquals(car, house, MODE.VALUE);
		
		String json = oson.serialize(car);
		String expected = oson.serialize(house);
		
		OsonAssert.assertEquals(expected, json, MODE.VALUE);
	}
	
	
	@Test
	public void testAssertValueJsonEquals() {
		String json = "{\"myStyle\":\"silly\",\"myvalue\":12345,\"double\":12.3456}";
		String expected = "{\"integer_value\":12345,\"float\":12.3456,\"boy\":\"silly\"}";

		OsonAssert.assertEquals(expected, json, MODE.VALUE);
	}
	
	
}


class House {
    public String style = "Chevron";

    public int rooms = 4;

    public int current = 2016;
	
    public int[] many;
    
}