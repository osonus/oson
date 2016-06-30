# oson
Oson means O(bject) Json, and pronounces as awesome. It is a Java serialization/deserialization library that can convert Java Objects into JSON and back.

This library implements its own Java to/from Json processor, and provides a common interface to Google's Gson and Jackson's ObjectMapper.

Just like annotation and lambda expressions make Java look like a functional language, these features make Oson as the most flexible choice of Java-Json processors.

###*Oson Goals*
  * Convert arbitray Java Object to Json data and back
  * Provide a common interface to Gson and ObjectMapper
  * Support major set of Json annotations: including com.fasterxml.jackson, com.google.gson, org.codehaus.jackson, javax.persistence, javax.validation (JPA), in addition to its own ca.oson.json annotation
  * Allow 3 level control of name and value conversions: global, class-level, and field level
  * Allow these conversions to be either annotation-oriented, or Java oriented, or both
  * Allow well-formatted JSON output: any indentation, any depth, as far as object linkage goes, without redundancy
  * Function of lambda expressions is added to the serialization and deserialization processes, allowing limitless value transformation, with an ease of mind

###*Oson Documentation*

  * Gson [user guide](https://github.com/osonus/oson/blob/master/UserGuide.md): This guide contains examples on how to use Oson in your code.

###*Usage*

```
		Oson oson = new Oson().includeClassTypeInJson(true);
		String gson = oson.serialize(new GsonBuilder());
		System.out.println(gson);
		GsonBuilder gbuilder = oson.deserialize(gson);
		
		String gson2 = oson.prettyPrinting(true).toJson(gbuilder);
		System.err.println(gson2);
		GsonBuilder gbuilder2 = oson.fromJson(gson2);
		
		String gson3 = oson.writeValueAsString(gbuilder2);
		System.out.println(gson3);
		
		if (gson2.equals(gson3)) {
			System.err.println("Successful");
		} else {
			System.err.println("Failed!!!");
		}
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