package ca.oson.json.domain;

import java.util.Date;
import java.util.List;

import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.annotation.ClassMapper;
import ca.oson.json.Oson.MODIFIER;

@ClassMapper(propertyOrders = {"firstName","lastName"}, orderByKeyAndProperties = BOOLEAN.TRUE,
ignoreFieldsWithAnnotations = {"javax.validation.constraints.NotNull", "com.fasterxml.jackson.annotation.JsonInclude"},
includeFieldsWithModifiers = {MODIFIER.Private, MODIFIER.Protected})
public class OrderedPerson {
	private Date birthDate;
    private String firstName;
    private String title;
    private String lastName;
    private int age;
    private List<Address> addressList;
    
    @javax.validation.constraints.NotNull
    private double ignoredValue;
    @com.fasterxml.jackson.annotation.JsonInclude
    private float ignoredFloat;
    
    public char a;
}