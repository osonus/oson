package ca.oson.json.gson.functional;

import ca.oson.json.ClassMapper;
import ca.oson.json.Oson;
import ca.oson.json.support.TestCaseBase;

public class GsonTest extends TestCaseBase {

	  public void testPrimitiveDoubleAutoboxedSerialization() {
	    assertEquals("-122.08234335", oson.setClassMappers(new ClassMapper(Double.class).setScale(8)).toJson(-122.08234335));
	    assertEquals("122.08112002", oson.setClassMappers(new ClassMapper(Double.class).setScale(8)).toJson(new Double(122.08112002)));
	  }
}
