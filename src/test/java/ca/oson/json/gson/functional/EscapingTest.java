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

import ca.oson.json.OsonAssert;
import ca.oson.json.OsonAssert.MODE;
import ca.oson.json.support.TestCaseBase;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import ca.oson.json.domain.TestTypes.BagOfPrimitives;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

/**
 * Performs some functional test involving JSON output escaping.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class EscapingTest extends TestCaseBase {
//  private Gson gson;
//
//  @Override
//  protected void setUp() throws Exception {
//    super.setUp();
//    gson = new Gson();
//  }

  public void testEscapingQuotesInStringArray() throws Exception {
    String[] valueWithQuotes = { "beforeQuote\"afterQuote" };
    String jsonRepresentation = oson.toJson(valueWithQuotes);
    String[] target = oson.fromJson(jsonRepresentation, String[].class);
    assertEquals(1, target.length);
    assertEquals(valueWithQuotes[0], target[0]);
  }

  public void testEscapeAllHtmlCharacters() {
    List<String> strings = new ArrayList<String>();
    strings.add("<");
    strings.add(">");
    strings.add("=");
    strings.add("&");
    strings.add("'");
    strings.add("\"");
    assertEquals("[\"\\u003c\",\"\\u003e\",\"\\u003d\",\"\\u0026\",\"\\u0027\",\"\\\"\"]",
        oson.toJson(strings)); // .setEscapeHtml(true)
  }

//  public void testEscapingObjectFields() throws Exception {
//    BagOfPrimitives objWithPrimitives = new BagOfPrimitives(1L, 1, true, "test with\" <script>");
//    String jsonRepresentation = oson.setEscapeHtml(true).toJson(objWithPrimitives);
//    //assertFalse(jsonRepresentation.contains("<"));
//    //assertFalse(jsonRepresentation.contains(">"));
//    assertTrue(jsonRepresentation.contains("\\\""));
//
//    BagOfPrimitives expectedObject = oson.clearAll().fromJson(jsonRepresentation, BagOfPrimitives.class);
//    OsonAssert.assertEquals(objWithPrimitives.getExpectedJson(), expectedObject.getExpectedJson(), MODE.SORTED);
//  }
  
  public void testGsonAcceptsEscapedAndNonEscapedJsonDeserialization() throws Exception {
    Gson escapeHtmlGson = new GsonBuilder().create();
    Gson noEscapeHtmlGson = new GsonBuilder().disableHtmlEscaping().create();
    
    BagOfPrimitives target = new BagOfPrimitives(1L, 1, true, "test' / w'ith\" / \\ <script>");
    String escapedJsonForm = escapeHtmlGson.toJson(target);
    String nonEscapedJsonForm = noEscapeHtmlGson.toJson(target);
    assertFalse(escapedJsonForm.equals(nonEscapedJsonForm));
    
    assertEquals(target, noEscapeHtmlGson.fromJson(escapedJsonForm, BagOfPrimitives.class));
    assertEquals(target, escapeHtmlGson.fromJson(nonEscapedJsonForm, BagOfPrimitives.class));
  }

  public void testGsonDoubleDeserialization() {
    BagOfPrimitives expected = new BagOfPrimitives(3L, 4, true, "value1");
    String json = oson.toJson(oson.toJson(expected));
    String value = oson.fromJson(json, String.class);
    BagOfPrimitives actual = oson.fromJson(value, BagOfPrimitives.class);
    assertEquals(expected, actual);
  }
}
