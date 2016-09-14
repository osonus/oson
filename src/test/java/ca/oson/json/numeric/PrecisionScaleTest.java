package ca.oson.json.numeric;

import java.math.BigDecimal;

import org.junit.Test;

import ca.oson.json.ClassMapper;
import ca.oson.json.FieldMapper;
import ca.oson.json.Oson;
import ca.oson.json.support.TestCaseBase;
import static org.junit.Assert.assertNotEquals;

//import ca.oson.json.ClassMapper;

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
		
		json = oson.setAppendingFloatingZero(false).serialize(value);
		
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
		
		json = oson.setAppendingFloatingZero(false).serialize(value);
		
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
		
		json = oson.setAppendingFloatingZero(false).serialize(value);
		
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
		String expected55 = "12.346";
		
		oson.setPrecision(1).setScale(null);
		String json = oson.setAppendingFloatingZero(false).serialize(value);
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
		json = oson.setAppendingFloatingZero(false).serialize(value);
		
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

		oson.clear().setPrecision(5).setScale(1);
		String json = oson.serialize(value);
		assertEquals(expected51, json);
		json = oson.serialize(valueDouble);
		assertEquals(expected51, json);
		json = oson.serialize(valueBigDecimal);
		assertEquals(expected51, json);

		json = oson.serialize(decimal);
		
		//System.err.println(json);
		assertEquals(expected42, json);
	}
	
	
	@Test
	public void testSerializationPrecisionAndScaleFloatDoubleDecimalClassLevelWithJavaConfiguration() {
		float value = 12.34567890f;
		Double valueDouble = (double)value;
		BigDecimal valueBigDecimal = new BigDecimal(valueDouble);
		
		String expected10 = "10";
		String expected51 = "12.3";
		String expected55 = "12.346";
		String expected10null = "10";
		
		oson.clear().setPrecision(1).setScale(null);
		oson.setClassMappers(new ca.oson.json.ClassMapper(Float.class).setPrecision(5).setScale(1));
		String json = oson.serialize(value);
		assertEquals(expected51, json);
		json = oson.setAppendingFloatingZero(false).serialize(valueDouble);
		assertEquals(expected10, json);
		json = oson.serialize(valueBigDecimal);
		assertEquals(expected10, json);

		oson.clear().setPrecision(5).setScale(1);
		oson.setClassMappers(new ca.oson.json.ClassMapper(Float.class).setPrecision(5).setScale(5));
		json = oson.setAppendingFloatingZero(false).serialize(value);
		assertEquals(expected55, json);
		json = oson.serialize(valueDouble);
		assertEquals(expected51, json);
		json = oson.serialize(valueBigDecimal);
		assertEquals(expected51, json);

		oson.clear().setPrecision(5).setScale(5);
		oson.setClassMappers(new ClassMapper(Float.class).setPrecision(1).setScale(0));
		json = oson.setAppendingFloatingZero(false).serialize(value);
		assertEquals(expected10, json);
		json = oson.serialize(valueDouble);
		assertEquals(expected55, json);
		json = oson.serialize(valueBigDecimal);
		assertEquals(expected55, json);
		
		oson.clear().setClassMappers(new ClassMapper(Float.class).setPrecision(1).setScale(null));
		json = oson.setAppendingFloatingZero(false).serialize(value);
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
		String expectedDecimal = "{\"valueDouble\":12.34568,\"valueBigDecimal\":12.34568,\"value\":12}";

		oson.clear().setPrecision(5).setScale(1);
		oson.setClassMappers(new ClassMapper[] {
		new ClassMapper(Float.class).setPrecision(3).setScale(0),
		new ClassMapper(Decimal.class).setPrecision(8).setScale(5)
		});
		
		String json = oson.setAppendingFloatingZero(false).serialize(value);
		assertEquals(expected30, json);
		json = oson.serialize(valueDouble);
		assertEquals(expected51, json);
		json = oson.serialize(valueBigDecimal);
		assertEquals(expected51, json);

		json = oson.serialize(decimal);
		
		// System.err.println(json);
		
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
		String expectedDecimal = "{\"valueDouble\":12.34568,\"valueBigDecimal\":12.34568,\"value\":12.3}";

		oson.clear().setPrecision(5).setScale(1);
		oson.setClassMappers(new ClassMapper[] {
		new ClassMapper(Float.class).setPrecision(3).setScale(0),
		new ClassMapper(Decimal2.class).setPrecision(8).setScale(5)
		});
		
		String json = oson.setAppendingFloatingZero(false).serialize(value);
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
		String expectedDecimal = "{\"valueDouble\":12.34568,\"valueBigDecimal\":12.34568,\"newFloatFieldName\":12.3457}"; 
		// "{\"valueDouble\":12.35,\"valueBigDecimal\":12.35,\"newFloatFieldName\":12.345700}";

		oson.clear().setPrecision(5).setScale(1);
		oson.setClassMappers(new ClassMapper[] {
		new ClassMapper(Float.class).setPrecision(3).setScale(0),
		new ClassMapper(Decimal2.class).setPrecision(8).setScale(5)
		}).setFieldMappers(new FieldMapper("value", "newFloatFieldName", Decimal2.class).setPrecision(6).setScale(6));
		
		String json = oson.setAppendingFloatingZero(false).serialize(value);
		assertEquals(expected30, json);
		json = oson.serialize(valueDouble);
		assertEquals(expected51, json);
		json = oson.serialize(valueBigDecimal);
		assertEquals(expected51, json);

		json = oson.setAppendingFloatingZero(false).serialize(decimal);
	
		//System.err.println(json);
		assertEquals(expectedDecimal, json);

	}
}


@ca.oson.json.annotation.ClassMapper (precision = 4, scale = 2)
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


@ca.oson.json.annotation.ClassMapper (precision = 4, scale = 2)
class Decimal2 {
	@ca.oson.json.annotation.FieldMapper (precision = 3, scale = 1)
	Float value;
	
	Double valueDouble;
	BigDecimal valueBigDecimal;
	
	public Decimal2 (float value) {
		this.value = value;
		this.valueDouble = (double)value;
		this.valueBigDecimal = new BigDecimal(valueDouble);
	}
}
