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
