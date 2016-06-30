#Oson User Guide

1. [Overview](#TOC-Overview)
2. [Goals for Oson](#TOC-Goals-for-Oson)
3. [Examples and Tests](#TOC-Oson-Example-and-Test)
4. [General Conversion Rules](#TOC-General-Conversion-Rules)
5. [How to convert Java object to Json document](#TOC-How-To-Convert-Java-Object-To-Json-Document)
  * [Java Configuration](#TOC-Serialize-Java-Configuration)
  * [Annotation](#TOC-Serialize-Annotation)
  * [Lamda Expression](#TOC-Serialize-Lamda-Expression)
6. [How to convert Json document to Java object](#TOC-How-To-Convert-Json-Document-Java-Object)
  * [Java Configuration](#TOC-Deserialize-Java-Configuration)
  * [Annotation](#TOC-Deserialize-Annotation)
  * [Lamda Expression](#TOC-Deserialize-Lamda-Expression)
7. [How to filter out information](#TOC-How-To-Filter-Out-Information)
  * [Java Configuration](#TOC-Filter-Java-Configuration)
  * [Annotation](#TOC-Filter-Annotation)
  * [Lamda Expression](#TOC-Filter-Lamda-Expression)
8. [How to Change Attribute Name](#TOC-How-To-Change-Attribute-Name)
  * [Java Configuration](#TOC-Name-Java-Configuration)
  * [Annotation](#TOC-Name-Annotation)
  * [Lamda Expression](#TOC-Name-Lamda-Expression)
9. [How to Change Attribute Value](#TOC-How-To-Change-Attribute-Value)
  * [Java Configuration](#TOC-Value-Java-Configuration)
  * [Annotation](#TOC-Value-Annotation)
  * [Lamda Expression](#TOC-Value-Lamda-Expression)
10. [How to Format Json Document](#TOC-How-To-Format-Json-Document)
  * [Java Configuration](#TOC-Value-Java-Configuration)


## <a name="TOC-Overview"></a>Overview

There are three aspects of transformation in the conversion between Java objects and JSON documents: attribute name, attribute value, and output formats. How to control these transformation processes are the main focus of this library. In order to provide a fine-tuned way of conversion, it is designed to support 3 level of configuration: global, class, and field levels. There are two strategies to implement these configuration: Java code oriented and annotation oriented one.

Here are four general rules applied during a conversion process:

1. Lower level configurations inherit from higher level ones, if missing
2. Lower level configurations override higher level ones, if exist
3. Java code configuration override annotations
4. Oson annotation override annotations from other sources


## <a name="TOC-Goals-for-Oson"></a>Goals for Oson

  * Convert arbitray Java Object to Json data and back
  * Provide a common interface to Gson and ObjectMapper
  * Support major set of Json annotations: including com.fasterxml.jackson, com.google.gson, org.codehaus.jackson, javax.persistence, javax.validation (JPA), in addition to its own ca.oson.json annotation
  * Allow 3 level control of name and value conversions: global, class-level, and field level
  * Allow these conversions to be either annotation-oriented, or Java oriented, or both
  * Allow well-formatted JSON output: any indentation, any depth, as far as object linkage goes, without redundancy
  * Function of lambda expressions is added to the serialization and deserialization processes, allowing limitless value transformation, with an ease of mind


## <a name="TOC-Oson-Example-and-Test"></a>Oson Examples and Tests

A hello-world example:

```java
package ca.oson.json.userguide;

import ca.oson.json.Oson;

public class HelloWorldTest {
	public static void main(String[] args) {
		Oson oson = new Oson();
		
		int one = 1;
		
		String json = oson.serialize(one);
		
		int result = oson.deserialize(json, int.class);
		
		if (one == result) {
			System.out.println("Hello world, awesome!");
		} else {
			System.err.println("What a day!");
		}
	}
}
```

More than 200 test cases have been created during the one month's off-work hour developing processes, starting June 03, 2016, and much more to come to test various features thouroughly.

These testing cases can be found at [github.com](https://github.com/osonus/oson/tree/master/src/test/java/ca/oson/json), and run by [TestRunner.java](https://github.com/osonus/oson/blob/master/src/test/java/ca/oson/json/TestRunner.java)

Here is another example of 2 test cases: [PrimitivesTest](https://github.com/osonus/oson/blob/master/src/test/java/ca/oson/json/userguide/PrimitivesTest.java)

```java
package ca.oson.json.userguide;

import org.junit.Test;

import ca.oson.json.support.TestCaseBase;

public class PrimitivesTest extends TestCaseBase {

	   @Test
	   public void testSerializationPrimitives() {
		   assertEquals("1", oson.serialize(1));
		   
		   assertEquals("abcd", oson.serialize("abcd"));
		   
		   assertEquals("10", oson.serialize(new Long(10)));

		   assertEquals("10", oson.serialize(new Long(10)));
		   
		   int[] values = { 1 };
		   
		   assertEquals("[1]", oson.serialize(values));
	   }
	
	
	   @Test
	   public void testDeserializationPrimitives() {
		   int one = oson.deserialize("1", int.class);
		   assertEquals(1, one);
		   
		   Integer two = oson.deserialize("2", Integer.class);
		   assertEquals(new Integer(2), two);

		   Long three = oson.deserialize("3", Long.class);
		   assertEquals(new Long(3), three);

		   Boolean four = oson.deserialize("false", Boolean.class);
		   assertFalse(four);

		   String str = oson.deserialize("\"abc\"", String.class);
		   assertEquals("abc", str);

		   String[] anotherStr = oson.deserialize("[\"abc\"]", String[].class);
		   
		   assertEquals("abc", anotherStr[0]);
	   }
	   
}
```


## <a name="TOC-General-Conversion-Rules"></a>General Conversion Rules

The first two general rules specify how to apply the 3 level configurations in Oson: global, class-level, and field or attribute level.
  * Lower level configurations inherit from higher level ones, if missing
  * Lower level configurations override higher level ones, if exist

This means that a setting in the global level will propagate into class-level, and the class-level settings go in turn to the field level.
It also means that a class-level setting will override the global settings, and a local one will replace the class level.

The second two general rules specify how to put these previous rules into practice by using either a Java code based configuration, or annotation based configuration, or both, at global, class and field levels.

In order to achieve these features, two Java classes and two Annotation classes are used, with similar names and patterns:

2 Java classes:
  * ca.oson.json.Oson.ClassMapper
  * ca.oson.json.Oson.FieldMapper
2 Annotation classes:
  * ca.oson.json.ClassMapper
  * ca.oson.json.FieldMapper

The Java classes have slightly more features than their corresponding annotation classes, owing to the fact that annotation can only support primitive types and Enum, not even null value. These classes contain more features than all configuration abilities from external sources, including com.fasterxml.jackson, com.google.gson, org.codehaus.jackson, javax.persistence, and javax.validation. To simulate the null default concept in annotation, NONE enum entry is introduced to various enums, including the BOOOLEAN enum, which has 3 values: BOOOLEAN.TRUE, BOOOLEAN.FALSE, BOOOLEAN.NONE, corresponding to true, false, and null in Boolean Java type. This way, the value false can be used to override previous true value. For example, if a field in ignored in external Java classes, and we cannot change its source code, yet we can easily set ignore to be false using FieldMapper class.

The detail overriding rules are:

1. Global Java configuration at the top
2. Apply annotations from other sources at the second level
3. Override these previous settings with annotation from Oson, which is ca.oson.json.ClassMapper
4. Override last 3 settings using Java code configuration. This configuration class is ca.oson.json.Oson.ClassMapper. At this step, we have class level configuration for the current Java class. The following steps of each field in this class will use this setting as the basis for each of its own configuration
5. Using the ClassMapper created at step 4, a new FieldMapper object is created
6. Apply configuration information from other sources to this FieldMapper
7. Apply configuration information from Oson field annotations class to this FieldMapper. Oson has a single Field annotation class, which is ca.oson.json.FieldMapper
8. Apply configuration information using Java code, with the help of Oson Java configuration class: ca.oson.json.Oson.FieldMapper
9. Make use of this final configuration data to configure how a field in a Java class is mapped, for both of its name and value

Once you understand these overriding rules, you will be able to configure any Java class at ease.

For example, Oson has a precision or/and scale setting. A precision is the number of front-end non-zero digits in a numeric value, while numeric scale refers to the maximum number of decimal places. This means that precision can apply to all numeric types, with 16 of them in Java: Integer, int, Long, long, Byte, byte, Double, double, Short, short, Float, float, BigDecimal, BigInteger, AtomicInteger, AtomicLong. There are less decimal types: Double, double, Float, float, BigDecimal. Now use decimal types as an example, we see how we can set it up at the global level:

```java
	@Test
	public void testSerializationPrecisionAndScaleFloatDoubleDecimalGlobal() {
		float value = 12.34567890f;
		Double valueDouble = 12.34567890d;
		BigDecimal valueBigDecimal = new BigDecimal(12.34567890d);
		
		oson.setPrecision(1).setScale(null);
		String expected = "10";
		String json = oson.serialize(value);
		assertEquals(expected, json);
		json = oson.serialize(valueDouble);
		assertEquals(expected, json);
		json = oson.serialize(valueBigDecimal);
		assertEquals(expected, json);

		oson.setPrecision(5).setScale(1);
		json = oson.serialize(value);
		expected = "12.3";
		assertEquals(expected, json);
		json = oson.serialize(valueDouble);
		assertEquals(expected, json);
		json = oson.serialize(valueBigDecimal);
		assertEquals(expected, json);

		oson.setPrecision(5).setScale(5);
		json = oson.serialize(value);
		expected = "12.34600";
		assertEquals(expected, json);
		json = oson.serialize(valueDouble);
		assertEquals(expected, json);
		json = oson.serialize(valueBigDecimal);
		assertEquals(expected, json);
	}
```

This global level configuration forms a basis for further action. Two test classes are created to demonstrate how each configuration steps can be applied and each of them is used to modify the behavior of Oson processor. Please check out them for detailed usage:

[ScaleTest](https://github.com/osonus/oson/blob/master/src/test/java/ca/oson/json/numeric/ScaleTest.java)
[PrecisionScaleTest](https://github.com/osonus/oson/blob/master/src/test/java/ca/oson/json/numeric/PrecisionScaleTest.java)

Json becomes popular, for 3 main reasons:

1. Javascript is the Web page language, and all its objects are in Json format: name-value pairs;
2. Easy to use, at least easier than XML;
3. Some major NoSQL databases use Json as their document types.

We will focus on how to do 4 things: filter out information, change name, change value, and change Json Document formatting, in a top-down approach.


## <a name="TOC-How-To-Convert-Java-Object-To-Json-Document"></a>How to convert Java object to Json document

Following the current convention, simply use one of the following interface methods to convert a Java object to a Json document:

```java
		Oson oson = new Oson();
		Car car = new Car("Chevron", 6);
		String json = oson.serialize(car);
		System.out.println(json);
```

This code can be put into 1 line:
System.out.println(new Oson().serialize(new Car("Chevron", 6)));
which prints out as
{"doors":6,"date":null,"brand":"Chevron"}

Further details can be found at [SerializeCarTest](https://github.com/osonus/oson/blob/master/src/test/java/ca/oson/json/userguide/SerializeCarTest.java)

A few notes:

1. Can use these methods for the same purpose: serialize, toJson, writeValueAsString
2. oson or oson.asOson() is the default behavior, but it is easy to use Gson and Jackson's version: oson.asGson(), oson.asJackson(), will automatically connection with these two popular Json-Java processors
3. Jackson's mapper does not take attributes as its default inputs, you need either to provide a get method, or use annotation to that purpose: useField to make use of field value, in case there are no get accessor


### <a name="TOC-Serialize-Java-Configuration"></a>Java Configuration

There are lots of way you can use to change the behavior of Oson Tool. At the center point, there is an option class that is used to all purposes.

Here are the list of atributes you can set in order to make it the way you want it to act:

```java
		// global level configurations
		
		/*
		 * a flag to determine if a date should be converted to a long number, or a formatted text
		 */
		private Boolean date2Long = null;
		/*
		 * Date formatter for all Date and its sub classes
		 */
		private String simpleDateFormat = DefaultValue.simpleDateFormat;
		private DateFormat dateFormat = new SimpleDateFormat(simpleDateFormat);
		
		/*
		 * a flag to indicate if the Oson tool should act as an interface to Gson, Jackson, or simply itself
		 */
		private JSON_PROCESSOR jsonProcessor = JSON_PROCESSOR.OSON;
		
		/*
		 * If either Gson or Jackson fails and throws exception
		 * turn this flag on to print out stack trace message
		 * and continue to use Oson's own implementation
		 */
		private Boolean printErrorUseOsonInFailure = false;
		
		/*
		 * Field naming convention, to make a field name: lowercase, uppercase, camelcase, space-delimited,
		 * dash-delimited, and more
		 */
		private FIELD_NAMING fieldNaming = FIELD_NAMING.FIELD;
		
		/*
		 * Attibute value convention, to allow NULL, empty, default values or not
		 */
		private JSON_INCLUDE defaultType = JSON_INCLUDE.NONE;
		
		/*
		 * Space indent the Json output, and with newline for each item, or not
		 */
		private Boolean prettyPrinting = false;
		
		/*
		 * How many spaces you want for each level of indentation, can be as high as 100 spaces apart for each level,
		 * or just use the default: 2 spaces. It it is set to 0, then no indentation is used, 
		 * the same effect like setting prettyPrinting to false
		 */
		private int indentation = 2;
		
		/*
		 * Turn annotation support on or off, normally should be on
		 */
		private boolean annotationSupport = true;
		
		/*
		 * sort the presentation of attributes in a Map or class by nature order or not
		 */
		private Boolean orderByKeyAndProperties = false;
		
		/*
		 * Include class name as metadata in the Json output or not
		 */
		private Boolean includeClassTypeInJson = false;
		
		/*
		 * Use this value to act as the attribute for class name metadata, or change to anything you want to use
		 * simply specify it during deserialization. If not specified, will try to interpret the Json document
		 * using this default value: @class
		 */
		private String jsonClassType = "@class";
		
		/*
		 * As the name goes, ignore any versioned attributes bigger than this double value
		 */
		private Double ignoreVersionsAfter = 10000d; // max allowed
		
		/*
		 * Ignore any fields with any of the annotations set here
		 */
		private Set<Class> ignoreFieldsWithAnnotations = null;
		
		/*
		 * Ignore any classes with any of the annotations set here
		 */
		private Set<Class> ignoreClassWithAnnotations = null;
		
		/*
		 * Include all the fields or attributes with these modifiers
		 * if not specified, then will use all of them, except:
		 * Transient and Volatile, Synthetic class is also ignored automatically
		 * unless specified otherwise
		 */
		private Set<MODIFIER> includeFieldsWithModifiers = null;
		
		/*
		 * Determine to use field to retrieve or set value, or not
		 */
		private Boolean useField = null;
		
		/*
		 * Determine to use attribute (get or set methods) to retrieve or set value, or not
		 */
		private Boolean useAttribute = null;
		
		/*
		 * Maximum level of depth the processing should go, can be up to 100 levels.
		 * A level means go from a class to its field or atributes. 
		 * There are cases a Java objects can contain other objects, and keeps on going.
		 * Endless loop is carefully prevented, using hashcode, but need more testing to prove.
		 * This setting can also be used for this purpose.
		 */
		private int level = MAX_LEVEL;
		
		/*
		 * Combined with precision and scale, to format the output of decimal values.
		 * Used for float, double, and BigDecimal data types. In the deserialzation process,
		 * only used for BigDecimal
		 */
		private RoundingMode roundingMode = RoundingMode.HALF_UP;
		
		/*
		 * Default enum output type, either int as ordinal, and string as enum name
		 */
		private EnumType enumType = null;
		
		/*
		 * Only used for String data type. It specifies the maximun length a string can hold
		 * or to output. Certain databases requires length limit. Might also used for
		 * testing purpose
		 */
		private Integer length = null;
		
		/*
		 * front-end number of digits in a numeric value
		 */
		private Integer precision = null;
		
		/*
		 * digits after decimal point in a numeric value.
		 * Mostly used for output
		 */
		private Integer scale = null;
		
		/*
		 * Minimum value a number can be, if required, or use default setting
		 */
		private Long min = null;
		
		/*
		 * Maximum value a number can be, if required, or use default setting
		 */
		private Long max = null;

		/*
		 * class level configurations
		 */
		private Map<Class, ClassMapper> classMappers = null;

		/*
		 * field level configurations
		 */
		private Set<FieldMapper> fieldMappers = null;
```

The Option object is set in Oson in a special way, using the convertion from Javascript:
either in the constructor of Oson class, or as parameter to the configure builder method.

It can be in JSONObject json, Object[] array, Map<String, Object> map, Options options, or simply a Json string, which will be deserialized by Oson itself, to retrieve all configuration information.

You can specify only the ones you want to, and use the rest of default values. And you can specify any time, either before or during the serializing or deserializing process. You can use any of the Builder method to set the configuration value you desire, in a train, and later ones will overwrite the earlier settings, or combined with you, depending on the circonstances. For most of the collection (Set) attributes, it behavior like add, unless you use a null value to set, which act like reset or clear, all previous values of this particular attribut is gone.

Some examples:
  * oson.pretty(): indentation is requested for output, the same as oson.pretty(true), or prettyPrinting(true)
  * oson.setLevel(5): maximum 5 levels of output and processing
  * oson.includeClassTypeInJson(true): include class name in Json output
  * oson.sort() === oson.sort(true) === oson.orderByKeyAndProperties(true): order output by key of a map or properties of a Java object
  * oson.setDefaultType(JSON_INCLUDE.NON_NULL): do not output null, use any of these values: ALWAYS, NON_NULL, NON_EMPTY, NON_DEFAULT, DEFAULT, NONE, to manage null or empty or default values
  * oson.setClassMappers(...): parameter can be one or multiple ClassMapper objects, useful to set class-level behavior
  * oson.setFieldMappers(...): parameter can be one or multiple FieldMapper objects, useful to set field-level behavior

Or you can train them all up, like this:
String json = oson.pretty().setLevel(5).includeClassTypeInJson(true).sort().setDefaultType(JSON_INCLUDE.NON_NULL)...serialize(myObject);

More details on each of these settings.


### <a name="TOC-Serialize-Annotation"></a>Annotation

Annotations can be used to set how to name an attribute, change a value, etc. And you can have lots of options to do the same thing. All up to personal flavor. In most cases, you might choose to annotate your own classes, and to configure classes from external sources using Java configurations.

When I faced with so many annotations, from different sources, and one processor only chooses to use its own set of annotations, I choose to implement a different Json-Java processor, which will support them all, and also provide its own set of annotations: only two of them: one is class level, and anothe one is field level. Both of these annotations try to deliver the same amount of information as its counterpart class, with the same name, just slightly different class path.


### <a name="TOC-Serialize-Lamda-Expression"></a>Lamda Expression

Lambda expression is one of the most powerful featuers in Java programming language. Or Java tends to behavior like a functional language, apart from the pure object-oriented language idealism.

Lambda expression as a single functional interface is perfect to act as a serializer and deserializer. It gives the true powerful of transformation into Oson processor. Basically, it allows you to do everything, or almost anything you want, to have full access to contextual data, to return types of Java data you want. This only feature makes Oson as the one you like to use, as a Json-Java processor.
