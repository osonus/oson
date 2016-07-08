package ca.oson.json.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import ca.oson.json.Oson;

/*
 * all: true: make it do deep copying all
 * else, only do a shallow copy
 */
public class CopyObjects {
	public static void copy(Object src, Object dest) {
		copy(src, dest, false);
	}

	public static void copy(Object src, Object dest, boolean all) {
		copyFields(src, dest, src.getClass(), all);
	}

	private static void copyFields(Object src, Object dest, Class<?> klass,
			boolean all) {
		Field[] fields = Oson.getFields(klass);
		for (Field f : fields) {
			f.setAccessible(true);
			copyFieldValue(src, dest, f, all);
		}
	}

	private static void copyFieldValue(Object src, Object dest, Field f,
			boolean all) {
		try {
			Class fieldType = f.getType();
			
			if (f.isSynthetic() || Modifier.isTransient(f.getModifiers())) {
				return;
			}
			
			Object value = f.get(src);
			
			if (all) {
				if (!ObjectUtil.isBasicDataType(fieldType)) {
					try {
						Object newObject = fieldType.newInstance();
						f.set(dest, newObject);
						copyFields(value, newObject, fieldType, all);
						return;
					} catch (Exception e) {
						// e.printStackTrace();
					}
				}
			}

			if (all || StringUtil.isEmpty(value)) {
				f.set(dest, value);
			}
		} catch (ReflectiveOperationException | IllegalArgumentException e) {
			// throw new RuntimeException(e);
		}
	}

}


