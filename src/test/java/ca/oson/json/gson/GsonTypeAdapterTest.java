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

package ca.oson.json.gson;

import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import ca.oson.json.support.TestCaseBase;
import junit.framework.TestCase;

/**
 * Contains numerous tests involving registered type converters with a Gson instance.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class GsonTypeAdapterTest extends TestCaseBase {

  public void testDefaultTypeAdapterThrowsParseException() throws Exception {
    try {
    	BigInteger b = oson.fromJson("{\"abc\":123}", BigInteger.class);
    	assertEquals(null, b);
    } catch (Exception expected) { }
  }

  public void testTypeAdapterThrowsException() throws Exception {

    	String json = oson.toJson(new AtomicLong(0));
    	assertEquals("0", json);

    	AtomicLong bl = oson.fromJson("123", AtomicLong.class);
    	assertEquals(123, bl.intValue());
  }

  public void testTypeAdapterProperlyConvertsTypes() throws Exception {
    int intialValue = 1;
    AtomicInteger atomicInt = new AtomicInteger(intialValue);
    String json = oson.toJson(atomicInt);
    assertEquals(intialValue, Integer.parseInt(json));

    atomicInt = oson.fromJson(json, AtomicInteger.class);
    assertEquals(intialValue, atomicInt.get());
  }

  public void testTypeAdapterDoesNotAffectNonAdaptedTypes() throws Exception {
    String expected = "blah";
    String actual = oson.toJson(expected);
    // first level, do not encode by "
    assertEquals(expected, actual);

    actual = oson.fromJson(actual, String.class);
    assertEquals(expected, actual);
  }


  static abstract class Abstract {
    String a;
  }

  static class Concrete extends Abstract {
    String b;
  }

  // https://groups.google.com/d/topic/google-gson/EBmOCa8kJPE/discussion
  public void testDeserializerForAbstractClass() {
	  oson.clear();
    Concrete instance = new Concrete();
    instance.a = "android";
    instance.b = "beep";
    assertSerialized("{\"a\":\"android\"}", Abstract.class, instance);
    assertSerialized("{\"a\":\"android\",\"b\":\"beep\"}", Concrete.class, instance);
  }

  private void assertSerialized(String expected, Class<?> instanceType, Object instance) {
    assertEquals(expected, oson.toJson(instance, instanceType));
  }
}
