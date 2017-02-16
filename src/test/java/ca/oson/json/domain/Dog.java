package ca.oson.json.domain;

import java.util.Date;

import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.annotation.FieldMapper;


public class Dog extends Pet {
	private String name;
	private BREED bread;
	private Date birthDate;
	
	private String someField_name;

	@FieldMapper(ignore = BOOLEAN.TRUE, name="Jackson name")
	private String mySpecial_field_name;

	@FieldMapper(ignore = BOOLEAN.TRUE, name="Oson name overwrites names from external sources")
	private String special_field_name;
	
	
	public static enum BREED {
		LABRADOR_RETRIEVER,
		GERMAN_SHEPHERD,
		BULLDOG
	};
	
	
	public Dog (String name) {
		this.name = name;
	}
	
	public Dog (String name, BREED bread) {
		this.name = name;
		this.bread = bread;
	}
	
	public void bark() {
		System.out.println("Woof!");
	}
	
	public void move() {
		System.out.println("Running");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public BREED getBread() {
		return bread;
	}

	public void setBread(BREED bread) {
		this.bread = bread;
	}

	public Date getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(Date birthDate) {
		this.birthDate = birthDate;
	}
	
	@FieldMapper(ignore = BOOLEAN.TRUE)
	public String toString() {
		return "{\"name\":\"Shepherd\"}";
		//return "{\"owner\":" + getOwner() + ",\"bread\":\"" + getBread() + "\",\"birthDate\":" + getBirthDate() + ",\"name\":\"" + getName() + "\",\"weight\":" + weight + ",\"age\":" + getAge() + "}";
	}
	
	@FieldMapper(ignore = BOOLEAN.TRUE)
	// @FieldMapper(jsonValue = BOOLEAN.TRUE)
	public String toJsonMessage() {
		return "{\"name\":\"Json\"}";
	}

	@FieldMapper(ignore = BOOLEAN.TRUE)
	public String getSomeField_name() {
		return someField_name;
	}

	public void setSomeField_name(String someField_name) {
		this.someField_name = someField_name;
	}
}
