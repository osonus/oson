/*
 * Copyright (C) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ca.oson.json.gson;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import ca.oson.json.support.TestCaseBase;
import junit.framework.TestCase;

public final class ObjectTypeAdapterTest extends TestCaseBase {
  public void testDeserialize() throws Exception {
    Map<?, ?> map = (Map<?, ?>) oson.fromJson("{\"a\":5,\"b\":[1,2,null],\"c\":{\"x\":\"y\"}}");
    assertEquals(5, map.get("a"));
    assertEquals(Arrays.asList(1, 2, null).toString(), map.get("b").toString());
    assertEquals(Collections.singletonMap("x", "y"), map.get("c"));
    assertEquals(3, map.size());
  }

  public void testSerialize() throws Exception {
    Object object = new RuntimeType();
    String result = oson.toJson(object).replace("\"", "'");
    assertEquals("{'a':5,'b':[1,2,null]}", result);
  }
  
  public void testSerializeNullValue() throws Exception {
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("a", null);
    String result = oson.toJson(map).replace('"', '\'');
    assertEquals("{'a':null}", result);
  }

  public void testDeserializeNullValue() throws Exception {
    Map<String, Object> map = new LinkedHashMap<String, Object>();
    map.put("a", null);
    Map<String, Object> result = oson.fromJson("{\"a\":null}");
    assertEquals(map, result);
  }

  public void testSerializeObject() throws Exception {
	String result = oson.setGetOnly().toJson(new Object());
    assertEquals("{}", result);
  }

  @SuppressWarnings("unused")
  private class RuntimeType {
    Object a = 5;
    Object b = Arrays.asList(1, 2, null);
  }
}
