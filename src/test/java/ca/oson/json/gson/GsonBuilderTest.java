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

import java.lang.reflect.Modifier;
import ca.oson.json.support.TestCaseBase;

/**
 * Unit tests for {@link GsonBuilder}.
 *
 * @author Inderjeet Singh
 */
public class GsonBuilderTest extends TestCaseBase {

  public void testExcludeFieldsWithModifiers() {
    oson.clear().excludeFieldsWithModifiers(Modifier.VOLATILE, Modifier.PRIVATE);
    assertEquals("{\"d\":\"d\"}", oson.toJson(new HasModifiers()));
  }


  @SuppressWarnings("unused")
  static class HasModifiers {
    private String a = "a";
    volatile String b = "b";
    private volatile String c = "c";
    String d = "d";
  }

  public void testTransientFieldExclusion() {
    assertEquals("{}", oson.clear().toJson(new HasTransients()));
  }

  static class HasTransients {
    transient String a = "a";
  }
}
