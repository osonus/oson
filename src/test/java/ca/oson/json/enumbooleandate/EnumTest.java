package ca.oson.json.enumbooleandate;

import org.junit.Test;

import ca.oson.json.ClassMapper;
import ca.oson.json.EnumType;
import ca.oson.json.OsonAssert;
import ca.oson.json.OsonAssert.MODE;
import ca.oson.json.annotation.FieldMapper;
import ca.oson.json.Oson.*;
import ca.oson.json.enumbooleandate.EnumMaster.MatchingSensitivity;
import ca.oson.json.function.Enum2JsonFunction;
import ca.oson.json.support.TestCaseBase;

public class EnumTest extends TestCaseBase {

	   @Test
	   public void testSerializeEnum() {
		   Enum value = MODIFIER.Public;
		   String expected = "Public";
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeEnum() {
		   String value = "DASH_UPPER_CAMELCASE";
		   Enum expected = FIELD_NAMING.DASH_UPPER_CAMELCASE;
		   
		   Enum result = oson.deserialize(value, expected.getClass());

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeEnumWithEnumType() {
		   Enum value = MODIFIER.Synchronized;
		   String expected = "10";
		   
		   oson.setEnumType(EnumType.ORDINAL);
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeEnumWithFunction() {
		   Enum value = MODIFIER.Public;
		   String expected = "Final";
		   
		   Enum2JsonFunction function = p -> MODIFIER.Final.name();
		   
		   oson.setClassMappers(new ClassMapper(value.getClass()).setSerializer(function));
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }

	   @Test
	   public void testDeSerializeEnumWithAnnotation() {
		   EnumMaster master = new EnumMaster();
		   master.threshold = 10;
		   
		   master.matchingSensitivity = MatchingSensitivity.MEDIUM;
		   
		   String json = oson.serialize(master);
		   
		   EnumMaster student = oson.deserialize(json, EnumMaster.class);
		   
		   OsonAssert.assertEquals(json, student, MODE.EXACT);
	   }
	   
}


class EnumMaster 
{
	public enum MatchingSensitivity {
		LOW,
		MEDIUM,
		HIGH;

		@FieldMapper(jsonValue = BOOLEAN.TRUE)
		public String toValue() {
			return this.name().toLowerCase();
		}

		@FieldMapper(jsonCreator = BOOLEAN.TRUE)
		public static MatchingSensitivity forValue(String value) {
			return MatchingSensitivity.valueOf(value.toUpperCase());
		}
	}
	
	
	MatchingSensitivity matchingSensitivity;
	int threshold;
	
}