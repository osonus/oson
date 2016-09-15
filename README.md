# oson
Oson means O(bject) Json, and pronounces as awesome. It is a Java serialization/deserialization library that can convert Java Objects into JSON and back.

This library implements its own Java to/from Json processor, and provides a common interface to Google's Gson and Jackson's ObjectMapper.

Just like annotation and lambda expressions make Java look like a functional language, these features make Oson as a flexible choice of Java-Json processors.

###*Oson Goals*
  * Convert arbitray Java Object to Json data and back
  * Provide a common interface to Gson and ObjectMapper
  * Support major set of Json annotations: including com.fasterxml.jackson, com.google.gson, org.codehaus.jackson, javax.persistence, javax.validation (JPA), in addition to its own ca.oson.json annotation
  * Allow 3 level control of name and value conversions: global, class-level, and field level
  * Allow these conversions to be either annotation-oriented, or Java oriented, or both
  * Allow well-formatted JSON output: any indentation, any depth, as far as object linkage goes, without redundancy
  * Function of lambda expressions is added to the serialization and deserialization processes, allowing easy value transformation

###*Oson Documentation*
  * Oson [API](http://www.javadoc.io/doc/ca.oson.json/oson): Javadocs for the current Oson release
  * Oson [user guide](https://github.com/osonus/oson/blob/master/UserGuide.md): This guide contains examples on how to use Oson in your code.

###*Using Oson with Maven*
To use Oson with Maven2/3, you can use the Oson version available in Maven Central by adding the following dependency:

```xml
<dependencies>
	<!-- https://mvnrepository.com/artifact/ca.oson.json/oson -->
	<dependency>
	    <groupId>ca.oson.json</groupId>
	    <artifactId>oson</artifactId>
	    <version>1.0.5</version>
	</dependency>
</dependencies>
```

###*Usage*

```
		Oson oson = new Oson();
		
		String json = "[\"Hello World\",\"\"]";

		Set set = oson.deserialize(json, LinkedHashSet.class);
		assertEquals(LinkedHashSet.class, set.getClass());
		assertEquals(json, oson.serialize(set));
		
		set = oson.asGson().fromJson(json, LinkedHashSet.class);
		assertEquals(LinkedHashSet.class, set.getClass());
		assertEquals(json, oson.toJson(set));
		
		set = oson.asJackson().readValue(json, LinkedHashSet.class);
		assertEquals(LinkedHashSet.class, set.getClass());
		assertEquals(json, oson.writeValueAsString(set));
```

###*License*

Oson is released under the [Apache 2.0 license](LICENSE).

```
Copyright 2016 oson.ca

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```