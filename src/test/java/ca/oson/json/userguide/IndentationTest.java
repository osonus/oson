package ca.oson.json.userguide;

import java.util.ArrayList;

import org.junit.Test;

import com.google.gson.GsonBuilder;

import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.domain.Support;
import ca.oson.json.domain.Volume;
import ca.oson.json.domain.VolumeContainer;
import ca.oson.json.support.TestCaseBase;
import ca.oson.json.util.StringUtil;

public class IndentationTest extends TestCaseBase {

	@Test
	public void testSerializationGsonBuilder() {

		GsonBuilder gbuilder = new GsonBuilder();

		String gson = oson.pretty(false).includeClassTypeInJson(true).useAttribute(false).serialize(gbuilder);
				//.setLevel(3).serialize(gbuilder);

		GsonBuilder gbuilder2 = oson.deserialize(gson);
		String gson2 = oson.serialize(gbuilder2);
		assertEquals(gson, gson2);

		GsonBuilder gbuilder3 = oson.deserialize(gson2);
		String gson3 = oson.serialize(gbuilder3);
		assertEquals(gson2, gson3);

		String gson4 = oson.pretty(true).includeClassTypeInJson(false).serialize(gbuilder3);
		
		//System.out.println(gson4);
	}
	
	
	@Test
	public void testSerializationVolume() {
		Support support = new Support();
		support.status = "supported";
		
		Volume volume = new Volume();
		volume.support = support;
		volume.status = "available";
		volume.managed = true;
		volume.size = 12;
		
		VolumeContainer volumeContainer = new VolumeContainer();
		volumeContainer.volumes = new ArrayList<Volume>();
		volumeContainer.volumes.add(volume);
		
		oson.pretty().setDefaultType(JSON_INCLUDE.NON_NULL);
		String json = oson.serialize(volumeContainer);
		String expected = "{\n  \"volumes\": [\n    {\n      \"status\": \"available\",\n      \"managed\": true,\n      \"support\": {\n        \"status\": \"supported\"\n      },\n      \"size\": 12\n    }\n  ]\n}";
		assertEquals(expected, json);
		
		oson.setIndentation(1);
		json = oson.serialize(volumeContainer);
		expected = "{\n \"volumes\": [\n  {\n   \"status\": \"available\",\n   \"managed\": true,\n   \"support\": {\n    \"status\": \"supported\"\n   },\n   \"size\": 12\n  }\n ]\n}";
		assertEquals(expected, json);
		
		oson.setIndentation(5).setLevel(1);
		json = oson.serialize(volumeContainer);
		expected = "{\n     \"volumes\": [\n          {}\n     ]\n}";
		assertEquals(expected, json);
		
		StringUtil.SPACE = '-';
		json = oson.serialize(volumeContainer);
		expected = "{\n-----\"volumes\":-[\n----------{}\n-----]\n}";
		assertEquals(expected, json);
	}
}
