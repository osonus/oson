package ca.oson.json.userguide;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import com.google.gson.Gson;

import ca.oson.json.ClassMapper;
import ca.oson.json.DefaultValue;
import ca.oson.json.FieldMapper;
import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.Oson.FIELD_NAMING;
import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.OsonAssert;
import ca.oson.json.OsonAssert.MODE;
import ca.oson.json.domain.Animal;
import ca.oson.json.domain.Car;
import ca.oson.json.domain.Dog;
import ca.oson.json.domain.Dog.BREED;
import ca.oson.json.domain.Eukaryote;
import ca.oson.json.domain.Pet;
import ca.oson.json.function.String2JsonFunction;
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
	    
	    Dog dog = oson.deserialize(expectedDog, Dog.class);
	    Animal animal = oson.deserialize(expectedAnimal, Dog.class);
	    Pet pet = oson.deserialize(expectedPet, Dog.class);
	    
	    OsonAssert.assertEquals(pet, dog, MODE.SUBSET);
	    OsonAssert.assertEquals(animal, pet, MODE.SUBSET);
	    
	    OsonAssert.assertEquals(expectedPet, expectedDog, MODE.SUBSET);
	    OsonAssert.assertEquals(expectedAnimal, expectedPet, MODE.SUBSET);
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
	
	
	@Test
	public void testSerializeFieldMapper() {
		FieldMapper fieldMapper = new FieldMapper("name", Dog.class).setIgnore(true);
		
		assertTrue(oson.serialize(dog).contains("\"name\":\"I am a dog\""));
		
		assertFalse(oson.setFieldMappers(fieldMapper).serialize(dog).contains("\"name\":\"I am a dog\""));
		
		fieldMapper.setIgnore(false).setLength(6);
		
		assertTrue(oson.serialize(dog).contains("\"name\":\"I am a\""));

		fieldMapper.setJsonValue(true);
		assertEquals("\"I am a\"", oson.serialize(dog));
		
		fieldMapper.setJsonRawValue(true);
		assertEquals("I am a", oson.serialize(dog));
		
		dog.setName("doggie");
		String2JsonFunction serializer = (String p) -> "My " + p;
		fieldMapper.setSerializer(serializer);
		assertEquals("My dog", oson.serialize(dog));
	}
	
	
	@Test
	public void testSerializeNullEmptyDefaultValues() {
	    String expectedNonNull = "{\"bread\":\"GERMAN_SHEPHERD\",\"name\":\"\",\"weight\":0.0,\"age\":1}";
	    String expectedNonEmpty = "{\"bread\":\"GERMAN_SHEPHERD\",\"weight\":0.0,\"age\":1}";
	    String expectedNonDefault = "{\"bread\":\"GERMAN_SHEPHERD\",\"age\":1}";
	    String expectedNonDefault2 = "{\"bread\":\"GERMAN_SHEPHERD\"}";
	    String expectedNonDefault3 = "{\"bread\":\"GERMAN_SHEPHERD\"}";
	    String expectedDefault = "{\"bread\":\"GERMAN_SHEPHERD\",\"age\":1}";
		
		dog.setName("");
		dog.setWeight(0.0);
		
		oson.clear().setDefaultType(JSON_INCLUDE.NON_NULL);
	    assertEquals(expectedNonNull, oson.serialize(dog));

		oson.setDefaultType(JSON_INCLUDE.NON_EMPTY);
	    assertEquals(expectedNonEmpty, oson.serialize(dog));

		oson.setDefaultType(JSON_INCLUDE.NON_DEFAULT);
	    assertEquals(expectedNonDefault, oson.serialize(dog));
	    
	    Integer integer = DefaultValue.integer;
	    DefaultValue.integer = 1;

	    assertEquals(expectedNonDefault2, oson.serialize(dog));

	    
	    oson.clear().setDefaultType(JSON_INCLUDE.DEFAULT);
	    
	    dog.setWeight(null);
	    DefaultValue.date = null;
	    
	    Double ddouble = DefaultValue.ddouble;

	    assertEquals("{\"owner\":null,\"bread\":\"GERMAN_SHEPHERD\",\"birthDate\":null,\"name\":\"\",\"weight\":0.0,\"age\":1}", oson.serialize(dog));

	    DefaultValue.ddouble = 1.0;
	    assertEquals("{\"owner\":null,\"bread\":\"GERMAN_SHEPHERD\",\"birthDate\":null,\"name\":\"\",\"weight\":1.0,\"age\":1}", oson.serialize(dog));

	    String format = "yyyy-MM-dd";
	    oson.setDateFormat(format);
	    Date date = oson.deserialize("2011-01-18", Date.class);
	    
	    FieldMapper fieldMapper = new FieldMapper("birthDate", Dog.class).setRequired(true);
	    oson.setFieldMappers(fieldMapper);

	    DefaultValue.date = date;
	    assertEquals("{\"owner\":null,\"bread\":\"GERMAN_SHEPHERD\",\"birthDate\":\"2011-01-18\",\"name\":\"\",\"weight\":1.0,\"age\":1}", oson.serialize(dog));

	    date = oson.deserialize("2011-01-19", Date.class);
	    ClassMapper classMapper = new ClassMapper(Date.class).setDefaultValue(date);
	    oson.setClassMappers(classMapper);
	    assertEquals("{\"owner\":null,\"bread\":\"GERMAN_SHEPHERD\",\"birthDate\":\"2011-01-19\",\"name\":\"\",\"weight\":1.0,\"age\":1}", oson.serialize(dog));

	    date = oson.deserialize("2011-01-20", Date.class);
	    fieldMapper.setDefaultValue(date);
	    assertEquals("{\"owner\":null,\"bread\":\"GERMAN_SHEPHERD\",\"birthDate\":\"2011-01-20\",\"name\":\"\",\"weight\":1.0,\"age\":1}", oson.serialize(dog));
	}
	
	
}
