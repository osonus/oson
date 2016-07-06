package ca.oson.json.annotation;

import org.junit.Test;

import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.support.TestCaseBase;


public class FieldMapperSeDeseTest extends TestCaseBase {
	@Test
	public void testDeSerializeMyNameFieldOverClassMapper() {
		MyName MyName = new MyName("This is a testing on ClassMapper");
		
		String expected = "{\"name\":\"Thi\"}";
		
		String json = oson.serialize(MyName);

		assertEquals(expected, json);
		
		json = "{\"name\":\"This is a testing on ClassMapper\"}";
		
		MyName MyName2 = oson.deserialize(json, MyName.class);
		
		expected = "This is a testing on ClassMapper";
		
		assertEquals(expected, MyName2.name);
	}
	

	@Test
	public void testDeSerializeMyName2() {
		MyName2 MyName = new MyName2("This is a testing on ClassMapper");
		
		String expected = "{\"name\":\"This is a testing on ClassMapper\"}";
		
		String json = oson.clear().serialize(MyName);
		
		assertEquals(expected, json);
		
		MyName2 MyName2 = oson.deserialize(json, MyName2.class);
		
		expected = "This is";
		
		assertEquals(expected, MyName2.name);
	}
	
	@Test
	public void testDeSerializeMyName3() {
		MyName3 MyName = new MyName3("This is a testing on ClassMapper");
		
		String expected = "{\"name\":\"This is\"}";
		
		String json = oson.clear().serialize(MyName);
		
		assertEquals(expected, json);
		
		json = "{\"name\":\"This is a testing on ClassMapper\"}";
		
		MyName3 MyName2 = oson.deserialize(json, MyName3.class);
		
		expected = "This is a testing on ClassMapper";
		
		assertEquals(expected, MyName2.name);
	}
	
	@Test
	public void testDeSerializeMyName4() {
		MyName4 MyName = new MyName4("This is a testing on ClassMapper");
		
		String expected = "{\"name\":\"This is\"}";
		
		String json = oson.clear().serialize(MyName);
		
		assertEquals(expected, json);
		
		json = "{\"name\":\"This is a testing on ClassMapper\"}";
		
		MyName4 MyName2 = oson.deserialize(json, MyName4.class);
		
		expected = "This is";
		
		assertEquals(expected, MyName2.name);
	}
	
	
	@Test
	public void testDeSerializeMyName5() {
		MyName5 MyName = new MyName5("This is a testing on ClassMapper");
		
		String expected = "{\"name\":\"This is\"}";
		
		String json = oson.clear().serialize(MyName);
		
		assertEquals(expected, json);
		
		json = "{\"name\":\"This is a testing on ClassMapper\"}";
		
		MyName5 MyName2 = oson.deserialize(json, MyName5.class);
		
		expected = "This is";
		
		assertEquals(expected, MyName2.name);
	}
	
	@Test
	public void testDeSerializeMyName6() {
		MyName6 MyName = new MyName6("This is a testing on ClassMapper");
		
		String expected = "{\"name\":\"This is\"}";
		
		String json = oson.clear().serialize(MyName);
		
		assertEquals(expected, json);
		
		json = "{\"name\":\"This is a testing on ClassMapper\"}";
		
		MyName6 MyName2 = oson.deserialize(json, MyName6.class);
		
		expected = "This";
		
		assertEquals(expected, MyName2.name);
	}
	
	@Test
	public void testDeSerializeMyName7() {
		MyName7 MyName = new MyName7("This is a testing on ClassMapper");
		
		String expected = "{\"name\":\"This is\"}";
		
		String json = oson.clear().serialize(MyName);
		
		assertEquals(expected, json);
		
		json = "{\"name\":\"This is a testing on ClassMapper\"}";
		
		MyName7 MyName2 = oson.deserialize(json, MyName7.class);
		
		expected = "This";
		
		assertEquals(expected, MyName2.name);
	}
	
	
	@Test
	public void testDeSerializeMyName8() {
		MyName8 MyName = new MyName8("This is a testing on ClassMapper");
		
		String expected = "{\"name\":\"This is a \"}";
		
		String json = oson.clear().serialize(MyName);
		
		assertEquals(expected, json);
		
		json = "{\"name\":\"This is a testing on ClassMapper\"}";
		
		MyName8 MyName2 = oson.deserialize(json, MyName8.class);
		
		expected = "This i";
		
		assertEquals(expected, MyName2.name);
	}
}

@ClassMapper(serialize=BOOLEAN.TRUE, length=7)
class MyName {
	
	@FieldMapper(serialize=BOOLEAN.TRUE, length=3)
    public String name;

    public MyName(String name) {
        this.name = name;
    }
}

class MyName2 {
	@FieldMapper(serialize=BOOLEAN.FALSE, length=7)
    public String name;

    public MyName2(String name) {
        this.name = name;
    }
}


class MyName3 {
	@FieldMapper(serialize=BOOLEAN.TRUE, length=7)
    public String name;

    public MyName3(String name) {
        this.name = name;
    }
}


class MyName4 {
	@FieldMapper(serialize=BOOLEAN.BOTH, length=7)
    public String name;

    public MyName4(String name) {
        this.name = name;
    }
}


class MyName5 {
	@FieldMapper(length=7)
    public String name;

    public MyName5(String name) {
        this.name = name;
    }
}



class MyName6 {
	@FieldMapper(serialize=BOOLEAN.TRUE, length=7)
	@FieldMapper(serialize=BOOLEAN.FALSE, length=4)
    public String name;

    public MyName6(String name) {
        this.name = name;
    }
}



class MyName7 {
	@FieldMappers({
		@FieldMapper(serialize=BOOLEAN.TRUE, length=7),
		@FieldMapper(serialize=BOOLEAN.FALSE, length=4)
	})
    public String name;

    public MyName7(String name) {
        this.name = name;
    }
}


class MyName8 {
	@FieldMapper(serialize=BOOLEAN.TRUE, length=7)
	@FieldMapper(serialize=BOOLEAN.FALSE, length=4)
	@FieldMapper(serialize=BOOLEAN.TRUE, length=10)
	@FieldMapper(serialize=BOOLEAN.FALSE, length=6)
    public String name;

    public MyName8(String name) {
        this.name = name;
    }
}