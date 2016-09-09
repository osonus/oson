#Oson User Guide

1. [Overview](#TOC-Overview)
2. [Goals for Oson](#TOC-Goals-for-Oson)
3. [Using Oson](#TOC-Using-Oson)
  * [Using Oson with Maven](#TOC-Oson-With-Maven)
  * [Interface to Gson and ObjectMapper](#TOC-Interface-To-Gson-And-ObjectMapper)
  * [Examples and Tests](#TOC-Oson-Example-and-Test)
  * [General Conversion Rules](#TOC-General-Conversion-Rules)
4. [How to convert Java object to Json document](#TOC-How-To-Convert-Java-Object-To-Json-Document)
  * [Java Configuration](#TOC-Serialize-Java-Configuration)
    * [Global Options](#TOC-Global-Options)
    * [Class Mappers](#TOC-Class-Mappers)
    * [Field Mappers](#TOC-Field-Mappers)
  * [Annotation](#TOC-Serialize-Annotation)
  * [Lambda Expression](#TOC-Serialize-Lambda-Expression)
  * [Class Type](#TOC-Serialize-Class-Type)
  * [Use Fields or Getters](#TOC-Serialize-Use-Fields-Or-Getters)
  * [Use a Json serializer Method](#TOC-Serialize-Use-Json-Serializer-Method)
  * [Change Attribute Names](#TOC-Serialize-Change-Attribute-Names)
  * [Ignore or Include](#TOC-Serialize-Ignore-Or-Include)
  * [Change Attribute Values](#TOC-Serialize-Change-Attribute-Values)
    * [Null, Empty, Default Values](#TOC-Serialize-Null-Empty-Default-Values)
    * [Raw Values](#TOC-Serialize-Raw-Values)
    * [Property Orders](#TOC-Serialize-Property-Orders)
    * [Serialize Date](#TOC-Serialize-Date)
  
5. [How to convert Json document to Java object](#TOC-How-To-Convert-Json-Document-Java-Object)
  * [How to Create Initial Java Object](#TOC-Deserialize-How-To-Create-Initial-Java-Object)
    * [Implement InstanceCreator](#TOC-Implement-InstanceCreator)
    * [Use Default Object](#TOC-Use-Default-Object)
    * [Use Constructor Annotation](#TOC-Use-Constructor-Annotation)
  * [Lambda Expression](#TOC-Deserialize-Lambda-Expression)


## <a name="TOC-Overview"></a>Overview

There are three aspects of transformation in the conversion between Java objects and JSON documents: attribute name, attribute value, and output structures. How to control these transformation processes are the main focus of this library. In order to provide a fine-tuned way of conversion, it is designed to support 3 level of configuration: global, class, and field levels. There are two strategies to implement these configurations: Java code oriented and annotation oriented one.

Four general rules are applied during a conversion process:

1. Lower level configurations inherit from higher level ones, if missing
2. Lower level configurations override higher level ones, if exist
3. Java code configurations override annotations
4. Oson annotation override annotations from other sources


## <a name="TOC-Goals-for-Oson"></a>Goals for Oson

  * Convert arbitray Java Object to Json data and back
  * Provide a common interface to Gson and ObjectMapper
  * Support major set of Json annotations: including com.fasterxml.jackson, com.google.gson, org.codehaus.jackson, javax.persistence, javax.validation (JPA), in addition to its own ca.oson.json annotation
  * Allow 3 level control of name and value conversions: global, class-level, and field level
  * Allow these conversions to be either annotation-oriented, or Java oriented, or both
  * Allow well-formatted JSON output: any indentation, any depth, as far as object linkage goes, without redundancy
  * Functions of lambda expressions are added to the serialization and deserialization processes, allowing limitless value transformation, with an ease of mind


## <a name="TOC-Using-Oson"></a>Using Oson

The primary class to use is [`Oson`](src/main/java/ca/oson/json/Oson.java) which you can just create by calling `new Oson()`. There is also a supplemental class [`OsonIO`](src/main/java/ca/oson/json/OsonIO.java) available that can be used to create an Oson instance with various IO abilities.

The same instance keeps state and type information, you can call clear() on the instance to clear statement data, call clearAll() to drops all cached data, with the same effect of creating a new instance.


### <a name="TOC-Oson-With-Maven"></a>Using Oson with Maven

To use Oson with Maven2/3, you can use the Oson version available in Maven Central by adding the following dependency:

```xml
<dependencies>
	<!-- https://mvnrepository.com/artifact/ca.oson.json/oson -->
	<dependency>
	    <groupId>ca.oson.json</groupId>
	    <artifactId>oson</artifactId>
	    <version>1.0.1</version>
	</dependency>
</dependencies>
```

### <a name="TOC-Interface-To-Gson-And-ObjectMapper"></a>Interface to Gson and ObjectMapper

An Oson instance can be turned into a Gson object by calling asGson(), an ObjectMapper by calling asJackson(), and back to Oson again by asOson().

Or it can be (re)configured by setJsonProcessor, with either JSON_PROCESSOR.GSON, JSON_PROCESSOR.JACKSON, or JSON_PROCESSOR.OSON as parameter at any time.

To serialize into a String, you can call serialize, toJson, or writeValueAsString method, with (T source), or (T source, Type type) as parameter(s), where T is any Java type.

To deserialize the String back to a Java object, you can call deserialize, fromJson, or readValue method, with (String source), (String source, Class<T> valueType), (String source, T obj), or (String source, Type type) as parameters.


### <a name="TOC-Oson-Example-and-Test"></a>Oson Examples and Tests

A [`hello-world`](https://github.com/osonus/oson/blob/master/src/test/java/ca/oson/json/userguide/HelloWorldTest.java) example:

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
			System.err.println("What a day, awful!");
		}
		
		
		oson.asGson();
		
		json = oson.toJson(one);
		
		result = oson.fromJson(json, int.class);
		
		if (one == result) {
			System.out.println("Hello world, Gson!");
		} else {
			System.err.println("What a day, Gson!");
		}
		
		
		oson.asJackson();
		
		json = oson.writeValueAsString(one);
		
		result = oson.readValue(json, int.class);
		
		if (one == result) {
			System.out.println("Hello world, Jackson!");
		} else {
			System.err.println("What a day, Jackson!");
		}
	}
}
```

More than 800 test cases have been created and run.

These testing cases can be found at [github.com](https://github.com/osonus/oson/tree/master/src/test/java/ca/oson/json), and run by [TestRunner.java](https://github.com/osonus/oson/blob/master/src/test/java/ca/oson/json/TestRunner.java)


### <a name="TOC-General-Conversion-Rules"></a>General Conversion Rules

The first two general rules specify how to apply the 3 level configurations in Oson: global, class-level, and field or attribute level.

  * Lower level configurations inherit from higher level ones, if missing
  * Lower level configurations override higher level ones, if exist

This means that a setting in the global level will propagate into class-level, and the class-level settings go in turn to the field level.
It also means that a class-level setting will override the global settings, and a local one will replace the class level.

The second two general rules specify how to put these previous rules into practice by using either Java code based configurations, or annotation based configurations, or both, at global, class and field levels.

In order to achieve these features, two Java classes and two Annotation classes are used, with similar names and patterns:

2 Java classes:

  * ca.oson.json.ClassMapper
  * ca.oson.json.FieldMapper

2 Annotation classes:

  * ca.oson.json.annotation.ClassMapper
  * ca.oson.json.annotation.FieldMapper

The Java classes have slightly more features than their corresponding annotation classes, owing to the fact that annotation can only support primitive types and Enum. These classes combine features from external sources, including com.fasterxml.jackson, org.codehaus.jackson, com.google.gson, javax.persistence, and javax.validation. To simulate the null default concept in annotation, NONE enum entry is introduced to various enums, including the BOOOLEAN enum, which has 3 values: BOOOLEAN.TRUE, BOOOLEAN.FALSE, BOOOLEAN.NONE, corresponding to true, false, and null in Boolean Java type. This way, the value false can be used to override previous true value. For example, if a field is ignored in external Java classes, and we cannot change its source code, yet we can easily set ignore to be false using FieldMapper class.

The class and property level overriding rules are:

1. Global Java configuration at the top
2. Inherit configuration from a higher level class if the current object serves as a field in the higher level class, unless configured not to do so
3. Apply annotations from other sources at the class level
4. Override these previous settings with annotation from Oson, which is ca.oson.json.annotation.ClassMapper
5. Override previous settings using Java code configuration. This configuration class is ca.oson.json.ClassMapper. At this step, we have class level configuration for the current Java class. The following steps of each field in this class will use this setting as the basis for its configuration
6. Create a blank field mapper instance for certain property with a returnType
7. Get the class mapper of the returnType
8. Override this field mapper with the class mapper of the return type
9. Override this field mapper with the class mapper created at step 5
10. Apply configuration information from other sources to this FieldMapper
11. Apply configuration information from Oson field annotations class to this FieldMapper. Oson has a single Field annotation class, which is ca.oson.json.annotation.FieldMapper
12. Apply configuration information using Java code, with the help of Oson Java configuration class: ca.oson.json.FieldMapper
13. Make use of this final configuration data to configure how a field in a Java class is converted, for both of its name and value

Once you understand these overriding rules, you will be able to customize the Java-Json conversion process.

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


## <a name="TOC-How-To-Convert-Java-Object-To-Json-Document"></a>How to convert Java object to Json document

Use one of the three public methods to convert a Java object to a Json document: serialize, toJson, writeValueAsString.

oson or oson.asOson() is the default behavior, but it is easy to use Gson and Jackson's version: oson.asGson(), oson.asJackson(), will use these two popular Json-Java processors. Use oson.setPrintErrorUseOsonInFailure(true) to make it use oson's own feature if either gson or jackson fails and throws exception. The ObjectMapper and Gson objects are revealed by calling oson.getJackson() and oson.getGson(), then you can configure and use them as you normally do for Json-Java conversion.

Customization can be done using Java classes and annotations.

A Java object has variables with values, and methods that act on these values. Ideally to serialize an object is to keep its state, so it will become the same object again when deserialized from its saved states in Json format.

You can configure how to keep the state of a Java object in a text string of Json format:

1. Keep attributes-values of the original object, or a subset by providing a second class type parameter
2. Use fields or use getters to retrieve values
3. Use a single Json method to retrieve the seriazed text
4. Use toString method to retrieve the seriazed text
5. Ignore or Include certain attributes
6. Change the name of attribute to output
7. Change the value to output
8. Do not quote string value for some particular attribute(s)
9. Change some values for some attribute, or some types
10. Do not output null values
11. Do not output empty values
12. Do not output default values
13. Output attributes in certain order
14. Print out certain indentation
15. Output values only to certain levels
16. Keep class type meta data

Here is a simple example of serializing a Car object:

```java
		Oson oson = new Oson();
		Car car = new Car("Chevron", 6);
		String json = oson.serialize(car);
		System.out.println(json);
```

This code can be put into 1 line:
System.out.println(new Oson().serialize(new Car("Chevron", 6)));
or String json = new OsonIO().print(new Car("Chevron", 6));
which prints out as
{"doors":6,"date":null,"brand":"Chevron"}

Further details can be found at [SerializeCarTest](https://github.com/osonus/oson/blob/master/src/test/java/ca/oson/json/userguide/SerializeCarTest.java)


### <a name="TOC-Serialize-Java-Configuration"></a>Java Configuration

There are lots of way you can use to change the behavior of Oson Tool. At the center point, there is an option class that is used for this purpose. Global level configurations are applied directly to the Oson object. Class level Java configurations are done through ca.oson.json.ClassMapper, and applied using setClassMappers method. Field level Java configurations are done through ca.oson.json.FieldMapper, and applied using setFieldMappers method.

#### <a name="TOC-Global-Options"></a>**Global Options**

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

ClassMapper class and its annotation counterpart control the class level configuration. Its annotation partner has similar features, excluding ones requiring Object abilities, such as constructor.

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
		
They have the same effect, and follows the overwriting rule: configuration for the same class type overwrites previous ones in the configuration train.


#### <a name="TOC-Field-Mappers"></a>**Field Mappers**

ClassMapper settings of an object forms the basis to create FieldMapper settings of a particular fields or methods of a class.

FieldMapper class control the field level configuration. Its annotation partner has similar features, excluding ones requiring Object abilities, such as serializer and deserializer features.

Take the the name field of [Dog class](https://github.com/osonus/oson/blob/master/src/test/java/ca/oson/json/domain/Dog.java) as an example, its FieldMapper can be created as new FieldMapper("name", Dog.class), then it can be used to configure how the name field is going to be serialized and deserialized. Here is a list of configurations that makes sense: oson.setFieldMappers(new FieldMapper("name", Dog.class).setJson("Dog Name").setLength(6).setJsonRawValue(true).setJsonValue(true)). The following are a few example test cases: 

```java
	public void testSerializeFieldMapper() {
		FieldMapper fieldMapper = new FieldMapper("name", Dog.class).setIgnore(true);
		
		assertTrue(oson.serialize(dog).contains("\"name\":\"I am a dog\""));
		
		assertFalse(oson.setFieldMappers(fieldMapper).serialize(dog).contains("\"name\":\"I am a dog\""));
		
		fieldMapper.setIgnore(false).setLength(6);
		
		assertTrue(oson.serialize(dog).contains("\"name\":\"I am a\""));

		fieldMapper.setJsonValue(true);
		assertEquals("\"I am a\"", oson.serialize(dog));
		
		fieldMapper.setJsonRawValue(true);
		assertEquals("I am a", oson.serialize(dog));
		
		dog.setName("doggie");
		String2JsonFunction serializer = (String p) -> "My " + p;
		fieldMapper.setSerializer(serializer);
		assertEquals("My dog", oson.serialize(dog));
	}
```


### <a name="TOC-Serialize-Annotation"></a>Annotation

Annotations can be used to set how to name an attribute, change a value, etc. And you can have lots of options to do the same thing. All up to personal flavor. In most cases, you might choose to annotate your own classes, and to configure classes from external sources using Java configurations.

When faced with so many annotations, from different sources, and one processor only chooses to use its own set of annotations, a decision is made to implement a different Json-Java processor, which will support most of them, and also provide its own set of annotations: only two of them: one is class level, and anothe one is field level. Both of these annotations try to deliver the same amount of information as its counterpart class, with the same name, just slightly different class path.

For now, ClassMapper annotation holds 23 attributes, and FieldMapper annotation holds 23 attributes. They should cover most of existing annotations used in Java-Json conversion libaries, and with some extra ones used in JPA framework.

As described in the above overwriting rules, Oson annotations will hide annotations from external sources, and Java configurations will overwrite annotation configurations, and the final effect can also be inherited in an object-oriented way.

The serialize attribute of both ClassMapper and FieldMapper annotations can be set to enable them to be used in either serialization (BOOLEAN.TRUE), or deserialization (BOOLEAN.FALSE), or both (BOOLEAN.BOTH), or neither (BOOLEAN.NONE).

Annotations can be disabled in Oson by using oson.setAnnotationSupport(false).



### <a name="TOC-Serialize-Lambda-Expression"></a>Lambda Expression

Lambda expression is one of the most powerful featuers in Java programming language. Or Java tends to behavior like a functional language, apart from the pure object-oriented language idealism.

Lambda expression as a single functional interface is perfect to act as a serializer and deserializer. It gives the true powerful of transformation into Oson processor. Basically, it allows you to do everything, or almost anything you want, to have full access to contextual data, to return types of Java data you want. This only feature makes Oson as the one you like to use, as a Json-Java processor.

To serialize a class object, you can provide a serializer using lambda expression. All Oson serializer and deserializer interfaces are @FunctionalInterface, and they still support overloading, the reason behind this is that Java provides an nice feature: default method in an interface. Here is an extract, out of the total 43 interfaces:

```java
	public static interface OsonFunction extends Function {
		@Override
		public default Object apply(Object t) {
			return t;
		}
	}
	
	@FunctionalInterface
	public interface DataMapper2JsonFunction extends OsonFunction {
		public String apply(DataMapper classData);
	}
	
	@FunctionalInterface
	public interface FieldData2JsonFunction extends OsonFunction {
		public Object apply(FieldData fieldData);
	}
	
	@FunctionalInterface
	public interface Date2JsonFunction extends OsonFunction {
		public String apply(Date t);
	}
	
	@FunctionalInterface
	public interface Date2LongFunction extends OsonFunction {
		public Long apply(Date t);
	}
```

All data types have 3 overloading versions of functions and 1 specific version:
the first accepts [DataMapper](https://github.com/osonus/oson/blob/master/src/main/java/ca/oson/json/DataMapper.java) paramter, which provides sufficient data most of the time
the second accepts another data type: [FieldData](https://github.com/osonus/oson/blob/master/src/main/java/ca/oson/json/FieldMapper.java), which holds more data than DataMapper class
the third one accepts an Object, and return Object
the last one uses a specific type parameter, such as Integer, to use the specific function Integer2JsonFunction, and returns String

Date type gets one more function: Date2LongFunction, in addition to Date2JsonFunction.

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

These functions are easy to use: specify the data type you want in the lambda expression, and you will get what you ask for.


### <a name="TOC-Serialize-Class-Type"></a>Class Type

As an OO language, a Java object can be assigned to its current type, or any of its supper class or interface. Java object can also be output to Json text as any of its parent, with defined subset of data.

In the following case, Dog is a pet, a pet is an animal, and an animal is a Eukaryote. The same object is converted to a subset of data, based on its class type definition. An interface type does not keep its own state, so it makes sense to just output the original state of the object.

```java
	@Test
	public void testSerializeClassType() {
	    Dog dog = new Dog("I am a dog", BREED.GERMAN_SHEPHERD);
	    dog.setWeight(12.5);
	    
	    String expectedDog = "{\"owner\":null,\"bread\":\"GERMAN_SHEPHERD\",\"birthDate\":null,\"name\":\"I am a dog\",\"weight\":12.5,\"age\":1}";
	    String expectedPet = "{\"owner\":null,\"weight\":12.5,\"age\":1}";
	    String expectedAnimal = "{\"weight\":12.5,\"age\":1}";

	    assertEquals(expectedDog, oson.serialize(dog));
	    assertEquals(expectedPet, oson.serialize(dog, Pet.class));
	    assertEquals(expectedAnimal, oson.serialize(dog, Animal.class));
	    assertEquals(expectedDog, oson.serialize(dog, Eukaryote.class));
	}
```

This is the typical inheritance/polymorphism behavior in Java. There is another kind of inheritance: A class can have other class types as its attributes, such as a Pet has Person as its owner. In this case, Pet is the enclosing class for Person, and the settings in Pet can be configured to be inherited by enlosed Person objects, using this configuration method: oson.setInheritMapping(true).


### <a name="TOC-Serialize-Use-Fields-Or-Getters"></a>Use Fields or Getters

By default, Oson will try to use both fields and get methods to retrieve values at its best efforts. It can be configured to use either fields or use getters to retrieve values.

In the following example, we can see these settings in action:  term \"age\":1 is gone, when methods (in the name of attributes) are not used; the json becomes empty when both fields and methods are not used.

```java
	public void testSerializeUseFieldsOnly() {
		Dog dog = new Dog("I am a dog", BREED.GERMAN_SHEPHERD);
		dog.setWeight(12.5);

		oson.useAttribute(false);
		String expected = "{\"owner\":null,\"bread\":\"GERMAN_SHEPHERD\",\"birthDate\":null,\"name\":\"I am a dog\",\"weight\":12.5}";
		assertEquals(expected, oson.serialize(dog));
		
		oson.useField(false);
		expected = "{}";
		assertEquals(expected, oson.serialize(dog));
	}
```

The fieldVisibility(), setterVisibility(), and getterVisibility() values of annotations com.fasterxml.jackson.annotation.JsonAutoDetect and org.codehaus.jackson.annotate.JsonAutoDetect are processed into useField and useAttribute.

In a general sense, any Java method that takes no arguments and returns some value is considered to be a get method, and any method that takes one argument is considered to be a set method, excluding constructors. Oson allows you to specify if you are only interested in the ones that start with either "get" or "set", by calling oson.setGetOnly(true), which defaults to false.


### <a name="TOC-Serialize-Use-Json-Serializer-Method"></a>Use a Json serializer Method

There is a case to use a single method of a class to get the Json output. The following example first uses setJsonValueFieldName of ClassMapper for Dog class to set the toJsonMessage to be the method to return the Json output. Then uses setToStringAsSerializer to set toString() method of Dog class to do the same thing.

This call oson.setToStringAsSerializer(true) will make toString() method be the one responsible for creating Json outputs for all Java objects during serialization.

These Java code settings overwrites the annotation setting: @FieldMapper(ignore = BOOLEAN.TRUE).

```java
	public void testSerializeASingleMethod() {
		oson.clear().setClassMappers(new ClassMapper(Dog.class).setJsonValueFieldName("toJsonMessage"));

		String expected = "{\"name\":\"Json\"}";
		assertEquals(expected, oson.serialize(dog));

		
		oson.clear().setToStringAsSerializer(Dog.class, true);
		// oson.setToStringAsSerializer(true);

		expected = "{\"name\":\"Shepherd\"}";
		assertEquals(expected, oson.serialize(dog));
	}
```

You can achieve the same effect by using annotation: Add @FieldMapper(jsonValue = BOOLEAN.TRUE) to a method. 


### <a name="TOC-Serialize-Change-Attribute-Names"></a>Change Attribute Names

Use FIELD_NAMING enum to configure how to output attribute names.

The default behavior, FIELD_NAMING.FIELD, is to keep original Java field, getter names (removing 'get', lowercase the first letter), and non get method names.

You can change this default naming convention by calling setFieldNaming(FIELD_NAMING fieldNaming) on oson object, as demonstrated in the following test case:

```java
	public void testSerializeSetFieldNaming() {
		oson.clear().setFieldMappers(new FieldMapper("someField_name", Dog.class).setIgnore(false)).setFieldNaming(FIELD_NAMING.CAMELCASE);
		assertTrue(oson.serialize(dog).contains("\"someFieldName\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.UPPER_CAMELCASE).serialize(dog).contains("\"SomeFieldName\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.UNDERSCORE_CAMELCASE).serialize(dog).contains("\"some_Field_Name\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.UNDERSCORE_UPPER_CAMELCASE).serialize(dog).contains("\"Some_Field_Name\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.UNDERSCORE_LOWER).serialize(dog).contains("\"some_field_name\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.UNDERSCORE_UPPER).serialize(dog).contains("\"SOME_FIELD_NAME\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.SPACE_CAMELCASE).serialize(dog).contains("\"some Field Name\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.SPACE_UPPER_CAMELCASE).serialize(dog).contains("\"Some Field Name\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.SPACE_LOWER).serialize(dog).contains("\"some field name\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.SPACE_UPPER).serialize(dog).contains("\"SOME FIELD NAME\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.DASH_CAMELCASE).serialize(dog).contains("\"some-Field-Name\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.DASH_UPPER_CAMELCASE).serialize(dog).contains("\"Some-Field-Name\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.DASH_LOWER).serialize(dog).contains("\"some-field-name\":"));
		
		assertTrue(oson.setFieldNaming(FIELD_NAMING.DASH_UPPER).serialize(dog).contains("\"SOME-FIELD-NAME\":"));
	}
```

You can use annotation to achieve the same effect per field.

Here is one code section from Dog class:
```java
	@com.fasterxml.jackson.annotation.JsonProperty("Jackson name")
	@FieldMapper(ignore = BOOLEAN.TRUE)
	private String mySpecial_field_name;

	@com.fasterxml.jackson.annotation.JsonProperty("Jackson json property name")
	@FieldMapper(ignore = BOOLEAN.TRUE, name="Oson name overwrites names from external sources")
	@com.google.gson.annotations.SerializedName("Gson name")
	@javax.persistence.Column(name="Column name")
	@com.google.inject.name.Named("google inject name")
	@javax.inject.Named("inject name")
	@org.codehaus.jackson.annotate.JsonProperty("jackson codehaus name")
	private String special_field_name;
```

Here is a test case to check related field names:

```java
	public void testSerializeChangeAttributeName() {
		FieldMapper fieldMapper = new FieldMapper("special_field_name", Dog.class).setIgnore(false);
		oson.clear().setFieldMappers(new FieldMapper("mySpecial_field_name", Dog.class).setIgnore(false))
			.setFieldMappers(fieldMapper);

		assertTrue(oson.serialize(dog).contains("\"Jackson name\":"));

		assertTrue(oson.serialize(dog).contains("\"Oson name overwrites names from external sources\":"));
		
		fieldMapper.json = "Java name";
		oson.setFieldMappers(fieldMapper);
		assertTrue(oson.serialize(dog).contains("\"Java name\":"));
		
		fieldMapper.json = "";
		oson.setFieldMappers(fieldMapper);
		assertFalse(oson.serialize(dog).contains("\"Oson name overwrites names from external sources\":"));
	}
```

These tests confirm the following points:
  * annotaton and Java code configuration can be used to overwrite the global level naming settings
  * Oson FieldMapper name value overwrites name values of annotations from external sources
  * Each field or method in Oson has a Java name, and a Json name. If Json name is different from Java name, then this Json name will be used in its Json output
  * If Json name is set to empty string or null, this attribite is ignored


### <a name="TOC-Serialize-Ignore-Or-Include"></a>Ignore or Include

You can configure Oson and target Java classes to ignore or include types, attributes in various ways.

For custom class types, the following choices are available:
  * by default, transient and volatile attributes are ignored, unless using includeFieldsWithModifier(s) to modify this behavior
  * oson.includeFieldsWithModifier(MODIFIER includeFieldsWithModifier), where MODIFIER is an enum list of Public, Protected, Package, Private, Abstract, Final, Interface, Native, Static, Strict, Synchronized, Transient, Volatile, Synthetic, All, None. oson.includeFieldsWithModifiers takes Set or array of MODIFIER
  * oson.ignoreClassWithAnnotation(Class ignoreClassWithAnnotation) ignores a class with the specified annotation type. ignoreClassWithAnnotations takes Set or array of Annotation subclasses
  * oson.ignoreFieldsWithAnnotation(s) do the same thing for an attribute
  * oson.includeFieldsWithModifier(s) do the opposite for an attribute
  * use ca.oson.json.ClassMapper for a class type, as shown in [testSerializeIgnore() of ClassMapperTest](https://github.com/osonus/oson/blob/master/src/test/java/ca/oson/json/userguide/ClassMapperTest.java). You can ignore basic Java types, such as String, using ClassMapper, then all String fields will be ignored, unless overwritten by more specific configurations
    * method setIgnore to set to true or false
    * method setIncludeFieldsWithModifiers to include a set of MODIFIERs
    * method setJsonIgnoreProperties to set a set of properties to ignore
    * method setIgnoreFieldsWithAnnotations to ignore fields with certain annotations
    * since and until values to set version controls. The version is specified using oson.setVersion for a particular custom class
  * use ca.oson.json.annotation.ClassMapper
    * attribute ignore to be BOOLEAN.TRUE to a custom Java class
    * ignoreFieldsWithAnnotations to be a list of annotation full class names, used in any one of its fields or methods of the target class
    * includeFieldsWithModifiers to include any fields with specified MODIFIERs
    * jsonIgnoreProperties to ignore list of attribute names (get or set methods with out the first 3 letters, lower case the following initial letter)
    * since and until values to set version controls
  * use ca.oson.json.FieldMapper to specify an attribute. No matter what way you create this configuration object, you need to specify three values: enclosing class type, Java field name, and Json name to convert to or from
    * If Json name is set to null or empty, this field will be ignored
    * setIgnore(true) to ignore this attribute
    * since and until values to set version controls. Version numbers should fall inbetween since (inclusive) and until (exclusive) to be included. Version rules will process from top to lower levels
  * use ca.oson.json.annotation.FieldMapper
    * set ignore value to BOOLEAN.TRUE for a particular attribute
    * use since and/or until for version control
  * annotation com.google.gson.annotations.Since and com.google.gson.annotations.Until for both class type and attributes
  * com.fasterxml.jackson.annotation.JsonIgnoreProperties and org.codehaus.jackson.annotate.JsonIgnoreProperties for ignore attributes or properties, specified in class type level
  * annotation org.junit.Ignore for type and attributes
  * the followings are for attributes only: com.fasterxml.jackson.annotation.JsonIgnore, and org.codehaus.jackson.annotate.JsonIgnore
  * javax.persistence.Transient
  * not allowGetters() of com.fasterxml.jackson.annotation.JsonIgnoreProperties
  * com.google.gson.annotations.Since and/or com.google.gson.annotations.Until
  * com.fasterxml.jackson.annotation.JsonProperty
  * com.google.gson.annotations.Expose, which is handled in a slightly different way: once some attributes use Expose, other ones that are not Expose-annotated will be excluded. To enable Gson's Expose annotation, you need to call oson.useGsonExpose(true) specifically, and its behavior can also be overwritten by Oson's field level configuration

Some example use cases are provided in [testSerializeIgnoreObject() of ObjectTest](https://github.com/osonus/oson/blob/master/src/test/java/ca/oson/json/userguide/ObjectTest.java).

The settings of default type (JSON_INCLUDE), useField, and useAttribute can all be used to filter out information.

The features are combined in logical ways: following top-down, coarse to fine-grained, global level through class level to attribute levels, external to Oson overriding rules.


### <a name="TOC-Serialize-Change-Attribute-Values"></a>Change Attribute Values

There are various aspects and ways you can change attribute values to output to Json.

#### <a name="TOC-Serialize-Null-Empty-Default-Values"></a>**Null, Empty, Default Values**

The coarse level of control comes with oson.setDefaultType(JSON_INCLUDE defaultType), where enum JSON_INCLUDE can be ALWAYS, NON_NULL, NON_EMPTY, NON_DEFAULT, DEFAULT, and NONE.
  * The default is NONE, similar to ALWAYS for now, showing all values.
  * NON_NULL: do not output attributes with null values
  * NON_EMPTY: do not output empty values, such as "" String
  * NON_DEFAULT: do not output DEFAULT Java values, such as 0 for Integer
  * DEFAULT: use default values when they are null, empty when they are either required, or of primitive types

These settings are used in the following test cases:

```java
	public void testSerializeNullEmptyDefaultValues() {
	    String expectedNonNull = "{\"bread\":\"GERMAN_SHEPHERD\",\"name\":\"\",\"weight\":0,\"age\":1}";
	    String expectedNonEmpty = "{\"bread\":\"GERMAN_SHEPHERD\",\"weight\":0,\"age\":1}";
	    String expectedNonDefault = "{\"bread\":\"GERMAN_SHEPHERD\",\"age\":1}";
	    String expectedNonDefault2 = "{\"bread\":\"GERMAN_SHEPHERD\"}";
	    String expectedNonDefault3 = "{\"bread\":\"GERMAN_SHEPHERD\"}";
	    String expectedDefault = "{\"bread\":\"GERMAN_SHEPHERD\",\"age\":1}";
		
		dog.setName("");
		dog.setWeight(0.0);
		
		oson.clear().setDefaultType(JSON_INCLUDE.NON_NULL);
	    assertEquals(expectedNonNull, oson.serialize(dog));

		oson.setDefaultType(JSON_INCLUDE.NON_EMPTY);
	    assertEquals(expectedNonEmpty, oson.serialize(dog));

		oson.setDefaultType(JSON_INCLUDE.NON_DEFAULT);
	    assertEquals(expectedNonDefault, oson.serialize(dog));
	    
	    Integer integer = DefaultValue.integer;
	    DefaultValue.integer = 1;

	    assertEquals(expectedNonDefault2, oson.serialize(dog));

	    
	    oson.clear().setDefaultType(JSON_INCLUDE.DEFAULT);
	    
	    dog.setWeight(null);
	    DefaultValue.date = null;
	    
	    Double ddouble = DefaultValue.ddouble;

	    assertEquals("{\"owner\":null,\"bread\":\"GERMAN_SHEPHERD\",\"birthDate\":null,\"name\":\"\",\"weight\":0.0,\"age\":1}", oson.serialize(dog));

	    DefaultValue.ddouble = 1.0;
	    assertEquals("{\"owner\":null,\"bread\":\"GERMAN_SHEPHERD\",\"birthDate\":null,\"name\":\"\",\"weight\":1.0,\"age\":1}", oson.serialize(dog));

	    String format = "yyyy-MM-dd";
	    oson.setDateFormat(format);
	    Date date = oson.deserialize("2011-01-18", Date.class);
	    
	    FieldMapper fieldMapper = new FieldMapper("birthDate", Dog.class).setRequired(true);
	    oson.setFieldMappers(fieldMapper);

	    DefaultValue.date = date;
	    assertEquals("{\"owner\":null,\"bread\":\"GERMAN_SHEPHERD\",\"birthDate\":\"2011-01-18\",\"name\":\"\",\"weight\":1.0,\"age\":1}", oson.serialize(dog));

	    date = oson.deserialize("2011-01-19", Date.class);
	    ClassMapper classMapper = new ClassMapper(Date.class).setDefaultValue(date);
	    oson.setClassMappers(classMapper);
	    assertEquals("{\"owner\":null,\"bread\":\"GERMAN_SHEPHERD\",\"birthDate\":\"2011-01-19\",\"name\":\"\",\"weight\":1.0,\"age\":1}", oson.serialize(dog));

	    date = oson.deserialize("2011-01-20", Date.class);
	    fieldMapper.setDefaultValue(date);
	    assertEquals("{\"owner\":null,\"bread\":\"GERMAN_SHEPHERD\",\"birthDate\":\"2011-01-20\",\"name\":\"\",\"weight\":1.0,\"age\":1}", oson.serialize(dog));
	}
```

You can notice the following interesting behaviors regarding JSON_INCLUDE.NON_DEFAULT and JSON_INCLUDE.DEFAULT settings:
  * Some Java types can have default values, such as numberic types use 0 as default. Oson allows you to set arbutary values to be default for certain data types
  * When an attribute is required, primitive, or oson.setDefaultType(JSON_INCLUDE.DEFAULT), any null values will be turned into default value
  * Default values can be set globally, for a type, or for an attribute specifically. Take Date field (birthDate) as example, it will take the default values in the sequence of field, type, to global levell
  * When oson.setDefaultType(JSON_INCLUDE.NON_DEFAULT), any attribute with default value will be ignored
  
Annotation com.fasterxml.jackson.annotation.JsonInclude is translated into a value of JSON_INCLUDE defaultType in Oson.


#### <a name="TOC-Serialize-Raw-Values"></a>**Raw Values**

String, char, Character, enum String, or Date text values need to be (double-)quoted in Json output.

Top level variables are not quoted, as it makes sense to serialize a String back to a String, without any quotes.

These text values can be configured not to be double-quoted, using:
  * set jsonRawValue of FieldMapper Java class to be true
  * set jsonRawValue of FieldMapper annotation to be true
  * use com.fasterxml.jackson.annotation.JsonRawValue annotation for an attribute
  * use org.codehaus.jackson.annotate.JsonRawValue annotation for an attribute

These features are tested in [testSerializeRawValues() of ObjectTest](https://github.com/osonus/oson/blob/master/src/test/java/ca/oson/json/userguide/ObjectTest.java).


#### <a name="TOC-Serialize-Property-Orders"></a>**Property Orders**

Json properties of a class can be outputed using a hard-coded list of specified order, and can also be sorted by attribute names or map keys naturally.

```java
	public void testSerializeOrderedPerson() {
		OrderedPerson obj = new OrderedPerson();
		String expected = "{\"firstName\":null,\"lastName\":null,\"addressList\":null,\"age\":0,\"birthDate\":null,\"title\":null}";
		assertEquals(expected, oson.serialize(obj));
		
		String[] propertyOrders = new String[] {"title", "birthDate"};
		
		ClassMapper classMapper = new ClassMapper(OrderedPerson.class)
			.setOrderByKeyAndProperties(false)
			.setPropertyOrders(propertyOrders);
		String json = oson.setClassMappers(OrderedPerson.class, classMapper).serialize(obj);
		expected = "{\"title\":null,\"birthDate\":null,\"addressList\":null,\"firstName\":null,\"age\":0,\"lastName\":null}";
		assertEquals(expected, json);
	}
```

From the above test cases, we can conclude that Json properties of a class can be outputed:
  * following the ordered list set by propertyOrders value of annotation class ClassMapper
  * then ordered by orderByKeyAndProperties BOOLEAN value of annotation class ClassMapper
  * these annotation values are overriden by same name settings of ClassMapper Java class


#### <a name="TOC-Serialize-Date"></a>**Serialize Date**

Date gets some extra configuration options in Oson: it can be either converted to Long number, or to a custom-formated String text; For the text formatting, it involves date, time, and locale components.
  * first, use oson.setDate2Long(Boolean date2Long) to decide if a date is converted to Long number, or a text string
  * to format a date string, use one of the following methods, which are parts of the Java language features
```java
	public Oson setDateFormat(String simpleDateFormat) {
		if (simpleDateFormat != null) {
			options.setSimpleDateFormat(simpleDateFormat);
			reset();
		}

		return this;
	}
	public Oson setDateFormat(DateFormat dateFormat) {
		options.setDateFormat(dateFormat);
		reset();

		return this;
	}
	public Oson setDateFormat(int style) {
		options.setDateFormat(DateFormat.getDateInstance(style));
		reset();

		return this;
	}
	public Oson setDateFormat(int style, Locale locale) {
		options.setDateFormat(DateFormat.getDateInstance(style, locale));
		reset();

		return this;
	}
	public Oson setDateFormat(int dateStyle, int timeStyle) {
		options.setDateFormat(DateFormat.getDateTimeInstance(dateStyle, timeStyle));
		reset();

		return this;
	}
	public Oson setDateFormat(int dateStyle, int timeStyle, Locale locale) {
		options.setDateFormat(DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale));
		reset();

		return this;
	}
```

These global level settings can be overwritten by class level and field level settings, in the following order:
  * ca.oson.json.annotation.ClassMapper has two values for this purpose: date2Long and simpleDateFormat
  * ca.oson.json.ClassMapper java configuration: setDate2Long(Boolean date2Long), setDateFormat(DateFormat dateFormat), setSimpleDateFormat(String simpleDateFormat), setDateFormat(int style), setDateFormat(int style, Locale locale), setDateFormat(int dateStyle, int timeStyle), setDateFormat(int dateStyle, int timeStyle, Locale locale). These configurations can be achieved through oson.setDateFormat() directly
  * ca.oson.json.annotation.FieldMapper: date2Long and simpleDateFormat
  * ca.oson.json.FieldMapper: setDate2Long(Boolean date2Long), setDateFormat(DateFormat dateFormat), setSimpleDateFormat(String simpleDateFormat), setDateFormat(int style), setDateFormat(int style, Locale locale), setDateFormat(int dateStyle, int timeStyle), setDateFormat(int dateStyle, int timeStyle, Locale locale)

These features are tested in [testSerializeDateTime() of ObjectTest](https://github.com/osonus/oson/blob/master/src/test/java/ca/oson/json/userguide/ObjectTest.java).



  
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








