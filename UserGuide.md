#Oson User Guide

1. [Overview](#TOC-Overview)
2. [Goals for Oson](#TOC-Goals-for-Oson)
3. [Oson Performance and Scalability](#TOC-Oson-Performance-and-Scalability)
4. [Oson Users](#TOC-Oson-Users)
5. [Using Oson](#TOC-Using-Oson)
  * [Using Oson with Maven](#TOC-Oson-With-Maven)
  * [Primitives Examples](#TOC-Primitives-Examples)
  * [Object Examples](#TOC-Object-Examples)
  * [Finer Points with Objects](#TOC-Finer-Points-with-Objects)
  * [Nested Classes (including Inner Classes)](#TOC-Nested-Classes-including-Inner-Classes-)
  * [Array Examples](#TOC-Array-Examples)
  * [Collections Examples](#TOC-Collections-Examples)
    * [Collections Limitations](#TOC-Collections-Limitations)
  * [Serializing and Deserializing Generic Types](#TOC-Serializing-and-Deserializing-Generic-Types)
  * [Serializing and Deserializing Collection with Objects of Arbitrary Types](#TOC-Serializing-and-Deserializing-Collection-with-Objects-of-Arbitrary-Types)
  * [Built-in Serializers and Deserializers](#TOC-Built-in-Serializers-and-Deserializers)
  * [Custom Serialization and Deserialization](#TOC-Custom-Serialization-and-Deserialization)
    * [Writing a Serializer](#TOC-Writing-a-Serializer)
    * [Writing a Deserializer](#TOC-Writing-a-Deserializer)
  * [Writing an Instance Creator](#TOC-Writing-an-Instance-Creator)
    * [InstanceCreator for a Parameterized Type](#TOC-InstanceCreator-for-a-Parameterized-Type)
  * [Compact Vs. Pretty Printing for JSON Output Format](#TOC-Compact-Vs.-Pretty-Printing-for-JSON-Output-Format)
  * [Null Object Support](#TOC-Null-Object-Support)
  * [Versioning Support](#TOC-Versioning-Support)
  * [Excluding Fields From Serialization and Deserialization](#TOC-Excluding-Fields-From-Serialization-and-Deserialization)
    * [Java Modifier Exclusion](#TOC-Java-Modifier-Exclusion)
    * [Oson's `@Expose`](#TOC-Oson-s-Expose)
    * [User Defined Exclusion Strategies](#TOC-User-Defined-Exclusion-Strategies)
  * [JSON Field Naming Support](#TOC-JSON-Field-Naming-Support)
  * [Sharing State Across Custom Serializers and Deserializers](#TOC-Sharing-State-Across-Custom-Serializers-and-Deserializers)
  * [Streaming](#TOC-Streaming)
6. [Issues in Designing Oson](#TOC-Issues-in-Designing-Oson)
7. [Future Enhancements to Oson](#TOC-Future-Enhancements-to-Oson)


## <a name="TOC-Overview"></a>Overview

There are three aspects of transformation in the conversion between Java objects and JSON document: attribute name, attribute value, and output formats. How to control these transformation processes are the main focus of this library. In order to provide a fine-tuned way of conversion, it is designed to support 3 level of configuration: global, class, and field levels. There are two strategy to implement these configuration: Java code oriented and annotation oriented.


## <a name="TOC-Goals-for-Oson"></a>Goals for Oson

  * Convert arbitray Java Object to Json data and back
  * Provide a user friendly common interface to Gson and ObjectMapper
  * Support major set of Json annotations: including com.fasterxml.jackson, com.google.gson, org.codehaus.jackson, javax.persistence, javax.validation (JPA), in addition to its own ca.oson.json annotation
  * Allow 3 level control of name and value conversions: global, class-level, and field level
  * Allow these conversions to be either annotation-oriented, or Java oriented, or both
  * Allow well-formatted JSON output: any indentation, any depth, as far as object linkage goes, without redundancy
  * Function of lambda expressions is added to the serialization and deserializatin processes, allowing limitless value transformation, with an ease of mind


## <a name="TOC-Oson-Performance-and-Scalability"></a>Oson Performance and Scalability


## <a name="TOC-Oson-Users"></a>Oson Users


## <a name="TOC-Using-Oson"></a>Using Oson


## <a name="TOC-Oson-With-Maven"></a>Using Oson with Maven


### <a name="TOC-Primitives-Examples"></a>Primitives Examples


### <a name="TOC-Object-Examples"></a>Object Examples


#### <a name="TOC-Finer-Points-with-Objects"></a>**Finer Points with Objects**



### <a name="TOC-Nested-Classes-including-Inner-Classes-"></a>Nested Classes (including Inner Classes)



### <a name="TOC-Array-Examples"></a>Array Examples



### <a name="TOC-Collections-Examples"></a>Collections Examples


#### <a name="TOC-Collections-Limitations"></a>Collections Limitations


### <a name="TOC-Serializing-and-Deserializing-Generic-Types"></a>Serializing and Deserializing Generic Types


### <a name="TOC-Serializing-and-Deserializing-Collection-with-Objects-of-Arbitrary-Types"></a>Serializing and Deserializing Collection with Objects of Arbitrary Types



### <a name="TOC-Built-in-Serializers-and-Deserializers"></a>Built-in Serializers and Deserializers



### <a name="TOC-Custom-Serialization-and-Deserialization"></a>Custom Serialization and Deserialization



#### <a name="TOC-Writing-a-Serializer"></a>Writing a Serializer



#### <a name="TOC-Writing-a-Deserializer"></a>Writing a Deserializer


### <a name="TOC-Writing-an-Instance-Creator"></a>Writing an Instance Creator


#### <a name="TOC-InstanceCreator-for-a-Parameterized-Type"></a>InstanceCreator for a Parameterized Type


### <a name="TOC-Compact-Vs.-Pretty-Printing-for-JSON-Output-Format"></a>Compact Vs. Pretty Printing for JSON Output Format



### <a name="TOC-Null-Object-Support"></a>Null Object Support



### <a name="TOC-Versioning-Support"></a>Versioning Support



### <a name="TOC-Excluding-Fields-From-Serialization-and-Deserialization"></a>Excluding Fields From Serialization and Deserialization



#### <a name="TOC-Oson-s-Expose"></a>Oson's `@Expose`


#### <a name="TOC-User-Defined-Exclusion-Strategies"></a>User Defined Exclusion Strategies




### <a name="TOC-JSON-Field-Naming-Support"></a>JSON Field Naming Support



### <a name="TOC-Sharing-State-Across-Custom-Serializers-and-Deserializers"></a>Sharing State Across Custom Serializers and Deserializers


### <a name="TOC-Streaming"></a>Streaming



## <a name="TOC-Issues-in-Designing-Oson"></a>Issues in Designing Oson



## <a name="TOC-Future-Enhancements-to-Oson"></a>Future Enhancements to Oson









