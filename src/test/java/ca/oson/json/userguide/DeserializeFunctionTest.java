package ca.oson.json.userguide;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ca.oson.json.Oson.DataMapper;
import ca.oson.json.Oson.Json2DataMapperFunction;
import ca.oson.json.domain.Car;
import ca.oson.json.support.TestCaseBase;
import static org.junit.Assert.assertNotEquals;

public class DeserializeFunctionTest extends TestCaseBase {

	   @Test
	   public void testDeserializeListWithClassData() {
		   Car car = new Car("Chevron", 2);
		   
		   String json = oson.serialize(car);

		   Json2DataMapperFunction function = (DataMapper p) -> {
			   Map<String, Object> data = p.getMap();
			   Car newcar = (Car) p.getObj();
			   
			   int doors = Integer.parseInt(data.get("doors").toString());
			   String brand = data.get("brand").toString();
			   
			   int level = p.getLevel();
			   
			   newcar.brand = brand + " is turned into a BMW at level " + level;
			   newcar.doors = doors * 2;

			   return newcar;
		   };
		   
		   Car newcar = oson.setDeserializer(Car.class, function).deserialize(json, Car.class);

		   assertNotEquals(car.toString(), newcar.toString());
		   
		   assertEquals(4, newcar.doors);
		   
		   assertEquals("Chevron is turned into a BMW at level 0", newcar.brand);
	   }
	
	
}
