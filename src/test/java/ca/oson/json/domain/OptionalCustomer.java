package ca.oson.json.domain;

import java.util.Optional;

import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.annotation.FieldMapper;

public class OptionalCustomer {
	private Optional<Customer> optCustomer = Optional.of(new Customer());
	
	public OptionalCustomer () {
		
	}
	
    @FieldMapper(ignore = BOOLEAN.TRUE)
    public String toString() {
    	return "{\"\"optCustomer\":" + optCustomer.toString() + "}";
    }
}
