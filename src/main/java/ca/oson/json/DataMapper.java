package ca.oson.json;

import java.util.HashMap;
import java.util.Map;

public class DataMapper<T> {
	//Map<String, Object> map;
	// make it more flexible: allow any value to pass over, in addition to map only data
	private Object map;
	private Class<T> valueType;
	private T obj;
	private ClassMapper classMapper;
	private int level;
	
	public int getLevel() {
		return level;
	}

	public DataMapper(Object map, Class<T> valueType, T obj, ClassMapper classMapper, int level) {
		// when expose the data, do not get corrupted
		this.map = map;
		this.valueType = valueType;
		this.obj = obj;
		ClassMapper mapper = new ClassMapper();
		mapper = Oson.overwriteBy (mapper, classMapper);
		this.classMapper = mapper;
	}
	
	public DataMapper(Map<String, Object> map, Class<T> valueType, T obj, ClassMapper classMapper, int level) {
		// when expose the data, do not get corrupted
		this.map = new HashMap(map);
		this.valueType = valueType;
		this.obj = obj;
		ClassMapper mapper = new ClassMapper();
		mapper = Oson.overwriteBy (mapper, classMapper);
		this.classMapper = mapper;
	}

	public DataMapper(Class<T> valueType, T obj, ClassMapper classMapper, int level) {
		this.valueType = valueType;
		this.obj = obj;
		ClassMapper mapper = new ClassMapper();
		mapper = Oson.overwriteBy (mapper, classMapper);
		this.classMapper = mapper;
		this.level = level;
	}

	public Map<String, Object> getMap() {
		if (map != null && Map.class.isAssignableFrom(map.getClass())) {
			return (Map)map;
		}
		return null;
	}
	
	public Object getValueObject() {
		return map;
	}

	public Class<T> getValueType() {
		return valueType;
	}

	public T getObj() {
		return obj;
	}

	public ClassMapper getClassMapper() {
		return classMapper;
	}
}