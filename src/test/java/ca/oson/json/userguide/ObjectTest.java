package ca.oson.json.userguide;

import java.io.File;
import java.net.URL;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import com.google.gson.GsonBuilder;

import ca.oson.json.ClassMapper;
import ca.oson.json.ComponentType;
import ca.oson.json.FieldMapper;
import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.Oson.JSON_PROCESSOR;
import ca.oson.json.Oson.MODIFIER;
import ca.oson.json.OsonIO;
import ca.oson.json.domain.DateTime;
import ca.oson.json.domain.IgnoreObject;
import ca.oson.json.domain.OrderedPerson;
import ca.oson.json.domain.RawValues;
import ca.oson.json.domain.Support;
import ca.oson.json.domain.Volume;
import ca.oson.json.domain.VolumeContainer;
import ca.oson.json.support.TestCaseBase;


public class ObjectTest extends TestCaseBase {
	@Test
	public void testSerializeIgnoreObject() {
		oson.clear();
		IgnoreObject obj = new IgnoreObject();
		
		String expected = "{}";

		// ignore by annotation
		oson.setVersion(1.5);
		String json = oson.serialize(obj);
		assertEquals(expected, json);
		
		// show all
		oson.setAnnotationSupport(false);
		oson.includeFieldsWithModifiers(new MODIFIER[] {MODIFIER.Transient, MODIFIER.Volatile, MODIFIER.Private, MODIFIER.Public});
		expected = "{\"addressList\":null,\"firstName\":null,\"birthDate\":null,\"aint\":null,\"ch\":null,\"intValue\":null,\"byte\":16,\"shortValue\":0,\"longValue\":null,\"title\":null,\"lastName\":null,\"fvalue\":null,\"myInt\":12,\"age\":0}";
		json = oson.serialize(obj);

		assertEquals(expected, json);

		// ignore by version, fieldmapper java configuration overwrites class level configuration and annotation
		oson.setAnnotationSupport(true);
		oson.setFieldMappers(new FieldMapper[] {
				new FieldMapper("age", IgnoreObject.class).setSince(1.0),
				new FieldMapper("firstName", IgnoreObject.class).setUntil(1.51)
		});
		expected = "{\"firstName\":null,\"birthDate\":null,\"title\":null,\"age\":0}";
		json = oson.serialize(obj);
		assertEquals(expected, json);

		// ignore them again, using java configuration
		oson.setAnnotationSupport(false);
		Set<MODIFIER> includeFieldsWithModifiers = null;
		oson.includeFieldsWithModifiers(includeFieldsWithModifiers);

		oson.setFieldMappers(new FieldMapper[] {
				new FieldMapper("lastName", null, IgnoreObject.class),
				new FieldMapper("addressList", IgnoreObject.class).setIgnore(true),
				new FieldMapper("age", null, IgnoreObject.class),
				new FieldMapper("firstName", null, IgnoreObject.class),
				new FieldMapper("addressList", IgnoreObject.class).setIgnore(true),
				new FieldMapper("fvalue", IgnoreObject.class).setIgnore(true),
				new FieldMapper("ch", IgnoreObject.class).setIgnore(true),
				new FieldMapper("longValue", IgnoreObject.class).setIgnore(true),
				new FieldMapper("shortValue", IgnoreObject.class).setIgnore(true),
				new FieldMapper("aint", IgnoreObject.class).setIgnore(true),
				new FieldMapper("intValue", IgnoreObject.class).setIgnore(true),
				new FieldMapper("byte", IgnoreObject.class).setIgnore(true),
				new FieldMapper("myInt", IgnoreObject.class).setIgnore(true)
		});
		
		expected = "{}";
		json = oson.serialize(obj);
		assertEquals(expected, json);
	}
	
	
	@Test
	public void testSerializeRawValues() {
		String svalue = "String value";
		char cvalue = 'c';
		Character chvalue = 'h';

		oson.setDateFormat("yyyy-MM-dd");
		Date dvalue = oson.deserialize("2016-09-07", Date.class);
		JSON_PROCESSOR jvalue = JSON_PROCESSOR.GSON;
		
		RawValues obj = new RawValues(svalue, cvalue, chvalue, dvalue, jvalue);
		// annotation in work
		String expected = "{\"chvalue\":h,\"cvalue\":c,\"svalue\":String value,\"jvalue\":GSON,\"dvalue\":2016-09-07}";
		String json = oson.serialize(obj);
		assertEquals(expected, json);
		
		// disable annotation
		oson.setAnnotationSupport(false);
		expected = "{\"chvalue\":\"h\",\"cvalue\":\"c\",\"svalue\":\"String value\",\"jvalue\":\"GSON\",\"dvalue\":\"2016-09-07\"}";
		json = oson.serialize(obj);
		assertEquals(expected, json);
		
		// put back annotation, and use Java configuration to overwrite annotations
		oson.setAnnotationSupport(true);
		oson.setFieldMappers(new FieldMapper[] {
				new FieldMapper("chvalue", RawValues.class).setJsonRawValue(false),
				new FieldMapper("svalue", RawValues.class).setJsonRawValue(false)
		});
		
		expected = "{\"chvalue\":\"h\",\"cvalue\":c,\"svalue\":\"String value\",\"jvalue\":GSON,\"dvalue\":2016-09-07}";
		json = oson.serialize(obj);
		assertEquals(expected, json);
	}
	
	
	@Test
	public void testSerializeDateTime() {
		oson.clear().setDate2Long(false);
		oson.setDateFormat("yyyy-MM-dd HH:mm:ss");
		Date myDate = oson.deserialize("2015-08-13 05:10:00", Date.class);
		java.sql.Date sqlDate = oson.deserialize("2013-05-28 03:06:00", java.sql.Date.class);
		Timestamp myTime = oson.deserialize("2016-09-08 04:16:00", Timestamp.class);
				
		DateTime obj = new DateTime(myDate, sqlDate, myTime);
		
		oson.setSimpleDateFormat(DateTime.class, "yyyy/MM/dd HH:mm:ss");
		
		String expected = "{\"myDate\":\"2015/08/13 05:10:00\",\"myTime\":\"2016-09-08 04:16:00.000\",\"sqlDate\":\"2013/05/28\"}";
		
		String json = oson.serialize(obj);
		assertEquals(expected, json);
		
		// field mapper java configuration overwrites upper level ones
		Locale.setDefault(Locale.CANADA);
		oson.clear().setFieldMappers(new FieldMapper[] {
				new FieldMapper("myDate", DateTime.class).setDate2Long(true),
				new FieldMapper("sqlDate", DateTime.class).setDateFormat(DateFormat.FULL),
				new FieldMapper("myTime", DateTime.class).setDateFormat(DateFormat.LONG)
		});
		expected = "{\"myDate\":1439467800000,\"myTime\":\"September 8, 2016\",\"sqlDate\":\"Tuesday, May 28, 2013\"}";
		json = oson.print(obj);
		assertEquals(expected, json);
		
		// date and time part
		oson.setFieldMappers(new FieldMapper[] {
				new FieldMapper("myDate", DateTime.class).setDate2Long(false).setDateFormat(DateFormat.LONG),
				new FieldMapper("sqlDate", DateTime.class).setDateFormat(DateFormat.SHORT, DateFormat.MEDIUM),
				new FieldMapper("myTime", DateTime.class).setDateFormat(DateFormat.LONG, DateFormat.LONG),
		});
		expected = "{\"myDate\":\"August 13, 2015\",\"myTime\":\"September 8, 2016 4:16:00 PDT AM\",\"sqlDate\":\"28/05/13 3:06:00 AM\"}";
		json = oson.serialize(obj);
		assertEquals(expected, json);
		
		
		// apply locale
		oson.setFieldMappers(new FieldMapper[] {
				new FieldMapper("myDate", DateTime.class).setDate2Long(false).setDateFormat(DateFormat.LONG, Locale.UK),
				new FieldMapper("sqlDate", DateTime.class).setDateFormat(DateFormat.SHORT, DateFormat.MEDIUM, Locale.GERMAN),
				new FieldMapper("myTime", DateTime.class).setDateFormat(DateFormat.LONG, DateFormat.LONG, Locale.ITALIAN),
		});
		expected = "{\"myDate\":\"13 August 2015\",\"myTime\":\"8 settembre 2016 4.16.00 PDT\",\"sqlDate\":\"28.05.13 03:06:00\"}";
		json = oson.serialize(obj);
		assertEquals(expected, json);
	}
	
	
	
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
