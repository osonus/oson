package ca.oson.json.domain;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.fasterxml.jackson.annotation.JsonProperty.Access;

import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.annotation.ClassMapper;
import ca.oson.json.annotation.FieldMapper;

@ClassMapper(ignoreFieldsWithAnnotations = {"javax.validation.constraints.NotNull"}, 
jsonIgnoreProperties = {"lastName"})
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "fvalue", "myInt"})
@org.codehaus.jackson.annotate.JsonIgnoreProperties({ "ch"})
public class IgnoreObject {
	transient private String title;
	volatile private Date birthDate;

    private String lastName;
	
    @FieldMapper(since=2.0)
    private int age;

    @FieldMapper(ignore=BOOLEAN.TRUE)
    private List<Address> addressList;
    
    @com.google.gson.annotations.Until(value = 1.0)
    private String firstName;
    
    private Float fvalue;
    
    private Character ch;
    
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Long longValue;
    
    @org.codehaus.jackson.annotate.JsonIgnore
    private short shortValue;
    
    @javax.persistence.Transient
    private AtomicInteger aint;
    
    @com.fasterxml.jackson.annotation.JsonProperty(access = Access.WRITE_ONLY)
    private Integer intValue;
    
    
    @org.junit.Ignore
    public byte getByte() {
    	return 16;
    }
    
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties (allowGetters = false)
    public int getMyInt() {
    	return 12;
    }
}
