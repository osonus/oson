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

import ca.oson.json.DataMapper;
import ca.oson.json.DefaultValue;
import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.support.TestCaseBase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import ca.oson.json.domain.TestTypes.BagOfPrimitives;
import ca.oson.json.domain.TestTypes.ClassOverridingEquals;

import com.google.gson.reflect.TypeToken;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import java.lang.reflect.Type;

/**
 * Functional tests that do not fall neatly into any of the existing classification.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class UncategorizedTest extends TestCaseBase {

  private Gson gson = null;

  @Override
  protected void setUp() {
    super.setUp();
    gson = new Gson();
  }

  public void testInvalidJsonDeserializationFails() throws Exception {
    BagOfPrimitives v = oson.clearAll().fromJson("adfasdf1112,,,\":", BagOfPrimitives.class);
    assertNull(v);

      v = oson.fromJson("{adfasdf1112,,,\":}", BagOfPrimitives.class);
      String json = oson.setDefaultType(JSON_INCLUDE.NON_DEFAULT).useAttribute(false).serialize(v);
      assertEquals("{}", json);
  }

  public void testObjectEqualButNotSameSerialization() throws Exception {
    ClassOverridingEquals objA = new ClassOverridingEquals();
    ClassOverridingEquals objB = new ClassOverridingEquals();
    objB.ref = objA;
    //
    String json = oson.useAttribute(false).setDefaultType(JSON_INCLUDE.ALWAYS).toJson(objB);
    assertEquals(objB.getExpectedJson(), json);
  }

  public void testStaticFieldsAreNotSerialized() {
    BagOfPrimitives target = new BagOfPrimitives();
    assertFalse(gson.toJson(target).contains("DEFAULT_VALUE"));
  }

  public void testGsonInstanceReusableForSerializationAndDeserialization() {
    BagOfPrimitives bag = new BagOfPrimitives();
    String json = oson.toJson(bag);
    BagOfPrimitives deserialized = oson.fromJson(json, BagOfPrimitives.class);
    assertEquals(bag, deserialized);
  }

  /**
   * This test ensures that a custom deserializer is able to return a derived class instance for a
   * base class object. For a motivation for this test, see Issue 37 and
   * http://groups.google.com/group/google-gson/browse_thread/thread/677d56e9976d7761
   */
  public void testReturningDerivedClassesDuringDeserialization() {
    Gson gson = new GsonBuilder().registerTypeAdapter(Base.class, new BaseTypeAdapter()).create();
    String json = "{\"opType\":\"OP1\"}";
    Base base = oson.des(Base.class, (DataMapper p) -> {
    	Base b = (Base) p.getObj();
    	Map map = p.getMap();
    	if (map.get("opType").equals("OP1")) {
    		return new Derived1();
    	}
    	return new Derived2();
    }).fromJson(json, Base.class);
    assertTrue(base instanceof Derived1);
    assertEquals(OperationType.OP1, base.opType);

    json = "{\"opType\":\"OP2\"}";
    base = oson.fromJson(json, Base.class);
    assertTrue(base instanceof Derived2);
    assertEquals(OperationType.OP2, base.opType);
  }

  /**
   * Test that trailing whitespace is ignored.
   * http://code.google.com/p/google-gson/issues/detail?id=302
   */
  public void testTrailingWhitespace() throws Exception {
    List<Integer> integers = oson.fromJson("[1,2,3]  \n\n  ",
        new TypeToken<List<Integer>>() {}.getType());
    assertEquals(Arrays.asList(1, 2, 3), integers);
  }

  private enum OperationType { OP1, OP2 }
  private static class Base {
    OperationType opType;
  }
  private static class Derived1 extends Base {
    Derived1() { opType = OperationType.OP1; }
  }
  private static class Derived2 extends Base {
    Derived2() { opType = OperationType.OP2; }
  }
  private static class BaseTypeAdapter implements JsonDeserializer<Base> {
    @Override public Base deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      String opTypeStr = json.getAsJsonObject().get("opType").getAsString();
      OperationType opType = OperationType.valueOf(opTypeStr);
      switch (opType) {
      case OP1:
        return new Derived1();
      case OP2:
        return new Derived2();
      }
      throw new JsonParseException("unknown type: " + json);
    }
  }
}
