/*
 * Copyright (C) 2011 Google Inc.
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

import java.net.InetAddress;
import java.net.URL;

import ca.oson.json.FieldMapper;
import ca.oson.json.support.TestCaseBase;

/**
 * Unit tests for the default serializer/deserializer for the {@code InetAddress} type.
 * 
 * @author Joel Leitch
 */
public class DefaultInetAddressTypeAdapterTest extends TestCaseBase {

  public void testInetAddressSerializationAndDeserialization() throws Exception {
	oson.clearAll().setFieldMappers(new FieldMapper("hostAddress", java.net.Inet4Address.class).setJsonValue(true));
	  
    InetAddress address = InetAddress.getByName("8.8.8.8");
    String jsonAddress = oson.setLevel(1).toJson(address);
    assertEquals("\"8.8.8.8\"", jsonAddress);

    InetAddress value = oson.fromJson(jsonAddress, InetAddress.class);
    // assertTrue(null == value);
  }
}
