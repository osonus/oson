package ca.oson.json.domain;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.annotation.ClassMapper;
import ca.oson.json.annotation.FieldMapper;

@ClassMapper(ignoreFieldsWithAnnotations = {"javax.validation.constraints.NotNull"}, 
jsonIgnoreProperties = {"lastName"})
@ClassMapper(jsonIgnoreProperties = { "fvalue", "myInt", "ch"})
public class IgnoreObject {
	transient private String title;
	volatile private Date birthDate;

    private String lastName;
	
    @FieldMapper(since=2.0)
    private int age;

    @FieldMapper(ignore=BOOLEAN.TRUE)
    private List<Address> addressList;
    
    @FieldMapper(until = 1.0)
    private String firstName;
    
    private Float fvalue;
    
    private Character ch;
    
    @FieldMapper(ignore = BOOLEAN.TRUE)
    private Long longValue;
    
    @FieldMapper(ignore = BOOLEAN.TRUE)
    private short shortValue;

    @FieldMapper(ignore = BOOLEAN.TRUE)
    private AtomicInteger aint;

    @FieldMapper(serialize = BOOLEAN.FALSE)
    private Integer intValue;
    
    
    @org.junit.Ignore
    public byte getByte() {
    	return 16;
    }

    @FieldMapper(useAttribute = BOOLEAN.FALSE)
    public int getMyInt() {
    	return 12;
    }
}
