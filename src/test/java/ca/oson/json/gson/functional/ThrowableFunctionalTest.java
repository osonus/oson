// Copyright (C) 2014 Trymph Inc.
package ca.oson.json.gson.functional;

import java.io.IOException;

import junit.framework.TestCase;
import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.support.TestCaseBase;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

@SuppressWarnings("serial")
public final class ThrowableFunctionalTest extends TestCaseBase {
  private final Gson gson = new Gson();

  public void testExceptionWithoutCause() {
    RuntimeException e = new RuntimeException("hello");
    String json = oson.toJson(e);
    assertTrue(json.contains("hello"));

    e = oson.fromJson("{'detailMessage':'hello'}", RuntimeException.class);
    assertEquals("hello", e.getMessage());
  }

  public void testExceptionWithCause() {
    Exception e = new Exception("top level", new IOException("io error"));
    String json = oson.useAttribute(false).setDefaultType(JSON_INCLUDE.NON_EMPTY).toJson(e);
    assertTrue(json.contains("{\"detailMessage\":\"top level\",\"cause\":{\"detailMessage\":\"io error\""));

    e = oson.fromJson("{'detailMessage':'top level','cause':{'detailMessage':'io error'}}", Exception.class);
    assertEquals("top level", e.getMessage());
    assertTrue(e.getCause() instanceof Throwable); // cause is not parameterized so type info is lost
    assertEquals("io error", e.getCause().getMessage());
  }

  public void testSerializedNameOnExceptionFields() {
    MyException e = new MyException();
    String json = oson.useAttribute(false).toJson(e);
    assertTrue(json.contains("\"my_custom_name\":\"myCustomMessageValue\""));
  }

  public void testErrorWithoutCause() {
    OutOfMemoryError e = new OutOfMemoryError("hello");
    String json = oson.toJson(e);
    assertTrue(json.contains("hello"));

    e = oson.fromJson("{'detailMessage':'hello'}", OutOfMemoryError.class);
    assertEquals("hello", e.getMessage());
  }

  public void testErrornWithCause() {
    Error e = new Error("top level", new IOException("io error"));
    String json = oson.toJson(e);
    assertTrue(json.contains("top level"));
    assertTrue(json.contains("io error"));

    e = oson.fromJson("{'detailMessage':'top level','cause':{'detailMessage':'io error'}}", Error.class);
    assertEquals("top level", e.getMessage());
    assertTrue(e.getCause() instanceof Throwable); // cause is not parameterized so type info is lost
    assertEquals("io error", e.getCause().getMessage());
  }

  private static final class MyException extends Throwable {
    @SerializedName("my_custom_name") String myCustomMessage = "myCustomMessageValue";
  }
}
