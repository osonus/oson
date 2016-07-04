package ca.oson.json.domain;

import java.util.Optional;

import ca.oson.json.FieldMapper;
import ca.oson.json.Oson.BOOLEAN;

public class OptionalObject {
	private Integer value = 12;
	private Optional opt = Optional.of(value);
	private Optional optString = Optional.of("This is an optional string");

	public OptionalObject() {
		// no-args constructor
	}
	
	public Integer getValue() {
		return value;
	}

	public void setValue(Integer value) {
		this.value = value;
	}

	public Optional getOpt() {
		return opt;
	}

	public void setOpt(Optional opt) {
		this.opt = opt;
	}

    @FieldMapper(ignore = BOOLEAN.TRUE)
    public String toString() {
    	return "{\"value\":\"" + value + "\",\"opt\":\"" + opt.toString() + 
    			"\",\"optString\":" + optString.toString() + "}";
    }
}