/*
 * Copyright (C) 2012 Google Inc.
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

import ca.oson.json.support.TestCaseBase;
import ca.oson.json.util.StringUtil;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Locale;

import junit.framework.TestCase;

/**
 * @author Jesse Wilson
 */
public class OverrideCoreTypeAdaptersTest extends TestCaseBase {

  public void testOverrideWrapperBooleanAdapter() {
    assertEquals("true", oson.toJson(true, boolean.class));
    assertEquals("true", oson.toJson(true, Boolean.class));
    assertEquals(Boolean.TRUE, oson.fromJson("true", boolean.class));
    assertEquals(Boolean.TRUE, oson.fromJson("1", Boolean.class));
    assertEquals(Boolean.FALSE, oson.fromJson("0", Boolean.class));
  }

  public void testOverridePrimitiveBooleanAdapter() {
    assertEquals("true", oson.toJson(true, boolean.class));
    assertEquals("true", oson.toJson(true, Boolean.class));
    assertEquals(Boolean.TRUE, oson.fromJson("1", boolean.class));
    assertEquals(Boolean.TRUE, oson.fromJson("true", Boolean.class));
    assertEquals("false", oson.toJson(false, boolean.class));
  }

  public void testOverrideStringAdapter() {
	  oson.setSerializer(String.class, (String p) -> StringUtil.doublequote(p.toUpperCase()))
	  		.setDeserializer(String.class, (String p) -> StringUtil.unquote(p.toLowerCase()));
    assertEquals("\"HELLO\"", oson.toJson("Hello", String.class));
    assertEquals("hello", oson.fromJson("\"Hello\"", String.class));
  }
}
