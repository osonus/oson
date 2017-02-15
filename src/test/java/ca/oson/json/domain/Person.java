package ca.oson.json.domain;

import java.util.List;

import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.annotation.FieldMapper;

public class Person {

    private String name;
    private String lastName;
    private int age;
    private List<Address> addressList;

    @FieldMapper(jsonCreator = BOOLEAN.TRUE)
    public Person(@FieldMapper(name="name") String name,
    		@FieldMapper(name="lastName") String lastName,
    		@FieldMapper(name="age") int age,
    		@FieldMapper(name="addressList") List<Address> addressList) {
        this.name = name;
        this.lastName = lastName;
        this.age = age;
        this.addressList = addressList;
    }

    // getters and setters

    @Override
    @FieldMapper(ignore = BOOLEAN.TRUE)
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("name: ")
                .append(this.name).append("\n")
                .append("lastName: ")
                .append(this.lastName).append("\n")
                .append("age: ")
                .append(this.age).append("\n");

        for (Address address: this.addressList) {
            stringBuilder.append(address.toString());
        }

        return stringBuilder.toString();
    }
}
