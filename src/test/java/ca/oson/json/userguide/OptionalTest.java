package ca.oson.json.userguide;

import org.junit.Test;

import ca.oson.json.ComponentType;
import ca.oson.json.domain.Customer;
import ca.oson.json.domain.OptionalCustomer;
import ca.oson.json.domain.OptionalCustomerNoGenericType;
import ca.oson.json.domain.OptionalObject;
import ca.oson.json.support.TestCaseBase;

public class OptionalTest extends TestCaseBase {
	@Test
	public void testSerializeOptional() {
		OptionalObject obj = new OptionalObject();

		//oson.asGson(); // {"value":12,"opt":{"value":12}} //  .asJackson(); {"value":12,"opt":{"present":true}}
		//oson.pretty();
		
		String json = oson.serialize(obj);

		String expected = "{\"opt\":{\"value\":12},\"optString\":{\"value\":\"This is an optional string\"},\"value\":12}";
		
		// oson.print(obj);

		assertEquals(expected, json);
	}
	
	
	@Test
	public void testDeserializeOptional() {
		OptionalObject obj = new OptionalObject();

		//oson.asGson(); // {"value":12,"opt":{"value":12}} //  .asJackson(); {"value":12,"opt":{"present":true}}
		//oson.pretty();
		
		String json = oson.serialize(obj);
		
		ComponentType componentType = new ComponentType(OptionalObject.class, Customer.class);

		OptionalObject result = oson.deserialize(json, componentType);

//		System.err.println(obj.toString());
//		System.err.println(result.toString());

		assertEquals(obj.toString(), result.toString());
	}
	
	
	@Test
	public void testSerializeOptionalWithGenericType() {
		OptionalCustomer obj = new OptionalCustomer();

		String json = oson.serialize(obj);

		String expected = "{\"optCustomer\":{\"value\":{\"vehicles\":[{\"doors\":4,\"year\":2016,\"brand\":\"Audi\",\"years\":null},{\"doors\":4,\"year\":2016,\"brand\":\"Mercedes\",\"years\":null}],\"carList\":[{\"doors\":4,\"year\":2016,\"brand\":\"BMW\",\"years\":null},{\"doors\":4,\"year\":2016,\"brand\":\"Chevy\",\"years\":null}]}}}";
		
		// oson.print(obj);

		assertEquals(expected, json);
	}
	
	
	@Test
	public void testDeserializeOptionalWithGenericType() {
		OptionalCustomer obj = new OptionalCustomer();

		String json = oson.serialize(obj);
		
		// componentType = new ComponentType(OptionalObject.class, Customer.class);

		OptionalCustomer result = oson.deserialize(json, OptionalCustomer.class);
		
//		System.err.println(obj.toString());
//		System.err.println(result.toString());

		assertEquals(obj.toString(), result.toString());
	}
	
	
	@Test
	public void testSerializeOptionalWithNoGenericType() {
		OptionalCustomerNoGenericType obj = new OptionalCustomerNoGenericType();

		String json = oson.serialize(obj);

		String expected = "{\"optCustomer\":{\"value\":{\"vehicles\":[{\"doors\":4,\"year\":2016,\"brand\":\"Audi\",\"years\":null},{\"doors\":4,\"year\":2016,\"brand\":\"Mercedes\",\"years\":null}],\"carList\":[{\"doors\":4,\"year\":2016,\"brand\":\"BMW\",\"years\":null},{\"doors\":4,\"year\":2016,\"brand\":\"Chevy\",\"years\":null}]}}}";
		
		// oson.print(obj);

		assertEquals(expected, json);
	}
	
	
	@Test
	public void testDeserializeOptionalWithNoGenericType() {
		OptionalCustomerNoGenericType obj = new OptionalCustomerNoGenericType();

		String json = oson.clear().serialize(obj);
		
		ComponentType componentType = new ComponentType(OptionalCustomerNoGenericType.class, Customer.class);

		OptionalCustomerNoGenericType result = oson.deserialize(json, componentType);
		
//		System.err.println(obj.toString());
//		System.err.println(result.toString());

		assertEquals(obj.toString(), result.toString());
	}
	
}
