package ca.oson.json.userguide;

import org.junit.Test;

import ca.oson.json.ClassMapper;
import ca.oson.json.domain.Animal;
import ca.oson.json.domain.Dog;
import ca.oson.json.domain.Dog.BREED;
import ca.oson.json.domain.Eukaryote;
import ca.oson.json.domain.Pet;
import ca.oson.json.support.TestCaseBase;

public class DogTest extends TestCaseBase {

	@Test
	public void testSerializeClassType() {
	    Dog dog = new Dog("I am a dog", BREED.GERMAN_SHEPHERD);
	    dog.setWeight(12.5);
	    
	    String expectedDog = "{\"owner\":null,\"bread\":\"GERMAN_SHEPHERD\",\"birthDate\":null,\"name\":\"I am a dog\",\"weight\":12.5,\"age\":1}";
	    String expectedPet = "{\"owner\":null,\"weight\":12.5,\"age\":1}";
	    String expectedAnimal = "{\"weight\":12.5,\"age\":1}";

	    assertEquals(expectedDog, oson.clearAll().serialize(dog));
	    assertEquals(expectedPet, oson.serialize(dog, Pet.class));
	    assertEquals(expectedAnimal, oson.serialize(dog, Animal.class));
	    assertEquals(expectedDog, oson.serialize(dog, Eukaryote.class));
	}
	
	
	@Test
	public void testSerializeUseFieldsOnly() {
		Dog dog = new Dog("I am a dog", BREED.GERMAN_SHEPHERD);
		dog.setWeight(12.5);

		oson.useAttribute(false);
		String expected = "{\"owner\":null,\"bread\":\"GERMAN_SHEPHERD\",\"birthDate\":null,\"name\":\"I am a dog\",\"weight\":12.5}";
		assertEquals(expected, oson.serialize(dog));
		
		oson.useField(false);
		expected = "{}";
		assertEquals(expected, oson.serialize(dog));
	}
	
	@Test
	public void testSerializeASingleMethod() {
		Dog dog = new Dog("I am a dog", BREED.GERMAN_SHEPHERD);
		dog.setWeight(12.5);
		
		oson.clear().setClassMappers(new ClassMapper(Dog.class).setJsonValueFieldName("toJsonMessage"));

		String expected = "{\"name\":\"Json\"}";
		assertEquals(expected, oson.serialize(dog));

		
		oson.clear().setToStringAsSerializer(Dog.class, true);
		// oson.setToStringAsSerializer(true);

		expected = "{\"name\":\"Shepherd\"}";
		assertEquals(expected, oson.serialize(dog));
	}
	
	
	
}
