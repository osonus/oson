package ca.oson.json.function;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonDateTime;
import org.bson.BsonDocument;
import org.bson.BsonDouble;
import org.bson.BsonInt32;
import org.bson.BsonInt64;
import org.bson.BsonString;
import org.bson.BsonValue;

import ca.oson.json.Oson.FieldData;
import ca.oson.json.util.NumberUtil;

public class Json2BsonValueFunction implements OsonFunction {
	public static <E> BsonValue apply(FieldData fieldData) {
		Class returnType = fieldData.returnType;
		Object valueToProcess = fieldData.valueToProcess;
		Object returnObj = fieldData.returnObj;
		
		if (valueToProcess == null) {
			if (returnObj == null){
				return null;
			} else {
				valueToProcess = returnObj;
			}
			
		} else if (returnType == null) {
			returnType = valueToProcess.getClass();
		}
		
		if (returnType == String.class) {
			return new BsonString(valueToProcess.toString());

		} else if (returnType == Character.class || returnType == char.class) {
			return new BsonString(valueToProcess.toString());
			
		} else if (returnType == Boolean.class || returnType == boolean.class || returnType == AtomicBoolean.class) {
			return new BsonBoolean((boolean)valueToProcess);

		} else if (Number.class.isAssignableFrom(returnType) || returnType.isPrimitive()) {
			valueToProcess = NumberUtil.getNumber(valueToProcess, returnType);

			if (returnType == Integer.class || returnType == int.class) {
				return new BsonInt32((int)valueToProcess);
				
			} else if (returnType == Long.class || returnType == long.class) {
				return new BsonInt64((long)valueToProcess);
	
			} else if (returnType == Double.class || returnType == double.class) {
				return new BsonDouble((double)valueToProcess);
	
			} else if (returnType == Byte.class || returnType == byte.class) {
				return new BsonDouble((double)valueToProcess);
	
			} else if (returnType == Short.class || returnType == short.class) {
				return new BsonInt32((int)valueToProcess);
	
			} else if (returnType == Float.class || returnType == float.class) {
				return new BsonDouble((double)valueToProcess);
	
			} else if (returnType == BigDecimal.class) {
				return new BsonDouble((double)valueToProcess);
	
			} else if (returnType == BigInteger.class) {
				return new BsonInt64((long)valueToProcess);

			} else if (returnType == AtomicInteger.class) {
				return new BsonInt32((int)valueToProcess);
				
			} else if (returnType == AtomicLong.class) {
				return new BsonInt64((long)valueToProcess);
				
			} else { // default to Double, in case no specific type is specified
				return new BsonDouble((double)valueToProcess);
			}
			
		} else if (returnType.isEnum() || Enum.class.isAssignableFrom(returnType)) {
			if (Number.class.isAssignableFrom(returnObj.getClass())) {
				return new BsonInt32((int)returnObj);
			} else {
				return new BsonString(returnObj.toString());
			}
			
		} else if (returnType == Date.class || Date.class.isAssignableFrom(returnType)) {
			Date date = (Date) returnObj;
			return new BsonDateTime(date.getTime());

		} else if (returnType.isArray() || Collection.class.isAssignableFrom(returnType)) {
			BsonArray array = new BsonArray();
			
			if (returnType.isArray()) {
				int size = Array.getLength(valueToProcess);
				
				for (int i = 0; i < size; i++) {
					E v = (E) Array.get(valueToProcess, i);
					array.add(i, Json2BsonValueFunction.apply(new FieldData(v, v.getClass())));
				}
				
			} else {
				List list = (List)valueToProcess;
				for (Object v: list) {
					array.add(Json2BsonValueFunction.apply(new FieldData(v, v.getClass())));
				}
			}
			
			return array;

		} else if (returnType != Optional.class && Map.class.isAssignableFrom(returnType)) {
			BsonDocument document = new BsonDocument();
			
			Map<String, Object> map = (Map)valueToProcess;
			for (String key: map.keySet()) {
				Object v = map.get(key);
				document.append(key, Json2BsonValueFunction.apply(new FieldData(v, v.getClass())));
			}
			
			return document;
		}
		
		return new BsonString(valueToProcess.toString());
	}
}
