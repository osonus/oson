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
import ca.oson.json.support.TestCaseBase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import ca.oson.json.domain.TestTypes.BagOfPrimitives;
import ca.oson.json.domain.TestTypes.ClassWithTransientFields;
import ca.oson.json.domain.TestTypes.Nested;
import ca.oson.json.domain.TestTypes.PrimitiveArray;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

/**
 * Functional tests for print formatting.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class PrintFormattingTest extends TestCaseBase {

  private Gson gson;

  @Override
  protected void setUp() {
    super.setUp();
    gson = new Gson();
  }

  @SuppressWarnings({"unchecked", "rawtypes"})
  public void testCompactFormattingLeavesNoWhiteSpace() {
    List list = new ArrayList();
    list.add(new BagOfPrimitives());
    list.add(new Nested());
    list.add(new PrimitiveArray());
    list.add(new ClassWithTransientFields());

    String json = oson.toJson(list);
    assertContainsNoWhiteSpace(json);
  }

  public void testJsonObjectWithNullValues() {
    JsonObject obj = new JsonObject();
    obj.addProperty("field1", "value1");
    obj.addProperty("field2", (String) null);
    String json = oson.useAttribute(false).setDefaultType(JSON_INCLUDE.NON_NULL).toJson(obj);
    assertTrue(json.contains("field1"));
    assertFalse(json.contains("field2"));
  }

  public void testJsonObjectWithNullValuesSerialized() {
    gson = new GsonBuilder().serializeNulls().create();
    JsonObject obj = new JsonObject();
    obj.addProperty("field1", "value1");
    obj.addProperty("field2", (String) null);
    String json = oson.toJson(obj);
    assertTrue(json.contains("field1"));
    assertTrue(json.contains("field2"));
  }

  private static void assertContainsNoWhiteSpace(String str) {
    for (char c : str.toCharArray()) {
      assertFalse(Character.isWhitespace(c));
    }
  }
}
