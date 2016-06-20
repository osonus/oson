# oson
A Java serialization/deserialization library that can convert Java Objects into JSON and back.

This library implements its own Java to/from Json processor, and also provides a common interface to Google's Gson and Jackson's ObjectMapper.

###*Oson Goals*
  * Convert any arbitray Java Object to Json data and back
  * Provide a user friendly common interface to Gson and ObjectMapper
  * Support major set of Json annotations: including com.fasterxml.jackson, com.google.gson, org.codehaus.jackson, org.springframework, javax.persistence, javax.validation (JPA), in addition to its own ca.oson.json annotation

###*Oson Documentation*

  * Gson [user guide](https://github.com/osonus/oson/blob/master/UserGuide.md): This guide contains examples on how to use Oson in your code.

###*License*

Oson should be released under the [Apache 2.0 license](LICENSE).

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