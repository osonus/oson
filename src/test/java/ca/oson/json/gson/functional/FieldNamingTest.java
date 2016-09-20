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

package ca.oson.json.gson.functional;

import static com.google.gson.FieldNamingPolicy.IDENTITY;
import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_DASHES;
import static com.google.gson.FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES;
import static com.google.gson.FieldNamingPolicy.UPPER_CAMEL_CASE;
import static com.google.gson.FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES;
import ca.oson.json.Oson.FIELD_NAMING;
import ca.oson.json.support.TestCaseBase;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

import junit.framework.TestCase;

public final class FieldNamingTest extends TestCaseBase {
  public void testIdentity() {
    Gson gson = getGsonWithNamingPolicy(IDENTITY);
    
    String json = oson.setFieldNaming(FIELD_NAMING.FIELD).toJson(new TestNames()).replace('\"', '\'');

    assertEquals("{'lowerCamel':1,'UpperCamel':2,'_lowerCamelLeadingUnderscore':3,'_UpperCamelLeadingUnderscore':4,'lower_words':5,'UPPER_WORDS':6,'annotatedName':7,'lowerId':8}",
        json);
  }

  public void testUpperCamelCase() {
    Gson gson = getGsonWithNamingPolicy(UPPER_CAMEL_CASE);
    
    String json = oson.setFieldNaming(FIELD_NAMING.UPPER_CAMELCASE).toJson(new TestNames()).replace('\"', '\'');

    assertEquals("{'LowerCamel':1,'UpperCamel':2,'LowerCamelLeadingUnderscore':3,'UpperCamelLeadingUnderscore':4,'LowerWords':5,'UpperWords':6,'annotatedName':7,'LowerId':8}",
    		json);
  }

  public void testUpperCamelCaseWithSpaces() {
    Gson gson = getGsonWithNamingPolicy(UPPER_CAMEL_CASE_WITH_SPACES);
    
    String json = oson.setFieldNaming(FIELD_NAMING.SPACE_UPPER_CAMELCASE).toJson(new TestNames()).replace('\"', '\'');

    assertEquals("{'Lower Camel':1,'Upper Camel':2,'Lower Camel Leading Underscore':3,'Upper Camel Leading Underscore':4,'Lower Words':5,'Upper Words':6,'annotatedName':7,'Lower Id':8}",json);
  }

  public void testLowerCaseWithUnderscores() {
    Gson gson = getGsonWithNamingPolicy(LOWER_CASE_WITH_UNDERSCORES);
    
    String json = oson.setFieldNaming(FIELD_NAMING.UNDERSCORE_LOWER).toJson(new TestNames()).replace('\"', '\'');

    assertEquals("{'lower_camel':1,'upper_camel':2,'lower_camel_leading_underscore':3,'upper_camel_leading_underscore':4,'lower_words':5,'upper_words':6,'annotatedName':7,'lower_id':8}", 
    		json);
  }

  public void testLowerCaseWithDashes() {
    Gson gson = getGsonWithNamingPolicy(LOWER_CASE_WITH_DASHES);
    
    String json = oson.setFieldNaming(FIELD_NAMING.DASH_LOWER).toJson(new TestNames()).replace('\"', '\'');

    assertEquals("{'lower-camel':1,'upper-camel':2,'lower-camel-leading-underscore':3,'upper-camel-leading-underscore':4,'lower-words':5,'upper-words':6,'annotatedName':7,'lower-id':8}",
        json);
  }

  private Gson getGsonWithNamingPolicy(FieldNamingPolicy fieldNamingPolicy){
    return new GsonBuilder()
      .setFieldNamingPolicy(fieldNamingPolicy)
        .create();
  }

  @SuppressWarnings("unused") // fields are used reflectively
  private static class TestNames {
    int lowerCamel = 1;
    int UpperCamel = 2;
    int _lowerCamelLeadingUnderscore = 3;
    int _UpperCamelLeadingUnderscore = 4;
    int lower_words = 5;
    int UPPER_WORDS = 6;
    @SerializedName("annotatedName") int annotated = 7;
    int lowerId = 8;
  }
}
