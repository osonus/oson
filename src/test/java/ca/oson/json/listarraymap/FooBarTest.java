package ca.oson.json.listarraymap;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.gson.reflect.TypeToken;

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
		   
		   Type targetType = new TypeToken<Map<Foo, Bar>>() {}.getType();
		   Map<Foo, Bar> map3 = oson.deserialize(json, targetType);
		   
		   ComponentType ctype = new ComponentType(map.getClass());
		   Map<Foo, Bar> map4 = oson.deserialize(json, ctype);
		   
		   
		   assertEquals(expected, oson.serialize(map2));
		   assertEquals(expected, oson.serialize(map3));
		   assertEquals(expected, oson.serialize(map4));

		   // System.err.println(oson.pretty().serialize(map));
	   }
	   
	   @Test
	   public void testSerializeDeserializeJackson() {
		   String expected = "{\"map\":[[{\"foo\":\"foo1\"},{\"bar\":\"bar1\"}],[{\"foo\":\"foo2\"},{\"bar\":\"bar2\"}]]}";
		   Baz baz = new Baz();
		   String json = oson.asJackson().serialize(baz);

		   Baz baz2 = oson.deserialize(json, Baz.class);
		   
		   assertEquals(baz.map.getClass(), baz2.map.getClass());
		   
		   String json2 = oson.serialize(baz2);

		   assertEquals(json, json2);
	   }
	   
	   
//	   @Test
//	   public void testSerializeDeserializeOson() {
//		   DataMapper2JsonFunction serializer = (DataMapper p) -> {
//			   Map map = (Map)p.getObj();
//			   
//			   List list = new ArrayList();
//			   
//		        for (Object key : map.keySet()) {
//		        	list.add(key);
//		        	list.add(map.get(key));
//		        }
//
//			   return oson.serialize(list);
//		   };
//		   
//		   Json2FieldDataFunction deserializer = (FieldData p) -> {
//			   Map<Object, Object> map2 = (Map)p.valueToProcess;
//
////			   Map map = new LinkedHashMap();
////			   for (Map.Entry<Object, Object> entry : map2.entrySet()) {
////		        	System.err.println(entry.getKey());
////		        	System.err.println(entry.getValue());
////		        }
//			   
//			   return map2;
//		   };
//
//		   oson.clear().setSerializer(LinkedHashMap.class, serializer).setDeserializer(Map.class, deserializer);
//		   
//		   String expected = "{\"map\":[[{\"foo\":\"foo1\"},{\"bar\":\"bar1\"}],[{\"foo\":\"foo2\"},{\"bar\":\"bar2\"}]]}";
//		   Baz baz = new Baz();
//		   String json = oson.serialize(baz);
//
//		   Baz baz2 = oson.deserialize(json, Baz.class);
//		   
//		   assertEquals(baz.map.getClass(), baz2.map.getClass());
//		   
//		   String json2 = oson.serialize(baz2);
//		   //{"map":[{"foo":"foo1"},{"bar":"bar1"},{"foo":"foo2"},{"bar":"bar2"}]}
//		   //{"map":[["foo","foo1"],["bar","bar1"],["foo","foo2"],["bar","bar2"]]}
//		   assertEquals(json, json2);
//	   }
	   
	   
	   @Test
	   public void testSerializeDeserializeMapWithOson() {
		   String expected = "{\"map\":[[{\"foo\":\"foo1\"},{\"bar\":\"bar1\"}],[{\"foo\":\"foo2\"},{\"bar\":\"bar2\"}]]}";
		   
		   Baz baz = new Baz();
		   // following the map to list configuration
		   String json = oson.setMap2ListStyle(true).serialize(baz);
		   assertEquals(expected, json);

		   Baz baz2 = oson.deserialize(json, Baz.class);
		   
		   assertEquals(expected, oson.serialize(baz2));
		   
		   OsonAssert.assertEquals(baz, baz2, MODE.EXACT);
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

		   Map<Foo, Bar> map2 = (Map<Foo, Bar>) oson.deserialize(json, new TypeToken<Map<Foo, Bar>>() {}.getType());
		   
		   assertEquals(expected, oson.serialize(map2));
		   
		   OsonAssert.assertEquals(map, map2, MODE.EXACT);
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

class ArrayToMapDeserializer extends JsonDeserializer<Map<?, ?>> {

	@Override
	public Map<?, ?> deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		Map map = new LinkedHashMap();

        ObjectCodec oc = p.getCodec();
        JsonNode anode = oc.readTree(p);

		for (int i = 0; i < anode.size(); i++) {
			JsonNode node = anode.get(i);
			map.put(node.get(0), node.get(1));
		}
		
		return map;
	}
}

class MapToArraySerializer extends JsonSerializer<Map<?, ?>> {

    @Override
    public void serialize(Map<?, ?> value, JsonGenerator gen, SerializerProvider serializers)
        throws IOException, JsonProcessingException {
        gen.writeStartArray();
        for (Map.Entry<?, ?> entry : value.entrySet()) {
            gen.writeStartArray();
            gen.writeObject(entry.getKey());
            gen.writeObject(entry.getValue());
            gen.writeEndArray();
        }
        gen.writeEndArray();
    }

}

class Baz {

    @JsonSerialize(using = MapToArraySerializer.class)
    @JsonDeserialize(using = ArrayToMapDeserializer.class, keyAs = Foo.class, contentAs = Bar.class)
    Map<Foo, Bar> map;
    
    public Baz () {
    	map = new LinkedHashMap<>();
		   map.put(new Foo("foo1"), new Bar("bar1"));
		   map.put(new Foo("foo2"), new Bar("bar2"));
	   
    }

}