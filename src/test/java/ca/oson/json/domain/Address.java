package ca.oson.json.domain;

import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.annotation.FieldMapper;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Address {

    private int zipcode;
    private String street;

    @JsonCreator
    public Address(@JsonProperty("zipcode") int zipcode,
                   @JsonProperty("street") String street) {
        this.zipcode = zipcode;
        this.street = street;
    }

    // getters and setters

    @FieldMapper(ignore = BOOLEAN.TRUE)
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("zipcode: ")
                .append(this.zipcode).append("\n")
                .append("street: ")
                .append(this.street).append("\n");

        return stringBuilder.toString();
    }
}
