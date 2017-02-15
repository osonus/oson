package ca.oson.json;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.function.Function;

import javax.persistence.EnumType;

import ca.oson.json.function.*;
import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.util.ObjectUtil;

/*
 * configuration options for a specific field
 */
public class FieldMapper<T, E> {
	// how to match field name, and its enclosing class
	// how to ignore its value, in case either java or json value is null
	
	/*
	 * Java property name
	 */
	public String java;
	
	/*
	 * Corresponding json name
	 */
	public String json;
	
	/*
	 * If present, it means the type of the enclosing class.
	 * otherwise, this mapper can be used by all properties with the same field name
	 */
	private Class<T> type;
	
	/*
	 * Not really used here, just for reference.
	 * Actual data are kept in another class called FieldData
	 */
	private Class<E> returnType;


	/*
	 * This field/attribute will be ignored if true
	 */
	public Boolean ignore = null;

	/*
	 * use field to get or set value of a Java object during serializing or deserializing
	 */
	public Boolean useField = null;
	
	/*
	 * Use attribute or getter and setter method of a Java object
	 */
	public Boolean useAttribute = null;

	/*
	 * Lambda expression style single method interface
	 * used to provide custom serializing mechanism
	 */
	public Function serializer;
	
	/*
	 * function for user to deserialize a Json data into Java property
	 */
	public Function deserializer;
	
	/*
	 * In case the field is an enumType, define its type to serialize
	 * Can be either int, or String format
	 */
	public EnumType enumType = null;
	
	/*
	 * Is this property is required, or not nullable
	 */
	public Boolean required = null;
	
	/*
	 * length of a string property
	 */
	public Integer length = null;
	
	/*
	 * the number of digits after decimal point in a float, double, or big decimal field
	 */
	public Integer scale = null;
	
	/*
	 * Non-zero leading digits
	 */
	public Integer precision = null;
	
	/*
	 * Minimum value of a property
	 */
	public Long min = null;
	
	/*
	 * Maximu value of a property
	 */
	public Long max = null;
	
	/*
	 * Default value of this property, in case it is required
	 * or defaultType is configured to be JSON_INCLUDE.DEFAULT
	 */
	public E defaultValue = null;
	
	/*
	 * How null or default values are handled in its serializing and deserializing process
	 */
	public JSON_INCLUDE defaultType = null;
	
	/*
	 *  serialize to double quotes, or not
	 */
	public Boolean jsonRawValue = null;
	
	
	/*
	 * If this value is true, the getter method of this property will return the Json data for the whole class.
	 * In a class, only one method returning a String value is allowed to set this value to true
	 */
	public Boolean jsonValue = null;
	
	/*
	 * If this value is true, then no attribute or key name will be used in Json output.
	 */
	public Boolean jsonNoName = null;

	/*
	 * method with this value set to true will get all properties not specified earlier.
	 * It will normally return a Map<String, Object>
	 */
	public Boolean jsonAnyGetter = null;

	/*
	 * method with this value set to true will set all properties not consumed earlier.
	 * It will normally store all the other data into a Map<String, Object>
	 */
	public Boolean jsonAnySetter = null;
	
	/*
	 * determine a date to be converted to long, instead of using date format to converted into a string
	 * This flag takes precedence over simpleDateFormat
	 */
	public Boolean date2Long = null;
	
	
	/*
	 * the version number since a member has been present
	 */
	public Double since = null;
	
	/*
	 * the version number until a member should be present.
	 */
	public Double until = null;
	

	/*
	 * property specific date formatter, in case it is Date type
	 */
	private DateFormat dateFormat = null;
	
	
	/*
	 * a private flag used to jackson
	 */
	private boolean processed = false;
	
	/*
	 * Determine is this field is used only for both deserializing
	 * Defaults to BOTH
	 * 
	 */
	private BOOLEAN deserializing = BOOLEAN.BOTH;
	
	
	boolean isValid() {
		if (java == null && json == null) {
			return false;
		}
		
		return true;
	}
	
	public boolean isProcessed() {
		return processed;
	}

	public void setProcessed(boolean processed) {
		this.processed = processed;
	}
	
	
	public FieldMapper(String java, String json, Class<T> type, Function serializer, Function deserializer) {
		this(java,json,type);
		this.serializer = serializer;
		this.deserializer = deserializer;
	}
	public FieldMapper(String name, Class<T> type, Function serializer, Function deserializer) {
		this(name,name,type);
		this.serializer = serializer;
		this.deserializer = deserializer;
	}
	
	public FieldMapper(String java, String json, Class<T> type) {
		this(java,json);
		this.setType(type);
	}
	public FieldMapper(String name, Class<T> type) {
		this(name,name);
		this.setType(type);
	}
	
	public FieldMapper(String java, String json) {
		super();
		this.java = java;
		this.json = json;
	}

	public FieldMapper(String name) {
		this(name,name);
	}
	
	public FieldMapper() {
		super();
	}

	public FieldMapper setJava(String java) {
		this.java = java;
		return this;
	}

	public Class<T> getType() {
		return type;
	}
	
	public FieldMapper setJson(String json) {
		this.json = json;
		return this;
	}

	public FieldMapper setType(Class<T> type) {
		this.type = ObjectUtil.getObjectType(type);
		return this;
	}

	public FieldMapper setIgnore(boolean ignore) {
		this.ignore = ignore;
		return this;
	}

	public FieldMapper setUseField(boolean useField) {
		this.useField = useField;
		return this;
	}

	public FieldMapper setUseAttribute(boolean useAttribute) {
		this.useAttribute = useAttribute;
		return this;
	}

	public FieldMapper setSerializer(Function serializer) {
		this.serializer = serializer;
		return this;
	}
	public FieldMapper setDeserializer(Function deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public FieldMapper setSerializer(Integer2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public FieldMapper setDeserializer(Json2IntegerFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public FieldMapper setSerializer(Long2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public FieldMapper setDeserializer(Json2LongFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public FieldMapper setSerializer(Double2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public FieldMapper setDeserializer(Json2DoubleFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public FieldMapper setSerializer(Short2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public FieldMapper setDeserializer(Json2ShortFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public FieldMapper setSerializer(Float2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public FieldMapper setDeserializer(Json2FloatFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public FieldMapper setSerializer(BigDecimal2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public FieldMapper setDeserializer(Json2BigDecimalFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public FieldMapper setSerializer(BigInteger2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public FieldMapper setDeserializer(Json2BigIntegerFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public FieldMapper setSerializer(Character2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public FieldMapper setDeserializer(Json2CharacterFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public FieldMapper setSerializer(Byte2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public FieldMapper setDeserializer(Json2ByteFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public FieldMapper setSerializer(Boolean2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public FieldMapper setDeserializer(Json2BooleanFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public FieldMapper setSerializer(Date2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public FieldMapper setDeserializer(Json2DateFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public FieldMapper setSerializer(Date2LongFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public FieldMapper setDeserializer(Long2DateFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public FieldMapper setSerializer(Enum2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public FieldMapper setDeserializer(Json2EnumFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public FieldMapper setSerializer(Collection2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public FieldMapper setDeserializer(Json2CollectionFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public FieldMapper setSerializer(Map2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public FieldMapper setDeserializer(Json2MapFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public FieldMapper setSerializer(Array2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public FieldMapper setDeserializer(Json2ArrayFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public FieldMapper setSerializer(AtomicInteger2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public FieldMapper setDeserializer(Json2AtomicIntegerFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public FieldMapper setSerializer(AtomicLong2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public FieldMapper setDeserializer(Json2AtomicLongFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public FieldMapper setSerializer(String2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public FieldMapper setDeserializer(Json2StringFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	public FieldMapper setSerializer(DataMapper2JsonFunction serializer) {
		this.serializer = serializer;
		return this;
	}
	public FieldMapper setDeserializer(Json2DataMapperFunction deserializer) {
		this.deserializer = deserializer;
		return this;
	}
	
	public FieldMapper setSimpleDateFormat(String simpleDateFormat) {
		if (simpleDateFormat != null) {
			setDateFormat(new SimpleDateFormat(simpleDateFormat));
		} else {
			setDateFormat(null);
		}
		return this;
	}
	public FieldMapper setDateFormat(int style) {
		this.setDateFormat(DateFormat.getDateInstance(style));

		return this;
	}
	public FieldMapper setDateFormat(int style, Locale locale) {
		this.setDateFormat(DateFormat.getDateInstance(style, locale));

		return this;
	}
	public FieldMapper setDateFormat(int dateStyle, int timeStyle) {
		this.setDateFormat(DateFormat.getDateTimeInstance(dateStyle, timeStyle));

		return this;
	}
	public FieldMapper setDateFormat(int dateStyle, int timeStyle, Locale locale) {
		this.setDateFormat(DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale));

		return this;
	}
	
	public FieldMapper setEnumType(EnumType enumType) {
		this.enumType = enumType;
		return this;
	}

	public FieldMapper setRequired(boolean required) {
		this.required = required;
		return this;
	}

	public FieldMapper setLength(Integer length) {
		this.length = length;
		return this;
	}

	public FieldMapper setScale(Integer scale) {
		this.scale = scale;
		return this;
	}

	public FieldMapper setMin(Long min) {
		this.min = min;
		return this;
	}

	public FieldMapper setMax(Long max) {
		this.max = max;
		return this;
	}

	public FieldMapper setDefaultValue(E defaultValue) {
		this.defaultValue = defaultValue;
		return this;
	}

	public FieldMapper setDefaultType(JSON_INCLUDE defaultType) {
		this.defaultType = defaultType;
		return this;
	}

	public FieldMapper setJsonRawValue(Boolean jsonRawValue) {
		this.jsonRawValue = jsonRawValue;
		return this;
	}

	public FieldMapper setJsonValue(Boolean jsonValue) {
		this.jsonValue = jsonValue;
		return this;
	}
	
	public Boolean getJsonNoName() {
		return jsonNoName;
	}

	public FieldMapper setJsonNoName(Boolean jsonNoName) {
		this.jsonNoName = jsonNoName;
		return this;
	}

	public FieldMapper setDate2Long(Boolean date2Long) {
		this.date2Long = date2Long;
		return this;
	}
	
	public FieldMapper setSince(Double since) {
		this.since = since;
		return this;
	}
	
	public FieldMapper setUntil(Double until) {
		this.until = until;
		return this;
	}
	
	/*
	 * the number of digits of decimal
	 */
	public FieldMapper setPrecision(Integer precision) {
		this.precision = precision;
		return this;
	}
	
	public boolean isJsonRawValue() {
		if (jsonRawValue != null && jsonRawValue) {
			return true;
		} else {
			return false;
		}
	}


	public DateFormat getDateFormat() {
		return dateFormat;
	}

	public FieldMapper setDateFormat(DateFormat dateFormat) {
		this.dateFormat = dateFormat;
		
		return this;
	}

	public BOOLEAN getDeserializing() {
		return deserializing;
	}
	
	public boolean isDeserializing() {
		return deserializing == BOOLEAN.BOTH || deserializing == BOOLEAN.TRUE;
	}
	public boolean isSerializing() {
		return deserializing == BOOLEAN.BOTH || deserializing == BOOLEAN.FALSE;
	}
	
	public void setDeserializing(BOOLEAN deserializing) {
		this.deserializing = deserializing;
	}
}
