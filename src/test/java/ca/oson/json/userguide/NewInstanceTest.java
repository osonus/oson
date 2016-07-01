package ca.oson.json.userguide;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;

import ca.oson.json.FieldMapper;
import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.domain.Address;
import ca.oson.json.domain.Customer;
import ca.oson.json.domain.Person;
import ca.oson.json.support.TestCaseBase;

public class NewInstanceTest extends TestCaseBase {

	@Test
	public void testSerializeAnyBean() {
		//oson.asGson();
		
		AnyBean anyBean = new AnyBean("Any Name", 35);
		anyBean.setType("Java");

		String json = oson.serialize(anyBean);

		String expected = "{\"name\":\"Any Name\",\"type\":\"Java\",\"age\":35}";

		assertEquals(expected, json);
	}
	
	@Test
	public void testDeserializeAnyBean() {
		//oson.asGson();
		
		AnyBean expected = new AnyBean("Any Name", 35);
		expected.setType("Java");

		String json = "{\"name\":\"Any Name\",\"type\":\"Java\",\"age\":35}";
		
		AnyBean result = oson.deserialize(json, AnyBean.class);

		assertEquals(expected.toString(), result.toString());
	}
	
	
	@Test
	public void testSerializeAnyPoint() {
		//oson.asGson();
		Point point = new Point(12.5d, 35.5d);
		
		AnyPoint anyPoint = new AnyPoint(point);

		String json = oson.serialize(anyPoint);
		
		// System.err.println(json);

		String expected = "{\"point\":{\"x\":12.5,\"y\":35.5}}";

		assertEquals(expected, json);
	}
	
	
	@Test
	public void testDeserializeAnyPoint() {
		//oson.asGson();
		Point point = new Point(12.5d, 35.5d);
		
		AnyPoint expected = new AnyPoint(point);

		String json = "{\"point\":{\"x\":12.5,\"y\":35.5}}";
		
		AnyPoint result = oson.deserialize(json, AnyPoint.class);

		assertEquals(expected.toString(), result.toString());
	}
	
	@Test
	public void testSerializePerson() {
        Address homeAddress = new Address(12345, "Stenhammer Drive");
        Address workAddress = new Address(7986, "Market Street");
        List<Address> addressList = new ArrayList<>();
        addressList.add(homeAddress);
        addressList.add(workAddress);
        Person person = new Person("Sawyer", "Bootstrapper", 23, addressList);
        
        String expected = "{\"addressList\":[{\"zipcode\":12345,\"street\":\"Stenhammer Drive\"},{\"zipcode\":7986,\"street\":\"Market Street\"}],\"name\":\"Sawyer\",\"age\":23,\"lastName\":\"Bootstrapper\"}";

        String result = oson.pretty(false).writeValueAsString(person);

        assertEquals(expected, result);
	}
	
	@Test
	public void testDeserializePerson() {
        Address homeAddress = new Address(12345, "Stenhammer Drive");
        Address workAddress = new Address(7986, "Market Street");
        List<Address> addressList = new ArrayList<>();
        addressList.add(homeAddress);
        addressList.add(workAddress);
        Person expected = new Person("Sawyer", "Bootstrapper", 23, addressList);
        
		String jsonValue = "{\"name\":\"Sawyer\",\"lastName\":\"Bootstrapper\",\"age\":23,\"addressList\":[{\"zipcode\":12345,\"street\":" +
	            "\"Stenhammer Drive\"},{\"zipcode\":7986,\"street\":\"Market Street\"}]}";

		Person result = oson.readValue(jsonValue, Person.class);

		assertEquals(expected.toString(), result.toString());
	}
	
	
	@Test
	public void testSerializeCustomer() {
		Customer customer = new Customer();
        
        String expected = "{\"vehicles\":[{\"doors\":4,\"year\":2016,\"brand\":\"Audi\"},{\"doors\":4,\"year\":2016,\"brand\":\"Mercedes\"}],\"carList\":[{\"doors\":4,\"year\":2016,\"brand\":\"BMW\"},{\"doors\":4,\"year\":2016,\"brand\":\"Chevy\"}]}";
        
        String result = oson.pretty(false).setDefaultType(JSON_INCLUDE.NON_NULL).writeValueAsString(customer);
        
        // System.out.println(result);

        assertEquals(expected, result);
	}
	
	
	@Test
	public void testDeserializeCustomer() {
		Customer expected = new Customer();
        
        String json = "{\"vehicles\":[{\"doors\":4,\"year\":2016,\"brand\":\"Audi\"},{\"doors\":4,\"year\":2016,\"brand\":\"Mercedes\"}],\"carList\":[{\"doors\":4,\"year\":2016,\"brand\":\"BMW\"},{\"doors\":4,\"year\":2016,\"brand\":\"Chevy\"}]}";
        
        Customer result = oson.deserialize(json, Customer.class);

        assertEquals(expected.toString(), result.toString());
	}
	
}


class AnyBean {
    private final String name;
    private final int age;
    private String type;

    //public AnyBean(@JsonProperty("name") String name, @JsonProperty("age") int age)
    public AnyBean(String name, int age)
    {
      this.name = name;
      this.age = age;
    }

    public void setType(String type) {
      this.type = type;
    }
    
    @FieldMapper(ignore = BOOLEAN.TRUE)
    public String toString() {
    	return "{\"name\":\"" + name + "\",\"type\":\"" + type + "\",\"age\":" + age + "}";
    }
  }


class AnyPoint {
    private final Point point;

    public AnyPoint(Point point)
    {
      this.point = point;
    }

    @FieldMapper(ignore = BOOLEAN.TRUE)
    public String toString() {
    	return "{\"x\":" + point.x + ",\"y\":" + point.y + "}";
    }
}

class Point {
	double x;
	double y;

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
}