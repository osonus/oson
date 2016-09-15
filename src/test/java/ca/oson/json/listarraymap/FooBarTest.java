package ca.oson.json.listarraymap;

import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import com.google.gson.reflect.TypeToken;

import ca.oson.json.ComponentType;
import ca.oson.json.support.TestCaseBase;

public class FooBarTest extends TestCaseBase {
	   @Test
	   public void testSerializeDeserializeMapWithObjectKey() {
		   Map<Foo, Bar> map = new LinkedHashMap<>();
		   map.put(new Foo("foo1"), new Bar("bar1"));
		   map.put(new Foo("foo2"), new Bar("bar2"));
		   
		   String expected = "{{\"foo\":\"foo1\"}:{\"bar\":\"bar1\"},{\"foo\":\"foo2\"}:{\"bar\":\"bar2\"}}";
		   
		   String json = oson.serialize(map);
		   assertEquals(expected, json);
		   // jackson: {"ca.oson.json.listarraymap.Foo@5f5a92bb":{"bar":"bar1"},"ca.oson.json.listarraymap.Foo@6fdb1f78":{"bar":"bar2"}}
		   // gson: {"ca.oson.json.listarraymap.Foo@5f5a92bb":{"bar":"bar1"},"ca.oson.json.listarraymap.Foo@6fdb1f78":{"bar":"bar2"}}

		   Map<Foo, Bar> map2 = oson.deserialize(json, map.getClass());
		   
		   Type targetType = new TypeToken<Map<Foo, Bar>>() {}.getType();
		   Map<Foo, Bar> map3 = oson.deserialize(json, map.getClass());
		   
		   ComponentType ctype = new ComponentType(map.getClass());
		   Map<Foo, Bar> map4 = oson.deserialize(json, ctype);
		   
		   
		   assertEquals(expected, oson.serialize(map2));
		   assertEquals(expected, oson.serialize(map3));
		   assertEquals(expected, oson.serialize(map4));
	   }
}

class Foo {
    public String foo;
    public Foo(String foo) {
        this.foo = foo;
    }
}

class Bar {
    public String bar;
    public Bar(String bar) {
        this.bar = bar;
    }
}