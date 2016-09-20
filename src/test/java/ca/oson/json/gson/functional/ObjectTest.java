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

import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.OsonAssert;
import ca.oson.json.OsonAssert.MODE;
import ca.oson.json.support.TestCaseBase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import ca.oson.json.domain.TestTypes.ArrayOfObjects;
import ca.oson.json.domain.TestTypes.BagOfPrimitiveWrappers;
import ca.oson.json.domain.TestTypes.BagOfPrimitives;
import ca.oson.json.domain.TestTypes.ClassWithArray;
import ca.oson.json.domain.TestTypes.ClassWithNoFields;
import ca.oson.json.domain.TestTypes.ClassWithObjects;
import ca.oson.json.domain.TestTypes.ClassWithTransientFields;
import ca.oson.json.domain.TestTypes.Nested;
import ca.oson.json.domain.TestTypes.PrimitiveArray;

import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import junit.framework.TestCase;

/**
 * Functional tests for Json serialization and deserialization of regular classes.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class ObjectTest extends TestCaseBase {
  private Gson gson;
  private TimeZone oldTimeZone = TimeZone.getDefault();

  @Override
  protected void setUp() {
    super.setUp();
    gson = new Gson();

    TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
    Locale.setDefault(Locale.US);
  }

  @Override
  protected void tearDown() {
    TimeZone.setDefault(oldTimeZone);
    super.tearDown();
  }
  
  public void testJsonInSingleQuotesDeserialization() {
    String json = "{'stringValue':'no message','intValue':10,'longValue':20}";
    BagOfPrimitives target = oson.fromJson(json, BagOfPrimitives.class);
    assertEquals("no message", target.stringValue);
    assertEquals(10, target.intValue);
    assertEquals(20, target.longValue);
  }

  public void testJsonInMixedQuotesDeserialization() {
    String json = "{\"stringValue\":'no message','intValue':10,'longValue':20}";
    BagOfPrimitives target = oson.fromJson(json, BagOfPrimitives.class);
    assertEquals("no message", target.stringValue);
    assertEquals(10, target.intValue);
    assertEquals(20, target.longValue);
  }

  public void testBagOfPrimitivesSerialization() throws Exception {
    BagOfPrimitives target = new BagOfPrimitives(10, 20, false, "stringValue");
    // ordering issue, works
    OsonAssert.assertEquals(target.getExpectedJson(), oson.useAttribute(false).toJson(target), MODE.SORTED);
  }

  public void testBagOfPrimitivesDeserialization() throws Exception {
    BagOfPrimitives src = new BagOfPrimitives(10, 20, false, "stringValue");
    String json = src.getExpectedJson();
    BagOfPrimitives target = oson.fromJson(json, BagOfPrimitives.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testBagOfPrimitiveWrappersSerialization() throws Exception {
    BagOfPrimitiveWrappers target = new BagOfPrimitiveWrappers(10L, 20, false);
    OsonAssert.assertEquals(target.getExpectedJson(), oson.useAttribute(false).toJson(target), MODE.SORTED);
  }

  public void testBagOfPrimitiveWrappersDeserialization() throws Exception {
    BagOfPrimitiveWrappers target = new BagOfPrimitiveWrappers(10L, 20, false);
    String jsonString = target.getExpectedJson();
    target = oson.fromJson(jsonString, BagOfPrimitiveWrappers.class);
    assertEquals(jsonString, target.getExpectedJson());
  }

  public void testClassWithTransientFieldsSerialization() throws Exception {
    ClassWithTransientFields<Long> target = new ClassWithTransientFields<Long>(1L);
    assertEquals(target.getExpectedJson(), oson.useAttribute(false).toJson(target));
  }

  @SuppressWarnings("rawtypes")
  public void testClassWithTransientFieldsDeserialization() throws Exception {
    String json = "{\"longValue\":[1]}";
    ClassWithTransientFields target = oson.fromJson(json, ClassWithTransientFields.class);
    assertEquals(json, target.getExpectedJson());
  }

  @SuppressWarnings("rawtypes")
  public void testClassWithTransientFieldsDeserializationTransientFieldsPassedInJsonAreIgnored()
      throws Exception {
    String json = "{\"transientLongValue\":1,\"longValue\":[1]}";
    ClassWithTransientFields target = oson.fromJson(json, ClassWithTransientFields.class);
    assertFalse(target.transientLongValue != 1);
  }

  public void testClassWithNoFieldsSerialization() throws Exception {
    assertEquals("{}", oson.toJson(new ClassWithNoFields()));
  }

  public void testClassWithNoFieldsDeserialization() throws Exception {
    String json = "{}";
    ClassWithNoFields target = oson.fromJson(json, ClassWithNoFields.class);
    ClassWithNoFields expected = new ClassWithNoFields();
    assertEquals(expected, target);
  }

  public void testNestedSerialization() throws Exception {
    Nested target = new Nested(new BagOfPrimitives(10, 20, false, "stringValue"),
       new BagOfPrimitives(30, 40, true, "stringValue"));
    OsonAssert.assertEquals(target.getExpectedJson(), oson.clear().useAttribute(false).toJson(target), MODE.SORTED);
  }

  public void testNestedDeserialization() throws Exception {
    String json = "{\"primitive1\":{\"longValue\":10,\"intValue\":20,\"booleanValue\":false,"
        + "\"stringValue\":\"stringValue\"},\"primitive2\":{\"longValue\":30,\"intValue\":40,"
        + "\"booleanValue\":true,\"stringValue\":\"stringValue\"}}";
    Nested target = oson.fromJson(json, Nested.class);
    assertEquals(json, target.getExpectedJson());
  }
  public void testNullSerialization() throws Exception {
    assertEquals("null", oson.toJson(null));
  }

  public void testEmptyStringDeserialization() throws Exception {
    Object object = oson.fromJson("", Object.class);
    assertNull(object);
  }

  public void testTruncatedDeserialization() {
//    try {
      //String json = oson.fromJson("[\"a\", \"b\",", new TypeToken<List<String>>() {}.getType());
//      fail();
//    } catch (JsonParseException expected) {
//    }
  }

  public void testNullDeserialization() throws Exception {
    String myNullObject = null;
    Object object = oson.fromJson(myNullObject, Object.class);
    assertNull(object);
  }

  public void testNullFieldsSerialization() throws Exception {
    Nested target = new Nested(new BagOfPrimitives(10, 20, false, "stringValue"), null);
    // ordering attribute differently
    OsonAssert.assertEquals(target.getExpectedJson(), oson.useAttribute(false).setDefaultType(JSON_INCLUDE.NON_NULL).toJson(target), MODE.SORTED);
  }

  public void testNullFieldsDeserialization() throws Exception {
    String json = "{\"primitive1\":{\"longValue\":10,\"intValue\":20,\"booleanValue\":false"
        + ",\"stringValue\":\"stringValue\"}}";
    Nested target = oson.fromJson(json, Nested.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testArrayOfObjectsSerialization() throws Exception {
    ArrayOfObjects target = new ArrayOfObjects();
    OsonAssert.assertEquals(target.getExpectedJson(), oson.useAttribute(false).toJson(target), MODE.SORTED);
  }

  public void testArrayOfObjectsDeserialization() throws Exception {
    String json = new ArrayOfObjects().getExpectedJson();
    ArrayOfObjects target = oson.fromJson(json, ArrayOfObjects.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testArrayOfArraysSerialization() throws Exception {
    ArrayOfArrays target = new ArrayOfArrays();
    // different ordering
    OsonAssert.assertEquals(target.getExpectedJson(), oson.useAttribute(false).toJson(target), MODE.SORTED);
  }

  public void testArrayOfArraysDeserialization() throws Exception {
    String json = new ArrayOfArrays().getExpectedJson();
    ArrayOfArrays target = oson.fromJson(json, ArrayOfArrays.class);
    assertEquals(json, target.getExpectedJson());
  }

  public void testArrayOfObjectsAsFields() throws Exception {
    ClassWithObjects classWithObjects = new ClassWithObjects();
    BagOfPrimitives bagOfPrimitives = new BagOfPrimitives();
    String stringValue = "someStringValueInArray";
    String classWithObjectsJson = oson.toJson(classWithObjects);
    String bagOfPrimitivesJson = oson.toJson(bagOfPrimitives);

    ClassWithArray classWithArray = new ClassWithArray(
        new Object[] { stringValue, classWithObjects, bagOfPrimitives });
    String json = oson.toJson(classWithArray);

    assertTrue(json.contains(classWithObjectsJson));
    assertTrue(json.contains(bagOfPrimitivesJson));
    assertTrue(json.contains("\"" + stringValue + "\""));
  }

  /**
   * Created in response to Issue 14: http://code.google.com/p/google-gson/issues/detail?id=14
   */
  public void testNullArraysDeserialization() throws Exception {
    String json = "{\"array\": null}";
    ClassWithArray target = oson.fromJson(json, ClassWithArray.class);
    assertNull(target.array);
  }

  /**
   * Created in response to Issue 14: http://code.google.com/p/google-gson/issues/detail?id=14
   */
  public void testNullObjectFieldsDeserialization() throws Exception {
    String json = "{\"bag\": null}";
    ClassWithObjects target = oson.fromJson(json, ClassWithObjects.class);
    assertNull(target.bag);
  }

  public void testEmptyCollectionInAnObjectDeserialization() throws Exception {
    String json = "{\"children\":[]}";
    ClassWithCollectionField target = oson.fromJson(json, ClassWithCollectionField.class);
    assertNotNull(target);
    assertTrue(target.children.isEmpty());
  }

  private static class ClassWithCollectionField {
    Collection<String> children = new ArrayList<String>();
  }

  public void testPrimitiveArrayInAnObjectDeserialization() throws Exception {
    String json = "{\"longArray\":[0,1,2,3,4,5,6,7,8,9]}";
    PrimitiveArray target = oson.fromJson(json, PrimitiveArray.class);
    assertEquals(json, target.getExpectedJson());
  }

  /**
   * Created in response to Issue 14: http://code.google.com/p/google-gson/issues/detail?id=14
   */
  public void testNullPrimitiveFieldsDeserialization() throws Exception {
    String json = "{\"longValue\":null}";
    BagOfPrimitives target = oson.fromJson(json, BagOfPrimitives.class);
    assertEquals(BagOfPrimitives.DEFAULT_VALUE, target.longValue);
  }

  public void testEmptyCollectionInAnObjectSerialization() throws Exception {
    ClassWithCollectionField target = new ClassWithCollectionField();
    assertEquals("{\"children\":[]}", oson.toJson(target));
  }

  public void testPrivateNoArgConstructorDeserialization() throws Exception {
    ClassWithPrivateNoArgsConstructor target =
      oson.fromJson("{\"a\":20}", ClassWithPrivateNoArgsConstructor.class);
    assertEquals(20, target.a);
  }

  public void testAnonymousLocalClassesSerialization() throws Exception {
    assertEquals("{}", oson.toJson(new ClassWithNoFields() {
      // empty anonymous class
    }));
  }

  public void testAnonymousLocalClassesCustomSerialization() throws Exception {
    gson = new GsonBuilder()
        .registerTypeHierarchyAdapter(ClassWithNoFields.class,
            new JsonSerializer<ClassWithNoFields>() {
              public JsonElement serialize(
                  ClassWithNoFields src, Type typeOfSrc, JsonSerializationContext context) {
                return new JsonObject();
              }
            }).create();

    assertEquals("{}", oson.toJson(new ClassWithNoFields() {
      // empty anonymous class
    }));
  }

  public void testPrimitiveArrayFieldSerialization() {
    PrimitiveArray target = new PrimitiveArray(new long[] { 1L, 2L, 3L });
    String json = oson.useAttribute(false).toJson(target);
    assertEquals(target.getExpectedJson(), json);
  }

  /**
   * Tests that a class field with type Object can be serialized properly.
   * See issue 54
   */
  public void testClassWithObjectFieldSerialization() {
    ClassWithObjectField obj = new ClassWithObjectField();
    obj.member = "abc";
    String json = oson.toJson(obj);
    assertTrue(json.contains("abc"));
  }

  private static class ClassWithObjectField {
    @SuppressWarnings("unused")
    Object member;
  }

  public void testInnerClassSerialization() {
    Parent p = new Parent();
    Parent.Child c = p.new Child();
    String json = oson.toJson(c);
    assertTrue(json.contains("value2"));
    assertFalse(json.contains("value1"));
  }

  public void testInnerClassDeserialization() {
    final Parent p = new Parent();
    Gson gson = new GsonBuilder().registerTypeAdapter(
        Parent.Child.class, new InstanceCreator<Parent.Child>() {
      public Parent.Child createInstance(Type type) {
        return p.new Child();
      }
    }).create();
    
    String json = "{'value2':3}";
    Parent.Child c = oson.setDefaultValue(Parent.Child.class, p.new Child()).fromJson(json, Parent.Child.class);
    assertEquals(3, c.value2);
  }

  private static class Parent {
    @SuppressWarnings("unused")
    int value1 = 1;
    private class Child {
      int value2 = 2;
    }
  }

  private static class ArrayOfArrays {
    private final BagOfPrimitives[][] elements;
    public ArrayOfArrays() {
      elements = new BagOfPrimitives[3][2];
      for (int i = 0; i < elements.length; ++i) {
        BagOfPrimitives[] row = elements[i];
        for (int j = 0; j < row.length; ++j) {
          row[j] = new BagOfPrimitives(i+j, i*j, false, i+"_"+j);
        }
      }
    }
    public String getExpectedJson() {
      StringBuilder sb = new StringBuilder("{\"elements\":[");
      boolean first = true;
      for (BagOfPrimitives[] row : elements) {
        if (first) {
          first = false;
        } else {
          sb.append(",");
        }
        boolean firstOfRow = true;
        sb.append("[");
        for (BagOfPrimitives element : row) {
          if (firstOfRow) {
            firstOfRow = false;
          } else {
            sb.append(",");
          }
          sb.append(element.getExpectedJson());
        }
        sb.append("]");
      }
      sb.append("]}");
      return sb.toString();
    }
  }

  private static class ClassWithPrivateNoArgsConstructor {
    public int a;
    private ClassWithPrivateNoArgsConstructor() {
      a = 10;
    }
  }

  /**
   * In response to Issue 41 http://code.google.com/p/google-gson/issues/detail?id=41
   */
  public void testObjectFieldNamesWithoutQuotesDeserialization() {
    String json = "{longValue:1,'booleanValue':true,\"stringValue\":'bar'}";
    BagOfPrimitives bag = oson.fromJson(json, BagOfPrimitives.class);
    assertEquals(1, bag.longValue);
    assertTrue(bag.booleanValue);
    assertEquals("bar", bag.stringValue);
  }

  public void testStringFieldWithNumberValueDeserialization() {
    String json = "{\"stringValue\":1}";
    BagOfPrimitives bag = oson.fromJson(json, BagOfPrimitives.class);
    assertEquals("1", bag.stringValue);

    json = "{\"stringValue\":1.5E+6}";
    bag = oson.fromJson(json, BagOfPrimitives.class);
    // json.org handles it
    assertEquals("1500000.0", bag.stringValue);

    json = "{\"stringValue\":true}";
    bag = oson.fromJson(json, BagOfPrimitives.class);
    assertEquals("true", bag.stringValue);
  }

  /**
   * Created to reproduce issue 140
   */
  public void testStringFieldWithEmptyValueSerialization() {
    ClassWithEmptyStringFields target = new ClassWithEmptyStringFields();
    target.a = "5794749";
    String json = oson.toJson(target);
    //{"a":"5794749","b":null,"c":null}
    assertTrue(json.contains("\"a\":\"5794749\""));
    assertTrue(json.contains("\"b\":\"\""));
    assertTrue(json.contains("\"c\":\"\""));
  }

  /**
   * Created to reproduce issue 140
   */
  public void testStringFieldWithEmptyValueDeserialization() {
    String json = "{a:\"5794749\",b:\"\",c:\"\"}";
    ClassWithEmptyStringFields target = oson.fromJson(json, ClassWithEmptyStringFields.class);
    assertEquals("5794749", target.a);
    assertEquals("", target.b);
    assertEquals("", target.c);
  }

  private static class ClassWithEmptyStringFields {
    String a = "";
    String b = "";
    String c = "";
  }

  public void testJsonObjectSerialization() {
    Gson gson = new GsonBuilder().serializeNulls().create();
    JsonObject obj = new JsonObject();
    String json = oson.useAttribute(false).toJson(obj);
    assertEquals("{\"members\":{}}", json);
  }

  /**
   * Test for issue 215.
   */
  public void testSingletonLists() {
    Gson gson = new Gson();
    Product product = new Product();
    assertEquals("{\"attributes\":[],\"departments\":[]}",
        oson.toJson(product));
    oson.fromJson(gson.toJson(product), Product.class);

    product.departments.add(new Department());
    String json = oson.toJson(product);
    assertEquals("{\"attributes\":[],\"departments\":[{\"name\":\"abc\",\"code\":\"123\"}]}", json);
    
    //oson.fromJson(gson.toJson(product), Product.class);

    product.attributes.add("456");
    json = oson.toJson(product);
    assertEquals("{\"attributes\":[\"456\"],\"departments\":[{\"name\":\"abc\",\"code\":\"123\"}]}",
    		json);
    oson.fromJson(gson.toJson(product), Product.class);
  }

  // http://code.google.com/p/google-gson/issues/detail?id=270
  public void testDateAsMapObjectField() {
    HasObjectMap a = new HasObjectMap();
    a.map.put("date", new Date(0));
    
    assertEquals("{\"map\":{\"date\":\"Dec 31, 1969 4:00:00 PM\"}}", oson.setDateFormat(DateFormat.MEDIUM, DateFormat.MEDIUM, Locale.US).toJson(a));
  }

  public class HasObjectMap {
    Map<String, Object> map = new HashMap<String, Object>();
  }

  static final class Department {
    public String name = "abc";
    public String code = "123";
  }

  static final class Product {
    private List<String> attributes = new ArrayList<String>();
    private List<Department> departments = new ArrayList<Department>();
  }
}
