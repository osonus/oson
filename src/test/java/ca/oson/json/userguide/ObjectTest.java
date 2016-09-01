package ca.oson.json.userguide;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.gson.GsonBuilder;

import ca.oson.json.ClassMapper;
import ca.oson.json.ComponentType;
import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.OsonIO;
import ca.oson.json.domain.OrderedPerson;
import ca.oson.json.domain.Support;
import ca.oson.json.domain.Volume;
import ca.oson.json.domain.VolumeContainer;
import ca.oson.json.support.TestCaseBase;


public class ObjectTest extends TestCaseBase {
	
	
	@Test
	public void testSerializeOrderedPerson() {
		OrderedPerson obj = new OrderedPerson();
		String expected = "{\"firstName\":null,\"lastName\":null,\"addressList\":null,\"age\":0,\"birthDate\":null,\"title\":null}";
		assertEquals(expected, oson.clear().serialize(obj));
		
		String[] propertyOrders = new String[] {"title", "birthDate"};
		
		ClassMapper classMapper = new ClassMapper(OrderedPerson.class)
			.setOrderByKeyAndProperties(false)
			.setPropertyOrders(propertyOrders);
		String json = oson.setClassMappers(OrderedPerson.class, classMapper).serialize(obj);
		expected = "{\"title\":null,\"birthDate\":null,\"addressList\":null,\"firstName\":null,\"age\":0,\"lastName\":null}";
		assertEquals(expected, json);
	}
	

	@Test
	public void testSerializationObject() {
		BagOfPrimitives obj = new BagOfPrimitives();
		String json = oson.serialize(obj);

		String expected = "{\"value2\":\"abc\",\"value1\":1}";

		assertEquals(expected, json);
	}

	@Test
	public void testSerializationObjectSort() {
		BagOfPrimitives obj = new BagOfPrimitives();
		String json = oson.sort().serialize(obj);

		String expected = "{\"value1\":1,\"value2\":\"abc\"}";

		assertEquals(expected, json);
	}

	@Test
	public void testDeserializationObject() {
		BagOfPrimitives obj = new BagOfPrimitives();
		String json = oson.serialize(obj);

		BagOfPrimitives obj2 = oson.deserialize(json, BagOfPrimitives.class);

		String json2 = oson.serialize(obj2);

		assertEquals(json, json2);

		// System.out.println(json);
	}

//	@Test
//	public void testSerializationGsonBuilder() {
//
//		GsonBuilder gbuilder = new GsonBuilder();
//
//		String gson = oson.pretty(false).includeClassTypeInJson(true).setLevel(3).serialize(gbuilder);
//
//		GsonBuilder gbuilder2 = oson.deserialize(gson);
//		String gson2 = oson.serialize(gbuilder2);
//
//		GsonBuilder gbuilder3 = oson.deserialize(gson2);
//		String gson3 = oson.serialize(gbuilder3);
//		//assertEquals(gson2, gson3);
//
//		String gson4 = oson.pretty(true).setDefaultType(JSON_INCLUDE.NON_DEFAULT).serialize(gbuilder3);
//		
//		//System.out.println(gson4);
//		//assertFalse(gson2.length() == gson4.length());
//	}
	
	
//	@Test
//	public void testDeserializeVolume() {
//		OsonIO oson = new OsonIO();
//		URL url = getClass().getResource("volume.txt");
//		File file = new File(url.getPath());
//		
//		VolumeContainer vc = oson.readValue(file, VolumeContainer.class);
//		
//		assertEquals(((Volume)(vc.volumes.get(0))).support.status, "supported");
//
//		String json = oson.serialize(vc);
//
//		assertTrue(json.contains("\"This volume is not a candidate for management because it is already attached to a virtual machine.  To manage this volume with PowerVC, select the virtual machine to which the volume is attached for management. The attached volume will be automatically included for management.\""));
//
//		// System.out.println(json);
//	}

//	@Test
//	public void testDeserializeVolumeList() {
//		OsonIO oson = new OsonIO();
//		URL url = getClass().getResource("volume.txt");
//		File file = new File(url.getPath());
//		
//		ComponentType type = new ComponentType("java.util.Map<String, java.util.List<ca.oson.json.domain.Volume>>");
//		
//		Map<String, List<Volume>> vc = oson.readValue(file, type);
//		
//		assertEquals(((Volume)(vc.get("volumes").get(0))).support.status, "supported");
//
//		String json = oson.serialize(vc);
//
//		assertTrue(json.contains("\"This volume is not a candidate for management because it is already attached to a virtual machine.  To manage this volume with PowerVC, select the virtual machine to which the volume is attached for management. The attached volume will be automatically included for management.\""));
//
//		// System.out.println(json);
//	}

}


class BagOfPrimitives {
	private int value1 = 1;
	private String value2 = "abc";
	private transient int value3 = 3;

	BagOfPrimitives() {
		// no-args constructor
	}
}
