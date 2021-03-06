/*
 * Copyright (C) 2009 Google Inc.
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
import junit.framework.TestCase;

/**
 * Unit test for the {@link LongSerializationPolicy} class.
 *
 * @author Inderjeet Singh
 * @author Joel Leitch
 */
public class LongSerializationPolicyTest extends TestCaseBase {

  public void testDefaultLongSerializationIntegration() {
    assertEquals("[1]", oson.toJson(new long[] { 1L }, long[].class));
    assertEquals("[1]", oson.toJson(new Long[] { 1L }, Long[].class));
  }

  public void testStringLongSerializationIntegration() {
    oson.ser(Long.class, (Long p) -> StringUtil.doublequote(p, oson.isEscapeHtml()));
    
    String result = oson.toJson(new Long[] { 1L }, Long[].class);

    assertEquals("[\"1\"]", result);
    
    assertEquals("[\"1\"]", oson.toJson(new long[] { 1L }, long[].class));
  }
}
