package ca.oson.json.userguide;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.EnumType;

import org.junit.Test;

import ca.oson.json.domain.BascicDateType;
import ca.oson.json.support.TestCaseBase;
import ca.oson.json.util.ObjectUtil;

public class BascicDateTypeTest extends TestCaseBase {
	@Test
	public void testSerializeBasicDateType() {
		BascicDateType datetype = new BascicDateType();
		
		datetype.character = 'd';
		datetype.pchar = 'A';

		String json = oson.clearAll().serialize(datetype);

		String expected = "{\"pbyte\":0,\"date\":\"1969-12-31T16:00:00.00Z\",\"pbool\":false,\"pfloat\":0,\"bigInteger\":0,\"dbyte\":0,\"bool\":false,\"string\":null,\"bigDecimal\":0,\"pshort\":0,\"pint\":0,\"integer\":0,\"dfloat\":0,\"processor\":\"GSON\",\"pdouble\":0,\"plong\":0,\"character\":\"d\",\"dshort\":0,\"dlong\":0,\"ddouble\":0,\"atomicLong\":0,\"simpleDateFormat\":\"yyyy-MM-dd'T'HH:mm:ss.SS'Z'\",\"atomicInteger\":0,\"pchar\":\"A\"}";

		//System.err.println(json);
		
		assertEquals(expected, json);
	}
	
	@Test
	public void testDeserializeBasicDateType() throws IllegalArgumentException, IllegalAccessException {
		BascicDateType datetype = new BascicDateType();

		String json = "{\"pbyte\":0,\"date\":\"1969-12-31T16:00:00.00Z\",\"pbool\":false,\"pfloat\":0,\"bigInteger\":0,\"dbyte\":0,\"bool\":false,\"string\":null,\"bigDecimal\":0,\"pshort\":0,\"pint\":0,\"integer\":0,\"dfloat\":0,\"processor\":GSON,\"pdouble\":0,\"plong\":0,\"character\":null,\"dshort\":0,\"dlong\":0,\"ddouble\":0,\"atomicLong\":0,\"simpleDateFormat\":\"yyyy-MM-dd'T'HH:mm:ss.SS'Z'\",\"atomicInteger\":0,\"pchar\":\"0\"}";
		
		BascicDateType result = oson.deserialize(json, BascicDateType.class);
		
		assertEquals(datetype.atomicInteger.intValue(), result.atomicInteger.intValue());
		
		Field[] fields = oson.getFields(BascicDateType.class);
		
		for (Field field: fields) {
			//System.err.println(field.getName());
			if (field.getType() == AtomicInteger.class) {
				assertEquals(((AtomicInteger)field.get(datetype)).intValue(), ((AtomicInteger)field.get(result)).intValue());

			} else if (field.getType() == AtomicLong.class) {
				assertEquals(((AtomicLong)field.get(datetype)).longValue(), ((AtomicLong)field.get(result)).longValue());

			} else {
				assertEquals(field.get(datetype), field.get(result));
			}
		}
	}
	

	@Test
	public void testSerializeBasicDateTypeRaw() {
		BascicDateType datetype = new BascicDateType();
		
		datetype.character = 'A';
		datetype.pdouble = 123.456;
		datetype.pfloat = 3.456f;
		datetype.ddouble = 5.34;
		datetype.dfloat = 89.345f;
		

		String json = oson.setEnumType(EnumType.ORDINAL).setDate2Long(true).serialize(datetype);

		Map<String, Object> map = (Map<String, Object>)oson.getListMapObject(json);
		
		Field[] fields = oson.getFields(BascicDateType.class);
		
		for (Field field: fields) {
			String name = field.getName();
//			System.err.println("\n" + field.getName() + ":");
			//System.err.println(field.getType());
			Object obj = map.get(name);
//			System.err.println(obj);

			if (obj != null) {
				//System.err.println(obj.getClass());
				assertTrue(ObjectUtil.isSameDataType(field.getType(), obj.getClass()));
			}
		}
	}
	
	
	
}
