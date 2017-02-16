/*******************************************************************************
 * Copyright (c) 2016- Oson.ca
 * @author	David Ruifang He
 * @email	osonus@gmail.com
 * 
 * All rights reserved.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * You may elect to redistribute this code, under the condition that you may not
 * modify this copyright header
 *******************************************************************************/
package ca.oson.json;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ca.oson.json.org.JSONArray;
import ca.oson.json.org.JSONException;
import ca.oson.json.org.JSONObject;

import ca.oson.json.function.*;
import ca.oson.json.util.*;


/**
 * Convert java object to and from Json String
 * 
 * @author	David Ruifang He
 * Date	June 15, 2016
 */
public class Oson {
	// used by annotations
	public static enum BOOLEAN {
		FALSE,
		TRUE,
		NONE, // the same as null
		BOTH; // the same as all
		
		public static Boolean valueOf(BOOLEAN bool) {
			switch (bool) {
				case FALSE: return false;
				case TRUE: return true;
				case NONE: return null;
				default: return null;
			}
		}
		
		public Boolean value() {
			return valueOf(this);
		}
	};

	// used by annotations
	public static enum ENUM_TYPE {
	    /** Persist enumerated type property or field as an integer */
	    ORDINAL,

	    /** Persist enumerated type property or field as a string */
	    STRING,
	    
		NONE; // the same as null
		
		public static EnumType valueOf(ENUM_TYPE en) {
			switch (en) {
				case ORDINAL: return EnumType.ORDINAL;
				case STRING: return EnumType.STRING;
				default: return null;
			}
		}
		
		public EnumType value() {
			return valueOf(this);
		}
	};
	
	public static enum FIELD_NAMING {
		FIELD, // someField_name: use field name of a class/object, original field name
		LOWER, // simple lower case
		UPPER, // simply upper case
		CAMELCASE, // someFieldName: convert underscore to camelcase, first leter lower case
		UPPER_CAMELCASE, // SomeFieldName: first letter upper case
		UNDERSCORE_CAMELCASE, // some_Field_Name
		UNDERSCORE_UPPER_CAMELCASE, // Some_Field_Name
		UNDERSCORE_LOWER, // some_field_name: convert camelcase to underscore, lower case
		UNDERSCORE_UPPER, // SOME_FIELD_NAME: convert camelcase to underscore, upper case
		SPACE_CAMELCASE, // some Field Name
		SPACE_UPPER_CAMELCASE, // Some Field Name
		SPACE_LOWER, // some field name
		SPACE_UPPER, // SOME FIELD NAME
		DASH_CAMELCASE, // some-Field-Name
		DASH_UPPER_CAMELCASE, // Some-Field-Name
		DASH_LOWER, // some-field-name
		DASH_UPPER // SOME-FIELD-NAME
	};

	public static enum JSON_INCLUDE {
		ALWAYS, NON_NULL, NON_EMPTY, NON_DEFAULT, DEFAULT, 
		NONE // means it has no value
	}
	
	public static enum MODIFIER {
		Public,
		Protected,
		Package, // Package Private, no modifier
		Private,
		Abstract,
		Final,
		Interface,
		Native,
		Static,
		Strict,
		Synchronized,
		Transient,
		Volatile,
		Synthetic,
		All,
		None
	}


	public static class FieldData<T, E, R> implements Cloneable {
		// enclosing object, such as a field or component in a enclosing class
		public T enclosingObj;
		private Class<T> enclosingtype = null;
		
		// initial default value
		public Object defaultValue = null;
		
		// object to return, might be created from the defaultValue
		public Class<R> returnType;
		public R returnObj = null;
		// private Class<E> componentType = null;
		
		// value coming in to be processed
		private Field field;
		transient public E valueToProcess;
		
		// specifically map data to process, mainly for deserializing json to class object
		//public Map<String, Object> mapToProcess;

		// keeps type information
		//public ComponentType componentType = null;
		public boolean json2Java = true;

		// extra field information
		transient private Method getter;
		transient private Method setter;
		
		transient private FieldMapper fieldMapper = null;
		public ClassMapper classMapper = null;
		
		// various configuration attributes
		Boolean required = false;
		JSON_INCLUDE defaultType = JSON_INCLUDE.NONE;
		
		EnumType enumType = null;
		Integer length = null; // Default: 255
		Integer precision = null;
		Integer scale = null; // Default: 0
		Long min = null; // default (int) 0;
		Long max = null; // default (int) 2147483647;

		public Boolean jsonRawValue = null;
		
		private int level = 0;
		private Set set;
		// for internal use only
		public boolean doubleQuote = false;
		transient private ComponentType componentType;
		transient public boolean isMapValue = false;
		
		public Integer getLength() {
			if (length == null && classMapper != null) {
				length = classMapper.length;
			}
			
			return length;
		}
		
		public void incrLevel() {
			this.level++;
		}
		public void descLevel() {
			this.level--;
		}
		
		//true if this set did not already contain the specified element
		public boolean goAhead(int hash) {
			return set.add(hash); // || level < 2;
		}
		
		public boolean isJsonRawValue() {
			if (level == 0 || (jsonRawValue != null && jsonRawValue)) {
				return true;
			} else {
				return false;
			}
		}
		
		public boolean required() {
			return (this.required != null && this.required);
		}
		
		public Class getEnclosingtype() {
			if (enclosingtype == null && enclosingObj != null) {
				enclosingtype = (Class<T>) enclosingObj.getClass();
			}
			
			return enclosingtype;
		}

		public FieldData(T enclosingObj, Field field, E valueToProcess,
				Class<R> returnType, boolean json2Java, FieldMapper fieldMapper) {
			this(enclosingObj, field, valueToProcess, returnType, json2Java, fieldMapper, 0, Collections.synchronizedSet(new HashSet()));
		}

		public FieldData(T enclosingObj, Field field, E valueToProcess,
				Class<R> returnType, boolean json2Java, FieldMapper fieldMapper, int level, Set set) {
			this.enclosingObj = enclosingObj; // enclosing object
			this.field = field;
			this.valueToProcess = valueToProcess;
			this.returnType = returnType;
			this.json2Java = json2Java;
			this.fieldMapper = fieldMapper;
			
			this.required = fieldMapper.required;
			this.defaultType = fieldMapper.defaultType;
			this.length = fieldMapper.length;
			this.precision = fieldMapper.precision;
			this.scale = fieldMapper.scale;
			this.min = fieldMapper.min;
			this.max = fieldMapper.max;
			this.defaultValue = fieldMapper.defaultValue;
			this.jsonRawValue = fieldMapper.jsonRawValue;
			
			this.level = level;
			this.set = set;
		}

		public FieldData(E valueToProcess, Class<R> returnType) {
			this.valueToProcess = valueToProcess;
			this.returnType = returnType;
			this.level = 0;
			this.set = Collections.synchronizedSet(new HashSet());
		}

		// deserialize2Object(Map<String, Object> mapToProcess, Class<T> valueType, T obj)
		public FieldData(E valueToProcess, Class<R> returnType, R returnObj, boolean json2Java) {	
			this.valueToProcess = valueToProcess;
			this.returnType = returnType;
			this.json2Java = json2Java;
			this.returnObj = returnObj;
			this.level = 0;
			this.set = Collections.synchronizedSet(new HashSet());
		}
		
		public FieldData(E valueToProcess, Class<R> returnType, boolean json2Java) {
			this.valueToProcess = valueToProcess;
			this.returnType = returnType;
			this.json2Java = json2Java;
			this.returnObj = returnObj;
			this.level = 0;
			this.set = Collections.synchronizedSet(new HashSet());
		}
		
		public FieldData(E valueToProcess, boolean json2Java) {
			this.valueToProcess = valueToProcess; // value to interpret
			this.json2Java = json2Java;
			this.level = 0;
			this.set = Collections.synchronizedSet(new HashSet());
		}

		public FieldData(E valueToProcess, Class<R> returnType, boolean json2Java, int level, Set set) {
			this.valueToProcess = valueToProcess;
			this.returnType = returnType;
			this.json2Java = json2Java;
			this.level = level;
			this.set = set;
		}
		
		private FieldData() {
		}
		
		public Class getEnclosingType() {
			if (enclosingtype != null) {
				return enclosingtype;
			}
			
			if (enclosingObj != null) {
				enclosingtype = (Class<T>) enclosingObj.getClass();
			}
			if (enclosingtype == null && classMapper != null) {
				enclosingtype = classMapper.getType();
			}
			if (enclosingtype == null && fieldMapper != null) {
				enclosingtype = fieldMapper.getType();
			}

			return enclosingtype;
		}

		
		public String getDefaultName() {
			if (fieldMapper != null) {
				if (fieldMapper.java != null) {
					return fieldMapper.java;
				} else {
					return fieldMapper.json;
				}
			}
			
			return null;
		}
		
		public Function getFunction() {
			if (this.json2Java) {
				return getDeserializer();
			} else {
				return getSerializer();
			}
		}
		
		public Function getDeserializer() {
			if (fieldMapper != null && fieldMapper.deserializer != null) {
				return fieldMapper.deserializer;
			}
			if (classMapper != null && classMapper.deserializer != null) {
				return classMapper.deserializer;
			}
			
			return null;
		}
		
		public Function getSerializer() {
			if (fieldMapper != null && fieldMapper.serializer != null) {
				return fieldMapper.serializer;
			}
			if (classMapper != null && classMapper.serializer != null) {
				return classMapper.serializer;
			}
			
			return null;
		}
		
		public Integer getScale() {
			if (scale == null && classMapper != null) {
				scale = classMapper.scale;
			}
			
			return scale;
		}
		
		public Integer getPrecision() {
			if (precision == null && classMapper != null) {
				precision = classMapper.precision;
			}
			
			return precision;
		}
		

		public Long getMin() {
			if (min == null && classMapper != null) {
				min = classMapper.min;
			}
			
			if (max != null && returnType != null) {
				if ((returnType == Character.class || returnType == char.class)) {
					if (min.intValue() < Character.MIN_CODE_POINT) {
						min = (long) Character.MIN_CODE_POINT;
					}
					
				} else if (Number.class.isAssignableFrom(returnType)
						|| returnType.isPrimitive()) {
					if (returnType == Short.class || returnType == short.class) {
						if (min.intValue() < Short.MIN_VALUE) {
							min = (long) Short.MIN_VALUE;
						}
						
					} else if (returnType == Byte.class || returnType == byte.class) {
						if (min.intValue() < Byte.MIN_VALUE) {
							min = (long) Byte.MIN_VALUE;
						}
					}
				}
			}
			
			return min;
		}
		public Long getMax() {
			if (max == null && classMapper != null) {
				max = classMapper.max;
			}
			
			
			if (max != null && returnType != null) {
				if ((returnType == Character.class || returnType == char.class)) {
					if (max.intValue() > Character.MAX_CODE_POINT) {
						max = (long) Character.MAX_CODE_POINT;
					}
					
				} else if (Number.class.isAssignableFrom(returnType)
						|| returnType.isPrimitive()) {
					if (returnType == Short.class || returnType == short.class) {
						if (max.intValue() > Short.MAX_VALUE) {
							max = (long) Short.MAX_VALUE;
						}
						
					} else if (returnType == Byte.class || returnType == byte.class) {
						if (max.intValue() > Byte.MAX_VALUE) {
							max = (long) Byte.MAX_VALUE;
						}
					}
				}
			}
			
			return max;
		}
		
		public Object getDefaultValue() {
			
			if (defaultValue != null) {
				return defaultValue;
			}

			if (enclosingObj != null) {
				if (getter == null) {
					if (field != null) {
						String name = field.getName();

						String getterName = "get" + StringUtil.capitalize(name);
						try {
							getter = enclosingObj.getClass().getMethod(getterName, null);

						} catch (NoSuchMethodException | IllegalArgumentException e) {
							// e.printStackTrace();
						}
					}
				}
				
				if (getter != null) {
					defaultValue = ObjectUtil.getMethodValue(enclosingObj, getter);
				}
			}
			
			if (defaultValue == null && classMapper != null) {
				defaultValue = classMapper.defaultValue;
			}

			return defaultValue;
		}
		
		public EnumType getEnumType() {
			if (enumType == null && classMapper != null) {
				enumType = classMapper.enumType;
			}
			
			return enumType;
		}
		
		private DateFormat dateFormat = null;
		public DateFormat getDateFormat() {
			if (dateFormat != null) {
				return dateFormat;
			}
			if (fieldMapper != null) {
				dateFormat = fieldMapper.getDateFormat();
			}
			if (dateFormat == null && classMapper != null) {
				dateFormat = classMapper.getDateFormat();
			}
			
			if (dateFormat == null) {
				dateFormat = new SimpleDateFormat(DefaultValue.simpleDateFormat());
			}
			
			return dateFormat;
		}
		
		public Boolean getDate2Long() {
			if (fieldMapper != null) {
				if (fieldMapper.date2Long != null) {
					return fieldMapper.date2Long;
				}
			}
			if (classMapper != null) {
				if (classMapper.date2Long != null) {
					return classMapper.date2Long;
				}
			}

			return null;
		}
		
		
		public Class getComponentType(String jsonClassType) {
			Class<E> componentType = (Class<E>) returnType.getComponentType();
			if (componentType == null) {
				if (componentType == null && valueToProcess != null && Collection.class.isAssignableFrom(valueToProcess.getClass())) {
					Collection<E> values = (Collection<E>) valueToProcess;
					
					componentType = CollectionArrayTypeGuesser.guessElementType(values, (Class<Collection<E>>)values.getClass(), jsonClassType);
				}
				
				if (componentType == null && valueToProcess != null && Map.class.isAssignableFrom(valueToProcess.getClass())) {
					Map<String, Object> values = (Map)valueToProcess;
					componentType = CollectionArrayTypeGuesser.guessElementType(values, returnType, jsonClassType);
				}
				
				if (componentType == null && valueToProcess != null && returnType.isArray()) {
					E[] array = (E[])valueToProcess;
					componentType = CollectionArrayTypeGuesser.guessElementType(array, returnType);
				}
				
				if (componentType == null) componentType = (Class<E>) Object.class;
				
				if (componentType.isPrimitive()) {
					componentType = ObjectUtil.getObjectType(componentType);
				}
			}
			
			return componentType;
		}
		
		protected FieldData clone() throws CloneNotSupportedException {
	        try{
	        	FieldData clone = new FieldData();
	        	CopyObjects.copy(this, clone, true);
	        	clone.valueToProcess = this.valueToProcess;
	        	
	            return clone;
	        }catch(Exception e){
	        	return null;
	        }
	    }
	}

	// holding all states inside these 3 objects
	private Options options = new Options();
	
	private enum METHOD {
		SET(0), GET(1), OTHER(2);
		private int value;
		 
		private METHOD(int value) {
			this.value = value;
		}
	}

	// there are caching mechanism, should be safe to be shared across
	private static Map<Class, Field[]> cachedFields = new ConcurrentHashMap<>();
	private static Map<String, Map <String, Method>[]> cachedMethods = new ConcurrentHashMap<>();
	private static Map<Class, ComponentType> cachedComponentTypes = new ConcurrentHashMap<>();
	private Class masterClass = null;

	////////////////////////////////////////////////////////////////////////////////
	// END OF variables and class definition
	////////////////////////////////////////////////////////////////////////////////


	public Oson() {
		ObjectUtil.getJSONObject(null);
	}

	public Oson(JSONObject json) {
		this();
		configure(json);
	}

	public Oson(String json) {
		this();
		configure(json);
	}

	public Oson(Object[] array) {
		this();
		configure(array);
	}

	public Oson(Map<String, Object> map) {
		this();
		configure(map);
	}

	public Oson(Options options) {
		this();
		configure(options);
	}

	public Oson configure(JSONObject json) {
		return configure(json.toString());
	}

	public Oson configure(String json) {
		Options options = this.fromJson(json, Options.class);

		return configure(options);
	}

	public Oson configure(Object[] array) {
		Map<String, Object> map = ArrayToJsonMap.array2Map(array);

		return configure(map);
	}

	public Oson configure(Map<String, Object> map) {
		Options options = this.deserialize2Object(map, Options.class, null);

		return configure(options);
	}

	public Oson configure(Options options) {
		if (options != null) {
			if (this.options == null) {
				this.options = new Options();
			}
			CopyObjects.copy(options, this.options, false);

			reset();
		}
		return this;
	}

	private FIELD_NAMING getFieldNaming() {
		return options.getFieldNaming();
	}

	public Oson setFieldNaming(FIELD_NAMING fieldNaming) {
		if (fieldNaming != null) {
			options.setFieldNaming(fieldNaming);
			reset();
		}

		return this;
	}

	private JSON_INCLUDE getDefaultType() {
		return options.getDefaultType();
	}

	public Oson setDefaultType(JSON_INCLUDE defaultType) {
		if (defaultType != null) {
			options.setDefaultType(defaultType);
			reset();
		}

		return this;
	}
	
	private String getSimpleDateFormat() {
		return options.getSimpleDateFormat();
	}

	private DateFormat getDateFormat() {
		return options.getDateFormat();
	}
	public <T> Oson setDateFormat(Class<T> type, String simpleDateFormat) {
		cMap(type).setSimpleDateFormat(simpleDateFormat);
		reset();

		return this;
	}
	public Oson setDateFormat(String simpleDateFormat) {
		if (simpleDateFormat != null) {
			options.setSimpleDateFormat(simpleDateFormat);
			reset();
		}

		return this;
	}
	public Oson setDateFormat(DateFormat dateFormat) {
		options.setDateFormat(dateFormat);
		reset();

		return this;
	}
	public Oson setDateFormat(int style) {
		options.setDateFormat(DateFormat.getDateInstance(style));
		reset();

		return this;
	}
	public <T> Oson setDateFormat(Class<T> type, int style) {
		cMap(type).setDateFormat(DateFormat.getDateInstance(style));
		reset();

		return this;
	}
	
	public Oson setDateFormat(int style, Locale locale) {
		options.setDateFormat(DateFormat.getDateInstance(style, locale));
		reset();

		return this;
	}
	public <T> Oson setDateFormat(Class<T> type, int style, Locale locale) {
		cMap(type).setDateFormat(DateFormat.getDateInstance(style, locale));
		reset();

		return this;
	}
	public Oson setDateFormat(int dateStyle, int timeStyle) {
		options.setDateFormat(DateFormat.getDateTimeInstance(dateStyle, timeStyle));
		reset();

		return this;
	}
	public <T> Oson setDateFormat(Class<T> type, int dateStyle, int timeStyle) {
		cMap(type).setDateFormat(DateFormat.getDateTimeInstance(dateStyle, timeStyle));
		reset();

		return this;
	}
	public Oson setDateFormat(int dateStyle, int timeStyle, Locale locale) {
		options.setDateFormat(DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale));
		reset();

		return this;
	}
	public <T> Oson setDateFormat(Class<T> type, int dateStyle, int timeStyle, Locale locale) {
		cMap(type).setDateFormat(DateFormat.getDateTimeInstance(dateStyle, timeStyle, locale));
		reset();

		return this;
	}
	
	
	private boolean getPrettyPrinting() {
		return options.getPrettyPrinting();
	}
	public Oson pretty() {
		return pretty(true);
	}
	public Oson pretty(Boolean prettyPrinting) {
		return prettyPrinting(prettyPrinting);
	}
	public Oson prettyPrinting(Boolean prettyPrinting) {
		if (prettyPrinting != null) {
			options.prettyPrinting(prettyPrinting);
			reset();
		}

		return this;
	}

	private String getPrettySpace() {
		if (getPrettyPrinting() && getIndentation() > 0) {
			return String.valueOf(StringUtil.SPACE);
		}

		return "";
	}
	private String getPrettyIndentation(int level) {
		if (options.getPrettyPrinting() && getIndentation() > 0) {
			return StringUtil.repeatSpace(level * getIndentation());
		}

		return "";
	}
	private String getPrettyIndentationln(int level) {
		if (options.getPrettyPrinting() && getIndentation() > 0) {
			return "\n" + StringUtil.repeatSpace(level * getIndentation());
		}

		return "";
	}

	private boolean getAnnotationSupport() {
		return options.getAnnotationSupport();
	}

	public Oson setAnnotationSupport(boolean annotationSupport) {
		options.setAnnotationSupport(annotationSupport);
		reset();

		return this;
	}

	private int getPrettyIndentation() {
		if (!getPrettyPrinting()) {
			return 0;
		}
		return options.getIndentation();
	}
	
	private int getIndentation() {
		return options.getIndentation();
	}

	/**
	 * This method is used to configure the Json output during serialization.
	 * A default value of 2 is used if not specified otherwise for pretty printing.
	 * @param indentation The indented spaces, with a value range of 0 to 100
	 * @return Oson object, allowing this method to be chained up as a builder method
	 */
	public Oson setIndentation(int indentation) {
		if (indentation != getIndentation() && indentation >= 0) {
			options.setIndentation(indentation);
			reset();
		}

		return this;
	}
	
	private Set<Class> getIgnoreFieldsWithAnnotations() {
		return options.getIgnoreFieldsWithAnnotations();
	}

	public Oson ignoreFieldsWithAnnotations(
			Set<Class> ignoreFieldsWithAnnotations) {
		options.ignoreFieldsWithAnnotations(ignoreFieldsWithAnnotations);

		return this;
	}

	public Oson ignoreFieldsWithAnnotations(
			Class[] ignoreFieldsWithAnnotations) {
		options.ignoreFieldsWithAnnotations(ignoreFieldsWithAnnotations);

		return this;
	}
	
	public Oson ignoreFieldsWithAnnotation(Class ignoreFieldsWithAnnotation) {
		options.ignoreFieldsWithAnnotation(ignoreFieldsWithAnnotation);

		return this;
	}

	private Set<Class> getIgnoreClassWithAnnotations() {
		return options.getIgnoreClassWithAnnotations();
	}

	public Oson ignoreClassWithAnnotations(
			Set<Class> ignoreClassWithAnnotations) {
		options.ignoreClassWithAnnotations(ignoreClassWithAnnotations);

		return this;
	}

	public Oson ignoreClassWithAnnotations(
			Class[] ignoreClassWithAnnotations) {
		options.ignoreClassWithAnnotations(ignoreClassWithAnnotations);

		return this;
	}
	
	public Oson ignoreClassWithAnnotation(Class ignoreClassWithAnnotation) {
		options.ignoreClassWithAnnotation(ignoreClassWithAnnotation);

		return this;
	}
	
	
	private Set<MODIFIER> getIncludeFieldsWithModifiers() {
		return options.getIncludeFieldsWithModifiers();
	}

	public Oson includeFieldsWithModifiers(
			Set<MODIFIER> includeFieldsWithModifiers) {
		options.includeFieldsWithModifiers(includeFieldsWithModifiers);

		return this;
	}
	
	public Oson includeFieldsWithModifiers(
			MODIFIER[] includeFieldsWithModifiers) {
		options.includeFieldsWithModifiers(includeFieldsWithModifiers);

		return this;
	}
	
	public Oson includeFieldsWithModifier(
			MODIFIER includeFieldsWithModifier) {
		options.includeFieldsWithModifiers(includeFieldsWithModifier);

		return this;
	}
	
	private String java2Json(String name) {
		return options.java2Json(name);
	}

	private String json2Java(String name) {
		return options.json2Java(name);
	}

	private String java2Json(Field field) {
		return options.java2Json(field);
	}

	private String json2Java(Field field) {
		return options.json2Java(field);
	}

	private Function getSerializer(String defaultName, Class valueType, Class enclosingType) {
		return options.getSerializer(defaultName, valueType, enclosingType);
	}
	private Function getSerializer(Class valueType) {
		return options.getSerializer(valueType);
	}
	private Function getDeserializer(String defaultName, Class valueType, Class enclosingType) {
		return options.getDeserializer(defaultName, valueType, enclosingType);
	}
	private Function getDeserializer(Class valueType) {
		return options.getDeserializer(valueType);
	}
	
	private Object getDefaultValue(Class valueType) {
		Object value = options.getDefaultValue(valueType);
		
		if (value == null) {
			value = DefaultValue.getSystemDefault(valueType);
		}
		
		return value;
	}
	
	private boolean ignoreClass(Class valueType) {
		Set<MODIFIER> set = getIncludeFieldsWithModifiers();
		if (valueType.isSynthetic() && (set == null || !set.contains(MODIFIER.Synthetic))) {
			return true;
		}
		
		Map<Class, ClassMapper> classMappers = getClassMappers();
		if (classMappers != null) {
			ClassMapper mapper = classMappers.get(valueType);
			
			if (mapper != null) {
				return mapper.ignore;
			}
		}
		
		return false;
	}
	
	
	private boolean ignoreClass(Annotation annotation) {
		Set<Class> annotations = getIgnoreClassWithAnnotations();
		if (annotations == null || annotation == null) {
			return false;
		}
		
		for (Class ann: annotations) {
			if (annotation.getClass() == ann || ann.isAssignableFrom(annotation.getClass())) {
				return true;
			}
		}
		
		return false;
	}
	

	private boolean ignoreField(Annotation annotation, Set<Class> annotations) {
		// Set<Class> annotations = getIgnoreFieldsWithAnnotations();
		if (annotations == null || annotation == null) {
			return false;
		}
		
		for (Class ann: annotations) {
			if (ann != null && (annotation.getClass() == ann || ann.isAssignableFrom(annotation.getClass()))) {
				return true;
			}
		}
		
		return ignoreField(annotation.getClass(), annotations);
	}

	private boolean ignoreField(Class annotationClass, Set<Class> annotations) {
		if (annotations == null || annotationClass == null) {
			return false;
		}
		
		for (Class ann: annotations) {
			if (ann != null && (annotationClass == ann || ann.isAssignableFrom(annotationClass))) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean ignoreModifiers(int modifiers, Set<MODIFIER> includeFieldsWithModifiers) {
		//Set<MODIFIER> includeFieldsWithModifiers = getIncludeFieldsWithModifiers();
		if (includeFieldsWithModifiers == null || includeFieldsWithModifiers.size() == 0) {
			// by default, transient and volatile are ignored
			// unless you specify otherwise, by using MODIFIER.Transient enum, or all
			if (Modifier.isTransient(modifiers)) {
				return true;
			}
			if (Modifier.isVolatile(modifiers)) {
				return true;
			}
			
			return false;
		}
		
		if (includeFieldsWithModifiers.contains(MODIFIER.All)) {
			return false;
		}
		
		for (MODIFIER modifier: includeFieldsWithModifiers) {
			switch (modifier) {
			case Abstract:
				if (Modifier.isAbstract(modifiers)) {
					return false;
				}
				break;
			case Final:
				if (Modifier.isFinal(modifiers)) {
					return false;
				}
				break;
			case Interface:
				if (Modifier.isInterface(modifiers)) {
					return false;
				}
				break;
			case Native:
				if (Modifier.isNative(modifiers)) {
					return false;
				}
				break;
			case Private:
				if (Modifier.isPrivate(modifiers)) {
					return false;
				}
				break;
			case Protected:
				if (Modifier.isProtected(modifiers)) {
					return false;
				}
				break;
			case Public:
				if (Modifier.isPublic(modifiers)) {
					return false;
				}
				break;
			case Package:
				if (ObjectUtil.isPackage(modifiers)) {
					return false;
				}
				break;
			case Static:
				if (Modifier.isStatic(modifiers)) {
					return false;
				}
				break;
			case Strict:
				if (Modifier.isStrict(modifiers)) {
					return false;
				}
				break;
			case Synchronized:
				if (Modifier.isSynchronized(modifiers)) {
					return false;
				}
				break;
			case Transient:
				if (Modifier.isTransient(modifiers)) {
					return false;
				}
				break;
			case Volatile:
				if (Modifier.isVolatile(modifiers)) {
					return false;
				}
				break;
			}
		}
		
		return true;
	}
	
	private Boolean getOrderArrayAndList() {
		return options.getOrderArrayAndList();
	}


	public Oson orderArrayAndList(Boolean orderArrayAndList) {
		if (orderArrayAndList != null) {
			options.setOrderArrayAndList(orderArrayAndList);
		}

		return this;
	}
	
	
	private Boolean getOrderByKeyAndProperties() {
		return options.getOrderByKeyAndProperties();
	}

	public Oson orderByKeyAndProperties(Boolean orderByKeyAndProperties) {
		if (orderByKeyAndProperties != null) {
			options.setOrderByKeyAndProperties(orderByKeyAndProperties);
			reset();
		}

		return this;
	}
	
	public Oson sort(boolean order) {
		orderArrayAndList(order);
		return orderByKeyAndProperties(order);
	}
	public Oson sort() {
		return sort(true);
	}
	
	private Boolean getIncludeClassTypeInJson() {
		return options.getIncludeClassTypeInJson();
	}

	public Oson includeClassTypeInJson(Boolean includeClassTypeInJson) {
		if (includeClassTypeInJson != null) {
			options.setIncludeClassTypeInJson(includeClassTypeInJson);
			reset();
		}

		return this;
	}

	private String getJsonClassType() {
		return options.getJsonClassType();
	}

	public Oson setJsonClassType(String jsonClassType) {
		if (jsonClassType != null) {
			options.setJsonClassType(jsonClassType);
			reset();
		}

		return this;
	}

	private Double getVersion() {
		return options.getVersion();
	}

	public Oson setVersion(Double version) {
		options.setVersion(version);
		reset();

		return this;
	}
	
	
	private Map<Class, ClassMapper> getClassMappers() {
		return options.getClassMappers();
	}

	private ClassMapper getClassMapper(Class valueType) {
		if (valueType == null) {
			return null;
		}
		
		// change primitive type to object type
		valueType = ObjectUtil.getObjectType(valueType);
		
		Map<Class, ClassMapper> mappers = getClassMappers();
		
		if (mappers == null) {
			return null;
		}
		
		ClassMapper classMapper = mappers.get(valueType);
		
		if (classMapper != null) {
			return classMapper;
		}
		
		for (Class cls: mappers.keySet()) {
			if (cls.isAssignableFrom(valueType)) {
				return mappers.get(cls);
			}
		}
		
		return null;
	}
	
	/*
	 * if a specific attribute is null, set it to the global setting
	 */
	private ClassMapper globalize(ClassMapper mapper) {
		// a little bit convenient
		// && Date.class.isAssignableFrom(valueType)
		if (mapper.getDateFormat() == null) {
			mapper.setDateFormat(getDateFormat());
		}
		
		if (mapper.includeClassTypeInJson == null) {
			mapper.includeClassTypeInJson =  getIncludeClassTypeInJson();
		}
		
		if (mapper.orderByKeyAndProperties == null) {
			mapper.orderByKeyAndProperties =  getOrderByKeyAndProperties();
		}
		
		if (mapper.orderArrayAndList == null) {
			mapper.orderArrayAndList =  getOrderArrayAndList();
		}
		
		if (mapper.useAttribute == null) {
			mapper.useAttribute =  isUseAttribute();
		}
		
		if (mapper.useField == null) {
			mapper.useField =  isUseField();
		}
		
		if (mapper.ignoreFieldsWithAnnotations == null) {
			mapper.ignoreFieldsWithAnnotations =  getIgnoreFieldsWithAnnotations();
		}
		
//		if (mapper.since == null) {
//			mapper.since = getVersion();
//		}
		
		if (mapper.includeFieldsWithModifiers == null) {
			mapper.includeFieldsWithModifiers =  getIncludeFieldsWithModifiers();
		}
		
		if (mapper.defaultType == null) {
			mapper.defaultType = getDefaultType();
		}
		
		if (mapper.enumType == null) {
			mapper.enumType = getEnumType();
		}
		
		if (mapper.date2Long == null) {
			mapper.date2Long = getDate2Long();
		}
		
		if (mapper.length == null) {
			mapper.length =  getLength();
		}
		
		if (mapper.precision == null) {
			mapper.precision = getPrecision();
		}
		
		if (mapper.scale == null) {
			mapper.scale = getScale();
		}
		
		if (mapper.min == null) {
			mapper.min = getMin();
		}
		
		if (mapper.max == null) {
			mapper.max = getMax();
		}
		
		if (mapper.isToStringAsSerializer() == null) {
			mapper.setToStringAsSerializer(isToStringAsSerializer());
		}
		
		if (mapper.getEscapeHtml() == null) {
			mapper.setEscapeHtml(isEscapeHtml());
		}
		
		return mapper;
	}
	
	private ClassMapper getGlobalizedClassMapper(Class valueType) {
		ClassMapper mapper = getClassMapper(valueType);

		if (mapper == null) {
			mapper = new ClassMapper();
		}
		
		return globalize(mapper);
	}

	
	public Oson setClassMappers(Map<Class, ClassMapper> classMappers) {
		options.setClassMappers(classMappers);
		reset();

		return this;
	}
	
	
	public Oson setClassMappers(ClassMapper[] classMappers) {
		options.setClassMappers(classMappers);
		reset();

		return this;
	}
	public Oson setClassMappers(List<ClassMapper> classMappers) {
		options.setClassMappers(classMappers);
		reset();

		return this;
	}
	public Oson setClassMappers(ClassMapper classMapper) {
		options.setClassMappers(classMapper);
		reset();

		return this;
	}
	public Oson setClassMappers(Class type, ClassMapper classMapper) {
		options.setClassMappers(type, classMapper);
		reset();

		return this;
	}
	
	private Set<FieldMapper> getFieldMappers() {
		return options.getFieldMappers();
	}

	private FieldMapper classifyFieldMapper(FieldMapper fieldMapper, ClassMapper classMapper) {
		if (classMapper == null) {
			return fieldMapper;
		}
		
		// classify it now
		if (fieldMapper.useAttribute == null) {
			fieldMapper.useAttribute = classMapper.useAttribute;
		}
		
		if (fieldMapper.useField == null) {
			fieldMapper.useField = classMapper.useField;
		}

		if (fieldMapper.getDateFormat() == null) {
			fieldMapper.setDateFormat(classMapper.getDateFormat());
		}
		
		if (fieldMapper.defaultType == null) {
			fieldMapper.defaultType = classMapper.defaultType;
		}
		
		if (fieldMapper.enumType == null) {
			fieldMapper.enumType = classMapper.enumType;
		}
		
		if (fieldMapper.date2Long == null) {
			fieldMapper.date2Long =  classMapper.date2Long;
		}
		
		if (fieldMapper.precision == null) {
			fieldMapper.precision =  classMapper.precision;
		}
		
		if (fieldMapper.scale == null) {
			fieldMapper.scale =  classMapper.scale;
		}
		
		if (fieldMapper.min == null) {
			fieldMapper.min =  classMapper.min;
		}
		
		if (fieldMapper.max == null) {
			fieldMapper.max =  classMapper.max;
		}
		
		if (fieldMapper.length == null) {
			fieldMapper.length =  classMapper.length;
		}
		
		if (fieldMapper.defaultValue == null) {
			fieldMapper.defaultValue =  classMapper.defaultValue;
		}
		
		if (fieldMapper.ignore == null) {
			fieldMapper.ignore =  classMapper.ignore;
		}
		
		return fieldMapper;
	}

	
	public Oson setFieldMappers(Set<FieldMapper> fieldMappers) {
		options.setFieldMappers(fieldMappers);
		reset();

		return this;
	}
	
	public Oson setFieldMappers(Collection<FieldMapper> fieldMappers) {
		options.setFieldMappers(fieldMappers);
		reset();

		return this;
	}
	
	public Oson setFieldMappers(FieldMapper[] fieldMappers) {
		options.setFieldMappers(fieldMappers);
		reset();

		return this;
	}
	
	public Oson setFieldMappers(FieldMapper fieldMapper) {
		options.setFieldMappers(fieldMapper);
		reset();

		return this;
	}
	
	
	public <T> Oson setFieldMappers(Class<T> type, BOOLEAN deserializing, String... javaJsons) {
		if (javaJsons.length < 2) {
			return this;
		}
		
		for (int i = 0; i + 1 < javaJsons.length; i++) {
			FieldMapper fieldMapper = new FieldMapper(javaJsons[i], javaJsons[++i], type);
			fieldMapper.setDeserializing(deserializing);
			options.setFieldMappers(fieldMapper);
		}
		
		reset();

		return this;
	}
	
	
	private FieldMapper getFieldMapper(String java, String json, Class type) {
		FieldMapper fieldMapper = options.getFieldMapper(java, json, type);
		if (fieldMapper == null) {
			fieldMapper = options.getFieldMapper(java, json, null);
		}
		
		return fieldMapper;
	}
	
	private Set<FieldMapper> getFieldMappers(Class type) {
		return options.getFieldMappers(type);
	}
	
	
	private Boolean isUseField() {
		return options.isUseField();
	}

	public Oson useField(Boolean useField) {
		options.setUseField(useField);
		reset();

		return this;
	}

	private Boolean isUseAttribute() {
		return options.isUseAttribute();
	}

	public Oson useAttribute(Boolean useAttribute) {
		options.setUseAttribute(useAttribute);
		reset();

		return this;
	}
	
	private int getLevel() {
		return options.getLevel();
	}

	/**
	 * This method is used to control the depth of attribute processing. 
	 * A Java class can hold attributes of Classes which can hold instance variables
	 * of other classes. This level setting will limit the depth of processing during
	 * serialization and de-serialization. A MAX_LEVEL constant is set to 100, so no more
	 * than 100 levels of depth is allowed. This can certain prevents endless processing
	 * loop in case other measures fail.
	 * @param level The depth of Json-Java processing, with a value range of 0 to 100
	 * @return Oson object, allowing this method to be chained up as a builder method
	 */
	public Oson setLevel(int level) {
		options.setLevel(level);
		
		return this;
	}
	
	private EnumType getEnumType() {
		return options.getEnumType();
	}

	public Oson setEnumType(EnumType enumType) {
		options.setEnumType(enumType);
		
		return this;
	}
	
	private Boolean getDate2Long() {
		return options.getDate2Long();
	}

	public Oson setDate2Long(Boolean date2Long) {
		options.setDate2Long(date2Long);
		
		return this;
	}
	
	private RoundingMode getRoundingMode() {
		return options.getRoundingMode();
	}

	public Oson setRoundingMode(RoundingMode roundingMode) {
		options.setRoundingMode(roundingMode);
		
		return this;
	}
	
	private Integer getLength() {
		return options.getLength();
	}

	public Oson setLength(Integer length) {
		options.setLength(length);

		return this;
	}

	private Integer getPrecision() {
		return options.getPrecision();
	}

	public Oson setPrecision(Integer precision) {
		options.setPrecision(precision);

		return this;
	}

	private Integer getScale() {
		return options.getScale();
	}

	public Oson setScale(Integer scale) {
		options.setScale(scale);

		return this;
	}
	public <T> Oson setScale(Class<T> type, Integer scale) {
		cMap(type).setScale(scale);

		return this;
	}
	
	
	public boolean isAppendingFloatingZero() {
		return options.isAppendingFloatingZero();
	}

	public Oson setAppendingFloatingZero(boolean appendingFloatingZero) {
		options.setAppendingFloatingZero(appendingFloatingZero);

		return this;
	}
	

	private Long getMin() {
		return options.getMin();
	}

	public Oson setMin(Long min) {
		options.setMin(min);

		return this;
	}
	
	private Long getMax() {
		return options.getMax();
	}

	public Oson setMax(Long max) {
		options.setMax(max);

		return this;
	}
	
	private boolean getSetGetOnly() {
		return options.getSetGetOnly();
	}

	public Oson setGetOnly(boolean setGetOnly) {
		options.setSetGetOnly(setGetOnly);

		return this;
	}
	public Oson setGetOnly() {
		return setGetOnly(true);
	}
	
	private boolean isInheritMapping() {
		return options.isInheritMapping();
	}

	public Oson setInheritMapping(boolean inheritMapping) {
		options.setInheritMapping(inheritMapping);

		return this;
	}
	
	private boolean isUseGsonExpose() {
		return options.isUseGsonExpose();
	}

	public Oson useGsonExpose(boolean useGsonExpose) {
		options.setUseGsonExpose(useGsonExpose);

		return this;
	}
	public Oson setCommentPatterns() {
		return setCommentPatterns(Options.defaultPatterns);
	}
	public Oson setCommentPatterns(String[] commentPatterns) {
		options.setCommentPatterns(commentPatterns);

		return this;
	}
	
	private Pattern[] getPatterns() {
		return options.getPatterns();
	}
	
	
	public Oson excludeFieldsWithModifiers(int... modifiers) {
		options.excludeFieldsWithModifiers(modifiers);

		return this;
	}
	
	
	private boolean isMap2ListStyle() {
		return options.isMap2ListStyle();
	}


	public Oson setMap2ListStyle(boolean map2ListStyle) {
		options.setMap2ListStyle(map2ListStyle);

		return this;
	}
	
	private boolean isValueOnly() {
		return options.isValueOnly();
	}

	public Oson setValueOnly(boolean valueOnly) {
		options.setValueOnly(valueOnly);

		return this;
	}
	
	
	/////////////////////////////////////////////////////////////////////////////////
	// start to set up class mapper
	
	private <T> ClassMapper cMap(Class<T> type) {
		return this.options.getClassMappers(type);
	}
	
	public <T> Oson setDefaultValue(Class<T> type, T defaultValue) {
		cMap(type).setDefaultValue(defaultValue);

		return this;
	}
	
	public <T> Oson setIgnore(Class<T> type, boolean ignore) {
		cMap(type).setIgnore(ignore);

		return this;
	}
	
	public <T> Oson setSerializer(Class<T> type, Function serializer) {
		cMap(type).setSerializer(serializer);

		return this;
	}
	
	public <T> Oson setDeserializer(Class<T> type, Function deserializer) {
		cMap(type).setDeserializer(deserializer);

		return this;
	}
	
	public <T> Oson setSerializer(Class<T> type, Integer2JsonFunction serializer) {
		cMap(type).setSerializer(serializer);

		return this;
	}
	public <T> Oson setDeserializer(Class<T> type, Json2IntegerFunction deserializer) {
		cMap(type).setDeserializer(deserializer);

		return this;
	}

	public <T> Oson setSerializer(Class<T> type, Long2JsonFunction serializer) {
		cMap(type).setSerializer(serializer);

		return this;
	}
	public <T> Oson setDeserializer(Class<T> type, Json2LongFunction deserializer) {
		cMap(type).setDeserializer(deserializer);

		return this;
	}
	
	public <T> Oson setSerializer(Class<T> type, Double2JsonFunction serializer) {
		cMap(type).setSerializer(serializer);

		return this;
	}
	public <T> Oson setDeserializer(Class<T> type, Json2DoubleFunction deserializer) {
		cMap(type).setDeserializer(deserializer);

		return this;
	}
	
	public <T> Oson setSerializer(Class<T> type, Short2JsonFunction serializer) {
		cMap(type).setSerializer(serializer);

		return this;
	}
	public <T> Oson setDeserializer(Class<T> type, Json2ShortFunction deserializer) {
		cMap(type).setDeserializer(deserializer);

		return this;
	}
	
	public <T> Oson setSerializer(Class<T> type, Float2JsonFunction serializer) {
		cMap(type).setSerializer(serializer);

		return this;
	}
	public <T> Oson setDeserializer(Class<T> type, Json2FloatFunction deserializer) {
		cMap(type).setDeserializer(deserializer);

		return this;
	}

	public <T> Oson setSerializer(Class<T> type, BigDecimal2JsonFunction serializer) {
		cMap(type).setSerializer(serializer);

		return this;
	}
	public <T> Oson setDeserializer(Class<T> type, Json2BigDecimalFunction deserializer) {
		cMap(type).setDeserializer(deserializer);

		return this;
	}
	
	public <T> Oson setSerializer(Class<T> type, BigInteger2JsonFunction serializer) {
		cMap(type).setSerializer(serializer);

		return this;
	}
	public <T> Oson setDeserializer(Class<T> type, Json2BigIntegerFunction deserializer) {
		cMap(type).setDeserializer(deserializer);

		return this;
	}
	
	public <T> Oson setSerializer(Class<T> type, Character2JsonFunction serializer) {
		cMap(type).setSerializer(serializer);

		return this;
	}
	public <T> Oson setDeserializer(Class<T> type, Json2CharacterFunction deserializer) {
		cMap(type).setDeserializer(deserializer);

		return this;
	}
	
	public <T> Oson setSerializer(Class<T> type, Byte2JsonFunction serializer) {
		cMap(type).setSerializer(serializer);

		return this;
	}
	public <T> Oson setDeserializer(Class<T> type, Json2ByteFunction deserializer) {
		cMap(type).setDeserializer(deserializer);

		return this;
	}
	
	public <T> Oson setSerializer(Class<T> type, Boolean2JsonFunction serializer) {
		cMap(type).setSerializer(serializer);

		return this;
	}
	public <T> Oson setDeserializer(Class<T> type, Json2BooleanFunction deserializer) {
		cMap(type).setDeserializer(deserializer);

		return this;
	}
	
	public <T> Oson setSerializer(Class<T> type, Date2JsonFunction serializer) {
		cMap(type).setSerializer(serializer);

		return this;
	}
	public <T> Oson setDeserializer(Class<T> type, Json2DateFunction deserializer) {
		cMap(type).setDeserializer(deserializer);

		return this;
	}
	
	public <T> Oson setSerializer(Class<T> type, Date2LongFunction serializer) {
		cMap(type).setSerializer(serializer);

		return this;
	}
	public <T> Oson setDeserializer(Class<T> type, Long2DateFunction deserializer) {
		cMap(type).setDeserializer(deserializer);

		return this;
	}
	
	public <T> Oson setSerializer(Class<T> type, Enum2JsonFunction serializer) {
		cMap(type).setSerializer(serializer);

		return this;
	}
	public <T> Oson setDeserializer(Class<T> type, Json2EnumFunction deserializer) {
		cMap(type).setDeserializer(deserializer);

		return this;
	}

	public <T> Oson setSerializer(Class<T> type, Collection2JsonFunction serializer) {
		cMap(type).setSerializer(serializer);

		return this;
	}
	public <T> Oson setDeserializer(Class<T> type, Json2CollectionFunction deserializer) {
		cMap(type).setDeserializer(deserializer);

		return this;
	}
	
	public <T> Oson setSerializer(Class<T> type, Map2JsonFunction serializer) {
		cMap(type).setSerializer(serializer);

		return this;
	}
	public <T> Oson setDeserializer(Class<T> type, Json2MapFunction deserializer) {
		cMap(type).setDeserializer(deserializer);

		return this;
	}
	
	public <T> Oson setSerializer(Class<T> type, Array2JsonFunction serializer) {
		cMap(type).setSerializer(serializer);

		return this;
	}
	public <T> Oson setDeserializer(Class<T> type, Json2ArrayFunction deserializer) {
		cMap(type).setDeserializer(deserializer);

		return this;
	}
	
	public <T> Oson setSerializer(Class<T> type, AtomicInteger2JsonFunction serializer) {
		cMap(type).setSerializer(serializer);

		return this;
	}
	public <T> Oson setDeserializer(Class<T> type, Json2AtomicIntegerFunction deserializer) {
		cMap(type).setDeserializer(deserializer);

		return this;
	}
	
	public <T> Oson setSerializer(Class<T> type, AtomicLong2JsonFunction serializer) {
		cMap(type).setSerializer(serializer);

		return this;
	}
	public <T> Oson setDeserializer(Class<T> type, Json2AtomicLongFunction deserializer) {
		cMap(type).setDeserializer(deserializer);

		return this;
	}
	
	public <T> Oson setSerializer(Class<T> type, DataMapper2JsonFunction serializer) {
		cMap(type).setSerializer(serializer);

		return this;
	}
	public <T> Oson setDeserializer(Class<T> type, Json2DataMapperFunction deserializer) {
		cMap(type).setDeserializer(deserializer);

		return this;
	}
	public <T> Oson setSerializer(Class<T> type, FieldData2JsonFunction serializer) {
		cMap(type).setSerializer(serializer);

		return this;
	}
	public <T> Oson setDeserializer(Class<T> type, Json2FieldDataFunction deserializer) {
		cMap(type).setDeserializer(deserializer);

		return this;
	}
	
	public <T> Oson setSerializer(Class<T> type, String2JsonFunction serializer) {
		cMap(type).setSerializer(serializer);

		return this;
	}
	public <T> Oson setDeserializer(Class<T> type, Json2StringFunction deserializer) {
		cMap(type).setDeserializer(deserializer);

		return this;
	}
	
	public <T> Oson ser(Class<T> type, Function serializer) {
		return setSerializer(type, serializer);
	}
	
	public <T> Oson des(Class<T> type, Function deserializer) {
		return setDeserializer(type, deserializer);
	}
	
	public <T> Oson ser(Class<T> type, Integer2JsonFunction serializer) {
		return setSerializer(type, serializer);
	}
	public <T> Oson des(Class<T> type, Json2IntegerFunction deserializer) {
		return setDeserializer(type, deserializer);
	}

	public <T> Oson ser(Class<T> type, Long2JsonFunction serializer) {
		return setSerializer(type, serializer);
	}
	public <T> Oson des(Class<T> type, Json2LongFunction deserializer) {
		return setDeserializer(type, deserializer);
	}
	
	public <T> Oson ser(Class<T> type, Double2JsonFunction serializer) {
		return setSerializer(type, serializer);
	}
	public <T> Oson des(Class<T> type, Json2DoubleFunction deserializer) {
		return setDeserializer(type, deserializer);
	}
	
	public <T> Oson ser(Class<T> type, Short2JsonFunction serializer) {
		return setSerializer(type, serializer);
	}
	public <T> Oson des(Class<T> type, Json2ShortFunction deserializer) {
		return setDeserializer(type, deserializer);
	}
	
	public <T> Oson ser(Class<T> type, Float2JsonFunction serializer) {
		return setSerializer(type, serializer);
	}
	public <T> Oson des(Class<T> type, Json2FloatFunction deserializer) {
		return setDeserializer(type, deserializer);
	}

	public <T> Oson ser(Class<T> type, BigDecimal2JsonFunction serializer) {
		return setSerializer(type, serializer);
	}
	public <T> Oson des(Class<T> type, Json2BigDecimalFunction deserializer) {
		return setDeserializer(type, deserializer);
	}
	
	public <T> Oson ser(Class<T> type, BigInteger2JsonFunction serializer) {
		return setSerializer(type, serializer);
	}
	public <T> Oson des(Class<T> type, Json2BigIntegerFunction deserializer) {
		return setDeserializer(type, deserializer);
	}
	
	public <T> Oson ser(Class<T> type, Character2JsonFunction serializer) {
		return setSerializer(type, serializer);
	}
	public <T> Oson des(Class<T> type, Json2CharacterFunction deserializer) {
		return setDeserializer(type, deserializer);
	}
	
	public <T> Oson ser(Class<T> type, Byte2JsonFunction serializer) {
		return setSerializer(type, serializer);
	}
	public <T> Oson des(Class<T> type, Json2ByteFunction deserializer) {
		return setDeserializer(type, deserializer);
	}
	
	public <T> Oson ser(Class<T> type, Boolean2JsonFunction serializer) {
		return setSerializer(type, serializer);
	}
	public <T> Oson des(Class<T> type, Json2BooleanFunction deserializer) {
		return setDeserializer(type, deserializer);
	}
	
	public <T> Oson ser(Class<T> type, Date2JsonFunction serializer) {
		return setSerializer(type, serializer);
	}
	public <T> Oson des(Class<T> type, Json2DateFunction deserializer) {
		return setDeserializer(type, deserializer);
	}
	
	public <T> Oson ser(Class<T> type, Date2LongFunction serializer) {
		return setSerializer(type, serializer);
	}
	public <T> Oson des(Class<T> type, Long2DateFunction deserializer) {
		return setDeserializer(type, deserializer);
	}
	
	public <T> Oson ser(Class<T> type, Enum2JsonFunction serializer) {
		return setSerializer(type, serializer);
	}
	public <T> Oson des(Class<T> type, Json2EnumFunction deserializer) {
		return setDeserializer(type, deserializer);
	}

	public <T> Oson ser(Class<T> type, Collection2JsonFunction serializer) {
		return setSerializer(type, serializer);
	}
	public <T> Oson des(Class<T> type, Json2CollectionFunction deserializer) {
		return setDeserializer(type, deserializer);
	}
	
	public <T> Oson ser(Class<T> type, Map2JsonFunction serializer) {
		return setSerializer(type, serializer);
	}
	public <T> Oson des(Class<T> type, Json2MapFunction deserializer) {
		return setDeserializer(type, deserializer);
	}
	
	public <T> Oson ser(Class<T> type, Array2JsonFunction serializer) {
		return setSerializer(type, serializer);
	}
	public <T> Oson des(Class<T> type, Json2ArrayFunction deserializer) {
		return setDeserializer(type, deserializer);
	}
	
	public <T> Oson ser(Class<T> type, AtomicInteger2JsonFunction serializer) {
		return setSerializer(type, serializer);
	}
	public <T> Oson des(Class<T> type, Json2AtomicIntegerFunction deserializer) {
		return setDeserializer(type, deserializer);
	}
	
	public <T> Oson ser(Class<T> type, AtomicLong2JsonFunction serializer) {
		return setSerializer(type, serializer);
	}
	public <T> Oson des(Class<T> type, Json2AtomicLongFunction deserializer) {
		return setDeserializer(type, deserializer);
	}
	
	public <T> Oson ser(Class<T> type, DataMapper2JsonFunction serializer) {
		return setSerializer(type, serializer);
	}
	public <T> Oson des(Class<T> type, Json2DataMapperFunction deserializer) {
		return setDeserializer(type, deserializer);
	}
	public <T> Oson ser(Class<T> type, FieldData2JsonFunction serializer) {
		return setSerializer(type, serializer);
	}
	public <T> Oson des(Class<T> type, Json2FieldDataFunction deserializer) {
		return setDeserializer(type, deserializer);
	}
	
	public <T> Oson ser(Class<T> type, String2JsonFunction serializer) {
		return setSerializer(type, serializer);
	}
	public <T> Oson des(Class<T> type, Json2StringFunction deserializer) {
		return setDeserializer(type, deserializer);
	}

	public <T> Oson useField(Class<T> type, Boolean useField) {
		cMap(type).setUseField(useField);

		return this;
	}
	public <T> Oson useAttribute(Class<T> type, Boolean useAttribute) {
		cMap(type).setUseAttribute(useAttribute);

		return this;
	}
	public <T> Oson setIncludeFieldsWithModifiers(Class<T> type, Set<MODIFIER> includeFieldsWithModifiers) {
		cMap(type).setIncludeFieldsWithModifiers(includeFieldsWithModifiers);

		return this;
	}
	
	public <T> Oson setSimpleDateFormat(Class<T> type, String simpleDateFormat) {
		cMap(type).setSimpleDateFormat(simpleDateFormat);

		return this;
	}
	public <T> Oson setDateFormat(Class<T> type, DateFormat dateFormat) {
		cMap(type).setDateFormat(dateFormat);

		return this;
	}

	public <T> Oson setOrderByKeyAndProperties(Class<T> type, Boolean orderByKeyAndProperties) {
		cMap(type).setOrderByKeyAndProperties(orderByKeyAndProperties);

		return this;
	}
	
	public <T> Oson setOrderArrayAndList(Class<T> type, Boolean orderArrayAndList) {
		cMap(type).setOrderArrayAndList(orderArrayAndList);

		return this;
	}
	

	public <T> Oson setPropertyOrders(Class<T> type, String[] propertyOrders) {
		cMap(type).setPropertyOrders(propertyOrders);

		return this;
	}
	public <T> Oson setIncludeClassTypeInJson(Class<T> type, Boolean includeClassTypeInJson) {
		cMap(type).setIncludeClassTypeInJson(includeClassTypeInJson);

		return this;
	}
	public <T> Oson setVersion(Class<T> type, Double version) {
		cMap(type).setSince(version);

		return this;
	}
	public <T> Oson setIgnoreFieldsWithAnnotations(Class<T> type, Set<Class> ignoreFieldsWithAnnotations) {
		cMap(type).setIgnoreFieldsWithAnnotations(ignoreFieldsWithAnnotations);

		return this;
	}
	public <T> Oson setJsonIgnoreProperties(Class<T> type, Set<String> jsonIgnoreProperties) {
		cMap(type).setJsonIgnoreProperties(jsonIgnoreProperties);

		return this;
	}
	public <T> Oson setDefaultType(Class<T> type, JSON_INCLUDE defaultType) {
		cMap(type).setDefaultType(defaultType);

		return this;
	}
	public <T> Oson setMin(Class<T> type, Long min) {
		cMap(type).setMin(min);

		return this;
	}
	public <T> Oson setMax(Class<T> type, Long max) {
		cMap(type).setMax(max);

		return this;
	}
	public <T> Oson setEnumType(Class<T> type, EnumType enumType) {
		cMap(type).setEnumType(enumType);

		return this;
	}	
	public <T> Oson setDate2Long(Class<T> type, Boolean date2Long) {
		cMap(type).setDate2Long(date2Long);

		return this;
	}
	public <T> Oson setLength(Class<T> type, Integer length) {
		cMap(type).setLength(length);

		return this;
	}
	public <T> Oson setPrecision(Class<T> type, Integer precision) {
		cMap(type).setPrecision(precision);

		return this;
	}
		
	// end of setting up class mapper
	
	
	private boolean isToStringAsSerializer() {
		return options.isToStringAsSerializer();
	}


	public Oson setToStringAsSerializer(boolean toStringAsSerializer) {
		options.setToStringAsSerializer(toStringAsSerializer);
		
		return this;
	}

	public <T> Oson setToStringAsSerializer(Class<T> type, boolean toStringAsSerializer) {
		cMap(type).setToStringAsSerializer(toStringAsSerializer);

		return this;
	}

	public <T> Oson setEscapeHtml(Class<T> type, boolean escapeHtml) {
		cMap(type).setEscapeHtml(escapeHtml);

		return this;
	}
	
	public boolean isEscapeHtml() {
		return options.isEscapeHtml();
	}


	public Oson setEscapeHtml(boolean escapeHtml) {
		options.setEscapeHtml(escapeHtml);
		
		return this;
	}

	
	public <T> Oson setJsonValueFieldName(Class<T> type, String jsonValueFieldName) {
		cMap(type).setJsonValueFieldName(jsonValueFieldName);

		return this;
	}
	
	
	private void reset() {
	}

	public Oson clear() {
		options = new Options();
		reset();

		return this;
	}
	
	public Oson clearAll() {
		clear();
		
		cachedFields.clear();
		cachedMethods.clear();
		cachedComponentTypes.clear();
		
		return this;
	}
	

	
	////////////////////////////////////////////////////////////////////////////////
	// END OF CONFIGURATION
	////////////////////////////////////////////////////////////////////////////////


	private static Object getMapValue(Map<String, Object> map, String name) {
		return getMapValue(map, name, null);
	}
	private static Object getMapValue(Map<String, Object> map, String name, Set<String> set) {
		if (map == null) {
			return null;
		}

		for (FIELD_NAMING format: FIELD_NAMING.values()) {
			String key = StringUtil.formatName(name, format);
			if (map.containsKey(key)) {
				if (set != null) {
					set.remove(key);
				}
				return map.get(key);
			}
		}

		return null;
	}
	
	
	private Function getDeserializer(FieldData objectDTO) {
		Function function = objectDTO.getDeserializer();
		if (function != null) {
			return function;
		}
		
		return getDeserializer(objectDTO.getDefaultName(), objectDTO.returnType, objectDTO.getEnclosingType());
	}
	
	private Function getSerializer(FieldData objectDTO) {
		Function function = objectDTO.getSerializer();
		if (function != null) {
			return function;
		}
		
		return getSerializer(objectDTO.getDefaultName(), objectDTO.returnType, objectDTO.getEnclosingType());
	}


	private <E,R> Double json2Double(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		E value = (E) objectDTO.valueToProcess;
		Class<R> returnType = objectDTO.returnType;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			Double valueToReturn = null;
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						Object returnedValue = null;

						if (function instanceof Json2DataMapperFunction) {
							DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
							returnedValue = ((Json2DataMapperFunction)function).apply(classData);

						} else if (function instanceof Json2FieldDataFunction) {
							Json2FieldDataFunction f = (Json2FieldDataFunction)function;
							FieldData fieldData = objectDTO.clone();
							
							returnedValue = f.apply(fieldData);
							
						} else if (function instanceof Json2DoubleFunction) {
							return ((Json2DoubleFunction)function).apply(valueToProcess);
						} else {
							returnedValue = function.apply(valueToProcess);
						}

						if (returnedValue instanceof Optional) {
							returnedValue = ObjectUtil.unwrap(returnedValue);
						}
						
						if (returnedValue == null) {
							return null;
							
						} else if (Number.class.isAssignableFrom(returnedValue.getClass()) || returnedValue.getClass().isPrimitive()) {
							
							if (returnedValue instanceof Double) {
								valueToReturn = (Double) returnedValue;
							} else if (returnedValue instanceof String) {
								valueToReturn = Double.parseDouble((String) returnedValue);
								
							} else if (returnedValue instanceof Integer) {
								valueToReturn = ((Integer) returnedValue).doubleValue();
							} else if (returnedValue instanceof Long) {
								valueToReturn = ((Long) returnedValue).doubleValue();
							} else if (returnedValue instanceof Byte) {
								valueToReturn = ((Byte) returnedValue).doubleValue();
							} else if (returnedValue instanceof Short) {
								valueToReturn = ((Short) returnedValue).doubleValue();
							} else if (returnedValue instanceof Float) {
								valueToReturn = ((Float) returnedValue).doubleValue();
							} else if (returnedValue instanceof BigInteger) {
								valueToReturn = ((BigInteger) returnedValue).doubleValue();
							} else if (returnedValue instanceof BigDecimal) {
								valueToReturn = ((BigDecimal) returnedValue).doubleValue();
							} else if (returnedValue instanceof AtomicInteger) {
								valueToReturn = ((AtomicInteger) returnedValue).doubleValue();
							} else if (returnedValue instanceof AtomicLong) {
								valueToReturn = ((AtomicLong) returnedValue).doubleValue();
							} else {
								valueToReturn = ((Number) returnedValue).doubleValue();
							}
							
						} else if (returnedValue instanceof Character) {
							valueToReturn = (double)(((Character) returnedValue).charValue());
							
						} else if (returnedValue instanceof Boolean) {
							if ((Boolean) returnedValue)
								valueToReturn = 1d;
							else
								valueToReturn = 0d;
							
						} else if (Enum.class.isAssignableFrom(returnedValue.getClass())) {
							valueToReturn = ((Integer)((Enum) returnedValue).ordinal()).doubleValue();
							
						} else if (Date.class.isAssignableFrom(returnedValue.getClass())) {
							valueToReturn = (double) ((Date) returnedValue).getTime();
							
						} else {
							valueToReturn = Double.parseDouble(returnedValue.toString());
						}
							
						return valueToReturn;
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					valueToReturn = Double.parseDouble(valueToProcess);
				}
				
				if (valueToReturn != null) {
					Long min = objectDTO.getMin();
					Long max = objectDTO.getMax();
					
					if (min != null && min.doubleValue() > valueToReturn) {
						return min.doubleValue();
					}
					
					if (max != null && max.doubleValue() < valueToReturn) {
						valueToReturn = max.doubleValue();
					}
					
					return valueToReturn;
				}
	
			} catch (Exception ex) {
				//ex.printStackTrace();
			}
		
		}
		
		return json2DoubleDefault(objectDTO);
	}
	
	


	private <E,R> String double2Json(FieldData objectDTO) {
		if (objectDTO == null || objectDTO.json2Java) {
			return null;
		}
		
		E value = (E) objectDTO.valueToProcess;
		Class<R> returnType = objectDTO.returnType;

		if (value != null && returnType != null && (returnType == double.class || returnType == Double.class)) {
			Double valueToProcess = null;
			String valueToReturn = null;
			
			if (returnType == double.class) {
				valueToProcess = Double.valueOf((Double)value);
			} else {
				valueToProcess = (Double)value;
			}
			
			if (valueToProcess != null) {
				try {
					Function function = objectDTO.getSerializer();
					if (function != null) {
						try {
							if (function instanceof DataMapper2JsonFunction) {
								DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
								return ((DataMapper2JsonFunction)function).apply(classData);
								
							} else if (function instanceof Double2JsonFunction) {
								return ((Double2JsonFunction)function).apply(valueToProcess);
								
							} else {
								
								Object returnedValue = null;
								if (function instanceof FieldData2JsonFunction) {
									FieldData2JsonFunction f = (FieldData2JsonFunction)function;
									FieldData fieldData = objectDTO.clone();
									returnedValue = f.apply(fieldData);
								} else {
									returnedValue = function.apply(value);
								}
							
								if (returnedValue instanceof Optional) {
									returnedValue = ObjectUtil.unwrap(returnedValue);
								}
								
								if (returnedValue == null) {
									return null;
									
								} else if (returnedValue instanceof Double) {
									valueToProcess = (Double) returnedValue;
									
								} else {
									objectDTO.valueToProcess = returnedValue;
									return object2String(objectDTO);
								}
								
							}
							
						} catch (Exception e) {}
					}

					if (valueToProcess != null) {
						Long min = objectDTO.getMin();
						Long max = objectDTO.getMax();
						
						if (min != null && min.doubleValue() > valueToProcess) {
							valueToProcess = min.doubleValue();
						}
						
						if (max != null && max.doubleValue() < valueToProcess) {
							valueToProcess = max.doubleValue();
						}
						
						Integer precision = objectDTO.getPrecision();
						Integer scale = objectDTO.getScale();
						
						String result = null;
						
						if (precision != null) {
							if (scale != null) {
								valueToProcess = (double) NumberUtil.setPrecision(valueToProcess, precision, getRoundingMode());
								BigDecimal b = new BigDecimal(valueToProcess);
								
								b = b.setScale(scale, getRoundingMode());
								
								result = NumberUtil.toPlainString(b);
								
							} else {
								result = NumberUtil.precision2Json(valueToProcess, precision, getRoundingMode());
							}
						
						} else if (scale != null) {
							BigDecimal b = new BigDecimal(valueToProcess);
							
							b = b.setScale(scale, getRoundingMode());
							result = NumberUtil.toPlainString(b);
							
						} else {
							result = NumberUtil.toPlainString(valueToProcess);
						}
						
						return NumberUtil.appendingFloatingZero(result, isAppendingFloatingZero());
					}

				} catch (Exception ex) {
					//ex.printStackTrace();
				}
			}
		}
		
		return double2JsonDefault(objectDTO);
	}
		
	private String double2JsonDefault(FieldData objectDTO) {
		Double valueToReturn = json2DoubleDefault(objectDTO);
		
		if (valueToReturn == null) {
			return null;
		}
		
		return valueToReturn.toString();
	}
		
	private <E,R> Double json2DoubleDefault(FieldData objectDTO) {
		E value = (E) objectDTO.valueToProcess;
		Class<R> returnType = objectDTO.returnType;
		boolean required = objectDTO.required();
		
		Long min = objectDTO.getMin();
		Long max = objectDTO.getMax();
		boolean json2Java = objectDTO.json2Java;

		if (returnType == double.class
				|| getDefaultType() == JSON_INCLUDE.DEFAULT || required) {
			Double defaultValue = (Double)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				if (min != null && min.doubleValue() > defaultValue) {
					return min.doubleValue();
				}

				return defaultValue;
			}

			if (min != null && min.doubleValue() > DefaultValue.ddouble) {
				return min.doubleValue();
			}

			return DefaultValue.ddouble;
		}

		return null;
	}

	private <E,R> Float json2Float(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		E value = (E) objectDTO.valueToProcess;
		Class<R> returnType = objectDTO.returnType;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			Float valueToReturn = null;
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						Object returnedValue = null;

						if (function instanceof Json2DataMapperFunction) {
							DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
							returnedValue = ((Json2DataMapperFunction)function).apply(classData);

						} else if (function instanceof Json2FieldDataFunction) {
							Json2FieldDataFunction f = (Json2FieldDataFunction)function;
							FieldData fieldData = objectDTO.clone();
							
							returnedValue = f.apply(fieldData);
							
						} else if (function instanceof Json2FloatFunction) {
							return ((Json2FloatFunction)function).apply(valueToProcess);
						} else {
							returnedValue = function.apply(valueToProcess);
						}

						if (returnedValue instanceof Optional) {
							returnedValue = ObjectUtil.unwrap(returnedValue);
						}
							
						if (returnedValue == null) {
							return null;
							
						} else if (Number.class.isAssignableFrom(returnedValue.getClass()) || returnedValue.getClass().isPrimitive()) {
							
							if (returnedValue instanceof Float) {
								valueToReturn = (Float) returnedValue;
							} else if (returnedValue instanceof String) {
								valueToReturn = Float.parseFloat((String) returnedValue);
								
							} else if (returnedValue instanceof Integer) {
								valueToReturn = ((Integer) returnedValue).floatValue();
							} else if (returnedValue instanceof Long) {
								valueToReturn = ((Long) returnedValue).floatValue();
							} else if (returnedValue instanceof Short) {
								valueToReturn = ((Short) returnedValue).floatValue();
							} else if (returnedValue instanceof Double) {
								valueToReturn = ((Double) returnedValue).floatValue();
							} else if (returnedValue instanceof Byte) {
								valueToReturn = ((Byte) returnedValue).floatValue();
							} else if (returnedValue instanceof BigInteger) {
								valueToReturn = ((BigInteger) returnedValue).floatValue();
							} else if (returnedValue instanceof BigDecimal) {
								valueToReturn = ((BigDecimal) returnedValue).floatValue();
							} else if (returnedValue instanceof AtomicInteger) {
								valueToReturn = ((AtomicInteger) returnedValue).floatValue();
							} else if (returnedValue instanceof AtomicLong) {
								valueToReturn = ((AtomicLong) returnedValue).floatValue();
							} else {
								valueToReturn = ((Number) returnedValue).floatValue();
							}
							
						} else if (returnedValue instanceof Character) {
							char c = (Character) returnedValue;
							valueToReturn = (float)c;
							
						} else if (returnedValue instanceof Boolean) {
							if ((Boolean) returnedValue)
								valueToReturn = 1f;
							else
								valueToReturn = 0f;
							
						} else if (Enum.class.isAssignableFrom(returnedValue.getClass())) {
							valueToReturn = ((Integer)((Enum) returnedValue).ordinal()).floatValue();
							
						} else if (Date.class.isAssignableFrom(returnedValue.getClass())) {
							valueToReturn = (float) ((Date) returnedValue).getTime();
							
						} else {
							valueToReturn = Float.parseFloat(returnedValue.toString());
						}
							
						return valueToReturn;
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					double doubleValue = Double.parseDouble(valueToProcess);
					
					if (doubleValue > Float.MAX_VALUE) {
						valueToReturn = Float.MAX_VALUE;
					} else {
						valueToReturn = (float)doubleValue;
					}
					
					// valueToReturn = Float.parseFloat(valueToProcess);
				}
				
				if (valueToReturn != null) {
					Long min = objectDTO.getMin();
					Long max = objectDTO.getMax();
					
					if (min != null && min.floatValue() > valueToReturn) {
						return min.floatValue();
					}
					
					if (max != null && max.floatValue() < valueToReturn) {
						valueToReturn = max.floatValue();
					}

					return valueToReturn;
				}
	
			} catch (Exception ex) {
				//ex.printStackTrace();
			}
		
		}
		
		return json2FloatDefault(objectDTO);
	}
	
	


	private <E,R> String float2Json(FieldData objectDTO) {
		if (objectDTO == null || objectDTO.json2Java) {
			return null;
		}
		
		E value = (E) objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;

		if (value != null && returnType != null && (returnType == float.class || returnType == Float.class)) {
			Float valueToProcess = null;
			String valueToReturn = null;
			
			if (returnType == float.class) {
				valueToProcess = Float.valueOf((Float)value);
			} else {
				valueToProcess = (Float)value;
			}
			
			if (valueToProcess != null) {
				try {
					Function function = objectDTO.getSerializer();
					if (function != null) {
						try {
							if (function instanceof DataMapper2JsonFunction) {
								DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
								return ((DataMapper2JsonFunction)function).apply(classData);
								
							} else if (function instanceof Float2JsonFunction) {
								return ((Float2JsonFunction)function).apply(valueToProcess);
								
							} else {
								
								Object returnedValue = null;
								if (function instanceof FieldData2JsonFunction) {
									FieldData2JsonFunction f = (FieldData2JsonFunction)function;
									FieldData fieldData = objectDTO.clone();
									returnedValue = f.apply(fieldData);
								} else {
									returnedValue = function.apply(value);
								}
							
								if (returnedValue instanceof Optional) {
									returnedValue = ObjectUtil.unwrap(returnedValue);
								}
								
								if (returnedValue == null) {
									return null;
									
								} else if (returnedValue instanceof Float) {
									valueToProcess = (Float) returnedValue;
									
								} else {
									objectDTO.valueToProcess = returnedValue;
									return object2String(objectDTO);
								}
								
							}
							
						} catch (Exception e) {}
					}

					if (valueToProcess != null) {
						Long min = objectDTO.getMin();
						Long max = objectDTO.getMax();
						
						if (min != null && min.floatValue() > valueToProcess) {
							valueToProcess = min.floatValue();
						}
						
						if (max != null && max.floatValue() < valueToProcess) {
							valueToProcess = max.floatValue();
						}
						
						Integer precision = objectDTO.getPrecision();
						Integer scale = objectDTO.getScale();
						
						String result = null;
						
						if (precision != null) {
							if (scale != null) {
								valueToProcess = (float) NumberUtil.setPrecision(valueToProcess, precision, getRoundingMode());
								BigDecimal b = new BigDecimal(valueToProcess);
								
								b = b.setScale(scale, getRoundingMode());
								result = NumberUtil.toPlainString(b);
								
							} else {
								result = NumberUtil.precision2Json(valueToProcess, precision, getRoundingMode());
							}
						
						} else if (scale != null) {
							BigDecimal b = new BigDecimal(valueToProcess);
							
							b = b.setScale(scale, getRoundingMode());
							result = NumberUtil.toPlainString(b);
						} else {
							result = NumberUtil.toPlainString(valueToProcess);
						}
						
						return NumberUtil.appendingFloatingZero(result, isAppendingFloatingZero());
					}

				} catch (Exception ex) {
					//ex.printStackTrace();
				}
			}
		}
		
		return float2JsonDefault(objectDTO);
	}
		
	private String float2JsonDefault(FieldData objectDTO) {
		Float valueToReturn = json2FloatDefault(objectDTO);
		
		if (valueToReturn == null) {
			return null;
		}
		
		return valueToReturn.toString();
	}
		
	private <E,R> Float json2FloatDefault(FieldData objectDTO) {
		E value = (E) objectDTO.valueToProcess;
		Class<R> returnType = objectDTO.returnType;
		boolean required = objectDTO.required();
		
		Long min = objectDTO.getMin();
		Long max = objectDTO.getMax();
		boolean json2Java = objectDTO.json2Java;

		if (returnType == float.class
				|| getDefaultType() == JSON_INCLUDE.DEFAULT || required) {
			Float defaultValue = (Float)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				if (min != null && min.floatValue() > defaultValue) {
					return min.floatValue();
				}

				return defaultValue;
			}

			if (min != null && min.floatValue() > DefaultValue.dfloat) {
				return min.floatValue();
			}

			return DefaultValue.dfloat;
		}

		return null;
	}

	private Date date2Date(Date currentDate, Class returnType) {
		if (currentDate == null) {
			return currentDate;
		}
		
		Class cls = currentDate.getClass();

		if (returnType.isAssignableFrom(cls)) {
			return currentDate;
		}

		if (Date.class != returnType) {
			return date2Date(currentDate.getTime(), returnType);
		} else {
			return currentDate;
		}
	}
	
	private Date date2Date(long longdatetime, Class returnType) {
		if (java.sql.Date.class == returnType) {
			return new java.sql.Date(longdatetime);
			
		} else if (java.sql.Time.class == returnType) {
			//currentDate.getHours(), currentDate.getMinutes(), currentDate.getSeconds()
			return new java.sql.Time(longdatetime);
			
		} else if (java.sql.Timestamp.class == returnType) {
			return new java.sql.Timestamp(longdatetime);
		} else {
			return new Date(longdatetime);
		}
	}
	
 	private <E,R> Date json2Date(FieldData objectDTO) {
 		if (objectDTO == null || !objectDTO.json2Java) {
 			return null;
 		}
 		
 		E value = (E) objectDTO.valueToProcess;
 		Class<R> returnType = objectDTO.returnType;

 		if (value != null && value.toString().trim().length() > 0) {
 			String valueToProcess = value.toString().trim();

 			try {
 				Function function = objectDTO.getDeserializer();
 				
 				if (function != null) {
 					try {
 						Object returnedValue = null;

						if (function instanceof Json2DataMapperFunction) {
							DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
							returnedValue = ((Json2DataMapperFunction)function).apply(classData);

						} else if (function instanceof Json2FieldDataFunction) {
							Json2FieldDataFunction f = (Json2FieldDataFunction)function;
							FieldData fieldData = objectDTO.clone();
							
							returnedValue = f.apply(fieldData);
							
						} else if (function instanceof Json2DateFunction) {
 							return ((Json2DateFunction)function).apply(valueToProcess);
 							
 						} else if (function instanceof Long2DateFunction) {
 							long longtoprocess = Long.parseLong(NumberUtil.removeTrailingDecimalZeros(valueToProcess));
 							return ((Long2DateFunction)function).apply(longtoprocess);
 						
 						} else {
 							returnedValue = function.apply(valueToProcess);
 						}
 
						if (returnedValue instanceof Optional) {
							returnedValue = ObjectUtil.unwrap(returnedValue);
						}
						
						if (returnedValue == null) {
							return null;
							
						} else if (Date.class.isAssignableFrom(returnedValue.getClass())) {
							
							return date2Date((Date) returnedValue, returnType);

						} else {
							valueToProcess = String.valueOf(returnedValue);
						}
 						
 					} catch (Exception e) {
 						e.printStackTrace();
 					}
 					
 				}
 				
 				if (valueToProcess != null) {
 					valueToProcess = StringUtil.unquote(valueToProcess, isEscapeHtml());
					if (StringUtil.isNumeric(valueToProcess)) {
						try {
							long longdatetime = Long.parseLong(NumberUtil.removeTrailingDecimalZeros(valueToProcess));
							return date2Date(longdatetime, returnType);
						} catch (Exception e) {
						}
					}
					
					try {
						DateFormat format = objectDTO.getDateFormat();
						return date2Date(format.parse(valueToProcess), returnType);
					} catch (Exception e) {
						return date2Date(new Date(valueToProcess), returnType);
					}

 				}
 	
 			} catch (Exception ex) {
 				//ex.printStackTrace();
 			}
 		
 		}
 		
 		return date2Date(json2DateDefault(objectDTO), returnType);
 	}
 	
 	
 	private <E,R> String date2Json(FieldData objectDTO) {
 		if (objectDTO == null || objectDTO.json2Java) {
 			return null;
 		}
 		
 		E value = (E) objectDTO.valueToProcess;
 		Class<R> returnType = objectDTO.returnType;
 
 		if (value != null && value.toString().trim().length() > 0) {
 			DateFormat format = objectDTO.getDateFormat();
 			Date valueToProcess = null;
 			String valueToReturn = null;
 			
 			if (value instanceof Date) {
 				valueToProcess = (Date)value;
 			} else {
 				try {
 					valueToProcess = format.parse(value.toString().trim());
 				} catch (Exception ex) {}
 			}
 			
 			if (valueToProcess != null) {
 				try {
 					Function function = objectDTO.getSerializer();
 					if (function != null) {
 						try {
 							if (function instanceof DataMapper2JsonFunction) {
 								DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
 								return ((DataMapper2JsonFunction)function).apply(classData);
 								
 							} else if (function instanceof Date2JsonFunction) {
 								return ((Date2JsonFunction)function).apply(valueToProcess);
 								
 							} else if (function instanceof Date2LongFunction) {
 	 								Long longtoprocess = ((Date2LongFunction)function).apply(valueToProcess);
 	 								return longtoprocess.toString();
 	 								
 							} else {
 								
								Object returnedValue = null;
								if (function instanceof FieldData2JsonFunction) {
									FieldData2JsonFunction f = (FieldData2JsonFunction)function;
									FieldData fieldData = objectDTO.clone();
									returnedValue = f.apply(fieldData);
								} else {
									returnedValue = function.apply(value);
								}
 							
								if (returnedValue instanceof Optional) {
									returnedValue = ObjectUtil.unwrap(returnedValue);
								}
 								
 								if (returnedValue == null) {
 									return null;
 									
								} else if (returnedValue instanceof Date) {
									valueToProcess = (Date) returnedValue;
 									
 								} else {
 									objectDTO.valueToProcess = returnedValue;
 									return object2String(objectDTO);
 								}
 								
 							}
 							
 						} catch (Exception e) {}
 					}
 
 					if (valueToProcess != null) {
 						Boolean date2Long = objectDTO.getDate2Long();
 						if (date2Long != null && date2Long) {
 							long longtoprocess = valueToProcess.getTime();
 							return NumberUtil.toPlainString(longtoprocess);
 						} else {
 							return format.format(valueToProcess);
 						}
 					}
 
 				} catch (Exception ex) {
 					//ex.printStackTrace();
 				}
 			}
 		}
 		
 		return date2JsonDefault(objectDTO);
 	}
 		
 	private String date2JsonDefault(FieldData objectDTO) {
 		Date valueToReturn = json2DateDefault(objectDTO);
 		
 		if (valueToReturn == null) {
 			return null;
 		}
 		
		objectDTO.valueToProcess = valueToReturn;
		return object2String(objectDTO);
 	}
 		
 	private <E> Date json2DateDefault(FieldData objectDTO) {
		if (getDefaultType() == JSON_INCLUDE.DEFAULT || objectDTO.required()) {
			Date defaultValue = (Date)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				return defaultValue;
			}

			return DefaultValue.getDate();
		}
 
 		return null;
	}
 	

	private <E,R> BigDecimal json2BigDecimal(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		E value = (E) objectDTO.valueToProcess;
		Class<R> returnType = objectDTO.returnType;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			BigDecimal valueToReturn = null;
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						Object returnedValue = null;

						if (function instanceof Json2DataMapperFunction) {
							DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
							returnedValue = ((Json2DataMapperFunction)function).apply(classData);

						} else if (function instanceof Json2FieldDataFunction) {
							Json2FieldDataFunction f = (Json2FieldDataFunction)function;
							FieldData fieldData = objectDTO.clone();
							
							returnedValue = f.apply(fieldData);
							
						} else if (function instanceof Json2BigDecimalFunction) {
							return ((Json2BigDecimalFunction)function).apply(valueToProcess);
						} else {
							returnedValue = function.apply(valueToProcess);
						}

						if (returnedValue instanceof Optional) {
							returnedValue = ObjectUtil.unwrap(returnedValue);
						}
						
						if (returnedValue == null) {
							return null;
							
						} else if (Number.class.isAssignableFrom(returnedValue.getClass()) || returnedValue.getClass().isPrimitive()) {
							
							if (returnedValue instanceof BigDecimal) {
								valueToReturn = (BigDecimal) returnedValue;
							} else if (returnedValue instanceof String) {
								valueToReturn = new BigDecimal((String) returnedValue);
								
							} else if (returnedValue instanceof Integer) {
								valueToReturn = new BigDecimal((Integer) returnedValue);
							} else if (returnedValue instanceof Long) {
								valueToReturn = new BigDecimal((Long) returnedValue);
							} else if (returnedValue instanceof Short) {
								valueToReturn = new BigDecimal((Short) returnedValue);
							} else if (returnedValue instanceof Double) {
								valueToReturn = new BigDecimal((Double) returnedValue);
							} else if (returnedValue instanceof Float) {
								valueToReturn = new BigDecimal((Float) returnedValue);
							} else if (returnedValue instanceof BigInteger) {
								valueToReturn = new BigDecimal((BigInteger) returnedValue);
							} else if (returnedValue instanceof Byte) {
								valueToReturn = new BigDecimal((Byte) returnedValue);
							} else if (returnedValue instanceof AtomicInteger) {
								valueToReturn = new BigDecimal(((AtomicInteger) returnedValue).intValue());
							} else if (returnedValue instanceof AtomicLong) {
								valueToReturn = new BigDecimal(((AtomicLong) returnedValue).longValue());
							} else {
								valueToReturn = new BigDecimal(((Number) returnedValue).doubleValue());
							}
							
						} else if (returnedValue instanceof Character) {
							char c = (Character) returnedValue;
							valueToReturn = new BigDecimal(Character.getNumericValue((Character) returnedValue));
							
						} else if (returnedValue instanceof Boolean) {
							if ((Boolean) returnedValue)
								valueToReturn = new BigDecimal(1);
							else
								valueToReturn = new BigDecimal(0);
							
						} else if (Enum.class.isAssignableFrom(returnedValue.getClass())) {
							valueToReturn = new BigDecimal((Integer)((Enum) returnedValue).ordinal());
							
						} else if (Date.class.isAssignableFrom(returnedValue.getClass())) {
							valueToReturn = new BigDecimal(((Date) returnedValue).getTime());
							
						} else {
							valueToReturn = new BigDecimal((returnedValue.toString()));
						}

						return valueToReturn;
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					valueToReturn = new BigDecimal(valueToProcess);
				}
				
				if (valueToReturn != null) {
					Long min = objectDTO.getMin();
					Long max = objectDTO.getMax();
					
					if (min != null && valueToReturn.compareTo(new BigDecimal(min)) < 0) {
						return new BigDecimal(min);
					}
					
					if (max != null && valueToReturn.compareTo(new BigDecimal(max)) > 0) {
						valueToReturn = new BigDecimal(max);
					}
					
					Integer precision = objectDTO.getPrecision();
					if (precision != null && precision < valueToReturn.precision()) {
						valueToReturn = (BigDecimal)NumberUtil.setPrecision(valueToReturn, precision, getRoundingMode());
					}
					
					Integer scale = objectDTO.getScale();
					if (scale != null) {
						valueToReturn = valueToReturn.setScale(scale, getRoundingMode());
					}

					return valueToReturn;
				}
	
			} catch (Exception ex) {
				//ex.printStackTrace();
			}
		
		}
		
		return json2BigDecimalDefault(objectDTO);
	}
	
	


	private <E,R> String bigDecimal2Json(FieldData objectDTO) {
		if (objectDTO == null || objectDTO.json2Java) {
			return null;
		}
		
		E value = (E) objectDTO.valueToProcess;
		Class<R> returnType = objectDTO.returnType;

		if (value != null && value.toString().trim().length() > 0) {
			BigDecimal valueToProcess = null;
			String valueToReturn = null;
			
			if (value instanceof BigDecimal) {
				valueToProcess = (BigDecimal)value;
			} else {
				try {
					valueToProcess = new BigDecimal(value.toString().trim());
				} catch (Exception ex) {}
			}
			
			if (valueToProcess != null) {
				try {
					Function function = objectDTO.getSerializer();
					if (function != null) {
						try {
							if (function instanceof DataMapper2JsonFunction) {
								DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
								return ((DataMapper2JsonFunction)function).apply(classData);
								
							} else if (function instanceof BigDecimal2JsonFunction) {
								return ((BigDecimal2JsonFunction)function).apply(valueToProcess);
								
							} else {
								
								Object returnedValue = null;
								if (function instanceof FieldData2JsonFunction) {
									FieldData2JsonFunction f = (FieldData2JsonFunction)function;
									FieldData fieldData = objectDTO.clone();
									returnedValue = f.apply(fieldData);
								} else {
									returnedValue = function.apply(value);
								}
							
								if (returnedValue instanceof Optional) {
									returnedValue = ObjectUtil.unwrap(returnedValue);
								}
								
								if (returnedValue == null) {
									return null;
									
								} else if (returnedValue instanceof BigDecimal) {
									valueToProcess = (BigDecimal) returnedValue;
									
								} else {
									objectDTO.valueToProcess = returnedValue;
									return object2String(objectDTO);
								}
								
							}
							
						} catch (Exception e) {}
					}

					if (valueToProcess != null) {
						Long min = objectDTO.getMin();
						Long max = objectDTO.getMax();
						
						if (min != null && valueToProcess.compareTo(new BigDecimal(min)) < 0) {
							valueToProcess = new BigDecimal(min);
						}
						
						if (max != null && valueToProcess.compareTo(new BigDecimal(max)) > 0) {
							valueToProcess = new BigDecimal(max);
						}
						
						Integer precision = objectDTO.getPrecision();
						Integer scale = objectDTO.getScale();
						
						if (precision != null && precision < valueToProcess.precision()) {
							valueToProcess = (BigDecimal)NumberUtil.setPrecision(valueToProcess, precision, getRoundingMode());
							
							if (scale != null) {
								valueToProcess = valueToProcess.setScale(scale, getRoundingMode());
							}
							
						} else if (scale != null) {
							valueToProcess = valueToProcess.setScale(scale, getRoundingMode());
						}

						return NumberUtil.appendingFloatingZero(NumberUtil.toPlainString(valueToProcess), this.isAppendingFloatingZero());
					}

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
		
		return bigDecimal2JsonDefault(objectDTO);
	}
		
	private String bigDecimal2JsonDefault(FieldData objectDTO) {
		BigDecimal valueToReturn = json2BigDecimalDefault(objectDTO);
		
		if (valueToReturn == null) {
			return null;
		}
		
		return valueToReturn.toString();
	}
		
	private <E,R> BigDecimal json2BigDecimalDefault(FieldData objectDTO) {
		E value = (E) objectDTO.valueToProcess;
		Class<R> returnType = objectDTO.returnType;
		boolean required = objectDTO.required();
		
		Long min = objectDTO.getMin();
		Long max = objectDTO.getMax();
		boolean json2Java = objectDTO.json2Java;

		if (getDefaultType() == JSON_INCLUDE.DEFAULT || required) {
			BigDecimal defaultValue = (BigDecimal)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				if (min != null && defaultValue.compareTo(new BigDecimal(min)) < 0) {
					return new BigDecimal(min);
				}
				
				if (max != null && defaultValue.compareTo(new BigDecimal(max)) > 0) {
					return new BigDecimal(max);
				}
				
				return defaultValue;
			}

			if (min != null && DefaultValue.bigDecimal.compareTo(new BigDecimal(min)) < 0) {
				return new BigDecimal(min);
			}

			return DefaultValue.bigDecimal;
		}

		return null;
	}	

	private <E,R> BigInteger json2BigInteger(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		E value = (E) objectDTO.valueToProcess;
		Class<R> returnType = objectDTO.returnType;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			BigInteger valueToReturn = null;
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						Object returnedValue = null;

						if (function instanceof Json2DataMapperFunction) {
							DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
							returnedValue = ((Json2DataMapperFunction)function).apply(classData);

						} else if (function instanceof Json2FieldDataFunction) {
							Json2FieldDataFunction f = (Json2FieldDataFunction)function;
							FieldData fieldData = objectDTO.clone();
							
							returnedValue = f.apply(fieldData);
							
						} else if (function instanceof Json2BigIntegerFunction) {
							return ((Json2BigIntegerFunction)function).apply(valueToProcess);
						} else {
							returnedValue = function.apply(valueToProcess);
						}

						if (returnedValue instanceof Optional) {
							returnedValue = ObjectUtil.unwrap(returnedValue);
						}
						
						if (returnedValue == null) {
							return null;
							
						} else if (Number.class.isAssignableFrom(returnedValue.getClass()) || returnedValue.getClass().isPrimitive()) {
							
							if (returnedValue instanceof BigInteger) {
								valueToReturn = (BigInteger) returnedValue;
							} else if (returnedValue instanceof String) {
								valueToReturn = new BigInteger((String) returnedValue);
								
							} else if (returnedValue instanceof Integer) {
								valueToReturn = BigInteger.valueOf((Integer) returnedValue);
							} else if (returnedValue instanceof Long) {
								valueToReturn = BigInteger.valueOf((Long) returnedValue);
							} else if (returnedValue instanceof Short) {
								valueToReturn = BigInteger.valueOf((Short) returnedValue);
							} else if (returnedValue instanceof Double) {
								valueToReturn = BigInteger.valueOf(((Double) returnedValue).longValue());
							} else if (returnedValue instanceof Float) {
								valueToReturn = BigInteger.valueOf(((Float) returnedValue).intValue());
							} else if (returnedValue instanceof BigDecimal) {
								valueToReturn = BigInteger.valueOf(((BigDecimal) returnedValue).longValue());
							} else if (returnedValue instanceof Byte) {
								valueToReturn = BigInteger.valueOf((Byte) returnedValue);
							} else if (returnedValue instanceof AtomicInteger) {
								valueToReturn = BigInteger.valueOf(((AtomicInteger) returnedValue).intValue());
							} else if (returnedValue instanceof AtomicLong) {
								valueToReturn = BigInteger.valueOf(((AtomicLong) returnedValue).longValue());
							} else {
								valueToReturn = BigInteger.valueOf(((Number) returnedValue).longValue());
							}
							
						} else if (returnedValue instanceof Character) {
							valueToReturn = BigInteger.valueOf(((Character) returnedValue));
							
						} else if (returnedValue instanceof Boolean) {
							if ((Boolean) returnedValue)
								valueToReturn = BigInteger.valueOf(1);
							else
								valueToReturn = BigInteger.valueOf(0);
							
						} else if (Enum.class.isAssignableFrom(returnedValue.getClass())) {
							valueToReturn = BigInteger.valueOf(((Enum) returnedValue).ordinal());
							
						} else if (Date.class.isAssignableFrom(returnedValue.getClass())) {
							valueToReturn = BigInteger.valueOf(((Date) returnedValue).getTime());
							
						} else {
							valueToReturn = new BigInteger((returnedValue.toString()));
						}
							
						return valueToReturn;
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					valueToReturn = new BigInteger(valueToProcess);
				}
				
				if (valueToReturn != null) {
					Long min = objectDTO.getMin();
					Long max = objectDTO.getMax();
					
					if (min != null && min > valueToReturn.longValue()) {
						return BigInteger.valueOf(min.intValue());
					}
		
					if (max != null && valueToReturn.compareTo(BigInteger.valueOf(max)) > 0) {
						valueToReturn = BigInteger.valueOf(max);
					}
					
					return valueToReturn;
				}
	
			} catch (Exception ex) {
				//ex.printStackTrace();
			}
		
		}
		
		return json2BigIntegerDefault(objectDTO);
	}
	
	


	private <E,R> String bigInteger2Json(FieldData objectDTO) {
		if (objectDTO == null || objectDTO.json2Java) {
			return null;
		}
		
		E value = (E) objectDTO.valueToProcess;
		Class<R> returnType = objectDTO.returnType;

		if (value != null && value.toString().trim().length() > 0) {
			BigInteger valueToProcess = null;
			String valueToReturn = null;
			
			if (value instanceof BigInteger) {
				valueToProcess = (BigInteger)value;
			} else {
				try {
					valueToProcess = new BigInteger(value.toString().trim());
				} catch (Exception ex) {}
			}
			
			if (valueToProcess != null) {
				try {
					Function function = objectDTO.getSerializer();
					if (function != null) {
						try {
							if (function instanceof DataMapper2JsonFunction) {
								DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
								return ((DataMapper2JsonFunction)function).apply(classData);
								
							} else if (function instanceof BigInteger2JsonFunction) {
								return ((BigInteger2JsonFunction)function).apply(valueToProcess);
								
							} else {
								
								Object returnedValue = null;
								if (function instanceof FieldData2JsonFunction) {
									FieldData2JsonFunction f = (FieldData2JsonFunction)function;
									FieldData fieldData = objectDTO.clone();
									returnedValue = f.apply(fieldData);
								} else {
									returnedValue = function.apply(value);
								}
							
								if (returnedValue instanceof Optional) {
									returnedValue = ObjectUtil.unwrap(returnedValue);
								}
								
								if (returnedValue == null) {
									return null;
									
								} else if (returnedValue instanceof BigInteger) {
									valueToProcess = (BigInteger) returnedValue;
								
								} else {
									objectDTO.valueToProcess = returnedValue;
									return object2String(objectDTO);
								}
								
							}
							
						} catch (Exception e) {}
					}

					if (valueToProcess != null) {
						Long min = objectDTO.getMin();
						Long max = objectDTO.getMax();
						
						if (min != null && min > valueToProcess.longValue()) {
							valueToProcess = BigInteger.valueOf(min);
						}
						
						if (max != null && valueToProcess.compareTo(BigInteger.valueOf(max)) > 0) {
							valueToProcess = BigInteger.valueOf(max);
						}
						
						Integer precision = objectDTO.getPrecision();
						if (precision != null) {
							valueToProcess = (BigInteger)NumberUtil.setPrecision(valueToProcess, precision, getRoundingMode());
						}
						
						return NumberUtil.toPlainString(valueToProcess);
					}

				} catch (Exception ex) {
					//ex.printStackTrace();
				}
			}
		}
		
		return bigInteger2JsonDefault(objectDTO);
	}
		
	private String bigInteger2JsonDefault(FieldData objectDTO) {
		BigInteger valueToReturn = json2BigIntegerDefault(objectDTO);
		
		if (valueToReturn == null) {
			return null;
		}
		
		return valueToReturn.toString();
	}
		
	private <E,R> BigInteger json2BigIntegerDefault(FieldData objectDTO) {
		E value = (E) objectDTO.valueToProcess;
		Class<R> returnType = objectDTO.returnType;
		boolean required = objectDTO.required();
		
		Long min = objectDTO.getMin();
		Long max = objectDTO.getMax();
		
		if (getDefaultType() == JSON_INCLUDE.DEFAULT || required) {
			BigInteger defaultValue = (BigInteger)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				if (min != null && min > defaultValue.longValue()) {
					return BigInteger.valueOf(min);
				}

				if (max != null && defaultValue.compareTo(BigInteger.valueOf(max)) > 0) {
					return BigInteger.valueOf(max);
				}				
				
				return defaultValue;
			}


			if (min != null && DefaultValue.bigInteger.compareTo(BigInteger.valueOf(min)) < 0) {
				return BigInteger.valueOf(min);
			}

			return DefaultValue.bigInteger;
		}

		return null;
	}

	private <E,R> AtomicInteger json2AtomicInteger(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		E value = (E) objectDTO.valueToProcess;
		Class<R> returnType = objectDTO.returnType;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			AtomicInteger valueToReturn = null;
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						Object returnedValue = null;
						// suppose to return AtomicInteger, but in case not, try to process
						if (function instanceof Json2DataMapperFunction) {
							DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
							returnedValue = ((Json2DataMapperFunction)function).apply(classData);

						} else if (function instanceof Json2FieldDataFunction) {
							Json2FieldDataFunction f = (Json2FieldDataFunction)function;
							FieldData fieldData = objectDTO.clone();
							
							returnedValue = f.apply(fieldData);
							
						} else if (function instanceof Json2AtomicIntegerFunction) {
							return ((Json2AtomicIntegerFunction)function).apply(valueToProcess);
							
						} else {
							returnedValue = function.apply(valueToProcess);
						}

						if (returnedValue instanceof Optional) {
							returnedValue = ObjectUtil.unwrap(returnedValue);
						}
						
						if (returnedValue == null) {
							return null;
							
						} else if (Number.class.isAssignableFrom(returnedValue.getClass()) || returnedValue.getClass().isPrimitive()) {
							
							if (returnedValue instanceof AtomicInteger) {
								valueToReturn = (AtomicInteger) returnedValue;
							} else if (returnedValue instanceof String) {
								valueToReturn = new AtomicInteger(Integer.parseInt(NumberUtil.removeTrailingDecimalZeros(returnedValue)));
								
							} else if (returnedValue instanceof Integer) {
								valueToReturn = new AtomicInteger((Integer) returnedValue);
							} else if (returnedValue instanceof Long) {
								valueToReturn = new AtomicInteger(((Long) returnedValue).intValue());
							} else if (returnedValue instanceof Short) {
								valueToReturn = new AtomicInteger((Short) returnedValue);
							} else if (returnedValue instanceof Double) {
								valueToReturn = new AtomicInteger(((Double) returnedValue).intValue());
							} else if (returnedValue instanceof Float) {
								valueToReturn = new AtomicInteger(((Float) returnedValue).intValue());
							} else if (returnedValue instanceof BigDecimal) {
								valueToReturn = new AtomicInteger(((BigDecimal) returnedValue).intValue());
							} else if (returnedValue instanceof Byte) {
								valueToReturn = new AtomicInteger((Byte) returnedValue);
							} else if (returnedValue instanceof BigInteger) {
								valueToReturn = new AtomicInteger(((BigInteger) returnedValue).intValue());
							} else if (returnedValue instanceof AtomicLong) {
								valueToReturn = new AtomicInteger(((AtomicLong) returnedValue).intValue());
							} else {
								valueToReturn = new AtomicInteger(((Number) returnedValue).intValue());
							}
							
						} else if (returnedValue instanceof Character) {
							valueToReturn = new AtomicInteger(((Character) returnedValue));
							
						} else if (returnedValue instanceof Boolean) {
							if ((Boolean) returnedValue)
								valueToReturn = new AtomicInteger(1);
							else
								valueToReturn = new AtomicInteger(0);
							
						} else if (Enum.class.isAssignableFrom(returnedValue.getClass())) {
							valueToReturn = new AtomicInteger(((Enum) returnedValue).ordinal());
							
						} else if (Date.class.isAssignableFrom(returnedValue.getClass())) {
							valueToReturn = new AtomicInteger((int)((Date) returnedValue).getTime());
							
						} else {
							valueToReturn = new AtomicInteger(Integer.parseInt(NumberUtil.removeTrailingDecimalZeros(returnedValue)));
						}
						
						return valueToReturn; 

						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					valueToReturn = new AtomicInteger(Integer.parseInt(NumberUtil.removeTrailingDecimalZeros(valueToProcess)));
				}
				
				if (valueToReturn != null) {
					Long min = objectDTO.getMin();
					Long max = objectDTO.getMax();
					
					if (min != null && min > valueToReturn.longValue()) {
						return new AtomicInteger(min.intValue());
					}
		
					if (max != null && valueToReturn.longValue() > max ) {
						valueToReturn = new AtomicInteger(max.intValue());
					}
					
					return valueToReturn;
				}
	
			} catch (Exception ex) {
				//ex.printStackTrace();
			}
		
		}
		
		return json2AtomicIntegerDefault(objectDTO);
	}


	private <E,R> String atomicInteger2Json(FieldData objectDTO) {
		if (objectDTO == null || objectDTO.json2Java) {
			return null;
		}
		
		E value = (E) objectDTO.valueToProcess;
		Class<R> returnType = objectDTO.returnType;

		if (value != null && value.toString().trim().length() > 0) {
			AtomicInteger valueToProcess = null;
			String valueToReturn = null;
			
			if (value instanceof AtomicInteger) {
				valueToProcess = (AtomicInteger)value;
			} else {
				try {
					valueToProcess = new AtomicInteger(Integer.parseInt(NumberUtil.removeTrailingDecimalZeros(value)));
				} catch (Exception ex) {}
			}
			
			if (valueToProcess != null) {
				try {
					Function function = objectDTO.getSerializer();
					if (function != null) {
						try {
							if (function instanceof DataMapper2JsonFunction) {
								DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
								return ((DataMapper2JsonFunction)function).apply(classData);
								
							} else if (function instanceof AtomicInteger2JsonFunction) {
								return ((AtomicInteger2JsonFunction)function).apply(valueToProcess);
								
							} else {
								Object returnedValue = null;
								if (function instanceof FieldData2JsonFunction) {
									FieldData2JsonFunction f = (FieldData2JsonFunction)function;
									FieldData fieldData = objectDTO.clone();
									returnedValue = f.apply(fieldData);
								} else {
									returnedValue = function.apply(value);
								}
							
								if (returnedValue instanceof Optional) {
									returnedValue = ObjectUtil.unwrap(returnedValue);
								}
								
								if (returnedValue == null) {
									return null;
									
								} else if (returnedValue instanceof AtomicInteger) {
									valueToProcess = (AtomicInteger) returnedValue;
									
								} else {
									objectDTO.valueToProcess = returnedValue;
									return object2String(objectDTO);
								}
								
							}
							
						} catch (Exception e) {}
					}

					if (valueToProcess != null) {
						Long min = objectDTO.getMin();
						Long max = objectDTO.getMax();
						
						if (min != null && min > valueToProcess.longValue()) {
							valueToProcess = new AtomicInteger(min.intValue());
						}
						
						if (max != null && max < valueToProcess.longValue()) {
							valueToProcess = new AtomicInteger(max.intValue());
						}
						
						Integer precision = objectDTO.getPrecision();
						if (precision != null) {
							valueToProcess = (AtomicInteger) NumberUtil.setPrecision(valueToProcess, precision, getRoundingMode());
						}
						
						return NumberUtil.toPlainString(valueToProcess);
					}

				} catch (Exception ex) {
					//ex.printStackTrace();
				}
			}
		}
		
		return atomicInteger2JsonDefault(objectDTO);
	}
		
	private String atomicInteger2JsonDefault(FieldData objectDTO) {
		AtomicInteger valueToReturn = json2AtomicIntegerDefault(objectDTO);
		
		if (valueToReturn == null) {
			return null;
		}
		
		return valueToReturn.toString();
	}
		
	private <E,R> AtomicInteger json2AtomicIntegerDefault(FieldData objectDTO) {
		E value = (E) objectDTO.valueToProcess;
		Class<R> returnType = objectDTO.returnType;
		boolean required = objectDTO.required();
		
		Long min = objectDTO.getMin();
		Long max = objectDTO.getMax();
		
		if (getDefaultType() == JSON_INCLUDE.DEFAULT || required) {
			AtomicInteger defaultValue = (AtomicInteger)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				if (min != null && min > defaultValue.longValue()) {
					return new AtomicInteger(min.intValue());
				}

				if (max != null && max < defaultValue.longValue()) {
					return new AtomicInteger(max.intValue());
				}				
				
				return defaultValue;
			}

			if (min != null && min > DefaultValue.atomicInteger.longValue()) {
				return new AtomicInteger(min.intValue());
			}

			return DefaultValue.atomicInteger;
		}

		return null;
	}
	
	
	private <E,R> AtomicLong json2AtomicLong(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		E value = (E) objectDTO.valueToProcess;
		Class<R> returnType = objectDTO.returnType;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			AtomicLong valueToReturn = null;
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						Object returnedValue = null;

						if (function instanceof Json2DataMapperFunction) {
							DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
							returnedValue = ((Json2DataMapperFunction)function).apply(classData);

						} else if (function instanceof Json2FieldDataFunction) {
							Json2FieldDataFunction f = (Json2FieldDataFunction)function;
							FieldData fieldData = objectDTO.clone();
							
							returnedValue = f.apply(fieldData);
							
						} else if (function instanceof Json2AtomicLongFunction) {
							return ((Json2AtomicLongFunction)function).apply(valueToProcess);
						} else {
							returnedValue = function.apply(valueToProcess);
						}

						if (returnedValue instanceof Optional) {
							returnedValue = ObjectUtil.unwrap(returnedValue);
						}
						
						if (returnedValue == null) {
							return null;
							
						} else if (Number.class.isAssignableFrom(returnedValue.getClass()) || returnedValue.getClass().isPrimitive()) {
							
							if (returnedValue instanceof AtomicLong) {
								valueToReturn = (AtomicLong) returnedValue;
							} else if (returnedValue instanceof String) {
								valueToReturn = new AtomicLong(Long.parseLong(NumberUtil.removeTrailingDecimalZeros(returnedValue)));
								
							} else if (returnedValue instanceof Integer) {
								valueToReturn = new AtomicLong((Integer) returnedValue);
							} else if (returnedValue instanceof Long) {
								valueToReturn = new AtomicLong(((Long) returnedValue).longValue());
							} else if (returnedValue instanceof Short) {
								valueToReturn = new AtomicLong((Short) returnedValue);
							} else if (returnedValue instanceof Double) {
								valueToReturn = new AtomicLong(((Double) returnedValue).longValue());
							} else if (returnedValue instanceof Float) {
								valueToReturn = new AtomicLong(((Float) returnedValue).intValue());
							} else if (returnedValue instanceof BigDecimal) {
								valueToReturn = new AtomicLong(((BigDecimal) returnedValue).longValue());
							} else if (returnedValue instanceof Byte) {
								valueToReturn = new AtomicLong((Byte) returnedValue);
							} else if (returnedValue instanceof BigInteger) {
								valueToReturn = new AtomicLong(((BigInteger) returnedValue).longValue());
							} else if (returnedValue instanceof AtomicInteger) {
								valueToReturn = new AtomicLong(((AtomicInteger) returnedValue).intValue());
							} else {
								valueToReturn = new AtomicLong(((Number) returnedValue).longValue());
							}
							
						} else if (returnedValue instanceof Character) {
							valueToReturn = new AtomicLong(((Character) returnedValue));
							
						} else if (returnedValue instanceof Boolean) {
							if ((Boolean) returnedValue)
								valueToReturn = new AtomicLong(1);
							else
								valueToReturn = new AtomicLong(0);
							
						} else if (Enum.class.isAssignableFrom(returnedValue.getClass())) {
							valueToReturn = new AtomicLong(((Enum) returnedValue).ordinal());
							
						} else if (Date.class.isAssignableFrom(returnedValue.getClass())) {
							valueToReturn = new AtomicLong(((Date) returnedValue).getTime());
							
						} else {
							valueToReturn = new AtomicLong(Long.parseLong(NumberUtil.removeTrailingDecimalZeros(returnedValue)));
						}

						return valueToReturn;
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					valueToReturn = new AtomicLong(Long.parseLong(NumberUtil.removeTrailingDecimalZeros(valueToProcess)));
				}
				
				if (valueToReturn != null) {
					Long min = objectDTO.getMin();
					Long max = objectDTO.getMax();
					
					if (min != null && min > valueToReturn.longValue()) {
						return new AtomicLong(min);
					}
		
					if (max != null && valueToReturn.longValue() > max ) {
						valueToReturn = new AtomicLong(max);
					}
					
					return valueToReturn;
				}
	
			} catch (Exception ex) {
				//ex.printStackTrace();
			}
		
		}
		
		return json2AtomicLongDefault(objectDTO);
	}


	private <E,R> String atomicLong2Json(FieldData objectDTO) {
		if (objectDTO == null || objectDTO.json2Java) {
			return null;
		}
		
		E value = (E) objectDTO.valueToProcess;
		Class<R> returnType = objectDTO.returnType;

		if (value != null && value.toString().trim().length() > 0) {
			AtomicLong valueToProcess = null;
			String valueToReturn = null;
			
			if (value instanceof AtomicLong) {
				valueToProcess = (AtomicLong)value;
			} else {
				try {
					valueToProcess = new AtomicLong(Long.parseLong(NumberUtil.removeTrailingDecimalZeros(value)));
				} catch (Exception ex) {}
			}
			
			if (valueToProcess != null) {
				try {
					Function function = objectDTO.getSerializer();
					if (function != null) {
						try {
							if (function instanceof DataMapper2JsonFunction) {
								DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
								return ((DataMapper2JsonFunction)function).apply(classData);
								
							} else if (function instanceof AtomicLong2JsonFunction) {
								return ((AtomicLong2JsonFunction)function).apply(valueToProcess);
								
							} else {
								Object returnedValue = null;
								if (function instanceof FieldData2JsonFunction) {
									FieldData2JsonFunction f = (FieldData2JsonFunction)function;
									FieldData fieldData = objectDTO.clone();
									returnedValue = f.apply(fieldData);
								} else {
									returnedValue = function.apply(value);
								}
							
								if (returnedValue instanceof Optional) {
									returnedValue = ObjectUtil.unwrap(returnedValue);
								}
								
								if (returnedValue == null) {
									return null;
									
								} else if (returnedValue instanceof AtomicLong) {
									valueToProcess = (AtomicLong) returnedValue;
									
								} else {
									objectDTO.valueToProcess = returnedValue;
									return object2String(objectDTO);
								}
								
							}
							
						} catch (Exception e) {}
					}

					if (valueToProcess != null) {
						Long min = objectDTO.getMin();
						Long max = objectDTO.getMax();
						
						if (min != null && min > valueToProcess.longValue()) {
							valueToProcess = new AtomicLong(min);
						}
						
						if (max != null && max < valueToProcess.longValue()) {
							valueToProcess = new AtomicLong(max);
						}
						
						Integer precision = objectDTO.getPrecision();
						if (precision != null) {
							valueToProcess = (AtomicLong) NumberUtil.setPrecision(valueToProcess, precision, getRoundingMode());
						}
						
						return NumberUtil.toPlainString(valueToProcess);
					}

				} catch (Exception ex) {
					//ex.printStackTrace();
				}
			}
		}
		
		return atomicLong2JsonDefault(objectDTO);
	}
		
	private String atomicLong2JsonDefault(FieldData objectDTO) {
		AtomicLong valueToReturn = json2AtomicLongDefault(objectDTO);
		
		if (valueToReturn == null) {
			return null;
		}
		
		return valueToReturn.toString();
	}
		
	private <E,R> AtomicLong json2AtomicLongDefault(FieldData objectDTO) {
		E value = (E) objectDTO.valueToProcess;
		Class<R> returnType = objectDTO.returnType;
		boolean required = objectDTO.required();
		
		Long min = objectDTO.getMin();
		Long max = objectDTO.getMax();
		
		if (getDefaultType() == JSON_INCLUDE.DEFAULT || required) {
			AtomicLong defaultValue = (AtomicLong)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				if (min != null && min > defaultValue.longValue()) {
					return new AtomicLong(min);
				}

				if (max != null && max < defaultValue.longValue()) {
					return new AtomicLong(max);
				}				
				
				return defaultValue;
			}

			if (min != null && min > DefaultValue.atomicLong.longValue()) {
				return new AtomicLong(min);
			}

			return DefaultValue.atomicLong;
		}

		return null;
	}
	
	
	private <E,R> String long2Json(FieldData objectDTO) {
		if (objectDTO == null || objectDTO.json2Java) {
			return null;
		}
		
		E value = (E) objectDTO.valueToProcess;
		Class<R> returnType = objectDTO.returnType;

		if (returnType != null && value != null && (returnType == long.class || returnType == Long.class)) {
			Long valueToProcess = null;
			
			if (returnType == long.class) {
				valueToProcess = Long.valueOf((Long)value);
			} else {
				valueToProcess = (Long)value;
			}
			
			if (valueToProcess != null) {
				try {
					
					Function function = objectDTO.getSerializer();
					if (function != null) {
						if (function instanceof DataMapper2JsonFunction) {
							DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
							return ((DataMapper2JsonFunction)function).apply(classData);
							
						} else if (function instanceof Long2JsonFunction) {
							return ((Long2JsonFunction)function).apply(valueToProcess);
							
						} else {
							
							Object returnedValue = function.apply(valueToProcess);
							
							if (returnedValue instanceof Optional) {
								returnedValue = ObjectUtil.unwrap(returnedValue);
							}
						
							if (returnedValue == null) {
								return null;
								
							} else if (returnedValue instanceof Long) {
								valueToProcess = (Long) returnedValue;
								
							} else {
								objectDTO.valueToProcess = returnedValue;
								return object2String(objectDTO);
							}
							
						}

					}

					if (valueToProcess != null) {
						Long min = objectDTO.getMin();
						Long max = objectDTO.getMax();
						
						if (min != null && min > valueToProcess.longValue()) {
							valueToProcess = min.longValue();
						}
						
						if (max != null && valueToProcess.compareTo(Long.valueOf(max)) > 0) {
							valueToProcess = Long.valueOf(max);
						}
						
						Integer precision = objectDTO.getPrecision();
						if (precision != null) {
							valueToProcess = (Long) NumberUtil.setPrecision(valueToProcess, precision, getRoundingMode());
						}
						
						return NumberUtil.toPlainString(valueToProcess);
					}

				} catch (Exception ex) {
					// ex.printStackTrace();
				}
			}
		}
		
		return long2JsonDefault(objectDTO);
	}
	

	private <E,R> Long json2Long(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		E value = (E) objectDTO.valueToProcess;
		Class<R> returnType = objectDTO.returnType;

		Long valueToReturn = null;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						Object returnedValue = null;

						if (function instanceof Json2DataMapperFunction) {
							DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
							returnedValue = ((Json2DataMapperFunction)function).apply(classData);

						} else if (function instanceof Json2FieldDataFunction) {
							Json2FieldDataFunction f = (Json2FieldDataFunction)function;
							FieldData fieldData = objectDTO.clone();
							
							returnedValue = f.apply(fieldData);
							
						} else if (function instanceof Json2LongFunction) {
							return ((Json2LongFunction)function).apply(valueToProcess);
						} else {
							returnedValue = function.apply(valueToProcess);
						}

						if (returnedValue instanceof Optional) {
							returnedValue = ObjectUtil.unwrap(returnedValue);
						}
							
						if (returnedValue == null) {
							return null;
							
							//  || returnedValue.getClass().isPrimitive()
						} else if (Number.class.isAssignableFrom(returnedValue.getClass())) {
							
							if (returnedValue instanceof Long) {
								valueToReturn = (Long) returnedValue;
								
							} else if (returnedValue instanceof String) {
								valueToReturn = Long.parseLong(NumberUtil.removeTrailingDecimalZeros(returnedValue));
							//  byte, double, float, int, long, and short. 
							} else if (returnedValue instanceof Integer) {
								valueToReturn = ((Integer) returnedValue).longValue();
							} else if (returnedValue instanceof Double) {
								valueToReturn = ((Double) returnedValue).longValue();
							} else if (returnedValue instanceof Float) {
								valueToReturn = ((Float) returnedValue).longValue();
							} else if (returnedValue instanceof Byte) {
								valueToReturn = ((Byte) returnedValue).longValue();
							} else if (returnedValue instanceof Short) {
								valueToReturn = ((Short) returnedValue).longValue();
							} else if (returnedValue instanceof BigInteger) {
								valueToReturn = ((BigInteger) returnedValue).longValue();
							} else if (returnedValue instanceof BigDecimal) {
								valueToReturn = ((BigDecimal) returnedValue).longValue();
							} else if (returnedValue instanceof AtomicInteger) {
								valueToReturn = ((AtomicInteger) returnedValue).longValue();
							} else if (returnedValue instanceof AtomicLong) {
								valueToReturn = ((AtomicLong) returnedValue).longValue();
							} else {
								valueToReturn = ((Number) returnedValue).longValue();
							}

						} else if (returnedValue instanceof Character) {
							valueToReturn = Long.valueOf((Character.getNumericValue((Character) returnedValue)));
							
						} else if (Enum.class.isAssignableFrom(returnedValue.getClass())) {
							valueToReturn = Long.valueOf(((Enum) returnedValue).ordinal());
							
						} else if (returnedValue instanceof Boolean) {
							if ((Boolean) returnedValue)
								valueToReturn = 1l;
							else
								valueToReturn = 0l;
							
						} else if (Date.class.isAssignableFrom(returnedValue.getClass())) {
							valueToReturn = ((Date) returnedValue).getTime();
							
						} else {
							valueToReturn = Long.parseLong(NumberUtil.removeTrailingDecimalZeros(returnedValue));
						}
						
						return valueToReturn;
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					valueToReturn = Long.parseLong(NumberUtil.removeTrailingDecimalZeros(valueToProcess));
				}
				
				if (valueToReturn != null) {
					Long min = objectDTO.getMin();
					Long max = objectDTO.getMax();
					if (min != null && min > valueToReturn.longValue()) {
						return min.longValue();
					}
					
					if (max != null && valueToReturn.compareTo(Long.valueOf(max)) > 0) {
						valueToReturn = Long.valueOf(max);
					}
					
					return valueToReturn;
				}
	
			} catch (Exception ex) {
				//ex.printStackTrace();
			}
		
		}
		
		return json2LongDefault(objectDTO);
	}
	
	
	private String long2JsonDefault(FieldData objectDTO) {
		Long value = json2LongDefault(objectDTO);
		
		if (value == null) {
			return null;
		}
		
		return value.toString();
	}
	
	private <E,R> Long json2LongDefault(FieldData objectDTO) {
		E value = (E) objectDTO.valueToProcess;
		Class<R> returnType = objectDTO.returnType;
		boolean required = objectDTO.required();
		
		Long min = objectDTO.getMin();
		Long max = objectDTO.getMax();
		
		if (returnType == long.class
				|| getDefaultType() == JSON_INCLUDE.DEFAULT || required) {
			Long defaultValue = (Long)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				if (min != null && min > defaultValue.longValue()) {
					return min.longValue();
				}

				return defaultValue;
			}


			if (min != null && min > DefaultValue.dlong) {
				return min.longValue();
			}

			return DefaultValue.dlong;
		}

		return null;
	}
	
	private <E,R> R json2Integer(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		E value = (E) objectDTO.valueToProcess;
		Class<R> returnType = objectDTO.returnType;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			Integer valueToReturn = null;
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						Object returnedValue = null;

						if (function instanceof Json2DataMapperFunction) {
							DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
							returnedValue = ((Json2DataMapperFunction)function).apply(classData);

						} else if (function instanceof Json2FieldDataFunction) {
							Json2FieldDataFunction f = (Json2FieldDataFunction)function;
							FieldData fieldData = objectDTO.clone();
							
							returnedValue = f.apply(fieldData);
							
						} else if (function instanceof Json2IntegerFunction) {
							return (R) ((Json2IntegerFunction)function).apply(valueToProcess);
						} else {
							returnedValue = function.apply(valueToProcess);
						}

						if (returnedValue instanceof Optional) {
							returnedValue = ObjectUtil.unwrap(returnedValue);
						}
						
						if (returnedValue == null) {
							return null;
							
						} else if (Number.class.isAssignableFrom(returnedValue.getClass()) || returnedValue.getClass().isPrimitive()) {
							
							if (returnedValue instanceof Integer) {
								valueToReturn = (Integer) returnedValue;
							} else if (returnedValue instanceof String) {
								valueToReturn = Integer.parseInt(NumberUtil.removeTrailingDecimalZeros(returnedValue));
							//  byte, double, float, int, long, and short. 
							} else if (returnedValue instanceof Long) {
								valueToReturn = ((Long) returnedValue).intValue();
							} else if (returnedValue instanceof Byte) {
								valueToReturn = ((Byte) returnedValue).intValue();
							} else if (returnedValue instanceof Short) {
								valueToReturn = ((Short) returnedValue).intValue();
							} else if (returnedValue instanceof Float) {
								valueToReturn = ((Float) returnedValue).intValue();
							} else if (returnedValue instanceof Double) {
								valueToReturn = ((Double) returnedValue).intValue();
							} else if (returnedValue instanceof BigInteger) {
								valueToReturn = ((BigInteger) returnedValue).intValue();
							} else if (returnedValue instanceof BigDecimal) {
								valueToReturn = ((BigDecimal) returnedValue).intValue();
							} else if (returnedValue instanceof AtomicInteger) {
								valueToReturn = ((AtomicInteger) returnedValue).intValue();
							} else if (returnedValue instanceof AtomicLong) {
								valueToReturn = ((AtomicLong) returnedValue).intValue();
							} else {
								valueToReturn = ((Number) returnedValue).intValue();
							}
							
						} else if (returnedValue instanceof Character) {
							valueToReturn = Character.getNumericValue((Character) returnedValue);
							
						} else if (returnedValue instanceof Boolean) {
							if ((Boolean) returnedValue)
								valueToReturn = 1;
							else
								valueToReturn = 0;
							
						} else if (Enum.class.isAssignableFrom(returnedValue.getClass())) {
							valueToReturn = ((Enum) returnedValue).ordinal();
							
						} else if (Date.class.isAssignableFrom(returnedValue.getClass())) {
							valueToReturn = (int) ((Date) returnedValue).getTime();
							
						} else {
							valueToReturn = Integer.parseInt(NumberUtil.removeTrailingDecimalZeros(returnedValue));
						}
							
						return (R) valueToReturn;
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					valueToReturn = Integer.parseInt(NumberUtil.removeTrailingDecimalZeros(valueToProcess));
				}
				
				if (valueToReturn != null) {
					Long min = objectDTO.getMin();
					Long max = objectDTO.getMax();
					if (min != null && min > valueToReturn.longValue()) {
						return (R)(Integer)min.intValue();
					}
					
					if (max != null && valueToReturn.longValue() > max) {
						valueToReturn = Integer.valueOf(max.intValue());
					}

					return (R)valueToReturn;
				}
	
			} catch (Exception ex) {
				//ex.printStackTrace();
			}
		
		}
		
		return json2IntegerDefault(objectDTO);
	}
	
	


	private <E> String integer2Json(FieldData objectDTO) {
		if (objectDTO == null || objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;

		if (value != null && returnType != null && (returnType == int.class || returnType == Integer.class)) {
			Integer valueToProcess = null;
			String valueToReturn = null;
			
			if (returnType == int.class) {
				valueToProcess = Integer.valueOf((int)value);
			} else {
				valueToProcess = (Integer)value;
			}
			
			if (valueToProcess != null) {
				try {
					Function function = objectDTO.getSerializer();
					if (function != null) {
						try {
							if (function instanceof DataMapper2JsonFunction) {
								DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
								return ((DataMapper2JsonFunction)function).apply(classData);
								
							} else if (function instanceof Integer2JsonFunction) {
								return ((Integer2JsonFunction)function).apply(valueToProcess);
								
							} else {
								
								Object returnedValue = function.apply(valueToProcess);
							
								if (returnedValue instanceof Optional) {
									returnedValue = ObjectUtil.unwrap(returnedValue);
								}
								
								if (returnedValue == null) {
									return integer2JsonDefault(objectDTO);
									
								} else if (returnedValue instanceof Integer) {
									valueToProcess = (Integer) returnedValue;
									
								} else {
									objectDTO.valueToProcess = returnedValue;
									return object2String(objectDTO);
								}
								
							}
							
						} catch (Exception e) {}
					}

					if (valueToProcess != null) {
						Long min = objectDTO.getMin();
						Long max = objectDTO.getMax();
						
						if (min != null && min > valueToProcess.longValue()) {
							valueToProcess = min.intValue();
						}
						
						if (max != null && valueToProcess > max.longValue()) {
							valueToProcess = max.intValue();
						}
						
						return NumberUtil.toPlainString(valueToProcess);
					}

				} catch (Exception ex) {
					//ex.printStackTrace();
				}
			}
		}
		
		return integer2JsonDefault(objectDTO);
	}
		
	private String integer2JsonDefault(FieldData objectDTO) {
		Integer valueToReturn = json2IntegerDefault(objectDTO);
		
		if (valueToReturn == null) {
			return null;
		}
		
		return valueToReturn.toString();
	}
		
	private <E,R> R json2IntegerDefault(FieldData objectDTO) {
		E value = (E) objectDTO.valueToProcess;
		Class<R> returnType = objectDTO.returnType;
		boolean required = objectDTO.required();

		if (returnType == int.class
				|| getDefaultType() == JSON_INCLUDE.DEFAULT || required) {
			Integer defaultValue = (Integer)objectDTO.getDefaultValue();
			Long min = objectDTO.getMin();
			Long max = objectDTO.getMax();
			if (defaultValue != null) {
				if (min != null && min > defaultValue.longValue()) {
					return (R)(Integer)min.intValue();
				}

				return (R)defaultValue;
			}

			if (min != null && min > DefaultValue.integer) {
				return (R)(Integer)min.intValue();
			}

			return (R)DefaultValue.integer;
		}

		return null;
	}

	private <E> Byte json2Byte(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			Byte valueToReturn = null;
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						Object returnedValue = null;

						if (function instanceof Json2DataMapperFunction) {
							DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
							returnedValue = ((Json2DataMapperFunction)function).apply(classData);

						} else if (function instanceof Json2FieldDataFunction) {
							Json2FieldDataFunction f = (Json2FieldDataFunction)function;
							FieldData fieldData = objectDTO.clone();
							
							returnedValue = f.apply(fieldData);
							
						} else if (function instanceof Json2ByteFunction) {
							return ((Json2ByteFunction)function).apply(valueToProcess);
						} else {
							returnedValue = function.apply(valueToProcess);
						}

						if (returnedValue instanceof Optional) {
							returnedValue = ObjectUtil.unwrap(returnedValue);
						}
						
						if (returnedValue == null) {
							return null;
							
						} else if (Number.class.isAssignableFrom(returnedValue.getClass()) || returnedValue.getClass().isPrimitive()) {
							
							if (returnedValue instanceof Byte) {
								valueToReturn = (Byte) returnedValue;
							} else if (returnedValue instanceof String) {
								valueToReturn = Byte.parseByte(NumberUtil.removeTrailingDecimalZeros(returnedValue));
								
							} else if (returnedValue instanceof Integer) {
								valueToReturn = ((Integer) returnedValue).byteValue();
							} else if (returnedValue instanceof Long) {
								valueToReturn = ((Long) returnedValue).byteValue();
							} else if (returnedValue instanceof Short) {
								valueToReturn = ((Short) returnedValue).byteValue();
							} else if (returnedValue instanceof Double) {
								valueToReturn = ((Double) returnedValue).byteValue();
							} else if (returnedValue instanceof Float) {
								valueToReturn = ((Float) returnedValue).byteValue();
							} else if (returnedValue instanceof BigInteger) {
								valueToReturn = ((BigInteger) returnedValue).byteValue();
							} else if (returnedValue instanceof BigDecimal) {
								valueToReturn = ((BigDecimal) returnedValue).byteValue();
							} else if (returnedValue instanceof AtomicInteger) {
								valueToReturn = ((AtomicInteger) returnedValue).byteValue();
							} else if (returnedValue instanceof AtomicLong) {
								valueToReturn = ((AtomicLong) returnedValue).byteValue();
							} else {
								valueToReturn = ((Number) returnedValue).byteValue();
							}
							
						} else if (returnedValue instanceof Character) {
							char c = (Character) returnedValue;
							valueToReturn = (byte)c;
							
						} else if (returnedValue instanceof Boolean) {
							if ((Boolean) returnedValue)
								valueToReturn = 1;
							else
								valueToReturn = 0;
							
						} else if (Enum.class.isAssignableFrom(returnedValue.getClass())) {
							valueToReturn = ((Integer)((Enum) returnedValue).ordinal()).byteValue();
							
						} else if (Date.class.isAssignableFrom(returnedValue.getClass())) {
							valueToReturn = (byte) ((Date) returnedValue).getTime();
							
						} else {
							valueToReturn = Byte.parseByte(NumberUtil.removeTrailingDecimalZeros(returnedValue));
						}
							
						return valueToReturn;
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					long longValue = Long.parseLong(NumberUtil.removeTrailingDecimalZeros(valueToProcess));
					
					if (longValue > Byte.MAX_VALUE) {
						valueToReturn = Byte.MAX_VALUE;
					} else {
						valueToReturn = (byte)longValue;
					}
					
					// valueToReturn = Byte.parseByte(valueToProcess);
				}
				
				if (valueToReturn != null) {
					Long min = objectDTO.getMin();
					Long max = objectDTO.getMax();
					
					if (min != null && min.byteValue() > valueToReturn) {
						return min.byteValue();
					}
					
					if (max != null && max.byteValue() < valueToReturn) {
						valueToReturn = max.byteValue();
					}

					return valueToReturn;
				}
	
			} catch (Exception ex) {
				//ex.printStackTrace();
			}
		
		}
		
		return json2ByteDefault(objectDTO);
	}
	
	


	private <E> String byte2Json(FieldData objectDTO) {
		if (objectDTO == null || objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;

		if (returnType != null && value != null && (returnType == byte.class || returnType == Byte.class)) {
			Byte valueToProcess = null;
			String valueToReturn = null;
			
			if (returnType == byte.class) {
				valueToProcess = Byte.valueOf((byte)value);
			} else {
				valueToProcess = (Byte)value;
			}
			
			if (valueToProcess != null) {
				try {
					Function function = objectDTO.getSerializer();
					if (function != null) {
						try {
							if (function instanceof DataMapper2JsonFunction) {
								DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
								return ((DataMapper2JsonFunction)function).apply(classData);
								
							} else if (function instanceof Byte2JsonFunction) {
								return ((Byte2JsonFunction)function).apply(valueToProcess);
								
							} else {
								
								Object returnedValue = null;
								if (function instanceof FieldData2JsonFunction) {
									FieldData2JsonFunction f = (FieldData2JsonFunction)function;
									FieldData fieldData = objectDTO.clone();
									returnedValue = f.apply(fieldData);
								} else {
									returnedValue = function.apply(value);
								}
								
								if (returnedValue instanceof Optional) {
									returnedValue = ObjectUtil.unwrap(returnedValue);
								}
							
								if (returnedValue == null) {
									return null;
									
								} else if (returnedValue instanceof Byte) {
									valueToProcess = (Byte) returnedValue;
									
								} else {
									objectDTO.valueToProcess = returnedValue;
									return object2String(objectDTO);
								}
								
							}
							
						} catch (Exception e) {}
					}

					if (valueToProcess != null) {
						Long min = objectDTO.getMin();
						Long max = objectDTO.getMax();
						
						if (min != null && min.byteValue() > valueToProcess) {
							valueToProcess = min.byteValue();
						}
						
						if (max != null && max.byteValue() < valueToProcess) {
							valueToProcess = max.byteValue();
						}
						
						Integer precision = objectDTO.getPrecision();
						if (precision != null) {
							valueToProcess = (Byte) NumberUtil.setPrecision(valueToProcess, precision, getRoundingMode());
						}
						
						return NumberUtil.toPlainString(valueToProcess);
					}

				} catch (Exception ex) {
					//ex.printStackTrace();
				}
			}
		}
		
		return byte2JsonDefault(objectDTO);
	}
		
	private String byte2JsonDefault(FieldData objectDTO) {
		Byte valueToReturn = json2ByteDefault(objectDTO);
		
		if (valueToReturn == null) {
			return null;
		}
		
		return valueToReturn.toString();
	}
		
	private <E> Byte json2ByteDefault(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required();
		
		Long min = objectDTO.getMin();
		Long max = objectDTO.getMax();
		boolean json2Java = objectDTO.json2Java;

		if (returnType == byte.class
				|| getDefaultType() == JSON_INCLUDE.DEFAULT || required) {
			Byte defaultValue = (Byte)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				if (min != null && min.byteValue() > defaultValue) {
					return min.byteValue();
				}

				return defaultValue;
			}

			if (min != null && min.byteValue() > DefaultValue.dbyte) {
				return min.byteValue();
			}

			return DefaultValue.dbyte;
		}

		return null;
	}
	
	private Character long2Character(long longvalue, Long min, Long max) {
		Character valueToReturn;
		
		if (min == null && longvalue < Character.MIN_CODE_POINT) {
			valueToReturn = null;
		} else if (max == null && longvalue > Character.MAX_CODE_POINT) {
			valueToReturn = null;
		} else if (min != null && min > longvalue) {
			valueToReturn = (char)min.intValue();
			
		} else if (max != null && max < longvalue) {
			valueToReturn = (char)max.intValue();
		} else  {
			valueToReturn = (char)longvalue;
		}
		
		return valueToReturn;
		
	}

	private <E> Character json2Character(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			Character valueToReturn = null;
			Long min = objectDTO.getMin();
			Long max = objectDTO.getMax();
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						Object returnedValue = null;

						if (function instanceof Json2DataMapperFunction) {
							DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
							returnedValue = ((Json2DataMapperFunction)function).apply(classData);

						} else if (function instanceof Json2FieldDataFunction) {
							Json2FieldDataFunction f = (Json2FieldDataFunction)function;
							FieldData fieldData = objectDTO.clone();
							
							returnedValue = f.apply(fieldData);
							
						} else if (function instanceof Json2CharacterFunction) {
							return ((Json2CharacterFunction)function).apply(valueToProcess);
						} else {
							returnedValue = function.apply(valueToProcess);
						}

						if (returnedValue instanceof Optional) {
							returnedValue = ObjectUtil.unwrap(returnedValue);
						}
						
						if (returnedValue == null) {
							return null;
							
						} else if (returnedValue instanceof Character) {
							return (Character) returnedValue;
							
						} else if (Number.class.isAssignableFrom(returnedValue.getClass()) || returnedValue.getClass().isPrimitive()) {
							long longvalue = ((Number) returnedValue).longValue();
							
							valueToReturn = long2Character(longvalue, min, max);

						} else if (returnedValue instanceof Boolean) {
							if ((Boolean) returnedValue)
								valueToReturn = (char)1;
							else
								valueToReturn = (char)0;
							
						} else if (Enum.class.isAssignableFrom(returnedValue.getClass())) {
							int intvalue = ((Enum) returnedValue).ordinal();
							
							valueToReturn = long2Character(intvalue, min, max);

						} else if (Date.class.isAssignableFrom(returnedValue.getClass())) {
							long longvalue = ((Date) returnedValue).getTime();
							
							valueToReturn = long2Character(longvalue, min, max);

						} else {
							long longvalue = Long.parseLong(NumberUtil.removeTrailingDecimalZeros(returnedValue));
							
							valueToReturn = long2Character(longvalue, min, max);
						}
							
						return valueToReturn;
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					if (valueToProcess.length() == 1) {
						return valueToProcess.charAt(0);
						
					} else {
						long longvalue = Long.parseLong(NumberUtil.removeTrailingDecimalZeros(valueToProcess));
						
						valueToReturn = long2Character(longvalue, min, max);
					}
				}
				
				if (valueToReturn != null) {
					return valueToReturn;
				}
	
			} catch (Exception ex) {
				//ex.printStackTrace();
			}
		}
		
		return json2CharacterDefault(objectDTO);
	}
	
	


	private <E> String character2Json(FieldData objectDTO) {
		if (objectDTO == null || objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;

		if (returnType != null && value != null && (returnType == char.class || returnType == Character.class)) {
			Character valueToProcess = (Character)value;
			String valueToReturn = null;
			
			// convert from char to Character
			if (returnType == char.class) {
				valueToProcess = Character.valueOf((char)value); // new Character(value)
			} else {
				valueToProcess = (Character)value;
			}
			
			if (valueToProcess != null) {
				try {
					Function function = objectDTO.getSerializer();
					if (function != null) {
						try {
							if (function instanceof DataMapper2JsonFunction) {
								DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
								return ((DataMapper2JsonFunction)function).apply(classData);
								
							} else if (function instanceof Character2JsonFunction) {
								return ((Character2JsonFunction)function).apply(valueToProcess);
								
							} else {
								
								Object returnedValue = null;
								if (function instanceof FieldData2JsonFunction) {
									FieldData2JsonFunction f = (FieldData2JsonFunction)function;
									FieldData fieldData = objectDTO.clone();
									returnedValue = f.apply(fieldData);
								} else {
									returnedValue = function.apply(value);
								}
							
								if (returnedValue instanceof Optional) {
									returnedValue = ObjectUtil.unwrap(returnedValue);
								}
								
								if (returnedValue == null) {
									return null;
									
								} else if (returnedValue instanceof Character || returnedValue.getClass() == char.class) {
									valueToProcess = (Character)returnedValue;
									
								} else {
									objectDTO.valueToProcess = returnedValue;
									return object2String(objectDTO);
								}
								
							}
							
						} catch (Exception e) {}
					}

					if (valueToProcess != null) {
						return Character.toString(valueToProcess); // String.valueOf(valueToProcess);
					}

				} catch (Exception ex) {
					//ex.printStackTrace();
				}
			}
		}
		
		return character2JsonDefault(objectDTO);
	}
		
	private String character2JsonDefault(FieldData objectDTO) {
		Character valueToReturn = json2CharacterDefault(objectDTO);
		
		if (valueToReturn == null) {
			return null;
		}
		
		return valueToReturn.toString();
	}
		
	private <E> Character json2CharacterDefault(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required();
		
		Long min = objectDTO.getMin();
		Long max = objectDTO.getMax();
		boolean json2Java = objectDTO.json2Java;

		if (returnType == char.class
				|| getDefaultType() == JSON_INCLUDE.DEFAULT || required) {
			Character defaultValue = (Character)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				if (min != null && min > (int)defaultValue) {
					return (char)min.intValue();
				}

				return defaultValue;
			}

			if (min != null && min > (int)DefaultValue.character) {
				return (char)min.intValue();
			}

			return DefaultValue.character;
		}

		return null;
	}


	private <E> Short json2Short(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			Short valueToReturn = null;
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						Object returnedValue = null;

						if (function instanceof Json2DataMapperFunction) {
							DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
							returnedValue = ((Json2DataMapperFunction)function).apply(classData);

						} else if (function instanceof Json2FieldDataFunction) {
							Json2FieldDataFunction f = (Json2FieldDataFunction)function;
							FieldData fieldData = objectDTO.clone();
							
							returnedValue = f.apply(fieldData);
							
						} else if (function instanceof Json2ShortFunction) {
							return ((Json2ShortFunction)function).apply(valueToProcess);
						} else {
							returnedValue = function.apply(valueToProcess);
						}

						if (returnedValue instanceof Optional) {
							returnedValue = ObjectUtil.unwrap(returnedValue);
						}
							
						if (returnedValue == null) {
							return null;
							
						} else if (Number.class.isAssignableFrom(returnedValue.getClass()) || returnedValue.getClass().isPrimitive()) {
							
							if (returnedValue instanceof Short) {
								valueToReturn = (Short) returnedValue;
							} else if (returnedValue instanceof String) {
								valueToReturn = Short.parseShort(NumberUtil.removeTrailingDecimalZeros(returnedValue));
								
							} else if (returnedValue instanceof Integer) {
								valueToReturn = ((Integer) returnedValue).shortValue();
							} else if (returnedValue instanceof Long) {
								valueToReturn = ((Long) returnedValue).shortValue();
							} else if (returnedValue instanceof Byte) {
								valueToReturn = ((Byte) returnedValue).shortValue();
							} else if (returnedValue instanceof Double) {
								valueToReturn = ((Double) returnedValue).shortValue();
							} else if (returnedValue instanceof Float) {
								valueToReturn = ((Float) returnedValue).shortValue();
							} else if (returnedValue instanceof BigInteger) {
								valueToReturn = ((BigInteger) returnedValue).shortValue();
							} else if (returnedValue instanceof BigDecimal) {
								valueToReturn = ((BigDecimal) returnedValue).shortValue();
							} else if (returnedValue instanceof AtomicInteger) {
								valueToReturn = ((AtomicInteger) returnedValue).shortValue();
							} else if (returnedValue instanceof AtomicLong) {
								valueToReturn = ((AtomicLong) returnedValue).shortValue();
							} else {
								valueToReturn = ((Number) returnedValue).shortValue();
							}
							
						} else if (returnedValue instanceof Character) {
							valueToReturn = (short)(((Character) returnedValue).charValue());
							
						} else if (returnedValue instanceof Boolean) {
							if ((Boolean) returnedValue)
								valueToReturn = 1;
							else
								valueToReturn = 0;
							
						} else if (Enum.class.isAssignableFrom(returnedValue.getClass())) {
							valueToReturn = ((Integer)((Enum) returnedValue).ordinal()).shortValue();
							
						} else if (Date.class.isAssignableFrom(returnedValue.getClass())) {
							valueToReturn = (short) ((Date) returnedValue).getTime();
							
						} else {
							valueToReturn = Short.parseShort(NumberUtil.removeTrailingDecimalZeros(returnedValue));
						}
							
						return valueToReturn;
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					long longValue = Long.parseLong(NumberUtil.removeTrailingDecimalZeros(valueToProcess));
					
					if (longValue > Short.MAX_VALUE) {
						valueToReturn = Short.MAX_VALUE;
					} else {
						valueToReturn = (short)longValue;
					}
					
					// valueToReturn = Short.parseShort(valueToProcess);
				}
				
				if (valueToReturn != null) {
					Long min = objectDTO.getMin();
					Long max = objectDTO.getMax();
					
					if (min != null && min.shortValue() > valueToReturn) {
						return min.shortValue();
					}
					
					if (max != null && max.shortValue() < valueToReturn) {
						valueToReturn = max.shortValue();
					}
					
					return valueToReturn;
				}
	
			} catch (Exception ex) {
				//ex.printStackTrace();
			}
		
		}
		
		return json2ShortDefault(objectDTO);
	}
	
	


	private <E> String short2Json(FieldData objectDTO) {
		if (objectDTO == null || objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;

		if (returnType != null && value != null && (returnType == short.class || returnType == Short.class)) {
			Short valueToProcess = null;
			String valueToReturn = null;
			
			if (returnType == short.class) {
				valueToProcess = Short.valueOf((short)value);
			} else {
				valueToProcess = (Short)value;
			}
			
			if (valueToProcess != null) {
				try {
					Function function = objectDTO.getSerializer();
					if (function != null) {
						try {
							if (function instanceof DataMapper2JsonFunction) {
								DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
								return ((DataMapper2JsonFunction)function).apply(classData);
								
							} else if (function instanceof Short2JsonFunction) {
								return ((Short2JsonFunction)function).apply(valueToProcess);
								
							} else {
								
								Object returnedValue = null;
								if (function instanceof FieldData2JsonFunction) {
									FieldData2JsonFunction f = (FieldData2JsonFunction)function;
									FieldData fieldData = objectDTO.clone();
									returnedValue = f.apply(fieldData);
								} else {
									returnedValue = function.apply(value);
								}
							
								if (returnedValue instanceof Optional) {
									returnedValue = ObjectUtil.unwrap(returnedValue);
								}
								
								if (returnedValue == null) {
									return null;
									
								} else if (returnedValue instanceof Short) {
									valueToProcess = (Short) returnedValue;
									
								} else {
									objectDTO.valueToProcess = returnedValue;
									return object2String(objectDTO);
								}
								
							}
							
						} catch (Exception e) {}
					}

					if (valueToProcess != null) {
						Long min = objectDTO.getMin();
						Long max = objectDTO.getMax();
						
						if (min != null && min.shortValue() > valueToProcess) {
							valueToProcess = min.shortValue();
						}
						
						if (max != null && max.shortValue() < valueToProcess) {
							valueToProcess = max.shortValue();
						}
						
						Integer precision = objectDTO.getPrecision();
						if (precision != null) {
							valueToProcess = (Short) NumberUtil.setPrecision(valueToProcess, precision, getRoundingMode());
						}
						
						return NumberUtil.toPlainString(valueToProcess);
					}

				} catch (Exception ex) {
					//ex.printStackTrace();
				}
			}
		}
		
		return short2JsonDefault(objectDTO);
	}
		
	private String short2JsonDefault(FieldData objectDTO) {
		Short valueToReturn = json2ShortDefault(objectDTO);
		
		if (valueToReturn == null) {
			return null;
		}
		
		return valueToReturn.toString();
	}
		
	private <E> Short json2ShortDefault(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required();
		
		Long min = objectDTO.getMin();
		Long max = objectDTO.getMax();
		boolean json2Java = objectDTO.json2Java;

		if (returnType == short.class
				|| getDefaultType() == JSON_INCLUDE.DEFAULT || required) {
			Short defaultValue = (Short)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				if (min != null && min.shortValue() > defaultValue) {
					return min.shortValue();
				}

				return defaultValue;
			}

			if (min != null && min.shortValue() > DefaultValue.dshort) {
				return min.shortValue();
			}

			return DefaultValue.dshort;
		}

		return null;
	}


	private <E> String json2String(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		
		String valueToProcess = null;
		if (value != null) {
			valueToProcess = value.toString().trim();
			
			if (valueToProcess.length() == 0) {
				return valueToProcess;
			}
		}

		if (value != null) {
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						Object returnedValue = null;

						if (function instanceof Json2DataMapperFunction) {
							DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
							returnedValue = ((Json2DataMapperFunction)function).apply(classData);

						} else if (function instanceof Json2FieldDataFunction) {
							Json2FieldDataFunction f = (Json2FieldDataFunction)function;
							FieldData fieldData = objectDTO.clone();
							
							returnedValue = f.apply(fieldData);
							
						} else if (function instanceof Json2StringFunction) {
							return ((Json2StringFunction)function).apply(valueToProcess);
						} else {
							returnedValue = function.apply(valueToProcess);
						}

						if (returnedValue instanceof Optional) {
							returnedValue = ObjectUtil.unwrap(returnedValue);
						}
							
						if (returnedValue == null) {
							return null;
							
						} else if (returnedValue instanceof String) {
							valueToProcess = (String) returnedValue;

						} else {
							objectDTO.valueToProcess = returnedValue;
							valueToProcess = object2String(objectDTO);
						}
							
						return valueToProcess;
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				}
				
				if (valueToProcess != null) {
					Integer length = objectDTO.getLength();

					if (length != null && length < valueToProcess.length()) {
						valueToProcess = valueToProcess.substring(0, length);
					}
					
					return StringUtil.unquote(valueToProcess, isEscapeHtml());
				}
	
			} catch (Exception ex) {
				//ex.printStackTrace();
			}
		}
		
		return json2StringDefault(objectDTO);
	}
	
	


	private <E> String string2Json(FieldData objectDTO) {
		if (objectDTO == null || objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;

		String valueToProcess = null;
		
		if (value != null) {
			valueToProcess = value.toString().trim();
			
			if (valueToProcess.length() == 0) {
				return valueToProcess;
			}
		}
		
		if (valueToProcess != null) {
			try {
				Function function = objectDTO.getSerializer();
				if (function != null) {
					try {
						if (function instanceof DataMapper2JsonFunction) {
							DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
							return ((DataMapper2JsonFunction)function).apply(classData);
							
						} else if (function instanceof String2JsonFunction) {
							valueToProcess = ((String2JsonFunction)function).apply(valueToProcess);
							
						} else {
							
							Object returnedValue = null;
							if (function instanceof FieldData2JsonFunction) {
								FieldData2JsonFunction f = (FieldData2JsonFunction)function;
								FieldData fieldData = objectDTO.clone();
								returnedValue = f.apply(fieldData);
							} else {
								returnedValue = function.apply(value);
							}
						
							if (returnedValue == null) {
								return null;
								
							} else if (returnedValue instanceof String) {
								valueToProcess = (String)returnedValue;
								
							} else {
								objectDTO.valueToProcess = returnedValue;
								valueToProcess = object2String(objectDTO);
							}
							
						}
						
					} catch (Exception e) {}
				}

				if (valueToProcess != null) {
					if ( isMapListArrayObject(valueToProcess) ) {
						return valueToProcess;
					}
					
					Integer length = objectDTO.getLength();

					if (length != null && length < valueToProcess.length()) {
						valueToProcess = valueToProcess.substring(0, length);
					}
					
					return valueToProcess;
				}

			} catch (Exception ex) {
				//ex.printStackTrace();
			}
		}
		
		return string2JsonDefault(objectDTO);
	}
		
	private String string2JsonDefault(FieldData objectDTO) {
		String valueToReturn = json2StringDefault(objectDTO);
		
		if (valueToReturn == null) {
			return null;
		}
		
		return valueToReturn.toString();
	}
		
	private <E> String json2StringDefault(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required();
		
		Long min = objectDTO.getMin();
		Long max = objectDTO.getMax();
		boolean json2Java = objectDTO.json2Java;

		if (returnType == char.class
				|| getDefaultType() == JSON_INCLUDE.DEFAULT || required) {
			String defaultValue = (String)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				return defaultValue;
			}

			return DefaultValue.string;
		}

		return null;
	}


	private <E> Object json2Boolean(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			Boolean valueToReturn = null;
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						Object returnedValue = null;

						if (function instanceof Json2DataMapperFunction) {
							DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
							returnedValue = ((Json2DataMapperFunction)function).apply(classData);

						} else if (function instanceof Json2FieldDataFunction) {
							Json2FieldDataFunction f = (Json2FieldDataFunction)function;
							FieldData fieldData = objectDTO.clone();
							
							returnedValue = f.apply(fieldData);
							
						} else if (function instanceof Json2BooleanFunction) {
							return ((Json2BooleanFunction)function).apply(valueToProcess);
						} else {
							returnedValue = function.apply(valueToProcess);
						}

						if (returnedValue instanceof Optional) {
							returnedValue = ObjectUtil.unwrap(returnedValue);
						}
						
						if (returnedValue == null) {
							return json2BooleanDefault(objectDTO);
							
						} else if (returnedValue instanceof Boolean || returnedValue.getClass() == boolean.class) {
							valueToReturn = (Boolean) returnedValue;
							
						} else if (returnedValue instanceof Character || returnedValue.getClass() == char.class) {
							char c = (Character) returnedValue;
							
							valueToReturn = BooleanUtil.char2Boolean((char)returnedValue);
							
						} else if (returnedValue instanceof String) {
							valueToReturn = BooleanUtil.string2Boolean((String)returnedValue);
							
						} else if (DefaultValue.isDefault(returnedValue, returnedValue.getClass())) {
							valueToReturn = false;
						} else {
							valueToReturn = BooleanUtil.string2Boolean(returnedValue.toString());
						}
							
						// return valueToReturn;
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					valueToReturn = BooleanUtil.string2Boolean(valueToProcess);
				}
				
				if (returnType == AtomicBoolean.class) {
					return new AtomicBoolean(valueToReturn);
				}
				
				if (valueToReturn == null) {
					return json2BooleanDefault(objectDTO);
				}

				return valueToReturn;

			} catch (Exception ex) {
				//ex.printStackTrace();
			}
		
		}
		
		if (returnType == AtomicBoolean.class && (getDefaultType() == JSON_INCLUDE.DEFAULT || objectDTO.required)) {
			return new AtomicBoolean();
		}
		
		return json2BooleanDefault(objectDTO);
	}
	
	
	
	private <E> String boolean2Json(FieldData objectDTO) {
		if (objectDTO == null || objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;

		if (returnType != null && value != null && (returnType == boolean.class || returnType == Boolean.class || returnType == AtomicBoolean.class)) {
			Boolean valueToProcess = null;
			String valueToReturn = null;
			
			if (returnType == boolean.class) {
				// return String.valueOf((boolean)value);
				valueToProcess = Boolean.valueOf((boolean)value);
				
			} else if (returnType == AtomicBoolean.class) {
				valueToProcess = ((AtomicBoolean)value).get();
				
			} else {
				valueToProcess = (Boolean)value;
			}
			
			if (valueToProcess != null) {
				try {
					Function function = objectDTO.getSerializer();
					if (function != null) {
						try {
							if (function instanceof DataMapper2JsonFunction) {
								DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
								return ((DataMapper2JsonFunction)function).apply(classData);
								
							} else if (function instanceof Boolean2JsonFunction) {
								return ((Boolean2JsonFunction)function).apply(valueToProcess);
								
							} else {
								Object returnedValue = null;
								if (function instanceof FieldData2JsonFunction) {
									FieldData2JsonFunction f = (FieldData2JsonFunction)function;
									FieldData fieldData = objectDTO.clone();
									returnedValue = f.apply(fieldData);
								} else {
									returnedValue = function.apply(value);
								}
							
								if (returnedValue instanceof Optional) {
									returnedValue = ObjectUtil.unwrap(returnedValue);
								}
								
								if (returnedValue == null) {
									return null;
									
								} else if (returnedValue instanceof Boolean) {
									valueToProcess = (Boolean) returnedValue;
									
								} else {
									objectDTO.valueToProcess = returnedValue;
									return object2String(objectDTO);
								}
								
							}
							
						} catch (Exception e) {}
					}

					return Boolean.toString(valueToProcess);

				} catch (Exception ex) {
					//ex.printStackTrace();
				}
			}
		}
		
		return boolean2JsonDefault(objectDTO);
	}
		
	private String boolean2JsonDefault(FieldData objectDTO) {
		Boolean valueToReturn = json2BooleanDefault(objectDTO);
		
		if (valueToReturn == null) {
			return null;
		}
		
		return valueToReturn.toString();
	}
		
	private <E> Boolean json2BooleanDefault(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required();
		
		Long min = objectDTO.getMin();
		Long max = objectDTO.getMax();
		boolean json2Java = objectDTO.json2Java;

		if (returnType == boolean.class
				|| getDefaultType() == JSON_INCLUDE.DEFAULT || required) {
			Boolean defaultValue = (Boolean)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				return defaultValue;
			}

			return DefaultValue.bool;
		}

		return null;
	}




	// deserialize only
	private <E> Enum<?> json2Enum(FieldData objectDTO) {
		objectDTO.valueToProcess = StringUtil.unquote(objectDTO.valueToProcess, isEscapeHtml());
		Object valueToProcess = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required();
		Enum defaultValue = (Enum)objectDTO.defaultValue;
		boolean json2Java = objectDTO.json2Java;

		if (returnType == null || valueToProcess == null) {
			if (required) {
				return defaultValue;
			}
			
			return null;
		}

		String value = (String)valueToProcess;

		Class<Enum> enumType = (Class<Enum>) returnType;

		try {
			Function function = objectDTO.getDeserializer();
			
			if (function != null) {

				Object returnedValue = null;

				if (function instanceof Json2DataMapperFunction) {
					DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
					returnedValue = ((Json2DataMapperFunction)function).apply(classData);

				} else if (function instanceof Json2FieldDataFunction) {
					Json2FieldDataFunction f = (Json2FieldDataFunction)function;
					FieldData fieldData = objectDTO.clone();
					
					returnedValue = f.apply(fieldData);
					
				} else if (function instanceof Json2EnumFunction) {
					return (Enum<?>) ((Json2EnumFunction)function).apply(value);
				} else {
					returnedValue = function.apply(value);
				}
				
				if (returnedValue instanceof Optional) {
					returnedValue = ObjectUtil.unwrap(returnedValue);
				}
				
				if (returnedValue == null) {
					return null;
				}
				
				Class type = returnedValue.getClass();
				
				if (Enum.class.isAssignableFrom(type)) {
					return (Enum<?>) returnedValue;
					
				} else if (Number.class.isAssignableFrom(type)) {
					int ordinal = ((Number)returnedValue).intValue();
					
					for (Enum enumValue : enumType.getEnumConstants()) {
						if (enumValue.ordinal() == ordinal) {
							return enumValue;
						}
					}
					
				} else {
					String name = returnedValue.toString();
					
					for (Enum enumValue : enumType.getEnumConstants()) {
						if (enumValue.toString().equalsIgnoreCase(name)
								|| enumValue.name().equalsIgnoreCase(name)) {
							return enumValue;
						}
					}
				}

			}
			
		} catch (Exception ex) {
		}
		
		for (Method method: enumType.getDeclaredMethods()) {
			for (Annotation annotation: method.getDeclaredAnnotations()) {
				String aname = annotation.annotationType().getName();
				
				switch (aname) {
					case "ca.oson.json.annotation.FieldMapper":
						ca.oson.json.annotation.FieldMapper fieldMapper = (ca.oson.json.annotation.FieldMapper)annotation;
						if (fieldMapper.jsonCreator() != null && fieldMapper.jsonCreator() == BOOLEAN.TRUE) {
							return ObjectUtil.getMethodValue(null, method, value);
						}
				}
			}

		}
		
		String fieldName = null;
		for (Field field: enumType.getDeclaredFields()) {
			String name = null;
			ca.oson.json.annotation.FieldMapper fieldMapper = field.getAnnotation(ca.oson.json.annotation.FieldMapper.class);
			if (fieldMapper != null) {
				name = fieldMapper.name();
				
				if (value.equalsIgnoreCase(name)) {
					fieldName = field.getName();
					break;
				}
				
			} else {
				for (Annotation annotation: field.getAnnotations()) {
					name = ObjectUtil.getName(annotation);
					if (value.equalsIgnoreCase(name)) {
						fieldName = field.getName();
						break;
					}
				}
			}
		}
		if (fieldName != null) {
			try {
				return Enum.valueOf(enumType, fieldName.toUpperCase());
			} catch (IllegalArgumentException ex) {
			}
		}
		
		
		try {
			return Enum.valueOf(enumType, value.toUpperCase());
		} catch (IllegalArgumentException ex) {
		}

		for (Enum enumValue : enumType.getEnumConstants()) {
			if (enumValue.toString().equalsIgnoreCase(value)
					|| enumValue.name().equalsIgnoreCase(value)) {
				return enumValue;
			}
		}


		FieldData fieldData = new FieldData(value, Integer.class, true);
		Integer ordinal = json2Integer(fieldData);

		if (ordinal != null) {
			for (Enum enumValue : enumType.getEnumConstants()) {
				if (enumValue.ordinal() == ordinal) {
					return enumValue;
				}
			}
		}

		return null;
	}
	
	private synchronized void startCachedComponentTypes(ComponentType componentType) {
		this.masterClass = componentType.getClassType();
		Class[] componentClassTypes = componentType.getComponentClassType();
		cachedComponentTypes(componentType);
	}
	
	private synchronized void startCachedComponentTypes(Class classType) {
		if (!ObjectUtil.isBasicDataType(classType)) {
			this.masterClass = classType;
			cachedComponentTypes(classType);
		}
	}
	
	private ComponentType cachedComponentTypes(ComponentType componentType) {
		Class ClassType = componentType.getClassType();
		Class[] componentClassTypes = componentType.getComponentClassType();
		
		if (this.masterClass == null) {
			this.masterClass = ClassType;
		}
		if (this.masterClass == null) {
			return null;
		}
		ComponentType oldComponentType = cachedComponentTypes.get(masterClass);
		
		if (oldComponentType == null) {
			cachedComponentTypes.put(this.masterClass, componentType);
			
		} else if (componentClassTypes != null) {
			for (Class componentClassType: componentClassTypes) {
				oldComponentType.add(componentClassType);
			}
		}
		
		return oldComponentType;
	}
	
	
	private ComponentType cachedComponentTypes(Class classType) {
		if (classType == null) {
			return null;
		}
		
		if (this.masterClass == null) {
			this.masterClass = classType;
		}
		
		ComponentType oldComponentType = cachedComponentTypes.get(masterClass);
		
		if (oldComponentType == null) {
			oldComponentType = new ComponentType(classType);
			cachedComponentTypes.put(this.masterClass, oldComponentType);
		}
		
		return oldComponentType;
	}
	
	private ComponentType getComponentType() {
		if (this.masterClass == null) {
			return null;
		}
		
		return cachedComponentTypes.get(masterClass);
	}
	
	
	private <E, R> Class guessMapKeyType(FieldData newFieldData) {
		Class returnType = newFieldData.returnType;
		String toGenericString = null;
		Class fieldType = null;
		if (newFieldData.field != null) {
			toGenericString = newFieldData.field.toGenericString();
			fieldType = ObjectUtil.getComponentType(toGenericString);
		}
		if (fieldType == null && returnType != null) {
			toGenericString = returnType.toGenericString();
			fieldType = ObjectUtil.getComponentType(toGenericString);
		}
		if (fieldType == null && newFieldData.setter != null) {
			toGenericString = newFieldData.setter.toGenericString();
			fieldType = ObjectUtil.getComponentType(toGenericString);
		}
		
		ComponentType type = getComponentType();
		if (type == null) {
			Class enclosingtype = newFieldData.getEnclosingtype();
			if (enclosingtype != null) {
				type = cachedComponentTypes(enclosingtype);
			}
		}
		
		int level = newFieldData.level;

		if (fieldType != null) {
			if (type == null) {
				type = new ComponentType(newFieldData.returnType, fieldType);
				type = cachedComponentTypes(type);
				
			} else {
				type.add(fieldType);
			}

			if (newFieldData.returnType == Object.class) {
				newFieldData.returnType = fieldType;
			}
			
			return fieldType;
		}
		
		if (type != null) {
			ComponentType componentType = type.getComponentType();
			while (componentType != null && componentType.getComponentType() != null && level > 1) {
				componentType = componentType.getComponentType();
				level--;
			}
			
			if (level == 1 && componentType != null && componentType.getKeyType() != null) {
				return componentType.getKeyType();
			}
			
			return type.getKeyType();
		}
		
		return guessComponentType(newFieldData);
	}
	
	private <E, R> Class guessComponentType(FieldData newFieldData) {
		boolean json2Java = newFieldData.json2Java;
		
		if (!json2Java) {
			return newFieldData.returnType;
		} else {
			
			E obj = (E) newFieldData.valueToProcess;
			
			Class returnType = newFieldData.returnType;
			Class itemType = null;
			if (obj != null) {
				itemType = obj.getClass();
			}
			
			if (newFieldData.componentType != null) {
				Class classType = newFieldData.componentType.getClassType();
				Class cls = newFieldData.componentType.getMainComponentType();
				if (cls != null && (ObjectUtil.isBasicDataType(cls) || ObjectUtil.isSameDataType(classType, returnType)) ) {
					return cls;
				}
			}
			
			String returnTypeName = null;
			if (returnType != null) {
				returnTypeName = returnType.getName();
			}
			String itemTypeName = null;
			if (itemType != null) {
				itemTypeName = itemType.getName();
			}
			
			int returnTypeCount = 0;
			int itemTypeCount = 0;
			String toGenericString = null;
			Class fieldType = null;
			if (newFieldData.field != null) {
				toGenericString = newFieldData.field.toGenericString();
				Class[] fieldTypes = ObjectUtil.getComponentTypes(toGenericString);
				
				if (fieldTypes != null && fieldTypes.length > 0) {
					if (fieldTypes.length == 1) {
						fieldType = fieldTypes[0];
					} else {
						fieldType = fieldTypes[0];
						
						if (newFieldData.isMapValue) {
							fieldType = fieldTypes[1];
						} else {
							fieldType = fieldTypes[0];
						}
					}
				}
				
				if (returnTypeName != null && toGenericString.indexOf(returnTypeName) > -1) {
					returnTypeCount++;
				}
				if (itemTypeName != null && toGenericString.indexOf(itemTypeName) > -1) {
					itemTypeCount++;
				}
			}
			if (fieldType == null && returnType != null) {
				toGenericString = returnType.toGenericString();
				fieldType = ObjectUtil.getComponentType(toGenericString);
				if (returnTypeName != null && toGenericString.indexOf(returnTypeName) > -1) {
					returnTypeCount++;
				}
				if (itemTypeName != null && toGenericString.indexOf(itemTypeName) > -1) {
					itemTypeCount++;
				}
			}
			if (fieldType == null && newFieldData.setter != null) {
				toGenericString = newFieldData.setter.toGenericString();
				fieldType = ObjectUtil.getComponentType(toGenericString);
				if (returnTypeName != null && toGenericString.indexOf(returnTypeName) > -1) {
					returnTypeCount++;
				}
				if (itemTypeName != null && toGenericString.indexOf(itemTypeName) > -1) {
					itemTypeCount++;
				}
				
				if (fieldType == null) {
					Class[] types = newFieldData.setter.getParameterTypes();
					if (types != null && types.length > 0) {
						toGenericString = types[0].toGenericString();
						fieldType = ObjectUtil.getComponentType(toGenericString);
						if (returnTypeName != null && toGenericString.indexOf(returnTypeName) > -1) {
							returnTypeCount++;
						}
						if (itemTypeName != null && toGenericString.indexOf(itemTypeName) > -1) {
							itemTypeCount++;
						}
					}
				}
			}
			
			ComponentType type = getComponentType();
			if (type == null) {
				Class enclosingtype = newFieldData.getEnclosingtype();
				if (enclosingtype != null) {
					type = cachedComponentTypes(enclosingtype);
				}
			}
			
			int level = newFieldData.level;

			if (fieldType != null) {
				if (returnTypeCount < itemTypeCount) {
					newFieldData.returnType = itemType;
				}

				if (type == null) {
					type = new ComponentType(newFieldData.returnType, fieldType);
					type = cachedComponentTypes(type);
					
				} else {
					type.add(fieldType);
				}

				if (newFieldData.returnType == Object.class) {
					newFieldData.returnType = fieldType;
				}
				
				return fieldType;
			}

			if (returnType != null) {
				Class comptype = returnType.getComponentType();
				if ( comptype != null && !comptype.isInterface() && !Modifier.isAbstract(comptype.getModifiers()) ) {
					return comptype;
				}
			}
			

			if ( (returnType != null && Map.class.isAssignableFrom(returnType)) || (itemType != null && Map.class.isAssignableFrom(itemType))) {
				String className = ((Map<String, String>)obj).get(getJsonClassType());
				if (className != null && className.length() > 0) {
					try {
						// figure out obj's class
						fieldType = Class.forName(className);
						
						if (type == null) {
							if (newFieldData.returnType != fieldType) {
								type = new ComponentType(newFieldData.returnType, fieldType);
								type = cachedComponentTypes(type);
							}
							
						} else {
							type.add(fieldType);
						}
						newFieldData.returnType = fieldType;

						return fieldType;
						
					} catch (ClassNotFoundException e) {
						// e.printStackTrace();
					}
				}
				
				if (returnTypeCount > 0 && !Map.class.isAssignableFrom(returnType)
						&& !Collection.class.isAssignableFrom(returnType) && returnType != Optional.class && 
						returnType != Object.class && !returnType.isArray()) {
					if (type != null) {
						type.add(returnType);
					}

					return returnType;
				}

				if (type != null) {
					ComponentType componentType = type.getComponentType();
					while (componentType != null && componentType.getComponentType() != null && level > 1) {
						componentType = componentType.getComponentType();
						level--;
					}
					
					if (level == 1 && componentType != null && componentType.getClassType() != null) {
						return componentType.getClassType();
					}
					
					Class[] ctypes = type.getComponentClassType();
					float MIN_MAX_COUNT = 20.0f;
					if (ctypes != null && ctypes.length > 0) {
						int length = ctypes.length;
						
						
						Map<String, R> map = (Map)obj;

						Map<String, Class> lnames = new HashMap<>(); //names.stream().map(name -> name.toLowerCase()).collect(Collectors.toSet());
						for (String name: map.keySet()) {
							Object value = map.get(name);
							if (value != null) {
								lnames.put(name.toLowerCase(), map.get(name).getClass());
							}
						}
						int maxIdx = -1;
						float maxCount = 0.0f;
						for (int i = 0; i < length; i++) {
							Class ctype = ctypes[i];
							
							Field[] fields = getFields(ctype);
							if (fields != null && fields.length > 0) {
								int count = 0;
								
								for (Field field: fields) {
									String name = field.getName().toLowerCase();
									if (lnames.containsKey(name)) {
										count++;
										
										Class ftype = field.getType();
										Class mtype = lnames.get(name);
										if (ObjectUtil.isSameDataType(ftype, mtype)) {
											count++;
										}
									}
								}
								float currentCount = count * 100.0f / fields.length;
								if (currentCount > maxCount) {
									maxCount = currentCount;
									maxIdx = i;
								} else if (maxIdx == -1 && ctype.isAssignableFrom(itemType)) {
									maxIdx = i;
								}
							}
						}
						
						if (maxIdx > -1) {
							newFieldData.returnType = ctypes[maxIdx];

							return ctypes[maxIdx];
						}
						
						Set<Class> processed = new HashSet(Arrays.asList(ctypes));
						
						// now try to locate it in all cachedComponentTypes
						for (Entry<Class, ComponentType> entry: cachedComponentTypes.entrySet()) {
							Class myMasterClass = entry.getKey();
							
							if (myMasterClass != this.masterClass && entry.getValue() != null) {
								ctypes = entry.getValue().getComponentClassType();
								if (ctypes != null) {
									length = ctypes.length;
									
									maxCount = 0.0f;
									for (int i = 0; i < length; i++) {
										Class ctype = ctypes[i];
										
										if (!processed.contains(ctype)) {
											Field[] fields = getFields(ctype);
											if (fields != null && fields.length > 0) {
												int count = 0;
												
												for (Field field: fields) {
													String name = field.getName();
													if (name != null) {
														name = name.toLowerCase();
														if (lnames.containsKey(name)) {
															count++;
															
															Class ftype = field.getType();
															Class mtype = lnames.get(name);
															if (ObjectUtil.isSameType(ftype, mtype)) {
																count++;
															}
														}
													}
												}
												float currentCount = count * 100.0f / fields.length;
												if (currentCount > maxCount) {
													maxCount = currentCount;
													maxIdx = i;
												}
	//											else if (maxIdx == -1 && ctype.isAssignableFrom(itemType)) {
	//												maxIdx = i;
	//											}
											}
										}
									}
									
									if (maxIdx > -1 && maxCount > MIN_MAX_COUNT) {
										newFieldData.returnType = ctypes[maxIdx];
										
										type.add(ctypes[maxIdx]);
										
										return ctypes[maxIdx];
									}
									
									
									processed.addAll(Arrays.asList(ctypes));
								}
							}
						}
						
						if (ctypes != null && ctypes.length == 1) {
							return ctypes[0];
						}
					}
					
				}

				
			} else if ((returnType != null && (Collection.class.isAssignableFrom(returnType) || returnType.isArray())) ||
						(itemType != null && (Collection.class.isAssignableFrom(itemType) || itemType.isArray()))) {
				
				//  && ComponentType.class.isAssignableFrom(erasedType.getClass())
				if (type != null) {
					
					ComponentType componentType = type.getComponentType();
					while (componentType != null && componentType.getComponentType() != null && level > 1) {
						componentType = componentType.getComponentType();
						level--;
					}
					
					if (level == 1 && componentType != null && componentType.getClassType() != null) {
						return componentType.getClassType();
					}
					
					Class[] ctypes = type.getComponentClassType();
					
					if (ctypes != null && ctypes.length > 0) {
						if (ctypes.length == 1) {
							Class cmptype = ctypes[0].getComponentType();
							if (cmptype != null && (cmptype.isArray() || Collection.class.isAssignableFrom(cmptype))) {
								type.add(cmptype);
							}
							//if (!ObjectUtil.isBasicDataType(ctypes[0])) {
								return ctypes[0];
							//}
						}
						
						int length = ctypes.length;
						int depth = CollectionArrayTypeGuesser.getDepth(obj);
						Class baseType = CollectionArrayTypeGuesser.getBaseType(obj);
						Class possible = null;
						for (int i = 0; i < length; i++) {
							Class ctype = ctypes[i];
							
							if (ctype.isArray() || Collection.class.isAssignableFrom(ctype)) {
								//Class compType = CollectionArrayTypeGuesser.guessElementType(collection, ctype, getJsonClassType());								
								int typedepth = CollectionArrayTypeGuesser.getDepth(ctype);
								Class cbaseType = CollectionArrayTypeGuesser.getBaseType(ctype);
								if (depth == typedepth) {
									if (ObjectUtil.isSameType(baseType, cbaseType)) {
										Class cmptype = ctype.getComponentType();
										if (cmptype.isArray() || Collection.class.isAssignableFrom(cmptype)) {
											type.add(cmptype);
										}
										
										return ctype;
										
									} else if (itemType.isAssignableFrom(ctype) || ctype.isAssignableFrom(itemType)) {
										possible = ctype;
									}
								}
							}

						}
						
						if (possible != null) {
							return possible;
						}

					}
					
				}
				
			}
//			else if (StringUtil.isNumeric(obj.toString())) {
//				if (obj.toString().contains(".")) {
//					return Double.class;
//				} else {
//					return Integer.class;
//				}
//			}
			
			if (type != null && type.getComponentType() != null) {
				Class classType = type.getComponentType().getClassType();
				if (classType != null && ObjectUtil.isSameDataType(classType, itemType)) {
					return classType;
				}
			}
			
			return itemType;
		}
	}


	private <E> Map json2Map(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Map returnObj = (Map)objectDTO.returnObj;
		Class<Map> returnType = objectDTO.returnType;
		Map defaultValue = (Map)objectDTO.defaultValue;
		
		if (!StringUtil.isEmpty(value)) {
			boolean map2ListStyle = this.isMap2ListStyle();
			
			Map<Object, Object> values = null;
			Class valueType = value.getClass();
			if (Map.class.isAssignableFrom(valueType)) {
				try {
					values = (Map<Object, Object>)value;
				} catch (Exception e) {
					// values = null; // new HashMap();
				}
				
			} else if (List.class.isAssignableFrom(valueType)) {
				values = ArrayToJsonMap.list2Map((List)value);
				objectDTO.valueToProcess = values;
				map2ListStyle = true;
				
			} else {
				try {
					values = (Map<Object, Object>)value;
				} catch (Exception e) {}
			}
	
			if (values != null && values.size() > 0) {
				Function function = objectDTO.getDeserializer();

				if (function != null) {
					try {
						Object returnedValue = null;

						if (function instanceof Json2DataMapperFunction) {
							DataMapper classData = new DataMapper(returnType, values, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
							returnedValue = ((Json2DataMapperFunction)function).apply(classData);

						} else if (function instanceof Json2FieldDataFunction) {
							Json2FieldDataFunction f = (Json2FieldDataFunction)function;
							FieldData fieldData = objectDTO.clone();
							
							fieldData.valueToProcess = values;

							returnedValue = f.apply(fieldData);
							
						} else if (function instanceof Json2MapFunction) {
							return ((Json2MapFunction)function).apply(values);

						} else {
							returnedValue = function.apply(values);
						}
						
						if (returnedValue instanceof Optional) {
							returnedValue = ObjectUtil.unwrap(returnedValue);
						}
						
						if (returnedValue == null) {
							return null;
							
						} else if (Map.class.isAssignableFrom(returnedValue.getClass())) {
							return (Map)returnedValue;

						} else {
							// do not know what to do
						}

					} catch (Exception e) {}
				}

				if (values != null && values.size() > 0) {
					if (returnObj == null) {
						if (defaultValue != null) {
							returnObj = defaultValue;
						}
			
						if (returnObj == null) {
							returnObj = newInstance(new HashMap(), returnType);
						}
			
						if (returnObj == null) {
							returnObj = DefaultValue.map(returnType);
						}
						
						if (returnObj == null) {
							returnObj = new HashMap();
						}
					}
					
					objectDTO.incrLevel();
					Class<E> componentType = guessComponentType(objectDTO); // objectDTO.getComponentType(getJsonClassType());
					boolean isObject = ObjectUtil.isObject(componentType);
					
					Collection<Object> keys = values.keySet();
					// || getOrderByKeyAndProperties()
					if (objectDTO.classMapper.orderByKeyAndProperties) {
						keys = new TreeSet(keys);
					}

					//for (Entry<String, Object> entry: values.entrySet()) {
					// Object obj = entry.getValue(); // obj.getClass()
					for (Object key: keys) {
						Object obj = values.get(key); // entry.getValue(); // obj.getClass()
						
						Object component = null;
						if (obj != null) {
							FieldData newFieldData = new FieldData(obj, obj.getClass(), objectDTO.json2Java, objectDTO.level, objectDTO.set);
							newFieldData.field = objectDTO.field;
							newFieldData.isMapValue = true;
							newFieldData.returnType = guessComponentType(newFieldData);
							if (isObject) {
								if (ObjectUtil.isBasicDataType(newFieldData.returnType)) {
									newFieldData.returnType = componentType;
								}
							}
							newFieldData.fieldMapper = objectDTO.fieldMapper;
							
							component = json2Object(newFieldData);
						}
						
						if (component == null && getDefaultType() == JSON_INCLUDE.DEFAULT) {
							component = getDefaultValue(componentType);
						}
						
						//String key = entry.getKey();
						Object keyObj = null;
						if (Map.class.isAssignableFrom(key.getClass()) || StringUtil.parenthesized((String)key)) {
							if (Map.class.isAssignableFrom(key.getClass())) {
								keyObj = key;
							} else {
								keyObj = getListMapObject ((String)key);
							}
							FieldData newFieldData = new FieldData(keyObj, keyObj.getClass(), objectDTO.json2Java, objectDTO.level, objectDTO.set);
							newFieldData.field = objectDTO.field;
							newFieldData.setter = objectDTO.setter;
							newFieldData.enclosingtype = objectDTO.enclosingtype;
							newFieldData.returnType = guessMapKeyType(newFieldData);
							newFieldData.fieldMapper = objectDTO.fieldMapper;
							keyObj = json2Object(newFieldData);
							
						} else {
							FieldData newFieldData = new FieldData(key, key.getClass(), objectDTO.json2Java, objectDTO.level, objectDTO.set);
							newFieldData.field = objectDTO.field;
							newFieldData.setter = objectDTO.setter;
							newFieldData.enclosingtype = objectDTO.enclosingtype;
							newFieldData.returnType = guessMapKeyType(newFieldData);
							newFieldData.fieldMapper = objectDTO.fieldMapper;
							keyObj = json2Object(newFieldData);
						}
						
						if (keyObj == null) { // failed to get a correct key value, just use its key
							returnObj.put(key, component);

						} else {
							returnObj.put(keyObj, component);
						}
					}
					
					return returnObj;
				}
			} else {
				return values;
			}
		}
		
		return json2MapDefault(objectDTO);
	}

	private <E> String map2Json(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<?> returnType = objectDTO.returnType;

		// && ((Map) value).size() > 0
		if (value != null) {
			Map<Object, Object> map = null;
			try {
				map = (Map) value;
			} catch (Exception ex) {}
			Function function = objectDTO.getSerializer();
			String valueToReturn = null;
			
			if (function != null) {
				try {

					// suppose to return String, but in case not, try to process
					if (function instanceof DataMapper2JsonFunction) {
						DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
						return ((DataMapper2JsonFunction)function).apply(classData);
						
					} else if (function instanceof Map2JsonFunction) {
						return ((Map2JsonFunction)function).apply(map);
							
					} else {
						
						Object returnedValue = function.apply(map);
	
						if (returnedValue instanceof Optional) {
							returnedValue = ObjectUtil.unwrap(returnedValue);
						}
						
						if (returnedValue == null) {
							return null;
							
						} else if (Map.class.isAssignableFrom(returnedValue.getClass())) {
							// keep on processing
							map = (Map) returnedValue;
							
						} else {
							objectDTO.valueToProcess = returnedValue;
							return object2String(objectDTO);
						}
					}
					
				} catch (Exception ex) {}
			}
	
			String repeated = getPrettyIndentationln(objectDTO.level), pretty = getPrettySpace();
			objectDTO.incrLevel();
			String repeatedItem = getPrettyIndentationln(objectDTO.level);
			StringBuilder sbuilder = new StringBuilder();
			
			String startCursor;
			String endCursor;
			String separator;
			if (this.isMap2ListStyle()) {
				startCursor = "[";
				endCursor = "]";
				separator = ",";
				repeatedItem = repeatedItem + startCursor;
			} else {
				startCursor = "{";
				endCursor = "}";
				separator = ":";
			}

			try {
				if (map != null) {
					Set<Object> names = map.keySet();
					
					if (objectDTO.classMapper.orderByKeyAndProperties) {
					// if (getOrderByKeyAndProperties()) {//LinkedHashSet
						names = new TreeSet(names);
					}
					
					Class lastValueType = null;
	
					for (Object name : names) {
						Object v = map.get(name);
	
						String str = null;
						if (!StringUtil.isNull(v)) {
							lastValueType = v.getClass();
							FieldData newFieldData = new FieldData(v, lastValueType, objectDTO.json2Java, objectDTO.level, objectDTO.set);
							newFieldData.fieldMapper = objectDTO.fieldMapper;
							str = object2Json(newFieldData);
						}
						
						if (str == null) {
							if (getDefaultType() == JSON_INCLUDE.DEFAULT) {
								str = "null"; // getDefaultValue(lastValueType).toString();
							} else {
								continue;
							}
						}
							
						if (name == null) {
							switch (objectDTO.defaultType) {
							case ALWAYS:
								name = "null";
							case NON_NULL:
								continue;
							case NON_EMPTY:
								continue;
							case NON_DEFAULT:
								continue;
							case DEFAULT:
								name = "null";
							default:
								name = "null";
							}
							
							FieldData newFieldData = new FieldData(name, name.getClass(), objectDTO.json2Java, objectDTO.level, objectDTO.set);
							newFieldData.field = objectDTO.field;
							newFieldData.setter = objectDTO.setter;
							newFieldData.enclosingtype = objectDTO.enclosingtype;
							newFieldData.returnType = guessMapKeyType(newFieldData);
							
							if (newFieldData.returnType == String.class) {
								sbuilder.append(repeatedItem + StringUtil.doublequote(name) + separator + pretty);
							} else {
								sbuilder.append(repeatedItem + name + separator + pretty);
							}
							
						} else {
							Class keyClass = name.getClass();
							
							if (!ObjectUtil.isBasicDataType(keyClass)) {
								FieldData newFieldData = new FieldData(name, keyClass, objectDTO.json2Java, objectDTO.level, objectDTO.set);
								newFieldData.fieldMapper = objectDTO.fieldMapper;
								name = object2Json(newFieldData);
							}
							sbuilder.append(repeatedItem + StringUtil.doublequote(name) + separator + pretty);
						}
						
						sbuilder.append(str);
						if (this.isMap2ListStyle()) {
							sbuilder.append("]");
						}
						sbuilder.append(",");
					}
				}
			} catch (Exception e) {
				//e.printStackTrace();
			}
		
			String str = sbuilder.toString();
			int size = str.length();
			if (size == 0) {
				switch (objectDTO.defaultType) {
				case ALWAYS:
					return "{}";
				case NON_NULL:
					return "{}";
				case NON_EMPTY:
					return null;
				case NON_DEFAULT:
					return null;
				case DEFAULT:
					return "{}";
				default:
					return "{}";
				}

			} else {
				return startCursor + str.substring(0, size - 1) + repeated + endCursor;
			}
		}
		
		return map2JsonDefault(objectDTO);
	}
	
	
	private String map2JsonDefault(FieldData objectDTO) {
		Map valueToReturn = json2MapDefault(objectDTO);
		
		if (valueToReturn == null) {
			return null;
		}

		switch (objectDTO.defaultType) {
		case ALWAYS:
			return "{}";
		case NON_NULL:
			return "{}";
		case NON_EMPTY:
			return null;
		case NON_DEFAULT:
			return null;
		case DEFAULT:
			return "{}";
		default:
			return "{}";
		}
	}
	
	private <E> Map json2MapDefault(FieldData objectDTO) {
		boolean required = objectDTO.required();

		if (getDefaultType() == JSON_INCLUDE.DEFAULT || required) {
			Map defaultValue = (Map)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				return defaultValue;
			}

			return DefaultValue.map(objectDTO.returnType);
		}

		return null;
	}


	private <E> Collection json2Collection(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Collection<E> returnObj = (Collection<E>)objectDTO.returnObj;
		Class<Collection<E>> returnType = objectDTO.returnType;
		Collection<E> defaultValue = (Collection<E>)objectDTO.defaultValue;

		if (returnType == null) {
			if (returnType == null && returnObj != null) {
				returnType = (Class<Collection<E>>) returnObj.getClass();
			}

			if (returnType == null) {
				returnType = (Class<Collection<E>>) DefaultValue.collection(returnType).getClass();
			}
			
			objectDTO.returnType = returnType;
		}
		
		// isEmpty
		if (!StringUtil.isNull(value)) {
			Collection<E> collection = null;
			try {
				collection = (Collection<E>) value;
			} catch (Exception e) {
				collection = DefaultValue.collection(returnType); // new ArrayList();
			}
	
			if (collection.size() > 0) {
				Function function = objectDTO.getDeserializer();

				if (function != null) {
					try {
						Object returnedValue = null;
						// suppose to return String, but in case not, try to process
						if (function instanceof Json2DataMapperFunction) {
							DataMapper classData = new DataMapper(returnType, collection, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
							returnedValue = ((Json2DataMapperFunction)function).apply(classData);
							
						} else if (function instanceof Json2FieldDataFunction) {
							Json2FieldDataFunction f = (Json2FieldDataFunction)function;
							FieldData fieldData = objectDTO.clone();
							
							returnedValue = f.apply(fieldData);
							
						} else if (function instanceof Json2CollectionFunction) {
							return ((Json2CollectionFunction)function).apply(collection);
								
						} else {
							returnedValue = function.apply(collection);
						}
						
						if (returnedValue == null) {
							return null;
							
						} else if (Collection.class.isAssignableFrom(returnedValue.getClass())) {
							return (Collection)returnedValue;
							
						} else if (returnedValue.getClass().isArray()) {
							return Arrays.asList((Object[])returnedValue);
						} else {
							// do not know what to do
						}

					} catch (Exception e) {}
				}

				
				if (returnObj == null) {
					if (defaultValue != null) {
						returnObj = defaultValue;
					}
					
					if (EnumSet.class.isAssignableFrom(returnType)) {
						ComponentType type = getComponentType();
						Class enm = type.getComponentType().getClassType();
						if (enm.isEnum()) {
							returnObj = (Collection<E>) EnumSet.allOf(enm);
						}
					}
		
					if (returnObj == null) {
						returnObj = newInstance(new HashMap(), returnType);
					}
		
					if (returnObj == null) {
						returnObj = DefaultValue.collection(returnType);
					}
					
					objectDTO.returnObj = returnObj;
				}
				
				objectDTO.incrLevel();
				
				objectDTO.valueToProcess = collection;
				//Class<E> componentType = objectDTO.getComponentType(getJsonClassType());
				Class<E> componentType = guessComponentType(objectDTO);

				for (E val : collection) {
					if (val != null) {
						FieldData newFieldData = new FieldData(val, objectDTO.returnType, objectDTO.json2Java, objectDTO.level, objectDTO.set);
						newFieldData.componentType = objectDTO.componentType;
						if (ObjectUtil.isSameDataType(componentType, val.getClass()) && ObjectUtil.isBasicDataType(val.getClass())) {
							newFieldData.returnType = componentType;
						} else {
							newFieldData.returnType = guessComponentType(newFieldData);
						}
						newFieldData.fieldMapper = objectDTO.fieldMapper;
						returnObj.add(json2Object(newFieldData));
					}
				}
				
				if (objectDTO.classMapper.orderArrayAndList) {
					try {
						if (!List.class.isAssignableFrom(returnObj.getClass())) {
							returnObj = new ArrayList(returnObj);
						}
						Collections.sort((List)returnObj);
					} catch (Exception ex) {}
				}
				
				return returnObj;
				
			} else {
				return collection;
			}
		}
		
		return json2CollectionDefault(objectDTO);
	}


	private int[] json2ArrayInt(FieldData objectDTO) {
		if (objectDTO.valueToProcess == null) {
			return null;
		}
		
		Function function = objectDTO.getDeserializer();
		
		Object returnedValue = objectDTO.valueToProcess;

		if (function != null) {
			try {
				
				// suppose to return String, but in case not, try to process
				if (function instanceof Json2DataMapperFunction) {
					DataMapper classData = new DataMapper(objectDTO.returnType, objectDTO.valueToProcess, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
					returnedValue = ((Json2DataMapperFunction)function).apply(classData);

				} else if (function instanceof Json2FieldDataFunction) {
					Json2FieldDataFunction f = (Json2FieldDataFunction)function;
					FieldData fieldData = objectDTO.clone();
					
					returnedValue = f.apply(fieldData);
					
				} else if (function instanceof Json2ArrayFunction) {
					returnedValue = ((Json2ArrayFunction)function).apply(objectDTO.valueToProcess);
						
				} else {
					returnedValue = function.apply(objectDTO.valueToProcess);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

		if (returnedValue == null) {
			return null;
			
		} else if (returnedValue.getClass().isArray()) {
			if (returnedValue.getClass() == int[].class) {
				return (int[]) returnedValue;
			}
			
			int size = Array.getLength(returnedValue);
			int[] arr = new int[size];
			int i = 0, j = 0;
			while (j < size) {
				try {
					arr[i] = (int) NumberUtil.getNumber(Array.get(returnedValue, j), int.class);
					i++;
				} catch (Exception ex) {}
				j++;
			}
			
			if (i == size) {
				return arr;
			}
			return Arrays.copyOfRange(arr, 0, i);
			
		} else if (!Collection.class.isAssignableFrom(returnedValue.getClass())) {
			return null;
		}
		
		Collection values = (Collection)returnedValue;
		
		int size = values.size();
		int[] arr = new int[size];
		int i = 0;

		for (Object value: values) {
			if (value != null) {
				try {
					arr[i] = (int) NumberUtil.getNumber(value, int.class);
					i++;
				} catch (Exception ex) {}
			}
		}
		
		if (i == size) {
			return arr;
		}
		return Arrays.copyOfRange(arr, 0, i);
	}
	
	private byte[] json2ArrayByte(FieldData objectDTO) {
		if (objectDTO.valueToProcess == null) {
			return null;
		}
		
		Function function = objectDTO.getDeserializer();
		
		Object returnedValue = objectDTO.valueToProcess;

		if (function != null) {
			try {
				
				// suppose to return String, but in case not, try to process
				if (function instanceof Json2DataMapperFunction) {
					DataMapper classData = new DataMapper(objectDTO.returnType, objectDTO.valueToProcess, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
					returnedValue = ((Json2DataMapperFunction)function).apply(classData);

				} else if (function instanceof Json2FieldDataFunction) {
					Json2FieldDataFunction f = (Json2FieldDataFunction)function;
					FieldData fieldData = objectDTO.clone();
					
					returnedValue = f.apply(fieldData);
					
				} else if (function instanceof Json2ArrayFunction) {
					returnedValue = ((Json2ArrayFunction)function).apply(objectDTO.valueToProcess);
						
				} else {
					returnedValue = function.apply(objectDTO.valueToProcess);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

		if (returnedValue == null) {
			return null;
			
		} else if (returnedValue.getClass().isArray()) {
			if (returnedValue.getClass() == byte[].class) {
				return (byte[]) returnedValue;
			}
			
			int size = Array.getLength(returnedValue);
			byte[] arr = new byte[size];
			int i = 0, j = 0;
			while (j < size) {
				try {
					arr[i] = (byte) NumberUtil.getNumber(Array.get(returnedValue, j), byte.class);
					i++;
				} catch (Exception ex) {}
				j++;
			}
			
			if (i == size) {
				return arr;
			}
			return Arrays.copyOfRange(arr, 0, i);
			
		} else if (!Collection.class.isAssignableFrom(returnedValue.getClass())) {
			return null;
		}
		
		Collection values = (Collection)returnedValue;
		
		int size = values.size();
		byte[] arr = new byte[size];
		int i = 0;

		for (Object value: values) {
			if (value != null) {
				try {
					arr[i] = (byte) NumberUtil.getNumber(value, byte.class);
					i++;
				} catch (Exception ex) {}
			}
		}
		
		if (i == size) {
			return arr;
		}
		return Arrays.copyOfRange(arr, 0, i);
	}

	
	private char[] json2ArrayChar(FieldData objectDTO) {
		if (objectDTO.valueToProcess == null) {
			return null;
		}
		
		Function function = objectDTO.getDeserializer();
		
		Object returnedValue = objectDTO.valueToProcess;

		if (function != null) {
			try {
				
				// suppose to return String, but in case not, try to process
				if (function instanceof Json2DataMapperFunction) {
					DataMapper classData = new DataMapper(objectDTO.returnType, objectDTO.valueToProcess, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
					returnedValue = ((Json2DataMapperFunction)function).apply(classData);

				} else if (function instanceof Json2FieldDataFunction) {
					Json2FieldDataFunction f = (Json2FieldDataFunction)function;
					FieldData fieldData = objectDTO.clone();
					
					returnedValue = f.apply(fieldData);
					
				} else if (function instanceof Json2ArrayFunction) {
					returnedValue = ((Json2ArrayFunction)function).apply(objectDTO.valueToProcess);
						
				} else {
					returnedValue = function.apply(objectDTO.valueToProcess);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

		if (returnedValue == null) {
			return null;
			
		} else if (returnedValue.getClass().isArray()) {
			if (returnedValue.getClass() == char[].class) {
				return (char[]) returnedValue;
			}
			
			int size = Array.getLength(returnedValue);
			char[] arr = new char[size];
			int i = 0, j = 0;
			while (j < size) {
				try {
					Object obj = Array.get(returnedValue, j);
					if (obj.getClass() == Character.class || obj.getClass() == char.class) {
						arr[i] = (char)obj;
						
					} else {
						String str = StringUtil.unquote(obj.toString(), isEscapeHtml());
						arr[i] = str.charAt(0);
					}
					i++;
				} catch (Exception ex) {}
				j++;
			}
			
			if (i == size) {
				return arr;
			}
			return Arrays.copyOfRange(arr, 0, i);
			
		} else if (!Collection.class.isAssignableFrom(returnedValue.getClass())) {
			return null;
		}
		
		Collection values = (Collection)returnedValue;
		
		int size = values.size();
		char[] arr = new char[size];
		int i = 0;

		for (Object obj: values) {
			if (obj != null) {
				try {
					if (obj.getClass() == Character.class || obj.getClass() == char.class) {
						arr[i] = (char)obj;
						
					} else {
						String str = StringUtil.unquote(obj.toString(), isEscapeHtml());
						arr[i] = str.charAt(0);
					}
					i++;
				} catch (Exception ex) {}
			}
		}
		
		if (i == size) {
			return arr;
		}
		return Arrays.copyOfRange(arr, 0, i);
	}
	
	private float[] json2ArrayFloat(FieldData objectDTO) {
		if (objectDTO.valueToProcess == null) {
			return null;
		}
		
		Function function = objectDTO.getDeserializer();
		
		Object returnedValue = objectDTO.valueToProcess;

		if (function != null) {
			try {
				
				// suppose to return String, but in case not, try to process
				if (function instanceof Json2DataMapperFunction) {
					DataMapper classData = new DataMapper(objectDTO.returnType, objectDTO.valueToProcess, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
					returnedValue = ((Json2DataMapperFunction)function).apply(classData);

				} else if (function instanceof Json2FieldDataFunction) {
					Json2FieldDataFunction f = (Json2FieldDataFunction)function;
					FieldData fieldData = objectDTO.clone();
					
					returnedValue = f.apply(fieldData);
					
				} else if (function instanceof Json2ArrayFunction) {
					returnedValue = ((Json2ArrayFunction)function).apply(objectDTO.valueToProcess);
						
				} else {
					returnedValue = function.apply(objectDTO.valueToProcess);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

		if (returnedValue == null) {
			return null;
			
		} else if (returnedValue.getClass().isArray()) {
			if (returnedValue.getClass() == float[].class) {
				return (float[]) returnedValue;
			}
			
			int size = Array.getLength(returnedValue);
			float[] arr = new float[size];
			int i = 0, j = 0;
			while (j < size) {
				try {
					arr[i] = (float) NumberUtil.getNumber(Array.get(returnedValue, j), float.class);
					i++;
				} catch (Exception ex) {}
				j++;
			}
			
			if (i == size) {
				return arr;
			}
			return Arrays.copyOfRange(arr, 0, i);
			
		} else if (!Collection.class.isAssignableFrom(returnedValue.getClass())) {
			return null;
		}
		
		Collection values = (Collection)returnedValue;
		
		int size = values.size();
		float[] arr = new float[size];
		int i = 0;

		for (Object value: values) {
			if (value != null) {
				try {
					arr[i] = (float) NumberUtil.getNumber(value, float.class);
					i++;
				} catch (Exception ex) {}
			}
		}
		
		if (i == size) {
			return arr;
		}
		return Arrays.copyOfRange(arr, 0, i);
	}
	
	private double[] json2ArrayDouble(FieldData objectDTO) {
		if (objectDTO.valueToProcess == null) {
			return null;
		}
		
		Function function = objectDTO.getDeserializer();
		
		Object returnedValue = objectDTO.valueToProcess;

		if (function != null) {
			try {
				
				// suppose to return String, but in case not, try to process
				if (function instanceof Json2DataMapperFunction) {
					DataMapper classData = new DataMapper(objectDTO.returnType, objectDTO.valueToProcess, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
					returnedValue = ((Json2DataMapperFunction)function).apply(classData);

				} else if (function instanceof Json2FieldDataFunction) {
					Json2FieldDataFunction f = (Json2FieldDataFunction)function;
					FieldData fieldData = objectDTO.clone();
					
					returnedValue = f.apply(fieldData);
					
				} else if (function instanceof Json2ArrayFunction) {
					returnedValue = ((Json2ArrayFunction)function).apply(objectDTO.valueToProcess);
						
				} else {
					returnedValue = function.apply(objectDTO.valueToProcess);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

		if (returnedValue == null) {
			return null;
			
		} else if (returnedValue.getClass().isArray()) {
			if (returnedValue.getClass() == double[].class) {
				return (double[]) returnedValue;
			}
			
			int size = Array.getLength(returnedValue);
			double[] arr = new double[size];
			int i = 0, j = 0;
			while (j < size) {
				try {
					arr[i] = (double) NumberUtil.getNumber(Array.get(returnedValue, j), double.class);
					i++;
				} catch (Exception ex) {}
				j++;
			}
			
			if (i == size) {
				return arr;
			}
			return Arrays.copyOfRange(arr, 0, i);
			
		} else if (!Collection.class.isAssignableFrom(returnedValue.getClass())) {
			return null;
		}
		
		Collection values = (Collection)returnedValue;
		
		int size = values.size();
		double[] arr = new double[size];
		int i = 0;

		for (Object value: values) {
			if (value != null) {
				try {
					arr[i] = (double) NumberUtil.getNumber(value, double.class);
					i++;
				} catch (Exception ex) {}
			}
		}
		
		if (i == size) {
			return arr;
		}
		return Arrays.copyOfRange(arr, 0, i);
	}
	
	
	private long[] json2ArrayLong(FieldData objectDTO) {
		if (objectDTO.valueToProcess == null) {
			return null;
		}
		
		Function function = objectDTO.getDeserializer();
		
		Object returnedValue = objectDTO.valueToProcess;

		if (function != null) {
			try {
				
				// suppose to return String, but in case not, try to process
				if (function instanceof Json2DataMapperFunction) {
					DataMapper classData = new DataMapper(objectDTO.returnType, objectDTO.valueToProcess, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
					returnedValue = ((Json2DataMapperFunction)function).apply(classData);

				} else if (function instanceof Json2FieldDataFunction) {
					Json2FieldDataFunction f = (Json2FieldDataFunction)function;
					FieldData fieldData = objectDTO.clone();
					
					returnedValue = f.apply(fieldData);
					
				} else if (function instanceof Json2ArrayFunction) {
					returnedValue = ((Json2ArrayFunction)function).apply(objectDTO.valueToProcess);
						
				} else {
					returnedValue = function.apply(objectDTO.valueToProcess);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

		if (returnedValue == null) {
			return null;
			
		} else if (returnedValue.getClass().isArray()) {
			if (returnedValue.getClass() == long[].class) {
				return (long[]) returnedValue;
			}
			
			int size = Array.getLength(returnedValue);
			long[] arr = new long[size];
			int i = 0, j = 0;
			while (j < size) {
				try {
					arr[i] = (long) NumberUtil.getNumber(Array.get(returnedValue, j), long.class);
					i++;
				} catch (Exception ex) {}
				j++;
			}
			
			if (i == size) {
				return arr;
			}
			return Arrays.copyOfRange(arr, 0, i);
			
		} else if (!Collection.class.isAssignableFrom(returnedValue.getClass())) {
			return null;
		}
		
		Collection values = (Collection)returnedValue;
		
		int size = values.size();
		long[] arr = new long[size];
		int i = 0;

		for (Object value: values) {
			if (value != null) {
				try {
					arr[i] = (long) NumberUtil.getNumber(value, long.class);
					i++;
				} catch (Exception ex) {}
			}
		}
		
		if (i == size) {
			return arr;
		}
		return Arrays.copyOfRange(arr, 0, i);
	}
	
	
	private short[] json2ArrayShort(FieldData objectDTO) {
		if (objectDTO.valueToProcess == null) {
			return null;
		}
		
		Function function = objectDTO.getDeserializer();
		
		Object returnedValue = objectDTO.valueToProcess;

		if (function != null) {
			try {
				
				// suppose to return String, but in case not, try to process
				if (function instanceof Json2DataMapperFunction) {
					DataMapper classData = new DataMapper(objectDTO.returnType, objectDTO.valueToProcess, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
					returnedValue = ((Json2DataMapperFunction)function).apply(classData);

				} else if (function instanceof Json2FieldDataFunction) {
					Json2FieldDataFunction f = (Json2FieldDataFunction)function;
					FieldData fieldData = objectDTO.clone();
					
					returnedValue = f.apply(fieldData);
					
				} else if (function instanceof Json2ArrayFunction) {
					returnedValue = ((Json2ArrayFunction)function).apply(objectDTO.valueToProcess);
						
				} else {
					returnedValue = function.apply(objectDTO.valueToProcess);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

		if (returnedValue == null) {
			return null;
			
		} else if (returnedValue.getClass().isArray()) {
			if (returnedValue.getClass() == short[].class) {
				return (short[]) returnedValue;
			}
			
			int size = Array.getLength(returnedValue);
			short[] arr = new short[size];
			int i = 0, j = 0;
			while (j < size) {
				try {
					arr[i] = (short) NumberUtil.getNumber(Array.get(returnedValue, j), short.class);
					i++;
				} catch (Exception ex) {}
				j++;
			}
			
			if (i == size) {
				return arr;
			}
			return Arrays.copyOfRange(arr, 0, i);
			
		} else if (!Collection.class.isAssignableFrom(returnedValue.getClass())) {
			return null;
		}
		
		Collection values = (Collection)returnedValue;
		
		int size = values.size();
		short[] arr = new short[size];
		int i = 0;

		for (Object value: values) {
			if (value != null) {
				try {
					arr[i] = (short) NumberUtil.getNumber(value, short.class);
					i++;
				} catch (Exception ex) {}
			}
		}
		
		if (i == size) {
			return arr;
		}
		return Arrays.copyOfRange(arr, 0, i);
	}
	
	
	private boolean[] json2ArrayBoolean(FieldData objectDTO) {
		if (objectDTO.valueToProcess == null) {
			return null;
		}
		
		Function function = objectDTO.getDeserializer();
		
		Object returnedValue = objectDTO.valueToProcess;

		if (function != null) {
			try {
				
				// suppose to return String, but in case not, try to process
				if (function instanceof Json2DataMapperFunction) {
					DataMapper classData = new DataMapper(objectDTO.returnType, objectDTO.valueToProcess, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
					returnedValue = ((Json2DataMapperFunction)function).apply(classData);

				} else if (function instanceof Json2FieldDataFunction) {
					Json2FieldDataFunction f = (Json2FieldDataFunction)function;
					FieldData fieldData = objectDTO.clone();
					
					returnedValue = f.apply(fieldData);
					
				} else if (function instanceof Json2ArrayFunction) {
					returnedValue = ((Json2ArrayFunction)function).apply(objectDTO.valueToProcess);
						
				} else {
					returnedValue = function.apply(objectDTO.valueToProcess);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

		if (returnedValue == null) {
			return null;
			
		} else if (returnedValue.getClass().isArray()) {
			if (returnedValue.getClass() == boolean[].class) {
				return (boolean[]) returnedValue;
			}
			
			int size = Array.getLength(returnedValue);
			boolean[] arr = new boolean[size];
			int i = 0, j = 0;
			while (j < size) {
				try {
					arr[i] = BooleanUtil.object2Boolean(Array.get(returnedValue, j));
					i++;
				} catch (Exception ex) {}
				j++;
			}
			
			if (i == size) {
				return arr;
			}
			return Arrays.copyOfRange(arr, 0, i);
			
		} else if (!Collection.class.isAssignableFrom(returnedValue.getClass())) {
			return null;
		}
		
		Collection values = (Collection)returnedValue;
		
		int size = values.size();
		boolean[] arr = new boolean[size];
		int i = 0;

		for (Object value: values) {
			if (value != null) {
				try {
					arr[i] = BooleanUtil.object2Boolean(value);
					i++;
				} catch (Exception ex) {}
			}
		}
		
		if (i == size) {
			return arr;
		}
		return Arrays.copyOfRange(arr, 0, i);
	}

	private <E> Object json2Array(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		E[] returnObj = (E[])objectDTO.returnObj;
		Class<E[]> returnType = objectDTO.returnType;
		E[] defaultValue = (E[])objectDTO.defaultValue;

		if (returnType == null) {
			if (returnType == null && returnObj != null) {
				returnType = (Class<E[]>) returnObj.getClass();
			}

			if (returnType == null) {
				returnType = (Class<E[]>) DefaultValue.array().getClass();
			}
			
			objectDTO.returnType = returnType;
		}
		
		if (value != null && value.toString().length() > 0) {	

			Function function = objectDTO.getDeserializer();

			if (function != null) {
				try {
					Object returnedValue = null;
					// suppose to return String, but in case not, try to process
					if (function instanceof Json2DataMapperFunction) {
						DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
						returnedValue = ((Json2DataMapperFunction)function).apply(classData);

					} else if (function instanceof Json2FieldDataFunction) {
						Json2FieldDataFunction f = (Json2FieldDataFunction)function;
						FieldData fieldData = objectDTO.clone();
						
						returnedValue = f.apply(fieldData);
						
					} else if (function instanceof Json2ArrayFunction) {
						return (E[]) ((Json2ArrayFunction)function).apply(value);
							
					} else {
						returnedValue = function.apply(value);
					}

					if (returnedValue == null) {
						return null;
						
					} else if (returnedValue.getClass().isArray()) {
						return (E[]) returnedValue;
						
					} else if (Collection.class.isAssignableFrom(returnedValue.getClass())) {
						Collection collection = (Collection)returnedValue;
						return (E[]) collection.toArray();

					} else {
						// do not know what to do
					}

				} catch (Exception e) {}
			}
			

			Collection<E> values = null;
			try {
				values = (Collection<E>) value;
				// Class<?> valueType = values.getClass();
				
				// int size = values.size();
				//if (size > 0) {
				
				if (returnObj == null) {
					if (defaultValue != null) {
						returnObj = defaultValue;
					}
		
					if (returnObj == null) {
						returnObj = (E[]) newInstance(new HashMap(), returnType);
					}
		
					if (returnObj == null) {
						returnObj = (E[]) DefaultValue.array();
					}
					
					objectDTO.returnObj = returnObj;
				}
				
				objectDTO.incrLevel();
				
				// Class<E> componentType = objectDTO.getComponentType(getJsonClassType());
				for (Object val: values) {
					if (val != null) {
						objectDTO.valueToProcess = val;
						break;
					}
				}
				Class<E> componentType = guessComponentType(objectDTO);
				
				E[] arr = (E[]) Array.newInstance(componentType, values.size());
				
				int i = 0;
				for (Object val: values) {
					if (!StringUtil.isNull(val)) {
						FieldData newFieldData = new FieldData(val, objectDTO.returnType, objectDTO.json2Java, objectDTO.level, objectDTO.set);
						newFieldData.componentType = objectDTO.componentType;
						newFieldData.returnType = guessComponentType(newFieldData);
						newFieldData.fieldMapper = objectDTO.fieldMapper;
						E object = json2Object(newFieldData);
						arr[i++] = object;
					} else {
						arr[i++] = null;
					}
				}
				
				if (objectDTO.classMapper.orderArrayAndList) {
					try {
						Arrays.sort(arr);
					} catch (Exception ex) {}
				}
				
				return arr;
			
			} catch (Exception e) {}
		}
		
		return json2ArrayDefault(objectDTO);
	}
	
	
	private <E> String array2Json(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<?> returnType = objectDTO.returnType;
		
		int size = 0;
		if (value != null && (size = Array.getLength(value)) > 0) {
			Function function = objectDTO.getSerializer();
			String valueToReturn = null;
			
			if (function != null) {
				try {

					// suppose to return String, but in case not, try to process
					if (function instanceof DataMapper2JsonFunction) {
						DataMapper classData = new DataMapper(returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
						return ((DataMapper2JsonFunction)function).apply(classData);
						
					} else if (function instanceof Array2JsonFunction) {
						return ((Array2JsonFunction)function).apply((E[])value);
							
					} else {
						
						Object returnedValue = null;
						if (function instanceof FieldData2JsonFunction) {
							FieldData2JsonFunction f = (FieldData2JsonFunction)function;
							FieldData fieldData = objectDTO.clone();
							returnedValue = f.apply(fieldData);
						} else {
							returnedValue = function.apply(value);
						}
	
						if (returnedValue instanceof Optional) {
							returnedValue = ObjectUtil.unwrap(returnedValue);
						}
						
						if (returnedValue == null) {
							return null;
							
						} else if (Array.class.isAssignableFrom(returnedValue.getClass())) {
							// keep on processing
							value = returnedValue;
							size = Array.getLength(value);
							
						} else {
							objectDTO.valueToProcess = returnedValue;
							return object2String(objectDTO);
						}
					}
					
				} catch (Exception ex) {}
			}
	
			String repeated = getPrettyIndentationln(objectDTO.level);
			objectDTO.incrLevel();
			String repeatedItem = getPrettyIndentationln(objectDTO.level);
			Class ctype = objectDTO.getComponentType(getJsonClassType());

			List<String> list = new ArrayList<>();
			
			for (int i = 0; i < size; i++) {
				Object componentValue = Array.get(value, i);
				String str = null;
				if (componentValue != null) {
					FieldData newFieldData = new FieldData(componentValue, componentValue.getClass(), objectDTO.json2Java, objectDTO.level, objectDTO.set);
					newFieldData.returnType = guessComponentType(newFieldData);
					newFieldData.fieldMapper = objectDTO.fieldMapper;
					str = object2Json(newFieldData);
				}
				
				if (str == null && ((ctype != null && ctype.isPrimitive())
						|| getDefaultType() == JSON_INCLUDE.DEFAULT) ) {
					str = getDefaultValue(ctype).toString();
				}
				
				list.add(str);
			}
			
			if (objectDTO.classMapper.orderArrayAndList) {
				Collections.sort(list);
			}
			
			StringBuilder sbuilder = new StringBuilder();
			for (String str: list) {
				sbuilder.append(repeatedItem + str + ",");
			}

			String str = sbuilder.toString();
			size = str.length();
			if (size > 0) {
				return "[" + str.substring(0, size - 1) + repeated + "]";
			}
		}
		
		return array2JsonDefault(objectDTO);
	}
	
	
	private <E> String array2JsonDefault(FieldData objectDTO) {
		Object valueToReturn = json2ArrayDefault(objectDTO);
		
		if (valueToReturn == null) {
			return null;
		}

		switch (objectDTO.defaultType) {
		case ALWAYS:
			return "[]";
		case NON_NULL:
			return "[]";
		case NON_EMPTY:
			return null;
		case NON_DEFAULT:
			return null;
		case DEFAULT:
			return "[]";
		default:
			return "[]";
		}
	}

	
	private <E> Object json2ArrayDefault(FieldData objectDTO) {
		//Object value = objectDTO.valueToProcess;
		//Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required();

		if (getDefaultType() == JSON_INCLUDE.DEFAULT || required) {
			E[] defaultValue = (E[])objectDTO.getDefaultValue();
			if (defaultValue != null) {
				return defaultValue;
			}

			return (E[]) DefaultValue.array();
		}
		
		if (objectDTO.valueToProcess != null) {
			return Array.newInstance(objectDTO.getComponentType(getJsonClassType()), 0);
		}

		return null;
	}
	

	/*
	 * deserialize, convert Json value to Java data
	 */
	@SuppressWarnings("unchecked")
	private <E> E json2Object(FieldData objectDTO) {
		// String value, Class<E> returnType
		Class<E> returnType = objectDTO.returnType;
		
		Object value = objectDTO.valueToProcess;
		if (StringUtil.isNull(value)) {
			if (objectDTO.required()) {
				return (E) DefaultValue.getSystemDefault(returnType);
			}
			
			return null;
		}

		if (returnType == null || returnType == Object.class) {
			if (StringUtil.isEmpty(value)) {
				return null;
			}
			
			returnType = (Class<E>) value.getClass();
			objectDTO.returnType = returnType;
		}
		
		
		
		// first, get the class mapper
		//if (objectDTO.fieldMapper == null && objectDTO.classMapper == null) {
			objectDTO.classMapper = getGlobalizedClassMapper(returnType);
		//}

		if (returnType == String.class) {
			return (E) json2String(objectDTO);

//		} else if (returnType == Optional.class) {
//			return (E) json2Optional(objectDTO);
			
		} else if (returnType == Character.class || returnType == char.class) {
			return (E) json2Character(objectDTO);
			
		} else if (returnType == Boolean.class || returnType == boolean.class || returnType == AtomicBoolean.class) {
			return (E) json2Boolean(objectDTO);
			
		} else if (Number.class.isAssignableFrom(returnType) || returnType.isPrimitive()) {
			
			objectDTO.valueToProcess = StringUtil.unquote(objectDTO.valueToProcess, isEscapeHtml());
			
			if (returnType == Integer.class || returnType == int.class) {
				return (E) json2Integer(objectDTO);
			} else if (returnType == Long.class || returnType == long.class) {
				return (E) json2Long(objectDTO);
	
			} else if (returnType == Double.class || returnType == double.class) {
				return (E) json2Double(objectDTO);
	
			} else if (returnType == Byte.class || returnType == byte.class) {
				return (E) json2Byte(objectDTO);
	
			} else if (returnType == Short.class || returnType == short.class) {
				return (E) json2Short(objectDTO);
	
			} else if (returnType == Float.class || returnType == float.class) {
				return (E) json2Float(objectDTO);
	
			} else if (returnType == BigDecimal.class) {
				return (E) json2BigDecimal(objectDTO);
	
			} else if (returnType == BigInteger.class) {
				return (E) json2BigInteger(objectDTO);

			} else if (returnType == AtomicInteger.class) {
				return (E) json2AtomicInteger(objectDTO);
				
			} else if (returnType == AtomicLong.class) {
				return (E) json2AtomicLong(objectDTO);
				
			} else { // default to Double, in case no specific type is specified
				return (E) json2Double(objectDTO);
			}
			
		} else if (returnType.isEnum() || Enum.class.isAssignableFrom(returnType)) {
			return (E) json2Enum(objectDTO);
				
		} else if (returnType == Date.class || Date.class.isAssignableFrom(returnType)) {
			return (E) json2Date(objectDTO);


		} else if (returnType.isArray() || Collection.class.isAssignableFrom(returnType)) {
			
			Collection<E> values = null;
			Class<?> valueType = value.getClass();
			if (Collection.class.isAssignableFrom(valueType)) {
				values = (Collection<E>) value;
			} else if (valueType.isArray()) {
				values = Arrays.asList((E[]) value);
			}
			
			if (values == null) {
				//return null;
			} else {
				objectDTO.valueToProcess = values;
			}
			
			Class<E> componentType = (Class<E>) returnType.getComponentType();
			if (componentType == null) {
				objectDTO.incrLevel();
				componentType = guessComponentType(objectDTO);
				objectDTO.descLevel();
			}
			if (componentType == null) {
				componentType = CollectionArrayTypeGuesser.guessElementType(values, returnType,  getJsonClassType());
			}
			if (componentType == null) componentType = (Class<E>) Object.class;

			if (componentType.isPrimitive()) {
				E arr = null;
				if (componentType == int.class) {
					arr = (E)json2ArrayInt(objectDTO);
					
				} else if (componentType == byte.class) {
					arr = (E)json2ArrayByte(objectDTO);
				} else if (componentType == char.class) {
					arr = (E)json2ArrayChar(objectDTO);
				} else if (componentType == float.class) {
					arr = (E)json2ArrayFloat(objectDTO);
				} else if (componentType == double.class) {
					arr = (E)json2ArrayDouble(objectDTO);
				} else if (componentType == long.class) {
					arr = (E)json2ArrayLong(objectDTO);
				} else if (componentType == short.class) {
					arr = (E)json2ArrayShort(objectDTO);
				} else if (componentType == boolean.class) {
					arr = (E)json2ArrayBoolean(objectDTO);
				} else {
					// return null;
				}
				
				if (arr != null && objectDTO.classMapper.orderArrayAndList) {
					Arrays.sort((E[])arr);
				}

				return arr;

			} else if (Collection.class.isAssignableFrom(returnType)) {
				return (E) json2Collection(objectDTO);
				
			} else {
				return (E) json2Array(objectDTO);
			}

		} else if (returnType != Optional.class && Map.class.isAssignableFrom(returnType)) {
			return (E) json2Map(objectDTO);
			
		} else {
			if (objectDTO.returnObj == null) {
				return (E) deserialize2Object(value, returnType, objectDTO);
				
			} else {
				objectDTO.returnType = returnType;
				return (E) deserialize2Object(objectDTO);
			}
		}
	}

	private <E> String enum2Json(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> valueType = objectDTO.returnType;
		EnumType enumType = objectDTO.getEnumType();
		
		if (value == null) {
			return null;
		}
		
		Enum en = (Enum)value;

		try {
			Function function = objectDTO.getSerializer();
			if (function != null) {
				try {
					if (function instanceof DataMapper2JsonFunction) {
						DataMapper classData = new DataMapper(objectDTO.returnType, value, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
						return ((DataMapper2JsonFunction)function).apply(classData);
						
					} else if (function instanceof Enum2JsonFunction) {
						return ((Enum2JsonFunction)function).apply(en);
						
					} else {
						
						Object returnedValue = null;
						if (function instanceof FieldData2JsonFunction) {
							FieldData2JsonFunction f = (FieldData2JsonFunction)function;
							FieldData fieldData = objectDTO.clone();
							returnedValue = f.apply(fieldData);
						} else {
							returnedValue = function.apply(value);
						}
					
						if (returnedValue instanceof Optional) {
							returnedValue = ObjectUtil.unwrap(returnedValue);
						}
						
						if (returnedValue == null) {
							return null; // just ignore it, right?
						} else if (Enum.class.isAssignableFrom(returnedValue.getClass())) {
							en = (Enum) returnedValue;
							
						} else {
							objectDTO.valueToProcess = returnedValue;
							return object2String(objectDTO);
						}
						
					}
					
				} catch (Exception e) {}
			}
		} catch (Exception ex) {}
		
		
		String name = en.name();
		
		if (enumType == null || enumType == EnumType.STRING) {

			for (Method method: valueType.getDeclaredMethods()) {
				for (Annotation annotation: method.getDeclaredAnnotations()) {
					String aname = annotation.annotationType().getName();
					
					switch (aname) {
						case "com.fasterxml.jackson.annotation.JsonValue":
						case "org.codehaus.jackson.annotate.JsonValue":
							return ObjectUtil.getMethodValue(en, method);
							
						case "ca.oson.json.annotation.FieldMapper":
							ca.oson.json.annotation.FieldMapper fieldMapper = (ca.oson.json.annotation.FieldMapper)annotation;
							if (fieldMapper.jsonValue() != null && fieldMapper.jsonValue() == BOOLEAN.TRUE) {
								return ObjectUtil.getMethodValue(en, method);
							}
					}
				}

			}
			
			for (Field field: valueType.getDeclaredFields()) {
				if (name.equalsIgnoreCase(field.getName())) {
					ca.oson.json.annotation.FieldMapper fieldMapper = field.getAnnotation(ca.oson.json.annotation.FieldMapper.class);
					if (fieldMapper != null) {
						String aname = fieldMapper.name();
						if (!StringUtil.isEmpty(aname)) {
							return aname;
						}
						
					} else {
						for (Annotation annotation: field.getAnnotations()) {
							String aname = ObjectUtil.getName(annotation);
							if (!StringUtil.isEmpty(aname)) {
								return aname;
							}
						}
					}
				}
			}

			return name;
		}

		switch (enumType) {
		case STRING:
			return en.name();
		case ORDINAL:
		default:
			return "" + en.ordinal();
		}
	}
	
	
	
	private <E> String collection2Json(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<?> returnType = objectDTO.returnType;
		

		if (value != null) {
			Collection collection = (Collection) value;
			Function function = objectDTO.getSerializer();
			String valueToReturn = null;
			
			if (function != null) {
				try {

					// suppose to return String, but in case not, try to process
					if (function instanceof DataMapper2JsonFunction) {
						DataMapper classData = new DataMapper(returnType, collection, objectDTO.classMapper, objectDTO.level, getPrettyIndentation());
						return ((DataMapper2JsonFunction)function).apply(classData);
						
					} else if (function instanceof Collection2JsonFunction) {
						return ((Collection2JsonFunction)function).apply(collection);
							
					} else {
						
						Object returnedValue = null;
						if (function instanceof FieldData2JsonFunction) {
							FieldData2JsonFunction f = (FieldData2JsonFunction)function;
							FieldData fieldData = objectDTO.clone();
							returnedValue = f.apply(fieldData);
						} else {
							returnedValue = function.apply(value);
						}
	
						if (returnedValue instanceof Optional) {
							returnedValue = ObjectUtil.unwrap(returnedValue);
						}
						
						if (returnedValue == null) {
							return null;
							
						} else if (Collection.class.isAssignableFrom(returnedValue.getClass())) {
							// keep on processing
							collection = (Collection) returnedValue;
							
						} else {
							objectDTO.valueToProcess = returnedValue;
							return object2String(objectDTO);
						}
					}
					
				} catch (Exception ex) {}
			}

			String repeated = getPrettyIndentationln(objectDTO.level);
			objectDTO.incrLevel();
			String repeatedItem = getPrettyIndentationln(objectDTO.level);
			StringBuilder sbuilder = new StringBuilder();
			Class ctype = objectDTO.getComponentType(getJsonClassType());

			try {
				List<String> list = new ArrayList<>();
					
				for (Object s : collection) {
					String str = null;
					if (s != null) {
						FieldData newFieldData = new FieldData(s, s.getClass(), objectDTO.json2Java, objectDTO.level, objectDTO.set);
						newFieldData.returnType = guessComponentType(newFieldData);
						newFieldData.fieldMapper = objectDTO.fieldMapper;
						str = object2Json(newFieldData);
					}
					
					if (str == null && ((ctype != null && ctype.isPrimitive())
							|| getDefaultType() == JSON_INCLUDE.DEFAULT) ) {
						str = getDefaultValue(ctype).toString();
					}
					
					list.add(str);
				}
				
				if (objectDTO.classMapper.orderArrayAndList) {
					Collections.sort(list);
				}
				
				for (String str: list) {
					sbuilder.append(repeatedItem + str + ",");
				}
				
			} catch (Exception ex) {}
		
			String str = sbuilder.toString();
			int size = str.length();
			if (size > 0) {
				return "[" + str.substring(0, size - 1) + repeated + "]";
			} else {
				switch (objectDTO.defaultType) {
				case ALWAYS:
					return "[]";
				case NON_NULL:
					return "[]";
				case NON_EMPTY:
					return null;
				case NON_DEFAULT:
					return null;
				case DEFAULT:
					return "[]";
				default:
					return "[]";
				}
				
			}
		}
		
		return collection2JsonDefault(objectDTO);
	}
	
	
	private String collection2JsonDefault(FieldData objectDTO) {
		Collection valueToReturn = json2CollectionDefault(objectDTO);
		
		if (valueToReturn == null) {
			return null;
		}

		switch (objectDTO.defaultType) {
		case ALWAYS:
			return "[]";
		case NON_NULL:
			return "[]";
		case NON_EMPTY:
			return null;
		case NON_DEFAULT:
			return null;
		case DEFAULT:
			return "[]";
		default:
			return "[]";
		}
	}
	
	private <E> Collection json2CollectionDefault(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required();

		if (getDefaultType() == JSON_INCLUDE.DEFAULT || required) {
			Collection defaultValue = (Collection)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				return defaultValue;
			}

			return DefaultValue.collection(returnType);
		}

		return null;
	}
	
	private <E,R> String optional2Json(FieldData objectDTO) {
		E value = (E) objectDTO.valueToProcess;
		
		value = ObjectUtil.unwraponce(value);

		objectDTO.valueToProcess = value;
		if (value != null) {
			objectDTO.returnType = (Class<R>) value.getClass();
		}
		
		String repeated = getPrettyIndentationln(objectDTO.level), pretty = getPrettySpace();
		objectDTO.incrLevel();
		String repeatedItem = getPrettyIndentationln(objectDTO.level);
	
		return "{" + repeatedItem + "\"value\":" + pretty + object2Json(objectDTO) + repeated + "}";
	}

	
	private String object2String(FieldData objectDTO) {
		Object returnedValue = objectDTO.valueToProcess;

		if (returnedValue == null) {
			return null;
			
		} else if (returnedValue instanceof String) {
			String str = (String) returnedValue;
			if (objectDTO.isJsonRawValue()) {
				return str;
			} else if (objectDTO.doubleQuote) {
				return StringUtil.doublequote(str, isEscapeHtml());
			} else {
				return str;
			}
		}
		
		Class type = returnedValue.getClass();
		
		if (type.isPrimitive()) {
			return returnedValue + "";
			
		} else if (Number.class.isAssignableFrom(type)) {
			return NumberUtil.toPlainString((Number)returnedValue, this.isAppendingFloatingZero());
			
		} else if (Enum.class.isAssignableFrom(type)) {
			return enum2Json(objectDTO);
			
		} else if (returnedValue instanceof Character) {
			String str = ((Character)returnedValue).toString();
			if (objectDTO.isJsonRawValue()) {
				return str;
			} else if (objectDTO.doubleQuote) {
				return StringUtil.doublequote(str, isEscapeHtml());
			} else {
				return str;
			}
			
		} else if (Date.class.isAssignableFrom(returnedValue.getClass())) {
			Date valueToProcess = (Date)returnedValue;
			
			Boolean date2Long = objectDTO.getDate2Long();

			if (date2Long != null && date2Long) {
				long longtoprocess = valueToProcess.getTime();
				return NumberUtil.toPlainString(longtoprocess);
			} else {
				String str = objectDTO.getDateFormat().format(valueToProcess);
				
				if (objectDTO.isJsonRawValue()) {
					return str;
				} else if (objectDTO.doubleQuote) {
					return StringUtil.doublequote(str, isEscapeHtml());
				} else {
					return str;
				}
			}
			
		} else if (returnedValue instanceof Boolean) {
			return ((Boolean) returnedValue).toString();

		} else {
			
			int oldLevel = this.getLevel();
			boolean oldPrettyPrinting = this.getPrettyPrinting();

			// now change it
			this.setLevel(5);
			this.pretty(false);

			String json = null;
			
			objectDTO.returnType = type;
			
			if (Enum.class.isAssignableFrom(type)) {
				json = enum2Json(objectDTO);			

			} else if (Collection.class.isAssignableFrom(type)) {
				json = collection2Json(objectDTO);
				
			} else if (type.isArray()) {
				json = array2Json(objectDTO);
				
			} else if (Map.class.isAssignableFrom(type)) {
				json = map2Json(objectDTO);
				
			} else {
				objectDTO.classMapper = getGlobalizedClassMapper(type);
				json = object2Serialize(objectDTO);
				objectDTO.jsonRawValue = true;
			}
			
			// set it back
			this.setLevel(oldLevel);
			this.pretty(oldPrettyPrinting);
			
			return json;
		}
	}
	
	
	private boolean isMapListArrayObject(String str) {
		if ( (str.startsWith("{") && str.endsWith("}") && str.contains(this.getJsonClassType()))
				|| (str.startsWith("[") && str.endsWith("]"))
				) {
			return true;
		}
		
		return false;
	}
	
	
	private <E> String processNullValue(int level) {
		if (level > 0) {
			return null;
		}
		
		switch(getDefaultType()) {
		case ALWAYS: return "null";
		case NON_NULL: return "";
		case NON_EMPTY: return "";
		case NON_DEFAULT: return "";
		case DEFAULT: return "null";
		case NONE: return "null";
		default: return "null";
		}
	}
	
	//, int level, Set set
	private <E,R> String object2Json(FieldData objectDTO) {
		E value = (E) objectDTO.valueToProcess;
		
		Class<R> returnType = objectDTO.returnType;
		
		if (returnType == null || returnType == Object.class) {
			if (value != null) {
				returnType = (Class<R>) value.getClass();
				objectDTO.returnType = returnType;
			}
		}
		
		int level  = objectDTO.level;
//		if (value == null) {
//			if ((returnType != null && returnType.isPrimitive())  || objectDTO.required()) {
//				Object obj = DefaultValue.getSystemDefault(returnType);
//				if (obj != null) {
//					return obj.toString();
//				}
//				
//			}
//
//			return processNullValue(level);
//		}
		
		// first, get the class mapper
		//if (objectDTO.mapper == null && objectDTO.classMapper == null) {
			objectDTO.classMapper = getGlobalizedClassMapper(returnType);
		//}

		if (objectDTO.defaultType == null) {
			objectDTO.defaultType = getDefaultType();
		}

		if (returnType == null) {
			if (value == null) {
				return processNullValue(level);
			} else if (objectDTO.isJsonRawValue()) {
				return value.toString();
			} else {
				return StringUtil.doublequote(value, isEscapeHtml());
			}

		} else if (value instanceof Optional) {
			return optional2Json(objectDTO);
			
		} else if (returnType == String.class) {
			value = (E) string2Json(objectDTO);

			if (value == null) {
				return processNullValue(level);
			}
			
			String str = value.toString();

			if (objectDTO.isJsonRawValue()) {
				return str;
			} else {
				if ( isMapListArrayObject(str) ) {
					return str;
					
				} else {
					return StringUtil.doublequote(str, isEscapeHtml());
				}
			}
			
			
		} else if (Character.class.isAssignableFrom(returnType) || char.class.isAssignableFrom(returnType)) {
			String valueToReturn = character2Json(objectDTO);

			if (valueToReturn == null) {
				return processNullValue(level);
			}

			if (objectDTO.isJsonRawValue()) {
				return (String) valueToReturn;
			} else {
				return StringUtil.doublequote(valueToReturn, isEscapeHtml());
			}

		} else if (BooleanUtil.isBoolean(returnType) && (value == null || BooleanUtil.isBoolean(value.getClass()))) {
			String returnedValue = boolean2Json(objectDTO);
			
			if (returnedValue != null) {
				if (objectDTO.isJsonRawValue()) {
					return returnedValue;
					
				} else if ("true".equalsIgnoreCase(returnedValue)) {
					return "true";
					
				} else if ("false".equalsIgnoreCase(returnedValue)) {
					return "false";
					
				} else if (StringUtil.isNumeric(returnedValue)) {
					return returnedValue;
					
				} else {
					return StringUtil.doublequote(returnedValue, isEscapeHtml());
				}
			}
			
			return processNullValue(level);
			
			
		} else if (Number.class.isAssignableFrom(returnType) || returnType.isPrimitive()) {
			
			if (returnType == Number.class && value != null) {
				returnType = (Class<R>) value.getClass();
				objectDTO.returnType = returnType;
			}
			
			String returnedValue = null;
			
			if (returnType == Integer.class || returnType == int.class) {
				returnedValue = integer2Json(objectDTO);
				
			} else if (returnType == Long.class || returnType == long.class) {
				returnedValue = long2Json(objectDTO);
				
			} else if (returnType == Byte.class || returnType == byte.class) {
				returnedValue = byte2Json(objectDTO);

			} else if (returnType == Double.class || returnType == double.class) {
				returnedValue = double2Json(objectDTO);

			} else if (returnType == Short.class || returnType == short.class) {
				returnedValue = short2Json(objectDTO);
	
			} else if (returnType == Float.class || returnType == float.class) {
				returnedValue = float2Json(objectDTO);
				
			} else if (returnType == BigDecimal.class) {
				returnedValue = bigDecimal2Json(objectDTO);
				
			} else if (returnType == BigInteger.class) {
				returnedValue = bigInteger2Json(objectDTO);

			} else if (returnType == AtomicInteger.class) {
				returnedValue = atomicInteger2Json(objectDTO);
				
			} else if (returnType == AtomicLong.class) {
				returnedValue = atomicLong2Json(objectDTO);
				
			} else if (value != null) {
				returnedValue = double2Json(objectDTO);
				
			} else {
				return processNullValue(level);
			}
			
			
			if (returnedValue != null) {
				if (objectDTO.isJsonRawValue()) {
					return returnedValue;
					
				} else if (StringUtil.isNumeric(returnedValue)) {
					return returnedValue;
					
				} else {
					return StringUtil.doublequote(returnedValue, isEscapeHtml());
				}
			}
			
			return processNullValue(level);

		} else if (Date.class.isAssignableFrom(returnType)) {
			String returnedValue = date2Json(objectDTO);
			
			if (returnedValue != null) {
				if (objectDTO.isJsonRawValue()) {
					return returnedValue;
					
				} else if (StringUtil.isNumeric(returnedValue)) {
					return returnedValue;
					
				} else {
					return StringUtil.doublequote(returnedValue, isEscapeHtml());
				}
			}
			
			return processNullValue(level);

		// returnType.isEnum()  || value instanceof Enum<?>
		} else if (Enum.class.isAssignableFrom(returnType)) {
			
			String returnedValue = enum2Json(objectDTO);

			if (returnedValue != null) {
				if (objectDTO.isJsonRawValue()) {
					return returnedValue;
					
				} else if (StringUtil.isNumeric(returnedValue)) {
					return returnedValue;
					
				} else {
					return StringUtil.doublequote(returnedValue, isEscapeHtml());
				}
			}
			
			return processNullValue(level);
			
		} else if (Collection.class.isAssignableFrom(returnType)) {
			return collection2Json(objectDTO);

		} else if (returnType.isArray()) {
			return array2Json(objectDTO);

		} else if (Map.class.isAssignableFrom(returnType)) {
			return map2Json(objectDTO);

		} else if (objectDTO.level < getLevel()) {
			if (value == null) {
				return null;
			}

			return object2Serialize(objectDTO);
			
		} else {
			return "{}";
		}
	}
	
	private <T> Method getOtherMethodByName(String name, T obj) {
		if (obj == null || name == null) {
			return null;
		}
		Class<T> valueType = (Class<T>) obj.getClass();
		if (valueType == null) {
			return null;
		}
		return getOtherMethodByName(name, valueType);
	}

	private <T> Method getOtherMethodByName(String name, Class<T> valueType) {
		Map <String, Method> otherMethods = getOtherMethods(valueType);

		if (otherMethods == null || name == null) {
			return null;
		}
		return otherMethods.get(name);
	}
	private <T> Method getSetterByName(String name, T obj) {
		if (obj == null || name == null) {
			return null;
		}
		Class<T> valueType = (Class<T>) obj.getClass();
		if (valueType == null) {
			return null;
		}
		return getSetterByName(name, valueType);
	}

	private <T> Method getSetterByName(String name, Class<T> valueType) {
		Map <String, Method> sets = getSetters(valueType);

		if (sets == null || name == null) {
			return null;
		}
		return sets.get(name);
	}
	
	private <T> Method getGetterByName(String name, T obj) {
		if (obj == null || name == null) {
			return null;
		}
		Class<T> valueType = (Class<T>) obj.getClass();
		if (valueType == null) {
			return null;
		}

		return getGetterByName(name, valueType);
	}

	private <T> Method getGetterByName(String name, Class<T> valueType) {
		Map <String, Method> gets = getGetters(valueType);

		if (gets == null || name == null) {
			return null;
		}
		return gets.get(name);
	}
	
	private <T> Map <String, Method> getSetters(T obj) {
		if (obj == null) {
			return null;
		}
		Class<T> valueType = (Class<T>) obj.getClass();

		return getSetters(valueType);
	}
	
	private static boolean ignored(Class valueType) {
		if (valueType == null || valueType == ClassLoader.class
				|| valueType == Object.class
				|| valueType.getName().startsWith("java.security.")
				|| valueType.getName().startsWith("sun.reflect.")
				) {
			return true;
		}
		
		return false;
	}
	
	private <T> Map <String, Method> getSetters(Class<T> valueType) {
		String fullName = valueType.getName();
		
		if (ignored(valueType)) {
			return new HashMap <>();
		}
		
		if (!cachedMethods.containsKey(fullName)) {
			processMethods(valueType, fullName);
		}
		
		// return a new copy, so original copy will not get modified
		return cachedMethods.get(fullName)[METHOD.SET.value];
	}
	private <T> Map <String, Method> getGetters(T obj) {
		if (obj == null) {
			return null;
		}
		Class<T> valueType = (Class<T>) obj.getClass();

		return getGetters(valueType);
	}
	private <T> Map <String, Method> getGetters(Class<T> valueType) {
		if (ignored(valueType)) {
			return new HashMap <>();
		}
		
		String fullName = valueType.getName();
		if (!cachedMethods.containsKey(fullName)) {
			processMethods(valueType, fullName);
		}
		
		return cachedMethods.get(fullName)[METHOD.GET.value];
	}
	private <T> Map <String, Method> getOtherMethods(T obj) {
		if (obj == null) {
			return null;
		}
		Class<T> valueType = (Class<T>) obj.getClass();

		return getOtherMethods(valueType);
	}
	private <T> Map <String, Method> getOtherMethods(Class<T> valueType) {
		if (ignored(valueType)) {
			return new HashMap <>();
		}
		
		String fullName = valueType.getName();
		if (!cachedMethods.containsKey(fullName)) {
			processMethods(valueType, fullName);
		}
		
		return cachedMethods.get(fullName)[METHOD.OTHER.value];
	}
	
	private <T> void processMethods(Class<T> valueType, String fullName) {
//		Stream<Method> stream = Arrays.stream(valueType.getDeclaredMethods());
//		while (valueType != null && valueType != Object.class) {
//			stream = Stream.concat(stream, Arrays.stream(valueType
//					.getSuperclass().getDeclaredMethods()));
//			valueType = (Class<T>) valueType.getSuperclass();
//		}

		Map <String, Method> setters = new LinkedHashMap<>();
		Map <String, Method> getters = new LinkedHashMap<>();
		Map <String, Method> others = new LinkedHashMap<>();
		
		String name;
		boolean notSetGetOnly = !getSetGetOnly();
		boolean completed = false;
		Class classType = valueType;
		int depth = 0;
		while (!completed && classType != null && classType != Object.class) {
			//for (Method method: stream.collect(Collectors.toList())) {
			for (Method method: classType.getDeclaredMethods()) {
				name = method.getName();
				
				if (name.startsWith("set")) {
					if (name.length() > 3 && method.getParameterCount() == 1) {
						setters.put(name.substring(3).toLowerCase(), method);
					} else {
						others.put(name.toLowerCase(), method);
					}
					
				} else if (name.startsWith("get")) {
					if (name.length() > 3 && method.getParameterCount() == 0 && !void.class.equals(method.getReturnType())) {
						getters.put(name.substring(3).toLowerCase(), method);
					} else {
						others.put(name.toLowerCase(), method);
					}
					
				} else if (notSetGetOnly && method.getParameterCount() == 0 && !void.class.equals(method.getReturnType())) {
					getters.put(name.toLowerCase(), method);
					
				} else if (notSetGetOnly && method.getParameterCount() == 1) {
					setters.put(name.toLowerCase(), method);
					
				} else {
					others.put(name.toLowerCase(), method);
				}
			}
		
			if (depth > 1 && (getters.size() > 0 || setters.size() > 0)) {
				completed = true;
			} else {
				classType = classType.getSuperclass();
				depth++;
			}
		}
		
		Map <String, Method>[] all = new HashMap[3];
		
		all[METHOD.GET.value] = getters;
		all[METHOD.SET.value] = setters;
		all[METHOD.OTHER.value] = others;
		cachedMethods.put(fullName, all);
	}
	
	
	public static <T> Field[] getFields(T obj) {
		Class<T> valueType = (Class<T>) obj.getClass();
		// try {
		// valueType = (Class<T>)Class.forName(obj.getClass().getName());
		// } catch (ClassNotFoundException e) {
		// e.printStackTrace();
		// }

		return getFields(valueType);
	}

	public static <T> Field[] getFields(Class<T> valueType) {
		if (ignored(valueType)) {
			return new Field[0];
		}

		if (cachedFields.containsKey(valueType)) {
			return cachedFields.get(valueType);
		}

		Stream<Field> stream = Arrays.stream(valueType.getDeclaredFields());
		Class valueTypeAll = valueType.getSuperclass();
		while (valueTypeAll != null && valueTypeAll != Object.class) {
			stream = Stream.concat(stream, Arrays.stream(valueTypeAll.getDeclaredFields()));
			valueTypeAll = valueTypeAll.getSuperclass();
		}

		//(Field[]) stream.distinct().toArray(size -> new Field[size])
		List<Field> uniqueFields = new ArrayList<>();
		Set<String> set = new HashSet<>();
		for (Field field: stream.collect(Collectors.toList())) {
			String name = field.getName().toLowerCase();
			Class ftype = field.getType();
			String fname = ftype.getName();
			// && ftype != valueType
			if (!set.contains(name) && !name.startsWith("this$") && ftype != Class.class
					&& ftype != Field.class && ftype != Field[].class && ftype != sun.reflect.ReflectionFactory.class
					// && ftype != ClassLoader.class
					&& !fname.startsWith("java.security.") && !fname.startsWith("[Ljava.security.") && !fname.startsWith("java.lang.Class$")
					&& !fname.startsWith("sun.reflect.") 
					) {
				set.add(name);
				uniqueFields.add(field);
			}
		}

		cachedFields.put(valueType, uniqueFields.toArray(new Field[uniqueFields.size()]));

		return cachedFields.get(valueType);
	}


	private ClassMapper overwriteBy (ClassMapper classMapper, ca.oson.json.annotation.ClassMapper classMapperAnnotation) {
		if (classMapper == null || classMapperAnnotation == null) {
			return classMapper;
		}
		
		if (classMapperAnnotation.ignore() != BOOLEAN.NONE) {
			classMapper.ignore = classMapperAnnotation.ignore().value();
		}
		
		if (classMapperAnnotation.includeClassTypeInJson() != BOOLEAN.NONE) {
			classMapper.includeClassTypeInJson = classMapperAnnotation.includeClassTypeInJson().value();
		}
		
		if (classMapperAnnotation.orderByKeyAndProperties() != BOOLEAN.NONE) {
			classMapper.orderByKeyAndProperties = classMapperAnnotation.orderByKeyAndProperties().value();
		}
		
		if (classMapperAnnotation.orderArrayAndList() != BOOLEAN.NONE) {
			classMapper.orderArrayAndList = classMapperAnnotation.orderArrayAndList().value();
		}

		if (classMapperAnnotation.useField() != BOOLEAN.NONE) {
			classMapper.useField = classMapperAnnotation.useField().value();
		}
		
		if (classMapperAnnotation.useAttribute() != BOOLEAN.NONE) {
			classMapper.useAttribute = classMapperAnnotation.useAttribute().value();
		}
		
		if (classMapperAnnotation.since() > 0) {
			classMapper.since = classMapperAnnotation.since();
		}
		
		if (classMapperAnnotation.until() > 0) {
			classMapper.until = classMapperAnnotation.until();
		}
		
		String defaultValue = classMapperAnnotation.defaultValue();
		if (defaultValue != null && defaultValue.length() > 0) {
			classMapper.defaultValue = defaultValue;
		}
		
		String[] ignoreFieldsWithAnnotations = classMapperAnnotation.ignoreFieldsWithAnnotations();
		if (ignoreFieldsWithAnnotations != null && ignoreFieldsWithAnnotations.length > 0) {
			if (classMapper.ignoreFieldsWithAnnotations == null) {
				classMapper.ignoreFieldsWithAnnotations = new HashSet();
			}
			for (String ignoreFieldsWithAnnotation: ignoreFieldsWithAnnotations) {
				classMapper.ignoreFieldsWithAnnotations.add(ComponentType.forName(ignoreFieldsWithAnnotation));
			}
		}
		
		String[] jsonIgnoreProps = classMapperAnnotation.jsonIgnoreProperties();
		if (jsonIgnoreProps != null && jsonIgnoreProps.length > 0) {
			if (classMapper.jsonIgnoreProperties == null) {
				classMapper.jsonIgnoreProperties = new HashSet();
			}
			for (String jsonIgnoreProp: jsonIgnoreProps) {
				classMapper.jsonIgnoreProperties.add(jsonIgnoreProp);
			}
		}

		String simpleDateFormat = classMapperAnnotation.simpleDateFormat();
		if (simpleDateFormat != null && simpleDateFormat.length() > 0) {
			classMapper.setSimpleDateFormat(simpleDateFormat);
		}

		String[] propertyOrders = classMapperAnnotation.propertyOrders();
		if (propertyOrders != null && propertyOrders.length > 0) {
			classMapper.propertyOrders = propertyOrders;
		}
		
		MODIFIER[] modifiers = classMapperAnnotation.includeFieldsWithModifiers();
		if (modifiers != null && modifiers.length > 0) {
			classMapper.includeFieldsWithModifiers = new HashSet(Arrays.asList(modifiers));
		}

		if (classMapperAnnotation.defaultType() != JSON_INCLUDE.NONE) {
			classMapper.defaultType = classMapperAnnotation.defaultType();
		}
		
		if (classMapperAnnotation.enumType() != ENUM_TYPE.NONE) {
			classMapper.enumType = classMapperAnnotation.enumType().value();
		}
		
		if (classMapperAnnotation.date2Long() != BOOLEAN.NONE) {
			classMapper.date2Long = classMapperAnnotation.date2Long().value();
		}
		
		if (classMapperAnnotation.escapeHtml() != BOOLEAN.NONE) {
			classMapper.escapeHtml = classMapperAnnotation.escapeHtml().value();
		}
		
		if (classMapperAnnotation.length() > 0) {
			classMapper.length = classMapperAnnotation.length();
		}
		
		if (classMapperAnnotation.precision() > 0) {
			classMapper.precision = classMapperAnnotation.precision();
		}
		
		if (classMapperAnnotation.scale() > 0) {
			classMapper.scale = classMapperAnnotation.scale();
		}
		
		if (classMapperAnnotation.min() > 0) {
			classMapper.min = classMapperAnnotation.min();
		}
		
		if (classMapperAnnotation.max() > 0) {
			classMapper.max = classMapperAnnotation.max();
		}
		

		return classMapper;
	}
	
	public static ClassMapper overwriteBy (ClassMapper classMapper, ClassMapper javaClassMapper) {
		if (classMapper == null || javaClassMapper == null) {
			return classMapper;
		}
		
		if (javaClassMapper.date2Long != null) {
			classMapper.date2Long = javaClassMapper.date2Long;
		}
	
		if (javaClassMapper.ignore != null) {
			classMapper.ignore = javaClassMapper.ignore;
		}
		
		if (javaClassMapper.includeClassTypeInJson != null) {
			classMapper.includeClassTypeInJson = javaClassMapper.includeClassTypeInJson;
		}
		
		if (javaClassMapper.orderByKeyAndProperties != null) {
			classMapper.orderByKeyAndProperties = javaClassMapper.orderByKeyAndProperties;
		}
		
		if (javaClassMapper.orderArrayAndList != null) {
			classMapper.orderArrayAndList = javaClassMapper.orderArrayAndList;
		}
		
		if (javaClassMapper.useAttribute != null) {
			classMapper.useAttribute = javaClassMapper.useAttribute;
		}
		
		if (javaClassMapper.useField != null) {
			classMapper.useField = javaClassMapper.useField;
		}
		
		if (javaClassMapper.defaultType != null) {
			classMapper.defaultType = javaClassMapper.defaultType;
		}
		
		if (javaClassMapper.defaultValue != null) {
			classMapper.defaultValue = javaClassMapper.defaultValue;
		}
		
		if (javaClassMapper.deserializer != null) {
			classMapper.deserializer = javaClassMapper.deserializer;
		}
		
		if (javaClassMapper.enumType != null) {
			classMapper.enumType = javaClassMapper.enumType;
		}
		
		if (javaClassMapper.ignoreFieldsWithAnnotations != null) {
			classMapper.ignoreFieldsWithAnnotations = javaClassMapper.ignoreFieldsWithAnnotations;
		}
		
		if (javaClassMapper.since != null) {
			classMapper.since = javaClassMapper.since;
		}
		
		if (javaClassMapper.until != null) {
			classMapper.until = javaClassMapper.until;
		}
		
		if (javaClassMapper.includeFieldsWithModifiers != null) {
			classMapper.includeFieldsWithModifiers = javaClassMapper.includeFieldsWithModifiers;
		}
		
		if (javaClassMapper.jsonIgnoreProperties != null) {
			classMapper.jsonIgnoreProperties = javaClassMapper.jsonIgnoreProperties;
		}
		
		if (javaClassMapper.max != null) {
			classMapper.max = javaClassMapper.max;
		}
		
		if (javaClassMapper.min != null) {
			classMapper.min = javaClassMapper.min;
		}
		
		if (javaClassMapper.propertyOrders != null) {
			classMapper.propertyOrders = javaClassMapper.propertyOrders;
		}
		
		if (javaClassMapper.scale != null) {
			classMapper.scale = javaClassMapper.scale;
		}
		
		if (javaClassMapper.serializer != null) {
			classMapper.serializer = javaClassMapper.serializer;
		}
		
		DateFormat dateFormat = javaClassMapper.getDateFormat();
		if (dateFormat != null) {
			classMapper.setDateFormat(dateFormat);
		}
		
		if (javaClassMapper.length != null) {
			classMapper.length = javaClassMapper.length;
		}
		
		if (javaClassMapper.precision != null) {
			classMapper.precision = javaClassMapper.precision;
		}
		
		if (javaClassMapper.isToStringAsSerializer() != null) {
			classMapper.setToStringAsSerializer(javaClassMapper.isToStringAsSerializer());
		}
		
		if (javaClassMapper.jsonValueFieldName != null) {
			classMapper.jsonValueFieldName = javaClassMapper.jsonValueFieldName;
		}
		
		return classMapper;
	}
	

	private ClassMapper overwriteBy(ClassMapper classMapper, FieldMapper fieldMapper) {
		// fieldify ?
		if (fieldMapper.useAttribute != null) {
			classMapper.useAttribute = fieldMapper.useAttribute;
		}
		
		if (fieldMapper.useField != null) {
			classMapper.useField = fieldMapper.useField;
		}

		DateFormat dateFormat = fieldMapper.getDateFormat();
		if (dateFormat != null) {
			classMapper.setDateFormat(dateFormat);
		}
		
		if (fieldMapper.defaultType != null) {
			classMapper.defaultType = fieldMapper.defaultType;
		}
		
		if (fieldMapper.enumType != null) {
			classMapper.enumType = fieldMapper.enumType;
		}
		
		if (fieldMapper.date2Long != null) {
			classMapper.date2Long =  fieldMapper.date2Long;
		}
		
		if (fieldMapper.precision != null) {
			classMapper.precision =  fieldMapper.precision;
		}
		
		if (fieldMapper.scale != null) {
			classMapper.scale =  fieldMapper.scale;
		}
		
		if (fieldMapper.min != null) {
			classMapper.min =  fieldMapper.min;
		}
		
		if (fieldMapper.max != null) {
			classMapper.max =  fieldMapper.max;
		}
		
		if (fieldMapper.length != null) {
			classMapper.length =  fieldMapper.length;
		}
		
		if (fieldMapper.defaultValue != null) {
			classMapper.defaultValue =  fieldMapper.defaultValue;
		}
		
		if (fieldMapper.ignore != null) {
			classMapper.ignore =  fieldMapper.ignore;
		}
		
		if (fieldMapper.since != null) {
			classMapper.since =  fieldMapper.since;
		}
		
		if (fieldMapper.until != null) {
			classMapper.until =  fieldMapper.until;
		}
		
		return classMapper;
	}
	
	
	
	private FieldMapper overwriteBy (FieldMapper fieldMapper, ca.oson.json.annotation.FieldMapper fieldMapperAnnotation, ClassMapper classMapper) {
		if (fieldMapperAnnotation.length() > 0) {
			fieldMapper.length = fieldMapperAnnotation.length();
		}
		
		if (fieldMapperAnnotation.min() > 0) {
			fieldMapper.min = fieldMapperAnnotation.min();
		}
		
		if (fieldMapperAnnotation.max() > 0) {
			fieldMapper.max = fieldMapperAnnotation.max();
		}
		if (fieldMapperAnnotation.scale() > 0) {
			fieldMapper.scale = fieldMapperAnnotation.scale();
		}
		if (fieldMapperAnnotation.precision() > 0) {
			fieldMapper.precision = fieldMapperAnnotation.precision();
		}
		
		if (fieldMapperAnnotation.ignore() != BOOLEAN.NONE) {
			fieldMapper.ignore = fieldMapperAnnotation.ignore().value();
		}
		
		if (fieldMapperAnnotation.jsonRawValue() != BOOLEAN.NONE) {
			fieldMapper.jsonRawValue = fieldMapperAnnotation.jsonRawValue().value();
		}
		
		if (fieldMapperAnnotation.jsonValue() != BOOLEAN.NONE) {
			fieldMapper.jsonValue = fieldMapperAnnotation.jsonValue().value();
		}
		
		if (fieldMapperAnnotation.jsonNoName() != BOOLEAN.NONE) {
			fieldMapper.jsonNoName = fieldMapperAnnotation.jsonNoName().value();
		}
		
		if (fieldMapperAnnotation.jsonAnySetter() != BOOLEAN.NONE) {
			fieldMapper.jsonAnySetter = fieldMapperAnnotation.jsonAnySetter().value();
		}
		
		if (fieldMapperAnnotation.jsonAnyGetter() != BOOLEAN.NONE) {
			fieldMapper.jsonAnyGetter = fieldMapperAnnotation.jsonAnyGetter().value();
		}
		
		if (fieldMapperAnnotation.useField() != BOOLEAN.NONE) {
			fieldMapper.useField = fieldMapperAnnotation.useField().value();
		}
		
		if (fieldMapperAnnotation.useAttribute() != BOOLEAN.NONE) {
			fieldMapper.useAttribute = fieldMapperAnnotation.useAttribute().value();
		}
		
		if (fieldMapperAnnotation.defaultType() != JSON_INCLUDE.NONE) {
			fieldMapper.defaultType = fieldMapperAnnotation.defaultType();
		}
		
		String defaultValue = fieldMapperAnnotation.defaultValue();
		if (defaultValue != null && defaultValue.length() > 0) {
			fieldMapper.defaultValue = defaultValue;
		}
		
		if (fieldMapperAnnotation.required() != BOOLEAN.NONE) {
			fieldMapper.required = fieldMapperAnnotation.required().value();
		}

		if (fieldMapperAnnotation.enumType() != ENUM_TYPE.NONE) {
			fieldMapper.enumType = fieldMapperAnnotation.enumType().value();
		}
		
		if (fieldMapperAnnotation.date2Long() != BOOLEAN.NONE) {
			fieldMapper.date2Long = fieldMapperAnnotation.date2Long().value();
		}
		
		String simpleDateFormat = fieldMapperAnnotation.simpleDateFormat();
		if (simpleDateFormat != null && simpleDateFormat.length() > 0) {
			fieldMapper.setSimpleDateFormat(simpleDateFormat);
		}

		if (fieldMapperAnnotation.since() > 0) {
			fieldMapper.since = fieldMapperAnnotation.since();
		}
		
		if (fieldMapperAnnotation.until() > 0) {
			fieldMapper.until = fieldMapperAnnotation.until();
		}
		
		if (!StringUtil.isEmpty(fieldMapperAnnotation.name())) {
			fieldMapper.json = fieldMapperAnnotation.name();
		}
		
		return fieldMapper;
	}
	
	
	
	public static FieldMapper overwriteBy (FieldMapper fieldMapper, FieldMapper javaFieldMapper) {
		if (fieldMapper == null || javaFieldMapper == null) {
			return fieldMapper;
		}
		
		if (javaFieldMapper.date2Long != null) {
			fieldMapper.date2Long = javaFieldMapper.date2Long;
		}
		
		if (javaFieldMapper.ignore != null) {
			fieldMapper.ignore = javaFieldMapper.ignore;
		}
		
		if (javaFieldMapper.jsonAnyGetter != null) {
			fieldMapper.jsonAnyGetter = javaFieldMapper.jsonAnyGetter;
		}
		
		if (javaFieldMapper.jsonAnySetter != null) {
			fieldMapper.jsonAnySetter = javaFieldMapper.jsonAnySetter;
		}
		
		if (javaFieldMapper.jsonRawValue != null) {
			fieldMapper.jsonRawValue = javaFieldMapper.jsonRawValue;
		}
		
		if (javaFieldMapper.jsonValue != null) {
			fieldMapper.jsonValue = javaFieldMapper.jsonValue;
		}
		
		if (javaFieldMapper.jsonNoName != null) {
			fieldMapper.jsonNoName = javaFieldMapper.jsonNoName;
		}
		
		if (javaFieldMapper.required != null) {
			fieldMapper.required = javaFieldMapper.required;
		}
		
		if (javaFieldMapper.useAttribute != null) {
			fieldMapper.useAttribute = javaFieldMapper.useAttribute;
		}
		
		if (javaFieldMapper.useField != null) {
			fieldMapper.useField = javaFieldMapper.useField;
		}
		
		if (javaFieldMapper.defaultType != JSON_INCLUDE.NONE) {
			fieldMapper.defaultType = javaFieldMapper.defaultType;
		}
		
		if (javaFieldMapper.defaultValue != null) {
			fieldMapper.defaultValue = javaFieldMapper.defaultValue;
		}
		
		if (javaFieldMapper.deserializer != null) {
			fieldMapper.deserializer = javaFieldMapper.deserializer;
		}
		
		if (javaFieldMapper.enumType != null) {
			fieldMapper.enumType = javaFieldMapper.enumType;
		}
		
		if (javaFieldMapper.java != null && !javaFieldMapper.java.equals(javaFieldMapper.json)) {
			fieldMapper.java = javaFieldMapper.java;
			fieldMapper.json = javaFieldMapper.json;
		}
		
		if (javaFieldMapper.length != null) {
			fieldMapper.length = javaFieldMapper.length;
		}
		
		if (javaFieldMapper.max != null) {
			fieldMapper.max = javaFieldMapper.max;
		}
		
		if (javaFieldMapper.min != null) {
			fieldMapper.min = javaFieldMapper.min;
		}
		
		if (javaFieldMapper.scale != null) {
			fieldMapper.scale = javaFieldMapper.scale;
		}
		
		if (javaFieldMapper.precision != null) {
			fieldMapper.precision = javaFieldMapper.precision;
		}
		
		if (javaFieldMapper.serializer != null) {
			fieldMapper.serializer = javaFieldMapper.serializer;
		}
		
		if (javaFieldMapper.getDateFormat() != null) {
			fieldMapper.setDateFormat(javaFieldMapper.getDateFormat());
		}
		
		if (javaFieldMapper.since != null) {
			fieldMapper.since = javaFieldMapper.since;
		}
		
		if (javaFieldMapper.until != null) {
			fieldMapper.until = javaFieldMapper.until;
		}
		
		return fieldMapper;
	}
	
	
	
	/*
	 * Object to string, serialize.
	 * 
	 * It involves 10 steps to apply processing rules:
	 * 1. Create a blank class mapper instance and Globalize it;
	 * 2. if it is a field in previous execution, and set to inherit from previous mapping, combine with previous mapping;
	 * 3. Apply annotations from other sources;
	 * 4. Apply annotations from Oson;
	 * 5. Apply Java configuration for this particular class;
	 * 6. Create a blank field mapper instance for certain property with a returnType;
	 * 7. Get the class mapper of the returnType;
	 * 8. Classify this field mapper with the class mapper of the return type;
	 * 9. Classify this field mapper with the class mapper created at step 5
	 * 10. Apply annotations from other sources;
	 * 11. Apply annotations from Oson;
	 * 12. Apply Java configuration for this particular field.
	 */
	private <E,R> String object2Serialize(FieldData objectDTO) {
		E obj = (E) objectDTO.valueToProcess;
		
		Class<R> valueType = objectDTO.returnType;

		if (obj == null) {
			return null;
		}

		// it is possible the same object shared by multiple variables inside the same enclosing object
		int hash = ObjectUtil.hashCode(obj, valueType);
		if (!objectDTO.goAhead(hash)) {
			return "{}";
		}
		
		ClassMapper classMapper = objectDTO.classMapper;
		// first build up the class-level processing rules
		// || (objectDTO.level == 0 && objectDTO.fieldMapper == null)
		//if (classMapper == null) {
			// 1. Create a blank class mapper instance
			classMapper = new ClassMapper(valueType);
			
			// 2. Globalize it
			classMapper = globalize(classMapper);
			objectDTO.classMapper = classMapper;
		//}
		
		if (objectDTO.fieldMapper != null && isInheritMapping()) {
			classMapper = overwriteBy (classMapper, objectDTO.fieldMapper);
		}
		
		
		FIELD_NAMING format = getFieldNaming();

		String repeated = getPrettyIndentationln(objectDTO.level), pretty = getPrettySpace();
		objectDTO.incrLevel();
		String repeatedItem = getPrettyIndentationln(objectDTO.level);
		

		// @Expose
		Set<String> exposed = null;
		if (isUseGsonExpose()) {
			exposed = new HashSet<>();
		}
		
		boolean annotationSupport = getAnnotationSupport();
		Annotation[] annotations = null;

		if (annotationSupport) {
			annotations = valueType.getAnnotations();
			
			ca.oson.json.annotation.ClassMapper classMapperAnnotation = null;
			
			// 3. Apply annotations from other sources
			for (Annotation annotation : annotations) {
				if (ignoreClass(annotation)) {
					return null;
				}

				switch (annotation.annotationType().getName()) {
				case "ca.oson.json.annotation.ClassMapper":
					classMapperAnnotation = (ca.oson.json.annotation.ClassMapper) annotation;
					if (!(classMapperAnnotation.serialize() == BOOLEAN.BOTH || classMapperAnnotation.serialize() == BOOLEAN.TRUE)) {
						classMapperAnnotation = null;
					}
					break;
					
				case "ca.oson.json.annotation.ClassMappers":
					ca.oson.json.annotation.ClassMappers classMapperAnnotations = (ca.oson.json.annotation.ClassMappers) annotation;
					for (ca.oson.json.annotation.ClassMapper ann: classMapperAnnotations.value()) {
						if (ann.serialize() == BOOLEAN.BOTH || ann.serialize() == BOOLEAN.TRUE) {
							classMapperAnnotation = ann;
							//break;
						}
					}
					break;
				}
			}
			
			// 4. Apply annotations from Oson
			if (classMapperAnnotation != null) {
				classMapper = overwriteBy (classMapper, classMapperAnnotation);
				exposed = null;
			}
			
		}
		
		// 5. Apply Java configuration for this particular class
		ClassMapper javaClassMapper = getClassMapper(valueType);
		if (javaClassMapper != null) {
			classMapper = overwriteBy (classMapper, javaClassMapper);
		}
		
		
		// now processing at the class level
		
		if (classMapper.ignore()) {
			return null;
		}
		
		if (classMapper.since != null && classMapper.since > getVersion()) {
			return null;
		} else if (classMapper.until != null && classMapper.until <= getVersion()) {
			return null;
		}
		
		Function function = classMapper.serializer; //getSerializer(valueType);
		if (function == null) {
			function = DeSerializerUtil.getSerializer(valueType.getName());
		}
		
		if (function != null) {
			try {
				Object returnValue = null;
				if (function instanceof DataMapper2JsonFunction) {
					DataMapper classData = new DataMapper(valueType, obj, classMapper, objectDTO.level, getPrettyIndentation());
					objectDTO.jsonRawValue = false;
					DataMapper2JsonFunction f = (DataMapper2JsonFunction)function;
					
					return f.apply(classData);
					
				} else if (function instanceof FieldData2JsonFunction) {
					FieldData2JsonFunction f = (FieldData2JsonFunction)function;
					FieldData fieldData = objectDTO.clone();
					
					returnValue = f.apply(fieldData);
					
				} else {
					returnValue = function.apply(obj);
				}

				if (returnValue != null) {
					Class returnType = returnValue.getClass();
					
					if (returnType == String.class) {
						return StringUtil.doublequote(returnValue, isEscapeHtml());
						
					} else if (returnType == valueType || valueType.isAssignableFrom(returnType)) {
						// just continue to do the serializing
					} else {
						objectDTO.valueToProcess = returnValue;
						objectDTO.returnType = returnType;
						
						return object2String(objectDTO);
					}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		


		Set<Class> ignoreFieldsWithAnnotations = classMapper.ignoreFieldsWithAnnotations;

		Map<String, String> keyJsonStrings = new LinkedHashMap<>();
		// to hold relation between name and changed name 
		Map<String, String> fieldNames = new LinkedHashMap<>();
		
		Set<String> processedNameSet = new HashSet<>();
		//StringBuffer sb = new StringBuffer();
		
		Map<String, Method> getters = null;
		Map<String, Method> setters = null;
		Map<String, Method> otherMethods = null;
		
		if (valueType.isInterface()) {
			valueType = (Class<R>) obj.getClass();
		} else if (Modifier.isAbstract( valueType.getModifiers() )) {
			// valueType
			
		} else {
//			Class objClass = obj.getClass();
//			if (valueType.isAssignableFrom(objClass)) {
//				valueType = objClass;
//			}
		}
		
//		if (valueType.isInterface()) {
//			getters = getGetters(obj);
//			setters = getSetters(obj);
//			otherMethods = getOtherMethods(obj);
//		} else {
			getters = getGetters(valueType);
			setters = getSetters(valueType);
			otherMethods = getOtherMethods(valueType);
//		}
		
		Set<Method> jsonAnyGetterMethods = new HashSet<>();
		
		
		if (classMapper.isToStringAsSerializer()) {
			try {
				Method getter = valueType.getDeclaredMethod("toString", null);
				
				if (getter != null) {
					E getterValue = ObjectUtil.getMethodValue(obj, getter);

					if (getterValue != null) {
						Class returnType = getterValue.getClass();
						
						if (returnType == String.class) {
							return StringUtil.doublequote(getterValue, isEscapeHtml());
							
						} else if (returnType == valueType || valueType.isAssignableFrom(returnType)) {
							// just continue to do the serializing
						} else {
							objectDTO.valueToProcess = getterValue;
							objectDTO.returnType = returnType;
							
							return object2String(objectDTO);
						}
					}
				}
				
			} catch (NoSuchMethodException | SecurityException e) {
				// e.printStackTrace();
			}
		}
		
		
		//if (getters != null && getters.size() > 0) {
			boolean isJsonRawValue = false;
			String jsonValueFieldName = DeSerializerUtil.getJsonValueFieldName(valueType.getName());
//			if (jsonValueFieldName == null) {
//				// get all fieldmappers for this class?
//				Set<FieldMapper> fieldMappers = getFieldMappers(valueType);
//				// looking for the method
//				for (FieldMapper fieldMapper: fieldMappers) {
//					if (fieldMapper.jsonValue != null && fieldMapper.jsonValue) {
//						jsonValueFieldName = fieldMapper.java;
//						isJsonRawValue = fieldMapper.isJsonRawValue();
//						break;
//					}
//				}
//			}
			if (jsonValueFieldName == null) {
				jsonValueFieldName = classMapper.getJsonValueFieldName();
			}
		
			if (jsonValueFieldName != null) {
				String lcjava = jsonValueFieldName.toLowerCase();
				Method getter = null;
				if (getters != null && getters.containsKey(lcjava)) {
					getter = getters.get(lcjava);
					
				} else {
					try {
						getter = valueType.getMethod(jsonValueFieldName);
					} catch (NoSuchMethodException | SecurityException e) {
						// e.printStackTrace();
					}
					
					if (getter == null) {
						try {
							getter = valueType.getMethod("get" + StringUtil.capitalize(jsonValueFieldName));
						} catch (NoSuchMethodException | SecurityException e) {
							//e.printStackTrace();
						}
					}
				}
				
				
				if (getter != null) {
					E getterValue = ObjectUtil.getMethodValue(obj, getter);
					
					if (getterValue != null) {
						Class returnType = getterValue.getClass();
						
						if (returnType == String.class) {
							if (isJsonRawValue) {
								return getterValue.toString();
								
							} else if (StringUtil.parenthesized(getterValue.toString())) {
								return getterValue.toString();
								
							} else {
								return StringUtil.doublequote(getterValue, isEscapeHtml());
							}
							
						} else if (returnType == valueType || valueType.isAssignableFrom(returnType)) {
							// just continue to do the serializing
						} else {
							objectDTO.valueToProcess = getterValue;
							objectDTO.returnType = returnType;
							objectDTO.jsonRawValue = isJsonRawValue;
							
							return object2String(objectDTO);
						}
					}
				}
			}
		//}
		
				
		try {
			Field[] fields = null;
//			if (valueType.isInterface()) {
//				fields = getFields(obj);
//			} else {
				fields = getFields(valueType);
//			}

			for (Field f : fields) {
				f.setAccessible(true);

				String name = f.getName();
				String fieldName = name;
				String lcfieldName = fieldName.toLowerCase();

				if (Modifier.isFinal(f.getModifiers()) && Modifier.isStatic(f.getModifiers())) {
					getters.remove(lcfieldName);
					continue;
				}
				
				// 6. Create a blank field mapper instance
				FieldMapper fieldMapper = new FieldMapper(name, name, valueType);
				

				Class<?> returnType = f.getType(); // value.getClass();
				
				// 7. get the class mapper of returnType
				ClassMapper fieldClassMapper = getClassMapper(returnType);
				
				
				// 8. Classify this field mapper with returnType
				fieldMapper = classifyFieldMapper(fieldMapper, fieldClassMapper);
				
				
				// 9. Classify this field mapper
				fieldMapper = classifyFieldMapper(fieldMapper, classMapper);

				FieldMapper javaFieldMapper = getFieldMapper(name, null, valueType);
				
				// getter and setter methods
				Method getter = getters.get(lcfieldName);
				Method setter = setters.get(lcfieldName);
				
				if (getter != null) {
					getter.setAccessible(true);
				}
				
				// control by visibility is not always a good idea
				// here consider the visibility of field and related getter method together
				if (ignoreModifiers(f.getModifiers(), classMapper.includeFieldsWithModifiers)) {
					if (getter != null) {
						if (ignoreModifiers(getter.getModifiers(), classMapper.includeFieldsWithModifiers)) {
							getters.remove(lcfieldName);
							continue;
						}
					} else {
						continue;
					}
				}

				boolean ignored = false;
				Set<String> names = new HashSet<>();
				
				if (annotationSupport) {
					annotations = f.getDeclaredAnnotations();//.getAnnotations();
					
					// field and getter should be treated the same way, if allowed in the class level
					// might not be 100% correct, as the useAttribute as not be applied from annotations yet
					//  && ((javaFieldMapper == null || javaFieldMapper.useAttribute == null) && (fieldMapper.useAttribute == null || fieldMapper.useAttribute))
					// || (javaFieldMapper != null && javaFieldMapper.useAttribute != null && javaFieldMapper.useAttribute) 
					// annotations might apply to method only, not the field, so need to get them, regardless using attribute or not
					if (getter != null) {
						annotations = Stream
								.concat(Arrays.stream(annotations),
										Arrays.stream(getter.getDeclaredAnnotations()))
								.toArray(Annotation[]::new);
						
						// no annotations, then try set method
						if ((annotations == null || annotations.length == 0) && setter != null) {
							annotations = setter.getDeclaredAnnotations();
						}
					}

					ca.oson.json.annotation.FieldMapper fieldMapperAnnotation = null;
					
					for (Annotation annotation : annotations) {
						if (ignoreField(annotation, ignoreFieldsWithAnnotations)) {
							ignored = true;
							break;
							
						} else if (annotation instanceof ca.oson.json.annotation.FieldMapper) {
							fieldMapperAnnotation = (ca.oson.json.annotation.FieldMapper) annotation;
							if (!(fieldMapperAnnotation.serialize() == BOOLEAN.BOTH || fieldMapperAnnotation.serialize() == BOOLEAN.TRUE)) {
								fieldMapperAnnotation = null;
							}
							
						} else if (annotation instanceof ca.oson.json.annotation.FieldMappers) {
							ca.oson.json.annotation.FieldMappers fieldMapperAnnotations = (ca.oson.json.annotation.FieldMappers) annotation;
							for (ca.oson.json.annotation.FieldMapper ann: fieldMapperAnnotations.value()) {
								if (ann.serialize() == BOOLEAN.BOTH || ann.serialize() == BOOLEAN.TRUE) {
									fieldMapperAnnotation = ann;
									//break;
								}
							}
							
						} else {
							String fname = ObjectUtil.getName(annotation);
							if (!StringUtil.isEmpty(fname)) {
								names.add(fname);
							}
						}
					}
					
					// 10. Apply annotations from Oson
					// special name to handle
					if (fieldMapperAnnotation != null) {
						fieldMapper = overwriteBy (fieldMapper, fieldMapperAnnotation, classMapper);
						exposed = null;
					}
				}

				if (ignored) {
					if (getter != null) {
						getters.remove(lcfieldName);
					}
					continue;
				}
				
				// 11. Apply Java configuration for this particular field
				if (javaFieldMapper != null && javaFieldMapper.isSerializing()) {
					fieldMapper = overwriteBy (fieldMapper, javaFieldMapper);
				}
				
				if (fieldMapper.ignore != null && fieldMapper.ignore) {
					if (getter != null) {
						getters.remove(lcfieldName);
					}
					continue;
				}

				// in the ignored list
				if (ObjectUtil.inSet(name, classMapper.jsonIgnoreProperties)) {
					getters.remove(lcfieldName);
					continue;
				}
				
				if (fieldMapper.jsonAnyGetter != null && fieldMapper.jsonAnyGetter && getter != null) {
					getters.remove(lcfieldName);
					jsonAnyGetterMethods.add(getter);
					continue;
				}
				
				if (fieldMapper.useField != null && !fieldMapper.useField) {
					// both should not be used, just like ignore
					if (fieldMapper.useAttribute != null && !fieldMapper.useAttribute) {
						getters.remove(lcfieldName);
					}
					continue;
				}
				
				if (fieldMapper.since != null && fieldMapper.since > getVersion()) {
					if (getter != null) {
						getters.remove(lcfieldName);
					}
					continue;
				} else if (fieldMapper.until != null && fieldMapper.until <= getVersion()) {
					if (getter != null) {
						getters.remove(lcfieldName);
					}
					continue;
				}
				
				
				//jsonIgnoreProperties
				
				// handling name now
				boolean jnameFixed = false;
				String json = fieldMapper.json;
				if (StringUtil.isEmpty(json)) {
					if (getter != null) {
						getters.remove(lcfieldName);
					}
					continue;
					
				} else if (!json.equals(name)) {
					name = json;
					jnameFixed = true;
				}
				
				if (!jnameFixed) {
					for (String jsoname: names) {
						if (!name.equals(jsoname) && !StringUtil.isEmpty(jsoname)) {
							name = jsoname;
							jnameFixed = true;
							break;
						}
					}
				}

				
				// only if the name is still the same as the field name
				// format it based on the naming settings
				// otherwise, it is set on purpose
				if (fieldName.equals(name)) {
					name = StringUtil.formatName(name, format);
					jnameFixed = true;
				}

				fieldMapper.java = fieldName;
				fieldMapper.json = name;
				
				
				// field valuie
				E value = null;
				try {
					value = (E) f.get(obj);// ObjectUtil.unwraponce(f.get(obj));
				} catch (Exception e) {}
				
				if (value != null) {
					Class vtype = value.getClass();
					if (returnType.isAssignableFrom(vtype)) {
						returnType = vtype;
					}
				}
				
				// value from getter
				E getterValue = null;

				if (getter != null) {
					if (fieldMapper.useAttribute == null || fieldMapper.useAttribute) {
						getterValue = ObjectUtil.getMethodValue(obj, getter);
						//getterValue = ObjectUtil.unwraponce(getterValue);
					}
					
					getters.remove(lcfieldName);
				}
				
				// determine which value to use
				if (getterValue != null) {
					if (getterValue.equals(value) || StringUtil.isEmpty(value)) {
						value = getterValue;
						
					} else if (DefaultValue.isDefault(value, returnType) && !DefaultValue.isDefault(getterValue, returnType)) {
						value = getterValue;
					}
//					else if (getterValue.toString().length() > value.toString().length()) {
//						value = getterValue;
//					}
				}


				String str;

				FieldData fieldData = new FieldData(obj, f, value, returnType, false, fieldMapper, objectDTO.level, objectDTO.set);

				str = object2Json(fieldData);
				
				
				if (fieldMapper.jsonValue != null && fieldMapper.jsonValue) {
					if (fieldMapper.isJsonRawValue()) {
						return StringUtil.unquote(str, isEscapeHtml());
					} else {
						return StringUtil.doublequote(str, isEscapeHtml());
					}
				}
				

				if (StringUtil.isNull(str)) {
					if (classMapper.defaultType == JSON_INCLUDE.NON_NULL 
							|| classMapper.defaultType == JSON_INCLUDE.NON_EMPTY
							 || classMapper.defaultType == JSON_INCLUDE.NON_DEFAULT
							 || fieldMapper.defaultType == JSON_INCLUDE.NON_NULL 
								|| fieldMapper.defaultType == JSON_INCLUDE.NON_EMPTY
								 || fieldMapper.defaultType == JSON_INCLUDE.NON_DEFAULT) {
						continue;
						
					} else {
						str = "null";
					}
					
				} else if (StringUtil.isEmpty(str)) {
					if (classMapper.defaultType == JSON_INCLUDE.NON_EMPTY
							 || classMapper.defaultType == JSON_INCLUDE.NON_DEFAULT
							 || fieldMapper.defaultType == JSON_INCLUDE.NON_EMPTY
							 || fieldMapper.defaultType == JSON_INCLUDE.NON_DEFAULT) {
						continue;
					}
					
					str = "\"\"";
					
				} else if ((classMapper.defaultType == JSON_INCLUDE.NON_DEFAULT || fieldMapper.defaultType == JSON_INCLUDE.NON_DEFAULT) && DefaultValue.isDefault(str, returnType)) {
					continue;
				}

				StringBuffer sb = new StringBuffer();
				sb.append(repeatedItem);
				if (fieldMapper.jsonNoName == null || !fieldMapper.jsonNoName) {
					sb.append("\"" + name + "\":" + pretty);
				}
				sb.append(str);
				sb.append(",");
				
				keyJsonStrings.put(lcfieldName, sb.toString());
				processedNameSet.add(name);
				fieldNames.put(lcfieldName, name.toLowerCase());
			}
			

			// now process get methods
			for (Entry<String, Method> entry: getters.entrySet()) {
				String lcfieldName = entry.getKey();
				Method getter = entry.getValue();

				if (ignoreModifiers(getter.getModifiers(), classMapper.includeFieldsWithModifiers)) {
					continue;
				}
				if (Modifier.isFinal(getter.getModifiers()) && Modifier.isStatic(getter.getModifiers())) {
					continue;
				}
				
				String name = getter.getName();
				if (name.substring(3).equalsIgnoreCase(lcfieldName)) {
					name = StringUtil.uncapitalize(name.substring(3));
				}
				
				// just use field name, even it might not be a field
				String fieldName = name;
				
				if (processedNameSet.contains(name) || fieldNames.containsKey(lcfieldName)) {
					continue;
				}
				
				getter.setAccessible(true);
				
				Method setter = setters.get(lcfieldName);
				
				// 6. Create a blank field mapper instance
				FieldMapper fieldMapper = new FieldMapper(name, name, valueType);
				

				Class<?> returnType = getter.getReturnType();
				
				// 7. get the class mapper of returnType
				ClassMapper fieldClassMapper = getClassMapper(returnType);
				
				
				// 8. Classify this field mapper with returnType
				fieldMapper = classifyFieldMapper(fieldMapper, fieldClassMapper);
				
				
				// 9. Classify this field mapper
				fieldMapper = classifyFieldMapper(fieldMapper, classMapper);

				FieldMapper javaFieldMapper = getFieldMapper(name, null, valueType);
				
				boolean ignored = false;
				Set<String> names = new HashSet<>();
				
				if (annotationSupport) {
					annotations = getter.getDeclaredAnnotations();//.getAnnotations();
					
					// no annotations, then try set method
					if ((annotations == null || annotations.length == 0) && setter != null) {
						annotations = setter.getDeclaredAnnotations();
					}

					ca.oson.json.annotation.FieldMapper fieldMapperAnnotation = null;
					
					for (Annotation annotation : annotations) {
						if (ignoreField(annotation, ignoreFieldsWithAnnotations)) {
							ignored = true;
							break;
							
						} else if (annotation instanceof ca.oson.json.annotation.FieldMapper) {
							fieldMapperAnnotation = (ca.oson.json.annotation.FieldMapper) annotation;
							if (!(fieldMapperAnnotation.serialize() == BOOLEAN.BOTH || fieldMapperAnnotation.serialize() == BOOLEAN.TRUE)) {
								fieldMapperAnnotation = null;
							}
							
						} else if (annotation instanceof ca.oson.json.annotation.FieldMappers) {
							ca.oson.json.annotation.FieldMappers fieldMapperAnnotations = (ca.oson.json.annotation.FieldMappers) annotation;
							for (ca.oson.json.annotation.FieldMapper ann: fieldMapperAnnotations.value()) {
								if (ann.serialize() == BOOLEAN.BOTH || ann.serialize() == BOOLEAN.TRUE) {
									fieldMapperAnnotation = ann;
									//break;
								}
							}
							
						} else {
							String fname = ObjectUtil.getName(annotation);
							if (fname != null) {
								names.add(fname);
							}
						}
					}
					
					// 10. Apply annotations from Oson
					// special name to handle
					if (fieldMapperAnnotation != null) {
						fieldMapper = overwriteBy (fieldMapper, fieldMapperAnnotation, classMapper);
						exposed = null;
					}
				}

				if (ignored) {
					continue;
				}
				
				// 11. Apply Java configuration for this particular field
				if (javaFieldMapper != null && javaFieldMapper.isSerializing()) {
					fieldMapper = overwriteBy (fieldMapper, javaFieldMapper);
				}
				
				if (fieldMapper.ignore != null && fieldMapper.ignore) {
					continue;
				}
				
				// in the ignored list
				if (ObjectUtil.inSet(name, classMapper.jsonIgnoreProperties)) {
					continue;
				}

				if (fieldMapper.jsonAnyGetter != null && fieldMapper.jsonAnyGetter) {
					jsonAnyGetterMethods.add(getter);
					continue;
				}
				
				if (fieldMapper.useAttribute != null && !fieldMapper.useAttribute) {
					continue;
				}
				

				if (fieldMapper.since != null && fieldMapper.since > getVersion()) {
					if (getter != null) {
						getters.remove(lcfieldName);
					}
					continue;
				} else if (fieldMapper.until != null && fieldMapper.until <= getVersion()) {
					if (getter != null) {
						getters.remove(lcfieldName);
					}
					continue;
				}

				// handling name now
				boolean jnameFixed = false;
				String json = fieldMapper.json;
				if (StringUtil.isEmpty(json)) {
					if (getter != null) {
						getters.remove(lcfieldName);
					}
					continue;
					
				} else if (!json.equals(name)) {
					name = json;
					jnameFixed = true;
				}
				
				if (!jnameFixed) {
					for (String jsoname: names) {
						if (!name.equals(jsoname) && !StringUtil.isEmpty(jsoname)) {
							name = jsoname;
							jnameFixed = true;
							break;
						}
					}
				}

				
				// only if the name is still the same as the field name
				// format it based on the naming settings
				// otherwise, it is set on purpose
				if (fieldName.equals(name)) {
					name = StringUtil.formatName(name, format);
					jnameFixed = true;
				}

				fieldMapper.java = fieldName;
				fieldMapper.json = name;
				
				
				// get value
				E value = ObjectUtil.getMethodValue(obj, getter);

				if (fieldMapper.jsonValue != null && fieldMapper.jsonValue) {
					if (value != null) {
						if (fieldMapper.isJsonRawValue()) {
							return value.toString();
						} else {
							return StringUtil.doublequote(value, isEscapeHtml());
						}
					}
				}

				
				if (returnType == Class.class) {
					if (value != null && returnType != value.getClass()) {
						returnType = value.getClass();
					} else {
						continue;
					}
				}
				
				String str = null;
				
				//if (returnType != valueType) {
					FieldData fieldData = new FieldData(obj, null, value, returnType, false, fieldMapper, objectDTO.level, objectDTO.set);
					objectDTO.getter = getter;
					str = object2Json(fieldData);
				//}

				if (StringUtil.isNull(str)) {
					if (classMapper.defaultType == JSON_INCLUDE.NON_NULL 
							|| classMapper.defaultType == JSON_INCLUDE.NON_EMPTY
							 || classMapper.defaultType == JSON_INCLUDE.NON_DEFAULT
							 || fieldMapper.defaultType == JSON_INCLUDE.NON_NULL 
								|| fieldMapper.defaultType == JSON_INCLUDE.NON_EMPTY
								 || fieldMapper.defaultType == JSON_INCLUDE.NON_DEFAULT) {
						continue;
						
					} else {
						str = "null";
					}
					
				} else if (StringUtil.isEmpty(str)) {
					if (classMapper.defaultType == JSON_INCLUDE.NON_EMPTY
							 || classMapper.defaultType == JSON_INCLUDE.NON_DEFAULT
							 || fieldMapper.defaultType == JSON_INCLUDE.NON_EMPTY
							 || fieldMapper.defaultType == JSON_INCLUDE.NON_DEFAULT) {
						continue;
					}
					
					str = "null";
					
				} else if ((classMapper.defaultType == JSON_INCLUDE.NON_DEFAULT || fieldMapper.defaultType == JSON_INCLUDE.NON_DEFAULT) && DefaultValue.isDefault(str, returnType)) {
					continue;
				}

				StringBuffer sb = new StringBuffer();
				sb.append(repeatedItem);
				if (fieldMapper.jsonNoName == null || !fieldMapper.jsonNoName) {
					sb.append("\"" + name + "\":" + pretty);
				}
				sb.append(str);
				sb.append(",");
				
				keyJsonStrings.put(lcfieldName, sb.toString());
				processedNameSet.add(name);
				fieldNames.put(lcfieldName, name.toLowerCase());
			}

			// handle @JsonAnyGetter
			if (annotationSupport) {
				for (Entry<String, Method> entry: otherMethods.entrySet()) {
					Method method = entry.getValue();
					if (ignoreModifiers(method.getModifiers(), classMapper.includeFieldsWithModifiers)) {
						continue;
					}
					
					for (Annotation annotation: method.getAnnotations()) {
						if (ignoreField(annotation, ignoreFieldsWithAnnotations)) {
							continue;
						}
						
						if (annotation instanceof ca.oson.json.annotation.FieldMapper ) {

							if (annotation instanceof ca.oson.json.annotation.FieldMapper) {
								ca.oson.json.annotation.FieldMapper fieldMapper = (ca.oson.json.annotation.FieldMapper)annotation;
								
								if (fieldMapper.jsonAnyGetter() == BOOLEAN.FALSE) {
									continue;
								}
							}

							jsonAnyGetterMethods.add(method);
						}
					}
				}
			}


			for (Method method: jsonAnyGetterMethods) {
				if (method != null) {
					Object allValues = ObjectUtil.getMethodValue(obj, method);

					if (allValues != null && allValues instanceof Map) {
						Map<String, Object> map = (Map)allValues;
						String str;
						for (String name: map.keySet()) {
							Object value = map.get(name);
							
							// java to json, check if this name is allowed or changed
							name = java2Json(name);
							
							if (!StringUtil.isEmpty(name)) {
							
								FieldData newFieldData = new FieldData(value, value.getClass(), false, objectDTO.level, objectDTO.set);
								newFieldData.defaultType = classMapper.defaultType;
								str = object2Json(newFieldData);
		
								if (StringUtil.isNull(str)) {
									if (classMapper.defaultType == JSON_INCLUDE.NON_NULL 
											|| classMapper.defaultType == JSON_INCLUDE.NON_EMPTY
											 || classMapper.defaultType == JSON_INCLUDE.NON_DEFAULT) {
										continue;
										
									} else {
										str = "null";
									}
									
								} else if (StringUtil.isEmpty(str)) {
									if (classMapper.defaultType == JSON_INCLUDE.NON_EMPTY
											 || classMapper.defaultType == JSON_INCLUDE.NON_DEFAULT) {
										continue;
									}
									
									str = "null";
									
								} else if (DefaultValue.isDefault(str, value.getClass())) {
									if (classMapper.defaultType == JSON_INCLUDE.NON_DEFAULT) {
										continue;
									}
								}
		
								StringBuffer sb = new StringBuffer();
								sb.append(repeatedItem);
								sb.append("\"" + name + "\":" + pretty);
								sb.append(str);
								sb.append(",");
								keyJsonStrings.put(name, sb.toString());
							}
						}
					}
				}
			}
			
			
			int size = keyJsonStrings.size();
			if (size == 0) {
				return "{}"; // ""

			} else {
				String includeClassType = "";
				if (classMapper.includeClassTypeInJson) { //getIncludeClassTypeInJson()
					includeClassType = repeatedItem + "\"@class\":" + pretty + "\"" + valueType.getName() + "\",";
				}
				
				
				if (exposed != null && exposed.size() > 0) {
					Map<String, String> map = new LinkedHashMap<>();
					
					for (String key: keyJsonStrings.keySet()) {
						if (exposed.contains(key)) {
							map.put(key, keyJsonStrings.get(key));
						}
					}
					
					keyJsonStrings = map;
				}
				
				
				if (keyJsonStrings.size() == 1 && this.isValueOnly()) {
					for (Map.Entry<String, String> entry: keyJsonStrings.entrySet()) {
						if (entry.getKey().toLowerCase().equals("value")) {
							String value = entry.getValue();
							String[] values = value.split(":");
							value = null;
							if (values.length == 1) {
								value = values[0];
							} else if (values.length == 2) {
								value = values[1];
							}
							if (value != null && value.length() > 1) {
								return value.substring(0, value.length() - 1);
							}
						}
					}
				}
				

				// based on sorting requirements
				StringBuffer sb = new StringBuffer();
				if (classMapper.propertyOrders != null) {
					for (String property: classMapper.propertyOrders) {
						property = property.toLowerCase();
						String jsonText = keyJsonStrings.get(property);
						if (jsonText != null) {
							sb.append(jsonText);
							keyJsonStrings.remove(property);
						} else {
							property = fieldNames.get(property);
							if (property != null && keyJsonStrings.containsKey(property)) {
								sb.append(keyJsonStrings.get(property));
								keyJsonStrings.remove(property);
							}
						}
					}
				}
				
				List<String> properties = new ArrayList(keyJsonStrings.keySet());
				if (classMapper.orderByKeyAndProperties) {
					Collections.sort(properties);
				}
				
				for (String property: properties) {
					sb.append(keyJsonStrings.get(property));
				}
				
				String text = sb.toString();
				size = text.length();
				if (size == 0) {
					return "{}";
				} else {
					return "{" + includeClassType + text.substring(0, size - 1) + repeated + "}";
				}
			}

		} catch (IllegalArgumentException | SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
			// } catch (InvocationTargetException e) {
		}
	}


	////////////////////////////////////////////////////////////////////////////////
	// START OF package methods
	////////////////////////////////////////////////////////////////////////////////


	public static Object fromJsonMap(Object obj) {
		FIELD_NAMING naming = FIELD_NAMING.FIELD;
		return fromJsonMap(obj, naming);
	}

	/*
	 * Rely on JSONObject to parse original json text
	 * more confortable to work with map and list
	 * instead of JSONObject and JSONArray
	 */
	public static Object fromJsonMap(Object obj, FIELD_NAMING naming) {
		if (obj instanceof JSONArray) {
			JSONArray jobj = (JSONArray) obj; // .getJSONArray(key);

			List<Object> list = new ArrayList();
			int size = jobj.length();
			try {
				for (int i = 0; i < size; i++) {
					list.add(fromJsonMap(jobj.get(i)));
				}
			} catch (JSONException ex) {
			}

			return list;

		} else if (obj instanceof JSONObject) {
			JSONObject jobj = (JSONObject) obj;

			// since JSONObject gives unordered map, nothing can be done about it?
			// LinkedHashMap
			Map<String, Object> map = new LinkedHashMap<String, Object>();

			Iterator<?> keys = jobj.keys();

			try {
				while (keys.hasNext()) {
					String key = (String) keys.next();
					String keyName = key;
					if (naming != FIELD_NAMING.FIELD) {
						keyName = StringUtil.formatName(key, naming);
					}
					
					if (!jobj.isNull(key)) {
						
						map.put(keyName, fromJsonMap(jobj.get(key)));
					} else {
						map.put(keyName, null);
					}
				}
			} catch (JSONException ex) {
			}

			return map;

		} else {
			return obj;
		}
	}
	

	<T> T fromJsonMap(String source, Type type) {
		try {
			Class<T> valueType = null;
			ComponentType componentType = null;
			boolean started = false;
			if (type != null) {
				if (ComponentType.class.isAssignableFrom(type.getClass())) {
					componentType = (ComponentType)type;
				} else {
					componentType = new ComponentType(type);
				}
				valueType = componentType.getClassType();
				
				startCachedComponentTypes(componentType);
				
				started = true;
				
			} else {
				
			}

			return fromJsonMap(source, valueType, started);

		} catch (JSONException ex) {
			//ex.printStackTrace();
		}

		return null;
	}


	<T> T fromJsonMap(String source, Class<T> valueType, boolean started) {
		return fromJsonMap(source, valueType, null, started);
	}
	
	
	private String removeComments(String source) {
		return StringUtil.applyPatterns(source, getPatterns());
	}
	
	// used to test data types only
	public static Object getListMapObject (String source) {
		return getListMapObject (source, FIELD_NAMING.FIELD);
	}
	public static Object getListMapObject (String source, FIELD_NAMING naming) {
		Object obj;
		if (source.startsWith("[")) {
			obj = new JSONArray(source);
		} else {
			obj = ObjectUtil.getJSONObject(source);
		}
		
		return fromJsonMap(obj, naming);
	}

	<T> T fromJsonMap(String source, Class<T> valueType, T object, boolean started) {
		if (source == null) {
			return null;
		}

		try {
			
			if (valueType == null && object != null) {
				valueType = (Class<T>) object.getClass();
			}
			
			source = removeComments(source);
			
			source = source.trim();//
			if (source.startsWith("[") && (ObjectUtil.isArrayOrCollection(valueType) || (this.isMap2ListStyle() && Map.class.isAssignableFrom(valueType)))) {
				List list = null;
//				try { // JSONArray performs better than ObjectMapper
//					list = new ObjectMapper().readValue(source, List.class);
//				} catch (IOException e) {
					JSONArray obj = new JSONArray(source);
					list = (List)fromJsonMap(obj);
//				}

				if (!started) {
					startCachedComponentTypes(valueType);
				}

				return json2Object(new FieldData(list, valueType, object, true));

			} else if (source.startsWith("{") && ObjectUtil.isMapOrObject(valueType)) {

				Map<String, Object> map = null;
//				try {
					JSONObject obj = ObjectUtil.getJSONObject(source);
					map = (Map)fromJsonMap(obj);
					
//				} catch (Exception e1) {
//					try {
//						map = getJackson().readValue(source, Map.class);
//					} catch(Exception ex) {
//						map = new HashMap<>();
//					}
//				}
				
				if (valueType == null) {
					String className = (String) map.get(getJsonClassType());
					if (className != null && className.length() > 0) {
						try {
							valueType = (Class<T>) Class.forName(className);
						} catch (ClassNotFoundException e) {
							// e.printStackTrace();
						}
					}
				}
				
				if (!started) {
					startCachedComponentTypes(valueType);
				}

				if (valueType == null || Map.class.isAssignableFrom(valueType)) {
					return json2Object(new FieldData(map, valueType, object, true));

				} else {
					if (object == null) {
						return deserialize2Object(map, valueType, null);
					} else {
						return deserialize2Object(new FieldData(map, valueType, object, true));
					}
				}

			} else {
				if (!started) {
					startCachedComponentTypes(valueType);
				}
				
				return json2Object(new FieldData(source, valueType, true));
			}

		} catch (JSONException ex) {
			//ex.printStackTrace();
			//throw ex;
		}

		return null;
	}

	private Class getClassType (String className) {
		if (className == null) {
			return null;
		}

		if (className.startsWith(".")) {
			String path = new OsonSecurityManager().getInitialCallerClassName();
			if (path == null) {
				return null;
			}

			int idx = path.lastIndexOf('.');
			if (idx == -1) {
				return null;
			}

			className = path.substring(0, idx) + className;
		}

		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			// e.printStackTrace();
		}

    	return null;
    }

	<T> T deserialize2Object(Object value, Class<T> valueType, FieldData fieldData) {
		ClassMapper classMapper = getGlobalizedClassMapper(valueType);
		
		Map<String, Object> map = null;
		
		if (value != null && Map.class.isAssignableFrom(value.getClass())) {
			map = (Map)value;
		}

		//boolean checkResult = false;
		if (map == null) {
			if (classMapper == null) {
				return null;
			}
			
			map = new HashMap();
			if (value != null) {
				if (value.toString().endsWith(":")) {
					return null;
				}
				
				map.put(valueType.getName(), value);
			}
			//checkResult = true;
		}

		T obj = null;
		
		 if (valueType == Class.class) {
				obj = (T) ComponentType.forName(value.toString());
		 } else {
			 obj = newInstance(map, valueType);
		 }
		
		if (obj != null) {
			if (fieldData == null) {
				fieldData = new FieldData(value, valueType, obj, true);
			} else {
				fieldData.valueToProcess = value;
				fieldData.returnType = valueType;
				fieldData.returnObj = obj;
			}
			
			return deserialize2Object(fieldData);

//			T t = deserialize2Object(fieldData);
//			if (checkResult) {
//				String source = this.useAttribute(false).setDefaultType(JSON_INCLUDE.NON_DEFAULT).serialize(t);
////				source = source.replaceAll("[^a-zA-Z0-9]", "");
////				value = (value+"").replaceAll("[^a-zA-Z0-9]", "");
////				if (!source.contains(value+"")) {
//				if (DefaultValue.isDefault(source)) {
//					return null;
//				}
//			}
//			return t;
			
		} else {
			return null;
		}
	}
	
	
	private <T> Object getParameterValue(Map<String, Object> map, Class<T> valueType, String parameterName, Class parameterType) {
		Object parameterValue = getMapValue(map, parameterName);

		Class fieldType = null;
		if (Map.class.isAssignableFrom(parameterType)) {
			// just give the whole map data to it
			// it can use as much of it as it likes
			parameterValue = map;
			
		} else {

			if (parameterType.isPrimitive()
					|| Number.class.isAssignableFrom(parameterType)
					|| parameterType == String.class
					|| parameterType == Character.class
					|| parameterType == Boolean.class
					|| Date.class.isAssignableFrom(parameterType)
					|| parameterType.isEnum() || Enum.class.isAssignableFrom(parameterType)) {
				// do nothing
				
			} else {
				String toGenericString = parameterType.toGenericString();
				fieldType = ObjectUtil.getComponentType(toGenericString);
				
				if (fieldType == null) {
					Field[] fields = getFields(valueType);
					for (Field field: fields) {
						if (field.getName().equalsIgnoreCase(parameterName)) {
							// private java.util.List<ca.oson.json.domain.Address> ca.oson.json.domain.Person.addressList
							toGenericString = field.toGenericString();
	
							// fieldType = field.getType(); // getClass();
							fieldType = ObjectUtil.getComponentType(toGenericString);
							break;
						}
					}
				}
			}
			
			FieldData objectDTO = null;
			if (fieldType != null) {
				// FieldData(Object valueToProcess, Type type, boolean json2Java)
				ComponentType componentType = new ComponentType(parameterType, fieldType);
				cachedComponentTypes(componentType);
				
				objectDTO = new FieldData(parameterValue, parameterType, true);
				objectDTO.componentType = componentType;
			} else {
				objectDTO = new FieldData(parameterValue, parameterType, true);
			}
			objectDTO.required = true;
			parameterValue = json2Object(objectDTO);
		}
		
		
		return parameterValue;
	}
	
	
	private <T> T setSingleMapValue(T obj, Class<T> valueType, Object singleMapValue, Class singleMapValueType) {
		if (obj == null || valueType == null || singleMapValue == null || singleMapValueType == null) {
			return obj;
		}
		
		//check all methods
		// and invoke the right one
		// public static InetAddress getByName(String host)
		//Map<String, Method> setters = getSetters(valueType);
		for (Method method: valueType.getDeclaredMethods()) {
			if (method.getParameterCount() == 1) {
				if (ObjectUtil.isSameDataType(method.getParameterTypes()[0], singleMapValueType)) {
					Object object = ObjectUtil.getMethodValue(obj, method, singleMapValue);

					if (object != null && valueType.isAssignableFrom(object.getClass())) {
						return (T) object;
					}
				}

			}
			
		}
		
		return obj;
	}

	/*
	 * create an initial object of valueType type to copy data into
	 */
	<T> T newInstance(Map<String, Object> map, Class<T> valueType) {
		T obj = null;
		
		if (valueType != null) {
			obj = (T) getDefaultValue(valueType);
			if (obj != null) {
				return obj;
			}
		}
		
		
		if (map == null) {
			return null;
		}

		// @JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = As.PROPERTY, property = "@class")
		//@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = As.PROPERTY, property = "@class")
		String JsonClassType = getJsonClassType();

		String className = null;
		if (map.containsKey(JsonClassType)) {
			className = map.get(JsonClassType).toString();
		}

		Class<T> classType = getClassType (className);

		//  && (valueType == null || valueType.isAssignableFrom(classType) || Map.class.isAssignableFrom(valueType))
		if (classType != null) {
			valueType = classType;
		}

		if (valueType == null) {
			return (T)map; // or null, which is better?
		}
		
		
		Constructor<?>[] constructors = null;
		
		Class implClass = null;
		if (valueType.isInterface() || Modifier.isAbstract(valueType.getModifiers())) {
			implClass = DeSerializerUtil.implementingClass(valueType.getName());
		}

		if (implClass != null) {
			constructors = implClass.getDeclaredConstructors();
		} else {
			constructors = valueType.getDeclaredConstructors();//.getConstructors();
		}
		
		Object singleMapValue = null;
		Class singleMapValueType = null;
		if (map.size() == 1) {
			singleMapValue = map.get(valueType.getName());
			
			if (singleMapValue != null) {
				singleMapValueType = singleMapValue.getClass();
				
				if (singleMapValueType == String.class) {
					singleMapValue = StringUtil.unquote(singleMapValue.toString(), isEscapeHtml());
				}

				try {
					if (valueType == Locale.class) {
						Constructor constructor = null;
						String[] parts = ((String)singleMapValue).split("_");
						if (parts.length == 1) {
							constructor = valueType.getConstructor(String.class);
							constructor.setAccessible(true);
							obj = (T) constructor.newInstance(singleMapValue);
						} else if (parts.length == 2) {
							constructor = valueType.getConstructor(String.class, String.class);
							constructor.setAccessible(true);
							obj = (T) constructor.newInstance(parts);
						} else if (parts.length == 3) {
							constructor = valueType.getConstructor(String.class, String.class, String.class);
							constructor.setAccessible(true);
							obj = (T) constructor.newInstance(parts);
						}
						
						if (obj != null) {
							return obj;
						}
					}
				} catch (Exception e) {}
				
				Map<Class, Constructor> cmaps = new HashMap<>();
				for (Constructor constructor: constructors) {
					//Class[] parameterTypes = constructor.getParameterTypes();

					int parameterCount = constructor.getParameterCount();
					if (parameterCount == 1) {
						
						Class[] types = constructor.getParameterTypes();
						
						cmaps.put(types[0], constructor);
					}
				}

				if (cmaps.size() > 0) {
					Constructor constructor = null;

					if ((cmaps.containsKey(Boolean.class) || cmaps.containsKey(boolean.class))
							&& BooleanUtil.isBoolean(singleMapValue.toString())) {
						constructor = cmaps.get(Boolean.class);
						if (constructor == null) {
							constructor = cmaps.get(boolean.class);
						}
						if (constructor != null) {
							try {
								constructor.setAccessible(true);
								obj = (T) constructor.newInstance(BooleanUtil.string2Boolean(singleMapValue.toString()));
								
								if (obj != null) {
									return obj;
								}
							} catch (Exception e) {}
						}
						
					} else if (StringUtil.isNumeric(singleMapValue.toString())) {
						
						Class[] classes = new Class[] {int.class, Integer.class, long.class, Long.class, double.class, Double.class,
								Byte.class, byte.class, Short.class, short.class, Float.class, float.class, BigDecimal.class,
								BigInteger.class, AtomicInteger.class, AtomicLong.class, Number.class};
						
						for (Class cls: classes) {
							constructor = cmaps.get(cls);

							if (constructor != null) {
								try {
									obj = (T) constructor.newInstance(NumberUtil.getNumber(singleMapValue, cls));
									
									if (obj != null) {
										return obj;
									}
								} catch (Exception e) {}
							}
						}

					} else if (StringUtil.isArrayOrList(singleMapValue.toString()) || singleMapValue.getClass().isArray() || Collection.class.isAssignableFrom(singleMapValue.getClass())) {
						for (Entry<Class, Constructor> entry: cmaps.entrySet()) {
							Class cls = entry.getKey();
							constructor = entry.getValue();
							
							if (cls.isArray() || Collection.class.isAssignableFrom(cls)) {
								Object listObject = null;
								if (singleMapValue instanceof String) {
									JSONArray objArray = new JSONArray(singleMapValue.toString());
									listObject = (List)fromJsonMap(objArray);
								} else {
									listObject = singleMapValue;
								}
								
								FieldData objectDTO = new FieldData(listObject, cls, true);
								listObject = json2Object(objectDTO);
								if (listObject != null) {
									try {
										obj = (T) constructor.newInstance(listObject);
										if (obj != null) {
											return obj;
										}
									} catch (Exception e) {}
								}
							}
							
						}
						
						
					}
					
				
					for (Entry<Class, Constructor> entry: cmaps.entrySet()) {
						Class cls = entry.getKey();
						constructor = entry.getValue();
						try {
							obj = (T) constructor.newInstance(singleMapValue);
							if (obj != null) {
								return obj;
							}
						} catch (Exception e) {}
					}
				
				}

			}
		}
		
		
		if (implClass != null) {
			valueType = implClass;
		}
		

		try {
			obj = valueType.newInstance();

			if (obj != null) {
				return setSingleMapValue(obj, valueType, singleMapValue, singleMapValueType);
			}

		} catch (InstantiationException | IllegalAccessException e) {
			//e.printStackTrace();
		}

			


		///*
		for (Constructor constructor: constructors) {
			//Class[] parameterTypes = constructor.getParameterTypes();

			int parameterCount = constructor.getParameterCount();
			if (parameterCount > 0) {
				constructor.setAccessible(true);

				Annotation[] annotations = constructor.getDeclaredAnnotations(); // getAnnotations();

				for (Annotation annotation : annotations) {
					boolean isJsonCreator = false;
					if (annotation instanceof ca.oson.json.annotation.FieldMapper) {
						ca.oson.json.annotation.FieldMapper fieldMapper = (ca.oson.json.annotation.FieldMapper)annotation;
						
						if (fieldMapper.jsonCreator() == BOOLEAN.TRUE) {
							isJsonCreator = true;
						}
					}
					
					if (isJsonCreator) {
						Parameter[] parameters = constructor.getParameters();
						String[] parameterNames = ObjectUtil.getParameterNames(parameters);

						//parameterCount = parameters.length;
						Object[] parameterValues = new Object[parameterCount];
						int i = 0;
						for (String parameterName: parameterNames) {
							parameterValues[i] = getParameterValue(map, valueType, parameterName, parameters[i].getType());
							i++;
						}

						try {
							obj = (T) constructor.newInstance(parameterValues);

							if (obj != null) {
								return setSingleMapValue(obj, valueType, singleMapValue, singleMapValueType);
							}

						} catch (InstantiationException
								| IllegalAccessException
								| IllegalArgumentException
								| InvocationTargetException e) {
							//e.printStackTrace();
						}
					}
				}

			} else {
				try {
					constructor.setAccessible(true);
					obj = (T) constructor.newInstance();

					if (obj != null) {
						return setSingleMapValue(obj, valueType, singleMapValue, singleMapValueType);
					}

				} catch (InstantiationException
						| IllegalAccessException
						| IllegalArgumentException
						| InvocationTargetException e) {
					//e.printStackTrace();
				}

			}
		}
		//*/


		// try again
		for (Constructor constructor: constructors) {
			int parameterCount = constructor.getParameterCount();
			if (parameterCount > 0) {
				constructor.setAccessible(true);

				try {
					List<String> parameterNames = ObjectUtil.getParameterNames(constructor);

					if (parameterNames != null && parameterNames.size() > 0) {
						Class[] parameterTypes = constructor.getParameterTypes();

						int length = parameterTypes.length;
						if (length == parameterNames.size()) {
							Object[] parameterValues = new Object[length];
							Object parameterValue;
							for (int i = 0; i < length; i++) {
								parameterValues[i] = getParameterValue(map, valueType, parameterNames.get(i), parameterTypes[i]);
							}

							try {
								obj = (T) constructor.newInstance(parameterValues);
								if (obj != null) {
									return setSingleMapValue(obj, valueType, singleMapValue, singleMapValueType);
								}

							} catch (InstantiationException
									| IllegalAccessException
									| IllegalArgumentException
									| InvocationTargetException e) {
								//e.printStackTrace();
							}

						}
					}

				} catch (IOException e1) {
					// e1.printStackTrace();
				}
			}
		}


		// try more
		for (Constructor constructor: constructors) {
			int parameterCount = constructor.getParameterCount();
			if (parameterCount > 0) {
				constructor.setAccessible(true);

				Class[] parameterTypes = constructor.getParameterTypes();
				List<String> parameterNames;
				try {
					parameterNames = ObjectUtil.getParameterNames(constructor);

					if (parameterNames != null) {
						int length = parameterTypes.length;
						
						if (length > parameterNames.size()) {
							length = parameterNames.size();
						}
		
						Object[] parameterValues = new Object[length];
						for (int i = 0; i < length; i++) {
							parameterValues[i] = getParameterValue(map, valueType, parameterNames.get(i), parameterTypes[i]);
						}
	
						obj = (T) constructor.newInstance(parameterValues);
						if (obj != null) {
							return setSingleMapValue(obj, valueType, singleMapValue, singleMapValueType);
						}
					}

				} catch (InstantiationException
						| IllegalAccessException
						| IllegalArgumentException
						| InvocationTargetException
						| IOException e) {
					//e.printStackTrace();
				}
			}
		}

		// try more
		try {
			Method[] methods = valueType.getMethods(); // .getMethod("getInstance", null);

			List<Method> methodList = new ArrayList<>();

			if (methods != null) {
				for (Method method: methods) {
					String methodName = method.getName();

					if (methodName.equals("getInstance") 
							|| methodName.equals("newInstance")
							|| methodName.equals("createInstance")
							|| methodName.equals("factory")) {
						Class returnType = method.getReturnType();

						if (valueType.isAssignableFrom(returnType) && Modifier.isStatic(method.getModifiers())) {
							int parameterCount = method.getParameterCount();
							if (parameterCount == 0) {
								try {
									obj = ObjectUtil.getMethodValue(null, method);
									if (obj != null) {
										return setSingleMapValue(obj, valueType, singleMapValue, singleMapValueType);
									}

								} catch (IllegalArgumentException e) {
									// TODO Auto-generated catch block
									//e.printStackTrace();
								}

							} else {
								methodList.add(method);
							}

						}
					}
				}


				for (Method method: methodList) {
					try {
						int parameterCount = method.getParameterCount();
						Object[] parameterValues = new Object[parameterCount];
						Object parameterValue;
						int i = 0;
						Class[] parameterTypes = method.getParameterTypes();

						if (parameterCount == 1 && valueType != null && singleMapValue != null && singleMapValueType != null) {
							
							if (ObjectUtil.isSameDataType(parameterTypes[0], singleMapValueType)) {
								try {
									obj = ObjectUtil.getMethodValue(null, method, singleMapValue);
									if (obj != null) {
										return obj;
									}

								} catch (IllegalArgumentException ex) {
										//ex.printStackTrace();
								}
								
							}

						} else {
							// try annotation
							Parameter[] parameters = method.getParameters();
							String[] parameterNames = ObjectUtil.getParameterNames(parameters);
							parameterCount = parameters.length;
							parameterValues = new Object[parameterCount];
							i = 0;
							for (String parameterName: parameterNames) {
								parameterValues[i] = getParameterValue(map, valueType, parameterName, parameterTypes[i]);
								i++;
							}
						}

						obj = ObjectUtil.getMethodValue(null, method, parameterValues);
						
						if (obj != null) {
							return setSingleMapValue(obj, valueType, singleMapValue, singleMapValueType);
						}

					} catch (IllegalArgumentException e) {
						//e.printStackTrace();
					}
				}

			}

		} catch (SecurityException e) {
			// e.printStackTrace();
		}

		
		// try all static methods, if the return type is correct, get it as the final object
		Method[] methods = valueType.getDeclaredMethods();
		for (Method method: methods) {
			if (Modifier.isStatic(method.getModifiers())) {
				Class returnType = method.getReturnType();
				
				if (valueType.isAssignableFrom(returnType)) {
					try {
						Object[] parameterValues = null;
						
						int parameterCount = method.getParameterCount();
						if (parameterCount > 0) {
							if (parameterCount == 1 && map.size() == 1 && singleMapValue != null && singleMapValueType != null) {
								if (ObjectUtil.isSameDataType(method.getParameterTypes()[0], singleMapValueType)) {
									obj = ObjectUtil.getMethodValue(null, method, singleMapValueType);
									if (obj != null) {
										return obj;
									}
								}
							}

							parameterValues = new Object[parameterCount];
							Object parameterValue;
							int i = 0;
							Class[] parameterTypes = method.getParameterTypes();

							// try annotation
							Parameter[] parameters = method.getParameters();
							String[] parameterNames = ObjectUtil.getParameterNames(parameters);
							parameterCount = parameters.length;
							parameterValues = new Object[parameterCount];
							i = 0;
							for (String parameterName: parameterNames) {
								parameterValues[i] = getParameterValue(map, valueType, parameterName, parameterTypes[i]);
								i++;
							}
						}

						obj = ObjectUtil.getMethodValue(obj, method, parameterValues);
						if (obj != null) {
							return setSingleMapValue(obj, valueType, singleMapValue, singleMapValueType);
						}

					} catch (IllegalArgumentException e) {
						//e.printStackTrace();
					}
					
				}
			}
			
		}

		return null;
	}

	
	private <T> void setNull(Field f, T obj) {
		if (f == null || obj == null) {
			return;
		}
		try {
			f.set(obj, null);
		} catch (Exception ex) {
		}
	}
	

	/*
	 * string to object, deserialize, set method should be used
	 * 
	 * It involves 10 steps to apply processing rules:
	 * 1. Create a blank class mapper instance and Globalize it;
	 * 2. if it is a field in previous execution, and set to inherit from previous mapping, combine with previous mapping;
	 * 3. Apply annotations from other sources;
	 * 4. Apply annotations from Oson;
	 * 5. Apply Java configuration for this particular class;
	 * 6. Create a blank field mapper instance;
	 * 7. get the global class mapper for this field
	 * 8. Classify this field mapper with returnType class mapper
	 * 9. Classify this field mapper;
	 * 10. Apply annotations from other sources;
	 * 11. Apply annotations from Oson;
	 * 12. Apply Java configuration for this particular field.
	 */
	<T> T deserialize2Object(FieldData objectDTO) {
		Object valueToProcess = objectDTO.valueToProcess;
		Map<String, Object> map = null;
		
		if (valueToProcess != null && Map.class.isAssignableFrom(valueToProcess.getClass())) {
			map = (Map)valueToProcess;
		} else {
			map = new HashMap<>();
		}
		
		Class<T> valueType = objectDTO.returnType;
		T obj = (T) objectDTO.returnObj;

		Set<String> nameKeys = new HashSet(map.keySet());
		
		if (valueType == null) {
			valueType = (Class<T>) obj.getClass();
		}

		// first build up the class-level processing rules
		
		
		ClassMapper classMapper = objectDTO.classMapper;
		//if (classMapper == null) {
			// 1. Create a blank class mapper instance
			classMapper = new ClassMapper(valueType);
		
			// 2. Globalize it
			classMapper = globalize(classMapper);
			objectDTO.classMapper = classMapper;
		//}

		if (objectDTO.fieldMapper != null && isInheritMapping()) {
			classMapper = overwriteBy (classMapper, objectDTO.fieldMapper);
		}

		objectDTO.incrLevel();

		try {
			boolean annotationSupport = getAnnotationSupport();
			Annotation[] annotations = null;

			if (annotationSupport) {
				ca.oson.json.annotation.ClassMapper classMapperAnnotation = null;
				
				// 3. Apply annotations from other sources
				annotations = valueType.getAnnotations();
				for (Annotation annotation : annotations) {
					if (ignoreClass(annotation)) {
						return null;
					}

					switch (annotation.annotationType().getName()) {
					case "ca.oson.json.annotation.ClassMapper":
						classMapperAnnotation = (ca.oson.json.annotation.ClassMapper) annotation;
						if (!(classMapperAnnotation.serialize() == BOOLEAN.BOTH || classMapperAnnotation.serialize() == BOOLEAN.FALSE)) {
							classMapperAnnotation = null;
						}
						break;
						
					case "ca.oson.json.annotation.ClassMappers":
						ca.oson.json.annotation.ClassMappers classMapperAnnotations = (ca.oson.json.annotation.ClassMappers) annotation;
						for (ca.oson.json.annotation.ClassMapper ann: classMapperAnnotations.value()) {
							if (ann.serialize() == BOOLEAN.BOTH || ann.serialize() == BOOLEAN.FALSE) {
								classMapperAnnotation = ann;
								// break;
							}
						}
						break;
					}
				}
				
				// 4. Apply annotations from Oson
				if (classMapperAnnotation != null) {
					classMapper = overwriteBy (classMapper, classMapperAnnotation);
				}
			}
			
			
			// 5. Apply Java configuration for this particular class
			ClassMapper javaClassMapper = getClassMapper(valueType);
			if (javaClassMapper != null) {
				classMapper = overwriteBy (classMapper, javaClassMapper);
			}
			
			// now processing at the class level
			
			if (classMapper.ignore()) {
				return null;
			}
			
			if (classMapper.since != null && classMapper.since > getVersion()) {
				return null;
			} else if (classMapper.until != null && classMapper.until <= getVersion()) {
				return null;
			}
			
			Function function = classMapper.deserializer; // = getDeserializer(valueType);
			if (function == null) {
				function = DeSerializerUtil.getDeserializer(valueType.getName());
			}
			if (function != null) {
				try {
					Object returnedValue = null;
					if (function instanceof Json2DataMapperFunction) {
						DataMapper classData = new DataMapper(valueToProcess, valueType, obj, classMapper, objectDTO.level, getPrettyIndentation());
						Json2DataMapperFunction f = (Json2DataMapperFunction)function;
						
						return (T) f.apply(classData);
						
					} else if (function instanceof Json2FieldDataFunction) {
						Json2FieldDataFunction f = (Json2FieldDataFunction)function;
						FieldData fieldData = objectDTO.clone();
						
						returnedValue = f.apply(fieldData);
						
					} else {
						returnedValue = function.apply(obj);
					}

					if (returnedValue instanceof Optional) {
						Optional opt = (Optional)returnedValue;
						returnedValue = opt.orElse(null);
					}
						
					if (returnedValue == null) {
						return null;
					} else if (valueType.isAssignableFrom(returnedValue.getClass())) {
						return (T) returnedValue;
					} else {
						// not the correct returned object type, do nothing
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			

			Map<String, Method> getters = getGetters(valueType);
			Map<String, Method> setters = getSetters(valueType);
			Map<String, Method> otherMethods = getOtherMethods(valueType);
			Set<String> processedNameSet = new HashSet<>();
			
			Method jsonAnySetterMethod = null;
			
			Field[] fields = getFields(valueType); // getFields(obj);

			FIELD_NAMING format = getFieldNaming();
			
			// @Expose
			boolean exposed = false;

			for (Field f : fields) {
				String name = f.getName();
				String fieldName = name;
				String lcfieldName = name.toLowerCase();
				
				Class<?> returnType = f.getType(); // value.getClass();
				
				if (Modifier.isFinal(f.getModifiers()) && Modifier.isStatic(f.getModifiers())) {
					setters.remove(lcfieldName);
					nameKeys.remove(name);
					continue;
				}
				
				f.setAccessible(true);
				
				// getter and setter methods
				
				Method getter = null;
				Method setter = null;
				if (getters != null) {
					getter = getters.get(lcfieldName);
				}
				if (setters != null) {
					setter = setters.get(lcfieldName);
				}
				
				if (ignoreModifiers(f.getModifiers(), classMapper.includeFieldsWithModifiers)) {
					if (setter != null) {
						if (ignoreModifiers(setter.getModifiers(), classMapper.includeFieldsWithModifiers)) {
							setters.remove(lcfieldName);
							nameKeys.remove(name);
							continue;
						}
						
					} else {
						continue;
					}
				}
				
				// 6. Create a blank field mapper instance
				// using valueType of enclosing obj
				FieldMapper fieldMapper = new FieldMapper(name, name, valueType);
				
				// 7. get the class mapper of returnType
				ClassMapper fieldClassMapper = getClassMapper(returnType);
				
				// 8. Classify this field mapper with returnType
				fieldMapper = classifyFieldMapper(fieldMapper, fieldClassMapper);
				
				// 9. Classify this field mapper with enclosing class type
				fieldMapper = classifyFieldMapper(fieldMapper, classMapper);

				FieldMapper javaFieldMapper = getFieldMapper(name, null, valueType);

				boolean ignored = false;
				
				if (setter != null) {
					setter.setAccessible(true);
				}
				
				Set<String> names = new HashSet<>();
				
				if (annotationSupport) {
					annotations = f.getAnnotations();
					
					if (setter != null && ((javaFieldMapper == null || javaFieldMapper.useAttribute == null) && (fieldMapper.useAttribute == null || fieldMapper.useAttribute))
							|| (javaFieldMapper != null && javaFieldMapper.isDeserializing() && javaFieldMapper.useAttribute != null && javaFieldMapper.useAttribute) ) {
						annotations = Stream
								.concat(Arrays.stream(annotations),
										Arrays.stream(setter.getDeclaredAnnotations()))
								.toArray(Annotation[]::new);
					}
					
					// no annotations, then try get method
					if ((annotations == null || annotations.length == 0) && getter != null) {
						annotations = getter.getDeclaredAnnotations();
					}
					
					ca.oson.json.annotation.FieldMapper fieldMapperAnnotation = null;
					
					boolean exposexists = false;
					for (Annotation annotation : annotations) {
						if (ignoreField(annotation, classMapper.ignoreFieldsWithAnnotations)) {
							ignored = true;
							break;
							
						} else if (annotation instanceof ca.oson.json.annotation.FieldMapper) {
							fieldMapperAnnotation = (ca.oson.json.annotation.FieldMapper) annotation;
							if (!(fieldMapperAnnotation.serialize() == BOOLEAN.BOTH || fieldMapperAnnotation.serialize() == BOOLEAN.FALSE)) {
								fieldMapperAnnotation = null;
							}
							
						} else if (annotation instanceof ca.oson.json.annotation.FieldMappers) {
							ca.oson.json.annotation.FieldMappers fieldMapperAnnotations = (ca.oson.json.annotation.FieldMappers) annotation;
							for (ca.oson.json.annotation.FieldMapper ann: fieldMapperAnnotations.value()) {
								if (ann.serialize() == BOOLEAN.BOTH || ann.serialize() == BOOLEAN.FALSE) {
									fieldMapperAnnotation = ann;
									//break; to enable the last one wins
								}
							}
							
						} else {
							String fname = ObjectUtil.getName(annotation);
							if (!StringUtil.isEmpty(fname)) {
								names.add(fname);
							}
						
						}
					}
					
					if (exposed && !exposexists) {
						fieldMapper.ignore = true;
					}

					// 10. Apply annotations from Oson
					if (fieldMapperAnnotation != null) {
						fieldMapper = overwriteBy (fieldMapper, fieldMapperAnnotation, classMapper);
					}
				}
				
				if (ignored) {
					nameKeys.remove(name);
					nameKeys.remove(fieldMapper.json);
					setters.remove(lcfieldName);
					if (exposed) {
						setNull(f, obj);
					}
					continue;
				}

				// 11. Apply Java configuration for this particular field
				if (javaFieldMapper != null && javaFieldMapper.isDeserializing()) {
					fieldMapper = overwriteBy (fieldMapper, javaFieldMapper);
				}
				
				if (fieldMapper.ignore != null && fieldMapper.ignore) {
					if (setter != null) {
						setters.remove(lcfieldName);
					}
					nameKeys.remove(name);
					nameKeys.remove(fieldMapper.json);
					if (exposed) {
						setNull(f, obj);
					}
					continue;
				}
				
				// in the ignored list
				if (ObjectUtil.inSet(name, classMapper.jsonIgnoreProperties)) {
					setters.remove(lcfieldName);
					nameKeys.remove(name);
					continue;
				}

				if (fieldMapper.jsonAnySetter != null && fieldMapper.jsonAnySetter && setter != null) {
					setters.remove(lcfieldName);
					otherMethods.put(lcfieldName, setter);
					continue;
				}
				
				if (fieldMapper.useField != null && !fieldMapper.useField) {
					// both should not be used, just like ignore
					if (fieldMapper.useAttribute != null && !fieldMapper.useAttribute) {
						getters.remove(lcfieldName);
					}
					continue;
				}
				

				if (fieldMapper.since != null && fieldMapper.since > getVersion()) {
					if (setter != null) {
						setters.remove(lcfieldName);
					}
					continue;
				} else if (fieldMapper.until != null && fieldMapper.until <= getVersion()) {
					if (setter != null) {
						setters.remove(lcfieldName);
					}
					continue;
				}

				// get value for name in map
				Object value = null;
				boolean jnameFixed = false;
				String json = fieldMapper.json;
				int size = nameKeys.size();
				if (json == null) {
					if (setter != null) {
						setters.remove(lcfieldName);
					}
					continue;
					
				} else if (!json.equals(name)) {
					name = json;
					value = getMapValue(map, name, nameKeys);
					jnameFixed = true;
				}
				
				if (!jnameFixed) {
					for (String jsoname: names) {
						if (!name.equals(jsoname) && !StringUtil.isEmpty(jsoname)) {
							name = jsoname;
							value = getMapValue(map, name, nameKeys);
							if (value != null) {
								jnameFixed = true;
								break;
							}
						}
					}
				}

				if (!jnameFixed) {
					value = getMapValue(map, name, nameKeys);
					jnameFixed = true;
				}

				fieldMapper.java = fieldName;
				fieldMapper.json = name;
				
				// either not null, or a null value exists in the value map
				if (value != null || size == nameKeys.size() + 1) {
					Object oldValue = value;
					FieldData fieldData = new FieldData(obj, f, value, returnType, true, fieldMapper, objectDTO.level, objectDTO.set);
					fieldData.setter = setter;
					Class fieldType = guessComponentType(fieldData);
					value = json2Object(fieldData);

					if (StringUtil.isNull(value)) {
						if (classMapper.defaultType == JSON_INCLUDE.NON_NULL 
								|| classMapper.defaultType == JSON_INCLUDE.NON_EMPTY
								 || classMapper.defaultType == JSON_INCLUDE.NON_DEFAULT) {
							continue;
							
						}
						
					} else if (StringUtil.isEmpty(value)) {
						if (classMapper.defaultType == JSON_INCLUDE.NON_EMPTY
								 || classMapper.defaultType == JSON_INCLUDE.NON_DEFAULT) {
							continue;
						}
						
					} else if (DefaultValue.isDefault(value, returnType)) {
						if (classMapper.defaultType == JSON_INCLUDE.NON_DEFAULT) {
							continue;
						}
					}

					try {
						if (value == null && oldValue != null && oldValue.equals(f.get(obj)+"")) {
							// keep original value
							
						} else {
							f.set(obj, value);
						}

					} catch (IllegalAccessException
							| IllegalArgumentException ex) {
						//ex.printStackTrace();
						if (setter != null) {
							ObjectUtil.setMethodValue(obj, setter, value);
						}
					}
				}
				
				setters.remove(lcfieldName);
				nameKeys.remove(name);
			}

			
			for (Entry<String, Method> entry: setters.entrySet()) {
				String lcfieldName = entry.getKey();
				Method setter = entry.getValue();
				
				setter.setAccessible(true);

				String name = setter.getName();
				if (name != null && name.length() > 3 && name.substring(0,3).equals("set") && name.substring(3).equalsIgnoreCase(lcfieldName)) {
					name = StringUtil.uncapitalize(name.substring(3));
				}
				
				// just use field name, even it might not be a field
				String fieldName = name;
				
				if (ignoreModifiers(setter.getModifiers(), classMapper.includeFieldsWithModifiers)) {
					nameKeys.remove(name);
					continue;
				}

				if (Modifier.isFinal(setter.getModifiers()) && Modifier.isStatic(setter.getModifiers())) {
					nameKeys.remove(name);
					continue;
				}
				
				// 6. Create a blank field mapper instance
				FieldMapper fieldMapper = new FieldMapper(name, name, valueType);
				
				Class returnType = null;
				Class[] types = setter.getParameterTypes();
				if (types != null && types.length > 0) {
					returnType = types[0];
				}
				
				// not a proper setter
				if (returnType == null) {
					continue;
				}
				
				// 7. get the class mapper of returnType
				ClassMapper fieldClassMapper = getClassMapper(returnType);
				
				// 8. Classify this field mapper with returnType
				fieldMapper = classifyFieldMapper(fieldMapper, fieldClassMapper);
				
				// 9. Classify this field mapper with enclosing class type
				fieldMapper = classifyFieldMapper(fieldMapper, classMapper);

				FieldMapper javaFieldMapper = getFieldMapper(name, null, valueType);


				boolean ignored = false;
				
				Method getter = getters.get(lcfieldName);
				
				Set<String> names = new HashSet<>();
				
				if (annotationSupport) {
					
					annotations = setter.getDeclaredAnnotations();
					
					// no annotations, then try get method
					if ((annotations == null || annotations.length == 0) && getter != null) {
						annotations = getter.getDeclaredAnnotations();
					}

					ca.oson.json.annotation.FieldMapper fieldMapperAnnotation = null;
					
					for (Annotation annotation : annotations) {
						if (ignoreField(annotation, classMapper.ignoreFieldsWithAnnotations)) {
							ignored = true;
							break;
							
						} else if (annotation instanceof ca.oson.json.annotation.FieldMapper) {
							fieldMapperAnnotation = (ca.oson.json.annotation.FieldMapper) annotation;
							if (!(fieldMapperAnnotation.serialize() == BOOLEAN.BOTH || fieldMapperAnnotation.serialize() == BOOLEAN.FALSE)) {
								fieldMapperAnnotation = null;
							}
							
						} else if (annotation instanceof ca.oson.json.annotation.FieldMappers) {
							ca.oson.json.annotation.FieldMappers fieldMapperAnnotations = (ca.oson.json.annotation.FieldMappers) annotation;
							for (ca.oson.json.annotation.FieldMapper ann: fieldMapperAnnotations.value()) {
								if (ann.serialize() == BOOLEAN.BOTH || ann.serialize() == BOOLEAN.FALSE) {
									fieldMapperAnnotation = ann;
									// break;
								}
							}
							
						} else {
							String fname = ObjectUtil.getName(annotation);
							if (fname != null) {
								names.add(fname);
							}
						}
					}

					// 10. Apply annotations from Oson
					if (fieldMapperAnnotation != null) {
						fieldMapper = overwriteBy (fieldMapper, fieldMapperAnnotation, classMapper);
					}
				}
				
				if (ignored) {
					nameKeys.remove(name);
					nameKeys.remove(fieldMapper.json);
					continue;
				}

				// 11. Apply Java configuration for this particular field
				if (javaFieldMapper != null && javaFieldMapper.isDeserializing()) {
					fieldMapper = overwriteBy (fieldMapper, javaFieldMapper);
				}
				
				if (fieldMapper.ignore != null && fieldMapper.ignore) {
					nameKeys.remove(name);
					nameKeys.remove(fieldMapper.json);
					continue;
				}
				
				// in the ignored list
				if (ObjectUtil.inSet(name, classMapper.jsonIgnoreProperties)) {
					nameKeys.remove(name);
					continue;
				}
				
				if (fieldMapper.useAttribute != null && !fieldMapper.useAttribute) {
					nameKeys.remove(name);
					nameKeys.remove(fieldMapper.json);
					continue;
				}
				
				if (fieldMapper.jsonAnySetter != null && fieldMapper.jsonAnySetter && setter != null) {
					setters.remove(lcfieldName);
					otherMethods.put(lcfieldName, setter);
					continue;
				}


				if (fieldMapper.since != null && fieldMapper.since > getVersion()) {
					nameKeys.remove(name);
					nameKeys.remove(fieldMapper.json);
					continue;
				} else if (fieldMapper.until != null && fieldMapper.until <= getVersion()) {
					nameKeys.remove(name);
					nameKeys.remove(fieldMapper.json);
					continue;
				}
				
				// get value for name in map
				Object value = null;
				boolean jnameFixed = false;
				String json = fieldMapper.json;
				if (json == null) {
					continue;
					
				} else if (!json.equals(name)) {
					name = json;
					value = getMapValue(map, name, nameKeys);
					jnameFixed = true;
				}
				
				if (!jnameFixed) {
					for (String jsoname: names) {
						if (!name.equals(jsoname) && !StringUtil.isEmpty(jsoname)) {
							name = jsoname;
							value = getMapValue(map, name, nameKeys);
							jnameFixed = true;
							break;
						}
					}
				}

				if (!jnameFixed) {
					value = getMapValue(map, name, nameKeys);
					jnameFixed = true;
				}

				fieldMapper.java = fieldName;
				fieldMapper.json = name;

				if (value != null) {
					
					FieldData fieldData = new FieldData(obj, null, value, returnType, true, fieldMapper, objectDTO.level, objectDTO.set);
					fieldData.setter = setter;
					Class fieldType = guessComponentType(fieldData);

					value = json2Object(fieldData);

					if (StringUtil.isNull(value)) {
						if (classMapper.defaultType == JSON_INCLUDE.NON_NULL 
								|| classMapper.defaultType == JSON_INCLUDE.NON_EMPTY
								 || classMapper.defaultType == JSON_INCLUDE.NON_DEFAULT) {
							continue;
							
						}
						
					} else if (StringUtil.isEmpty(value)) {
						if (classMapper.defaultType == JSON_INCLUDE.NON_EMPTY
								 || classMapper.defaultType == JSON_INCLUDE.NON_DEFAULT) {
							continue;
						}
						
					} else if (DefaultValue.isDefault(value, returnType)) {
						if (classMapper.defaultType == JSON_INCLUDE.NON_DEFAULT) {
							continue;
						}
					}

					ObjectUtil.setMethodValue(obj, setter, value);

					nameKeys.remove(name);
				}
			}


			if (annotationSupport) {
				//@JsonAnySetter
				if (nameKeys.size() > 0) {
					for (Entry<String, Method> entry: otherMethods.entrySet()) {
						Method method = entry.getValue();
						
						if (ignoreModifiers(method.getModifiers(), classMapper.includeFieldsWithModifiers)) {
							continue;
						}
						
						if (method.isAnnotationPresent(ca.oson.json.annotation.FieldMapper.class)) {
							ca.oson.json.annotation.FieldMapper annotation = (ca.oson.json.annotation.FieldMapper)method.getAnnotation(ca.oson.json.annotation.FieldMapper.class);
							if (annotation.jsonAnySetter() == BOOLEAN.TRUE) {
								jsonAnySetterMethod = method;
								break;
							}
						}
					}
				}
			}

			if (jsonAnySetterMethod != null) {
				Parameter[] parameters = jsonAnySetterMethod.getParameters();
				if (parameters != null && parameters.length == 2) {
					for (String name: nameKeys) {
						Object value = map.get(name);
						
						// json to java, check if this name is allowed or changed
						String java = json2Java(name);

						if (value != null && !StringUtil.isEmpty(java)) {
							ObjectUtil.setMethodValue(obj, jsonAnySetterMethod, java, value);
						}

					}
				}
			}


			return obj;

			// | InvocationTargetException
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

		return null;
	}


	////////////////////////////////////////////////////////////////////////////////
	// START OF public methods
	////////////////////////////////////////////////////////////////////////////////

	
	/*
	 * Deserialize JSONObject object, to Map object
	 */
	public <T> T deserialize(JSONObject source) {
		Map<String, Object> map = (Map)fromJsonMap(source);
		
		return json2Object(new FieldData(map, null, null, true));
	}
	public <T> T deserialize(JSONObject source, T obj) {
		Map<String, Object> map = (Map)fromJsonMap(source);
		
		return json2Object(new FieldData(map, null, obj, true));
	}
	public <T> T deserialize(JSONObject source, Class<T> valueType) {
		ComponentType componentType = new ComponentType(valueType);
		startCachedComponentTypes(componentType);
		
		Map<String, Object> map = (Map)fromJsonMap(source);
		
		return json2Object(new FieldData(map, valueType, null, true));
	}
	public <T> T deserialize(JSONObject source, Type type) {
		Map<String, Object> map = (Map)fromJsonMap(source);
		
		Class<T> valueType = null;
		ComponentType componentType = null;
		if (type != null) {
			if (ComponentType.class.isAssignableFrom(type.getClass())) {
				componentType = (ComponentType)type;
			} else {
				componentType = new ComponentType(type);
			}
			valueType = componentType.getClassType();
			
			startCachedComponentTypes(componentType);
		}
		
		return deserialize(source, valueType);
	}
	
	
	/*
	 * string to object, deserialize, set method should be used
	 */
	public <T> T deserialize(String source, T obj) {
		if (source == null || source.length() < 2 || obj == null) {
			return null;
		}
		source = source.trim();

		if (source.length() < 2
				|| (source.startsWith("[") && !source.endsWith("]"))
				|| (source.startsWith("{") && !source.endsWith("}"))) {
			return null;
		}

		Class<T> valueType = (Class<T>) obj.getClass();

		try {
			return fromJsonMap(source, valueType, obj, false);

		} catch (IllegalArgumentException e) {
			//e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	public <T> T deserialize(String source, Class<T> valueType) {
		return fromJsonMap(source, valueType);
	}
	
	public <T> T deserialize(String source, Type type) {
		return fromJsonMap(source, type);
	}

	public <T> String serialize(T source, Type type) {
		Class<T> valueType = null;
		ComponentType componentType = null;
		if (type != null) {
			if (ComponentType.class.isAssignableFrom(type.getClass())) {
				componentType = (ComponentType)type;
			} else {
				componentType = new ComponentType(type);
			}
			valueType = componentType.getClassType();
		}
		
		if (valueType == null) {
			valueType = (Class<T>) source.getClass();
		}

		return object2Json(new FieldData(source, valueType, componentType, false));
	}
	

	public <T> String serialize(T source, Class<T> valueType) {
		if (valueType == null) {
			valueType = (Class<T>) source.getClass();
		}

		String value = object2Json(new FieldData(source, valueType, false));
		if (value == null) {
			return "null";
		}
		return value;
	}
	
	public <T> String serialize(T source) {
		if (source == null) {
			return "null";
		}

		Class<T> valueType = (Class<T>) source.getClass();
		
		return serialize(source, valueType);
	}

	public <T> T deserialize(String source) {
		return deserialize(source, null);
	}
	
	public <T> T fromJson(String source) {
		return deserialize(source);
	}
	public <T> T fromJson(String source, T obj) {
		return deserialize(source, obj);
	}
	public <T> T fromJson(String source, Class<T> valueType) {
		return deserialize(source, valueType);
	}
	public <T> T fromJson(String source, Type type) {
		return deserialize(source, type);
	}
	public <T> String toJson(T source, Type type) {
		return serialize(source, type);
	}
	public <T> String toJson(T source, Class<T> valueType) {
		return serialize(source, valueType);
	}
	public <T> String toJson(T source) {
		return serialize(source);
	}
	public <T> T readValue(String source) {
		return deserialize(source);
	}
	public <T> T readValue(String source, T obj) {
		return deserialize(source, obj);
	}
	public <T> T readValue(String source, Class<T> valueType) {
		return deserialize(source, valueType);
	}
	public <T> T readValue(String source, Type type) {
		return deserialize(source, type);
	}
	public <T> String writeValueAsString(T source, Type type) {
		return serialize(source, type);
	}
	public <T> String writeValueAsString(T source) {
		return serialize(source);
	}


	public static class OsonSecurityManager extends SecurityManager
	{
		public Class[] context() {
			return getClassContext();
		}
		public String getInitialCallerClassName() {
			Class[] classes = context();
			if (classes == null || classes.length == 0) {
				return null;
			}
			return classes[classes.length - 1].getName();
		}
	}
	
}
