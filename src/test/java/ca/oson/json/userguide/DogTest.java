package ca.oson.json.userguide;

import org.junit.Test;

import com.google.gson.Gson;

import ca.oson.json.ClassMapper;
import ca.oson.json.FieldMapper;
import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.Oson.FIELD_NAMING;
import ca.oson.json.domain.Animal;
import ca.oson.json.domain.Dog;
import ca.oson.json.domain.Dog.BREED;
import ca.oson.json.domain.Eukaryote;
import ca.oson.json.domain.Pet;
import ca.oson.json.support.TestCaseBase;

public class DogTest extends TestCaseBase {
	private Dog dog;
	
	  @Override
	  protected void setUp() {
	    super.setUp();
	    dog = new Dog("I am a dog", BREED.GERMAN_SHEPHERD);
	    dog.setWeight(12.5);
	  }

	@Test
	public void testSerializeClassType() {
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
		oson.clear().useAttribute(false);
		String expected = "{\"owner\":null,\"bread\":\"GERMAN_SHEPHERD\",\"birthDate\":null,\"name\":\"I am a dog\",\"weight\":12.5}";
		assertEquals(expected, oson.serialize(dog));
		
		oson.useField(false);
		expected = "{}";
		assertEquals(expected, oson.serialize(dog));
	}
	
	@Test
	public void testSerializeASingleMethod() {
		oson.clear().setClassMappers(new ClassMapper(Dog.class).setJsonValueFieldName("toJsonMessage"));

		String expected = "{\"name\":\"Json\"}";
		assertEquals(expected, oson.serialize(dog));

		
		oson.clear().setToStringAsSerializer(Dog.class, true);
		// oson.setToStringAsSerializer(true);

		expected = "{\"name\":\"Shepherd\"}";
		assertEquals(expected, oson.serialize(dog));
	}
	
	@Test
	public void testSerializeSetFieldNaming() {
		oson.clear().setFieldMappers(new FieldMapper("someField_name", Dog.class).setIgnore(false)).setFieldNaming(FIELD_NAMING.CAMELCASE);
		assertTrue(oson.serialize(dog).contains("\"someFieldName\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.UPPER_CAMELCASE).serialize(dog).contains("\"SomeFieldName\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.UNDERSCORE_CAMELCASE).serialize(dog).contains("\"some_Field_Name\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.UNDERSCORE_UPPER_CAMELCASE).serialize(dog).contains("\"Some_Field_Name\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.UNDERSCORE_LOWER).serialize(dog).contains("\"some_field_name\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.UNDERSCORE_UPPER).serialize(dog).contains("\"SOME_FIELD_NAME\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.SPACE_CAMELCASE).serialize(dog).contains("\"some Field Name\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.SPACE_UPPER_CAMELCASE).serialize(dog).contains("\"Some Field Name\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.SPACE_LOWER).serialize(dog).contains("\"some field name\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.SPACE_UPPER).serialize(dog).contains("\"SOME FIELD NAME\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.DASH_CAMELCASE).serialize(dog).contains("\"some-Field-Name\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.DASH_UPPER_CAMELCASE).serialize(dog).contains("\"Some-Field-Name\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.DASH_LOWER).serialize(dog).contains("\"some-field-name\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.DASH_UPPER).serialize(dog).contains("\"SOME-FIELD-NAME\":"));
	}
	
	
	@Test
	public void testSerializeChangeAttributeName() {
		FieldMapper fieldMapper = new FieldMapper("special_field_name", Dog.class).setIgnore(false);
		oson.clear().setFieldMappers(new FieldMapper("mySpecial_field_name", Dog.class).setIgnore(false))
			.setFieldMappers(fieldMapper);

		assertTrue(oson.serialize(dog).contains("\"Jackson name\":"));

		assertTrue(oson.serialize(dog).contains("\"Oson name overwrites names from external sources\":"));
		
		fieldMapper.json = "Java name";
		oson.setFieldMappers(fieldMapper);
		assertTrue(oson.serialize(dog).contains("\"Java name\":"));
		
		fieldMapper.json = null; // "";
		oson.setFieldMappers(fieldMapper);
		assertFalse(oson.serialize(dog).contains("\"Oson name overwrites names from external sources\":"));
	}
	
}
