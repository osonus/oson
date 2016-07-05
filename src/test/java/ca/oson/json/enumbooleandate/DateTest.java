package ca.oson.json.enumbooleandate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import ca.oson.json.ClassMapper;
import ca.oson.json.function.Date2LongFunction;
import ca.oson.json.support.TestCaseBase;

public class DateTest extends TestCaseBase {

	   @Test
	   public void testSerializeDate() throws ParseException {
		   String format = "dd/MM/yyyy";
		   SimpleDateFormat sdf = new SimpleDateFormat(format);
		   String expected = "21/12/2012";
		   Date value = sdf.parse(expected);
		   
		   String result = oson.setDateFormat(format).serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeDate() throws ParseException {
		   String format = "dd/MM/yyyy";
		   SimpleDateFormat sdf = new SimpleDateFormat(format);
		   String value = "21/12/2012";
		   Date expected = sdf.parse(value);
		   
		   Date result = oson.setDateFormat(format).deserialize(value, Date.class);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeDate2Long() throws ParseException {
		   String format = "dd/MM/yyyy";
		   SimpleDateFormat sdf = new SimpleDateFormat(format);
		   Date value = sdf.parse("21/12/2012");
		   long expected = value.getTime();
		   
		   String result = oson.setDate2Long(true).serialize(value);
		   
		   // System.err.println(result); 1356076800000

		   assertEquals(expected, Long.parseLong(result));
	   }
	   
	   @Test
	   public void testDeserializeFromLong() throws ParseException {
		   String format = "dd/MM/yyyy";
		   Long value = 1356076800000l;
		   Date expected = new Date(value);
		   
		   Date result = oson.setDateFormat(format).deserialize(value.toString(), Date.class);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeWithFunction() throws ParseException {
		   String format = "dd/MM/yyyy";
		   Long value = 1356076800000l;
		   Date expected = new Date(value * 2);
		   
		   Date result = oson.setClassMappers(new ClassMapper(Date.class)
		   	.setDeserializer((Long p) -> {
				   p = p * 2;
				return new Date(p);
			   })).deserialize(value.toString(), Date.class);

		   assertEquals(expected, result);
	   }
	   
	   
	   @Test
	   public void testSerializeWithFunction() throws ParseException {
		   Long value = 1356076800000l;
		   long expected = value * 2;
		   
		   Date2LongFunction function = (Date p) -> p.getTime() * 2;
		   
		   String result = oson.setClassMappers(new ClassMapper(Date.class)
		   	.setSerializer(function)).serialize(new Date(value));

		   assertEquals(expected, Long.parseLong(result));
	   }
	   
}
