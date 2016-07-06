package ca.oson.json.charstring;

import org.junit.Test;

import ca.oson.json.function.Character2JsonFunction;
import ca.oson.json.ClassMapper;
import ca.oson.json.support.TestCaseBase;

public class CharacterTest extends TestCaseBase {

	   @Test
	   public void testSerializeCharacter() {
		   Character value = 'a';
		   String expected = "a";
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeCharacter() {
		   String value = "A";
		   Character expected = 'A';

		   Character result = oson.deserialize(value, Character.class);

		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testDeserializeCharacterFromNumber() {
		   String value = "69";
		   Character expected = '6';

		   Character result = oson.deserialize(value, Character.class);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeCharacterOutOfRange() {
		   String value = "900000";
		   Character expected = '9';
		   
		   Character result = oson.deserialize(value, Character.class);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeCharacterOutOfRangeWithMax() {
		   String value = "6900000";
		   Character expected = '6';

		   Character result = oson.setClassMappers(new ClassMapper(Character.class).setMax(69l)).deserialize(value, Character.class);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeCharacterWithMin() {
		   String value = "1";
		   Character expected = '1';

		   Character result = oson.setClassMappers(new ClassMapper(Character.class).setMin(65l)).deserialize(value, Character.class);

		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testDeserializeCharacterWithFunction() {
		   String value = "1";
		   Character expected = 'B';

		   Character result = oson.setClassMappers(new ClassMapper(Character.class)
		   .setDeserializer((String p) -> (char)(Long.parseLong(p) + 65)))
				   .deserialize(value, Character.class);

		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testSerializeCharacterWithFunction() {
		   Character value = 'B';
		   String expected = "1";
		   
		   Character2JsonFunction function = (Character p) -> "" + ((int)p - 65);

		   String result = oson.setClassMappers(new ClassMapper(Character.class).setSerializer(function))
				.serialize(value, Character.class);

		   assertEquals(expected, result);
	   }
	   
}
