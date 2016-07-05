package ca.oson.json.util;

import java.lang.reflect.Field;

import ca.oson.json.Oson;

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
			Object value = f.get(src);
			if (all
					|| (value != null && !value.toString().trim()
							.equals(""))) {
				f.set(dest, value);
			}
		} catch (ReflectiveOperationException | IllegalArgumentException e) {
			// throw new RuntimeException(e);
		}
	}

}


