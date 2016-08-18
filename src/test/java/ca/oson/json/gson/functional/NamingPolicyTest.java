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

import ca.oson.json.Oson.FIELD_NAMING;
import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.support.TestCaseBase;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import ca.oson.json.domain.TestTypes.ClassWithSerializedNameFields;
import ca.oson.json.domain.TestTypes.StringWrapper;
import junit.framework.TestCase;

import java.lang.reflect.Field;

/**
 * Functional tests for naming policies.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class NamingPolicyTest extends TestCaseBase {
  private GsonBuilder builder;

  @Override
  protected void setUp() {
    super.setUp();
    builder = new GsonBuilder();
  }

  public void testGsonWithNonDefaultFieldNamingPolicySerialization() {
    Gson gson = builder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
    StringWrapper target = new StringWrapper("blah");
    assertEquals("{\"SomeConstantStringInstanceField\":\""
        + target.someConstantStringInstanceField + "\"}", oson.setFieldNaming(FIELD_NAMING.UPPER_CAMELCASE).toJson(target));
  }

  public void testGsonWithNonDefaultFieldNamingPolicyDeserialiation() {
    Gson gson = builder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();
    String target = "{\"SomeConstantStringInstanceField\":\"someValue\"}";
    StringWrapper deserializedObject = oson.fromJson(target, StringWrapper.class);
    assertEquals("someValue", deserializedObject.someConstantStringInstanceField);
  }

  public void testGsonWithLowerCaseDashPolicySerialization() {
    Gson gson = builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).create();
    StringWrapper target = new StringWrapper("blah");
    assertEquals("{\"some-constant-string-instance-field\":\""
        + target.someConstantStringInstanceField + "\"}", oson.setFieldNaming(FIELD_NAMING.DASH_LOWER).toJson(target));
  }

  public void testGsonWithLowerCaseDashPolicyDeserialiation() {
    Gson gson = builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES).create();
    String target = "{\"some-constant-string-instance-field\":\"someValue\"}";
    StringWrapper deserializedObject = oson.fromJson(target, StringWrapper.class);
    assertEquals("someValue", deserializedObject.someConstantStringInstanceField);
  }

  public void testGsonWithLowerCaseUnderscorePolicySerialization() {
    Gson gson = builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create();
    StringWrapper target = new StringWrapper("blah");
    assertEquals("{\"some_constant_string_instance_field\":\""
        + target.someConstantStringInstanceField + "\"}", oson.setFieldNaming(FIELD_NAMING.UNDERSCORE_LOWER).toJson(target));
  }

  public void testGsonWithLowerCaseUnderscorePolicyDeserialiation() {
    Gson gson = builder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .create();
    String target = "{\"some_constant_string_instance_field\":\"someValue\"}";
    StringWrapper deserializedObject = oson.fromJson(target, StringWrapper.class);
    assertEquals("someValue", deserializedObject.someConstantStringInstanceField);
  }

  public void testGsonWithSerializedNameFieldNamingPolicySerialization() {
    Gson gson = builder.create();
    ClassWithSerializedNameFields expected = new ClassWithSerializedNameFields(5, 6);
    String actual = oson.useAttribute(false).toJson(expected);
    String expecteds = expected.getExpectedJson();
    assertEquals(expecteds, actual);
  }

  public void testGsonWithSerializedNameFieldNamingPolicyDeserialization() {
    Gson gson = builder.create();
    ClassWithSerializedNameFields expected = new ClassWithSerializedNameFields(5, 7);
    ClassWithSerializedNameFields actual =
        oson.fromJson(expected.getExpectedJson(), ClassWithSerializedNameFields.class);
    assertEquals(expected.f, actual.f);
  }

  public void testGsonDuplicateNameUsingSerializedNameFieldNamingPolicySerialization() {
    Gson gson = builder.create();
//    try {
      ClassWithDuplicateFields target = new ClassWithDuplicateFields(10);
      String json = oson.toJson(target);
      assertEquals("{\"a\":10,\"a\":null}", json);
//      fail();
//    } catch (IllegalArgumentException expected) {
//    }
  }

  public void testGsonWithUpperCamelCaseSpacesPolicySerialiation() {
    Gson gson = builder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES)
        .create();
    StringWrapper target = new StringWrapper("blah");
    assertEquals("{\"Some Constant String Instance Field\":\""
        + target.someConstantStringInstanceField + "\"}", oson.setFieldNaming(FIELD_NAMING.SPACE_UPPER_CAMELCASE).toJson(target));
  }

  public void testGsonWithUpperCamelCaseSpacesPolicyDeserialiation() {
    Gson gson = builder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES)
        .create();
    String target = "{\"Some Constant String Instance Field\":\"someValue\"}";
    StringWrapper deserializedObject = oson.fromJson(target, StringWrapper.class);
    assertEquals("someValue", deserializedObject.someConstantStringInstanceField);
  }

  public void testDeprecatedNamingStrategy() throws Exception {
    Gson gson = builder.setFieldNamingStrategy(new UpperCaseNamingStrategy()).create();
    ClassWithDuplicateFields target = new ClassWithDuplicateFields(10);
    String actual = oson.setFieldNaming(FIELD_NAMING.UNDERSCORE_UPPER).setDefaultType(JSON_INCLUDE.NON_NULL).toJson(target);
    assertEquals("{\"A\":10}", actual);
  }

  public void testComplexFieldNameStrategy() throws Exception {
    Gson gson = new Gson();
    String json = oson.toJson(new ClassWithComplexFieldName(10));
    String escapedFieldName = "@value\"_s$\\";
    assertEquals("{\"" + escapedFieldName + "\":10}", json);

//    json = "{\"@value\\\"_s$\\\\\":10}";
//    ClassWithComplexFieldName obj = oson.clear().fromJson(json, ClassWithComplexFieldName.class);
//    assertEquals(10, obj.value);
  }

  /** http://code.google.com/p/google-gson/issues/detail?id=349 */
  public void testAtSignInSerializedName() {
    assertEquals("{\"@foo\":\"bar\"}", new Gson().toJson(new AtName()));
  }

  static final class AtName {
    @SerializedName("@foo") String f = "bar";
  }

  private static final class UpperCaseNamingStrategy implements FieldNamingStrategy {
    @Override
    public String translateName(Field f) {
      return f.getName().toUpperCase();
    }
  }

  @SuppressWarnings("unused")
  private static class ClassWithDuplicateFields {
    public Integer a;
    @SerializedName("a") public Double b;

    public ClassWithDuplicateFields(Integer a) {
      this(a, null);
    }

    public ClassWithDuplicateFields(Double b) {
      this(null, b);
    }

    public ClassWithDuplicateFields(Integer a, Double b) {
      this.a = a;
      this.b = b;
    }
  }

  private static class ClassWithComplexFieldName {
    @SerializedName("@value\"_s$\\") public final long value;

    ClassWithComplexFieldName(long value) {
      this.value = value;
    }
  }
}
