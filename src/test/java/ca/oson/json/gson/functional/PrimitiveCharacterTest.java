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

import junit.framework.TestCase;
import ca.oson.json.support.TestCaseBase;

import com.google.gson.Gson;

/**
 * Functional tests for Java Character values.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class PrimitiveCharacterTest extends TestCaseBase {
  private Gson gson;

  @Override
  protected void setUp() {
    super.setUp();
    gson = new Gson();
  }

  public void testPrimitiveCharacterAutoboxedSerialization() {
    assertEquals("A", oson.toJson('A'));
    assertEquals("A", oson.toJson('A', char.class));
    assertEquals("A", oson.toJson('A', Character.class));
  }

  public void testPrimitiveCharacterAutoboxedDeserialization() {
    char expected = 'a';
    char actual = oson.fromJson("a", char.class);
    assertEquals(expected, actual);

    actual = oson.fromJson("a", char.class);
    assertEquals(expected, actual);

    actual = oson.fromJson("a", Character.class);
    assertEquals(expected, actual);
  }
}
