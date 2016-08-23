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
import ca.oson.json.Oson;
import ca.oson.json.support.TestCaseBase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.LongSerializationPolicy;
import com.google.gson.reflect.TypeToken;

import java.io.Serializable;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

/**
 * Functional tests for Json primitive values: integers, and floating point numbers.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class PrimitiveTest extends TestCaseBase {
  private Gson gson;

  @Override
  protected void setUp() {
    super.setUp();
    gson = new Gson();
  }

  public void testPrimitiveIntegerAutoboxedSerialization() {
    assertEquals("1", oson.toJson(1));
  }

  public void testPrimitiveIntegerAutoboxedDeserialization() {
    int expected = 1;
    int actual = oson.fromJson("1", int.class);
    assertEquals(expected, actual);

    actual = oson.fromJson("1", Integer.class);
    assertEquals(expected, actual);
  }

  public void testByteSerialization() {
    assertEquals("1", oson.toJson(1, byte.class));
    assertEquals("1", oson.toJson(1, Byte.class));
  }

  public void testShortSerialization() {
    assertEquals("1", oson.toJson(1, short.class));
    assertEquals("1", oson.toJson(1, Short.class));
  }

  public void testByteDeserialization() {
    Byte target = oson.fromJson("1", Byte.class);
    assertEquals(1, (byte)target);
    byte primitive = oson.fromJson("1", byte.class);
    assertEquals(1, primitive);
  }

  public void testPrimitiveIntegerAutoboxedInASingleElementArraySerialization() {
    int target[] = {-9332};
    assertEquals("[-9332]", oson.toJson(target));
    assertEquals("[-9332]", oson.toJson(target, int[].class));
    assertEquals("[-9332]", oson.toJson(target, Integer[].class));
  }

  public void testReallyLongValuesSerialization() {
    long value = 333961828784581L;
    assertEquals("333961828784581", oson.toJson(value));
  }

  public void testReallyLongValuesDeserialization() {
    String json = "333961828784581";
    long value = oson.fromJson(json, Long.class);
    assertEquals(333961828784581L, value);
  }

  public void testPrimitiveLongAutoboxedSerialization() {
    assertEquals("1", oson.toJson(1L, long.class));
    assertEquals("1", oson.toJson(1L, Long.class));
  }

  public void testPrimitiveLongAutoboxedDeserialization() {
    long expected = 1L;
    long actual = oson.fromJson("1", long.class);
    assertEquals(expected, actual);

    actual = oson.fromJson("1", Long.class);
    assertEquals(expected, actual);
  }

  public void testPrimitiveLongAutoboxedInASingleElementArraySerialization() {
    long[] target = {-23L};
    assertEquals("[-23]", oson.toJson(target));
    assertEquals("[-23]", oson.toJson(target, long[].class));
    assertEquals("[-23]", oson.toJson(target, Long[].class));
  }

  public void testPrimitiveBooleanAutoboxedSerialization() {
    assertEquals("true", oson.toJson(true));
    assertEquals("false", oson.toJson(false));
  }

  public void testBooleanDeserialization() {
    boolean value = oson.fromJson("false", boolean.class);
    assertEquals(false, value);
    value = oson.fromJson("true", boolean.class);
    assertEquals(true, value);
  }

  public void testPrimitiveBooleanAutoboxedInASingleElementArraySerialization() {
    boolean target[] = {false};
    assertEquals("[false]", oson.toJson(target));
    assertEquals("[false]", oson.toJson(target, boolean[].class));
    assertEquals("[false]", oson.toJson(target, Boolean[].class));
  }

  public void testNumberSerialization() {
    Number expected = 1L;
    String json = oson.toJson(expected);
    assertEquals(expected.toString(), json);

    json = oson.toJson(expected, Number.class);
    assertEquals(expected.toString(), json);
  }

  public void testNumberDeserialization() {
    String json = "1";
    Number expected = new Integer(json);
    Number actual = oson.fromJson(json, Number.class);
    assertEquals(expected.intValue(), actual.intValue());

    json = String.valueOf(Long.MAX_VALUE);
    expected = new Long(json);
    actual = oson.fromJson(json, Number.class);
    assertEquals(expected.longValue(), actual.longValue());

    json = "1.0";
    actual = oson.fromJson(json, Number.class);
    assertEquals(1L, actual.longValue());
  }


  public void testPrimitiveDoubleAutoboxedDeserialization() {
    double actual = oson.fromJson("-122.08858585", double.class);
    assertEquals(-122.08858585, actual);

    actual = oson.fromJson("122.023900008000", Double.class);
    assertEquals(122.023900008, actual);
  }

  public void testPrimitiveDoubleAutoboxedInASingleElementArraySerialization() {
    double[] target = {-122.08D};
    assertEquals("[-122.08]", oson.toJson(target));
    assertEquals("[-122.08]", oson.toJson(target, double[].class));
    assertEquals("[-122.08]", oson.toJson(target, Double[].class));
  }

  public void testDoubleAsStringRepresentationDeserialization() {
    String doubleValue = "1.0043E+5";
    Double expected = Double.valueOf(doubleValue);
    Double actual = oson.fromJson(doubleValue, Double.class);
    assertEquals(expected, actual);

    double actual1 = oson.fromJson(doubleValue, double.class);
    assertEquals(expected.doubleValue(), actual1);
  }

  public void testDoubleNoFractAsStringRepresentationDeserialization() {
    String doubleValue = "1E+5";
    Double expected = Double.valueOf(doubleValue);
    Double actual = oson.fromJson(doubleValue, Double.class);
    assertEquals(expected, actual);

    double actual1 = oson.fromJson(doubleValue, double.class);
    assertEquals(expected.doubleValue(), actual1);
  }

  public void testDoubleArrayDeserialization() {
      String json = "[0.0, 0.004761904761904762, 3.4013606962703525E-4, 7.936508173034305E-4,"
              + "0.0011904761904761906, 0.0]";
      double[] values = oson.fromJson(json, double[].class);
      assertEquals(6, values.length);
      assertEquals(0.0, values[0]);
      assertEquals(0.004761904761904762, values[1]);
      assertEquals(3.4013606962703525E-4, values[2]);
      assertEquals(7.936508173034305E-4, values[3]);
      assertEquals(0.0011904761904761906, values[4]);
      assertEquals(0.0, values[5]);
  }

  public void testLargeDoubleDeserialization() {
    String doubleValue = "1.234567899E8";
    Double expected = Double.valueOf(doubleValue);
    Double actual = oson.fromJson(doubleValue, Double.class);
    assertEquals(expected, actual);

    double actual1 = oson.fromJson(doubleValue, double.class);
    assertEquals(expected.doubleValue(), actual1);
  }

  public void testBigDecimalSerialization() {
    BigDecimal target = new BigDecimal("-122.0e-21");
    String json = oson.toJson(target);
    assertEquals(target, new BigDecimal(json));
  }

  public void testBigDecimalDeserialization() {
    BigDecimal target = new BigDecimal("-122.0e-21");
    String json = "-122.0e-21";
    assertEquals(target, oson.fromJson(json, BigDecimal.class));
  }

  public void testBigDecimalInASingleElementArraySerialization() {
    BigDecimal[] target = {new BigDecimal("-122.08e-21")};
    String json = oson.toJson(target);
    String actual = extractElementFromArray(json);
    assertEquals(target[0], new BigDecimal(actual));

    json = oson.toJson(target, BigDecimal[].class);
    actual = extractElementFromArray(json);
    assertEquals(target[0], new BigDecimal(actual));
  }

  public void testSmallValueForBigDecimalSerialization() {
    BigDecimal target = new BigDecimal("1.55");
    String actual = oson.toJson(target);
    assertEquals(target.toString(), actual);
  }

  public void testSmallValueForBigDecimalDeserialization() {
    BigDecimal expected = new BigDecimal("1.55");
    BigDecimal actual = oson.fromJson("1.55", BigDecimal.class);
    assertEquals(expected, actual);
  }

  public void testBigDecimalPreservePrecisionSerialization() {
    String expectedValue = "1.000";
    BigDecimal obj = new BigDecimal(expectedValue);
    String actualValue = oson.toJson(obj);

    assertEquals(expectedValue, actualValue);
  }

  public void testBigDecimalPreservePrecisionDeserialization() {
    String json = "1.000";
    BigDecimal expected = new BigDecimal(json);
    BigDecimal actual = oson.fromJson(json, BigDecimal.class);

    assertEquals(expected, actual);
  }

  public void testBigDecimalAsStringRepresentationDeserialization() {
    String doubleValue = "0.05E+5";
    BigDecimal expected = new BigDecimal(doubleValue);
    BigDecimal actual = oson.fromJson(doubleValue, BigDecimal.class);
    assertEquals(expected, actual);
  }

  public void testBigDecimalNoFractAsStringRepresentationDeserialization() {
    String doubleValue = "5E+5";
    BigDecimal expected = new BigDecimal(doubleValue);
    BigDecimal actual = oson.fromJson(doubleValue, BigDecimal.class);
    assertEquals(expected, actual);
  }

  public void testBigIntegerSerialization() {
    BigInteger target = new BigInteger("12121211243123245845384534687435634558945453489543985435");
    assertEquals(target.toString(), oson.toJson(target));
  }

  public void testBigIntegerDeserialization() {
    String json = "12121211243123245845384534687435634558945453489543985435";
    BigInteger target = new BigInteger(json);
    assertEquals(target, oson.fromJson(json, BigInteger.class));
  }

  public void testBigIntegerInASingleElementArraySerialization() {
    BigInteger[] target = {new BigInteger("1212121243434324323254365345367456456456465464564564")};
    String json = oson.toJson(target);
    String actual = extractElementFromArray(json);
    assertEquals(target[0], new BigInteger(actual));

    json = oson.toJson(target, BigInteger[].class);
    actual = extractElementFromArray(json);
    assertEquals(target[0], new BigInteger(actual));
  }

  public void testSmallValueForBigIntegerSerialization() {
    BigInteger target = new BigInteger("15");
    String actual = oson.toJson(target);
    assertEquals(target.toString(), actual);
  }

  public void testSmallValueForBigIntegerDeserialization() {
    BigInteger expected = new BigInteger("15");
    BigInteger actual = oson.fromJson("15", BigInteger.class);
    assertEquals(expected, actual);
  }

  public void testBadValueForBigIntegerDeserialization() {
	  assertEquals(null, oson.fromJson("15.099", BigInteger.class));
  }

  public void testMoreSpecificSerialization() {
    Gson gson = new Gson();
    String expected = "This is a string";
    String expectedJson = oson.toJson(expected);

    Serializable serializableString = expected;
    String actualJson = oson.toJson(serializableString, Serializable.class);
    assertFalse(expectedJson.equals(actualJson));
  }

  private String extractElementFromArray(String json) {
    return json.substring(json.indexOf('[') + 1, json.indexOf(']'));
  }

  public void testDoubleNaNSerializationNotSupportedByDefault() {

      double nan = Double.NaN;
      assertEquals("null", oson.toJson(nan));

      assertEquals("null", oson.toJson(Double.NaN));
  }

  public void testDoubleNaNSerialization() {
    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    double nan = Double.NaN;
    assertEquals("NaN", oson.toJson(nan));
    assertEquals("NaN", oson.toJson(Double.NaN));
  }

  public void testDoubleNaNDeserialization() {
    assertTrue(Double.isNaN(gson.fromJson("NaN", Double.class)));
    assertTrue(Double.isNaN(gson.fromJson("NaN", double.class)));
  }

  public void testFloatNaNSerializationNotSupportedByDefault() {
//    try {
      float nan = Float.NaN;
      assertEquals("null", oson.toJson(nan));

//      fail("Gson should not accept NaN for serialization");
//    } catch (IllegalArgumentException expected) {
//    }
      assertEquals("null", oson.toJson(Float.NaN));
//      fail("Gson should not accept NaN for serialization");
//    } catch (IllegalArgumentException expected) {
//    }
  }

  public void testFloatNaNSerialization() {
    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    float nan = Float.NaN;
    assertEquals("null", oson.toJson(nan));
    assertEquals("null", oson.toJson(Float.NaN));
  }

  public void testFloatNaNDeserialization() {
    assertTrue(Float.isNaN(gson.fromJson("NaN", Float.class)));
    assertTrue(Float.isNaN(gson.fromJson("NaN", float.class)));
  }

  public void testBigDecimalNaNDeserializationNotSupported() {
	  assertNull(oson.fromJson("NaN", BigDecimal.class));
  }

  public void testDoubleInfinitySerializationNotSupportedByDefault() {
      double infinity = Double.POSITIVE_INFINITY;
      assertEquals("null", oson.toJson(infinity));

      assertEquals("null", oson.toJson(Double.POSITIVE_INFINITY));
  }

  public void testDoubleInfinitySerialization() {
    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    double infinity = Double.POSITIVE_INFINITY;
    assertEquals("Infinity", oson.toJson(infinity));
    assertEquals("Infinity", oson.toJson(Double.POSITIVE_INFINITY));
  }

  public void testDoubleInfinityDeserialization() {
    assertTrue(Double.isInfinite(gson.fromJson("Infinity", Double.class)));
    assertTrue(Double.isInfinite(gson.fromJson("Infinity", double.class)));
  }

  public void testFloatInfinitySerializationNotSupportedByDefault() {
    try {
      float infinity = Float.POSITIVE_INFINITY;
      oson.toJson(infinity);
      fail("Gson should not accept positive infinity for serialization by default");
    } catch (IllegalArgumentException expected) {
    }
    try {
      oson.toJson(Float.POSITIVE_INFINITY);
      fail("Gson should not accept positive infinity for serialization by default");
    } catch (IllegalArgumentException expected) {
    }
  }

  public void testFloatInfinityDeserialization() {
    assertTrue(Float.isInfinite(gson.fromJson("Infinity", Float.class)));
    assertTrue(Float.isInfinite(gson.fromJson("Infinity", float.class)));
  }

  public void testBigDecimalInfinityDeserializationNotSupported() {
	  assertNull(oson.fromJson("Infinity", BigDecimal.class));
  }

  public void testNegativeInfinitySerializationNotSupportedByDefault() {

      double negativeInfinity = Double.NEGATIVE_INFINITY;
      assertEquals("null", oson.toJson(negativeInfinity));

      assertEquals("null", oson.toJson(Double.NEGATIVE_INFINITY));
  }

  public void testNegativeInfinityDeserialization() {
    assertTrue(Double.isInfinite(gson.fromJson("-Infinity", double.class)));
    assertTrue(Double.isInfinite(gson.fromJson("-Infinity", Double.class)));
  }

  public void testNegativeInfinityFloatSerializationNotSupportedByDefault() {

      float negativeInfinity = Float.NEGATIVE_INFINITY;
      assertEquals("null", oson.toJson(negativeInfinity));
      assertEquals("null",oson.toJson(Float.NEGATIVE_INFINITY));
  }

  public void testNegativeInfinityFloatSerialization() {
    Gson gson = new GsonBuilder().serializeSpecialFloatingPointValues().create();
    float negativeInfinity = Float.NEGATIVE_INFINITY;
    assertEquals("-Infinity", oson.toJson(negativeInfinity));
    assertEquals("-Infinity", oson.toJson(Float.NEGATIVE_INFINITY));
  }

  public void testNegativeInfinityFloatDeserialization() {
    assertTrue(Float.isInfinite(gson.fromJson("-Infinity", float.class)));
    assertTrue(Float.isInfinite(gson.fromJson("-Infinity", Float.class)));
  }

  public void testBigDecimalNegativeInfinityDeserializationNotSupported() {
	  assertEquals(null, oson.fromJson("-Infinity", BigDecimal.class));
  }



  public void testLongAsStringDeserialization() throws Exception {
    long value = oson.fromJson("\"15\"", long.class);
    assertEquals(15, value);

    gson = new GsonBuilder().setLongSerializationPolicy(LongSerializationPolicy.STRING).create();
    value = oson.fromJson("\"25\"", long.class);
    assertEquals(25, value);
  }

  public void testQuotedStringSerializationAndDeserialization() throws Exception {
    String value = "String Blah Blah Blah...1, 2, 3";
    String serializedForm = oson.toJson(value);
    assertEquals(value, serializedForm);

    String actual = oson.fromJson(serializedForm, String.class);
    assertEquals(value, actual);
  }

  public void testUnquotedStringDeserializationFails() throws Exception {
    assertEquals("UnquotedSingleWord", oson.fromJson("UnquotedSingleWord", String.class));

    String value = "String Blah Blah Blah...1, 2, 3";

    assertEquals(value, oson.fromJson(value, String.class));

  }

  public void testHtmlCharacterSerialization() throws Exception {
    String target = "<script>var a = 12;</script>";
    String result = oson.toJson(target);
    assertFalse(result.equals('"' + target + '"'));

    gson = new GsonBuilder().disableHtmlEscaping().create();
    result = oson.toJson(target);
    assertTrue(result.equals(target));
  }

  public void testDeserializePrimitiveWrapperAsObjectField() {
    String json = "{i:10}";
    ClassWithIntegerField target = oson.fromJson(json, ClassWithIntegerField.class);
    assertEquals(10, target.i.intValue());
  }

  private static class ClassWithIntegerField {
    Integer i;
  }

  public void testPrimitiveClassLiteral() {
    assertEquals(1, oson.fromJson("1", int.class).intValue());
//    assertEquals(1, oson.fromJson(new StringReader("1"), int.class).intValue());
//    assertEquals(1, oson.fromJson(new JsonPrimitive(1), int.class).intValue());
  }

  public void testDeserializeJsonObjectAsLongPrimitive() {
    try {
      oson.fromJson("{'abc':1}", long.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

  public void testDeserializeJsonArrayAsLongWrapper() {
	  assertEquals(null, oson.fromJson("[1,2,3]", Long.class));
  }

  public void testDeserializeJsonArrayAsInt() {
	  int i = oson.fromJson("[1, 2, 3, 4]", int.class);
	  assertEquals(0,i);
  }

  public void testDeserializeJsonObjectAsInteger() {
	  assertEquals(null, oson.fromJson("{}", Integer.class));
  }

  public void testDeserializeJsonObjectAsShortPrimitive() {
	  assertEquals(null, oson.fromJson("{'abc':1}", short.class));
  }

  public void testDeserializeJsonArrayAsShortWrapper() {
    assertEquals(null, oson.fromJson("['a','b']", Short.class));
  }

  public void testDeserializeJsonArrayAsDoublePrimitive() {
	  assertEquals(0.0d, oson.fromJson("[1,2]", double.class));
  }

  public void testDeserializeJsonObjectAsDoubleWrapper() {
	  assertEquals(null, oson.fromJson("{'abc':1}", Double.class));
  }

  public void testDeserializeJsonObjectAsFloatPrimitive() {
	  assertEquals(0.0f, oson.fromJson("{'abc':1}", float.class));
  }

  public void testDeserializeJsonArrayAsFloatWrapper() {
	  assertNull(oson.fromJson("[1,2,3]", Float.class));
  }

  public void testDeserializeJsonObjectAsBytePrimitive() {
	  byte b = oson.fromJson("{'abc':1}", byte.class);
	  assertEquals(0, b);
  }

  public void testDeserializeJsonArrayAsByteWrapper() {
	  assertNull(oson.fromJson("[1,2,3,4]", Byte.class));
  }

  public void testDeserializeJsonObjectAsBooleanPrimitive() {
	  boolean b = oson.fromJson("{'abc':1}", boolean.class);
	  assertEquals(false, b);
  }

  public void testDeserializeJsonArrayAsBooleanWrapper() {
	  assertNull(oson.clear().fromJson("[1,2,3,4]", Boolean.class));
  }

  public void testDeserializeJsonArrayAsBigDecimal() {
    //try {
    	BigDecimal d = oson.fromJson("[1,2,3,4]", BigDecimal.class);

    	assertNull(d);
    	//      fail();
//    } catch (JsonSyntaxException expected) {}
  }



  public void testDeserializeJsonArrayAsBigInteger() {
	  assertNull(oson.fromJson("[1,2,3,4]", BigInteger.class));
  }

  public void testDeserializeJsonObjectAsBigInteger() {
    try {
      oson.fromJson("{'c':2}", BigInteger.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

  public void testDeserializeJsonArrayAsNumber() {
    try {
      oson.fromJson("[1,2,3,4]", Number.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }

  public void testDeserializeJsonObjectAsNumber() {
    try {
      oson.fromJson("{'c':2}", Number.class);
      fail();
    } catch (JsonSyntaxException expected) {}
  }


  public void testDeserializingNonZeroDecimalPointValuesAsIntegerFails() {
    try {
      oson.fromJson("1.02", Byte.class);
      fail();
    } catch (JsonSyntaxException expected) {
    }
    try {
      oson.fromJson("1.02", Short.class);
      fail();
    } catch (JsonSyntaxException expected) {
    }
    try {
      oson.fromJson("1.02", Integer.class);
      fail();
    } catch (JsonSyntaxException expected) {
    }
    try {
      oson.fromJson("1.02", Long.class);
      fail();
    } catch (JsonSyntaxException expected) {
    }
  }

  public void testDeserializingBigDecimalAsIntegerFails() {
	  assertNull(oson.fromJson("-122.08e-213", Integer.class));
  }

  public void testDeserializingBigIntegerAsInteger() {
	  assertEquals(null, oson.fromJson("12121211243123245845384534687435634558945453489543985435", Integer.class));
  }

  public void testDeserializingBigIntegerAsLong() {
//    try {
    	assertNull(oson.fromJson("12121211243123245845384534687435634558945453489543985435", Long.class));
//      fail();
//    } catch (JsonSyntaxException expected) {
//    }
  }

  public void testValueVeryCloseToZeroIsZero() {
    assertEquals(0, (byte) oson.fromJson("-122.08e-2132", byte.class));
    assertEquals(0, (short) oson.fromJson("-122.08e-2132", short.class));
    assertEquals(0, (int) oson.fromJson("-122.08e-2132", int.class));
    assertEquals(0, (long) oson.fromJson("-122.08e-2132", long.class));
    assertEquals(-0.0f, oson.fromJson("-122.08e-2132", float.class));
    assertEquals(-0.0, oson.fromJson("-122.08e-2132", double.class));
    assertEquals(0.0f, oson.fromJson("122.08e-2132", float.class));
    assertEquals(0.0, oson.fromJson("122.08e-2132", double.class));
  }

  public void testDeserializingBigDecimalAsFloat() {
    String json = "-122.08e-2132332";
    float actual = oson.fromJson(json, float.class);
    assertEquals(-0.0f, actual);
  }

  public void testDeserializingBigDecimalAsDouble() {
    String json = "-122.08e-2132332";
    double actual = oson.fromJson(json, double.class);
    assertEquals(-0.0d, actual);
  }

  public void testDeserializingBigDecimalAsBigIntegerFails() {
	  assertNull(oson.fromJson("-122.08e-213", BigInteger.class));
  }

  public void testDeserializingBigIntegerAsBigDecimal() {
    BigDecimal actual =
      oson.fromJson("12121211243123245845384534687435634558945453489543985435", BigDecimal.class);
    assertEquals("12121211243123245845384534687435634558945453489543985435", actual.toPlainString());
  }


}
