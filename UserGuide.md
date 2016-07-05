#Oson User Guide

1. [Overview](#TOC-Overview)
2. [Goals for Oson](#TOC-Goals-for-Oson)
3. [Examples and Tests](#TOC-Oson-Example-and-Test)
4. [General Conversion Rules](#TOC-General-Conversion-Rules)
5. [How to convert Java object to Json document](#TOC-How-To-Convert-Java-Object-To-Json-Document)
  * [Java Configuration](#TOC-Serialize-Java-Configuration)
    * [Global Options](#TOC-Global-Options)
    * [Class Mappers](#TOC-Class-Mappers)
    * [Field Mappers](#TOC-Field-Mappers)
  * [Annotation](#TOC-Serialize-Annotation)
  * [Lambda Expression](#TOC-Serialize-Lambda-Expression)
6. [How to convert Json document to Java object](#TOC-How-To-Convert-Json-Document-Java-Object)
  * [How to Create Initial Java Object](#TOC-Deserialize-How-To-Create-Initial-Java-Object)
    * [Implement InstanceCreator](#TOC-Implement-InstanceCreator)
    * [Use Default Object](#TOC-Use-Default-Object)
    * [Use Constructor Annotation](#TOC-Use-Constructor-Annotation)
  * [Lambda Expression](#TOC-Deserialize-Lambda-Expression)
7. [How to filter out information](#TOC-How-To-Filter-Out-Information)
  * [Java Configuration](#TOC-Filter-Java-Configuration)
  * [Annotation](#TOC-Filter-Annotation)
  * [Lambda Expression](#TOC-Filter-Lambda-Expression)
8. [How to Change Attribute Name](#TOC-How-To-Change-Attribute-Name)
  * [Java Configuration](#TOC-Name-Java-Configuration)
  * [Annotation](#TOC-Name-Annotation)
  * [Lambda Expression](#TOC-Name-Lambda-Expression)
9. [How to Change Attribute Value](#TOC-How-To-Change-Attribute-Value)
  * [Java Configuration](#TOC-Value-Java-Configuration)
  * [Annotation](#TOC-Value-Annotation)
  * [Lambda Expression](#TOC-Value-Lambda-Expression)
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

The class and property level overriding rules are:

1. Global Java configuration at the top
2. Inherit configuration from a higher level class if the current object serves as a field in the higher level class, unless configured not to do so
3. Apply annotations from other sources at the second level
4. Override these previous settings with annotation from Oson, which is ca.oson.json.ClassMapper
5. Override last 3 settings using Java code configuration. This configuration class is ca.oson.json.Oson.ClassMapper. At this step, we have class level configuration for the current Java class. The following steps of each field in this class will use this setting as the basis for each of its own configuration
6. Create a blank field mapper instance for certain property with a returnType
7. Get the class mapper of the returnType
8. Classify this field mapper with the class mapper of the return type
9. Classify this field mapper with the class mapper created at step 5
10. Apply configuration information from other sources to this FieldMapper
11. Apply configuration information from Oson field annotations class to this FieldMapper. Oson has a single Field annotation class, which is ca.oson.json.FieldMapper
12. Apply configuration information using Java code, with the help of Oson Java configuration class: ca.oson.json.Oson.FieldMapper
13. Make use of this final configuration data to configure how a field in a Java class is mapped, for both of its name and value

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

#### <a name="TOC-Global-Options"></a>**Global Options**
    
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
		 * When process methods of a class, only use methods starting with "get"
		 * or "set", otherwise, any no-arg method returning values are considered as get,
		 * any method that accepts 1 value are considered set, excluding constructors
		 */
		private boolean setGetOnly = false;
		
		/*
		 * Determine if a field object should inherit its configuration from a higher level enclosing class
		 */
		private boolean inheritMapping = true;

		/*
		 * Dertermine if Oson should use @Expose annotation from Gson.
		 * Once Oson FieldMapper annotation is used, this flag will be disabled
		 * Full support in seriaze, partial support in deserialize
		 */
		private boolean useGsonExpose = false;

		/*
		 * Patterns of comments in Java regular expressions
		 * User can define custom comment regex patterns
		 * The default comments are: single-line //, 
		 * single-line #, 
		 * and multiple lines /* .... *\/
		 */
		private String[] commentPatterns = new String[] {"//[^\n\r]*\n?", "#[^\n\r]*\n?", "/\\*[^\\*/]*\\*/"};

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

You can specify only the ones you want to, and use the rest of default values. And you can specify any time, either before or during the serializing or deserializing process. You can use any of the Builder method to set the configuration value you desire, in a chain, and later ones will overwrite the earlier settings, or combined with you, depending on the circonstances. For most of the collection (Set) attributes, it behavior like add, unless you use a null value to set, which act like reset or clear, all previous values of this particular attribut is gone.

Some examples:
  * oson.pretty(): indentation is requested for output, the same as oson.pretty(true), or prettyPrinting(true)
  * oson.setLevel(5): maximum 5 levels of output and processing
  * oson.includeClassTypeInJson(true): include class name in Json output
  * oson.sort() === oson.sort(true) === oson.orderByKeyAndProperties(true): order output by key of a map or properties of a Java object
  * oson.setDefaultType(JSON_INCLUDE.NON_NULL): do not output null, use any of these values: ALWAYS, NON_NULL, NON_EMPTY, NON_DEFAULT, DEFAULT, NONE, to manage null or empty or default values
  * oson.setClassMappers(...): parameter can be one or multiple ClassMapper objects, useful to set class-level behavior
  * oson.setFieldMappers(...): parameter can be one or multiple FieldMapper objects, useful to set field-level behavior

Or you can chain them all up, like this:
String json = oson.pretty().setLevel(5).includeClassTypeInJson(true).sort().setDefaultType(JSON_INCLUDE.NON_NULL)...serialize(myObject);


#### <a name="TOC-Class-Mappers"></a>**Class Mappers**

ClassMapper class and its annotation counterpart control the class level configuration.

Here is the attributes for the class level configuration of the Java class ClassMapper. Its annotation partner has similar features, excluding ones requiring Object abilities, such as constructor:

```java
		/*
		 * the class type of this mapper
		 * primitive type is processed as Object counterpart in the aspect of configuration
		 */
		private Class<T> type;

		/*
		 * user implemented creator of this type
		 */
		public InstanceCreator<T> constructor;
		/*
		 * a default or dummy object of this type
		 */
		public T defaultValue;
		
		/*
		 * Fields or variables of this class type will be ignored or not
		 */
		public Boolean ignore = null;
		
		/*
		 * user defines function to convert specific type
		 * it can be user declared classes, or basic Java type, such as how an Integer value
		 * can be converted into string, using single function interface, such as a Lambda expression
		 */
		public Function serializer;
		
		/*
		 * a custom deserializer from user, using single function interface, such as a Lambda expression
		 */
		public Function deserializer;
		
		/*
		 *  class level specification on how to get values from an object
		 *  use field to get or set a value
		 */
		public Boolean useField = null;
		
		/*
		 * use attribute to get or set a value
		 * here attribute means a method, either a getter or a setter
		 */
		public Boolean useAttribute = null;
		
		/*
		 *  fields inside this class, mostly for user declared fields
		 *  include field or attribute with this java MODIFIER, such as public, private
		 */
		public Set<MODIFIER> includeFieldsWithModifiers = null;
		
		/*
		 *  class specific date formatting, such as "MM/dd/yyyy"
		 */
		public String simpleDateFormat = null;
		
		/*
		 * Sort a Json output based on the natural order of key for a Java map,
		 * or properties for a Java class object
		 */
		public Boolean orderByKeyAndProperties = null;
		
		/*
		 * Set a Json output using hard-coded list of properties in the specified order
		 */
		public String[] propertyOrders = null;

		/*
		 * Embed class name meta data in a Json output
		 */
		public Boolean includeClassTypeInJson = null;
		
		/*
		 * Ignore any field or attribute with a version greater than the specified value
		 */
		public Double ignoreVersionsAfter;
		
		/*
		 * Ignore any class, field, or attribute in this set of annotations
		 */
		public Set<Class> ignoreFieldsWithAnnotations = null;
		
		/*
		 * Ignore these specified properties
		 */
		public Set<String> jsonIgnoreProperties;
		
		/*
		 * Control certain values to be output to a Json document or not, such as NON_NULL
		 */
		public JSON_INCLUDE defaultType = null;
		
		/*
		 * maximum length of a string
		 */
		public Integer length = null;
		
		/*
		 * leading digits before 0 in a number
		 */
		public Integer precision = null;
		
		/*
		 * Number of digits after decimal point
		 * Only used for float, double, and big decimal
		 */
		public Integer scale = null;
		
		/*
		 * The minimum value a number can have
		 */
		public Long min = null;
		
		/*
		 * The maximum value a number is allowed
		 */
		public Long max = null;
		
		/*
		 * Output an enum to integer or a string format
		 */
		public EnumType enumType = null;
		
		/*
		 * convert a date to a long number or a string format
		 */
		public Boolean date2Long = null;
```

There are two ways to set up a Class mapper:

1. set directly to oson by class type
2. set through a new ClassMapper object, then set this object to the oson instance

The first one looks like: oson.setSimpleDateFormat(MyClass.class, "E, dd MMM yyyy HH:mm:ss Z")
			.setMax(Integer.class, 1000l)
			.setLength(MyCustomerClass.class, 6)
			.setMax(MyCustomerClass.class, 500l);
			
The second one looks like this: oson.clear().setPrecision(5).setScale(1);
		oson.setClassMappers(new ca.oson.json.Oson.ClassMapper[] {
		new ca.oson.json.Oson.ClassMapper(Float.class).setPrecision(3).setScale(0),
		new ca.oson.json.Oson.ClassMapper(Decimal2.class).setPrecision(8).setScale(5)
		});
		
They have the similar effect, and follows the overwriting rule: configuration for the same class type overwrites previous ones.


#### <a name="TOC-Field-Mappers"></a>**Field Mappers**

FieldMapper class and its annotation counterpart control the field level configuration.

The following are the attributes for the field or attribute level configuration of the Java class FieldMapper. Its annotation partner has similar features, excluding ones requiring Object abilities, such as serializer and deserializer:

```java
		/*
		 * Java property name
		 */
		public String java;
		
		/*
		 * Corresponding json name
		 */
		public String json;
		
		/*
		 * If present, it means the type of the enclosing class.
		 * otherwise, this mapper can be used by all properties with the same field name
		 */
		private Class<T> type;
		
		/*
		 * Not really used here, just for reference.
		 * Actual data are kept in another class called FieldData
		 */
		private Class<E> returnType;


		/*
		 * This field/attribute will be ignored if true
		 */
		public Boolean ignore = null;

		/*
		 * use field to get or set value of a Java object during serializing or deserializing
		 */
		public Boolean useField = null;
		
		/*
		 * Use attribute or getter and setter method of a Java object
		 */
		public Boolean useAttribute = null;

		/*
		 * Lambda expression style single method interface
		 * used to provide custom serializing mechanism
		 */
		public Function serializer;
		
		/*
		 * function for user to deserialize a Json data into Java property
		 */
		public Function deserializer;
		
		/*
		 * In case the field is an enumType, define its type to serialize
		 * Can be either int, or String format
		 */
		public EnumType enumType = null;
		
		/*
		 * Is this property is required, or not nullable
		 */
		public Boolean required = null;
		
		/*
		 * length of a string property
		 */
		public Integer length = null;
		
		/*
		 * the number of digits after decimal point in a float, double, or big decimal field
		 */
		public Integer scale = null;
		
		/*
		 * Non-zero leading digits
		 */
		public Integer precision = null;
		
		/*
		 * Minimum value of a property
		 */
		public Long min = null;
		
		/*
		 * Maximu value of a property
		 */
		public Long max = null;
		
		/*
		 * Default value of this property, in case it is required
		 * or defaultType is configured to be JSON_INCLUDE.DEFAULT
		 */
		public E defaultValue = null;
		
		/*
		 * How null or default values are handled in its serializing and deserializing process
		 */
		public JSON_INCLUDE defaultType = null;
		
		/*
		 *  serialize to double quotes, or not
		 */
		public Boolean jsonRawValue = null;
		
		
		/*
		 * If this value is true, the getter method of this property will return the Json data for the whole class.
		 * In a class, only one method returning a String value is allowed to set this value to true
		 */
		public Boolean jsonValue = null;
		
		/*
		 * method with this value set to true will get all properties not specified earlier.
		 * It will normally return a Map<String, Object>
		 */
		public Boolean jsonAnyGetter = null;

		/*
		 * method with this value set to true will set all properties not consumed earlier.
		 * It will normally store all the other data into a Map<String, Object>
		 */
		public Boolean jsonAnySetter = null;
		
		/*
		 * determine a date to be converted to long, instead of using date format to converted into a string
		 * This flag takes precedence over simpleDateFormat
		 */
		public Boolean date2Long = null;

		/*
		 * property specific date formatter, in case it is Date type
		 */
		private String simpleDateFormat = null;
```


### <a name="TOC-Serialize-Annotation"></a>Annotation

Annotations can be used to set how to name an attribute, change a value, etc. And you can have lots of options to do the same thing. All up to personal flavor. In most cases, you might choose to annotate your own classes, and to configure classes from external sources using Java configurations.

When faced with so many annotations, from different sources, and one processor only chooses to use its own set of annotations, a decision is made to implement a different Json-Java processor, which will support most of them, and also provide its own set of annotations: only two of them: one is class level, and anothe one is field level. Both of these annotations try to deliver the same amount of information as its counterpart class, with the same name, just slightly different class path.

For now, ClassMapper annotation holds 20 attributes, and FieldMapper annotation holds 21 attributes. The should cover most of existing annotations used in Java-Json conversion libaries, and with some extra ones used in JPA framework.

As described in the overwriting rules, Oson annotations will hide annotations from external sources, and Java configurations will overwrite annotation configurations, and the final effect can also be inherited in an object-oriented way.


### <a name="TOC-Serialize-Lambda-Expression"></a>Lambda Expression

Lambda expression is one of the most powerful featuers in Java programming language. Or Java tends to behavior like a functional language, apart from the pure object-oriented language idealism.

Lambda expression as a single functional interface is perfect to act as a serializer and deserializer. It gives the true powerful of transformation into Oson processor. Basically, it allows you to do everything, or almost anything you want, to have full access to contextual data, to return types of Java data you want. This only feature makes Oson as the one you like to use, as a Json-Java processor.

To serialize a class object, you can provide a serializer using lambda expression. All Oson serializer and deserializer interfaces are @FunctionalInterface, and they still support overloading, the reason behind this is that Java provides an nice feature: default method in an interface. Here is an extract, out of the total 41 interfaces:

```java
	public static interface OsonFunction extends Function {
		@Override
		public default Object apply(Object t) {
			return t;
		}
	}
	
	@FunctionalInterface
	public static interface Integer2JsonFunction extends OsonFunction {
		public String apply(Integer t);
	}
	
	@FunctionalInterface
	public static interface Json2IntegerFunction extends OsonFunction {
		public Integer apply(String t);
	}
	
	...
	
	@FunctionalInterface
	public static interface DataMapper2JsonFunction extends OsonFunction {
		public String apply(DataMapper classData);
	}
	
	@FunctionalInterface
	public static interface Json2DataMapperFunction extends OsonFunction {
		public Object apply(DataMapper classData);
	}
```

All data types have at least 3 overloading versions of functions:

one accepting DataMapper parameter, and returns String
the second accepts a specific data type, and returns String
the last one accepts an Object, and return Object

Take BigInteger as an example. You can see that the the first one is targeted directly for the current data type. The second one provides lots of contextual information to a user, do as the user wants in order to return a appropriate String. The last one is so flexible as to allow a user to return any thing as pleased, in third example, the function returns a Car object, from a simple BigInteger.

```java
	   @Test
	   public void testSerializeBigIntegerWithBigInteger2JsonFunction() {
		   BigInteger value = BigInteger.valueOf(6);
		   String expected = "Six";

		   oson.setClassMappers(new ClassMapper(BigInteger.class).setSerializer((BigInteger p) -> {
			   switch (p.intValue()) {
			   case 1: return "One";
			   case 2: return "Two";
			   case 3: return "Three";
			   case 4: return "Four";
			   case 5: return "Five";
			   case 6: return "Six";
			   case 7: return "Seven";
			   case 8: return "Eight";
			   case 9: return "Nine";
			   case 10: return "Ten";
			   default: return p.toString();
			   }
		   }));
		   
		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }

	   @Test
	   public void testSerializeBigIntegerWithDataMapper2JsonFunction() {
		   BigInteger value = BigInteger.valueOf(8);
		   String expected = "Eight";
		   
		   DataMapper2JsonFunction function = (DataMapper p) -> {
			   BigInteger bint = (BigInteger) p.getObj();
			   
			   switch (bint.intValue()) {
			   case 1: return "One";
			   case 2: return "Two";
			   case 3: return "Three";
			   case 4: return "Four";
			   case 5: return "Five";
			   case 6: return "Six";
			   case 7: return "Seven";
			   case 8: return "Eight";
			   case 9: return "Nine";
			   case 10: return "Ten";
			   default: return p.toString();
			   }
		   };

		   oson.setSerializer(BigInteger.class, function);
		   
		   String result = oson.serialize(value);
		   
		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testSerializeBigIntegerWithGenericFunction() {
		   BigInteger value = BigInteger.valueOf(8);
		   String expected = "{\"@class\":\"ca.oson.json.domain.Car\",\"doors\":4,\"year\":2016,\"brand\":\"Eight\",\"years\":null}";
		   
		   Function function = (Object p) -> {
			   BigInteger bint = (BigInteger) p;
			   
			   switch (bint.intValue()) {
			   case 1: return new Car("One");
			   case 2: return new Car("Two");
			   case 3: return new Car("Three");
			   case 4: return new Car("Four");
			   case 5: return new Car("Five");
			   case 6: return new Car("Six");
			   case 7: return new Car("Seven");
			   case 8: return new Car("Eight");
			   case 9: return new Car("Nine");
			   case 10: return new Car("Ten");
			   default: return new Car(p.toString());
			   }
		   };

		   oson.setSerializer(BigInteger.class, function);
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
```


## <a name="TOC-How-To-Convert-Json-Document-Java-Object"></a>How to convert Json document to Java object
 
It is a little bit more complex to convert Json document to Java object. The main reason is that we need figure out which Java object types to map the data inside the Json string to. There are only two ways to handle this hard task:

1. Pass in type information to the Json processor
2. Embed the type information inside the Json document

Here are the list of four public methods for deserialization:

```java
public <T> T deserialize(String source)

public <T> T deserialize(String source, Class<T> valueType)

public <T> T deserialize(String source, Type type)

public <T> T deserialize(String source, T obj)
```

The first method only uses a Json text document as source. This simply means that the Json document embeds its target Java class name(s) inside its content. Unless the data type is a simple one, there is no other way we are able to figure it out.

The second method accepts a class type, in addition to its Json content. This will work for most of the cases where we do not need to figure out the original type of generic data types in Java, which invovles class type erasure, where generic type information gets lost. In order to overcome these, we can use available implementation of java.lang.reflect.Type interface. This interface only defines a symple abstract method: public java.lang.String getTypeName(). The typeName is in the form of "Enclosing_class_name<component_class_name>", which allows us to figure out the component class inside the enclosing class. One complex implementation is Google's com.google.gson.reflect.TypeToken class. See [CollectionsTest](https://github.com/osonus/oson/blob/master/src/test/java/ca/oson/json/userguide/CollectionsTest.java) for its usage.

Yet there are still more complex cases involving Map, Array, or Collection, that can hold objects of various class types, using Object data type in its generic type: Map<String, Object>, Collection<Object>, or Object[], so it becomes a real challenge in figuring out each unique case. Some hard-coded approaches are recommended in GSON's documentation. Here we can adopt a simple apploach, either embed type information inside a Json document, or provide more type information insdie the implementation of the Type interface.

One of this implementation is ca.oson.json.ComponentType, which accepts one or multiple component types in one of its constructors:

```java
		public ComponentType(String typeName)
		
		public ComponentType(Type type)
		
		public ComponentType(Class type, Class... componentTypes)
```

The third constructor accepts a variable array of component types, which can be used to guess data types in complex data structures, such as array, collection, and map, including array, collection and map themselves. Oson has a sophisticated guessing algorithm to match Json input data to Java classes. It bases its matching criteria on the depth of data structures, field name and types, etc, to calculate a percentage float points, to decide the winner of a piece of data. Inside one load of processing, it accumulates its knowledge about the data types, and uses this component store as the knowledge basis for analysis. For most of the time, you only need to provide a top level user-defined class type.

Inside the [CollectionsTest](https://github.com/osonus/oson/blob/master/src/test/java/ca/oson/json/userguide/CollectionsTest.java) test cases, you can find various approaches to solve this issue. Pick one to show here:

```java
	@Test
	public void testDeserializeListOfMapListMap() {
		List<Object> expected = new ArrayList<>();

		Map<String, Object> map = new HashMap<>();
		Event event = new Event("GREETINGS", "guest");
		map.put("event", event);
		Customer customer = new Customer();
		map.put("customer", customer);
		Boolean[] bools = new Boolean[] { true, false, true };
		map.put("integer", 12345);
		map.put("string", "I am a string.");
		map.put("bools", bools);
		expected.add(map);

		int[][][] ints = { { { 1, 2 }, { 3, 24 } }, { { 5, 6 }, { 7, 8 } },
				{ { 9, 10 }, { 11, 12 } } };
		expected.add(ints);
		expected.add(999876);
		expected.add("This is a testing.");

		List<Object> list2 = new ArrayList<>();
		Car car = new Car("Ford", 4);
		list2.add(car);
		list2.add(1);
		Map<String, Object> map2 = new HashMap<>();
		Car car2 = new Car("Toyota", 2);
		map2.put("toyota", car2);
		Event event2 = new Event("HELLO", "hostess");
		map2.put("new_event", event2);
		list2.add(map2);
		expected.add(list2);

		Oson oson = new Oson();

		String json = oson.setDefaultType(JSON_INCLUDE.NON_NULL).serialize(
				expected);

		String myjson = "[{\"bools\":[true,false,true],\"string\":\"I am a string.\",\"integer\":12345,\"event\":{\"name\":\"GREETINGS\",\"source\":\"guest\"},\"customer\":{\"vehicles\":[{\"doors\":4,\"year\":2016,\"brand\":\"Audi\"},{\"doors\":4,\"year\":2016,\"brand\":\"Mercedes\"}],\"carList\":[{\"doors\":4,\"year\":2016,\"brand\":\"BMW\"},{\"doors\":4,\"year\":2016,\"brand\":\"Chevy\"}]}},[[[1,2],[3,24]],[[5,6],[7,8]],[[9,10],[11,12]]],999876,\"This is a testing.\",[{\"doors\":4,\"year\":2016,\"brand\":\"Ford\"},1,{\"toyota\":{\"doors\":2,\"year\":2016,\"brand\":\"Toyota\"},\"new_event\":{\"name\":\"HELLO\",\"source\":\"hostess\"}}]]";

		assertEquals(json, myjson);

		ComponentType type = new ComponentType(List.class, Customer.class,
				Event.class, Car.class, int[][][].class, Boolean[].class,
				HashMap.class, ArrayList.class);

		List<Object> result = oson.deserialize(myjson, type);

		for (int i = 0; i < result.size(); i++) {
			Object obj = result.get(i);
			if (i == 0) {
				Map<String, Object> mymap = (Map) obj;
				for (Map.Entry<String, Object> entry : mymap.entrySet()) {
					String key = entry.getKey();
					Object value = entry.getValue();

					if (value instanceof Event) {
						Event myevent = (Event) value;
						assertEquals(key, "event");
						assertEquals(event.toString(), myevent.toString());

					} else if (value instanceof Customer) {
						Customer mycustomer = (Customer) value;
						assertEquals(key, "customer");
						assertEquals(mycustomer.toString(), customer.toString());

					} else if (value instanceof Boolean[]) {
						Boolean[] mybools = (Boolean[]) value;
						assertEquals(key, "bools");
						String myboolstr = oson.serialize(mybools);
						String boolstr = oson.serialize(bools);
						assertEquals(myboolstr, boolstr);

					} else {
						assertEquals(value.toString(), map.get(key).toString());
					}
				}

			} else if (obj instanceof int[][][]) {
				int[][][] ints3 = (int[][][]) result.get(i);
				int[][][] intsexpected = (int[][][]) expected.get(i);

				for (int p = 0; p < ints3.length; p++) {
					for (int j = 0; j < ints3[0].length; j++) {
						for (int k = 0; k < ints3[0][0].length; k++) {
							assertEquals(intsexpected[p][j][k], ints3[p][j][k]);
						}
					}
				}

			} else if (i == 4) {
				List<Object> mylist2 = (List) obj;

				int j = 0;
				for (Object object : mylist2) {
					if (object instanceof Car) {
						Car cCar = (Car) object;
						assertEquals(cCar.toString(), car.toString());

					} else if (Map.class.isAssignableFrom(object.getClass())) {
						Map<String, Object> mymap2 = (Map) object;

						for (String key : mymap2.keySet()) {
							Object val = mymap2.get(key);

							if (obj instanceof Car) {
								Car mycar2 = (Car) val;
								assertEquals(key, "toyota");
								assertEquals(mycar2.toString(), car2.toString());

							} else if (obj instanceof Event) {
								Event myevent2 = (Event) obj;
								assertEquals(key, "new_event");
								assertEquals(myevent2.toString(),
										event2.toString());

							}
						}

					} else {
						assertEquals(object.toString(), list2.get(j).toString());
					}

					j++;
				}

			} else {
				assertEquals(expected.get(i).toString(), result.get(i)
						.toString());
			}
		}
	}
```

Inside this test case, we create a list of data, with 9 different types: Customer, Event, Car, int[][][], Boolean[], HashMap, ArrayList, and Integer, String. We only need to pass in the class types of self-defined classes, and complex data structure, into the variable array of ComponentType class, which implements the Type interface. The Oson library uses this information to figure out these data types correctly. The main reason to pass in such data types as List, Array, or Map is to confirm that we do use them inside the Json document, and it is not a mistake, as normally people would not do such a crazy thing, unless inside a actual class, which would have no problem to process. After Oson has serialized and deserialized this data structure successfully, it might have convinced you that it can handle very complex data structures.

### <a name="TOC-Deserialize-How-To-Create-Initial-Java-Object"></a>How to Create Initial Java Object

The same as the serialization process, we can provide configuration information to the tool to help it deserialize data into a target Java object correctly. In addition to the challenge to figure out the class types of some complex data structures, we still need to figure out a way to create its initial object from the Type in Java. Only after we have this initial object, we can then copy data from Json document into this initial object. It is not always easy to handle this task.

We are multiple ways you can help:

1. Provide a com.google.gson.InstanceCreator implementation using ClassMapper
2. Provide a default object directly using ClassMapper configuration
3. Provide constructor annotations

If all of these are not met, the tool will try tens of other ways to create a new instance, or fails in the end.

I tried to create a case where Oson cannot initiate a class by itself, but in these cases it can do it without particular help, one main reason is that the compiler I used must have included type information inside bytecode, so it causes no issue to get parameter names from it. In case parameter names inside constructors get erased, we do need to provide name support by annotations.

#### <a name="TOC-Implement-InstanceCreator"></a>**Implement InstanceCreator**

InstanceCreator is a simple interface that gives you a way to provide an initial object: public T createInstance(Type type). You can specify one with 
public ClassMapper setConstructor(InstanceCreator<T> constructor) inside ClassMapper class. Here is one test case at [CollectionsTest](https://github.com/osonus/oson/blob/master/src/test/java/ca/oson/json/userguide/NewInstanceTest.java):

```java
	@Test
	public void testDeserializeAnyBeanWithCreateInstance() {
		AnyBean expected = new AnyBean("Any Name", 35);
		expected.setType("Java");
		
		String json = oson.serialize(expected);
		String jsonExpected = "{\"name\":\"Any Name\",\"type\":\"Java\",\"age\":35}";
		
		assertEquals(jsonExpected, json);
		
		AnyBean result = oson.setClassMappers(AnyBean.class, new ClassMapper()
		.setConstructor(new InstanceCreator(){
			@Override
			public Object createInstance(Type type) {
				return new AnyBean(null, 0);
			}
			
		})).deserialize(json, AnyBean.class);

		assertEquals(expected.toString(), result.toString());
	}
```

#### <a name="TOC-Use-Default-Object"></a>**Use Default Object**

You can also provide a default object directly using ClassMapper configuration, instead of trying to fix the constructor issue. Here is the method:

```java
	@Test
	public void testDeserializeAnyBeanUseDefaultObject() {
		AnyBean expected = new AnyBean("Any Name", 35);
		expected.setType("Java");
		
		String json = oson.serialize(expected);
		String jsonExpected = "{\"name\":\"Any Name\",\"type\":\"Java\",\"age\":35}";
		
		assertEquals(jsonExpected, json);
		
		AnyBean result = oson
				.setDefaultValue(AnyBean.class, new AnyBean(null, 0))
				.deserialize(json, AnyBean.class);

		assertEquals(expected.toString(), result.toString());
	}
```

Simply set it as you wish, with one line of code: setDefaultValue(Class type, Object obj). Looks much simpler than the InstanceCreator approach?

#### <a name="TOC-Use-Constructor-Annotation"></a>**Use Constructor Annotation**

As most of Java configurations have an annotation approach, this one is for helping you build a new object. Give the constructor a flag, than give a name for each of its parameter. For example, this is the way Jackson does things:

```java
    @JsonCreator
    public Person(@JsonProperty("name") String name,
                  @JsonProperty("lastName") String lastName,
                  @JsonProperty("age") int age,
                  @JsonProperty("addressList") List<Address> addressList) {
        this.name = name;
        this.lastName = lastName;
        this.age = age;
        this.addressList = addressList;
    }
```

Here is Oson's version, as usual, do one level of annotations in one annotation class:

```java
    @FieldMapper(jsonCreator = BOOLEAN.TRUE)
    public AnyPoint(@FieldMapper(name = "point") Point point)
    {
      this.point = point;
    }
```

If curious, you can see how it works by using any IDE in debug mode.

### <a name="TOC-Deserialize-Lambda-Expression"></a>How to Use Lambda Expression to Deserialize Java Object

To deserialize a class object, you can provide a deserializer using lambda expression. Here is interface you use:
oson.setDeserializer(Class<T> type, Function deserializer), or oson.setDeserializer(Class<T> type, Json2DataMapperFunction deserializer). The two versions are overloading each other.

If you provide a specific parameter, it will use it, which is Json2DataMapperFunction. Otherwise, it will use the generic one, which is java.util.function.Function. Here is the rule for handling a deserializer: if it returns an object of expected (which is Class<T> type), it will use this object as the deserialized product and return it. If the deserializer returns a null, then Oson uses this as your intention to ignore this class and returns null. Any other cases, Oson will continue its normal routine, which is to continue the deserialization process.

Here is an example of lambda expression as a deserializer:

```java
	   @Test
	   public void testDeserializeListWithDataMapper() {
		   Car car = new Car("Chevron", 2);
		   
		   String json = oson.serialize(car);

		   Json2DataMapperFunction function = (DataMapper p) -> {
			   Map<String, Object> data = p.getMap();
			   Car newcar = (Car) p.getObj();
			   
			   int doors = Integer.parseInt(data.get("doors").toString());
			   String brand = data.get("brand").toString();
			   
			   int level = p.getLevel();
			   
			   newcar.brand = brand + " is turned into a BMW at level " + level;
			   newcar.doors = doors * 2;

			   return newcar;
		   };
		   
		   Car newcar = oson.setDeserializer(Car.class, function).deserialize(json, Car.class);

		   assertNotEquals(car.toString(), newcar.toString());
		   
		   assertEquals(4, newcar.doors);
		   
		   assertEquals("Chevron is turned into a BMW at level 0", newcar.brand);
	   }
```

The DataMapper parameter to function Json2DataMapperFunction provides lots of detailed information to help you build your own version of deserializer.


## <a name="TOC-How-To-Filter-Out-Information"></a>How to filter out information

Json-Java converter is all about data exchange, and filtering is one major aspect of this conversion process. You can choose what to show, based on various criteria.

### <a name="TOC-Filter-Java-Configuration"></a>Java Configuration

1. 








