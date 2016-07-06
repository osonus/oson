package ca.oson.json.annotation;

import org.junit.Test;

import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.support.TestCaseBase;

public class ClassMapperSeDeseTest extends TestCaseBase {
	@Test
	public void testDeSerializeAnyName() {
		AnyName anyName = new AnyName("This is a testing on ClassMapper");
		
		String expected = "{\"name\":\"This is a testing on ClassMapper\"}";
		
		String json = oson.serialize(anyName);
		
		assertEquals(expected, json);
		
		AnyName anyName2 = oson.deserialize(json, AnyName.class);
		
		expected = "This is a testing on ClassMapper";
		
		assertEquals(expected, anyName2.name);
	}
	

	@Test
	public void testDeSerializeAnyName2() {
		AnyName2 anyName = new AnyName2("This is a testing on ClassMapper");
		
		String expected = "{\"name\":\"This is a testing on ClassMapper\"}";
		
		String json = oson.clear().serialize(anyName);
		
		assertEquals(expected, json);
		
		AnyName2 anyName2 = oson.deserialize(json, AnyName2.class);
		
		expected = "This is";
		
		assertEquals(expected, anyName2.name);
	}
	
	@Test
	public void testDeSerializeAnyName3() {
		AnyName3 anyName = new AnyName3("This is a testing on ClassMapper");
		
		String expected = "{\"name\":\"This is\"}";
		
		String json = oson.clear().serialize(anyName);
		
		assertEquals(expected, json);
		
		json = "{\"name\":\"This is a testing on ClassMapper\"}";
		
		AnyName3 anyName2 = oson.deserialize(json, AnyName3.class);
		
		expected = "This is a testing on ClassMapper";
		
		assertEquals(expected, anyName2.name);
	}
	
	@Test
	public void testDeSerializeAnyName4() {
		AnyName4 anyName = new AnyName4("This is a testing on ClassMapper");
		
		String expected = "{\"name\":\"This is\"}";
		
		String json = oson.clear().serialize(anyName);
		
		assertEquals(expected, json);
		
		json = "{\"name\":\"This is a testing on ClassMapper\"}";
		
		AnyName4 anyName2 = oson.deserialize(json, AnyName4.class);
		
		expected = "This is";
		
		assertEquals(expected, anyName2.name);
	}
	
	
	@Test
	public void testDeSerializeAnyName5() {
		AnyName5 anyName = new AnyName5("This is a testing on ClassMapper");
		
		String expected = "{\"name\":\"This is\"}";
		
		String json = oson.clear().serialize(anyName);
		
		assertEquals(expected, json);
		
		json = "{\"name\":\"This is a testing on ClassMapper\"}";
		
		AnyName5 anyName2 = oson.deserialize(json, AnyName5.class);
		
		expected = "This is";
		
		assertEquals(expected, anyName2.name);
	}
	
	@Test
	public void testDeSerializeAnyName6() {
		AnyName6 anyName = new AnyName6("This is a testing on ClassMapper");
		
		String expected = "{\"name\":\"This is\"}";
		
		String json = oson.clear().serialize(anyName);
		
		assertEquals(expected, json);
		
		json = "{\"name\":\"This is a testing on ClassMapper\"}";
		
		AnyName6 anyName2 = oson.deserialize(json, AnyName6.class);
		
		expected = "This";
		
		assertEquals(expected, anyName2.name);
	}
	
	@Test
	public void testDeSerializeAnyName7() {
		AnyName7 anyName = new AnyName7("This is a testing on ClassMapper");
		
		String expected = "{\"name\":\"This is\"}";
		
		String json = oson.clear().serialize(anyName);
		
		assertEquals(expected, json);
		
		json = "{\"name\":\"This is a testing on ClassMapper\"}";
		
		AnyName7 anyName2 = oson.deserialize(json, AnyName7.class);
		
		expected = "This";
		
		assertEquals(expected, anyName2.name);
	}
	
	
	@Test
	public void testDeSerializeAnyName8() {
		AnyName8 anyName = new AnyName8("This is a testing on ClassMapper");
		
		String expected = "{\"name\":\"This is a \"}";
		
		String json = oson.clear().serialize(anyName);
		
		assertEquals(expected, json);
		
		json = "{\"name\":\"This is a testing on ClassMapper\"}";
		
		AnyName8 anyName2 = oson.deserialize(json, AnyName8.class);
		
		expected = "This i";
		
		assertEquals(expected, anyName2.name);
	}
}

class AnyName {
    public String name;

    public AnyName(String name) {
        this.name = name;
    }
}

@ClassMapper(serialize=BOOLEAN.FALSE, length=7)
class AnyName2 {
    public String name;

    public AnyName2(String name) {
        this.name = name;
    }
}

@ClassMapper(serialize=BOOLEAN.TRUE, length=7)
class AnyName3 {
    public String name;

    public AnyName3(String name) {
        this.name = name;
    }
}

@ClassMapper(serialize=BOOLEAN.BOTH, length=7)
class AnyName4 {
    public String name;

    public AnyName4(String name) {
        this.name = name;
    }
}

@ClassMapper(length=7)
class AnyName5 {
    public String name;

    public AnyName5(String name) {
        this.name = name;
    }
}


@ClassMapper(serialize=BOOLEAN.TRUE, length=7)
@ClassMapper(serialize=BOOLEAN.FALSE, length=4)
class AnyName6 {
    public String name;

    public AnyName6(String name) {
        this.name = name;
    }
}


@ClassMappers({
	@ClassMapper(serialize=BOOLEAN.TRUE, length=7),
	@ClassMapper(serialize=BOOLEAN.FALSE, length=4)
})
class AnyName7 {
    public String name;

    public AnyName7(String name) {
        this.name = name;
    }
}


@ClassMapper(serialize=BOOLEAN.TRUE, length=7)
@ClassMapper(serialize=BOOLEAN.FALSE, length=4)
@ClassMapper(serialize=BOOLEAN.TRUE, length=10)
@ClassMapper(serialize=BOOLEAN.FALSE, length=6)
class AnyName8 {
    public String name;

    public AnyName8(String name) {
        this.name = name;
    }
}
