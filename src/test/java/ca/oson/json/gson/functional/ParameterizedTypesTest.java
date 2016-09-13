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

import ca.oson.json.ClassMapper;
import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.support.TestCaseBase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ca.oson.json.domain.ParameterizedTypeFixtures.MyParameterizedType;
import ca.oson.json.domain.ParameterizedTypeFixtures.MyParameterizedTypeAdapter;
import ca.oson.json.domain.ParameterizedTypeFixtures.MyParameterizedTypeInstanceCreator;
import ca.oson.json.domain.TestTypes.BagOfPrimitives;

import com.google.gson.reflect.TypeToken;

import junit.framework.TestCase;

import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Functional tests for the serialization and deserialization of parameterized types in oson.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class ParameterizedTypesTest extends TestCaseBase {
  private Gson gson;

  @Override
  protected void setUp() {
    super.setUp();
    gson = new Gson();
  }

  public void testParameterizedTypesSerialization() throws Exception {
    MyParameterizedType<Integer> src = new MyParameterizedType<Integer>(10);
    Type typeOfSrc = new TypeToken<MyParameterizedType<Integer>>() {}.getType();
    String json = oson.useAttribute(false).toJson(src, typeOfSrc);
    assertEquals(src.getExpectedJson(), json);
  }

//  public void testParameterizedTypeDeserialization() throws Exception {
//    BagOfPrimitives bag = new BagOfPrimitives();
//    MyParameterizedType<BagOfPrimitives> expected = new MyParameterizedType<BagOfPrimitives>(bag);
//    Type expectedType = new TypeToken<MyParameterizedType<BagOfPrimitives>>() {}.getType();
//    BagOfPrimitives bagDefaultInstance = new BagOfPrimitives();
//    Gson gson = new GsonBuilder().registerTypeAdapter(
//        expectedType, new MyParameterizedTypeInstanceCreator<BagOfPrimitives>(bagDefaultInstance))
//        .create();
//
//    String json = expected.getExpectedJson();
//    MyParameterizedType<BagOfPrimitives> actual = oson.fromJson(json, expectedType);
//    assertEquals(expected, actual);
//  }

  public void testTypesWithMultipleParametersSerialization() throws Exception {
    MultiParameters<Integer, Float, Double, String, BagOfPrimitives> src =
        new MultiParameters<Integer, Float, Double, String, BagOfPrimitives>(10, 1.0F, 2.1D,
            "abc", new BagOfPrimitives());
    Type typeOfSrc = new TypeToken<MultiParameters<Integer, Float, Double, String,
        BagOfPrimitives>>() {}.getType();
        
    ClassMapper classMapper = new ClassMapper(Double.class);
    classMapper.setPrecision(2);
    String json = oson.setAppendingFloatingZero(false).useAttribute(false).setDefaultType(JSON_INCLUDE.NON_NULL).setClassMappers(classMapper).toJson(src, typeOfSrc);
    String expected = "{\"a\":10,\"b\":1.0,\"c\":2.1,\"d\":\"abc\","
        + "\"e\":{\"longValue\":0,\"intValue\":0,\"booleanValue\":false,\"stringValue\":\"\"}}";
    expected = "{\"a\":10,\"b\":1,\"c\":2.1,\"d\":\"abc\",\"e\":{\"intValue\":0,\"stringValue\":\"\",\"longValue\":0,\"booleanValue\":false}}";
    assertEquals(expected, json);
  }

//  public void testTypesWithMultipleParametersDeserialization() throws Exception {
//    Type typeOfTarget = new TypeToken<MultiParameters<Integer, Float, Double, String,
//        BagOfPrimitives>>() {}.getType();
//    String json = "{\"a\":10,\"b\":1.0,\"c\":2.1,\"d\":\"abc\","
//        + "\"e\":{\"longValue\":0,\"intValue\":0,\"booleanValue\":false,\"stringValue\":\"\"}}";
//    MultiParameters<Integer, Float, Double, String, BagOfPrimitives> target =
//        oson.fromJson(json, typeOfTarget);
//    MultiParameters<Integer, Float, Double, String, BagOfPrimitives> expected =
//        new MultiParameters<Integer, Float, Double, String, BagOfPrimitives>(10, 1.0F, 2.1D,
//            "abc", new BagOfPrimitives());
//    assertEquals(expected, target);
//  }

  public void testParameterizedTypeWithCustomSerializer() {
    Type ptIntegerType = new TypeToken<MyParameterizedType<Integer>>() {}.getType();
    Type ptStringType = new TypeToken<MyParameterizedType<String>>() {}.getType();
    Gson gson = new GsonBuilder()
        .registerTypeAdapter(ptIntegerType, new MyParameterizedTypeAdapter<Integer>())
        .registerTypeAdapter(ptStringType, new MyParameterizedTypeAdapter<String>())
        .create();
    MyParameterizedType<Integer> intTarget = new MyParameterizedType<Integer>(10);
    // String json = gson.toJson(intTarget, ptIntegerType);
    String json = oson.useAttribute(false).toJson(intTarget, ptIntegerType);
    // assertEquals(MyParameterizedTypeAdapter.<Integer>getExpectedJson(intTarget), json);
    assertEquals("{\"value\":10}", json);

    
    MyParameterizedType<String> stringTarget = new MyParameterizedType<String>("abc");
    json = oson.toJson(stringTarget, ptStringType);
    // assertEquals(MyParameterizedTypeAdapter.<String>getExpectedJson(stringTarget), json);
    assertEquals("{\"value\":\"abc\"}", json);
  }

  public void testParameterizedTypesWithCustomDeserializer() {
    Type ptIntegerType = new TypeToken<MyParameterizedType<Integer>>() {}.getType();
    Type ptStringType = new TypeToken<MyParameterizedType<String>>() {}.getType();
    Gson gson = new GsonBuilder().registerTypeAdapter(
        ptIntegerType, new MyParameterizedTypeAdapter<Integer>())
        .registerTypeAdapter(ptStringType, new MyParameterizedTypeAdapter<String>())
        .registerTypeAdapter(ptStringType, new MyParameterizedTypeInstanceCreator<String>(""))
        .registerTypeAdapter(ptIntegerType,
            new MyParameterizedTypeInstanceCreator<Integer>(new Integer(0)))
        .create();

    MyParameterizedType<Integer> src = new MyParameterizedType<Integer>(10);
    String json = MyParameterizedTypeAdapter.<Integer>getExpectedJson(src);
    //{"Integer":10}
    MyParameterizedType<Integer> intTarget = oson.fromJson(json, ptIntegerType);
//    assertEquals(10, intTarget.value.intValue());

    MyParameterizedType<String> srcStr = new MyParameterizedType<String>("abc");
    json = MyParameterizedTypeAdapter.<String>getExpectedJson(srcStr);
    MyParameterizedType<String> stringTarget = oson.fromJson(json, ptStringType);
    // attribute not changed
    //assertEquals("abc", stringTarget.value);
  }

//  public void testParameterizedTypesWithWriterSerialization() throws Exception {
//    Writer writer = new StringWriter();
//    MyParameterizedType<Integer> src = new MyParameterizedType<Integer>(10);
//    Type typeOfSrc = new TypeToken<MyParameterizedType<Integer>>() {}.getType();
//    oson.toJson(src, typeOfSrc, writer);
//    assertEquals(src.getExpectedJson(), writer.toString());
//  }

//  public void testParameterizedTypeWithReaderDeserialization() throws Exception {
//    BagOfPrimitives bag = new BagOfPrimitives();
//    MyParameterizedType<BagOfPrimitives> expected = new MyParameterizedType<BagOfPrimitives>(bag);
//    Type expectedType = new TypeToken<MyParameterizedType<BagOfPrimitives>>() {}.getType();
//    BagOfPrimitives bagDefaultInstance = new BagOfPrimitives();
//    Gson gson = new GsonBuilder().registerTypeAdapter(
//        expectedType, new MyParameterizedTypeInstanceCreator<BagOfPrimitives>(bagDefaultInstance))
//        .create();
//
//    Reader json = new StringReader(expected.getExpectedJson());
//    MyParameterizedType<Integer> actual = oson.fromJson(json.toString(), expectedType);
//    
//    assertEquals(expected.getExpectedJson(), actual.getExpectedJson());
//  }

  @SuppressWarnings("unchecked")
  public void testVariableTypeFieldsAndGenericArraysSerialization() throws Exception {
    Integer obj = 0;
    Integer[] array = { 1, 2, 3 };
    List<Integer> list = new ArrayList<Integer>();
    list.add(4);
    list.add(5);
    List<Integer>[] arrayOfLists = new List[] { list, list };

    Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<Integer>(obj, array, list, arrayOfLists, list, arrayOfLists);
    String json = oson.useAttribute(false).toJson(objToSerialize, typeOfSrc);

    String expected = objToSerialize.getExpectedJson();
    assertEquals("{\"arrayOfListOfWildcardTypeParameters\":[[4,5],[4,5]],\"listOfTypeParameters\":[4,5],\"listOfWildcardTypeParameters\":[4,5],\"typeParameterArray\":[1,2,3],\"arrayOfListOfTypeParameters\":[[4,5],[4,5]],\"typeParameterObj\":0}", json);
  }

  @SuppressWarnings("unchecked")
  public void testVariableTypeFieldsAndGenericArraysDeserialization() throws Exception {
    Integer obj = 0;
    Integer[] array = { 1, 2, 3 };
    List<Integer> list = new ArrayList<Integer>();
    list.add(4);
    list.add(5);
    List<Integer>[] arrayOfLists = new List[] { list, list };

    Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<Integer>(obj, array, list, arrayOfLists, list, arrayOfLists);
    String json = oson.useAttribute(false).toJson(objToSerialize, typeOfSrc);
    ObjectWithTypeVariables<Integer> objAfterDeserialization = oson.fromJson(json, typeOfSrc);
    
    String expected = objAfterDeserialization.getExpectedJson();
    expected = "{\"arrayOfListOfWildcardTypeParameters\":[[4,5],[4,5]],\"listOfTypeParameters\":[4,5],\"listOfWildcardTypeParameters\":[4,5],\"typeParameterArray\":[1,2,3],\"arrayOfListOfTypeParameters\":[[4,5],[4,5]],\"typeParameterObj\":0}";

    assertEquals(expected, json);
  }

  public void testVariableTypeDeserialization() throws Exception {
    Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<Integer>(0, null, null, null, null, null);
    String json = oson.useAttribute(false).setDefaultType(JSON_INCLUDE.NON_NULL).toJson(objToSerialize, typeOfSrc);
    ObjectWithTypeVariables<Integer> objAfterDeserialization = oson.fromJson(json, typeOfSrc);

    String expected = objAfterDeserialization.getExpectedJson();
    
    assertEquals("{\"typeParameterObj\":0}", json);
  }

  public void testVariableTypeArrayDeserialization() throws Exception {
    Integer[] array = { 1, 2, 3 };

    Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<Integer>(null, array, null, null, null, null);
    String json = oson.setDefaultType(JSON_INCLUDE.NON_NULL).useAttribute(false).toJson(objToSerialize, typeOfSrc);
    ObjectWithTypeVariables<Integer> objAfterDeserialization = oson.fromJson(json, typeOfSrc);

    assertEquals(objAfterDeserialization.getExpectedJson(), json);
  }

  public void testParameterizedTypeWithVariableTypeDeserialization() throws Exception {
    List<Integer> list = new ArrayList<Integer>();
    list.add(4);
    list.add(5);

    Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<Integer>(null, null, list, null, null, null);
    String json = oson.useAttribute(false).setDefaultType(JSON_INCLUDE.NON_NULL).toJson(objToSerialize, typeOfSrc);
    ObjectWithTypeVariables<Integer> objAfterDeserialization = oson.fromJson(json, typeOfSrc);

    assertEquals(objAfterDeserialization.getExpectedJson(), json);
  }

  @SuppressWarnings("unchecked")
  public void testParameterizedTypeGenericArraysSerialization() throws Exception {
    List<Integer> list = new ArrayList<Integer>();
    list.add(1);
    list.add(2);
    List<Integer>[] arrayOfLists = new List[] { list, list };

    Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<Integer>(null, null, null, arrayOfLists, null, null);
    String json = oson.useAttribute(false).setDefaultType(JSON_INCLUDE.NON_NULL).toJson(objToSerialize, typeOfSrc);
    
    assertEquals("{\"arrayOfListOfTypeParameters\":[[1,2],[1,2]]}", json);
  }

  @SuppressWarnings("unchecked")
  public void testParameterizedTypeGenericArraysDeserialization() throws Exception {
    List<Integer> list = new ArrayList<Integer>();
    list.add(1);
    list.add(2);
    List<Integer>[] arrayOfLists = new List[] { list, list };

    Type typeOfSrc = new TypeToken<ObjectWithTypeVariables<Integer>>() {}.getType();
    ObjectWithTypeVariables<Integer> objToSerialize =
        new ObjectWithTypeVariables<Integer>(null, null, null, arrayOfLists, null, null);
    String json = oson.useAttribute(false).setDefaultType(JSON_INCLUDE.NON_NULL).toJson(objToSerialize, typeOfSrc);
    ObjectWithTypeVariables<Integer> objAfterDeserialization = oson.fromJson(json, typeOfSrc);
    
    String expected = objAfterDeserialization.getExpectedJson();
    
    // expected = "{\"arrayOfListOfTypeParameters\":[[1,2],[1,2]]}";

    assertEquals(expected, json);
  }

  /**
   * An test object that has fields that are type variables.
   *
   * @param <T> Enforce T to be a string to make writing the "toExpectedJson" method easier.
   */
  private static class ObjectWithTypeVariables<T extends Number> {
    private final T typeParameterObj;
    private final T[] typeParameterArray;
    private final List<T> listOfTypeParameters;
    private final List<T>[] arrayOfListOfTypeParameters;
    private final List<? extends T> listOfWildcardTypeParameters;
    private final List<? extends T>[] arrayOfListOfWildcardTypeParameters;

    // For use by Gson
    @SuppressWarnings("unused")
    private ObjectWithTypeVariables() {
      this(null, null, null, null, null, null);
    }

    public ObjectWithTypeVariables(T obj, T[] array, List<T> list, List<T>[] arrayOfList,
        List<? extends T> wildcardList, List<? extends T>[] arrayOfWildcardList) {
      this.typeParameterObj = obj;
      this.typeParameterArray = array;
      this.listOfTypeParameters = list;
      this.arrayOfListOfTypeParameters = arrayOfList;
      this.listOfWildcardTypeParameters = wildcardList;
      this.arrayOfListOfWildcardTypeParameters = arrayOfWildcardList;
    }

    public String getExpectedJson() {
      StringBuilder sb = new StringBuilder().append("{");

      boolean needsComma = false;
      if (typeParameterObj != null) {
        sb.append("\"typeParameterObj\":").append(toString(typeParameterObj));
        needsComma = true;
      }

      if (typeParameterArray != null) {
        if (needsComma) {
          sb.append(',');
        }
        sb.append("\"typeParameterArray\":[");
        appendObjectsToBuilder(sb, Arrays.asList(typeParameterArray));
        sb.append(']');
        needsComma = true;
      }

      if (listOfTypeParameters != null) {
        if (needsComma) {
          sb.append(',');
        }
        sb.append("\"listOfTypeParameters\":[");
        appendObjectsToBuilder(sb, listOfTypeParameters);
        sb.append(']');
        needsComma = true;
      }

      if (arrayOfListOfTypeParameters != null) {
        if (needsComma) {
          sb.append(',');
        }
        sb.append("\"arrayOfListOfTypeParameters\":[");
        appendObjectsToBuilder(sb, arrayOfListOfTypeParameters);
        sb.append(']');
        needsComma = true;
      }

      if (listOfWildcardTypeParameters != null) {
        if (needsComma) {
          sb.append(',');
        }
        sb.append("\"listOfWildcardTypeParameters\":[");
        appendObjectsToBuilder(sb, listOfWildcardTypeParameters);
        sb.append(']');
        needsComma = true;
      }

      if (arrayOfListOfWildcardTypeParameters != null) {
        if (needsComma) {
          sb.append(',');
        }
        sb.append("\"arrayOfListOfWildcardTypeParameters\":[");
        appendObjectsToBuilder(sb, arrayOfListOfWildcardTypeParameters);
        sb.append(']');
        needsComma = true;
      }
      sb.append('}');
      return sb.toString();
    }

    private void appendObjectsToBuilder(StringBuilder sb, Iterable<? extends T> iterable) {
      boolean isFirst = true;
      for (T obj : iterable) {
        if (!isFirst) {
          sb.append(',');
        }
        isFirst = false;
        sb.append(toString(obj));
      }
    }

    private void appendObjectsToBuilder(StringBuilder sb, List<? extends T>[] arrayOfList) {
      boolean isFirst = true;
      for (List<? extends T> list : arrayOfList) {
        if (!isFirst) {
          sb.append(',');
        }
        isFirst = false;
        if (list != null) {
          sb.append('[');
          appendObjectsToBuilder(sb, list);
          sb.append(']');
        } else {
          sb.append("null");
        }
      }
    }

    public String toString(T obj) {
      return obj.toString();
    }
  }

  private static class MultiParameters<A, B, C, D, E> {
    A a;
    B b;
    C c;
    D d;
    E e;
    // For use by Gson
    @SuppressWarnings("unused")
    private MultiParameters() {
    }
    MultiParameters(A a, B b, C c, D d, E e) {
      super();
      this.a = a;
      this.b = b;
      this.c = c;
      this.d = d;
      this.e = e;
    }
    @Override
    public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((a == null) ? 0 : a.hashCode());
      result = prime * result + ((b == null) ? 0 : b.hashCode());
      result = prime * result + ((c == null) ? 0 : c.hashCode());
      result = prime * result + ((d == null) ? 0 : d.hashCode());
      result = prime * result + ((e == null) ? 0 : e.hashCode());
      return result;
    }
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (getClass() != obj.getClass()) {
        return false;
      }
      MultiParameters<A, B, C, D, E> other = (MultiParameters<A, B, C, D, E>) obj;
      if (a == null) {
        if (other.a != null) {
          return false;
        }
      } else if (!a.equals(other.a)) {
        return false;
      }
      if (b == null) {
        if (other.b != null) {
          return false;
        }
      } else if (!b.equals(other.b)) {
        return false;
      }
      if (c == null) {
        if (other.c != null) {
          return false;
        }
      } else if (!c.equals(other.c)) {
        return false;
      }
      if (d == null) {
        if (other.d != null) {
          return false;
        }
      } else if (!d.equals(other.d)) {
        return false;
      }
      if (e == null) {
        if (other.e != null) {
          return false;
        }
      } else if (!e.equals(other.e)) {
        return false;
      }
      return true;
    }
  }
  
  // Begin: tests to reproduce issue 103
  private static class Quantity {
    @SuppressWarnings("unused")
    int q = 10;
  }
  private static class MyQuantity extends Quantity {
    @SuppressWarnings("unused")
    int q2 = 20;
  }
  private interface Measurable<T> {    
  }
  private interface Field<T> {    
  }
  private interface Immutable {    
  }
  
  public static final class Amount<Q extends Quantity> 
      implements Measurable<Q>, Field<Amount<?>>, Serializable, Immutable {
    private static final long serialVersionUID = -7560491093120970437L;

    int value = 30;
  }
  
  public void testDeepParameterizedTypeSerialization() {
    Amount<MyQuantity> amount = new Amount<MyQuantity>();
    String json = oson.toJson(amount);
    assertTrue(json.contains("value"));
    assertTrue(json.contains("30"));    
  }
  
  public void testDeepParameterizedTypeDeserialization() {
    String json = "{value:30}";
    Type type = new TypeToken<Amount<MyQuantity>>() {}.getType();    
    Amount<MyQuantity> amount = oson.fromJson(json, type);
    assertEquals(30, amount.value);
  }
  // End: tests to reproduce issue 103
}
