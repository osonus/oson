package ca.oson.json.asserts;

import org.junit.Test;

import ca.oson.json.Oson.FIELD_NAMING;
import ca.oson.json.OsonAssert;
import ca.oson.json.OsonAssert.MODE;
import ca.oson.json.domain.Car;
import ca.oson.json.domain.Point;
import ca.oson.json.support.TestCaseBase;

public class AssertTest extends TestCaseBase {
	@Test
	public void testAssertEqualsExact() {
		Point point = new Point(123.4567, 3456);
		
		String json = oson.setAppendingFloatingZero(true).serialize(point);
		
		String json2 = oson.setScale(5).serialize(point);
		
		OsonAssert.assertNotSame(json, json2);

		OsonAssert.assertEquals(json, json2, MODE.EXACT);
	}

	
	@Test
	public void testAssertEqualsNAMING() {
		House house = new House();
		
		String json = oson.serialize(house);

		oson.setFieldNaming(FIELD_NAMING.DASH_UPPER_CAMELCASE);
		String json2 = oson.serialize(house);
		
		OsonAssert.assertNotSame(json, json2);

		OsonAssert.assertEquals(json, json2, MODE.NAMING);
	}
	
	
	@Test
	public void testAssertEqualsKEY_SORT() {
		House house = new House();
		
		String json = oson.serialize(house);

		oson.setFieldNaming(FIELD_NAMING.SPACE_LOWER);
		String json2 = oson.asGson().serialize(house);
		
		OsonAssert.assertNotSame(json, json2);

		// System.err.println(json + "\n" + json2);
		OsonAssert.assertEquals(json, json2, MODE.KEY_SORT);
	}
	
	@Test
	public void testAssertEqualsLIST_SORT() {
		House house = new House();
		house.many = new int[]{77,45,12,98,65};
		House house2 = new House();
		house2.many = new int[]{12,98,45,65,77};

		String json = oson.serialize(house);

		oson.setFieldNaming(FIELD_NAMING.UNDERSCORE_LOWER);
		String json2 = oson.serialize(house2);
		
		OsonAssert.assertNotSame(json, json2);

		//System.err.println(json + "\n" + json2);
		OsonAssert.assertEquals(json, json2, MODE.LIST_SORT);
	}

	@Test
	public void testAssertEqualsSORT() {
		House house = new House();
		house.many = new int[]{77,45,12,98,65};
		House house2 = new House();
		house2.many = new int[]{12,98,45,65,77};

		String json = oson.serialize(house);

		oson.setFieldNaming(FIELD_NAMING.DASH_UPPER);
		String json2 = oson.asGson().serialize(house2);
		
		OsonAssert.assertNotSame(json, json2);

		//System.err.println(json + "\n" + json2);
		OsonAssert.assertEquals(json, json2, MODE.SORTED);
	}

	
	@Test
	public void testAssertEqualsVALUE() {
		Point point = new Point(123.4567, 3456);
		
		MyPoint mypoint = new MyPoint(123.4567, 3456);
		
		String json = oson.setAppendingFloatingZero(true).serialize(point);
		
		String json2 = oson.setScale(6).serialize(mypoint);
		
		OsonAssert.assertNotSame(json, json2);

		OsonAssert.assertEquals(json, json2, MODE.VALUE);
	}

	
	
	
	@Test
	public void testAssertValueObjectEquals() {
		Car car = new Car();
		House house = new House();
		
		OsonAssert.assertNotSame(car, house);

		OsonAssert.assertEquals(car, house, MODE.VALUE);
		
		car.years = new int[]{1,9,81,45,3};
		house.many = new int[]{45,3,81,1,9};
		
		car.year = 1990;
		house.current = 1990;
		
		OsonAssert.assertNotSame(car, house);
				
		OsonAssert.assertEquals(car, house, MODE.VALUE);
		
		String json = oson.serialize(car);
		String expected = oson.serialize(house);
		
		OsonAssert.assertNotSame(car, house);
		
		OsonAssert.assertEquals(expected, json, MODE.VALUE);
	}
	
	
	@Test
	public void testAssertValueJsonEquals() {
		String json = "{\"myStyle\":\"silly\",\"myvalue\":12345,\"double\":12.3456}";
		String expected = "{\"integer_value\":12345,\"float\":12.3456,\"boy\":\"silly\"}";
		
		OsonAssert.assertNotSame(expected, json);

		OsonAssert.assertEquals(expected, json, MODE.VALUE);
	}
	
	
}


class House {
    public String style = "Chevron";

    public int roomNumbers = 4;

    public int current = 2016;
	
    public int[] many;
    
}


class MyPoint {
	double a;
	double b;

	public MyPoint(double a, double b) {
		this.a = a;
		this.b = b;
	}
}