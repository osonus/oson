package ca.oson.json.numeric;

import java.util.function.Function;

import org.junit.Test;

import ca.oson.json.Oson.*;
import ca.oson.json.TestCaseBase;

public class IntegerTest extends TestCaseBase {
	   
	   @Test
	   public void testSerializeInt() {
		   int value = 5;
		   String text = oson.serialize(value);
		   
		   assertEquals("5", text);
	   }
	   
	   
	   @Test
	   public void testDeserializeInt() {
		   int value = 5;
		   String text = oson.serialize(value);
		   
		   int returned = oson.deserialize(text, Integer.class);
		   
		   assertEquals(value, returned);
	   }
	   
	   @Test
	   public void testSerializeIntWithFunction() {
		   int value = 6;
		   
		   ClassMapper classMapper = new ClassMapper(Integer.class);

		   Function serializer = p -> {
			   switch (Integer.parseInt(p.toString())) {
			   case 1: return "One";
			   case 2: return "Two";
			   case 3: return "Three";
			   case 4: return "Four";
			   case 5: return "Five";
			   case 6: return "Six";
			   case 7: return "Seven";
			   case 8: return "Eight";
			   case 9: return "Nine";
			   case 10: return "Ten";
			   default: return p;
			   }
		   };
		   
		   classMapper.setSerializer(serializer);
		   
		   oson.setClassMappers(classMapper);
		   
		   String text = oson.serialize(value);
		   
		   assertEquals("Six", text);
	   }
	   
	   
	   @Test
	   public void testDeserializeIntWithFunction() {
		   String text = "Seven";
		   Integer expected = 7;

		   oson.setClassMappers(new ClassMapper(Integer.class)
		   	.setDeserializer(p -> {
				   switch (p.toString()) {
				   case "One": return 1;
				   case "Two": return 2;
				   case "Three": return 3;
				   case "Four": return 4;
				   case "Five": return 5;
				   case "Six": return 6;
				   case "Seven": return 7;
				   case "Eight": return 8;
				   case "Nine": return 9;
				   case "Ten": return 10;
				   default: return p;
				   }
			   }));

		   Integer value = oson.deserialize(text, Integer.class);
		   
		   assertEquals(expected, value);
	   }

}
