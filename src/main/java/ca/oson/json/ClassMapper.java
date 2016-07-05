package ca.oson.json;

import java.util.Set;
import java.util.function.Function;

import javax.persistence.EnumType;

import ca.oson.json.function.*;
import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.Oson.MODIFIER;
import ca.oson.json.util.ObjectUtil;
import com.google.gson.InstanceCreator;

/*
 * configuration options for a specific class type
 */
public class ClassMapper<T> {
	/*
	 * the class type of this mapper
	 * primitive type is processed as Object counterpart in the aspect of configuration
	 */
	private Class<T> type;

	/*
	 * user implemented creator of this type
	 */
	public InstanceCreator<T> constructor;
	/*
	 * a default or dummy object of this type
	 */
	public T defaultValue;
	
	/*
	 * Fields or variables of this class type will be ignored or not
	 */
	public Boolean ignore = null;
	
	/*
	 * user defines function to convert specific type
	 * it can be user declared classes, or basic Java type, such as how an Integer value
	 * can be converted into string, using single function interface, such as a Lambda expression
	 */
	public Function serializer;
	
	/*
	 * a custom deserializer from user, using single function interface, such as a Lambda expression
	 */
	public Function deserializer;
	
	/*
	 *  class level specification on how to get values from an object
	 *  use field to get or set a value
	 */
	public Boolean useField = null;
	
	/*
	 * use attribute to get or set a value
	 * here attribute means a method, either a getter or a setter
	 */
	public Boolean useAttribute = null;
	
	/*
	 *  fields inside this class, mostly for user declared fields
	 *  include field or attribute with this java MODIFIER, such as public, private
	 */
	public Set<MODIFIER> includeFieldsWithModifiers = null;
	
	/*
	 *  class specific date formatting, such as "MM/dd/yyyy"
	 */
	public String simpleDateFormat = null;
	
	/*
	 * Sort a Json output based on the natural order of key for a Java map,
	 * or properties for a Java class object
	 */
	public Boolean orderByKeyAndProperties = null;
	
	/*
	 * Set a Json output using hard-coded list of properties in the specified order
	 */
	public String[] propertyOrders = null;

	/*
	 * Embed class name meta data in a Json output
	 */
	public Boolean includeClassTypeInJson = null;
	
	/*
	 * Ignore any field or attribute with a version greater than the specified value
	 */
	public Double ignoreVersionsAfter;
	
	/*
	 * Ignore any class, field, or attribute in this set of annotations
	 */
	public Set<Class> ignoreFieldsWithAnnotations = null;
	
	/*
	 * Ignore these specified properties
	 */
	public Set<String> jsonIgnoreProperties;
	
	/*
	 * Control certain values to be output to a Json document or not, such as NON_NULL
	 */
	public JSON_INCLUDE defaultType = null;
	
	/*
	 * maximum length of a string
	 */
	public Integer length = null;
	
	/*
	 * leading digits before 0 in a number
	 */
	public Integer precision = null;
	
	/*
	 * Number of digits after decimal point
	 * Only used for float, double, and big decimal
	 */
	public Integer scale = null;
	
	/*
	 * The minimum value a number can have
	 */
	public Long min = null;
	
	/*
	 * The maximum value a number is allowed
	 */
	public Long max = null;
	
	/*
	 * Output an enum to integer or a string format
	 */
	public EnumType enumType = null;
	
	/*
	 * convert a date to a long number or a string format
	 */
	public Boolean date2Long = null;
	
	
	public ClassMapper() {
		super();
	}
	public ClassMapper(Class<T> type) {
		super();
		this.setType(type);
	}
	public ClassMapper(String className) {
		super();
		try {
			this.setType((Class<T>) Class.forName(className));
		} catch (ClassNotFoundException e) {
			// e.printStackTrace();
		}
	}
	public ClassMapper setType(Class<T> type) {
		this.type = ObjectUtil.getObjectType(type);
		return this;
	}
	
	public Class<T> getType() {
		return type;
	}
		public ClassMapper setConstructor(InstanceCreator<T> constructor) {
		this.constructor = constructor;
		return this;
	}
	public ClassMapper setDefaultValue(T defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}
	public ClassMapper setIgnore(boolean ignore) {
		this.ignore = ignore;
		return this;
	}
	public ClassMapper setSerializer(Function serializer) {
		this.serializer = serializer;
		return this;
	}
	public ClassMapper setDeserializer(Function deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public ClassMapper setSerializer(Integer2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public ClassMapper setDeserializer(Json2IntegerFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public ClassMapper setSerializer(Long2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public ClassMapper setDeserializer(Json2LongFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public ClassMapper setSerializer(Double2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public ClassMapper setDeserializer(Json2DoubleFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public ClassMapper setSerializer(Short2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public ClassMapper setDeserializer(Json2ShortFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public ClassMapper setSerializer(Float2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public ClassMapper setDeserializer(Json2FloatFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public ClassMapper setSerializer(BigDecimal2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public ClassMapper setDeserializer(Json2BigDecimalFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public ClassMapper setSerializer(BigInteger2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public ClassMapper setDeserializer(Json2BigIntegerFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public ClassMapper setSerializer(Character2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public ClassMapper setDeserializer(Json2CharacterFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public ClassMapper setSerializer(Byte2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public ClassMapper setDeserializer(Json2ByteFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public ClassMapper setSerializer(Boolean2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public ClassMapper setDeserializer(Json2BooleanFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public ClassMapper setSerializer(Date2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public ClassMapper setDeserializer(Json2DateFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public ClassMapper setSerializer(Date2LongFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public ClassMapper setDeserializer(Long2DateFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public ClassMapper setSerializer(Enum2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public ClassMapper setDeserializer(Json2EnumFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public ClassMapper setSerializer(Collection2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public ClassMapper setDeserializer(Json2CollectionFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public ClassMapper setSerializer(Map2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public ClassMapper setDeserializer(Json2MapFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public ClassMapper setSerializer(Array2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public ClassMapper setDeserializer(Json2ArrayFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public ClassMapper setSerializer(AtomicInteger2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public ClassMapper setDeserializer(Json2AtomicIntegerFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public ClassMapper setSerializer(AtomicLong2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public ClassMapper setDeserializer(Json2AtomicLongFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public ClassMapper setSerializer(DataMapper2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public ClassMapper setDeserializer(Json2DataMapperFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public ClassMapper setSerializer(String2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public ClassMapper setDeserializer(Json2StringFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	
	public ClassMapper setUseField(Boolean useField) {
		this.useField = useField;
		return this;
	}
	public ClassMapper setUseAttribute(Boolean useAttribute) {
		this.useAttribute = useAttribute;
		return this;
	}
	public ClassMapper setIncludeFieldsWithModifiers(
			Set<MODIFIER> includeFieldsWithModifiers) {
		this.includeFieldsWithModifiers = includeFieldsWithModifiers;
		return this;
	}
	public ClassMapper setSimpleDateFormat(String simpleDateFormat) {
		this.simpleDateFormat = simpleDateFormat;
		return this;
	}
	public ClassMapper setOrderByKeyAndProperties(Boolean orderByKeyAndProperties) {
		this.orderByKeyAndProperties = orderByKeyAndProperties;
		return this;
	}
	public ClassMapper setPropertyOrders(String[] propertyOrders) {
		this.propertyOrders = propertyOrders;
		return this;
	}
	public ClassMapper setIncludeClassTypeInJson(Boolean includeClassTypeInJson) {
		this.includeClassTypeInJson = includeClassTypeInJson;
		return this;
	}
	public ClassMapper setIgnoreVersionsAfter(Double ignoreVersionsAfter) {
		this.ignoreVersionsAfter = ignoreVersionsAfter;
		return this;
	}
	public ClassMapper setIgnoreFieldsWithAnnotations(
			Set<Class> ignoreFieldsWithAnnotations) {
		this.ignoreFieldsWithAnnotations = ignoreFieldsWithAnnotations;
		return this;
	}
	public ClassMapper setJsonIgnoreProperties(Set<String> jsonIgnoreProperties) {
		this.jsonIgnoreProperties = jsonIgnoreProperties;
		return this;
	}
	public ClassMapper setDefaultType(JSON_INCLUDE defaultType) {
		this.defaultType = defaultType;
		return this;
	}
	
	public ClassMapper setScale(Integer scale) {
		this.scale = scale;
		return this;
	}
	public ClassMapper setMin(Long min) {
		this.min = min;
		return this;
	}
	public ClassMapper setMax(Long max) {
		this.max = max;
		return this;
	}
	public ClassMapper setEnumType(EnumType enumType) {
		this.enumType = enumType;
		return this;
	}
	public ClassMapper setDate2Long(Boolean date2Long) {
		this.date2Long = date2Long;
		return this;
	}
	
	public boolean ignore() {
		if (ignore != null && ignore) {
			return true;
		}
		return false;
	}
	
	public ClassMapper setLength(Integer length) {
		this.length = length;
		return this;
	}
	
	/*
	 * the number of digits of decimal
	 */
	public ClassMapper setPrecision(Integer precision) {
		this.precision = precision;
		return this;
	}
}