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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import ca.oson.json.support.TestCaseBase;

/**
 * A simple unit test for the {@link DefaultDateTypeAdapter} class.
 *
 * @author Joel Leitch
 */
public class DefaultDateTypeAdapterTest extends TestCaseBase {
	
	public static final Date startDate = new Date(0);

  public void testFormattingInEnUs() {
    assertFormattingAlwaysEmitsUsLocale(Locale.US);
  }

  private void assertFormattingAlwaysEmitsUsLocale(Locale locale) {
    TimeZone defaultTimeZone = TimeZone.getDefault();
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    Locale defaultLocale = Locale.getDefault();
    Locale.setDefault(locale);
    try {
    	DateFormat format = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());

    	assertEquals("Jan 1, 1970 12:00:00 AM", oson.setDateFormat(format).serialize(startDate));
    	assertEquals("1/1/70", oson.setDateFormat(DateFormat.SHORT).serialize(startDate));
    	assertEquals("Jan 1, 1970", oson.setDateFormat(DateFormat.MEDIUM).serialize(startDate));
    	assertEquals("January 1, 1970", oson.setDateFormat(DateFormat.LONG).serialize(startDate));
    	assertEquals("1/1/70 12:00 AM", oson.setDateFormat(DateFormat.SHORT, DateFormat.SHORT).serialize(startDate));
    	assertEquals("Jan 1, 1970 12:00:00 AM", oson.setDateFormat(DateFormat.MEDIUM, DateFormat.MEDIUM).serialize(startDate));
    	assertEquals("January 1, 1970 12:00:00 AM UTC", oson.setDateFormat(DateFormat.LONG, DateFormat.LONG).serialize(startDate));
    	assertEquals("Thursday, January 1, 1970 12:00:00 AM UTC", oson.setDateFormat(DateFormat.FULL, DateFormat.FULL).serialize(startDate));
    } finally {
      TimeZone.setDefault(defaultTimeZone);
      Locale.setDefault(defaultLocale);
    }
  }


	public void testParsingDatesFormattedWithSystemLocale() {
		TimeZone defaultTimeZone = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		Locale defaultLocale = Locale.getDefault();
		Locale.setDefault(Locale.FRANCE);
		try {
			DateFormat format = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.getDefault());
	    	
	      assertEquals("1 janv. 1970 00:00:00", oson.setDateFormat(format).serialize(startDate));
	      assertEquals("01/01/70", oson.setDateFormat(DateFormat.SHORT).serialize(startDate));
	      assertEquals("1 janv. 1970", oson.setDateFormat(DateFormat.MEDIUM).serialize(startDate));
	      assertEquals("1 janvier 1970", oson.setDateFormat(DateFormat.LONG).serialize(startDate));
	      assertEquals("01/01/70 00:00",
	    		  oson.setDateFormat(DateFormat.SHORT, DateFormat.SHORT).serialize(startDate));
	      assertEquals("1 janv. 1970 00:00:00",
	    		  oson.setDateFormat(DateFormat.MEDIUM, DateFormat.MEDIUM).serialize(startDate));
	      assertEquals("1 janvier 1970 00:00:00 UTC",
	    		  oson.setDateFormat(DateFormat.LONG, DateFormat.LONG).serialize(startDate));
	      assertEquals("jeudi 1 janvier 1970 00 h 00 UTC",
	    		  oson.setDateFormat(DateFormat.FULL, DateFormat.FULL).serialize(startDate));
		} finally {
			TimeZone.setDefault(defaultTimeZone);
			Locale.setDefault(defaultLocale);
		}
	}
  
	public void testFormatUsesDefaultTimezone() {
		TimeZone defaultTimeZone = TimeZone.getDefault();
		TimeZone.setDefault(TimeZone.getTimeZone("America/Los_Angeles"));
		Locale defaultLocale = Locale.getDefault();
		Locale.setDefault(Locale.US);
		DateFormat format = DateFormat.getDateTimeInstance(DateFormat.DEFAULT,
				DateFormat.DEFAULT, Locale.getDefault());
		try {
			assertEquals("Dec 31, 1969 4:00:00 PM", oson.setDateFormat(format)
					.serialize(startDate));
		} finally {
			TimeZone.setDefault(defaultTimeZone);
			Locale.setDefault(defaultLocale);
		}
	}

	public void testDateSerialization() throws Exception {
		int dateStyle = DateFormat.LONG;

		DateFormat formatter = DateFormat.getDateInstance(dateStyle, Locale.US);
		Date currentDate = new Date();

		String dateString = oson.setDateFormat(dateStyle, Locale.US).serialize(
				currentDate);
		assertEquals(formatter.format(currentDate), dateString);
	}

	public void testDatePattern() throws Exception {
		String pattern = "yyyy-MM-dd";
		DateFormat formatter = new SimpleDateFormat(pattern);
		Date currentDate = new Date();

		String dateString = oson.setDateFormat(pattern).serialize(currentDate);
		assertEquals(formatter.format(currentDate), dateString);
	}
	
	public void testInvalidDatePattern() throws Exception {
		try {
			oson.setDateFormat("I am a bad Date pattern....");
			fail("Invalid date pattern should fail.");
		} catch (IllegalArgumentException expected) {
		}
	}
	
}
