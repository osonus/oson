package ca.oson.json.domain;

import java.util.Optional;

import ca.oson.json.FieldMapper;
import ca.oson.json.Oson.BOOLEAN;

public class OptionalCustomer {
	private Optional<Customer> optCustomer = Optional.of(new Customer());
	
	public OptionalCustomer () {
		
	}
	
    @FieldMapper(ignore = BOOLEAN.TRUE)
    public String toString() {
    	return "{\"\"optCustomer\":" + optCustomer.toString() + "}";
    }
}
