package ca.oson.json.domain;

import java.util.Optional;

import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.annotation.FieldMapper;

public class OptionalCustomerNoGenericType {
	private Optional optCustomer = Optional.of(new Customer());
	
	public OptionalCustomerNoGenericType () {
	}
	
    @FieldMapper(ignore = BOOLEAN.TRUE)
    public String toString() {
    	return "{\"\"optCustomer\":" + optCustomer.toString() + "}";
    }
}
