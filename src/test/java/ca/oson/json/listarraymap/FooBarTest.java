package ca.oson.json.listarraymap;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.junit.Test;

import ca.oson.json.ComponentType;
import ca.oson.json.DataMapper;
import ca.oson.json.Oson.FieldData;
import ca.oson.json.OsonAssert;
import ca.oson.json.OsonAssert.MODE;
import ca.oson.json.function.DataMapper2JsonFunction;
import ca.oson.json.function.Json2FieldDataFunction;
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

		   ComponentType ctype = new ComponentType(map.getClass());
		   Map<Foo, Bar> map4 = oson.deserialize(json, ctype);
		   
		   
		   assertEquals(expected, oson.serialize(map2));

		   assertEquals(expected, oson.serialize(map4));

		   // System.err.println(oson.pretty().serialize(map));
	   }
	   
	   @Test
	   public void testSerializeDeserializeJackson() {
		   String expected = "{\"map\":[[{\"foo\":\"foo1\"},{\"bar\":\"bar1\"}],[{\"foo\":\"foo2\"},{\"bar\":\"bar2\"}]]}";
		   Baz baz = new Baz();
		   String json = oson.serialize(baz);

		   Baz baz2 = oson.deserialize(json, Baz.class);
		   
		   assertEquals(baz.map.getClass(), baz2.map.getClass());
	   }
	   
	   @Test
	   public void testSerializeDeserializeMapWithOson() {
		   String expected = "{\"map\":[[{\"foo\":\"foo1\"},{\"bar\":\"bar1\"}],[{\"foo\":\"foo2\"},{\"bar\":\"bar2\"}]]}";
		   
		   Baz baz = new Baz();
		   // following the map to list configuration
		   String json = oson.setMap2ListStyle(true).serialize(baz);
		   assertEquals(expected, json);
	   }
	   
	   
	   @Test
	   public void testSerializeDeserializeMap2List() {
		   Map<Foo, Bar> map = new LinkedHashMap<>();
		   map.put(new Foo("foo1"), new Bar("bar1"));
		   map.put(new Foo("foo2"), new Bar("bar2"));
		   map.put(new Foo("foo3"), new Bar("bar3"));
		   map.put(new Foo("foo4"), new Bar("bar4"));
		   
		   String expected = "[[{\"foo\":\"foo1\"},{\"bar\":\"bar1\"}],[{\"foo\":\"foo2\"},{\"bar\":\"bar2\"}],[{\"foo\":\"foo3\"},{\"bar\":\"bar3\"}],[{\"foo\":\"foo4\"},{\"bar\":\"bar4\"}]]";

		   // following the map to list configuration
		   String json = oson.setMap2ListStyle(true).serialize(map);
		   assertEquals(expected, json);
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



class Baz {

    Map<Foo, Bar> map;
    
    public Baz () {
    	map = new LinkedHashMap<>();
		   map.put(new Foo("foo1"), new Bar("bar1"));
		   map.put(new Foo("foo2"), new Bar("bar2"));
	   
    }

}