package ca.oson.json.userguide;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import ca.oson.json.ClassMapper;
import ca.oson.json.Oson.MODIFIER;
import ca.oson.json.domain.Dog;
import ca.oson.json.domain.OrderedPerson;
import ca.oson.json.domain.Person;
import ca.oson.json.domain.Dog.BREED;
import ca.oson.json.support.TestCaseBase;

public class ClassMapperTest extends TestCaseBase {

	@Test
	public void testSerializeIgnore() {
		OrderedPerson obj = new OrderedPerson();

		ClassMapper stringMapper = new ClassMapper(String.class).setIgnore(true);
		oson.clear().setClassMappers(stringMapper);
		assertEquals("{\"addressList\":null,\"age\":0,\"birthDate\":null}", oson.serialize(obj));
		
		stringMapper.setIgnore(false);
		
		ClassMapper classMapper = new ClassMapper(OrderedPerson.class);
		Set set = new HashSet();
		set.add(MODIFIER.Public);
		classMapper.setIncludeFieldsWithModifiers(set);
		oson.setClassMappers(classMapper);

		assertTrue(oson.serialize(obj).contains("\"a\":\""));
		
		Set jsonIgnoreProperties = new HashSet();
		jsonIgnoreProperties.add("a");
		classMapper.setJsonIgnoreProperties(jsonIgnoreProperties);
		
		assertFalse(oson.serialize(obj).contains("\"a\":\""));
		
		
		
		Person scooby = new Person("Scooby", "Doo", 5, null);
		
		Dog dog = new Dog("Sandy");
		dog.setOwner(scooby);
		assertTrue(oson.serialize(dog).contains("\"name\":\"Scooby\""));

		ClassMapper personMapper = new ClassMapper(Person.class).setIgnore(true);
		oson.setClassMappers(personMapper);
		assertFalse(oson.serialize(dog).contains("\"name\":\"Scooby\""));
	}
	
	
	
}
