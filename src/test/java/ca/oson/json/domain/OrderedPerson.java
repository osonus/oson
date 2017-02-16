package ca.oson.json.domain;

import java.util.Date;
import java.util.List;

import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.annotation.ClassMapper;
import ca.oson.json.annotation.FieldMapper;
import ca.oson.json.Oson.MODIFIER;

@ClassMapper(propertyOrders = {"firstName","lastName"}, orderByKeyAndProperties = BOOLEAN.TRUE,
includeFieldsWithModifiers = {MODIFIER.Private, MODIFIER.Protected})
public class OrderedPerson {
	private Date birthDate;
    private String firstName;
    private String title;
    private String lastName;
    private int age;
    private List<Address> addressList;

    @FieldMapper(ignore = BOOLEAN.TRUE)
    private double ignoredValue;

    @FieldMapper(defaultType = JSON_INCLUDE.NON_DEFAULT)
    private float ignoredFloat;
    
    public char a;
}