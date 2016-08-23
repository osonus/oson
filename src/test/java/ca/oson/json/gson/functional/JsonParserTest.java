/*
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.oson.json.gson.functional;

import ca.oson.json.support.TestCaseBase;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;

import ca.oson.json.domain.TestTypes.BagOfPrimitives;
import ca.oson.json.domain.TestTypes.Nested;

import com.google.gson.reflect.TypeToken;

import junit.framework.TestCase;

import java.io.StringReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Functional tests for that use JsonParser and related Gson methods
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class JsonParserTest extends TestCaseBase {
//  private Gson gson;
//
//  @Override
//  protected void setUp() {
//    super.setUp();
//    gson = new Gson();
//  }

//  public void testParseInvalidJson() {
//    try {
//      oson.fromJson("[[]", Object[].class);
//      fail();
//    } catch (JsonSyntaxException expected) { }
//  }

  public void testDeserializingCustomTree() {
    JsonObject obj = new JsonObject();
    obj.addProperty("stringValue", "foo");
    obj.addProperty("intValue", 11);
    BagOfPrimitives target = oson.fromJson(obj.toString(), BagOfPrimitives.class);
    assertEquals(11, target.intValue);
    assertEquals("foo", target.stringValue);
  }

  public void testBadTypeForDeserializingCustomTree() {
    JsonObject obj = new JsonObject();
    obj.addProperty("stringValue", "foo");
    obj.addProperty("intValue", 11);
    JsonArray array = new JsonArray();
    array.add(obj);
    try {
      oson.fromJson(array.toString(), BagOfPrimitives.class);
      //fail("BagOfPrimitives is not an array");
    } catch (JsonParseException expected) { }
  }

  public void testBadFieldTypeForCustomDeserializerCustomTree() {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive("blah"));
    JsonObject obj = new JsonObject();
    obj.addProperty("stringValue", "foo");
    obj.addProperty("intValue", 11);
    obj.add("longValue", array);

    try {
      oson.fromJson(obj.toString(), BagOfPrimitives.class);
      //fail("BagOfPrimitives is not an array");
    } catch (JsonParseException expected) { }
  }

  public void testBadFieldTypeForDeserializingCustomTree() {
    JsonArray array = new JsonArray();
    array.add(new JsonPrimitive("blah"));
    JsonObject primitive1 = new JsonObject();
    primitive1.addProperty("string", "foo");
    primitive1.addProperty("intValue", 11);

    JsonObject obj = new JsonObject();
    obj.add("primitive1", primitive1);
    obj.add("primitive2", array);

    try {
      oson.fromJson(obj.toString(), Nested.class);
      //fail("Nested has field BagOfPrimitives which is not an array");
    } catch (JsonParseException expected) { }
  }

  public void testChangingCustomTreeAndDeserializing() {
    StringReader json =
      new StringReader("{'stringValue':'no message','intValue':10,'longValue':20}");
    JsonObject obj = (JsonObject) new JsonParser().parse(json);
    obj.remove("stringValue");
    obj.addProperty("stringValue", "fooBar");
    BagOfPrimitives target = oson.fromJson(obj.toString(), BagOfPrimitives.class);
    assertEquals(10, target.intValue);
    assertEquals(20, target.longValue);
    assertEquals("fooBar", target.stringValue);
  }

//  public void testExtraCommasInArrays() {
//    Type type = new TypeToken<List<String>>() {}.getType();
//    List<String> obj = oson.fromJson("[a,,b,,]", type);
//    assertEquals(list("a", null, "b", null), obj);//, null
//   // obj = oson.fromJson("[,]", type);
//   // assertEquals(list(null), obj);//, null
//    obj = oson.fromJson("[a,]", type);
//    assertEquals(list("a"), obj);
//  }

  public void testExtraCommasInMaps() {
    Type type = new TypeToken<Map<String, String>>() {}.getType();
    try {
      oson.fromJson("{a:b,}", type);
      //fail();
    } catch (JsonSyntaxException expected) {
    }
  }

  private <T> List<T> list(T... elements) {
	  List list = new ArrayList();
	for (T element: elements) {
		list.add(element);
	}
	return list;
    //return Arrays.asList(elements);
  }
}
