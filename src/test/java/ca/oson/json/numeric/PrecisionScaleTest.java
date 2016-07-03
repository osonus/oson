package ca.oson.json.numeric;

import java.math.BigDecimal;

import org.junit.Test;

import ca.oson.json.Oson;
import ca.oson.json.support.TestCaseBase;
import static org.junit.Assert.assertNotEquals;

//import ca.oson.json.Oson.ClassMapper;

public class PrecisionScaleTest extends TestCaseBase {

	@Test
	public void testSerializationPrecisionFloatGlobal() {
		float value = 12.34567890f;

		String json = oson.serialize(value);

		String expected = "12.345678";

		assertNotEquals(expected, json);

		
		oson.setPrecision(5).setScale(null);
		
		expected = "12.346";
		
		json = oson.serialize(value);
		
		assertEquals(expected, json);
		
		
		oson.setPrecision(2);
		
		expected = "12";
		
		json = oson.serialize(value);
		
		assertEquals(expected, json);
		
		
		oson.setPrecision(1);
		
		expected = "10";
		
		json = oson.serialize(value);
		
		assertEquals(expected, json);
		
		
		oson.setPrecision(7);
		
		expected = "12.34568";
		
		json = oson.serialize(value);
		
		assertEquals(expected, json);
	}
	
	
	@Test
	public void testSerializationPrecisionAndScaleFloatGlobal() {
		float value = 12.34567890f;
		
		oson.setPrecision(5).setScale(1);
		String json = oson.serialize(value);
		String expected = "12.3";
		assertEquals(expected, json);
		
		oson.setPrecision(5).setScale(5);
		json = oson.serialize(value);
		expected = "12.34600";
		assertEquals(expected, json);
	}
	
	
	@Test
	public void testSerializationPrecisionDoubleGlobal() {
		double value = 12.34567890d;

		String json = oson.serialize(value);

		String expected = "12.345678";

		assertNotEquals(expected, json);

		
		oson.setPrecision(5).setScale(null);
		
		expected = "12.346";
		
		json = oson.serialize(value);
		
		assertEquals(expected, json);
		
		
		oson.setPrecision(2);
		
		expected = "12";
		
		json = oson.serialize(value);
		
		assertEquals(expected, json);
		
		
		oson.setPrecision(1);
		
		expected = "10";
		
		json = oson.serialize(value);
		
		assertEquals(expected, json);
		
		
		oson.setPrecision(7);
		
		expected = "12.34568";
		
		json = oson.serialize(value);
		
		assertEquals(expected, json);
	}
	
	
	@Test
	public void testSerializationPrecisionAndScaleDoubleGlobal() {
		Double value = 12.34567890d;
		
		oson.setPrecision(5).setScale(1);
		String json = oson.serialize(value);
		String expected = "12.3";
		assertEquals(expected, json);
		
		oson.setPrecision(5).setScale(5);
		json = oson.serialize(value);
		expected = "12.34600";
		assertEquals(expected, json);
	}
	
	@Test
	public void testSerializationPrecisionAndScaleBigDecimalGlobal() {
		BigDecimal value = new BigDecimal(12.34567890d);
		
		oson.setPrecision(5).setScale(1);
		String json = oson.serialize(value);
		String expected = "12.3";
		assertEquals(expected, json);
		
		oson.setPrecision(5).setScale(5);
		json = oson.serialize(value);
		expected = "12.34600";
		assertEquals(expected, json);
	}
	
	
	@Test
	public void testSerializationPrecisionBigDecimalGlobal() {
		BigDecimal value = new BigDecimal(12.34567890d);

		String json = oson.serialize(value);

		String expected = "12.345678";

		assertNotEquals(expected, json);

		
		oson.setPrecision(5).setScale(null);
		
		expected = "12.346";
		
		json = oson.serialize(value);
		
		assertEquals(expected, json);
		
		
		oson.setPrecision(2);
		
		expected = "12";
		
		json = oson.serialize(value);
		
		assertEquals(expected, json);
		
		
		oson.setPrecision(1);
		
		expected = "10";
		
		json = oson.serialize(value);
		
		assertEquals(expected, json);
		
		
		oson.setPrecision(7);
		
		expected = "12.34568";
		
		json = oson.serialize(value);
		
		assertEquals(expected, json);
	}
	
	
	
	@Test
	public void testSerializationPrecisionAndScaleFloatDoubleDecimalGlobal() {
		float value = 12.34567890f;
		Double valueDouble = (double)value;
		BigDecimal valueBigDecimal = new BigDecimal(valueDouble);
		
		String expected10 = "10";
		String expected51 = "12.3";
		String expected55 = "12.34600";
		
		oson.setPrecision(1).setScale(null);
		String json = oson.serialize(value);
		assertEquals(expected10, json);
		json = oson.serialize(valueDouble);
		assertEquals(expected10, json);
		json = oson.serialize(valueBigDecimal);
		assertEquals(expected10, json);

		oson.setPrecision(5).setScale(1);
		json = oson.serialize(value);
		
		assertEquals(expected51, json);
		json = oson.serialize(valueDouble);
		assertEquals(expected51, json);
		json = oson.serialize(valueBigDecimal);
		assertEquals(expected51, json);

		oson.setPrecision(5).setScale(5);
		json = oson.serialize(value);
		
		assertEquals(expected55, json);
		json = oson.serialize(valueDouble);
		assertEquals(expected55, json);
		json = oson.serialize(valueBigDecimal);
		assertEquals(expected55, json);
	}
	
	
	@Test
	public void testSerializationPrecisionAndScaleFloatDoubleDecimalClassLevelWithAnnotation() {
		float value = 12.34567890f;
		Double valueDouble = (double)value;
		BigDecimal valueBigDecimal = new BigDecimal(valueDouble);
		
		Decimal decimal = new Decimal(value);
		
		String expected51 = "12.3";
		String expected42 = "{\"valueDouble\":12.35,\"valueBigDecimal\":12.35,\"value\":12.35}";
		
		oson = new Oson();

		oson.setPrecision(5).setScale(1);
		String json = oson.serialize(value);
		assertEquals(expected51, json);
		json = oson.serialize(valueDouble);
		assertEquals(expected51, json);
		json = oson.serialize(valueBigDecimal);
		assertEquals(expected51, json);

		json = oson.serialize(decimal);
		assertEquals(expected42, json);
	}
	
	
	@Test
	public void testSerializationPrecisionAndScaleFloatDoubleDecimalClassLevelWithJavaConfiguration() {
		float value = 12.34567890f;
		Double valueDouble = (double)value;
		BigDecimal valueBigDecimal = new BigDecimal(valueDouble);
		
		String expected10 = "10";
		String expected51 = "12.3";
		String expected55 = "12.34600";
		String expected10null = "10";
		
		oson.clear().setPrecision(1).setScale(null);
		oson.setClassMappers(new ca.oson.json.Oson.ClassMapper(Float.class).setPrecision(5).setScale(1));
		String json = oson.serialize(value);
		assertEquals(expected51, json);
		json = oson.serialize(valueDouble);
		assertEquals(expected10, json);
		json = oson.serialize(valueBigDecimal);
		assertEquals(expected10, json);

		oson.clear().setPrecision(5).setScale(1);
		oson.setClassMappers(new ca.oson.json.Oson.ClassMapper(Float.class).setPrecision(5).setScale(5));
		json = oson.serialize(value);
		assertEquals(expected55, json);
		json = oson.serialize(valueDouble);
		assertEquals(expected51, json);
		json = oson.serialize(valueBigDecimal);
		assertEquals(expected51, json);

		oson.clear().setPrecision(5).setScale(5);
		oson.setClassMappers(new ca.oson.json.Oson.ClassMapper(Float.class).setPrecision(1).setScale(0));
		json = oson.serialize(value);
		assertEquals(expected10, json);
		json = oson.serialize(valueDouble);
		assertEquals(expected55, json);
		json = oson.serialize(valueBigDecimal);
		assertEquals(expected55, json);
		
		oson.clear().setClassMappers(new ca.oson.json.Oson.ClassMapper(Float.class).setPrecision(1).setScale(null));
		json = oson.serialize(value);
		assertEquals(expected10null, json);

	}
	
	
	@Test
	public void testSerializationPrecisionAndScaleFloatDoubleDecimalClassLevelWithAnnotationOverridenByJavaConfiguration() {
		float value = 12.34567890f;
		Double valueDouble = (double)value;
		BigDecimal valueBigDecimal = new BigDecimal(valueDouble);
		
		Decimal decimal = new Decimal(value);
		
		String expected51 = "12.3";
		String expected30 = "12";
		String expectedDecimal = "{\"valueDouble\":12.35,\"valueBigDecimal\":12.35,\"value\":12.35}";
		
		oson = new Oson();

		oson.setPrecision(5).setScale(1);
		oson.setClassMappers(new ca.oson.json.Oson.ClassMapper[] {
		new ca.oson.json.Oson.ClassMapper(Float.class).setPrecision(3).setScale(0),
		new ca.oson.json.Oson.ClassMapper(Decimal.class).setPrecision(8).setScale(5)
		});
		
		String json = oson.serialize(value);
		assertEquals(expected30, json);
		json = oson.serialize(valueDouble);
		assertEquals(expected51, json);
		json = oson.serialize(valueBigDecimal);
		assertEquals(expected51, json);

		json = oson.serialize(decimal);
		
		//System.err.println(json);
		
		assertEquals(expectedDecimal, json);
	}
	
	
	@Test
	public void testSerializationPrecisionAndScaleFloatDoubleDecimalClassLevelAnnotationFieldLevelAnnotation() {
		float value = 12.34567890f;
		Double valueDouble = (double)value;
		BigDecimal valueBigDecimal = new BigDecimal(valueDouble);
		
		Decimal2 decimal = new Decimal2(value);
		
		String expected51 = "12.3";
		String expected30 = "12";
		String expectedDecimal = "{\"valueDouble\":12.35,\"valueBigDecimal\":12.35,\"value\":12.3}";
		
		oson = new Oson();

		oson.setPrecision(5).setScale(1);
		oson.setClassMappers(new ca.oson.json.Oson.ClassMapper[] {
		new ca.oson.json.Oson.ClassMapper(Float.class).setPrecision(3).setScale(0),
		new ca.oson.json.Oson.ClassMapper(Decimal2.class).setPrecision(8).setScale(5)
		});
		
		String json = oson.serialize(value);
		assertEquals(expected30, json);
		json = oson.serialize(valueDouble);
		assertEquals(expected51, json);
		json = oson.serialize(valueBigDecimal);
		assertEquals(expected51, json);

		json = oson.serialize(decimal);
		//System.err.println(json);
		assertEquals(expectedDecimal, json);

	}
	
	
	@Test
	public void testSerializationPrecisionAndScaleFloatDoubleDecimalClassLevelAnnotationFieldLevelAnnotationAndFieldMapper() {
		float value = 12.34567890f;
		Double valueDouble = (double)value;
		BigDecimal valueBigDecimal = new BigDecimal(valueDouble);
		
		Decimal2 decimal = new Decimal2(value);
		
		String expected51 = "12.3";
		String expected30 = "12";
		String expectedDecimal = "{\"valueDouble\":12.35,\"valueBigDecimal\":12.35,\"value\":12.345700}";
		oson = new Oson();

		oson.setPrecision(5).setScale(1);
		oson.setClassMappers(new ca.oson.json.Oson.ClassMapper[] {
		new ca.oson.json.Oson.ClassMapper(Float.class).setPrecision(3).setScale(0),
		new ca.oson.json.Oson.ClassMapper(Decimal2.class).setPrecision(8).setScale(5)
		}).setFieldMappers(new ca.oson.json.Oson.FieldMapper("value", "newFloatFieldName", Decimal2.class).setPrecision(6).setScale(6));
		
		String json = oson.serialize(value);
		assertEquals(expected30, json);
		json = oson.serialize(valueDouble);
		assertEquals(expected51, json);
		json = oson.serialize(valueBigDecimal);
		assertEquals(expected51, json);

		json = oson.serialize(decimal);
	
		//System.err.println(json);
		assertEquals(expectedDecimal, json);

	}
}


@ca.oson.json.ClassMapper (precision = 4, scale = 2)
class Decimal {
	float value;
	Double valueDouble;
	BigDecimal valueBigDecimal;
	
	public Decimal (float value) {
		this.value = value;
		this.valueDouble = (double)value;
		this.valueBigDecimal = new BigDecimal(valueDouble);
	}
}


@ca.oson.json.ClassMapper (precision = 4, scale = 2)
class Decimal2 {
	@ca.oson.json.FieldMapper (precision = 3, scale = 1)
	Float value;
	
	Double valueDouble;
	BigDecimal valueBigDecimal;
	
	public Decimal2 (float value) {
		this.value = value;
		this.valueDouble = (double)value;
		this.valueBigDecimal = new BigDecimal(valueDouble);
	}
}
