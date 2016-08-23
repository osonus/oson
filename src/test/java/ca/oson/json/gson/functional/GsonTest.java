package ca.oson.json.gson.functional;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.reflect.TypeToken;

import ca.oson.json.ClassMapper;
import ca.oson.json.Oson;
import ca.oson.json.support.TestCaseBase;

public class GsonTest extends TestCaseBase {

	@Test
	  public void testPrimitiveDoubleAutoboxedSerialization() {
	    assertEquals("-122.08234335", oson.setClassMappers(new ClassMapper(Double.class).setScale(8)).toJson(-122.08234335));
	    assertEquals("122.08112002", oson.setClassMappers(new ClassMapper(Double.class).setScale(8)).toJson(new Double(122.08112002)));
	  }
	  
	  @Test
	  public void testDeserializeJsonObjectAsBigDecimal() {
		  assertNull(oson.fromJson("{'a':1}", BigDecimal.class));
	  }
	  
	  public void testNegativeInfinitySerialization() {
	    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
	    double negativeInfinity = Double.NEGATIVE_INFINITY;
	    //"-Infinity"
	    String json = oson.toJson(negativeInfinity);
	    assertEquals("null", json);
	    assertEquals("null", oson.toJson(Double.NEGATIVE_INFINITY));
	  }
	  

	  public void testFloatInfinitySerialization() {
	    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
	    float infinity = Float.POSITIVE_INFINITY;
	    //"Infinity"
	    String json = oson.toJson(infinity);
	    assertEquals("null", json);
	    assertEquals("null", oson.toJson(Float.POSITIVE_INFINITY));
	  }

	  
	  public void testStringsAsBooleans() {
	    String json = "['true', 'false', 'TRUE', 'yes', '1']";
	    assertEquals(Arrays.asList(true, false, true, null, true),
	        oson.<List<Boolean>>fromJson(json, new TypeToken<List<Boolean>>() {}.getType()));
	  }
	  

	  @Test
	  public void testDeserializingDecimalPointValueZeroSucceeds() {
		  Integer value = oson.fromJson("1.0", Integer.class);
		  
	    assertEquals(1, (int) value);
	  }
	  
	  
	  public void testLongAsStringSerialization() throws Exception {
//	    gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING).create();
	    String result = oson.toJson(15L);
	    assertEquals("15", result);

	    // Test with an integer and ensure its still a number
	    result = oson.toJson(2);
	    assertEquals("2", result);
	  }
	  
	  
}
