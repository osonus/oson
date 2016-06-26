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

import java.beans.Expression;
import java.beans.Statement;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;

import ca.oson.json.Oson.ClassMapper;

import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.introspect.AnnotatedParameter;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.google.gson.annotations.Since;

import org.springframework.web.bind.annotation.RequestParam;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.AnnotationMemberValue;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ByteMemberValue;
import javassist.bytecode.annotation.CharMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.DoubleMemberValue;
import javassist.bytecode.annotation.EnumMemberValue;
import javassist.bytecode.annotation.FloatMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.LongMemberValue;
import javassist.bytecode.annotation.ShortMemberValue;
import javassist.bytecode.annotation.StringMemberValue;


/**
 * Convert java object to and from Json String
 * 
 * @author	David Ruifang He
 * @Date	June 15, 2016
 */
public class Oson {
	private static final char SPACE = ' ';
	private static final String mixin = "MixIn";
	private static final int MAX_LEVEL = 10;
	public static enum JSON_PROCESSOR {
		JACKSON, // use Jacksopn's implementation
		GSON, // use google's gson implementation
		OSON // Oson Json processor in Java
	};
	
	// used by annotations
	public static enum BOOLEAN {
		FALSE,
		TRUE,
		NONE; // the same as null
		
		public static Boolean valueOf(BOOLEAN bool) {
			switch (bool) {
				case FALSE: return false;
				case TRUE: return true;
				case NONE: return null;
				default: return false;
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
		All,
		None
	}


	/*
	 * Defines system-level default values for various Java types.
	 * These values are mostly the default values of Java language types.
	 * They can be overwritten for specific processing needs.
	 *
	 * In case the value is required,
	 * the processing steps are:
	 * 1. local value exists, use it;
	 * 2. otherwise, if default value exists in FieldData, use it;
	 * 3. othrewise, if can be constructed from newInstance function, use it;
	 * 4. finally, use this systen default value.
	 */
	public static class DefaultValue {
		public static Collection collection = new ArrayList();
		public static Map map = new HashMap();
		public static Integer integer = new Integer(0);
		public static BigInteger bigInteger = BigInteger.ZERO;
		public static BigDecimal bigDecimal = BigDecimal.ZERO;
		public static Object[] array = new Object[0];
		public static Boolean bool = false;
		public static String string = "";
		public static Character character = '\u0000';
		public static Short dshort = 0;
		public static Byte dbyte = 0;
		public static Long dlong = 0l;
		public static Float dfloat = 0f;
		public static Double ddouble = 0d;
		public static String simpleDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'";
		public static Date date = Calendar.getInstance().getTime(); // new Date();
		public static AtomicInteger atomicInteger = new AtomicInteger();
		public static AtomicLong atomicLong = new AtomicLong();
		
		public static Date getDate() {
			date = Calendar.getInstance().getTime();
			
			return date;
		}
		
		/*
		 * this is the default behavior: use both field and attribute, to try the best
		 */
		public static boolean useField = true;
		
		/*
		 * The default behaviour is: 
		 * serialize, use getter
		 * deserialsize, use setter
		 */
		public static boolean useAttribute = true;
		
		
		
		public static Object getSystemDefault(Class type) {
			if (type == String.class) {
				return DefaultValue.string;
			} else if (Collection.class.isAssignableFrom(type)) {
				return DefaultValue.collection;
			} else if (Map.class.isAssignableFrom(type)) {
				return DefaultValue.map;
			} else if (type.isArray()) {
				return DefaultValue.array;

			} else if (type == Integer.class || type == int.class) {
				return DefaultValue.integer;
				
			} else if (type == BigInteger.class) {
				return DefaultValue.bigInteger;
				
			} else if (type == BigDecimal.class) {
				return DefaultValue.bigDecimal;
				
			} else if (type == Character.class || type == char.class) {
				return DefaultValue.character;
				
			} else if (type == Short.class || type == short.class) {
				return DefaultValue.dshort;
				
			} else if (type == Byte.class || type == byte.class) {
				return DefaultValue.dbyte;
				
			} else if (type == Long.class || type == long.class) {
				return DefaultValue.dlong;
				
			} else if (type == Float.class || type == float.class) {
				return DefaultValue.dfloat;
				
			} else if (type == Float.class || type == float.class) {
				return DefaultValue.dfloat;
				
			} else if (type == Double.class || type == double.class) {
				return DefaultValue.ddouble;
				
			} else if (type == Short.class) {
				return DefaultValue.dshort;
				
			}
			
			return null;
		}
		

		public static boolean isDefault(Object obj) {
			if (StringUtil.isEmpty(obj)) {
				return true;
			}
			
			Class type = obj.getClass();
			
			if (obj instanceof String) {
				String str = obj.toString().trim();
				return (str.equals("[]") || str.equals("{}"));
				
			} else if (Collection.class.isAssignableFrom(type)) {
				Collection list = (Collection)obj;
				if (list.size() == 0) {
					return true;
				}

				for (Object item: list) {
					if (!isDefault(item)) {
						return false;
					}
				}
				
				return true;
				
			} else if (Map.class.isAssignableFrom(type)) {
				Map map = (Map)obj;
				return (map.size() == 0);
			} else if (type.isArray()) {
				Object[] array = (Object[])obj;
				
				if (array.length == 0) {
					return true;
				}
				
				for (Object item: array) {
					if (!isDefault(item)) {
						return false;
					}
				}
				
				return true;

			} else if (obj instanceof Character || type == char.class) {
				return DefaultValue.character.equals(obj);
				
			} else if (Number.class.isAssignableFrom(obj.getClass()) || obj.getClass().isPrimitive()) {

				if (obj instanceof Integer || type == int.class) {
					return DefaultValue.integer.equals(obj);
					
				} else if (obj instanceof BigInteger) {
					return DefaultValue.bigInteger.equals(obj);
					
				} else if (obj instanceof BigDecimal) {
					return DefaultValue.bigDecimal.equals(obj);
					
				} else if (obj instanceof Short || type == short.class) {
					return DefaultValue.dshort.equals(obj);
					
				} else if (obj instanceof Byte || type == byte.class) {
					return DefaultValue.dbyte.equals(obj);
					
				} else if (obj instanceof Long || type == long.class) {
					return DefaultValue.dlong.equals(obj);
					
				} else if (obj instanceof Float || type == float.class) {
					return DefaultValue.dfloat.equals(obj);
					
				} else if (obj instanceof AtomicInteger) {
					return (DefaultValue.atomicInteger.intValue() == ((AtomicInteger)obj).intValue());
					
				} else if (obj instanceof AtomicLong) {
					return (DefaultValue.atomicLong.longValue() == ((AtomicLong)obj).longValue());
					
				} else if (obj instanceof Double || type == double.class) {
					return DefaultValue.ddouble.equals(obj);
					
				} else if (obj instanceof Short) {
					return DefaultValue.dshort.equals(obj);
				} else {
				}
			
			}
			
			return false;
		}
	}
	
	
	/*
	 * configuration options for a specific class type
	 */
	public static class ClassMapper<T> {
		public ClassMapper() {
			super();
		}
		public ClassMapper(Class<T> type) {
			super();
			this.type = type;
		}
		public ClassMapper(String className) {
			super();
			try {
				this.type = (Class<T>) Class.forName(className);
			} catch (ClassNotFoundException e) {
				// e.printStackTrace();
			}
		}
		public ClassMapper setType(Class<T> type) {
			this.type = type;
			return this;
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
		public ClassMapper setSerializer(ClassData2JsonFunction serializer) {
			this.serializer = serializer;
			return this;
		}
		public ClassMapper setDeserializer(Json2ClassDataFunction deserializer) {
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
		public ClassMapper setMin(Integer min) {
			this.min = min;
			return this;
		}
		public ClassMapper setMax(Integer max) {
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
		
		public Class<T> type;

		// class level
		// user provided constructor or actual dummy object
		public InstanceCreator<T> constructor;
		public T defaultValue;
		
		/*
		 * Fields or variables of this class type will be ignored
		 */
		public Boolean ignore = null;
		
		// user defines function to convert specific type
		// it can be user declared classes, or basic Java type, such as how an Integer value
		// can be converted into string, using single function interface, such as a Lamda expression
		public Function serializer;
		public Function deserializer;
		
		// class level specification on how to get values from an object
		public Boolean useField = null;
		public Boolean useAttribute = null;
		
		// fields inside this class, mostly for user declared fields
		public Set<MODIFIER> includeFieldsWithModifiers = null;
		// class specific date formatter
		public String simpleDateFormat = null;
		
		public Boolean orderByKeyAndProperties = null;
		public String[] propertyOrders = null;

		public Boolean includeClassTypeInJson = null;
		public Double ignoreVersionsAfter;
		public Set<Class> ignoreFieldsWithAnnotations = null;
		public Set<String> jsonIgnoreProperties;
		
		public JSON_INCLUDE defaultType = null; // JSON_INCLUDE.NONE;
		
		public Integer scale = null; // Default: 0
		public Integer min = null; // default (int) 0;
		public Integer max = null; // default (int) 2147483647;
		
		public EnumType enumType = null;
		public Boolean date2Long = null;

	}
	
	/*
	 * configuration options for a specific field
	 */
	public static class FieldMapper<T, E> {
		public FieldMapper(String java, String json, Class<T> type, Function serializer, Function deserializer) {
			this(java,json,type);
			this.serializer = serializer;
			this.deserializer = deserializer;
		}
		
		public FieldMapper(String java, String json, Class<T> type) {
			this(java,json);
			this.type = type;
		}

		public FieldMapper(String java, String json) {
			super();
			this.java = java;
			this.json = json;
		}

		public FieldMapper() {
			super();
		}

		public FieldMapper setJava(String java) {
			this.java = java;
			return this;
		}

		public FieldMapper setJson(String json) {
			this.json = json;
			return this;
		}

		public FieldMapper setType(Class<T> type) {
			this.type = type;
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

		public FieldMapper setSimpleDateFormat(String simpleDateFormat) {
			this.simpleDateFormat = simpleDateFormat;
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

		public FieldMapper setMin(Integer min) {
			this.min = min;
			return this;
		}

		public FieldMapper setMax(Integer max) {
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

		public FieldMapper setJsonRawValue(boolean jsonRawValue) {
			this.jsonRawValue = jsonRawValue;
			return this;
		}

		public FieldMapper setJsonValue(boolean jsonValue) {
			jsonValue = jsonValue;
			return this;
		}

		public FieldMapper setDate2Long(Boolean date2Long) {
			this.date2Long = date2Long;
			return this;
		}
		
		/*
		 * the number of digits of decimal
		 */
		public void setPrecision(Integer precision) {
			this.precision = precision;
		}
		
		// how to match field name, and its enclosing class
		// how to ignore its value, in case either java or json value is null
		public String java;// field name
		public String json;
		public Class<T> type; // for this class only, if present; otherwise, all classes for the same field name
		
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

		/*
		 * This field/attribute will be ignored if true
		 */
		public Boolean ignore = null;
		
		// how to get its value during serializing or deserializing
		public Boolean useField = null;
		public Boolean useAttribute = null;
		
		// how to modify it value
		public Function serializer;
		public Function deserializer;

		// specific requirement on its value
		// class specific date formatter, in case it is Date type
		private String simpleDateFormat = null;
		
		// in case a enumType, define its type to serialize
		public EnumType enumType = null;
		public Boolean required = null;// not nullable
		public Integer length = null; // Default: 255
		public Integer scale = null; // Default: 0
		public Integer precision = null;
		public Integer min = null; // default (int) 0;
		public Integer max = null; // default (int) 2147483647;
		public E defaultValue = null; // default ""
		public JSON_INCLUDE defaultType = null;
		// serialize to double quotes, or not
		public Boolean jsonRawValue = null;
		// in a class, only one method returning a String value is allowed to set this value to true
		public Boolean jsonValue = null;
		
		/*
		 * method with this value set to true will get all properties not specified earlier.
		 * It will normally return a Map<String , Object>
		 */
		public Boolean jsonAnyGetter = null;

		/*
		 * method with this value set to true will set all properties not consumed earlier.
		 * It will normally store all the other data into a Map<String , Object>
		 */
		public Boolean jsonAnySetter = null;
		/*
		 * determine a date to be converted to long, instead of using date format to converted into a string
		 */
		public Boolean date2Long = null;
		
		/*
		 * a flag used to jackson
		 */
		private boolean processed = false;
	}

	// make sure options have valid values
	public static class Options {
		// global level configurations
		private String simpleDateFormat = null; // DefaultValue.simpleDateFormat;
		private DateFormat dateFormat = null; // new SimpleDateFormat(simpleDateFormat);
		private JSON_PROCESSOR jsonProcessor = JSON_PROCESSOR.OSON;
		private FIELD_NAMING fieldNaming = FIELD_NAMING.FIELD;
		private JSON_INCLUDE defaultType = JSON_INCLUDE.NONE;
		private Boolean prettyPrinting = false;
		private int indentation = 2;
		private boolean annotationSupport = true;
		private Boolean orderByKeyAndProperties = false;
		private Boolean includeClassTypeInJson = false;
		private Boolean printErrorUseOsonInFailure = true;
		private String jsonClassType = "@class";
		private Double ignoreVersionsAfter = 10000d; // max allowed
		private Set<Class> ignoreFieldsWithAnnotations = null;
		private Set<Class> ignoreClassWithAnnotations = null;
		private Set<MODIFIER> includeFieldsWithModifiers = null;
		private Boolean useField = null;
		private Boolean useAttribute = null;
		private int level = MAX_LEVEL;
		
		// class level configuration
		private Map<Class, ClassMapper> classMappers = null;
		private Set<FieldMapper> fieldMappers = null;
		
		private EnumType enumType = null;
		private Boolean date2Long = null;
		
		
		private Boolean getDate2Long() {
			return date2Long;
		}

		public void setDate2Long(Boolean date2Long) {
			this.date2Long = date2Long;
		}

		private EnumType getEnumType() {
			return enumType;
		}

		public void setEnumType(EnumType enumType) {
			this.enumType = enumType;
		}

		private Set<Class> getIgnoreClassWithAnnotations() {
			return ignoreClassWithAnnotations;
		}

		public void ignoreClassWithAnnotations(
				Set<Class> ignoreClassWithAnnotations) {
			this.ignoreClassWithAnnotations = ignoreClassWithAnnotations;
		}

		public void ignoreClassWithAnnotations(
				Class[] ignoreClassWithAnnotations) {
			this.ignoreClassWithAnnotations = new HashSet<Class>(Arrays.asList(ignoreClassWithAnnotations));
		}
		
		public void ignoreClassWithAnnotation(Class ignoreClassWithAnnotation) {
			if (this.ignoreClassWithAnnotations == null) {
				this.ignoreClassWithAnnotations = new HashSet<Class>();
			}
			
			this.ignoreClassWithAnnotations.add(ignoreClassWithAnnotation);
		}
		
		
		private Set<Class> getIgnoreFieldsWithAnnotations() {
			return ignoreFieldsWithAnnotations;
		}

		public void ignoreFieldsWithAnnotations(
				Set<Class> ignoreFieldsWithAnnotations) {
			this.ignoreFieldsWithAnnotations = ignoreFieldsWithAnnotations;
		}

		public void ignoreFieldsWithAnnotations(
				Class[] ignoreFieldsWithAnnotations) {
			this.ignoreFieldsWithAnnotations = new HashSet<Class>(Arrays.asList(ignoreFieldsWithAnnotations));
		}
		
		public void ignoreFieldsWithAnnotation(Class ignoreFieldsWithAnnotation) {
			if (this.ignoreFieldsWithAnnotations == null) {
				this.ignoreFieldsWithAnnotations = new HashSet<Class>();
			}
			
			this.ignoreFieldsWithAnnotations.add(ignoreFieldsWithAnnotation);
		}
		
		
		private Set<MODIFIER> getIncludeFieldsWithModifiers() {
			return includeFieldsWithModifiers;
		}

		public void includeFieldsWithModifiers(
				Set<MODIFIER> includeFieldsWithModifiers) {
			if (includeFieldsWithModifiers == null || includeFieldsWithModifiers.contains(MODIFIER.None)) {
				this.includeFieldsWithModifiers = null;
			}
			if (this.includeFieldsWithModifiers == null) {
				this.includeFieldsWithModifiers = includeFieldsWithModifiers;
			} else {
				this.includeFieldsWithModifiers.addAll(includeFieldsWithModifiers);
			}
		}
		
		public void includeFieldsWithModifiers(
				MODIFIER[] includeFieldsWithModifiers) {
			if (includeFieldsWithModifiers == null) {
				this.includeFieldsWithModifiers = null;
			} else {
				includeFieldsWithModifiers(new HashSet<MODIFIER>(Arrays.asList(includeFieldsWithModifiers)));
			}
		}
		
		public void includeFieldsWithModifiers(
				MODIFIER includeFieldsWithModifier) {
			if (includeFieldsWithModifier == null || includeFieldsWithModifier == MODIFIER.None) {
				this.includeFieldsWithModifiers = null;
			} else {
				if (this.includeFieldsWithModifiers == null) {
					this.includeFieldsWithModifiers = new HashSet<MODIFIER>();
				}
				
				this.includeFieldsWithModifiers.add(includeFieldsWithModifier);
			}
		}

		/*
		 * get json name during serialization
		 */
		private String java2Json(String name) {
			if (name == null) {
				return null;
			}
			name = name.trim();
			String lname = name.toLowerCase();
			if (lname.isEmpty()) {
				return null;
			}

			//Set<FieldMapper> fieldMappers
			if (fieldMappers == null) {
				return name;
			}

			for (FieldMapper mapper: fieldMappers) {
				String java = mapper.java;

				if (java != null && lname.equals(java.toLowerCase())) {
					// if is null, ignore it
					if (mapper.ignore) {
						return null;
					} else {
						return mapper.json;
					}
				}
			}

			return name;
		}



		/*
		 * get java name during deserialization
		 */
		private String json2Java(String name) {
			if (name == null) {
				return null;
			}
			name = name.trim();
			String lname = name.toLowerCase();
			if (lname.isEmpty()) {
				return null;
			}

			if (fieldMappers == null) {
				return name;
			}

			for (FieldMapper mapper: fieldMappers) {
				String json = mapper.json;

				if (json != null && lname.equals(json.toLowerCase())) {
					// if is null, ignore it
					if (mapper.ignore) {
						return null;
					} else {
						return mapper.java;
					}
				}
			}

			return name;
		}


		/*
		 * get json name during serialization
		 */
		private String java2Json(Field field) {
			String name = field.getName();

			if (name == null) {
				return null;
			}
			name = name.trim();
			String lname = name.toLowerCase();
			if (lname.isEmpty()) {
				return null;
			}

			//Set<FieldMapper> fieldMappers
			if (fieldMappers == null) {
				return name;
			}

			List<FieldMapper> names = new ArrayList<>();

			for (FieldMapper mapper: fieldMappers) {
				String java = mapper.java;

				if (java != null && lname.equals(java.toLowerCase())) {
					Class<?> type = mapper.type;

					if (type != null) {
						Field fld;
						try {
							fld = type.getDeclaredField(name);

							if (fld != null && fld.equals(field)) {
								if (mapper.ignore) {
									return null;
								} else {
									return mapper.json;
								}
							}

						} catch (NoSuchFieldException | SecurityException e) {
							// e.printStackTrace();
						}

					} else {
						names.add(mapper);
					}
				}
			}

			for (FieldMapper mapper: names) {
				if (mapper.ignore) {
					return null;
				} else {
					return mapper.json;
				}
			}

			return name;
		}


		/*
		 * get java name during deserialization
		 */
		private String json2Java(Field field) {
			String name = field.getName();

			if (name == null) {
				return null;
			}
			name = name.trim();
			String lname = name.toLowerCase();
			if (lname.isEmpty()) {
				return null;
			}

			if (fieldMappers == null) {
				return name;
			}

			List<FieldMapper> names = new ArrayList<>();

			for (FieldMapper mapper: fieldMappers) {
				String json = mapper.json;

				if (json != null && lname.equals(json.toLowerCase())) {
					Class<?> type = mapper.type;

					if (type != null) {
						Field fld;
						try {
							fld = type.getDeclaredField(name);

							if (fld != null && fld.equals(field)) {
								if (mapper.ignore) {
									return null;
								}
								return mapper.java;
							}

						} catch (NoSuchFieldException | SecurityException e) {
							// e.printStackTrace();
						}

					} else {
						names.add(mapper);
					}
				}
			}

			for (FieldMapper mapper: names) {
				if (mapper.ignore) {
					return null;
				} else {
					return mapper.java;
				}
			}

			return name;
		}


		private Function getDeserializer(String name, Class valueType, Class enclosingType) {
			if (StringUtil.isEmpty(name) || fieldMappers == null) {
				return getDeserializer(valueType);
			}

			name = name.trim();
			String lname = name.toLowerCase();

			List<Function> functions = new ArrayList<>();

			for (FieldMapper mapper: fieldMappers) {
				String java = mapper.java;
				String json = mapper.json;
				Class<?> type = mapper.type;

				if ((java != null && lname.equals(java.toLowerCase())) ||
						(json != null && lname.equals(json.toLowerCase())) ) {

					if (type == enclosingType) {
						if (mapper.deserializer != null) {
							return mapper.deserializer;
						}

					} else if ((type == null || enclosingType == null) && mapper.deserializer != null) {
						functions.add(mapper.deserializer);
					}
					
				}
			}

			if (functions.size() > 0) {
				return functions.get(0);
			}

			return getDeserializer(valueType);
		}

		
		private Function getDeserializer(Class valueType) {
			if (classMappers == null || valueType == null) {
				return null;
			}
			
			if (classMappers.containsKey(valueType)) {
				ClassMapper ClassMapper = classMappers.get(valueType);
				if (ClassMapper.ignore) {
					return null;
				}
				return ClassMapper.deserializer;
			} else {
				return null;
			}
		}
		

		private Function getSerializer(String name, Class valueType, Class enclosingType) {
			if (StringUtil.isEmpty(name) || fieldMappers == null) {
				return getSerializer(valueType);
			}
			
			name = name.trim();
			String lname = name.toLowerCase();

			List<Function> functions = new ArrayList<>();

			for (FieldMapper mapper: fieldMappers) {
				String java = mapper.java;
				String json = mapper.json;
				Class<?> type = mapper.type;

				if ((java != null && lname.equals(java.toLowerCase())) ||
						(json != null && lname.equals(json.toLowerCase())) ) {
					
					if (mapper.type == enclosingType) {
						if (mapper.serializer != null) {
							return mapper.serializer;
						}
					} else if ((type == null || enclosingType == null) && mapper.serializer != null) {
						functions.add(mapper.serializer);
					}
					
				}
			}

			if (functions.size() > 0) {
				return functions.get(0);
			}
			
			return getSerializer(valueType);
		}

		
		private Function getSerializer(Class valueType) {
			if (classMappers == null || valueType == null) {
				return null;
			}
			
			if (classMappers.containsKey(valueType)) {
				ClassMapper ClassMapper = classMappers.get(valueType);
				if (ClassMapper.ignore) {
					return null;
				}
				return ClassMapper.serializer;
			} else {
				return null;
			}
		}
		
		
		private Object getDefaultValue(Class valueType) {
			if (valueType == null) {
				return null;
			}
			Object value = null;
			
			if (classMappers != null && classMappers.containsKey(valueType)) {
				ClassMapper ClassMapper = classMappers.get(valueType);
				
				value = ClassMapper.defaultValue;
			}
			
			if (value != null) {
				return value;
			}
			
			return null;
		}

		protected int getIndentation() {
			return indentation;
		}

		public void setIndentation(int indentation) {
			if (indentation >= 0 && indentation < 10) {
				this.indentation = indentation;
			}
		}

		protected String getSimpleDateFormat() {
			return simpleDateFormat;
		}

		public void setSimpleDateFormat(String simpleDateFormat) {
			if (simpleDateFormat != null) {
				this.simpleDateFormat = simpleDateFormat;

				this.dateFormat = new SimpleDateFormat(simpleDateFormat);
			}
		}

		protected DateFormat getDateFormat() {
			return dateFormat;
		}

		public void setDateFormat(DateFormat dateFormat) {
			if (dateFormat != null) {
				if (dateFormat instanceof SimpleDateFormat) {
					SimpleDateFormat simpleDateFormat = (SimpleDateFormat) dateFormat;
					String patten = simpleDateFormat.toPattern();
					setSimpleDateFormat(patten);
				}

				this.dateFormat = dateFormat;
			}
		}

		protected JSON_INCLUDE getDefaultType() {
			return defaultType;
		}

		public void setDefaultType(JSON_INCLUDE defaultType) {
			if (defaultType != null) {
				this.defaultType = defaultType;
			}
		}

		protected Boolean getPrettyPrinting() {
			return prettyPrinting;
		}

		public void prettyPrinting(Boolean prettyPrinting) {
			if (prettyPrinting != null) {
				this.prettyPrinting = prettyPrinting;
			}
		}

		protected JSON_PROCESSOR getJsonProcessor() {
			return jsonProcessor;
		}

		public void setJsonProcessor(JSON_PROCESSOR jsonProcessor) {
			if (jsonProcessor != null) {
				this.jsonProcessor = jsonProcessor;
			}
		}

		private boolean getAnnotationSupport() {
			return annotationSupport;
		}

		public void setAnnotationSupport(boolean annotationSupport) {
			this.annotationSupport = annotationSupport;
		}

		private Boolean getOrderByKeyAndProperties() {
			return orderByKeyAndProperties;
		}

		public void setOrderByKeyAndProperties(Boolean orderByKeyAndProperties) {
			this.orderByKeyAndProperties = orderByKeyAndProperties;
		}

		private Boolean getIncludeClassTypeInJson() {
			return includeClassTypeInJson;
		}

		public void setIncludeClassTypeInJson(Boolean includeClassTypeInJson) {
			this.includeClassTypeInJson = includeClassTypeInJson;
		}

		private Boolean getPrintErrorUseOsonInFailure() {
			return printErrorUseOsonInFailure;
		}

		public void setPrintErrorUseOsonInFailure(
				Boolean printErrorUseOsonInFailure) {
			this.printErrorUseOsonInFailure = printErrorUseOsonInFailure;
		}

		private String getJsonClassType() {
			return jsonClassType;
		}

		public void setJsonClassType(String jsonClassType) {
			this.jsonClassType = jsonClassType;
		}

		private FIELD_NAMING getFieldNaming() {
			return fieldNaming;
		}

		public void setFieldNaming(FIELD_NAMING fieldNaming) {
			this.fieldNaming = fieldNaming;
		}

		private Double getIgnoreVersionsAfter() {
			return ignoreVersionsAfter;
		}

		public void ignoreVersionsAfter(Double ignoreVersionsAfter) {
			this.ignoreVersionsAfter = ignoreVersionsAfter;
		}

		/*
		 * The basic strategy:
		 * if set with Collection or Set, either reset if null, or merge
		 */
		private Map<Class, ClassMapper> getClassMappers() {
			return classMappers;
		}

		public void setClassMappers(Map<Class, ClassMapper> classMappers) {
			if (this.classMappers == null || classMappers == null) {
				this.classMappers = classMappers;
			} else {
				this.classMappers.putAll(classMappers);
			}
		}
		
		
		public void setClassMappers(ClassMapper[] classMappers) {
			if (this.classMappers == null) {
				this.classMappers = new HashMap<Class, ClassMapper>();
			}
			
			for (ClassMapper classMapper: classMappers) {
				if (classMapper.type != null) {
					this.classMappers.put(classMapper.type, classMapper);
				}
			}
		}
		public void setClassMappers(List<ClassMapper> classMappers) {
			if (this.classMappers == null) {
				this.classMappers = new HashMap<Class, ClassMapper>();
			}
			
			for (ClassMapper classMapper: classMappers) {
				if (classMapper.type != null) {
					this.classMappers.put(classMapper.type, classMapper);
				}
			}
		}
		public void setClassMappers(ClassMapper classMapper) {
			if (classMapper.type == null) {
				return;
			}
			if (this.classMappers == null) {
				this.classMappers = new HashMap<Class, ClassMapper>();
			}
			
			this.classMappers.put(classMapper.type, classMapper);
		}
		
		private Set<FieldMapper> getFieldMappers() {
			return fieldMappers;
		}

		public void setFieldMappers(Set<FieldMapper> fieldMappers) {
			if (this.fieldMappers == null || fieldMappers == null) {
				this.fieldMappers = fieldMappers;
			} else {
				this.fieldMappers.addAll(fieldMappers);
			}
		}
		
		public void setFieldMappers(FieldMapper[] fieldMappers) {
			if (fieldMappers == null) {
				this.fieldMappers = null;
			} else {
				if (this.fieldMappers == null) {
					this.fieldMappers = new HashSet<>();
				}
				
				for (FieldMapper fieldMapper: fieldMappers) {
					if (fieldMapper.isValid() && !this.fieldMappers.contains(fieldMapper)) {
						this.fieldMappers.add(fieldMapper);
					}
				}
			}
		}
		
		public void setFieldMappers(FieldMapper fieldMapper) {
			if (fieldMapper == null || !fieldMapper.isValid()) {
				return;
			}

			if (this.fieldMappers == null) {
				this.fieldMappers = new HashSet<>();
			}
			
			this.fieldMappers.add(fieldMapper);
		}

		private Boolean isUseField() {
			return useField;
		}

		public void setUseField(Boolean useField) {
			this.useField = useField;
		}

		private Boolean isUseAttribute() {
			return useAttribute;
		}

		public void setUseAttribute(Boolean useAttribute) {
			this.useAttribute = useAttribute;
		}
		
		private int getLevel() {
			return level;
		}

		/*
		 * Configure the maximum level of data the output will be
		 */
		public void setLevel(int level) {
			if (level > 0 && level <= MAX_LEVEL) {
				this.level = level;
			}
		}
	}

	
	public static class ClassData<T> {
		private Map<String, Object> map;
		private Class<T> valueType;
		private T obj;
		private ClassMapper classMapper;
		private int level;
		
		public int getLevel() {
			return level;
		}

		public ClassData(Map<String, Object> map, Class<T> valueType, T obj, ClassMapper classMapper) {
			// when expose the data, do not get corrupted
			this.map = new HashMap(map);
			this.valueType = valueType;
			this.obj = obj;
			ClassMapper mapper = new ClassMapper();
			mapper = overwriteBy (mapper, classMapper);
			this.classMapper = mapper;
		}
		
		public ClassData(Class<T> valueType, T obj, ClassMapper classMapper, int level) {
			// when expose the data, do not get corrupted
			this.valueType = valueType;
			this.obj = obj;
			ClassMapper mapper = new ClassMapper();
			mapper = overwriteBy (mapper, classMapper);
			this.classMapper = mapper;
			this.level = level;
		}

		public Map<String, Object> getMap() {
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

	private static class FieldData<T, E> {
		public T enclosingObj;
		public Field field;
		public Object valueToProcess;
		public Class<E> returnType;
		public E returnObj = null;
		public Type erasedType = null;
		public boolean json2Java = true;
		
		// extra field information
		public Method getter;
		public Method setter;
		public FieldMapper mapper = null;
		public ClassMapper classMapper = null;
		
		
		Boolean required = false;
		JSON_INCLUDE defaultType = JSON_INCLUDE.NONE;
		private Class enclosingtype = null;
		EnumType enumType = null;
		Integer length = null; // Default: 255
		Integer scale = null; // Default: 0
		Integer min = null; // default (int) 0;
		Integer max = null; // default (int) 2147483647;

		public Object defaultValue = null;
		public boolean jsonRawValue = false;
		
		public int level;
		public Set set;
		// for internal use
		public boolean doubleQuote = false;
		

		public FieldData(T enclosingObj, Field field, Object valueToProcess,
				Class<E> returnType, boolean json2Java, FieldMapper mapper) {
			this.enclosingObj = enclosingObj; // enclosing object
			this.field = field;
			this.valueToProcess = valueToProcess;
			this.returnType = returnType;
			this.json2Java = json2Java;
			this.mapper = mapper;
			
			this.required = mapper.required;
			this.defaultType = mapper.defaultType;
			this.length = mapper.length;
			this.scale = mapper.scale;
			this.min = mapper.min;
			this.max = mapper.max;
			this.defaultValue = mapper.defaultValue;
			this.jsonRawValue = mapper.jsonRawValue;
		}


		public FieldData(Object valueToProcess, Class<E> returnType) {
			this.valueToProcess = valueToProcess;
			this.returnType = returnType;
		}

		public FieldData(Object valueToProcess, Class<E> returnType, boolean json2Java) {
			this(valueToProcess, returnType);
			this.json2Java = json2Java;
		}
		
		public FieldData(Object valueToProcess, Class<E> returnType, boolean json2Java, E returnObj) {
			this(valueToProcess, returnType, json2Java);
			this.returnObj = returnObj;
		}

		public FieldData(Object valueToProcess, Class<E> returnType, boolean json2Java, Type type) {
			this(valueToProcess, returnType, json2Java);
			this.erasedType = type; // generic type information
		}

		public FieldData(Object valueToProcess, Type type, boolean json2Java) {
			this.valueToProcess = valueToProcess; // value to interpret
			this.erasedType = type; // generic type information
			this.json2Java = json2Java;
		}


		public Class getEnclosingType() {
			if (enclosingtype != null) {
				return enclosingtype;
			}
			
			if (enclosingObj != null) {
				enclosingtype = enclosingObj.getClass();
			}
			if (enclosingtype == null && erasedType != null) {
				enclosingtype = ObjectUtil.getTypeClass(erasedType);
			}
			if (enclosingtype == null && mapper != null) {
				enclosingtype = mapper.type;
			}

			return enclosingtype;
		}

		
		public String getDefaultName() {
			if (mapper != null) {
				if (mapper.java != null) {
					return mapper.java;
				} else {
					return mapper.json;
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
			if (mapper != null && mapper.deserializer != null) {
				return mapper.deserializer;
			}
			if (classMapper != null && classMapper.deserializer != null) {
				return classMapper.deserializer;
			}
			
			return null;
		}
		
		public Function getSerializer() {
			if (mapper != null && mapper.serializer != null) {
				return mapper.serializer;
			}
			if (classMapper != null && classMapper.serializer != null) {
				return classMapper.serializer;
			}
			
			return null;
		}

		public Integer getMin() {
			if (min == null && classMapper != null) {
				min = classMapper.min;
			}
			
			if (min != null && returnType != null && (Number.class.isAssignableFrom(returnType)
					|| returnType.isPrimitive())) {
				if (returnType == Short.class || returnType == short.class) {
					if (min.intValue() < Short.MIN_VALUE) {
						min = (int) Short.MIN_VALUE;
					}
					
				} else if (returnType == Byte.class || returnType == byte.class) {
					if (min.intValue() < Byte.MIN_VALUE) {
						min = (int) Byte.MIN_VALUE;
					}
				}
			}
			
			return min;
		}
		public Integer getMax() {
			if (max == null && classMapper != null) {
				max = classMapper.max;
			}
			
			if (max != null && returnType != null && (Number.class.isAssignableFrom(returnType)
					|| returnType.isPrimitive())) {
				if (returnType == Short.class || returnType == short.class) {
					if (max.intValue() > Short.MAX_VALUE) {
						max = (int) Short.MAX_VALUE;
					}
					
				} else if (returnType == Byte.class || returnType == byte.class) {
					if (max.intValue() > Byte.MAX_VALUE) {
						max = (int) Byte.MAX_VALUE;
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
					getter.setAccessible(true);
					try {
						defaultValue = getter.invoke(enclosingObj, null);
					} catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
						// e.printStackTrace();
						defaultValue = getter.getDefaultValue();
					}
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
	}

	//private static Oson converter = null;

	private Options options = new Options();
	private ObjectMapper jackson = null;
	private Gson gson = null;
	
	private enum METHOD {
		SET(0), GET(1), OTHER(2);
		private int value;
		 
		private METHOD(int value) {
			this.value = value;
		}
	}

	private static Map<Class, Field[]> cachedFields = new ConcurrentHashMap<>();
	private static Map<String, Map <String, Method>[]> cachedMethods = new ConcurrentHashMap<>();

	////////////////////////////////////////////////////////////////////////////////
	// END OF variables and class definition
	////////////////////////////////////////////////////////////////////////////////


	public Oson() {
	}

	public Oson(JSONObject json) {
		configure(json);
	}

	public Oson(String json) {
		configure(json);
	}

	public Oson(Object[] array) {
		configure(array);
	}

	public Oson(Map<String, Object> map) {
		configure(map);
	}

	public Oson(Options options) {
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
		Options options = this.deserialize(map, Options.class);

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

	private JSON_PROCESSOR getJsonProcessor() {
		return options.getJsonProcessor();
	}

	public Oson setJsonProcessor(JSON_PROCESSOR jsonProcessor) {
		if (jsonProcessor != null) {
			options.setJsonProcessor(jsonProcessor);
			reset();
		}
		
		return this;
	}

	public Oson asJackson() {
		return setJsonProcessor(JSON_PROCESSOR.JACKSON);
	}
	
	public Oson asGson() {
		return setJsonProcessor(JSON_PROCESSOR.GSON);
	}
	
	public Oson asOson() {
		return setJsonProcessor(JSON_PROCESSOR.OSON);
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

	public Oson setDateFormat(String simpleDateFormat) {
		if (simpleDateFormat != null) {
			options.setSimpleDateFormat(simpleDateFormat);
			reset();
		}

		return this;
	}

	private boolean getPrettyPrinting() {
		return options.getPrettyPrinting();
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
			return String.valueOf(SPACE);
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

	private int getIndentation() {
		return options.getIndentation();
	}

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
	

	private InstanceCreator getTypeAdapter(Class type) {
		ClassMapper classMapper = getClassMapper(type);
		
		if (classMapper == null || classMapper.ignore) {
			return null;
		}
		
		return classMapper.constructor;
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
		if (annotation instanceof JsonIgnoreType) {
			JsonIgnoreType jsonIgnoreType = (JsonIgnoreType)annotation;
			if (jsonIgnoreType.value()) {
				return true;
			}
			
		} else if (annotation instanceof org.codehaus.jackson.annotate.JsonIgnoreType) {
			org.codehaus.jackson.annotate.JsonIgnoreType jsonIgnoreType = (org.codehaus.jackson.annotate.JsonIgnoreType)annotation;
			if (jsonIgnoreType.value()) {
				return true;
			}
		}

		
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
			if (annotation.getClass() == ann || ann.isAssignableFrom(annotation.getClass())) {
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
			if (annotationClass == ann || ann.isAssignableFrom(annotationClass)) {
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

	private Boolean getPrintErrorUseOsonInFailure() {
		return options.getPrintErrorUseOsonInFailure();
	}

	public Oson setPrintErrorUseOsonInFailure(
			Boolean printErrorUseOsonInFailure) {
		if (printErrorUseOsonInFailure != null) {
			options.setPrintErrorUseOsonInFailure(printErrorUseOsonInFailure);
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

	private Double getIgnoreVersionsAfter() {
		return options.getIgnoreVersionsAfter();
	}

	public Oson ignoreVersionsAfter(Double ignoreVersionsAfter) {
		options.ignoreVersionsAfter(ignoreVersionsAfter);
		reset();

		return this;
	}
	
	private boolean ignoreVersionsAfter(double ignoreVersionsAfter) {
		Double version = getIgnoreVersionsAfter();
		if (version == null || ignoreVersionsAfter <= version) {
			return false;
		}

		return true;
	}
	
	
	private Map<Class, ClassMapper> getClassMappers() {
		return options.getClassMappers();
	}

	private ClassMapper getClassMapper(Class valueType) {
		if (valueType == null) {
			return null;
		}
		
		Map<Class, ClassMapper> mappers = getClassMappers();
		
		if (mappers == null) {
			return null;
		}
		
		return mappers.get(valueType);
	}
	
	/*
	 * if a specific attribute is null, set it to the global setting
	 */
	private ClassMapper globalize(ClassMapper mapper) {
		// a little bit convenient
		// && Date.class.isAssignableFrom(valueType)
		if (mapper.simpleDateFormat == null) {
			mapper.simpleDateFormat =  getSimpleDateFormat();
		}
		
		if (mapper.includeClassTypeInJson == null) {
			mapper.includeClassTypeInJson =  getIncludeClassTypeInJson();
		}
		
		if (mapper.orderByKeyAndProperties == null) {
			mapper.orderByKeyAndProperties =  getOrderByKeyAndProperties();
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
		
		if (mapper.ignoreVersionsAfter == null) {
			mapper.ignoreVersionsAfter =  getIgnoreVersionsAfter();
		}
		
		if (mapper.includeFieldsWithModifiers == null) {
			mapper.includeFieldsWithModifiers =  getIncludeFieldsWithModifiers();
		}
		
		if (mapper.defaultType == null) {
			mapper.defaultType =  getDefaultType();
		}
		
		if (mapper.enumType == null) {
			mapper.enumType =  getEnumType();
		}
		
		if (mapper.date2Long == null) {
			mapper.date2Long =  getDate2Long();
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
	
	private Set<FieldMapper> getFieldMappers() {
		return options.getFieldMappers();
	}
	

	private FieldMapper getFieldMapper(String name, Class classtype) {
		Set<FieldMapper> fieldMappers = getFieldMappers();
		FieldMapper fieldMapper = null;
		
		if (fieldMappers != null && name != null && name.trim().length() > 0) {
			name = name.trim();
			String lname = name.toLowerCase();
			
			Set<FieldMapper> mapps = new HashSet<>();

			for (FieldMapper mapper: fieldMappers) {
				String java = mapper.java;
				String json = mapper.json;
	
				if (java != null && lname.equals(java.toLowerCase())) {
					if (mapper.type == null) {
						mapps.add(mapper);
						
					} else if (mapper.type == classtype) {
						fieldMapper = mapper;
						break;
					}
					
				} else if (json != null && lname.equals(json.toLowerCase())) {
					if (mapper.type == null) {
						mapps.add(mapper);
						
					} else if (mapper.type == classtype) {
						fieldMapper = mapper;
						break;
					}
					
				}
			}
	
			if (fieldMapper == null && mapps.size() > 0) {
				for (FieldMapper mapper: mapps) {
					fieldMapper = mapper;
					break;
				}
			}
		}
		
		if (fieldMapper != null) {
			fieldMapper.type = classtype;
		}

		return fieldMapper;
	}
	
	
		
	private FieldMapper classifyFieldMapper(FieldMapper fieldMapper, ClassMapper classMapper) {
		// classify it now
		if (fieldMapper.useAttribute == null) {
			fieldMapper.useAttribute = classMapper.useAttribute;
		}
		
		if (fieldMapper.useField == null) {
			fieldMapper.useField = classMapper.useField;
		}
		
		if (fieldMapper.simpleDateFormat == null) {
			fieldMapper.simpleDateFormat = classMapper.simpleDateFormat;
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
		
		return fieldMapper;
	}

	
	public Oson setFieldMappers(Set<FieldMapper> fieldMappers) {
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
	
	private Boolean isUseField() {
		return options.isUseField();
	}

	public Oson setUseField(Boolean useField) {
		options.setUseField(useField);
		reset();

		return this;
	}

	private Boolean isUseAttribute() {
		return options.isUseAttribute();
	}

	public Oson setUseAttribute(Boolean useAttribute) {
		options.setUseAttribute(useAttribute);
		reset();

		return this;
	}
	
	private int getLevel() {
		return options.getLevel();
	}

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
	
	
	private void reset() {
		jackson = null;
		gson = null;
	}

	private ObjectMapper getJackson() {
		if (jackson == null) {
			jackson = new ObjectMapper();
			prefixProcessing();
			
			// do not fail for funny reason
			jackson.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			jackson.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

			switch (getDefaultType()) {
			case ALWAYS:
				jackson.setSerializationInclusion(Include.ALWAYS);
				break;
			case NON_NULL:
				jackson.setSerializationInclusion(Include.NON_NULL);
				break;
			case NON_EMPTY:
				jackson.setSerializationInclusion(Include.NON_EMPTY);
				break;
			case DEFAULT:
				jackson.setSerializationInclusion(Include.USE_DEFAULTS);
				break;
			default:
				jackson.setSerializationInclusion(Include.NON_NULL);
				break;
			}

			if (getPrettyPrinting() && getIndentation() > 0) {
				jackson.enable(SerializationFeature.INDENT_OUTPUT);
				jackson.configure(SerializationFeature.INDENT_OUTPUT, true);
			}

			if (getOrderByKeyAndProperties()) {
				jackson.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
			}

			Set<FieldMapper> mappers = getFieldMappers();
			if (mappers != null) {
				//defines how names of JSON properties ("external names") are derived from names of POJO methods and fields ("internal names"),
				// in cases where they are not auto-detected and no explicit annotations exist for naming
				// config - Configuration in used: either SerializationConfig or DeserializationConfig,
				// depending on whether method is called during serialization or deserialization
				jackson.setPropertyNamingStrategy(new PropertyNamingStrategy() {
					@Override
					public String nameForField(MapperConfig<?> config, AnnotatedField field, String defaultName) {
						String name = defaultName;
						if (name == null) {
							name = field.getName();
						}
						name = name.trim();
						if (name.isEmpty()) {
							return super.nameForField(config, field, defaultName);
						}

						String lname = name.toLowerCase();

						if (config instanceof SerializationConfig || !(config instanceof DeserializationConfig)) {
							for (FieldMapper mapper: mappers) {
								String java = mapper.java;

								if (java != null && lname.equals(java.toLowerCase())) {
									Class<?> type = mapper.type;
									String fullName = field.getFullName(); // ca.oson.json.Artist#age;
									int index = fullName.indexOf('#');
									if (index > -1) {
										fullName = fullName.substring(0, index);
									}
									
									if (type != null) {
										try {
											// mapper.type.getTypeName() is the same as mapper.type.getName()
											if (type.getTypeName().equals(fullName)) {
												return mapper.json;
											}

										} catch (SecurityException e) {
											// e.printStackTrace();
										}

									} else { // just by name
										return mapper.json;
									}
								}
							}

						}

						if (config instanceof DeserializationConfig || !(config instanceof SerializationConfig)) {
							for (FieldMapper mapper: mappers) {
								String json = mapper.json;

								if (json != null && lname.equals(json.toLowerCase())) {
									Class<?> type = mapper.type;
									String fullName = field.getFullName(); // ca.oson.json.Artist#age;
									int index = fullName.indexOf('#');
									if (index > -1) {
										fullName = fullName.substring(0, index);
									}
									
									if (type != null) {
										try {
											// mapper.type.getTypeName() is the same as mapper.type.getName()
											if (type.getTypeName().equals(fullName)) {
												return mapper.java;
											}

										} catch (SecurityException e) {
											// e.printStackTrace();
										}

									} else { // just by name
										return mapper.java;
									}
								}
							}

						}

						return super.nameForField(config, field, defaultName);
					}

					@Override
					public String nameForGetterMethod(MapperConfig<?> config, AnnotatedMethod method, String defaultName) {
						String name = defaultName;
						if (name == null) {
							name = method.getName().substring(3);
						}
						name = name.trim();
						if (name.isEmpty()) {
							return super.nameForGetterMethod(config, method, defaultName);
						}

						String lname = name.toLowerCase();
						
						if (config instanceof SerializationConfig || !(config instanceof DeserializationConfig)) {
							for (FieldMapper mapper: mappers) {
								String java = mapper.java;

								if (java != null && lname.equals(java.toLowerCase())) {
									Class<?> type = mapper.type;
									String fullName = method.getFullName(); // java.util.Date#getTime(0 params)
									int index = fullName.indexOf('#');
									if (index > -1) {
										fullName = fullName.substring(0, index);
									}
									
									if (type != null) {
										try {
											// mapper.type.getTypeName() is the same as mapper.type.getName()
											if (type.getTypeName().equals(fullName)) {
												return mapper.json;
											}

										} catch (SecurityException e) {
											// e.printStackTrace();
										}

									} else { // just by name
										return mapper.json;
									}
								}
							}

						}


						if (config instanceof DeserializationConfig || !(config instanceof SerializationConfig)) {
							for (FieldMapper mapper: mappers) {
								String json = mapper.json;

								if (json != null && lname.equals(json.toLowerCase())) {
									Class<?> type = mapper.type;
									String fullName = method.getFullName(); // java.util.Date#getTime(0 params)
									int index = fullName.indexOf('#');
									if (index > -1) {
										fullName = fullName.substring(0, index);
									}
									
									if (type != null) {
										try {
											// mapper.type.getTypeName() is the same as mapper.type.getName()
											if (type.getTypeName().equals(fullName)) {
												return mapper.java;
											}

										} catch (SecurityException e) {
											// e.printStackTrace();
										}

									} else { // just by name
										return mapper.java;
									}
								}
							}

						}

						return super.nameForGetterMethod(config, method, defaultName);
					  }


					/*
					 * (non-Javadoc)
					 * @see com.fasterxml.jackson.databind.PropertyNamingStrategy#nameForSetterMethod(com.fasterxml.jackson.databind.cfg.MapperConfig, com.fasterxml.jackson.databind.introspect.AnnotatedMethod, java.lang.String)
					 *
					 * Method called to find external name (name used in JSON) for given logical POJO property, as defined by given setter method; typically called when building a deserializer (but not necessarily only then).
					 */
					@Override
					public String nameForSetterMethod(MapperConfig<?> config,
                            AnnotatedMethod method,
                            String defaultName) {
						String name = defaultName;
						if (name == null) {
							name = method.getName().substring(3);
						}
						name = name.trim();
						if (name.isEmpty()) {
							return super.nameForSetterMethod(config, method, defaultName);
						}

						String lname = name.toLowerCase();

						if (config instanceof SerializationConfig || !(config instanceof DeserializationConfig)) {
							for (FieldMapper mapper: mappers) {
								String java = mapper.java;

								if (java != null && lname.equals(java.toLowerCase())) {
									Class<?> type = mapper.type;
									String fullName = method.getFullName(); // java.util.Date#getTime(0 params)
									int index = fullName.indexOf('#');
									if (index > -1) {
										fullName = fullName.substring(0, index);
									}
									
									if (type != null) {
										try {
											// mapper.type.getTypeName() is the same as mapper.type.getName()
											if (type.getTypeName().equals(fullName)) {
												return mapper.json;
											}

										} catch (SecurityException e) {
											// e.printStackTrace();
										}

									} else { // just by name
										return mapper.json;
									}
								}
							}

						}


						if (config instanceof DeserializationConfig || !(config instanceof SerializationConfig)) {
							for (FieldMapper mapper: mappers) {
								String json = mapper.json;

								if (json != null && lname.equals(json.toLowerCase())) {
									Class<?> type = mapper.type;
									String fullName = method.getFullName(); // java.util.Date#getTime(0 params)
									int index = fullName.indexOf('#');
									if (index > -1) {
										fullName = fullName.substring(0, index);
									}
									
									if (type != null) {
										try {
											// mapper.type.getTypeName() is the same as mapper.type.getName()
											if (type.getTypeName().equals(fullName)) {
												return mapper.java;
											}

										} catch (SecurityException e) {
											// e.printStackTrace();
										}

									} else { // just by name
										return mapper.java;
									}
								}
							}

						}

						return super.nameForSetterMethod(config, method, defaultName);
					}



					/*
					 * (non-Javadoc)
					 * @see com.fasterxml.jackson.databind.PropertyNamingStrategy#nameForConstructorParameter(com.fasterxml.jackson.databind.cfg.MapperConfig, com.fasterxml.jackson.databind.introspect.AnnotatedParameter, java.lang.String)
					 *
					 * find external name (name used in JSON) for given logical POJO property, as defined by given setter method; typically called when building a deserializer (but not necessarily only then).
					 */
					@Override
					public String nameForConstructorParameter(MapperConfig<?> config,
		                    AnnotatedParameter ctorParam,
		                    String defaultName) {
						String name = defaultName;
						if (name == null) {
							name = ctorParam.getName();
						}
						name = name.trim();
						if (name.isEmpty()) {
							return super.nameForConstructorParameter(config, ctorParam, defaultName);
						}

						String lname = name.toLowerCase();

						if (config instanceof SerializationConfig || !(config instanceof DeserializationConfig)) {
							for (FieldMapper mapper: mappers) {
								String java = mapper.java;

								if (java != null && lname.equals(java.toLowerCase())) {
									Class<?> type = mapper.type;
									String fullName = ctorParam.getName(); // java.util.Date#
									int index = fullName.indexOf('#');
									if (index > -1) {
										fullName = fullName.substring(0, index);
									}
									
									if (type != null) {
										try {
											// mapper.type.getTypeName() is the same as mapper.type.getName()
											if (type.getTypeName().equals(fullName)) {
												return mapper.json;
											}

										} catch (SecurityException e) {
											// e.printStackTrace();
										}

									} else { // just by name
										String json = mapper.json;

										if (json == null) {
											// do nothing?

										} else {
											return json;
										}
									}
								}
							}

						}


						if (config instanceof DeserializationConfig || !(config instanceof SerializationConfig)) {
							for (FieldMapper mapper: mappers) {
								String json = mapper.json;

								if (json != null && lname.equals(json.toLowerCase())) {
									Class<?> type = mapper.type;
									String fullName = ctorParam.getName(); // java.util.Date#
									int index = fullName.indexOf('#');
									if (index > -1) {
										fullName = fullName.substring(0, index);
									}
									
									if (type != null) {
										try {
											// mapper.type.getTypeName() is the same as mapper.type.getName()
											if (type.getTypeName().equals(fullName)) {
												return mapper.java;
											}

										} catch (SecurityException e) {
											// e.printStackTrace();
										}

									} else { // just by name
										return mapper.java;
									}
								}
							}

						}

						return super.nameForConstructorParameter(config, ctorParam, defaultName);
					}

				});
			}



			jackson.setDateFormat(getDateFormat());
		}



		return jackson;
	}

	
	private void prefixProcessing() {
		if (getJsonProcessor() != JSON_PROCESSOR.JACKSON) {
			return;
		}
		
		Set<FieldMapper> mappers = getFieldMappers();
		if (mappers == null) {
			return;
		}
		
		for (FieldMapper mapper: mappers) {
			if (mapper.isProcessed()) {
				continue;
			}
			
			String json = mapper.json;
			String java = mapper.java;
			
			if (mapper.type == null || json == null || java == null) {
				mapper.setProcessed(true);
				continue;
			}

			String className = mapper.type.getName();
			if (mapper.ignore) {
				
				try {
					if (java != null) {
						ObjectUtil.addAnnotationToField(className, java,
						        "com.fasterxml.jackson.annotation.JsonIgnore");
						
					} else if (json != null) {
						ObjectUtil.addAnnotationToField(className, json,
						        "com.fasterxml.jackson.annotation.JsonIgnore");
					}
					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				mapper.setProcessed(true);
			}
			
			if (json == null) {
				// serialize
				// String methodName = "get" + StringUtil.capitalize(java);
				try {
					ObjectUtil.addAnnotationToField(className, java,
					        "com.fasterxml.jackson.annotation.JsonIgnore");
					mapper.setProcessed(true);

				} catch (Exception e) {
					e.printStackTrace();
					if (!mapper.isProcessed()) {
						// should ignore it, have to use mixin abstract class
						try {
							ObjectUtil.addAnnotationToField(className, java,
							        "com.fasterxml.jackson.annotation.JsonIgnore", mixin);

							jackson.addMixInAnnotations(mapper.getClass(), Class.forName(className + mixin));
							
							mapper.setProcessed(true);
							
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						
					}
				}

			} else if (java == null) {
				// deserialize
				//String methodName = "set" + StringUtil.capitalize(json);
				try {
					ObjectUtil.addAnnotationToField(className, json,
					        "com.fasterxml.jackson.annotation.JsonIgnore");
					mapper.setProcessed(true);
				} catch (Exception e) {
					e.printStackTrace();
					if (!mapper.isProcessed()) {
						// should ignore it, have to use mixin abstract class
						try {
							ObjectUtil.addAnnotationToField(className, json,
							        "com.fasterxml.jackson.annotation.JsonIgnore", mixin);

							jackson.addMixInAnnotations(mapper.getClass(), Class.forName(className + mixin));
							
							mapper.setProcessed(true);
							
						} catch (Exception ex) {
							ex.printStackTrace();
						}
						
					}
				}
			}

		}
	}

	
	private Gson getGson() {
		if (gson == null) {
			GsonBuilder gsonBuilder = new GsonBuilder();

			switch (getDefaultType()) {
			case ALWAYS:
				gsonBuilder.serializeNulls();
				break;
			case NON_NULL:
				break;
			case NON_EMPTY:
				break;
			case DEFAULT:
				gsonBuilder.serializeNulls();
				break;
			default:
				gsonBuilder.serializeNulls();
				break;
			}

			switch (getFieldNaming()) {
			case FIELD: // original field name: someField_name
				gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);
				break;
			case CAMELCASE: // someFieldName
				gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);
				break;
			case UPPER_CAMELCASE: // SomeFieldName
				gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE);
				break;
			case UNDERSCORE_CAMELCASE: // some_Field_Name
				gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
				break;
			case UNDERSCORE_UPPER_CAMELCASE: // Some_Field_Name
				gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
				break;
			case UNDERSCORE_LOWER: // some_field_name
				gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
				break;
			case UNDERSCORE_UPPER: // SOME_FIELD_NAME
				gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES);
				break;
			case SPACE_CAMELCASE: // some Field Name
				gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES);
				break;
			case SPACE_UPPER_CAMELCASE: // Some Field Name
				gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES);
				break;
			case SPACE_LOWER: // some field name
				gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES);
				break;
			case SPACE_UPPER: // SOME FIELD NAME
				gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE_WITH_SPACES);
				break;
			case DASH_CAMELCASE: // some-Field-Name
				gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES);
				break;
			case DASH_UPPER_CAMELCASE: // Some-Field-Name
				gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES);
				break;
			case DASH_LOWER: // some-field-name
				gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES);
				break;
			case DASH_UPPER: // SOME-FIELD-NAME
				gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_DASHES);
				break;
			default:
				gsonBuilder.setFieldNamingPolicy(FieldNamingPolicy.IDENTITY);
				break;
			}


			if (getPrettyPrinting() && getIndentation() > 0) {
				gsonBuilder.setPrettyPrinting();
			}

			gsonBuilder.setDateFormat(options.getSimpleDateFormat());

			Set<FieldMapper> mappers = getFieldMappers();
			Map<Class, ClassMapper> classMappers = getClassMappers();
			List<ExclusionStrategy> strategies = new ArrayList<>();
			if (mappers != null) {
				gsonBuilder.setFieldNamingStrategy(new FieldNamingStrategy() {
					@Override
					public String translateName(Field f) {
						String fieldName = f.getName();
						String serializedName = java2Json(f);
						if (!fieldName.equalsIgnoreCase(serializedName)) {
							// if null returned, this field is ignored
							return serializedName;
						}
						return json2Java(f);
					}
				});
				
				
				for (FieldMapper mapper: mappers) {
					if (mapper.java == null || mapper.json == null || mapper.ignore) {
						strategies.add(new ExclusionStrategy() {

							@Override
							public boolean shouldSkipField(FieldAttributes f) {
								String name = f.getName();
								Class cls = f.getClass();
								
								if (mapper.java == null) {
									if (mapper.json.equals(name)) {
										if (mapper.type == null || cls.equals(mapper.type)) {
											return true;
										}
									}
									
								} else if (mapper.json == null || mapper.ignore) {
									if (mapper.java.equals(name)) {
										if (mapper.type == null || cls.equals(mapper.type)) {
											return true;
										}
									}
									
								}
								
								return false;
							}

							@Override
							public boolean shouldSkipClass(Class<?> clazz) {
								return false;
							}
							});
					}
				}
			}

			
			if (classMappers != null) {
				for (Entry<Class, ClassMapper> entry: classMappers.entrySet()) {
					ClassMapper mapper = entry.getValue();
					if (mapper.type == null) {
						mapper.type = entry.getKey();
					}
					if (mapper.ignore) {
						strategies.add(new ExclusionStrategy() {

							@Override
							public boolean shouldSkipField(FieldAttributes f) {
								return false;
							}

							@Override
							public boolean shouldSkipClass(Class<?> clazz) {
								if (clazz.equals(mapper.type)) {
									return true;
								}
								return false;
							}
						});
					}
				}
				
				for (Entry<Class, ClassMapper> entry: classMappers.entrySet()) {
					ClassMapper mapper = entry.getValue();
					if (!mapper.ignore && mapper.constructor != null) {
						gsonBuilder.registerTypeAdapter(entry.getKey(), mapper.constructor);
					}
				}
			}
			
			int size = strategies.size();
			if (size > 0) {
				gsonBuilder.setExclusionStrategies(strategies.toArray(new ExclusionStrategy[size]));
			}
			
			Double ignoreVersionsAfter = getIgnoreVersionsAfter();
			if (ignoreVersionsAfter != null) {
				gsonBuilder.setVersion(ignoreVersionsAfter);
			}
			
			
			gson = gsonBuilder.create();
		}

		return gson;
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


	private <E> Double json2Double(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			Double valueToReturn = null;
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						// suppose to return Double, but in case not, try to process
						if (function instanceof Json2DoubleFunction) {
							valueToReturn = ((Json2DoubleFunction)function).apply(valueToProcess);
						} else {
							
							Object returnedValue = function.apply(valueToProcess);

							if (returnedValue instanceof Optional) {
								Optional opt = (Optional)returnedValue;
								returnedValue = opt.orElse(null);
							}
							
							if (returnedValue == null) {
								return json2DoubleDefault(objectDTO);
								
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
							
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					valueToReturn = Double.parseDouble(valueToProcess);
				}
				
				if (valueToReturn != null) {
					Integer min = objectDTO.getMin();
					Integer max = objectDTO.getMax();
					
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
	
	


	private <E> String double2Json(FieldData objectDTO) {
		if (objectDTO == null || objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;

		if (value != null && value.toString().trim().length() > 0) {
			Double valueToProcess = null;
			String valueToReturn = null;
			
			if (value instanceof Double) {
				valueToProcess = (Double)value;
			} else {
				try {
					valueToProcess = Double.valueOf(value.toString().trim());
				} catch (Exception ex) {}
			}
			
			if (valueToProcess != null) {
				try {
					Function function = objectDTO.getSerializer();
					if (function != null) {
						try {
							if (function instanceof Double2JsonFunction) {
								return ((Double2JsonFunction)function).apply(valueToProcess);
								
							} else {
								
								Object returnedValue = function.apply(valueToProcess);
							
								if (returnedValue == null) {
									return double2JsonDefault(objectDTO);
								} else {
									objectDTO.valueToProcess = returnedValue;
									return object2Json(objectDTO);
								}
								
							}
							
						} catch (Exception e) {}
					}

					if (valueToProcess != null) {
						Integer min = objectDTO.getMin();
						Integer max = objectDTO.getMax();
						
						if (min != null && min.doubleValue() > valueToProcess) {
							valueToProcess = min.doubleValue();
						}
						
						if (max != null && max.doubleValue() < valueToProcess) {
							valueToProcess = max.doubleValue();
						}
						
						return valueToProcess.toString();
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
		
	private <E> Double json2DoubleDefault(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;
		
		Integer min = objectDTO.getMin();
		Integer max = objectDTO.getMax();
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

	private <E> Float json2Float(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			Float valueToReturn = null;
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						// suppose to return Float, but in case not, try to process
						if (function instanceof Json2FloatFunction) {
							valueToReturn = ((Json2FloatFunction)function).apply(valueToProcess);
						} else {
							
							Object returnedValue = function.apply(valueToProcess);

							if (returnedValue instanceof Optional) {
								Optional opt = (Optional)returnedValue;
								returnedValue = opt.orElse(null);
							}
							
							if (returnedValue == null) {
								return json2FloatDefault(objectDTO);
								
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
							
						}
						
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
					Integer min = objectDTO.getMin();
					Integer max = objectDTO.getMax();
					
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
	
	


	private <E> String float2Json(FieldData objectDTO) {
		if (objectDTO == null || objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;

		if (value != null && value.toString().trim().length() > 0) {
			Float valueToProcess = null;
			String valueToReturn = null;
			
			if (value instanceof Float) {
				valueToProcess = (Float)value;
			} else {
				try {
					valueToProcess = Float.valueOf(value.toString().trim());
				} catch (Exception ex) {}
			}
			
			if (valueToProcess != null) {
				try {
					Function function = objectDTO.getSerializer();
					if (function != null) {
						try {
							if (function instanceof Float2JsonFunction) {
								return ((Float2JsonFunction)function).apply(valueToProcess);
								
							} else {
								
								Object returnedValue = function.apply(valueToProcess);
							
								if (returnedValue == null) {
									return float2JsonDefault(objectDTO);
								} else {
									objectDTO.valueToProcess = returnedValue;
									return object2Json(objectDTO);
								}
								
							}
							
						} catch (Exception e) {}
					}

					if (valueToProcess != null) {
						Integer min = objectDTO.getMin();
						Integer max = objectDTO.getMax();
						
						if (min != null && min.floatValue() > valueToProcess) {
							valueToProcess = min.floatValue();
						}
						
						if (max != null && max.floatValue() < valueToProcess) {
							valueToProcess = max.floatValue();
						}
						
						return valueToProcess.toString();
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
		
	private <E> Float json2FloatDefault(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;
		
		Integer min = objectDTO.getMin();
		Integer max = objectDTO.getMax();
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


	private <E> BigDecimal json2BigDecimal(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			BigDecimal valueToReturn = null;
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						// suppose to return BigDecimal, but in case not, try to process
						if (function instanceof Json2BigDecimalFunction) {
							valueToReturn = ((Json2BigDecimalFunction)function).apply(valueToProcess);
						} else {
							
							Object returnedValue = function.apply(valueToProcess);

							if (returnedValue instanceof Optional) {
								Optional opt = (Optional)returnedValue;
								returnedValue = opt.orElse(null);
							}
							
							if (returnedValue == null) {
								return json2BigDecimalDefault(objectDTO);
								
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
							
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					valueToReturn = new BigDecimal(valueToProcess);
				}
				
				if (valueToReturn != null) {
					Integer min = objectDTO.getMin();
					Integer max = objectDTO.getMax();
					
					if (min != null && valueToReturn.compareTo(new BigDecimal(min)) < 0) {
						return new BigDecimal(min);
					}
					
					if (max != null && valueToReturn.compareTo(new BigDecimal(max)) > 0) {
						valueToReturn = new BigDecimal(max);
					}
					
					if (objectDTO.scale != null) {
						valueToReturn = valueToReturn.setScale(objectDTO.scale);
					}
					
					return valueToReturn;
				}
	
			} catch (Exception ex) {
				//ex.printStackTrace();
			}
		
		}
		
		return json2BigDecimalDefault(objectDTO);
	}
	
	


	private <E> String bigDecimal2Json(FieldData objectDTO) {
		if (objectDTO == null || objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;

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
							if (function instanceof BigDecimal2JsonFunction) {
								return ((BigDecimal2JsonFunction)function).apply(valueToProcess);
								
							} else {
								
								Object returnedValue = function.apply(valueToProcess);
							
								if (returnedValue == null) {
									return bigDecimal2JsonDefault(objectDTO);
								} else {
									objectDTO.valueToProcess = returnedValue;
									return object2Json(objectDTO);
								}
								
							}
							
						} catch (Exception e) {}
					}

					if (valueToProcess != null) {
						Integer min = objectDTO.getMin();
						Integer max = objectDTO.getMax();
						
						if (min != null && valueToProcess.compareTo(new BigDecimal(min)) < 0) {
							valueToProcess = new BigDecimal(min);
						}
						
						if (max != null && valueToProcess.compareTo(new BigDecimal(max)) > 0) {
							valueToProcess = new BigDecimal(max);
						}
						
						if (objectDTO.scale != null) {
							valueToProcess = valueToProcess.setScale(objectDTO.scale);
						}
						
						return valueToProcess.toString();
					}

				} catch (Exception ex) {
					//ex.printStackTrace();
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
		
	private <E> BigDecimal json2BigDecimalDefault(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;
		
		Integer min = objectDTO.getMin();
		Integer max = objectDTO.getMax();
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

	private <E> BigInteger json2BigInteger(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			BigInteger valueToReturn = null;
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						// suppose to return BigInteger, but in case not, try to process
						if (function instanceof Json2BigIntegerFunction) {
							valueToReturn = ((Json2BigIntegerFunction)function).apply(valueToProcess);
						} else {
							
							Object returnedValue = function.apply(valueToProcess);

							if (returnedValue instanceof Optional) {
								Optional opt = (Optional)returnedValue;
								returnedValue = opt.orElse(null);
							}
							
							if (returnedValue == null) {
								return json2BigIntegerDefault(objectDTO);
								
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
							
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					valueToReturn = new BigInteger(valueToProcess);
				}
				
				if (valueToReturn != null) {
					Integer min = objectDTO.getMin();
					Integer max = objectDTO.getMax();
					
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
	
	


	private <E> String bigInteger2Json(FieldData objectDTO) {
		if (objectDTO == null || objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;

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
							if (function instanceof BigInteger2JsonFunction) {
								return ((BigInteger2JsonFunction)function).apply(valueToProcess);
								
							} else {
								
								Object returnedValue = function.apply(valueToProcess);
							
								if (returnedValue == null) {
									return bigInteger2JsonDefault(objectDTO);
								} else {
									objectDTO.valueToProcess = returnedValue;
									return object2Json(objectDTO);
								}
								
							}
							
						} catch (Exception e) {}
					}

					if (valueToProcess != null) {
						Integer min = objectDTO.getMin();
						Integer max = objectDTO.getMax();
						
						if (min != null && min > valueToProcess.longValue()) {
							valueToProcess = BigInteger.valueOf(min);
						}
						
						if (max != null && valueToProcess.compareTo(BigInteger.valueOf(max)) > 0) {
							valueToProcess = BigInteger.valueOf(max);
						}
						
						return valueToProcess.toString();
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
		
	private <E> BigInteger json2BigIntegerDefault(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;
		
		Integer min = objectDTO.getMin();
		Integer max = objectDTO.getMax();
		
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

	private <E> AtomicInteger json2AtomicInteger(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			AtomicInteger valueToReturn = null;
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						// suppose to return AtomicInteger, but in case not, try to process
						if (function instanceof Json2AtomicIntegerFunction) {
							valueToReturn = ((Json2AtomicIntegerFunction)function).apply(valueToProcess);
						} else {
							
							Object returnedValue = function.apply(valueToProcess);

							if (returnedValue instanceof Optional) {
								Optional opt = (Optional)returnedValue;
								returnedValue = opt.orElse(null);
							}
							
							if (returnedValue == null) {
								return json2AtomicIntegerDefault(objectDTO);
								
							} else if (Number.class.isAssignableFrom(returnedValue.getClass()) || returnedValue.getClass().isPrimitive()) {
								
								if (returnedValue instanceof AtomicInteger) {
									valueToReturn = (AtomicInteger) returnedValue;
								} else if (returnedValue instanceof String) {
									valueToReturn = new AtomicInteger(Integer.parseInt((String) returnedValue));
									
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
								valueToReturn = new AtomicInteger(Integer.parseInt(returnedValue.toString()));
							}
							
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					valueToReturn = new AtomicInteger(Integer.parseInt(valueToProcess));
				}
				
				if (valueToReturn != null) {
					Integer min = objectDTO.getMin();
					Integer max = objectDTO.getMax();
					
					if (min != null && min > valueToReturn.intValue()) {
						return new AtomicInteger(min);
					}
		
					if (max != null && valueToReturn.intValue() > max ) {
						valueToReturn = new AtomicInteger(max);
					}
					
					return valueToReturn;
				}
	
			} catch (Exception ex) {
				//ex.printStackTrace();
			}
		
		}
		
		return json2AtomicIntegerDefault(objectDTO);
	}


	private <E> String atomicInteger2Json(FieldData objectDTO) {
		if (objectDTO == null || objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;

		if (value != null && value.toString().trim().length() > 0) {
			AtomicInteger valueToProcess = null;
			String valueToReturn = null;
			
			if (value instanceof AtomicInteger) {
				valueToProcess = (AtomicInteger)value;
			} else {
				try {
					valueToProcess = new AtomicInteger(Integer.parseInt(value.toString().trim()));
				} catch (Exception ex) {}
			}
			
			if (valueToProcess != null) {
				try {
					Function function = objectDTO.getSerializer();
					if (function != null) {
						try {
							if (function instanceof AtomicInteger2JsonFunction) {
								return ((AtomicInteger2JsonFunction)function).apply(valueToProcess);
								
							} else {
								
								Object returnedValue = function.apply(valueToProcess);
							
								if (returnedValue == null) {
									return atomicInteger2JsonDefault(objectDTO);
								} else {
									objectDTO.valueToProcess = returnedValue;
									return object2Json(objectDTO);
								}
								
							}
							
						} catch (Exception e) {}
					}

					if (valueToProcess != null) {
						Integer min = objectDTO.getMin();
						Integer max = objectDTO.getMax();
						
						if (min != null && min > valueToProcess.intValue()) {
							valueToProcess = new AtomicInteger(min);
						}
						
						if (max != null && max < valueToProcess.intValue()) {
							valueToProcess = new AtomicInteger(max);
						}
						
						return valueToProcess.toString();
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
		
	private <E> AtomicInteger json2AtomicIntegerDefault(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;
		
		Integer min = objectDTO.getMin();
		Integer max = objectDTO.getMax();
		
		if (getDefaultType() == JSON_INCLUDE.DEFAULT || required) {
			AtomicInteger defaultValue = (AtomicInteger)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				if (min != null && min > defaultValue.intValue()) {
					return new AtomicInteger(min);
				}

				if (max != null && max < defaultValue.intValue()) {
					return new AtomicInteger(max);
				}				
				
				return defaultValue;
			}

			if (min != null && min > DefaultValue.atomicInteger.intValue()) {
				return new AtomicInteger(min);
			}

			return DefaultValue.atomicInteger;
		}

		return null;
	}
	
	
	private <E> AtomicLong json2AtomicLong(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			AtomicLong valueToReturn = null;
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						// suppose to return AtomicLong, but in case not, try to process
						if (function instanceof Json2AtomicLongFunction) {
							valueToReturn = ((Json2AtomicLongFunction)function).apply(valueToProcess);
						} else {
							
							Object returnedValue = function.apply(valueToProcess);

							if (returnedValue instanceof Optional) {
								Optional opt = (Optional)returnedValue;
								returnedValue = opt.orElse(null);
							}
							
							if (returnedValue == null) {
								return json2AtomicLongDefault(objectDTO);
								
							} else if (Number.class.isAssignableFrom(returnedValue.getClass()) || returnedValue.getClass().isPrimitive()) {
								
								if (returnedValue instanceof AtomicLong) {
									valueToReturn = (AtomicLong) returnedValue;
								} else if (returnedValue instanceof String) {
									valueToReturn = new AtomicLong(Integer.parseInt((String) returnedValue));
									
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
								valueToReturn = new AtomicLong(Long.parseLong(returnedValue.toString()));
							}
							
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					valueToReturn = new AtomicLong(Long.parseLong(valueToProcess));
				}
				
				if (valueToReturn != null) {
					Integer min = objectDTO.getMin();
					Integer max = objectDTO.getMax();
					
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


	private <E> String atomicLong2Json(FieldData objectDTO) {
		if (objectDTO == null || objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;

		if (value != null && value.toString().trim().length() > 0) {
			AtomicLong valueToProcess = null;
			String valueToReturn = null;
			
			if (value instanceof AtomicLong) {
				valueToProcess = (AtomicLong)value;
			} else {
				try {
					valueToProcess = new AtomicLong(Long.parseLong(value.toString().trim()));
				} catch (Exception ex) {}
			}
			
			if (valueToProcess != null) {
				try {
					Function function = objectDTO.getSerializer();
					if (function != null) {
						try {
							if (function instanceof AtomicLong2JsonFunction) {
								return ((AtomicLong2JsonFunction)function).apply(valueToProcess);
								
							} else {
								
								Object returnedValue = function.apply(valueToProcess);
							
								if (returnedValue == null) {
									return atomicLong2JsonDefault(objectDTO);
								} else {
									objectDTO.valueToProcess = returnedValue;
									return object2Json(objectDTO);
								}
								
							}
							
						} catch (Exception e) {}
					}

					if (valueToProcess != null) {
						Integer min = objectDTO.getMin();
						Integer max = objectDTO.getMax();
						
						if (min != null && min > valueToProcess.longValue()) {
							valueToProcess = new AtomicLong(min);
						}
						
						if (max != null && max < valueToProcess.longValue()) {
							valueToProcess = new AtomicLong(max);
						}
						
						return valueToProcess.toString();
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
		
	private <E> AtomicLong json2AtomicLongDefault(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;
		
		Integer min = objectDTO.getMin();
		Integer max = objectDTO.getMax();
		
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
	
	
	private <E> String long2Json(FieldData objectDTO) {
		if (objectDTO == null || objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;

		if (value != null && value.toString().trim().length() > 0) {
			Long valueToProcess = null;
			
			if (value instanceof Long) {
				valueToProcess = (Long)value;
			} else {
				try {
					valueToProcess = Long.valueOf(value.toString().trim());
				} catch (Exception ex) {}
			}
			
			if (valueToProcess != null) {
				try {
					
					Function function = objectDTO.getSerializer();
					if (function != null) {
						if (function instanceof Long2JsonFunction) {
							return ((Long2JsonFunction)function).apply(valueToProcess);
							
						} else {
							
							Object returnedValue = function.apply(valueToProcess);
						
							if (returnedValue == null) {
								return integer2JsonDefault(objectDTO);
							} else {
								objectDTO.valueToProcess = returnedValue;
								return object2Json(objectDTO);
							}
							
						}

					}

					if (valueToProcess != null) {
						Integer min = objectDTO.getMin();
						Integer max = objectDTO.getMax();
						
						if (min != null && min > valueToProcess.longValue()) {
							valueToProcess = min.longValue();
						}
						
						if (max != null && valueToProcess.compareTo(Long.valueOf(max)) > 0) {
							valueToProcess = Long.valueOf(max);
						}
						
						return valueToProcess.toString();
					}

				} catch (Exception ex) {
					// ex.printStackTrace();
				}
			}
		}
		
		return long2JsonDefault(objectDTO);
	}
	

	private <E> Long json2Long(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;

		Long valueToReturn = null;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						// suppose to return Long, but in case not, try to process
						if (function instanceof Json2LongFunction) {
							valueToReturn = ((Json2LongFunction)function).apply(valueToProcess);
						} else {
							Object returnedValue = function.apply(valueToProcess);

							if (returnedValue instanceof Optional) {
								Optional opt = (Optional)returnedValue;
								returnedValue = opt.orElse(null);
							}
							
							if (returnedValue instanceof Optional) {
								Optional opt = (Optional)returnedValue;
								returnedValue = opt.orElse(null);
							}
							
							if (returnedValue == null) {
								return json2LongDefault(objectDTO);
								
								//  || returnedValue.getClass().isPrimitive()
							} else if (Number.class.isAssignableFrom(returnedValue.getClass())) {
								
								if (returnedValue instanceof Long) {
									valueToReturn = (Long) returnedValue;
									
								} else if (returnedValue instanceof String) {
									valueToReturn = Long.parseLong((String) returnedValue);
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
								valueToReturn = Long.parseLong(returnedValue.toString());
							}
						
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					valueToReturn = Long.parseLong(valueToProcess);
				}
				
				if (valueToReturn != null) {
					Integer min = objectDTO.getMin();
					Integer max = objectDTO.getMax();
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
	
	private <E> Long json2LongDefault(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;
		
		Integer min = objectDTO.getMin();
		Integer max = objectDTO.getMax();
		
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
	
	private <E> Integer json2Integer(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			Integer valueToReturn = null;
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						// suppose to return Integer, but in case not, try to process
						if (function instanceof Json2IntegerFunction) {
							valueToReturn = ((Json2IntegerFunction)function).apply(valueToProcess);
						} else {
							
							Object returnedValue = function.apply(valueToProcess);

							if (returnedValue instanceof Optional) {
								Optional opt = (Optional)returnedValue;
								returnedValue = opt.orElse(null);
							}
							
							if (returnedValue == null) {
								return json2IntegerDefault(objectDTO);
								
							} else if (Number.class.isAssignableFrom(returnedValue.getClass()) || returnedValue.getClass().isPrimitive()) {
								
								if (returnedValue instanceof Integer) {
									valueToReturn = (Integer) returnedValue;
								} else if (returnedValue instanceof String) {
									valueToReturn = Integer.parseInt((String) returnedValue);
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
								valueToReturn = Integer.parseInt(returnedValue.toString());
							}
							
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					valueToReturn = Integer.parseInt(valueToProcess);
				}
				
				if (valueToReturn != null) {
					Integer min = objectDTO.getMin();
					Integer max = objectDTO.getMax();
					if (min != null && min > valueToReturn.intValue()) {
						return min;
					}
					
					if (max != null && valueToReturn.compareTo(Integer.valueOf(max)) > 0) {
						valueToReturn = Integer.valueOf(max);
					}
					
					return valueToReturn;
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

		if (value != null && value.toString().trim().length() > 0) {
			Integer valueToProcess = null;
			String valueToReturn = null;
			
			if (value instanceof Integer) {
				valueToProcess = (Integer)value;
			} else {
				try {
					valueToProcess = Integer.valueOf(value.toString().trim());
				} catch (Exception ex) {}
			}
			
			if (valueToProcess != null) {
				try {
					Function function = objectDTO.getSerializer();
					if (function != null) {
						try {
							if (function instanceof Integer2JsonFunction) {
								return ((Integer2JsonFunction)function).apply(valueToProcess);
								
							} else {
								
								Object returnedValue = function.apply(valueToProcess);
							
								if (returnedValue == null) {
									return integer2JsonDefault(objectDTO);
								} else {
									objectDTO.valueToProcess = returnedValue;
									return object2Json(objectDTO);
								}
								
							}
							
						} catch (Exception e) {}
					}

					if (valueToProcess != null) {
						Integer min = objectDTO.getMin();
						Integer max = objectDTO.getMax();
						
						if (min != null && min > valueToProcess.intValue()) {
							valueToProcess = min;
						}
						
						if (max != null && valueToProcess.compareTo(Integer.valueOf(max)) > 0) {
							valueToProcess = Integer.valueOf(max);
						}
						
						return valueToProcess.toString();
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
		
	private <E> Integer json2IntegerDefault(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;
		
		Integer min = objectDTO.getMin();
		Integer max = objectDTO.getMax();
		boolean json2Java = objectDTO.json2Java;

		if (returnType == int.class
				|| getDefaultType() == JSON_INCLUDE.DEFAULT || required) {
			Integer defaultValue = (Integer)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				if (min != null && min > defaultValue.intValue()) {
					return min;
				}

				return defaultValue;
			}

			if (min != null && min > DefaultValue.integer) {
				return min;
			}

			return DefaultValue.integer;
		}

		return null;
	}

	private <E> Byte json2Byte(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			Byte valueToReturn = null;
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						// suppose to return Byte, but in case not, try to process
						if (function instanceof Json2ByteFunction) {
							valueToReturn = ((Json2ByteFunction)function).apply(valueToProcess);
						} else {
							
							Object returnedValue = function.apply(valueToProcess);

							if (returnedValue instanceof Optional) {
								Optional opt = (Optional)returnedValue;
								returnedValue = opt.orElse(null);
							}
							
							if (returnedValue == null) {
								return json2ByteDefault(objectDTO);
								
							} else if (Number.class.isAssignableFrom(returnedValue.getClass()) || returnedValue.getClass().isPrimitive()) {
								
								if (returnedValue instanceof Byte) {
									valueToReturn = (Byte) returnedValue;
								} else if (returnedValue instanceof String) {
									valueToReturn = Byte.parseByte((String) returnedValue);
									
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
								valueToReturn = Byte.parseByte(returnedValue.toString());
							}
							
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					long longValue = Long.parseLong(valueToProcess);
					
					if (longValue > Byte.MAX_VALUE) {
						valueToReturn = Byte.MAX_VALUE;
					} else {
						valueToReturn = (byte)longValue;
					}
					
					// valueToReturn = Byte.parseByte(valueToProcess);
				}
				
				if (valueToReturn != null) {
					Integer min = objectDTO.getMin();
					Integer max = objectDTO.getMax();
					
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

		if (value != null && value.toString().trim().length() > 0) {
			Byte valueToProcess = null;
			String valueToReturn = null;
			
			if (value instanceof Byte) {
				valueToProcess = (Byte)value;
			} else {
				try {
					valueToProcess = Byte.valueOf(value.toString().trim());
				} catch (Exception ex) {}
			}
			
			if (valueToProcess != null) {
				try {
					Function function = objectDTO.getSerializer();
					if (function != null) {
						try {
							if (function instanceof Byte2JsonFunction) {
								return ((Byte2JsonFunction)function).apply(valueToProcess);
								
							} else {
								
								Object returnedValue = function.apply(valueToProcess);
							
								if (returnedValue == null) {
									return byte2JsonDefault(objectDTO);
								} else {
									objectDTO.valueToProcess = returnedValue;
									return object2Json(objectDTO);
								}
								
							}
							
						} catch (Exception e) {}
					}

					if (valueToProcess != null) {
						Integer min = objectDTO.getMin();
						Integer max = objectDTO.getMax();
						
						if (min != null && min.byteValue() > valueToProcess) {
							valueToProcess = min.byteValue();
						}
						
						if (max != null && max.byteValue() < valueToProcess) {
							valueToProcess = max.byteValue();
						}
						
						return valueToProcess.toString();
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
		boolean required = objectDTO.required;
		
		Integer min = objectDTO.getMin();
		Integer max = objectDTO.getMax();
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
	

	private <E> Object getChar(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;
		
		boolean json2Java = objectDTO.json2Java;

		if (value != null && value.toString().trim().length() > 0) {
			Function function = null;
			String str = value.toString().trim();
			try {
				Character valueToReturn = null;
				if (value instanceof Character) {
					valueToReturn = (Character)value;
				} else {
					valueToReturn = str.charAt(0);
				}
				
				if (json2Java) {
					function = getDeserializer(objectDTO.getDefaultName(), returnType, objectDTO.getEnclosingType());
					if (function != null) {
						try {
							valueToReturn = (Character)function.apply(valueToReturn);
						} catch (Exception e) {}
					}
					
				} else {
					function = getSerializer(objectDTO.getDefaultName(), returnType, objectDTO.getEnclosingType());
					if (function != null) {
						try {
							return function.apply(valueToReturn);
						} catch (Exception e) {}
					}
				}

				return valueToReturn;
				
			} catch (Exception err) {
				if (function != null && !json2Java) {
					try {
						return function.apply(str);
					} catch (Exception e) {}
				}
			}
		}
		
		if (returnType == char.class
				|| getDefaultType() == JSON_INCLUDE.DEFAULT || required) {
			Character defaultValue = (Character)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				return defaultValue;
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
		boolean required = objectDTO.required;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			Short valueToReturn = null;
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						// suppose to return Short, but in case not, try to process
						if (function instanceof Json2ShortFunction) {
							valueToReturn = ((Json2ShortFunction)function).apply(valueToProcess);
						} else {
							
							Object returnedValue = function.apply(valueToProcess);

							if (returnedValue instanceof Optional) {
								Optional opt = (Optional)returnedValue;
								returnedValue = opt.orElse(null);
							}
							
							if (returnedValue == null) {
								return json2ShortDefault(objectDTO);
								
							} else if (Number.class.isAssignableFrom(returnedValue.getClass()) || returnedValue.getClass().isPrimitive()) {
								
								if (returnedValue instanceof Short) {
									valueToReturn = (Short) returnedValue;
								} else if (returnedValue instanceof String) {
									valueToReturn = Short.parseShort((String) returnedValue);
									
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
								valueToReturn = Short.parseShort(returnedValue.toString());
							}
							
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					long longValue = Long.parseLong(valueToProcess);
					
					if (longValue > Short.MAX_VALUE) {
						valueToReturn = Short.MAX_VALUE;
					} else {
						valueToReturn = (short)longValue;
					}
					
					// valueToReturn = Short.parseShort(valueToProcess);
				}
				
				if (valueToReturn != null) {
					Integer min = objectDTO.getMin();
					Integer max = objectDTO.getMax();
					
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

		if (value != null && value.toString().trim().length() > 0) {
			Short valueToProcess = null;
			String valueToReturn = null;
			
			if (value instanceof Short) {
				valueToProcess = (Short)value;
			} else {
				try {
					valueToProcess = Short.valueOf(value.toString().trim());
				} catch (Exception ex) {}
			}
			
			if (valueToProcess != null) {
				try {
					Function function = objectDTO.getSerializer();
					if (function != null) {
						try {
							if (function instanceof Short2JsonFunction) {
								return ((Short2JsonFunction)function).apply(valueToProcess);
								
							} else {
								
								Object returnedValue = function.apply(valueToProcess);
							
								if (returnedValue == null) {
									return short2JsonDefault(objectDTO);
								} else {
									objectDTO.valueToProcess = returnedValue;
									return object2Json(objectDTO);
								}
								
							}
							
						} catch (Exception e) {}
					}

					if (valueToProcess != null) {
						Integer min = objectDTO.getMin();
						Integer max = objectDTO.getMax();
						
						if (min != null && min.shortValue() > valueToProcess) {
							valueToProcess = min.shortValue();
						}
						
						if (max != null && max.shortValue() < valueToProcess) {
							valueToProcess = max.shortValue();
						}
						
						return valueToProcess.toString();
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
		boolean required = objectDTO.required;
		
		Integer min = objectDTO.getMin();
		Integer max = objectDTO.getMax();
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


	private <E> Object getString(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;
		String defaultValue = (String)objectDTO.getDefaultValue();
		Integer length = objectDTO.length; // not handled yet, some dummy string might be good for testing?
		boolean json2Java = objectDTO.json2Java;
		
		if (value == null || value.toString().trim().length() == 0 || value.toString().trim().equalsIgnoreCase("null")) {
			if (getDefaultType() == JSON_INCLUDE.DEFAULT || required) {

				if (defaultValue != null) {
					return defaultValue;
				}

				return DefaultValue.string;
			}

			return null;
		}
		
		String valueToReturn = null;
		if (value instanceof String) {
			valueToReturn = ((String)value).trim();
		} else {
			valueToReturn = value.toString().trim();
		}
		
		Function function = null;
		if (json2Java) {
			function = getDeserializer(objectDTO.getDefaultName(), returnType, objectDTO.getEnclosingType());
			if (function != null) {
				try {
					valueToReturn = (String)function.apply(valueToReturn);
				} catch (Exception e) {}
			}
			
		} else {
			function = getSerializer(objectDTO.getDefaultName(), returnType, objectDTO.getEnclosingType());
			if (function != null) {
				try {
					return function.apply(valueToReturn);
				} catch (Exception e) {}
			}
		}

		return valueToReturn;
	}


	private <E> Boolean json2Boolean(FieldData objectDTO) {
		if (objectDTO == null || !objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;

		if (value != null && value.toString().trim().length() > 0) {
			String valueToProcess = value.toString().trim();
			Boolean valueToReturn = null;
			
			try {
				Function function = objectDTO.getDeserializer();
				
				if (function != null) {
					try {
						// suppose to return Boolean, but in case not, try to process
						if (function instanceof Json2BooleanFunction) {
							valueToReturn = ((Json2BooleanFunction)function).apply(valueToProcess);
						} else {
							
							Object returnedValue = function.apply(valueToProcess);

							if (returnedValue instanceof Optional) {
								Optional opt = (Optional)returnedValue;
								returnedValue = opt.orElse(null);
							}
							
							if (returnedValue == null) {
								return json2BooleanDefault(objectDTO);
								
							} else if (returnedValue instanceof Boolean || returnedValue.getClass() == boolean.class) {
								valueToReturn = (Boolean) returnedValue;
								
							} else if (returnedValue instanceof Character || returnedValue.getClass() == char.class) {
								char c = (Character) returnedValue;
								
								if (c == 'f' || c == 'F' || c == '0' || c == '\0' || c == DefaultValue.character) {
									valueToReturn = DefaultValue.bool;
								} else {
									valueToReturn = true;
								}
								
							} else if (returnedValue instanceof String) {
								valueToReturn = string2Boolean((String)returnedValue);
								
							} else if (DefaultValue.isDefault(returnedValue)) {
								valueToReturn = false;
							} else {
								valueToReturn = string2Boolean(returnedValue.toString());
							}
							
						}
						
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					valueToReturn = string2Boolean(valueToProcess);
				}

				return valueToReturn;

			} catch (Exception ex) {
				//ex.printStackTrace();
			}
		
		}
		
		return json2BooleanDefault(objectDTO);
	}
	
	
	private Boolean string2Boolean(String str) {
		if (str == null) {
			return DefaultValue.bool;
		}
		
		str = str.trim().toLowerCase();
	
		if (str.equals("false") || str.equals("0") || str.equals("\0") || str.equals("f") ) {
			return false;
		}
	
		if (str.equals("true") || str.equals("1") || str.equals("t")) {
			return true;
		}
		
		try {
			return Boolean.parseBoolean(str);
		} catch (Exception ex) {}
	
		return DefaultValue.bool;
	}


	private <E> String boolean2Json(FieldData objectDTO) {
		if (objectDTO == null || objectDTO.json2Java) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;

		if (value != null && value.toString().trim().length() > 0) {
			Boolean valueToProcess = null;
			String valueToReturn = null;
			
			if (value instanceof Boolean) {
				valueToProcess = (Boolean)value;
			} else {
				try {
					valueToProcess = Boolean.valueOf(value.toString().trim());
				} catch (Exception ex) {}
			}
			
			if (valueToProcess != null) {
				try {
					Function function = objectDTO.getSerializer();
					if (function != null) {
						try {
							if (function instanceof Boolean2JsonFunction) {
								return ((Boolean2JsonFunction)function).apply(valueToProcess);
								
							} else {
								
								Object returnedValue = function.apply(valueToProcess);
							
								if (returnedValue == null) {
									return boolean2JsonDefault(objectDTO);
								} else {
									objectDTO.valueToProcess = returnedValue;
									return object2Json(objectDTO);
								}
								
							}
							
						} catch (Exception e) {}
					}

					if (valueToProcess != null) {
						return valueToProcess.toString();
					}

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
		boolean required = objectDTO.required;
		
		Integer min = objectDTO.getMin();
		Integer max = objectDTO.getMax();
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

	private <E> Object getDate(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;
		
		boolean json2Java = objectDTO.json2Java;
		
		if (value != null && value.toString().trim().length() > 0) {
			Function function = null;
			String str = value.toString().trim();
			try {
				Date valueToReturn = null;
				if (value instanceof Date) {
					valueToReturn = (Date)value;
				} else {
					DateFormat format = getDateFormat();
					
					valueToReturn = format.parse(value.toString());
				}
	
				// processing jsonFunction
				if (json2Java) {
					function = getDeserializer(objectDTO.getDefaultName(), returnType, objectDTO.getEnclosingType());
					if (function != null) {
						try {
							valueToReturn = (Date)function.apply(valueToReturn);
						} catch (Exception e) {}
					}
					
				} else {
					function = getSerializer(objectDTO.getDefaultName(), returnType, objectDTO.getEnclosingType());
					if (function != null) {
						try {
							return function.apply(valueToReturn);
						} catch (Exception e) {}
					}
				}
				
				return valueToReturn;
				
			} catch (Exception err) {
				if (function != null && !json2Java) {
					try {
						return function.apply(str);
					} catch (Exception e) {}
				}
				if (str.matches("\\d*")) {
					try {
						return new Date(Long.parseLong(value.toString()));
					} catch (NumberFormatException e) {}
				}
			}
		}
		
		if (getDefaultType() == JSON_INCLUDE.DEFAULT || required) {
			Date defaultValue = (Date)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				return defaultValue;
			}

			return DefaultValue.getDate();
		}

		return null;
	}


	// deserialize only
	private <E> Enum<?> json2Enum(FieldData objectDTO) {
		Object valueToProcess = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;
		Enum defaultValue = (Enum)objectDTO.defaultValue;
		boolean json2Java = objectDTO.json2Java;

		if (returnType == null || valueToProcess == null) {
			if (required) {
				return defaultValue;
			}
			
			return null;
		}

		String value = valueToProcess.toString().trim();

		Class<Enum> enumType = (Class<Enum>) returnType;

		try {
			Function function = objectDTO.getDeserializer();
			
			if (function != null) {
				Object valueToReturn = null;
				if (function instanceof Json2EnumFunction) {
					valueToReturn = ((Json2EnumFunction)function).apply(valueToProcess);
				} else {
					valueToReturn = function.apply(valueToProcess);
				}
				
				if (valueToReturn != null) {
					Class type = valueToReturn.getClass();
					if (Enum.class.isAssignableFrom(type)) {
						return (Enum<?>) valueToReturn;
						
					} else if (Number.class.isAssignableFrom(type)) {
						int ordinal = ((Number)valueToReturn).intValue();
						
						for (Enum enumValue : enumType.getEnumConstants()) {
							if (enumValue.ordinal() == ordinal) {
								return enumValue;
							}
						}
						
					} else {
						String name = valueToReturn.toString();
						
						for (Enum enumValue : enumType.getEnumConstants()) {
							if (enumValue.toString().equalsIgnoreCase(value)
									|| enumValue.name().equalsIgnoreCase(value)) {
								return enumValue;
							}
						}
					}
				}
			}
			
		} catch (Exception ex) {
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

		FieldData fieldData = new FieldData(value, int.class, true);
		fieldData.json2Java = true;
		Integer ordinal = json2Integer(fieldData);

		for (Enum enumValue : enumType.getEnumConstants()) {
			if (enumValue.ordinal() == ordinal) {
				return enumValue;
			}
		}

		return null;
	}


	private <E> Object getMap(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Map returnObj = (Map)objectDTO.returnObj;
		Class<Map> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;
		Map defaultValue = (Map)objectDTO.defaultValue;
		boolean json2Java = objectDTO.json2Java;

		if (value == null) {
			if (required) {
				if (returnObj != null) {
					return returnObj;
				}

				if (defaultValue != null) {
					return defaultValue;
				}

				returnObj = newInstance((Map<String, Object>)value, returnType);

				if (returnObj != null) {
					return returnObj;
				}

				return DefaultValue.map;
			}

			return null;
		}

		if (returnObj == null) {
			if (defaultValue != null) {
				returnObj = defaultValue;
			}

			if (returnObj == null) {
				returnObj = newInstance((Map<String, Object>)value, returnType);
			}

			if (returnObj == null) {
				returnObj = DefaultValue.map;
			}
		}

		Map<String, Object> values = (Map<String, Object>)value;

		if (values.size() == 0) {
			return returnObj;
		}
		
		Function function = null;
		if (json2Java) {
			function = getDeserializer(objectDTO.getDefaultName(), returnType, objectDTO.getEnclosingType());
			if (function != null) {
				try {
					return function.apply(values);
				} catch (Exception e) {}
			}
			
		} else {
			function = getSerializer(objectDTO.getDefaultName(), returnType, objectDTO.getEnclosingType());
			if (function != null) {
				try {
					return function.apply(values);
				} catch (Exception e) {}
			}
		}

		for (Entry<String, Object> entry: values.entrySet()) {
			Object obj = entry.getValue();
			FieldData newFieldData = new FieldData(obj, obj.getClass());
			newFieldData.json2Java = objectDTO.json2Java;
			returnObj.put(entry.getKey(), json2Object(newFieldData));
		}
		
		return returnObj;
	}


	private <E> Object getCollection(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Collection<E> returnObj = (Collection<E>)objectDTO.returnObj;
		Class<Collection<E>> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;
		Collection<E> defaultValue = (Collection<E>)objectDTO.defaultValue;
		Type erasedType = objectDTO.erasedType;
		boolean json2Java = objectDTO.json2Java;

		if (returnType == null) {
			if (erasedType != null) {
				returnType = ObjectUtil.getTypeClass(erasedType);
			}

			if (returnType == null && returnObj != null) {
				returnType = (Class<Collection<E>>) returnObj.getClass();
			}

			if (returnType == null) {
				returnType = (Class<Collection<E>>) DefaultValue.collection.getClass();
			}
		}
		
		if (value != null && value.toString().length() > 0) {	
			Collection<E> values = (Collection<E>) value;
	
			if (values.size() > 0) {
				Function function = null;
				if (json2Java) {
					function = getDeserializer(objectDTO.getDefaultName(), returnType, objectDTO.getEnclosingType());
					if (function != null) {
						try {
							return function.apply(values);
						} catch (Exception e) {}
					}
					
				} else {
					function = getSerializer(objectDTO.getDefaultName(), returnType, objectDTO.getEnclosingType());
					if (function != null) {
						try {
							return function.apply(values);
						} catch (Exception e) {}
					}
				}
				
				
				if (returnObj == null) {
					if (defaultValue != null) {
						returnObj = defaultValue;
					}
		
					if (returnObj == null) {
						returnObj = newInstance(new HashMap(), returnType);
					}
		
					if (returnObj == null) {
						returnObj = DefaultValue.collection;
					}
				}
				
				Class<E> componentType = null;
				if (objectDTO.erasedType != null) {
					componentType = ObjectUtil.getTypeComponentClass(objectDTO.erasedType);
				}
		
				if (componentType == null) {
					if (returnType == null && returnObj != null) {
						returnType = (Class<Collection<E>>) returnObj.getClass();
					}
		
					componentType = CollectionArrayTypeGuesser.guessElementType(values, returnType,  getJsonClassType());
				}
		
				for (E val : values) {
					FieldData newFieldData = new FieldData(val, componentType);
					newFieldData.json2Java = objectDTO.json2Java;
					returnObj.add(json2Object(newFieldData));
				}
				
				return returnObj;
			}
		}
		
		
		if (required) {
			if (returnObj != null) {
				return returnObj;
			}

			if (defaultValue != null) {
				return defaultValue;
			}

			returnObj = newInstance(new HashMap(), returnType);

			if (returnObj != null) {
				return returnObj;
			}

			return DefaultValue.collection;
		}

		return returnObj;
	}


	/*
	 * deserialize only
	 */
	private <E> Object getArray(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		E[] returnObj = (E[])objectDTO.returnObj;
		Class<E[]> returnType = objectDTO.returnType;
		boolean required = objectDTO.required;
		
		Type erasedType = objectDTO.erasedType;
		
		if (returnType == null) {
			if (erasedType != null) {
				returnType = ObjectUtil.getTypeClass(erasedType);
			}

			if (returnType == null && returnObj != null) {
				returnType = (Class<E[]>) returnObj.getClass();
			}

			if (returnType == null) {
				returnType = (Class<E[]>) DefaultValue.array.getClass();
			}
		}
		
		
		if (value != null && value.toString().length() > 0) {
			Collection<E> values = null;
			Class<?> valueType = value.getClass();
			if (Collection.class.isAssignableFrom(valueType)) {
				values = (Collection<E>) value;
			} else if (valueType.isArray()) {
				values = Arrays.asList((E[]) value);
			}
			
			Class<E> componentType = (Class<E>) returnType.getComponentType();
			
			if (objectDTO.erasedType != null) {
				componentType = ObjectUtil.getTypeComponentClass(objectDTO.erasedType);
			}
			
			if (values != null) {
				if (componentType == null) {
					componentType = CollectionArrayTypeGuesser.guessElementType(values, (Class<Collection<E>>)values.getClass(),  getJsonClassType());
				}
				
				if (componentType == null) componentType = (Class<E>) Object.class;
				
				if (values.size() > 0) {
					boolean json2Java = objectDTO.json2Java;
					Function function = null;
					if (json2Java) {
						function = getDeserializer(objectDTO.getDefaultName(), returnType, objectDTO.getEnclosingType());
						if (function != null) {
							try {
								return function.apply(values);
							} catch (Exception e) {}
						}
						
					} else {
						function = getSerializer(objectDTO.getDefaultName(), returnType, objectDTO.getEnclosingType());
						if (function != null) {
							try {
								return function.apply(values);
							} catch (Exception e) {}
						}
					}
					
					
					E[] arr = (E[]) Array.newInstance(componentType, values.size());
	
					int i = 0;
					for (Object val: values) {
						FieldData newFieldData = new FieldData(val, componentType);
						newFieldData.json2Java = objectDTO.json2Java;
						arr[i++] = json2Object(newFieldData);
					}
					
					return arr;
				}
			}
		}


		if (required) {
			if (returnObj != null) {
				return returnObj;
			}

			E[] defaultValue = (E[])objectDTO.defaultValue;
			if (defaultValue != null) {
				return defaultValue;
			}

			returnObj = (E[]) newInstance(new HashMap(), returnType);

			if (returnObj != null) {
				return returnObj;
			}

			return (E[]) DefaultValue.array;
		}

		return returnObj;
	}

	/*
	 * deserialize, convert Json value to Java data
	 */
	@SuppressWarnings("unchecked")
	private <E> E json2Object(FieldData objectDTO) {
		// String value, Class<E> returnType
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		if (returnType == null) {
			returnType = (Class<E>) value.getClass();
		}
		
		// first, get the class mapper
		if (objectDTO.mapper == null && objectDTO.classMapper == null) {
			objectDTO.classMapper = getGlobalizedClassMapper(returnType);
		}

		if (returnType == String.class) {
			return (E) getString(objectDTO);

		} else if (returnType == Character.class || returnType == char.class) {
			return (E) getChar(objectDTO);
			
		} else if (returnType == Boolean.class || returnType == boolean.class) {
			return (E) json2Boolean(objectDTO);
			
		} else if (Number.class.isAssignableFrom(returnType) || returnType.isPrimitive()) {
			
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
				
			} else {
				return null;
			}
			
		} else if (returnType.isEnum() || Enum.class.isAssignableFrom(returnType)) {
			return (E) json2Enum(objectDTO);
				
		} else if (returnType == Date.class || Date.class.isAssignableFrom(returnType)) {
			return (E) getDate(objectDTO);

		} else if (Collection.class.isAssignableFrom(returnType)) {
			return (E) getCollection(objectDTO);

		} else if (returnType.isArray()) {
			return (E) getArray(objectDTO);

		} else if (Map.class.isAssignableFrom(returnType)) {
			return (E) getMap(objectDTO);

		} else {
			E obj = (E)objectDTO.returnObj;
			Map<String, Object> mvalue = null;
			if (value != null && Map.class.isAssignableFrom(value.getClass())) {
				mvalue = (Map<String, Object>)value;
			}
			

			if (obj == null) {
				return (E) deserialize(mvalue, returnType);
				
			} else {
				return (E) deserialize(mvalue, returnType, obj);
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
					if (function instanceof Enum2JsonFunction) {
						return ((Enum2JsonFunction)function).apply(en);
						
					} else {
						
						Object returnedValue = function.apply(en);
					
						if (returnedValue == null) {
							//return null; // just ignore it, right?
						} else if (Enum.class.isAssignableFrom(returnedValue.getClass())) {
							en = (Enum) returnedValue;
							
						} else {
							objectDTO.valueToProcess = returnedValue;
							return object2Json(objectDTO);
						}
						
					}
					
				} catch (Exception e) {}
			}
		} catch (Exception ex) {}
		
		
		if (enumType == null) {
			return en.name();
		}

		switch (enumType) {
		case STRING:
			return en.name();
		case ORDINAL:
		default:
//			FieldData newFieldData = new FieldData(value, valueType, true);
//			newFieldData.json2Java = false;
//			Enum e = getEnum(newFieldData);
			return "" + en.ordinal();
		}
	}

	
	private String object2Json(FieldData objectDTO) {
		Object returnedValue = objectDTO.valueToProcess;

		if (returnedValue == null) {
			return null;
			
		} else if (returnedValue instanceof String) {
			String str = (String) returnedValue;
			if (objectDTO.jsonRawValue) {
				return str;
			} else if (objectDTO.doubleQuote) {
				return StringUtil.doublequote(str);
			} else {
				return str;
			}
		}
		
		Class type = returnedValue.getClass();
		
		if (type.isPrimitive()) {
			return returnedValue + "";
			
		} else if (Number.class.isAssignableFrom(type)) {
			return String.valueOf(returnedValue);
			
		} else if (Enum.class.isAssignableFrom(type)) {
			return enum2Json(objectDTO);
			
		} else if (returnedValue instanceof Character) {
			String str = ((Character)returnedValue).toString();
			if (objectDTO.jsonRawValue) {
				return str;
			} else if (objectDTO.doubleQuote) {
				return StringUtil.doublequote(str);
			} else {
				return str;
			}
			
		} else if (returnedValue instanceof Boolean) {
			return ((Boolean) returnedValue).toString();
			
		} else {
			
			// might lead to endless loop, so limit it here, by reducing it maxlevel by 1
			int maxlevel = getLevel();
			if (maxlevel > 3) {
				this.setLevel(maxlevel - 1);
			}
			
			if (Enum.class.isAssignableFrom(returnedValue.getClass())) {
				return enum2Json(objectDTO);
			
			} else if (Date.class.isAssignableFrom(returnedValue.getClass())) {

				//return date2Json(objectDTO);
				
				return returnedValue.toString();
				
			} else {
				return serialize(objectDTO, objectDTO.level, objectDTO.set);
			}
		}
	}
	
	
	private <E> String object2Json(FieldData objectDTO, int level, Set set) {
		if (objectDTO == null) {
			return null;
		}
		
		Object value = objectDTO.valueToProcess;
		Class<?> returnType = objectDTO.returnType;
		
		if (returnType == null) {
			returnType = value.getClass();
		}
		
		// first, get the class mapper
		if (objectDTO.mapper == null && objectDTO.classMapper == null) {
			objectDTO.classMapper = getGlobalizedClassMapper(returnType);
		}

		boolean required = objectDTO.required;
		
		if (level == 0) {
			objectDTO.jsonRawValue = true;
		}
		boolean jsonRawValue = objectDTO.jsonRawValue;
		if (objectDTO.defaultType == null) {
			objectDTO.defaultType = getDefaultType();
		}

		if (returnType == null) {
			if (value == null) {
				return null;
			} else if (jsonRawValue || level == 0) {
				return value.toString();
			} else {
				return "\"" + StringUtil.escapeDoublequote(value.toString())
						+ "\"";
			}

		} else if (returnType == String.class) {
			value = getString(objectDTO);

			if (value == null) {
				return null;
			}
			
			if (!required && (objectDTO.defaultType == JSON_INCLUDE.NON_DEFAULT || objectDTO.defaultType == JSON_INCLUDE.NON_EMPTY)) {
				if (value.equals(DefaultValue.string)) {
					return null;
				}
			}
			
			if (jsonRawValue) {
				return value.toString();
			} else {
				return StringUtil.doublequote(value.toString());
			}

			
			
		} else if (Character.class.isAssignableFrom(returnType) || char.class.isAssignableFrom(returnType)) {
			Object valueToReturn = getChar(objectDTO);

			if (valueToReturn == null) {
				return null;
			}

			if (valueToReturn instanceof String) {
				if (jsonRawValue || level == 0) {
					return (String) valueToReturn;
				} else {
					return "\"" + StringUtil.escapeDoublequote(valueToReturn) + "\"";
				}
			}
			
			if (!required && objectDTO.defaultType == JSON_INCLUDE.NON_DEFAULT) {
				if (valueToReturn.equals(DefaultValue.character)) {
					return null;
				}
			}
			
			return "\"" + StringUtil.escapeDoublequote(valueToReturn.toString()) + "\"";

		} else if (returnType == Boolean.class || returnType == boolean.class) {
			String returnedValue = boolean2Json(objectDTO);
			
			if (returnedValue != null) {
				if (objectDTO.jsonRawValue) {
					return returnedValue;
					
				} else if ("true".equalsIgnoreCase(returnedValue)) {
					return "true";
					
				} else if ("false".equalsIgnoreCase(returnedValue)) {
					return "false";
					
				} else if (StringUtil.isNumeric(returnedValue)) {
					return returnedValue;
					
				} else {
					return StringUtil.doublequote(returnedValue);
				}
			}
			
			return null;
			
			
		} else if (Number.class.isAssignableFrom(returnType) || returnType.isPrimitive()) {
			
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
				
			} else {
				return null;
			}
			
			
			if (returnedValue != null) {
				if (objectDTO.jsonRawValue) {
					return returnedValue;
					
				} else if (StringUtil.isNumeric(returnedValue)) {
					return returnedValue;
					
				} else {
					return StringUtil.doublequote(returnedValue);
				}
			}
			
			return null;

		} else if (Date.class.isAssignableFrom(returnType)) {
			Object valueToReturn = getDate(objectDTO);
			
			if (valueToReturn == null) {
				return null;
			}
			
			if (valueToReturn instanceof String) {
				if (jsonRawValue || level == 0) {
					return (String) valueToReturn;
				} else {
					return "\"" + StringUtil.escapeDoublequote(valueToReturn) + "\"";
				}
			}

			try {
				DateFormat format = getDateFormat();

				if (jsonRawValue || level == 0) {
					return format.format((Date)valueToReturn);
				} else {
					return "\"" + format.format((Date)valueToReturn) + "\"";
				}
				
			} catch (Exception err) {
				return null;
			}

		// returnType.isEnum()  || value instanceof Enum<?>
		} else if (Enum.class.isAssignableFrom(returnType)) {
			return enum2Json(objectDTO);

		} else if (Collection.class.isAssignableFrom(returnType)) {
			if (value == null) {
				return null;
			}

			Collection collection = (Collection) value;

			Class componentType = CollectionArrayTypeGuesser
					.guessElementType(collection, (Class<Collection<E>>) returnType, getJsonClassType());

			String repeated = getPrettyIndentationln(level);
			String repeatedItem = getPrettyIndentationln(++level);
			StringBuilder sbuilder = new StringBuilder();
			try {
				for (Object s : collection) {
					FieldData newFieldData = new FieldData(s, componentType);
					newFieldData.json2Java = objectDTO.json2Java;
					
					String str = object2Json(newFieldData, level, set);
					if (str != null && str.length() > 0) {
						sbuilder.append(repeatedItem + str + ",");
					}
				}
			} catch (Exception ex) {}

			String str = sbuilder.toString();
			int size = str.length();
			if (size == 0) {
				switch (objectDTO.defaultType) {
				case ALWAYS:
					return "[]";
				case NON_NULL:
					return "[]";
				case NON_EMPTY:
					return null;
				case DEFAULT:
					return "[]";
				default:
					return "[]";
				}
			} else {
				return "[" + str.substring(0, size - 1) + repeated + "]";
			}

		} else if (returnType.isArray()) {
			if (value == null) {
				return null;
			}

			Class<?> componentType = returnType.getComponentType(); // CollectionArrayTypeGuesser.getBaseType(returnType);

			int size = Array.getLength(value);

			
			String repeated = getPrettyIndentationln(level);
			String repeatedItem = getPrettyIndentationln(++level);
			StringBuilder sbuilder = new StringBuilder();
			for (int i = 0; i < size; i++) {
				Object componentValue = Array.get(value, i);
				
				FieldData newFieldData = new FieldData(componentValue, componentType);
				newFieldData.json2Java = objectDTO.json2Java;
				String str = object2Json(newFieldData, level, set);
				if (str != null && str.length() > 0) {
					sbuilder.append(repeatedItem + str + ",");
				}
			}

			String str = sbuilder.toString();
			size = str.length();
			if (size == 0) {
				switch (objectDTO.defaultType) {
				case ALWAYS:
					return "[]";
				case NON_NULL:
					return "[]";
				case NON_EMPTY:
					return null;
				case DEFAULT:
					return "[]";
				default:
					return "[]";
				}

			} else {
				return "[" + str.substring(0, size - 1) + repeated + "]";
			}

		} else if (Map.class.isAssignableFrom(returnType)) {
			if (value == null) {
				return null;
			}
			
			Class valueType = value.getClass();
			if (!Map.class.isAssignableFrom(valueType)) {
				return null;
			}

			Map<String, Object> map = (Map) value;

			StringBuilder sbuilder = new StringBuilder();

			String repeated = getPrettyIndentationln(level), pretty = getPrettySpace();
			String repeatedItem = getPrettyIndentationln(++level);

			//for (Map.Entry<Object, ?> entry : map.entrySet()) {
			Set<String> names = map.keySet();
			try {
				if (getOrderByKeyAndProperties()) {//LinkedHashSet
					names = new TreeSet(names);
				}

				for (String name : names) {
					Object v = map.get(name);

					if (name != null) {
						sbuilder.append(repeatedItem + "\"" + name + "\":" + pretty);
						
						FieldData newFieldData = new FieldData(v, v.getClass());
						newFieldData.json2Java = objectDTO.json2Java;
						sbuilder.append(object2Json(newFieldData, level, set));
						sbuilder.append(",");
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			String str = sbuilder.toString();
			int size = str.length();
			if (size == 0) {
				return "{}";
			} else {
				return "{" + str.substring(0, size - 1) + repeated + "}";
			}

		} else if (level < getLevel()) {
			if (value == null) {
				return null;
			}

			return serialize(objectDTO, level, set);
			
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
	private <T> Map <String, Method> getSetters(Class<T> valueType) {
		String fullName = valueType.getName();
		if (!cachedMethods.containsKey(fullName)) {
			processMethods(valueType, fullName);
		}
		
		// return a new copy, so original copy will not get modified
		return new HashMap(cachedMethods.get(fullName)[METHOD.SET.value]);
	}
	private <T> Map <String, Method> getGetters(T obj) {
		if (obj == null) {
			return null;
		}
		Class<T> valueType = (Class<T>) obj.getClass();

		return getGetters(valueType);
	}
	private <T> Map <String, Method> getGetters(Class<T> valueType) {
		String fullName = valueType.getName();
		if (!cachedMethods.containsKey(fullName)) {
			processMethods(valueType, fullName);
		}
		
		return new HashMap(cachedMethods.get(fullName)[METHOD.GET.value]);
	}
	private <T> Map <String, Method> getOtherMethods(T obj) {
		if (obj == null) {
			return null;
		}
		Class<T> valueType = (Class<T>) obj.getClass();

		return getOtherMethods(valueType);
	}
	private <T> Map <String, Method> getOtherMethods(Class<T> valueType) {
		String fullName = valueType.getName();
		if (!cachedMethods.containsKey(fullName)) {
			processMethods(valueType, fullName);
		}
		
		return new HashMap(cachedMethods.get(fullName)[METHOD.OTHER.value]);
	}
	
	private static <T> void processMethods(Class<T> valueType, String fullName) {
		Stream<Method> stream = Arrays.stream(valueType.getDeclaredMethods());
//		while (valueType != null && valueType != Object.class) {
//			stream = Stream.concat(stream, Arrays.stream(valueType
//					.getSuperclass().getDeclaredMethods()));
//			valueType = (Class<T>) valueType.getSuperclass();
//		}

		Map <String, Method> setters = new HashMap<>();
		Map <String, Method> getters = new HashMap<>();
		Map <String, Method> others = new HashMap<>();
		
		String name;
		for (Method method: stream.collect(Collectors.toList())) {
			name = method.getName();
			
			if (name.startsWith("set")) {
				if (name.length() > 3 && method.getParameterCount() == 1) {
					setters.put(name.substring(3).toLowerCase(), method);
				} else {
					others.put(name.toLowerCase(), method);
				}
				
			} else if (name.startsWith("get")) {
				if (name.length() > 3 && method.getParameterCount() == 0) {
					getters.put(name.substring(3).toLowerCase(), method);
				} else {
					others.put(name.toLowerCase(), method);
				}
				
			} else if (method.getParameterCount() == 0) {
				getters.put(name.toLowerCase(), method);
			} else {
				others.put(name.toLowerCase(), method);
			}
		}
		
		Map <String, Method>[] all = new HashMap[3];
		
		all[METHOD.GET.value] = getters;
		all[METHOD.SET.value] = setters;
		all[METHOD.OTHER.value] = others;
		cachedMethods.put(fullName, all);
	}
	
	
	private static <T> Field[] getFields(T obj) {
		Class<T> valueType = (Class<T>) obj.getClass();
		// try {
		// valueType = (Class<T>)Class.forName(obj.getClass().getName());
		// } catch (ClassNotFoundException e) {
		// e.printStackTrace();
		// }

		return getFields(valueType);
	}

	private static <T> Field[] getFields(Class<T> valueType) {
		if (cachedFields.containsKey(valueType)) {
			return cachedFields.get(valueType);
		}

		Class<T> valueTypeAll = valueType;
		Stream<Field> stream = Arrays.stream(valueTypeAll.getDeclaredFields());
		while (valueTypeAll != null && valueTypeAll != Object.class) {
			stream = Stream.concat(stream, Arrays.stream(valueTypeAll
					.getSuperclass().getDeclaredFields()));
			valueTypeAll = (Class<T>) valueTypeAll.getSuperclass();
		}

		//(Field[]) stream.distinct().toArray(size -> new Field[size])
		List<Field> uniqueFields = new ArrayList<>();
		Set<String> set = new HashSet<>();
		for (Field field: stream.collect(Collectors.toList())) {
			String name = field.getName().toLowerCase();
			if (!set.contains(name)) {
				set.add(name);
				uniqueFields.add(field);
			}
		}

		cachedFields.put(valueType, uniqueFields.toArray(new Field[uniqueFields.size()]));

		return cachedFields.get(valueType);
	}


	private ClassMapper overwriteBy (ClassMapper classMapper, ca.oson.json.ClassMapper classMapperAnnotation) {
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

		if (classMapperAnnotation.useField() != BOOLEAN.NONE) {
			classMapper.useField = classMapperAnnotation.useField().value();
		}
		
		if (classMapperAnnotation.useAttribute() != BOOLEAN.NONE) {
			classMapper.useAttribute = classMapperAnnotation.useAttribute().value();
		}
		
		if (classMapperAnnotation.ignoreVersionsAfter() > 0) {
			classMapper.ignoreVersionsAfter = classMapperAnnotation.ignoreVersionsAfter();
		}
		
		String defaultValue = classMapperAnnotation.defaultValue();
		if (defaultValue != null && defaultValue.length() > 0) {
			classMapper.defaultValue = defaultValue;
		}
		
		String[] ignoreFieldsWithAnnotations = classMapperAnnotation.ignoreFieldsWithAnnotations();
		if (ignoreFieldsWithAnnotations != null && ignoreFieldsWithAnnotations.length > 0) {
			classMapper.ignoreFieldsWithAnnotations = new HashSet(Arrays.asList(ignoreFieldsWithAnnotations));
		}
		
		String[] jsonIgnoreProps = classMapperAnnotation.jsonIgnoreProperties();
		if (jsonIgnoreProps != null && jsonIgnoreProps.length > 0) {
			classMapper.jsonIgnoreProperties = new HashSet(Arrays.asList(ignoreFieldsWithAnnotations));
		}

		String simpleDateFormat = classMapperAnnotation.simpleDateFormat();
		if (simpleDateFormat != null && simpleDateFormat.length() > 0) {
			classMapper.simpleDateFormat = simpleDateFormat;
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

		return classMapper;
	}
	
	private static ClassMapper overwriteBy (ClassMapper classMapper, ClassMapper javaClassMapper) {
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
		
		if (javaClassMapper.useAttribute != null) {
			classMapper.useAttribute = javaClassMapper.useAttribute;
		}
		
		if (javaClassMapper.useField != null) {
			classMapper.useField = javaClassMapper.useField;
		}
		
		if (javaClassMapper.constructor != null) {
			classMapper.constructor = javaClassMapper.constructor;
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
		
		if (javaClassMapper.ignoreVersionsAfter != null) {
			classMapper.ignoreVersionsAfter = javaClassMapper.ignoreVersionsAfter;
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
		
		if (javaClassMapper.simpleDateFormat != null) {
			classMapper.simpleDateFormat = javaClassMapper.simpleDateFormat;
		}
		
		return classMapper;
	}
	
	
	private FieldMapper overwriteBy (FieldMapper mapper, ca.oson.json.FieldMapper fieldMapperAnnotation, ClassMapper classMapper) {
		if (fieldMapperAnnotation.length() > 0) {
			mapper.length = fieldMapperAnnotation.length();
		}
		
		if (fieldMapperAnnotation.min() > 0) {
			mapper.min = fieldMapperAnnotation.min();
		}
		
		if (fieldMapperAnnotation.max() > 0) {
			mapper.max = fieldMapperAnnotation.max();
		}
		if (fieldMapperAnnotation.scale() > 0) {
			mapper.scale = fieldMapperAnnotation.scale();
		}
		if (fieldMapperAnnotation.precision() > 0) {
			mapper.precision = fieldMapperAnnotation.precision();
		}
		
		if (fieldMapperAnnotation.ignore() != BOOLEAN.NONE) {
			mapper.ignore = fieldMapperAnnotation.ignore().value();
		}
		
		if (fieldMapperAnnotation.jsonRawValue() != BOOLEAN.NONE) {
			mapper.jsonRawValue = fieldMapperAnnotation.jsonRawValue().value();
		}
		
		if (fieldMapperAnnotation.jsonValue() != BOOLEAN.NONE) {
			mapper.jsonValue = fieldMapperAnnotation.jsonValue().value();
		}
		
		if (fieldMapperAnnotation.jsonAnySetter() != BOOLEAN.NONE) {
			mapper.jsonAnySetter = fieldMapperAnnotation.jsonAnySetter().value();
		}
		
		if (fieldMapperAnnotation.jsonAnyGetter() != BOOLEAN.NONE) {
			mapper.jsonAnyGetter = fieldMapperAnnotation.jsonAnyGetter().value();
		}
		
		if (fieldMapperAnnotation.useField() != BOOLEAN.NONE) {
			mapper.useField = fieldMapperAnnotation.useField().value();
		}
		
		if (fieldMapperAnnotation.useAttribute() != BOOLEAN.NONE) {
			mapper.useAttribute = fieldMapperAnnotation.useAttribute().value();
		}
		
		if (fieldMapperAnnotation.defaultType() != JSON_INCLUDE.NONE) {
			mapper.defaultType = fieldMapperAnnotation.defaultType();
		}
		
		String defaultValue = fieldMapperAnnotation.defaultValue();
		if (defaultValue != null && defaultValue.length() > 0) {
			mapper.defaultValue = defaultValue;
		}
		
		if (fieldMapperAnnotation.required() != BOOLEAN.NONE) {
			mapper.required = fieldMapperAnnotation.required().value();
		}

		if (fieldMapperAnnotation.enumType() != ENUM_TYPE.NONE) {
			mapper.enumType = fieldMapperAnnotation.enumType().value();
		}
		
		if (fieldMapperAnnotation.date2Long() != BOOLEAN.NONE) {
			mapper.date2Long = fieldMapperAnnotation.date2Long().value();
		}
		
		String simpleDateFormat = fieldMapperAnnotation.simpleDateFormat();
		if (simpleDateFormat != null && simpleDateFormat.length() > 0) {
			mapper.simpleDateFormat = simpleDateFormat;
		}

		if (fieldMapperAnnotation.ignoreVersionsAfter() > classMapper.ignoreVersionsAfter) {
			mapper.ignore = true;
		}
		
		if (fieldMapperAnnotation.name() != null && fieldMapperAnnotation.name().length() > 0) {
			mapper.json = fieldMapperAnnotation.name();
		}
		
		return mapper;
	}
	
	
	
	private FieldMapper overwriteBy (FieldMapper mapper, FieldMapper javaFieldMapper) {
		if (mapper == null || javaFieldMapper == null) {
			return mapper;
		}
		
		if (javaFieldMapper.date2Long != null) {
			mapper.date2Long = javaFieldMapper.date2Long;
		}
		
		if (javaFieldMapper.ignore != null) {
			mapper.ignore = javaFieldMapper.ignore;
		}
		
		if (javaFieldMapper.jsonAnyGetter != null) {
			mapper.jsonAnyGetter = javaFieldMapper.jsonAnyGetter;
		}
		
		if (javaFieldMapper.jsonAnySetter != null) {
			mapper.jsonAnySetter = javaFieldMapper.jsonAnySetter;
		}
		
		if (javaFieldMapper.jsonRawValue != null) {
			mapper.jsonRawValue = javaFieldMapper.jsonRawValue;
		}
		
		if (javaFieldMapper.jsonValue != null) {
			mapper.jsonValue = javaFieldMapper.jsonValue;
		}
		
		if (javaFieldMapper.required != null) {
			mapper.required = javaFieldMapper.required;
		}
		
		if (javaFieldMapper.useAttribute != null) {
			mapper.useAttribute = javaFieldMapper.useAttribute;
		}
		
		if (javaFieldMapper.useField != null) {
			mapper.useField = javaFieldMapper.useField;
		}
		
		if (javaFieldMapper.defaultType != JSON_INCLUDE.NONE) {
			mapper.defaultType = javaFieldMapper.defaultType;
		}
		
		if (javaFieldMapper.defaultValue != null) {
			mapper.defaultValue = javaFieldMapper.defaultValue;
		}
		
		if (javaFieldMapper.deserializer != null) {
			mapper.deserializer = javaFieldMapper.deserializer;
		}
		
		if (javaFieldMapper.enumType != null) {
			mapper.enumType = javaFieldMapper.enumType;
		}
		
		mapper.java = javaFieldMapper.java;

		mapper.json = javaFieldMapper.json;
		
		if (javaFieldMapper.length != null) {
			mapper.length = javaFieldMapper.length;
		}
		
		if (javaFieldMapper.max != null) {
			mapper.max = javaFieldMapper.max;
		}
		
		if (javaFieldMapper.min != null) {
			mapper.min = javaFieldMapper.min;
		}
		
		if (javaFieldMapper.scale != null) {
			mapper.scale = javaFieldMapper.scale;
		}
		
		if (javaFieldMapper.precision != null) {
			mapper.precision = javaFieldMapper.precision;
		}
		
		if (javaFieldMapper.serializer != null) {
			mapper.serializer = javaFieldMapper.serializer;
		}
		
		if (javaFieldMapper.simpleDateFormat != null) {
			mapper.simpleDateFormat = javaFieldMapper.simpleDateFormat;
		}
		
		return mapper;
	}
	
	
	
	/*
	 * Object to string, serialize.
	 * 
	 * It involves 10 steps to apply processing rules:
	 * 1. Create a blank class mapper instance;
	 * 2. Globalize it;
	 * 3. Apply annotations from other sources;
	 * 4. Apply annotations from Oson;
	 * 5. Apply Java configuration for this particular class;
	 * 6. Create a blank field mapper instance;
	 * 7. Classify this field mapper;
	 * 8. Apply annotations from other sources;
	 * 9. Apply annotations from Oson;
	 * 10. Apply Java configuration for this particular field.
	 */
	private <T> String serialize(FieldData objectDTO , int level, Set set) {
		Object obj = objectDTO.valueToProcess;
		Class<T> valueType = objectDTO.returnType;
		
		if (obj == null) {
			return "";
		}

		int hash = ObjectUtil.hashCode(obj);
		if (set.contains(hash)) {
			return "{}";
		} else {
			set.add(ObjectUtil.hashCode(obj));
		}
		
		// first build up the class-level processing rules
		
		// 1. Create a blank class mapper instance
		ClassMapper classMapper = new ClassMapper(valueType);
		
		// 2. Globalize it
		classMapper = globalize(classMapper);
		
		
		FIELD_NAMING format = getFieldNaming();

		String repeated = getPrettyIndentationln(level), pretty = getPrettySpace();
		String repeatedItem = getPrettyIndentationln(++level);

		boolean annotationSupport = getAnnotationSupport();
		Annotation[] annotations = null;

		if (annotationSupport) {
			annotations = valueType.getAnnotations();
			
			ca.oson.json.ClassMapper classMapperAnnotation = null;
			
			// 3. Apply annotations from other sources
			for (Annotation annotation : annotations) {
				if (ignoreClass(annotation)) {
					return "";
				}

				switch (annotation.annotationType().getName()) {
				case "ca.oson.json.ClassMapper":
					classMapperAnnotation = (ca.oson.json.ClassMapper) annotation;					
					break;

				case "com.fasterxml.jackson.annotation.JsonIgnoreProperties":
					JsonIgnoreProperties jsonIgnoreProperties = (JsonIgnoreProperties) annotation;
					String[] jsonnames = jsonIgnoreProperties.value();
					if (jsonnames != null && jsonnames.length > 0) {
						if (classMapper.jsonIgnoreProperties == null) {
							classMapper.jsonIgnoreProperties = new HashSet();
						}
						
						classMapper.jsonIgnoreProperties.addAll(Arrays.asList(jsonnames));
					}
					break;
					
				case "org.codehaus.jackson.annotate.JsonIgnoreProperties":
					org.codehaus.jackson.annotate.JsonIgnoreProperties jsonIgnoreProperties2 = (org.codehaus.jackson.annotate.JsonIgnoreProperties) annotation;
					String[] jsonnames2 = jsonIgnoreProperties2.value();
					if (jsonnames2 != null && jsonnames2.length > 0) {
						if (classMapper.jsonIgnoreProperties == null) {
							classMapper.jsonIgnoreProperties = new HashSet();
						}
						
						classMapper.jsonIgnoreProperties.addAll(Arrays.asList(jsonnames2));
					}
					break;
					
				case "com.fasterxml.jackson.annotation.JsonPropertyOrder":
					// first come first serve
					if (classMapper.propertyOrders == null) {
						classMapper.propertyOrders = ((JsonPropertyOrder) annotation).value();
					}
					break;
					
				case "org.codehaus.jackson.annotate.JsonPropertyOrder":
					// first come first serve
					if (classMapper.propertyOrders == null) {
						classMapper.propertyOrders = ((org.codehaus.jackson.annotate.JsonPropertyOrder) annotation).value();
					}
					break;
					
				case "com.fasterxml.jackson.annotation.JsonInclude":
					if (classMapper.defaultType == JSON_INCLUDE.NONE) {
						JsonInclude jsonInclude = (JsonInclude) annotation;
						switch (jsonInclude.content()) {
						case ALWAYS:
							classMapper.defaultType = JSON_INCLUDE.ALWAYS;
							break;
						case NON_NULL:
							classMapper.defaultType = JSON_INCLUDE.NON_NULL;
							break;
						case NON_ABSENT:
							classMapper.defaultType = JSON_INCLUDE.NON_NULL;
							break;
						case NON_EMPTY:
							classMapper.defaultType = JSON_INCLUDE.NON_EMPTY;
							break;
						case NON_DEFAULT:
							classMapper.defaultType = JSON_INCLUDE.NON_DEFAULT;
							break;
						case USE_DEFAULTS:
							classMapper.defaultType = JSON_INCLUDE.DEFAULT;
							break;
						}
					}
					break;	
					
				case "com.fasterxml.jackson.annotation.JsonAutoDetect":
					JsonAutoDetect jsonAutoDetect = (JsonAutoDetect) annotation;
					if (jsonAutoDetect.fieldVisibility() == Visibility.NONE) {
						classMapper.useField = false;
					}
					if (jsonAutoDetect.getterVisibility() == Visibility.NONE) {
						classMapper.useAttribute = false;
					}
					break;
					
				case "org.codehaus.jackson.annotate.JsonAutoDetect":
					org.codehaus.jackson.annotate.JsonAutoDetect jsonAutoDetect2 = (org.codehaus.jackson.annotate.JsonAutoDetect) annotation;
					if (jsonAutoDetect2.fieldVisibility() == org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.NONE) {
						classMapper.useField = false;
					}
					if (jsonAutoDetect2.getterVisibility() == org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.NONE) {
						classMapper.useAttribute = false;
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
		
		if (classMapper.ignore != null && classMapper.ignore) {
			return "";
		}
		
		Function function = classMapper.serializer; //getSerializer(valueType);
		if (function != null) {
			try {
				if (function instanceof ClassData2JsonFunction) {
					ClassData classData = new ClassData(valueType, obj, classMapper, level);
					
					return ((ClassData2JsonFunction)function).apply(classData);
					
				} else {
					Object returnValue = function.apply(obj);
					
					if (returnValue != null) {
						Class returnType = returnValue.getClass();
						
						if (returnType == String.class) {
							return (String)returnValue;
							
						} else if (returnType == valueType || valueType.isAssignableFrom(returnType)) {
							// just continue to do the serializing
						} else {
							FieldData fieldData = new FieldData(returnValue, returnType, false);
							fieldData.level = level;
							fieldData.set = set;
							if (level == 0) {
								fieldData.jsonRawValue = true;
							}
							fieldData.doubleQuote = true;
							
							return object2Json(fieldData);
						}
					}
				}
				
			} catch (Exception e) {}
		}
		


		Set<Class> ignoreFieldsWithAnnotations = classMapper.ignoreFieldsWithAnnotations;

		Map<String, String> keyJsonStrings = new HashMap<>();
		// to hold relation between name and changed name 
		Map<String, String> fieldNames = new HashMap<>();
		
		Set<String> processedNameSet = new HashSet<>();
		//StringBuffer sb = new StringBuffer();
		
		Map<String, Method> getters = getGetters(obj);
		Map<String, Method> setters = getSetters(obj);
		Map<String, Method> otherMethods = getOtherMethods(obj);
		
		Set<Method> jsonAnyGetterMethods = new HashSet<>();
		
		try {
			Field[] fields = getFields(obj);

			for (Field f : fields) {
				f.setAccessible(true);

				String name = f.getName();
				String fieldName = name;
				String lcfieldName = fieldName.toLowerCase();

				// in the ignored list
				if (ObjectUtil.inSet(name, classMapper.jsonIgnoreProperties)) {
					getters.remove(lcfieldName);
					continue;
				}
				
				// 6. Create a blank field mapper instance
				FieldMapper mapper = new FieldMapper(name, name, valueType);
				
				// 7. Classify this field mapper
				mapper = classifyFieldMapper(mapper, classMapper);

				FieldMapper javaFieldMapper = getFieldMapper(name, valueType);
				
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
					if (getter != null && (javaFieldMapper.useAttribute == null && (mapper.useAttribute == null || mapper.useAttribute))
							|| (javaFieldMapper.useAttribute != null && javaFieldMapper.useAttribute) ) {
						annotations = Stream
								.concat(Arrays.stream(annotations),
										Arrays.stream(getter.getDeclaredAnnotations()))
								.toArray(Annotation[]::new);
						
						// no annotations, then try set method
						if ((annotations == null || annotations.length == 0) && setter != null) {
							annotations = setter.getDeclaredAnnotations();
						}
					}

					ca.oson.json.FieldMapper fieldMapperAnnotation = null;
					
					for (Annotation annotation : annotations) {
						if (ignoreField(annotation, ignoreFieldsWithAnnotations)) {
							ignored = true;
							break;
						}
						
						if (annotation instanceof ca.oson.json.FieldMapper) {
							fieldMapperAnnotation = (ca.oson.json.FieldMapper) annotation;
							
						} else {
						
							// to improve performance, using swith on string
							switch (annotation.annotationType().getName()) {
							case "com.fasterxml.jackson.annotation.JsonAnyGetter":
							case "org.codehaus.jackson.annotate.JsonAnyGetter":
								mapper.jsonAnyGetter = true;
								break;
								
							case "com.fasterxml.jackson.annotation.JsonIgnore":
							case "org.codehaus.jackson.annotate.JsonIgnore":
								mapper.ignore = true;
								break;
								
							case "javax.persistence.Transient":
								ignored = true;
								break;
								
							case "com.fasterxml.jackson.annotation.JsonIgnoreProperties":
								JsonIgnoreProperties jsonIgnoreProperties = (JsonIgnoreProperties) annotation;
								if (!jsonIgnoreProperties.allowGetters()) {
									mapper.ignore = true;
								}
								break;
								
							case "com.google.gson.annotations.Expose":
								Expose expose = (Expose) annotation;
								if (!expose.serialize()) {
									mapper.ignore = true;
								}
								break;
								
							case "com.google.gson.annotations.Since":
								Since since = (Since) annotation;
								if (since.value() > classMapper.ignoreVersionsAfter) {
									mapper.ignore = true;
								}
								break;
								
							case "com.fasterxml.jackson.annotation.JsonInclude":
								if (mapper.defaultType == JSON_INCLUDE.NONE) {
									JsonInclude jsonInclude = (JsonInclude) annotation;
									
									switch (jsonInclude.content()) {
									case ALWAYS:
										mapper.defaultType = JSON_INCLUDE.ALWAYS;
										break;
									case NON_NULL:
										mapper.defaultType = JSON_INCLUDE.NON_NULL;
										break;
									case NON_ABSENT:
										mapper.defaultType = JSON_INCLUDE.NON_NULL;
										break;
									case NON_EMPTY:
										mapper.defaultType = JSON_INCLUDE.NON_EMPTY;
										break;
									case NON_DEFAULT:
										mapper.defaultType = JSON_INCLUDE.NON_DEFAULT;
										break;
									case USE_DEFAULTS:
										mapper.defaultType = JSON_INCLUDE.DEFAULT;
										break;
									}
								}
								break;
								
							case "com.fasterxml.jackson.annotation.JsonRawValue":
								if (((JsonRawValue) annotation).value()) {
									mapper.jsonRawValue = true;
								}
								break;
								
							case "org.codehaus.jackson.annotate.JsonRawValue":
								if (((org.codehaus.jackson.annotate.JsonRawValue) annotation).value()) {
									mapper.jsonRawValue = true;
								}
								break;
								
							case "com.fasterxml.jackson.annotation.JsonValue":
							case "org.codehaus.jackson.annotate.JsonValue":
								mapper.jsonValue = true;
								break;
								
							case "javax.persistence.Enumerated":
								mapper.enumType = ((Enumerated) annotation).value();
								break;
								
	//						case "javax.persistence.MapKeyEnumerated":
	//							mapper.enumType = ((javax.persistence.MapKeyEnumerated) annotation).value();
	//							break;
								
							case "javax.validation.constraints.NotNull":
								mapper.required = true;
								break;
								
							case "com.fasterxml.jackson.annotation.JsonProperty":
								JsonProperty jsonProperty = (JsonProperty) annotation;
								Access access = jsonProperty.access();
								if (access == Access.WRITE_ONLY) {
									mapper.ignore = true;
									break;
								}
	
								if (jsonProperty.required()) {
									mapper.required = true;
								}
	
								if (jsonProperty.defaultValue() != null && jsonProperty.defaultValue().length() > 0) {
									mapper.defaultValue = jsonProperty.defaultValue();
								}
								break;
								
							case "javax.validation.constraints.Size":
								Size size = (Size) annotation;
								if (size.min() > 0) {
									mapper.min = size.min();
								}
								if (size.max() < Integer.MAX_VALUE) {
									mapper.max = size.max();
								}
								break;
								
							case "javax.persistence.Column":
								Column column = (Column) annotation;
								if (column.length() != 255) {
									mapper.length = column.length();
								}
								if (column.scale() > 0) {
									mapper.scale = column.scale();
								}
	
								if (column.precision() > 0) {
									mapper.precision = column.precision();
								}
								
								if (!column.nullable()) {
									mapper.required = true;
								}
	
								break;
							}
	
							String fname = ObjectUtil.getName(annotation);
							if (fname != null) {
								names.add(fname);
							}
						}
					}
					
					// 9. Apply annotations from Oson
					// special name to handle
					if (fieldMapperAnnotation != null) {
						mapper = overwriteBy (mapper, fieldMapperAnnotation, classMapper);
					}
				}

				if (ignored) {
					if (getter != null) {
						getters.remove(lcfieldName);
					}
					continue;
				}
				
				// 10. Apply Java configuration for this particular field
				if (javaFieldMapper != null) {
					mapper = overwriteBy (mapper, javaFieldMapper);
				}
				
				if (mapper.ignore != null && mapper.ignore) {
					if (getter != null) {
						getters.remove(lcfieldName);
					}
					continue;
				}

				if (mapper.jsonAnyGetter != null && mapper.jsonAnyGetter && getter != null) {
					getters.remove(lcfieldName);
					jsonAnyGetterMethods.add(getter);
					continue;
				}
				
				if (mapper.useField != null && !mapper.useField) {
					// both should not be used, just like ignore
					if (mapper.useAttribute != null && !mapper.useAttribute) {
						getters.remove(lcfieldName);
					}
					continue;
				}
				
				
				// handling name now
				boolean jnameFixed = false;
				String jname = mapper.json;
				if (jname == null) {
					if (getter != null) {
						getters.remove(lcfieldName);
					}
					continue;
					
				} else if (!jname.equals(name)) {
					jnameFixed = true;
				}
				
				if (!jnameFixed) {
					jname = java2Json(name);
					if (jname == null) {
						continue;
					} else if (!jname.equals(name)) {
						name = jname;
						mapper.java = name;
						mapper.json = jname;
						jnameFixed = true;
						
					} else {
						for (String nm: names) {
							jname = java2Json(nm);
							if (jname == null) {
								ignored = true;
								break;
							}
							if (!jname.equals(nm)) {
								name = jname;
								mapper.java = name;
								mapper.json = jname;
								jnameFixed = true;
								break;
							} else {
								name = nm;
								mapper.java = name;
								mapper.json = name;
								jnameFixed = true;
							}
						}
					}
				}
				
				if (ignored) {
					getters.remove(lcfieldName);
					continue;
				}
				
				// only if the name is still the same as the field name
				// format it based on the naming settings
				// otherwise, it is set on purpose
				if (!jnameFixed && fieldName.equals(name)) {
					name = StringUtil.formatName(name, format);
					mapper.java = fieldName;
					mapper.json = name;
					jnameFixed = true;
				}
				
				// possible?, just in case
				if (!jnameFixed) {
					mapper.java = fieldName;
					mapper.json = fieldName;
					name = fieldName;
					jnameFixed = true;
				}
				
				
				// field valuie
				Object value = f.get(obj);
				
				// value from getter
				Object getterValue = null;

				if (getter != null) {
					if (mapper.useAttribute == null || mapper.useAttribute) {
						try {
							getterValue = getter.invoke(obj, null);
						} catch (InvocationTargetException e) {
							// e.printStackTrace();
							try {
								Expression expr = new Expression(obj, getter.getName(), new Object[0]);
								expr.execute();
								getterValue = expr.getValue();
								
								if (getterValue == null) {
									getterValue = getter.getDefaultValue();
								}
								
							} catch (Exception e1) {
								// e1.printStackTrace();
							}
						}
					}
					
					getters.remove(lcfieldName);
				}
				
				// determine which value to use
				if (getterValue != null) {
					if (getterValue.equals(value) || StringUtil.isEmpty(value)) {
						value = getterValue;
						
					} else if (DefaultValue.isDefault(value) && !DefaultValue.isDefault(getterValue)) {
						value = getterValue;
					} else if (getterValue.toString().length() > value.toString().length()) {
						value = getterValue;
					}
				}


				if (mapper.jsonValue) {
					if (value != null) {
						if (mapper.jsonRawValue) {
							return value.toString();
						} else {
							return StringUtil.doublequote(value);
						}
					}
				}
				
				String str;

				Class<?> returnType = f.getType(); // value.getClass();
				FieldData fieldData = new FieldData(obj, f, value, returnType, false, mapper);

				str = object2Json(fieldData, level, set);

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
					
				} else if (classMapper.defaultType == JSON_INCLUDE.NON_DEFAULT && DefaultValue.isDefault(str)) {
					continue;
				}

				StringBuffer sb = new StringBuffer();
				sb.append(repeatedItem);
				sb.append("\"" + name + "\":" + pretty);
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
				
				String name = StringUtil.uncapitalize(getter.getName().substring(3));
				// just use field name, even it might not be a field
				String fieldName = name;
				
				// in the ignored list
				if (ObjectUtil.inSet(name, classMapper.jsonIgnoreProperties)) {
					continue;
				}
				
				if (processedNameSet.contains(name) || fieldNames.containsKey(lcfieldName)) {
					continue;
				}
				
				getter.setAccessible(true);
				
				Method setter = setters.get(lcfieldName);
				
				// 6. Create a blank field mapper instance
				FieldMapper mapper = new FieldMapper(name, name, valueType);
				
				// 7. Classify this field mapper
				mapper = classifyFieldMapper(mapper, classMapper);

				FieldMapper javaFieldMapper = getFieldMapper(name, valueType);
				
				boolean ignored = false;
				Set<String> names = new HashSet<>();
				
				if (annotationSupport) {
					annotations = getter.getDeclaredAnnotations();//.getAnnotations();
					
					// no annotations, then try set method
					if ((annotations == null || annotations.length == 0) && setter != null) {
						annotations = setter.getDeclaredAnnotations();
					}

					ca.oson.json.FieldMapper fieldMapperAnnotation = null;
					
					for (Annotation annotation : annotations) {
						if (ignoreField(annotation, ignoreFieldsWithAnnotations)) {
							ignored = true;
							break;
						}
						
						if (annotation instanceof ca.oson.json.FieldMapper) {
							fieldMapperAnnotation = (ca.oson.json.FieldMapper) annotation;
							
						} else {
						
							// to improve performance, using swith on string
							switch (annotation.annotationType().getName()) {
							case "com.fasterxml.jackson.annotation.JsonAnyGetter":
							case "org.codehaus.jackson.annotate.JsonAnyGetter":
								mapper.jsonAnyGetter = true;
								break;
								
							case "com.fasterxml.jackson.annotation.JsonIgnore":
							case "org.codehaus.jackson.annotate.JsonIgnore":
								mapper.ignore = true;
								break;
								
							case "javax.persistence.Transient":
								ignored = true;
								break;
								
							case "com.fasterxml.jackson.annotation.JsonIgnoreProperties":
								JsonIgnoreProperties jsonIgnoreProperties = (JsonIgnoreProperties) annotation;
								if (!jsonIgnoreProperties.allowGetters()) {
									mapper.ignore = true;
								}
								break;
								
							case "com.google.gson.annotations.Expose":
								Expose expose = (Expose) annotation;
								if (!expose.serialize()) {
									mapper.ignore = true;
								}
								break;
								
							case "com.google.gson.annotations.Since":
								Since since = (Since) annotation;
								if (since.value() > classMapper.ignoreVersionsAfter) {
									mapper.ignore = true;
								}
								break;
								
							case "com.fasterxml.jackson.annotation.JsonInclude":
								if (mapper.defaultType == JSON_INCLUDE.NONE) {
									JsonInclude jsonInclude = (JsonInclude) annotation;
									
									switch (jsonInclude.content()) {
									case ALWAYS:
										mapper.defaultType = JSON_INCLUDE.ALWAYS;
										break;
									case NON_NULL:
										mapper.defaultType = JSON_INCLUDE.NON_NULL;
										break;
									case NON_ABSENT:
										mapper.defaultType = JSON_INCLUDE.NON_NULL;
										break;
									case NON_EMPTY:
										mapper.defaultType = JSON_INCLUDE.NON_EMPTY;
										break;
									case NON_DEFAULT:
										mapper.defaultType = JSON_INCLUDE.NON_DEFAULT;
										break;
									case USE_DEFAULTS:
										mapper.defaultType = JSON_INCLUDE.DEFAULT;
										break;
									}
								}
								break;
								
							case "com.fasterxml.jackson.annotation.JsonRawValue":
								if (((JsonRawValue) annotation).value()) {
									mapper.jsonRawValue = true;
								}
								break;
								
							case "org.codehaus.jackson.annotate.JsonRawValue":
								if (((org.codehaus.jackson.annotate.JsonRawValue) annotation).value()) {
									mapper.jsonRawValue = true;
								}
								break;
								
							case "com.fasterxml.jackson.annotation.JsonValue":
							case "org.codehaus.jackson.annotate.JsonValue":
								mapper.jsonValue = true;
								break;
								
							case "javax.persistence.Enumerated":
								mapper.enumType = ((Enumerated) annotation).value();
								break;
								
	//						case "javax.persistence.MapKeyEnumerated":
	//							mapper.enumType = ((javax.persistence.MapKeyEnumerated) annotation).value();
	//							break;
								
							case "javax.validation.constraints.NotNull":
								mapper.required = true;
								break;
								
							case "com.fasterxml.jackson.annotation.JsonProperty":
								JsonProperty jsonProperty = (JsonProperty) annotation;
								Access access = jsonProperty.access();
								if (access == Access.WRITE_ONLY) {
									mapper.ignore = true;
									break;
								}
	
								if (jsonProperty.required()) {
									mapper.required = true;
								}
	
								if (jsonProperty.defaultValue() != null && jsonProperty.defaultValue().length() > 0) {
									mapper.defaultValue = jsonProperty.defaultValue();
								}
								break;
								
							case "javax.validation.constraints.Size":
								Size size = (Size) annotation;
								if (size.min() > 0) {
									mapper.min = size.min();
								}
								if (size.max() < Integer.MAX_VALUE) {
									mapper.max = size.max();
								}
								break;
								
							case "javax.persistence.Column":
								Column column = (Column) annotation;
								if (column.length() != 255) {
									mapper.length = column.length();
								}
								if (column.scale() > 0) {
									mapper.scale = column.scale();
								}
	
								if (column.precision() > 0) {
									mapper.precision = column.precision();
								}
								
								if (!column.nullable()) {
									mapper.required = true;
								}
	
								break;
							}
	
							String fname = ObjectUtil.getName(annotation);
							if (fname != null) {
								names.add(fname);
							}
						}
					}
					
					// 9. Apply annotations from Oson
					// special name to handle
					if (fieldMapperAnnotation != null) {
						mapper = overwriteBy (mapper, fieldMapperAnnotation, classMapper);
					}
				}

				if (ignored) {
					continue;
				}
				
				// 10. Apply Java configuration for this particular field
				if (javaFieldMapper != null) {
					mapper = overwriteBy (mapper, javaFieldMapper);
				}
				
				if (mapper.ignore != null && mapper.ignore) {
					continue;
				}

				if (mapper.jsonAnyGetter != null && mapper.jsonAnyGetter) {
					jsonAnyGetterMethods.add(getter);
					continue;
				}
				
				if (mapper.useAttribute != null && !mapper.useAttribute) {
					continue;
				}

				// handling name
				boolean jnameFixed = false;
				String jname = mapper.json;
				if (jname == null) {
					continue;
					
				} else if (!jname.equals(name)) {
					jnameFixed = true;
				}

				if (!jnameFixed) {
					jname = java2Json(name);
					if (jname == null) {
						continue;
					} else if (!jname.equals(name)) {
						name = jname;
						mapper.java = name;
						mapper.json = jname;
						jnameFixed = true;
						
					} else {
						for (String nm: names) {
							jname = java2Json(nm);
							if (jname == null) {
								ignored = true;
								break;
							}
							if (!jname.equals(nm)) {
								name = jname;
								mapper.java = name;
								mapper.json = jname;
								jnameFixed = true;
								break;
							} else {
								name = nm;
								mapper.java = name;
								mapper.json = name;
								jnameFixed = true;
							}
						}
					}
				}
				
				if (ignored) {
					continue;
				}
				
				// only if the name is still the same as the field name
				// format it based on the naming settings
				// otherwise, it is set on purpose
				if (!jnameFixed && fieldName.equals(name)) {
					name = StringUtil.formatName(name, format);
					mapper.java = fieldName;
					mapper.json = name;
					jnameFixed = true;
				}

				// possible?, just in case
				if (!jnameFixed) {
					mapper.java = fieldName;
					mapper.json = fieldName;
					name = fieldName;
					jnameFixed = true;
				}
				
				
				// get value
				Object value = null;

				try {
					value = getter.invoke(obj, null);
				} catch (InvocationTargetException e) {
					// e.printStackTrace();
					try {
						Expression expr = new Expression(obj, getter.getName(), new Object[0]);
						expr.execute();
						value = expr.getValue();
						
						if (value == null) {
							value = getter.getDefaultValue();
						}
						
					} catch (Exception e1) {
						// e1.printStackTrace();
					}
				}

				if (mapper.jsonValue != null && mapper.jsonValue) {
					if (value != null) {
						if (mapper.jsonRawValue) {
							return value.toString();
						} else {
							return StringUtil.doublequote(value);
						}
					}
				}
				

				String str;

				Class<?> returnType = getter.getReturnType(); // value.getClass();
				FieldData fieldData = new FieldData(obj, null, value, returnType, false, mapper);
				objectDTO.getter = getter;

				str = object2Json(fieldData, level, set);

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
					
				} else if (classMapper.defaultType == JSON_INCLUDE.NON_DEFAULT && DefaultValue.isDefault(str)) {
					continue;
				}

				StringBuffer sb = new StringBuffer();
				sb.append(repeatedItem);
				sb.append("\"" + name + "\":" + pretty);
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
						
						if (annotation instanceof JsonValue) {
							try {
								method.setAccessible(true);
								Object mvalue = method.invoke(obj, null);
			
								if (mvalue != null) {
									return StringUtil.doublequote(mvalue);
								}
								
							} catch (InvocationTargetException e) {
								// e.printStackTrace();
							}
							
						} else if (annotation instanceof org.codehaus.jackson.annotate.JsonValue) {
							try {
								method.setAccessible(true);
								Object mvalue = method.invoke(obj, null);
			
								if (mvalue != null) {
									return StringUtil.doublequote(mvalue);
								}
								
							} catch (InvocationTargetException e) {
								// e.printStackTrace();
							}
							
						} else if (annotation instanceof JsonAnyGetter || annotation instanceof org.codehaus.jackson.annotate.JsonAnyGetter
								|| annotation instanceof ca.oson.json.FieldMapper ) {

							if (annotation instanceof ca.oson.json.FieldMapper) {
								ca.oson.json.FieldMapper mapper = (ca.oson.json.FieldMapper)annotation;
								
								if (mapper.jsonAnyGetter() == BOOLEAN.FALSE) {
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
					Object allValues = null;
					
					try {
						method.setAccessible(true);
						allValues = method.invoke(obj, null);
					} catch (Exception e) {
						try {
							Expression expr = new Expression(obj, method.getName(), new Object[0]);
							expr.execute();
							allValues = expr.getValue();
							
							if (allValues == null) {
								allValues = method.getDefaultValue();
							}
							
						} catch (Exception e1) {
							// e1.printStackTrace();
						}
						
					}

					if (allValues != null && allValues instanceof Map) {
						Map<String, Object> map = (Map)allValues;
						String str;
						for (String name: map.keySet()) {
							Object value = map.get(name);
							
							FieldData newFieldData = new FieldData(value, value.getClass());
							newFieldData.json2Java = false;
							newFieldData.defaultType = classMapper.defaultType;
							str = object2Json(newFieldData, level, set);
	
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
								
							} else if (DefaultValue.isDefault(str)) {
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

			
			
			int size = keyJsonStrings.size();
			if (size == 0) {
				return "{}"; // ""

			} else {
				String includeClassType = "";
				if (classMapper.includeClassTypeInJson) { //getIncludeClassTypeInJson()
					includeClassType = repeatedItem + "\"@class\":" + pretty + "\"" + valueType.getName() + "\",";
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

		} catch (IllegalAccessException | IllegalArgumentException
				| SecurityException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
			// } catch (InvocationTargetException e) {
		}
	}


	////////////////////////////////////////////////////////////////////////////////
	// START OF package methods
	////////////////////////////////////////////////////////////////////////////////


	/*
	 * Rely on JSONObject to parse original json text
	 * more confortable to work with map and list
	 * instead of JSONObject and JSONArray
	 */
	Object fromJsonMap(Object obj) {
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

			Map<String, Object> map = new LinkedHashMap<String, Object>();

			Iterator<?> keys = jobj.keys();

			try {
				while (keys.hasNext()) {
					String key = (String) keys.next();

					// map.put(camelCase2Underscore(key),
					// fromJsonToMap(jobj.get(key)));
					map.put(key, fromJsonMap(jobj.get(key)));
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
			source = source.trim();
			if (source.startsWith("[")) {
				JSONArray obj = new JSONArray(source);

				List list = (List)fromJsonMap(obj);

				FieldData fieldData = new FieldData(list, type, true);
				return json2Object(fieldData);

			} else if (source.startsWith("{")) {
				JSONObject obj = new JSONObject(source);

				Map<String, Object> map = (Map)fromJsonMap(obj);

				Class<T> valueType = ObjectUtil.getTypeClass(type);

				if (Iterable.class.isAssignableFrom(valueType) || Map.class.isAssignableFrom(valueType)) {
					FieldData fieldData = new FieldData(map, type, true);
					fieldData.returnType = valueType;

					return json2Object(fieldData);
				} else {
					return (T) deserialize(map, valueType);
				}

			} else {
				return null;
			}

		} catch (JSONException ex) {
			//ex.printStackTrace();
		}

		return null;
	}


	<T> T fromJsonMap(String source, Class<T> valueType) {
		return fromJsonMap(source, valueType, null);
	}

	<T> T fromJsonMap(String source, Class<T> valueType, T object) {
		// use gson TypeToken
		// Gson gson = new Gson();
		// map = gson.fromJson(source, new TypeToken<Map<String,
		// Object>>(){}.getType());

		try {
			source = source.trim();
			if (source.startsWith("[")) {
				JSONArray obj = new JSONArray(source);

				List list = (List)fromJsonMap(obj);

				return json2Object(new FieldData(list, valueType, true, object));

			} else if (source.startsWith("{")) {
				JSONObject obj = new JSONObject(source);

				Map<String, Object> map = (Map)fromJsonMap(obj);

				if (valueType != null && Map.class.isAssignableFrom(valueType)) {
					return json2Object(new FieldData(map, valueType, true, object));

				} else {
					if (object == null) {
						return deserialize(map, valueType);
					} else {
						return deserialize(map, valueType, object);
					}
				}

			} else {
				return json2Object(new FieldData(source, valueType, true, null));
			}

		} catch (JSONException ex) {
			ex.printStackTrace();
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

	<T> T deserialize(Map<String, Object> map, Class<T> valueType) {
		if (map == null) {
			return null;
		}

		T obj = newInstance(map, valueType);

		return deserialize(map, valueType, obj);
	}

	/*
	 * create an initial object of valueType type to copy data into
	 */
	<T> T newInstance(Map<String, Object> map, Class<T> valueType) {
		InstanceCreator creator = getTypeAdapter(valueType);
		
		if (creator != null) {
			return (T) creator.createInstance(valueType);
		}
		
		
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
		String JsonClassType = null;

		if (valueType != null) {
			valueType.getAnnotations();
			if (getAnnotationSupport()) {
				for (Annotation annotation : valueType.getAnnotations()) {
					if (annotation instanceof JsonTypeInfo) {
						JsonTypeInfo jsonTypeInfo = (JsonTypeInfo) annotation;
						JsonTypeInfo.Id use = jsonTypeInfo.use();
						JsonTypeInfo.As as = jsonTypeInfo.include();
						if ((use == JsonTypeInfo.Id.MINIMAL_CLASS || use == JsonTypeInfo.Id.CLASS)
								&& as == As.PROPERTY
								) {
							JsonClassType = jsonTypeInfo.property();
						}
					}
				}
			}
		}
		if (JsonClassType == null) {
			JsonClassType = getJsonClassType();
		}


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


		try {
			obj = valueType.newInstance();

			if (obj != null) {
				return obj;
			}

		} catch (InstantiationException | IllegalAccessException e) {
			//e.printStackTrace();
		}

		Constructor<?>[] constructors = valueType.getDeclaredConstructors();//.getConstructors();

		///*
		for (Constructor constructor: constructors) {
			//Class[] parameterTypes = constructor.getParameterTypes();

			int parameterCount = constructor.getParameterCount();
			if (parameterCount > 0) {
				constructor.setAccessible(true);

				Annotation[] annotations = constructor.getDeclaredAnnotations(); // getAnnotations();

				for (Annotation annotation : annotations) {
					boolean isJsonCreator = false;
					if (annotation instanceof JsonCreator) {
						isJsonCreator = true;
					} else if (annotation instanceof ca.oson.json.FieldMapper) {
						ca.oson.json.FieldMapper mapper = (ca.oson.json.FieldMapper)annotation;
						
						if (mapper.jsonCreator() == BOOLEAN.TRUE) {
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
							Object parameterValue = getMapValue(map, parameterName);

							if (parameterValue == null) {
								FieldData objectDTO = new FieldData(null, parameters[i].getType());
								objectDTO.required = true;
								parameterValue = json2Object(objectDTO);
							}

							parameterValues[i++] = parameterValue;
						}

						try {
							obj = (T) constructor.newInstance(parameterValues);

							if (obj != null) {
								return obj;
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
						return obj;
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
								parameterValue = getMapValue(map, parameterNames.get(i));

								if (parameterValue == null) {
									FieldData objectDTO = new FieldData(null, parameterTypes[i]);
									objectDTO.required = true;
									parameterValue = json2Object(objectDTO);
								}

								parameterValues[i] = parameterValue;
							}

							try {
								obj = (T) constructor.newInstance(parameterValues);
								if (obj != null) {
									return obj;
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

				int length = parameterTypes.length;

				Object[] parameterValues = new Object[length];
				for (int i = 0; i < length; i++) {
					FieldData objectDTO = new FieldData(null, parameterTypes[i]);
					objectDTO.required = true;
					parameterValues[i] = json2Object(objectDTO);
				}

				try {
					obj = (T) constructor.newInstance(parameterValues);
					if (obj != null) {
						return obj;
					}

				} catch (InstantiationException
						| IllegalAccessException
						| IllegalArgumentException
						| InvocationTargetException e) {
					//e.printStackTrace();
				}
			}
		}

		// try last time
		try {
			Method[] methods = valueType.getMethods(); // .getMethod("getInstance", null);

			List<Method> methodList = new ArrayList<>();

			if (methods != null) {
				for (Method method: methods) {
					String methodName = method.getName();

					if (methodName.equals("getInstance") || methodName.equals("newInstance")) {
						Class returnType = method.getReturnType();

						if (valueType.isAssignableFrom(returnType) && Modifier.isStatic(method.getModifiers())) {
							int parameterCount = method.getParameterCount();
							if (parameterCount == 0) {
								try {
									obj = (T)method.invoke(null);
									if (obj != null) {
										return obj;
									}

								} catch (IllegalAccessException
										| IllegalArgumentException
										| InvocationTargetException e) {
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

						String[] parameterNames = ObjectUtil.getParameterNames(method);
						if (parameterNames != null && parameterNames.length == parameterCount) {
							for (String parameterName: ObjectUtil.getParameterNames(method)) {
								parameterValue = getMapValue(map, parameterName);

								if (parameterValue == null) {
									FieldData objectDTO = new FieldData(null, parameterTypes[i]);
									objectDTO.required = true;
									parameterValue = json2Object(objectDTO);
								}

								parameterValues[i] = parameterValue;
							}

						} else {
							// try annotation
							Parameter[] parameters = method.getParameters();
							parameterNames = ObjectUtil.getParameterNames(parameters);
							parameterCount = parameters.length;
							parameterValues = new Object[parameterCount];
							i = 0;
							for (String parameterName: parameterNames) {
								parameterValue = getMapValue(map, parameterName);

								if (parameterValue == null) {
									FieldData objectDTO = new FieldData(null, parameters[i].getType());
									objectDTO.required = true;
									parameterValue = json2Object(objectDTO);
								}

								parameterValues[i++] = parameterValue;
							}
						}

						obj = (T)method.invoke(null, parameterValues);
						if (obj != null) {
							return obj;
						}

					} catch (IOException
							| IllegalAccessException
							| IllegalArgumentException
							| InvocationTargetException e) {
						//e.printStackTrace();
					}
				}

			}

		} catch (SecurityException e) {
			// e.printStackTrace();
		}

		return null;
	}


	/*
	 * string to object, deserialize, set method should be used
	 * 
	 * It involves 10 steps to apply processing rules:
	 * 1. Create a blank class mapper instance;
	 * 2. Globalize it;
	 * 3. Apply annotations from other sources;
	 * 4. Apply annotations from Oson;
	 * 5. Apply Java configuration for this particular class;
	 * 6. Create a blank field mapper instance;
	 * 7. Classify this field mapper;
	 * 8. Apply annotations from other sources;
	 * 9. Apply annotations from Oson;
	 * 10. Apply Java configuration for this particular field.
	 */
	<T> T deserialize(Map<String, Object> map, Class<T> valueType, T obj) {

		Set<String> nameKeys = new HashSet(map.keySet());

		// first build up the class-level processing rules
		
		// 1. Create a blank class mapper instance
		ClassMapper classMapper = new ClassMapper(valueType);
		
		// 2. Globalize it
		classMapper = globalize(classMapper);


		try {
			boolean annotationSupport = getAnnotationSupport();
			Annotation[] annotations = null;

			if (annotationSupport) {
				ca.oson.json.ClassMapper classMapperAnnotation = null;
				
				// 3. Apply annotations from other sources
				annotations = valueType.getAnnotations();
				for (Annotation annotation : annotations) {
					if (ignoreClass(annotation)) {
						return null;
					}

					switch (annotation.annotationType().getName()) {
					case "ca.oson.json.ClassMapper":
						classMapperAnnotation = (ca.oson.json.ClassMapper) annotation;
						break;

					case "com.fasterxml.jackson.annotation.JsonIgnoreProperties":
						JsonIgnoreProperties jsonIgnoreProperties = (JsonIgnoreProperties) annotation;
						String[] jsonnames = jsonIgnoreProperties.value();
						if (jsonnames != null && jsonnames.length > 0) {
							if (classMapper.jsonIgnoreProperties == null) {
								classMapper.jsonIgnoreProperties = new HashSet();
							}
							
							classMapper.jsonIgnoreProperties.addAll(Arrays.asList(jsonnames));
						}
						break;
						
					case "org.codehaus.jackson.annotate.JsonIgnoreProperties":
						org.codehaus.jackson.annotate.JsonIgnoreProperties jsonIgnoreProperties2 = (org.codehaus.jackson.annotate.JsonIgnoreProperties) annotation;
						String[] jsonnames2 = jsonIgnoreProperties2.value();
						if (jsonnames2 != null && jsonnames2.length > 0) {
							if (classMapper.jsonIgnoreProperties == null) {
								classMapper.jsonIgnoreProperties = new HashSet();
							}
							
							classMapper.jsonIgnoreProperties.addAll(Arrays.asList(jsonnames2));
						}
						break;
						
					case "com.fasterxml.jackson.annotation.JsonPropertyOrder":
						// first come first serve
						if (classMapper.propertyOrders == null) {
							classMapper.propertyOrders = ((JsonPropertyOrder) annotation).value();
						}
						break;
						
					case "org.codehaus.jackson.annotate.JsonPropertyOrder":
						// first come first serve
						if (classMapper.propertyOrders == null) {
							classMapper.propertyOrders = ((org.codehaus.jackson.annotate.JsonPropertyOrder) annotation).value();
						}
						break;
						
					case "com.fasterxml.jackson.annotation.JsonInclude":
						if (classMapper.defaultType == JSON_INCLUDE.NONE) {
							JsonInclude jsonInclude = (JsonInclude) annotation;
							switch (jsonInclude.content()) {
							case ALWAYS:
								classMapper.defaultType = JSON_INCLUDE.ALWAYS;
								break;
							case NON_NULL:
								classMapper.defaultType = JSON_INCLUDE.NON_NULL;
								break;
							case NON_ABSENT:
								classMapper.defaultType = JSON_INCLUDE.NON_NULL;
								break;
							case NON_EMPTY:
								classMapper.defaultType = JSON_INCLUDE.NON_EMPTY;
								break;
							case NON_DEFAULT:
								classMapper.defaultType = JSON_INCLUDE.NON_DEFAULT;
								break;
							case USE_DEFAULTS:
								classMapper.defaultType = JSON_INCLUDE.DEFAULT;
								break;
							}
						}
						break;	
						
					case "com.fasterxml.jackson.annotation.JsonAutoDetect":
						JsonAutoDetect jsonAutoDetect = (JsonAutoDetect) annotation;
						if (jsonAutoDetect.fieldVisibility() == Visibility.NONE) {
							classMapper.useField = false;
						} else if (jsonAutoDetect.fieldVisibility() != Visibility.DEFAULT) {
							classMapper.useField = true;
						}
						if (jsonAutoDetect.setterVisibility() == Visibility.NONE) {
							classMapper.useAttribute = false;
						} else if (jsonAutoDetect.setterVisibility() != Visibility.DEFAULT) {
							classMapper.useAttribute = true;
						}
						
						break;
						
					case "org.codehaus.jackson.annotate.JsonAutoDetect":
						org.codehaus.jackson.annotate.JsonAutoDetect jsonAutoDetect2 = (org.codehaus.jackson.annotate.JsonAutoDetect) annotation;
						if (jsonAutoDetect2.fieldVisibility() == org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.NONE) {
							classMapper.useField = false;
						}
						if (jsonAutoDetect2.getterVisibility() == org.codehaus.jackson.annotate.JsonAutoDetect.Visibility.NONE) {
							classMapper.useAttribute = false;
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
			
			if (classMapper.ignore != null && classMapper.ignore) {
				return null;
			}
			
			Function function = classMapper.deserializer; // = getDeserializer(valueType);
			if (function != null) {
				try {
					
					if (function instanceof Json2ClassDataFunction) {
						ClassData classData = new ClassData(map, valueType, obj, classMapper);
						
						return (T) ((Json2ClassDataFunction)function).apply(classData);
						
					} else {
						Object returnedValue = function.apply(obj);

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
					}
				} catch (Exception e) {}
			}
			

			Map<String, Method> getters = getGetters(obj);
			Map<String, Method> setters = getSetters(obj);
			Map<String, Method> otherMethods = getOtherMethods(obj);
			Set<String> processedNameSet = new HashSet<>();
			
			Method jsonAnySetterMethod = null;
			
			Field[] fields = getFields(obj);

			FIELD_NAMING format = getFieldNaming();

			for (Field f : fields) {
				f.setAccessible(true);

				String name = f.getName();
				String fieldName = name;
				String lcfieldName = name.toLowerCase();

				// in the ignored list
				if (ObjectUtil.inSet(name, classMapper.jsonIgnoreProperties)) {
					setters.remove(lcfieldName);
					nameKeys.remove(name);
					continue;
				}
				
				// getter and setter methods
				Method getter = getters.get(lcfieldName);
				Method setter = setters.get(lcfieldName);
				
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
				FieldMapper mapper = new FieldMapper(name, name, valueType);
				
				// 7. Classify this field mapper
				mapper = classifyFieldMapper(mapper, classMapper);

				FieldMapper javaFieldMapper = getFieldMapper(name, valueType);

				Class<?> returnType = f.getType(); // value.getClass();

				boolean ignored = false;
				
				if (setter != null) {
					setter.setAccessible(true);
				}
				
				Set<String> names = new HashSet<>();
				
				if (annotationSupport) {
					annotations = f.getAnnotations();
					
					if (setter != null && (javaFieldMapper.useAttribute == null && (mapper.useAttribute == null || mapper.useAttribute))
							|| (javaFieldMapper.useAttribute != null && javaFieldMapper.useAttribute) ) {
						annotations = Stream
								.concat(Arrays.stream(annotations),
										Arrays.stream(setter.getDeclaredAnnotations()))
								.toArray(Annotation[]::new);
					}
					
					// no annotations, then try get method
					if ((annotations == null || annotations.length == 0) && getter != null) {
						annotations = getter.getDeclaredAnnotations();
					}
					
					ca.oson.json.FieldMapper fieldMapperAnnotation = null;
					
					for (Annotation annotation : annotations) {
						if (ignoreField(annotation, classMapper.ignoreFieldsWithAnnotations)) {
							ignored = true;
							break;
						}
						
						if (annotation instanceof ca.oson.json.FieldMapper) {
							fieldMapperAnnotation = (ca.oson.json.FieldMapper) annotation;
							
						} else {
							// to improve performance, using swith on string
							switch (annotation.annotationType().getName()) {
							case "com.fasterxml.jackson.annotation.JsonAnySetter":
							case "org.codehaus.jackson.annotate.JsonAnySetter":
								mapper.jsonAnySetter = true;
								break;
								
							case "javax.persistence.Transient":
								mapper.ignore = true;
								break;
								
							case "com.fasterxml.jackson.annotation.JsonIgnore":
							case "org.codehaus.jackson.annotate.JsonIgnore":
								mapper.ignore = true;
								break;
								
							case "com.fasterxml.jackson.annotation.JsonIgnoreProperties":
								JsonIgnoreProperties jsonIgnoreProperties = (JsonIgnoreProperties) annotation;
								if (!jsonIgnoreProperties.allowSetters()) {
									mapper.ignore = true;
								}
								break;
								
							case "com.google.gson.annotations.Expose":
								Expose expose = (Expose) annotation;
								if (!expose.deserialize()) {
									mapper.ignore = true;
								}
								break;
								
							case "com.google.gson.annotations.Since":
								Since since = (Since) annotation;
								if (since.value() > classMapper.ignoreVersionsAfter) {
									mapper.ignore = true;
								}
								break;
								
							case "com.fasterxml.jackson.annotation.JsonInclude":
								if (mapper.defaultType == JSON_INCLUDE.NONE) {
									JsonInclude jsonInclude = (JsonInclude) annotation;
									
									switch (jsonInclude.content()) {
									case ALWAYS:
										mapper.defaultType = JSON_INCLUDE.ALWAYS;
										break;
									case NON_NULL:
										mapper.defaultType = JSON_INCLUDE.NON_NULL;
										break;
									case NON_ABSENT:
										mapper.defaultType = JSON_INCLUDE.NON_NULL;
										break;
									case NON_EMPTY:
										mapper.defaultType = JSON_INCLUDE.NON_EMPTY;
										break;
									case NON_DEFAULT:
										mapper.defaultType = JSON_INCLUDE.NON_DEFAULT;
										break;
									case USE_DEFAULTS:
										mapper.defaultType = JSON_INCLUDE.DEFAULT;
										break;
									}
								}
								break;
								
							case "com.fasterxml.jackson.annotation.JsonRawValue":
								if (((JsonRawValue) annotation).value()) {
									mapper.jsonRawValue = true;
								}
								break;
								
							case "org.codehaus.jackson.annotate.JsonRawValue":
								if (((org.codehaus.jackson.annotate.JsonRawValue) annotation).value()) {
									mapper.jsonRawValue = true;
								}
								break;
								
							case "javax.persistence.Enumerated":
								mapper.enumType = ((Enumerated) annotation).value();
								break;
								
							case "javax.validation.constraints.NotNull":
								mapper.required = true;
								break;
								
							case "com.fasterxml.jackson.annotation.JsonProperty":
								JsonProperty jsonProperty = (JsonProperty) annotation;
								Access access = jsonProperty.access();
								if (access == Access.READ_ONLY) {
									mapper.ignore = true;
								}
	
								if (jsonProperty.required()) {
									mapper.required = true;
								}
	
								if (mapper.defaultValue == null) {
									mapper.defaultValue = jsonProperty.defaultValue();
								}
								break;
								
							case "javax.validation.constraints.Size":
								Size size = (Size) annotation;
								if (size.min() > 0) {
									mapper.min = size.min();
								}
								if (size.max() < Integer.MAX_VALUE) {
									mapper.max = size.max();
								}
								break;
								
							case "javax.persistence.Column":
								Column column = (Column) annotation;
								if (column.length() != 255) {
									mapper.length = column.length();
								}
								if (column.scale() > 0) {
									mapper.scale = column.scale();
								}
								if (column.precision() > 0) {
									mapper.precision = column.precision();
								}

								if (!column.nullable()) {
									mapper.required = true;
								}
								break;
							}
						
							String fname = ObjectUtil.getName(annotation);
							if (fname != null) {
								names.add(fname);
							}
						}
					}

					// 9. Apply annotations from Oson
					if (fieldMapperAnnotation != null) {
						mapper = overwriteBy (mapper, fieldMapperAnnotation, classMapper);
					}
				}
				
				if (ignored) {
					nameKeys.remove(name);
					nameKeys.remove(mapper.json);
					setters.remove(lcfieldName);
					continue;
				}

				// 10. Apply Java configuration for this particular field
				if (javaFieldMapper != null) {
					mapper = overwriteBy (mapper, javaFieldMapper);
				}
				
				if (mapper.ignore != null && mapper.ignore) {
					if (setter != null) {
						setters.remove(lcfieldName);
					}
					nameKeys.remove(name);
					nameKeys.remove(mapper.json);
					continue;
				}

				if (mapper.jsonAnySetter != null && mapper.jsonAnySetter && setter != null) {
					setters.remove(lcfieldName);
					otherMethods.put(lcfieldName, setter);
					continue;
				}
				
				if (mapper.useField != null && !mapper.useField) {
					// both should not be used, just like ignore
					if (mapper.useAttribute != null && !mapper.useAttribute) {
						getters.remove(lcfieldName);
					}
					continue;
				}
				

				// get value for name in map
				Object value = getMapValue(map, name, nameKeys);
				
				boolean jnameFixed = false;
				String jname = json2Java(name);

				if (jname == null) {
					continue;
				} else if (!jname.equals(name)) {
					Object jvalue = getMapValue(map, jname, nameKeys);
					if (jvalue != null) {
						value = jvalue;
						name = jname;
						
						mapper.java = name;
						mapper.json = jname;
						jnameFixed = true;
						
					} else {
						String jname2 = json2Java(mapper.json);
						
						if (jname2 == null) {
							continue;
						} else if (!jname2.equals(name) && !jname2.equals(jname)) {
							jvalue = getMapValue(map, jname2, nameKeys);
							if (jvalue != null) {
								value = jvalue;
								name = jname2;
								
								mapper.java = name;
								mapper.json = jname;
								jnameFixed = true;
								
							}
						}
					}
				}

				if (!jnameFixed) {
					for (String nm: names) {
						jname = json2Java(nm);
						if (jname == null) {
							ignored = true;
							break;
						}
						if (!jname.equals(nm)) {
							Object jvalue = getMapValue(map, jname, nameKeys);
							if (jvalue != null) {
								value = jvalue;
								name = jname;
								
								mapper.java = nm;
								mapper.json = jname;
								jnameFixed = true;
								break;
							} else {
								jvalue = getMapValue(map, nm, nameKeys);
								if (jvalue != null) {
									value = jvalue;
									name = nm;
									
									mapper.java = name;
									mapper.json = name;
									jnameFixed = true;
									break;
								}
							}
							
						} else {
							Object jvalue = getMapValue(map, jname, nameKeys);
							if (jvalue != null) {
								value = jvalue;
								name = jname;
								
								mapper.java = nm;
								mapper.json = jname;
								jnameFixed = true;
								break;
							}
						}
					}
					
					if (ignored) {
						setters.remove(lcfieldName);
						nameKeys.remove(name);
						nameKeys.remove(mapper.json);
						continue;
					}
				}

				
				if (value != null) {
					FieldData objectDTO = new FieldData(obj, f, value, returnType, true, mapper);
					objectDTO.setter = setter;
					value = json2Object(objectDTO);

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
						
					} else if (DefaultValue.isDefault(value)) {
						if (classMapper.defaultType == JSON_INCLUDE.NON_DEFAULT) {
							continue;
						}
					}

					try {
						f.set(obj, value);

					} catch (IllegalAccessException
							| IllegalArgumentException ex) {
						//ex.printStackTrace();
						if (setter != null) {
							try {
								setter.invoke(obj, value);

							} catch (IllegalAccessException
									| IllegalArgumentException | InvocationTargetException exc) {
									//exc.printStackTrace();
								try {
									Statement stmt = new Statement(obj, setter.getName(), new Object[]{value});
									stmt.execute();
								} catch (Exception e) {
									//e.printStackTrace();
								}
							}
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
				
				String name = StringUtil.uncapitalize(setter.getName().substring(3));
				// just use field name, even it might not be a field
				String fieldName = name;

				// in the ignored list
				if (ObjectUtil.inSet(name, classMapper.jsonIgnoreProperties)) {
					nameKeys.remove(name);
					continue;
				}
				
				if (ignoreModifiers(setter.getModifiers(), classMapper.includeFieldsWithModifiers)) {
					nameKeys.remove(name);
					continue;
				}
				
				// 6. Create a blank field mapper instance
				FieldMapper mapper = new FieldMapper(name, name, valueType);
				
				// 7. Classify this field mapper
				mapper = classifyFieldMapper(mapper, classMapper);

				FieldMapper javaFieldMapper = getFieldMapper(name, valueType);


				boolean ignored = false;
				
				Method getter = getters.get(lcfieldName);
				
				Set<String> names = new HashSet<>();
				
				if (annotationSupport) {
					
					annotations = setter.getDeclaredAnnotations();
					
					// no annotations, then try get method
					if ((annotations == null || annotations.length == 0) && getter != null) {
						annotations = getter.getDeclaredAnnotations();
					}

					ca.oson.json.FieldMapper fieldMapperAnnotation = null;
					
					for (Annotation annotation : annotations) {
						if (ignoreField(annotation, classMapper.ignoreFieldsWithAnnotations)) {
							ignored = true;
							break;
						}
						
						if (annotation instanceof ca.oson.json.FieldMapper) {
							fieldMapperAnnotation = (ca.oson.json.FieldMapper) annotation;
							
						} else {
							// to improve performance, using swith on string
							switch (annotation.annotationType().getName()) {
							case "com.fasterxml.jackson.annotation.JsonAnySetter":
							case "org.codehaus.jackson.annotate.JsonAnySetter":
								mapper.jsonAnySetter = true;
								break;
								
							case "javax.persistence.Transient":
								mapper.ignore = true;
								break;
								
							case "com.fasterxml.jackson.annotation.JsonIgnore":
							case "org.codehaus.jackson.annotate.JsonIgnore":
								mapper.ignore = true;
								break;
								
							case "com.fasterxml.jackson.annotation.JsonIgnoreProperties":
								JsonIgnoreProperties jsonIgnoreProperties = (JsonIgnoreProperties) annotation;
								if (!jsonIgnoreProperties.allowSetters()) {
									mapper.ignore = true;
								}
								break;
								
							case "com.google.gson.annotations.Expose":
								Expose expose = (Expose) annotation;
								if (!expose.deserialize()) {
									mapper.ignore = true;
								}
								break;
								
							case "com.google.gson.annotations.Since":
								Since since = (Since) annotation;
								if (since.value() > classMapper.ignoreVersionsAfter) {
									mapper.ignore = true;
								}
								break;
								
							case "com.fasterxml.jackson.annotation.JsonInclude":
								if (mapper.defaultType == JSON_INCLUDE.NONE) {
									JsonInclude jsonInclude = (JsonInclude) annotation;
									
									switch (jsonInclude.content()) {
									case ALWAYS:
										mapper.defaultType = JSON_INCLUDE.ALWAYS;
										break;
									case NON_NULL:
										mapper.defaultType = JSON_INCLUDE.NON_NULL;
										break;
									case NON_ABSENT:
										mapper.defaultType = JSON_INCLUDE.NON_NULL;
										break;
									case NON_EMPTY:
										mapper.defaultType = JSON_INCLUDE.NON_EMPTY;
										break;
									case NON_DEFAULT:
										mapper.defaultType = JSON_INCLUDE.NON_DEFAULT;
										break;
									case USE_DEFAULTS:
										mapper.defaultType = JSON_INCLUDE.DEFAULT;
										break;
									}
								}
								break;
								
							case "com.fasterxml.jackson.annotation.JsonRawValue":
								if (((JsonRawValue) annotation).value()) {
									mapper.jsonRawValue = true;
								}
								break;
								
							case "org.codehaus.jackson.annotate.JsonRawValue":
								if (((org.codehaus.jackson.annotate.JsonRawValue) annotation).value()) {
									mapper.jsonRawValue = true;
								}
								break;
								
							case "javax.persistence.Enumerated":
								mapper.enumType = ((Enumerated) annotation).value();
								break;
								
							case "javax.validation.constraints.NotNull":
								mapper.required = true;
								break;
								
							case "com.fasterxml.jackson.annotation.JsonProperty":
								JsonProperty jsonProperty = (JsonProperty) annotation;
								Access access = jsonProperty.access();
								if (access == Access.READ_ONLY) {
									mapper.ignore = true;
								}
	
								if (jsonProperty.required()) {
									mapper.required = true;
								}
	
								if (mapper.defaultValue == null) {
									mapper.defaultValue = jsonProperty.defaultValue();
								}
								break;
								
							case "javax.validation.constraints.Size":
								Size size = (Size) annotation;
								if (size.min() > 0) {
									mapper.min = size.min();
								}
								if (size.max() < Integer.MAX_VALUE) {
									mapper.max = size.max();
								}
								break;
								
							case "javax.persistence.Column":
								Column column = (Column) annotation;
								if (column.length() != 255) {
									mapper.length = column.length();
								}
								if (column.scale() > 0) {
									mapper.scale = column.scale();
								}
								if (column.precision() > 0) {
									mapper.precision = column.precision();
								}

								if (!column.nullable()) {
									mapper.required = true;
								}
								break;
							}
						
							String fname = ObjectUtil.getName(annotation);
							if (fname != null) {
								names.add(fname);
							}
						}
					}

					// 9. Apply annotations from Oson
					if (fieldMapperAnnotation != null) {
						mapper = overwriteBy (mapper, fieldMapperAnnotation, classMapper);
					}
				}
				
				if (ignored) {
					nameKeys.remove(name);
					nameKeys.remove(mapper.json);
					continue;
				}

				// 10. Apply Java configuration for this particular field
				if (javaFieldMapper != null) {
					mapper = overwriteBy (mapper, javaFieldMapper);
				}
				
				if (mapper.ignore != null && mapper.ignore) {
					nameKeys.remove(name);
					nameKeys.remove(mapper.json);
					continue;
				}
				
				if (mapper.useAttribute != null && !mapper.useAttribute) {
					nameKeys.remove(name);
					nameKeys.remove(mapper.json);
					continue;
				}
				
				if (mapper.jsonAnySetter != null && mapper.jsonAnySetter && setter != null) {
					setters.remove(lcfieldName);
					otherMethods.put(lcfieldName, setter);
					continue;
				}

				
				// get value for name in map
				Object value = getMapValue(map, name, nameKeys);
				
				boolean jnameFixed = false;
				String jname = json2Java(name);

				if (jname == null) {
					continue;
				} else if (!jname.equals(name)) {
					Object jvalue = getMapValue(map, jname, nameKeys);
					if (jvalue != null) {
						value = jvalue;
						name = jname;
						
						mapper.java = name;
						mapper.json = jname;
						jnameFixed = true;
						
					} else {
						String jname2 = json2Java(mapper.json);
						
						if (jname2 == null) {
							continue;
						} else if (!jname2.equals(name) && !jname2.equals(jname)) {
							jvalue = getMapValue(map, jname2, nameKeys);
							if (jvalue != null) {
								value = jvalue;
								name = jname2;
								
								mapper.java = name;
								mapper.json = jname;
								jnameFixed = true;
								
							}
						}
					}
				}

				if (!jnameFixed) {
					for (String nm: names) {
						jname = json2Java(nm);
						if (jname == null) {
							ignored = true;
							break;
						}
						if (!jname.equals(nm)) {
							Object jvalue = getMapValue(map, jname, nameKeys);
							if (jvalue != null) {
								value = jvalue;
								name = jname;
								
								mapper.java = nm;
								mapper.json = jname;
								jnameFixed = true;
								break;
							} else {
								jvalue = getMapValue(map, nm, nameKeys);
								if (jvalue != null) {
									value = jvalue;
									name = nm;
									
									mapper.java = name;
									mapper.json = name;
									jnameFixed = true;
									break;
								}
							}
							
						} else {
							Object jvalue = getMapValue(map, jname, nameKeys);
							if (jvalue != null) {
								value = jvalue;
								name = jname;
								
								mapper.java = nm;
								mapper.json = jname;
								jnameFixed = true;
								break;
							}
						}
					}
					
					if (ignored) {
						setters.remove(lcfieldName);
						nameKeys.remove(name);
						nameKeys.remove(mapper.json);
						continue;
					}
				}

				if (value != null) {
					Class returnType = null;
					Class[] types = setter.getParameterTypes();
					if (types != null && types.length > 0) {
						returnType = types[0];
					}
					
					FieldData objectDTO = new FieldData(obj, null, value, returnType, true, mapper);
					objectDTO.setter = setter;

					value = json2Object(objectDTO);

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
						
					} else if (DefaultValue.isDefault(value)) {
						if (classMapper.defaultType == JSON_INCLUDE.NON_DEFAULT) {
							continue;
						}
					}

					try {
						setter.invoke(obj, value);

					} catch (IllegalAccessException
							| IllegalArgumentException | InvocationTargetException ex) {
							//ex.printStackTrace();
						try {
							Statement stmt = new Statement(obj, setter.getName(), new Object[]{value});
							stmt.execute();
						} catch (Exception e) {
							//e.printStackTrace();
						}
					}
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
						
						if (method.isAnnotationPresent(JsonAnySetter.class)) {
							if (ignoreField(JsonAnySetter.class, classMapper.ignoreFieldsWithAnnotations)) {
								continue;
							}

							jsonAnySetterMethod = method;
							
						} else if (method.isAnnotationPresent(org.codehaus.jackson.annotate.JsonAnySetter.class)) {
							if (ignoreField(org.codehaus.jackson.annotate.JsonAnySetter.class, classMapper.ignoreFieldsWithAnnotations) ) {
								continue;
							}

							jsonAnySetterMethod = method;
							
						} else if (method.isAnnotationPresent(ca.oson.json.FieldMapper.class)) {
							ca.oson.json.FieldMapper annotation = (ca.oson.json.FieldMapper)method.getAnnotation(ca.oson.json.FieldMapper.class);
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

						if (value != null) {
							try {
								jsonAnySetterMethod.setAccessible(true);
								jsonAnySetterMethod.invoke(obj, name, value);
							} catch (InvocationTargetException e) {
								// e.printStackTrace();
								try {
									Statement stmt = new Statement(obj, jsonAnySetterMethod.getName(), new Object[]{name, value});
									stmt.execute();
								} catch (Exception ex) {
									//e.printStackTrace();
								}
							}
						}

					}
				}
			}


			return obj;

			// | InvocationTargetException
		} catch (IllegalAccessException | IllegalArgumentException e) {
			e.printStackTrace();
		}

		return null;
	}


	////////////////////////////////////////////////////////////////////////////////
	// START OF public methods
	////////////////////////////////////////////////////////////////////////////////


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
		JSON_PROCESSOR processor = getJsonProcessor();

		if (processor == JSON_PROCESSOR.JACKSON
				|| processor == JSON_PROCESSOR.GSON) {
			return fromJson(source, valueType);
		}


		try {
			return fromJsonMap(source, valueType, obj);

		} catch (IllegalArgumentException e) {
			//e.printStackTrace();
			throw new RuntimeException(e);
		}

	}

	public <T> T deserialize(String source, Class<T> valueType) {
		JSON_PROCESSOR processor = getJsonProcessor();

		if (processor == JSON_PROCESSOR.JACKSON) {
			try {
				return getJackson().readValue(source, valueType);
			} catch (IOException e) {
				if (getPrintErrorUseOsonInFailure()) {
					e.printStackTrace();
				} else {
					throw new RuntimeException(e);
				}
			}

		} else if (processor == JSON_PROCESSOR.GSON) {
			try {
				return getGson().fromJson(source, valueType);
			} catch (Exception e) {
				if (getPrintErrorUseOsonInFailure()) {
					e.printStackTrace();
				} else {
					throw new RuntimeException(e);
				}
			}
		}

		return fromJsonMap(source, valueType);
	}
	
	public <T> T deserialize(String source, Type type) {
		JSON_PROCESSOR processor = getJsonProcessor();

		if (processor == JSON_PROCESSOR.JACKSON) {
			try {
				return (T) getJackson().readValue(source, new TypeReference<T>(){
				    @Override
				    public Type getType() {
				        return type;
				    }
				}); // ObjectUtil.getTypeClass(type)
			} catch (IOException e) {
				if (getPrintErrorUseOsonInFailure()) {
					e.printStackTrace();
				} else {
					throw new RuntimeException(e);
				}
			}

		} else if (processor == JSON_PROCESSOR.GSON) {
			try {
				return getGson().fromJson(source, type);
			} catch (Exception e) {
				if (getPrintErrorUseOsonInFailure()) {
					e.printStackTrace();
				} else {
					throw new RuntimeException(e);
				}
			}
		}

		return fromJsonMap(source, type);
	}

	public <T> String serialize(T source, Type type) {
		if (source == null) {
			return "";
		}

		JSON_PROCESSOR processor = getJsonProcessor();

		// should not reach here, just for reference
		if (processor == JSON_PROCESSOR.JACKSON) {
			try {
				return getJackson().writeValueAsString(source);
			} catch (JsonProcessingException e) {
				if (getPrintErrorUseOsonInFailure()) {
					e.printStackTrace();
				} else {
					throw new RuntimeException(e);
				}
			}
		} else if (processor == JSON_PROCESSOR.GSON) {
			try {
				return getGson().toJson(source, type);
			} catch (Exception e) {
				if (getPrintErrorUseOsonInFailure()) {
					e.printStackTrace();
				} else {
					throw new RuntimeException(e);
				}
			}
		}

		int level = 0;
		Set set = new HashSet();
		Class<T> valueType = null;

		if (type != null) {
			valueType = ObjectUtil.getTypeClass(type);
		}
		if (valueType == null) {
			valueType = (Class<T>) source.getClass();
		}

		return object2Json(new FieldData(source, valueType, false, type), level, set);
	}
	public <T> String serialize(T source) {
		if (source == null) {
			return "";
		}

		JSON_PROCESSOR processor = getJsonProcessor();

		// should not reach here, just for reference
		if (processor == JSON_PROCESSOR.JACKSON) {
			try {
				return getJackson().writeValueAsString(source);
			} catch (JsonProcessingException e) {
				if (getPrintErrorUseOsonInFailure()) {
					e.printStackTrace();
				} else {
					throw new RuntimeException(e);
				}
			}
		} else if (processor == JSON_PROCESSOR.GSON) {
			try {
				return getGson().toJson(source);
			} catch (Exception e) {
				if (getPrintErrorUseOsonInFailure()) {
					e.printStackTrace();
				} else {
					throw new RuntimeException(e);
				}
			}
		}

		int level = 0;
		Set set = new HashSet();
		Class<T> valueType = (Class<T>) source.getClass();
		
		return object2Json(new FieldData(source, valueType, false), level, set);
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

	/*
	 * Helper classes
	 */
	public static class CollectionArrayTypeGuesser {
		static Set<Class<?>> supers(Class<?> c) {
			if (c == null)
				return new HashSet<Class<?>>();

			Set<Class<?>> s = supers(c.getSuperclass());
			s.add(c);
			return s;
		}

		static Class<?> lowestCommonSuper(Class<?> a, Class<?> b) {
			Set<Class<?>> aSupers = supers(a);
			while (!aSupers.contains(b)) {
				b = b.getSuperclass();
			}
			return b;
		}

		public static <E> Class<E> guessElementType(Collection<E> collection, Class<Collection<E>> valueType, String jsonClassType) {
			if (collection == null) {
				return null;
			}

			Class componentType = null;
			
			if (valueType != null) {
				componentType = valueType.getComponentType();
				
				if (componentType != null) {
					return componentType;
				}
			}
			
			valueType = (Class<Collection<E>>) collection.getClass();

			componentType = valueType.getComponentType();
			if (componentType != null) {
				return componentType;
			}

			componentType = ObjectUtil.getTypeComponentClass(valueType.getGenericSuperclass());
			if (componentType != null) {
				return componentType;
			}

//			TypeVariable<?>[] typeParameters = valueType.getTypeParameters();
//			for (TypeVariable type: valueType.getTypeParameters()) {
//				System.err.println("type.getClass(): ");
//				System.err.println(type.getClass());
//				System.err.println("type.getBounds(): ");
//				System.err.println(Arrays.toString(type.getBounds()));
//			}

			Class<?> guess = null;
			for (Object o : collection) {
				if (o != null) {
					if (guess == null) {
						guess = o.getClass();
					} else if (guess != o.getClass()) {
						guess = lowestCommonSuper(guess, o.getClass());
					}
					
					if (Map.class.isAssignableFrom(guess)) {
						Map m = (Map)o;
						if (m.containsKey(jsonClassType)) {
							String className = (String) m.get(jsonClassType);
							try {
								return (Class<E>) Class.forName(className);
							} catch (ClassNotFoundException e) {
								// e.printStackTrace();
							}
						}

					}
				}
			}
			return (Class<E>)guess;
		}

		public static <E> Class<E> guessElementType(E[] array, Class<E[]> valueType) {
			if (array == null) {
				return null;
			}

			Class componentType = null;
			
			if (valueType != null) {
				componentType = valueType.getComponentType();
				
				if (componentType != null) {
					return componentType;
				}
			}

			valueType = (Class<E[]>) array.getClass();
			
			if (componentType != null) {
				return componentType;
			}

			componentType = ObjectUtil.getTypeComponentClass(valueType.getGenericSuperclass());
			if (componentType != null) {
				return componentType;
			}

//			TypeVariable<?>[] typeParameters = valueType.getTypeParameters();
//			for (TypeVariable type: valueType.getTypeParameters()) {
//				System.err.println("type.getClass(): ");
//				System.err.println(type.getClass());
//				System.err.println("type.getBounds(): ");
//				System.err.println(Arrays.toString(type.getBounds()));
//			}

			Class<?> guess = null;
			for (Object o : array) {
				if (o != null) {
					if (guess == null) {
						guess = o.getClass();
					} else if (guess != o.getClass()) {
						guess = lowestCommonSuper(guess, o.getClass());
					}
				}
			}
			return (Class<E>)guess;
		}
		
		public static final Class<?> getBaseType(Object obj) {
			Class<?> type = obj.getClass();
			while (type.isArray()) {
				type = type.getComponentType();
			}
			return type;
		}
	}

	public static class CopyObjects {
		public static void copy(Object src, Object dest) {
			copy(src, dest, false);
		}

		public static void copy(Object src, Object dest, boolean all) {
			copyFields(src, dest, src.getClass(), all);
		}

		private static void copyFields(Object src, Object dest, Class<?> klass,
				boolean all) {
			Field[] fields = getFields(klass);
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

	public static class StringUtil {
		public static boolean isNull(Object obj) {
			if (obj == null) return true;
			
			if (obj instanceof Optional) {
				Optional<Object> opt = Optional.ofNullable(obj);
				
				if (!opt.isPresent()) {
					return true;
				}
			}
			
			return false;
		}
		
		public static boolean isEmpty(Object obj) {
			if (isNull(obj)) {
				return true;
			}
			
			String str = obj.toString().trim();
			return (str.length() == 0 || str.equals("\"\"") || str.equals("''"));
		}

		
		public static String repeatSpace(int repeat) {
			return repeatChar(SPACE, repeat);
		}

		public static String repeatChar(char c, int repeat) {
			if (repeat < 1) {
				return "";
			}
			return (new String(new char[repeat]).replace('\0', c));
		}

		public static String underscore2CamelCase(String name) {
			int idx = name.indexOf('_');
			if (idx == -1) {
				return name;
			}

			String[] parts = name.split("_");
			String camelCaseString = parts[0];

			for (int i = 1; i < parts.length; i++) {
				camelCaseString = camelCaseString + capitalize(parts[i]);
			}
			return camelCaseString;
		}


		private static String camelCase2Delimiter(String name, char delimiter) {
			if (!name.matches(".*[A-Z].*"))
				return name;

			String regex = "([a-z])([A-Z])";
			String replacement = "$1" + delimiter + "$2";

			return name.replaceAll(regex, replacement);
		}

		public static String camelCase2Underscore(String name) {
			return camelCase2Delimiter(name, '_');
		}

		public static String camelCase2Space(String name) {
			return camelCase2Delimiter(name, ' ');
		}

		public static String camelCase2Dash(String name) {
			return camelCase2Delimiter(name, '-');
		}

		//
		public static String camelCase(String name) {
			String regex = "[_ -]([a-zA-Z])";
			String replacement = "_$1";

			return uncapitalize(underscore2CamelCase(name.replaceAll(regex, replacement)));
		}


		public static String camelCase2UnderscoreLowercase(String name) {
			return camelCase2Underscore(name).toLowerCase().replace('-', '_');
		}


		public static String camelCase2UnderscoreUppercase(String name) {
			return camelCase2Underscore(name).toUpperCase().replace('-', '_');
		}



		public static String capitalize(String name) {
			if (name == null || name.length() == 0) {
				return name;
			}

			return name.substring(0, 1).toUpperCase() + name.substring(1); //.toLowerCase();
		}

		public static String uncapitalize(String name) {
			if (name == null || name.length() == 0) {
				return name;
			}

			char first = name.charAt(0);

			if (first >= 65 && first <= 90) { // uppercase + 32
				return Character.toLowerCase(first) + name.substring(1);
			} else {
				return name;
			}
		}

		public static String escapeDoublequote(String str) {
			if (str == null) {
				return null;
			}
			
			if (str.startsWith("\"") && str.endsWith("\"")) {
				return str.substring(1, str.length()-1);
			}
			
			return str.replaceAll("\"", "\\\\\"");
		}
		public static String escapeDoublequote(Object obj) {
			return escapeDoublequote(obj.toString());
		}
		public static String doublequote(String str) {
			if (str == null) {
				return null;
			}
			
			if (str.startsWith("\"") && str.endsWith("\"")) {
				return str;
			}
			
			return "\"" + str.replaceAll("\"", "\\\\\"") + "\"";
		}
		public static String doublequote(Object obj) {
			return doublequote(obj.toString());
		}

		public static String formatName(String name, FIELD_NAMING format) {
			switch(format) {
			case FIELD: // someField_name
				return name;

			case CAMELCASE: // someFieldName
				return camelCase(name);

			case UPPER_CAMELCASE: // SomeFieldName
				return capitalize(camelCase(name));

			case UNDERSCORE_CAMELCASE: // some_Field_Name
				return camelCase2Underscore(camelCase(name));

			case UNDERSCORE_UPPER_CAMELCASE: // Some_Field_Name
				return capitalize(camelCase2Underscore(camelCase(name)));

			case UNDERSCORE_LOWER: // some_field_name
				return camelCase2Underscore(camelCase(name)).toLowerCase();

			case UNDERSCORE_UPPER: // SOME_FIELD_NAME
				return camelCase2Underscore(camelCase(name)).toUpperCase();

			case SPACE_CAMELCASE: // some Field Name
				return camelCase2Space(camelCase(name));

			case SPACE_UPPER_CAMELCASE: // Some Field Name
				return capitalize(camelCase2Space(camelCase(name)));

			case SPACE_LOWER: // some field name
				return camelCase2Space(camelCase(name)).toLowerCase();

			case SPACE_UPPER: // SOME FIELD NAME
				return camelCase2Space(camelCase(name)).toUpperCase();

			case DASH_CAMELCASE: // some-Field-Name
				return camelCase2Dash(camelCase(name));

			case DASH_UPPER_CAMELCASE: // Some-Field-Name
				return capitalize(camelCase2Dash(camelCase(name)));

			case DASH_LOWER: // some-field-name
				return camelCase2Dash(camelCase(name)).toLowerCase();

			case DASH_UPPER: // SOME-FIELD-NAME
				return camelCase2Dash(camelCase(name)).toUpperCase();
			}

			return name;
		}
		
		
		public static boolean isNumeric(String str) {
			try {
				double d = Double.parseDouble(str);
			} catch (NumberFormatException nfe) {
				return str.matches("-?\\d+(\\.\\d+)?");
			}
			return true;
		}
	}

	public static class ObjectUtil {
		public static boolean isPackage(int modifiers) {
			if (modifiers == 0) {
				return true;
			}
			
			if (Modifier.isPrivate(modifiers) || Modifier.isProtected(modifiers)
					|| Modifier.isPublic(modifiers)) {
				return false;
			}

			return true;
		}
		
		public static void addAnnotationToMethod(String className,
				String methodName, String annotationFullName) throws Exception {
			addAnnotationToMethod(className,
					methodName, annotationFullName, null, null);
		}

		public static void addAnnotationToMethod(String className,
				String methodName, String annotationFullName, String postFix, Map<String, Object> nameValues) throws Exception {

			// pool creation
			ClassPool pool = ClassPool.getDefault();
			// extracting the class
			CtClass cc = pool.getCtClass(className);
			// looking for the method to apply the annotation on
			CtMethod methodDescriptor = cc.getDeclaredMethod(methodName);

	        for (Object obj: methodDescriptor.getAnnotations()) {
	        	Annotation an = (Annotation)obj;

	        	if (an.getClass().getName().equals(annotationFullName)) {
	        		return;
	        	}
	        }

			// create the annotation
			ClassFile ccFile = cc.getClassFile();
			ConstPool constpool = ccFile.getConstPool();
			AnnotationsAttribute attr = new AnnotationsAttribute(constpool,
					AnnotationsAttribute.visibleTag);
			javassist.bytecode.annotation.Annotation annot = new javassist.bytecode.annotation.Annotation(annotationFullName, constpool);

			if (nameValues != null) {
				for (Entry<String, Object> entry: nameValues.entrySet()) {
					String name = entry.getKey();
					Object value = entry.getValue();
					Class<?> returnType = value.getClass();

					if (returnType == String.class) {
						annot.addMemberValue(name, new StringMemberValue(
								(String)value, ccFile.getConstPool()));

					} else if (returnType == Boolean.class || returnType == boolean.class) {
						annot.addMemberValue(entry.getKey(), new BooleanMemberValue(
								(boolean)value, ccFile.getConstPool()));

					} else if (returnType == Character.class || returnType == char.class) {
						annot.addMemberValue(entry.getKey(), new CharMemberValue(
								(char)value, ccFile.getConstPool()));

					} else if (returnType == Long.class || returnType == long.class) {
						annot.addMemberValue(entry.getKey(), new LongMemberValue(
								(long)value, ccFile.getConstPool()));

					} else if (returnType == Integer.class || returnType == int.class) {
						annot.addMemberValue(entry.getKey(), new IntegerMemberValue(
								(int)value, ccFile.getConstPool()));

					} else if (returnType == Double.class || returnType == double.class) {
						annot.addMemberValue(entry.getKey(), new DoubleMemberValue(
								(double)value, ccFile.getConstPool()));

					} else if (returnType == Byte.class || returnType == byte.class) {
						annot.addMemberValue(entry.getKey(), new ByteMemberValue(
								(byte)value, ccFile.getConstPool()));

					} else if (returnType == Short.class || returnType == short.class) {
						annot.addMemberValue(entry.getKey(), new ShortMemberValue(
								(short)value, ccFile.getConstPool()));

					} else if (returnType == Float.class || returnType == float.class) {
						annot.addMemberValue(entry.getKey(), new FloatMemberValue(
								(float)value, ccFile.getConstPool()));

					} else if (returnType.isEnum() || Enum.class.isAssignableFrom(returnType)) {
						annot.addMemberValue(entry.getKey(), new EnumMemberValue(ccFile.getConstPool()));

					} else if (returnType.isArray()) {
						annot.addMemberValue(entry.getKey(), new ArrayMemberValue(ccFile.getConstPool()));

					} else if (Annotation.class.isAssignableFrom(returnType)) {
						annot.addMemberValue(entry.getKey(), new AnnotationMemberValue((javassist.bytecode.annotation.Annotation)value, ccFile.getConstPool()));

					} else if (value instanceof Class) {
						annot.addMemberValue(entry.getKey(), new ClassMemberValue(((Class)value).getName(), ccFile.getConstPool()));

					}
				}
			}

			attr.addAnnotation(annot);
			// add the annotation to the method descriptor
			methodDescriptor.getMethodInfo().addAttribute(attr);

	        if (postFix != null) {
	        	String newClassName = className + postFix;
	        	cc.setName(newClassName);
	        	cc = pool.makeClass(newClassName);
	        	//cc.writeFile();
	        }
			
			// transform the ctClass to java class
			Class dynamiqueBeanClass = cc.toClass();
			//http://stackoverflow.com/questions/23336172/adding-an-annotation-to-a-runtime-generated-class-using-javassist

			// instanciating the updated class
			//dynamiqueBeanClass.newInstance();
		}

	    public static void addAnnotationToField(
	    		String className,
	            String fieldName,
	            String annotationFullName) throws Exception {
	    	
	    	addAnnotationToField(className, fieldName, annotationFullName, null);
	    }
	    
	    public static void addAnnotationToField(
	    		String className,
	            String fieldName,
	            String annotationFullName,
	            String postFix) throws Exception
	    {
			// pool creation
			ClassPool pool = ClassPool.getDefault();
			// extracting the class
			CtClass cc = pool.getCtClass(className);

	        CtField cfield  = cc.getField(fieldName);

	        for (Object obj: cfield.getAnnotations()) {
	        	Annotation an = (Annotation)obj;

	        	if (an.getClass().getName().equals(annotationFullName)) {
	        		return;
	        	}
	        }

	        ClassFile cfile = cc.getClassFile();
	        ConstPool cpool = cfile.getConstPool();

	        AnnotationsAttribute attr =
	                new AnnotationsAttribute(cpool, AnnotationsAttribute.visibleTag);
	        javassist.bytecode.annotation.Annotation annot = new javassist.bytecode.annotation.Annotation(annotationFullName, cpool);

	        //if (cfield.getAnnotation(annot.getClass()) != null) {
		        attr.addAnnotation(annot);
		        cfield.getFieldInfo().addAttribute(attr);
	        //}
		        
		        if (postFix != null) {
		        	String newClassName = className + postFix;
		        	cc.setName(newClassName);
		        	cc = pool.makeClass(newClassName);
		        	//cc.writeFile();
		        }
		        
				// transform the ctClass to java class
				Class dynamiqueBeanClass = cc.toClass(); 
	    }


		public static <E> Class<E> getTypeClass(java.lang.reflect.Type type) {
			//java.util.List<ca.oson.json.test.Dataset>
			String className = type.getTypeName();
			try {
				int idx = className.indexOf('<');
				if (idx > 0) {
					className = className.substring(0, idx);
				}

				return (Class<E>) Class.forName(className);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			Class cl = type.getClass();

			// Collection<String>, return String.class
			if (ParameterizedType.class.isAssignableFrom(cl)) {
				java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) type;

				return (Class<E>) pt.getRawType().getClass();

				// GenericArrayType represents an array type whose component
				// type is either a parameterized type or a type variable.
			} else if (java.lang.reflect.GenericArrayType.class
					.isAssignableFrom(cl)) {
				java.lang.reflect.GenericArrayType pt = (java.lang.reflect.GenericArrayType) type;

				return (Class<E>) pt.getClass();

			} else if (java.lang.reflect.TypeVariable.class
					.isAssignableFrom(cl)) {
				java.lang.reflect.TypeVariable pt = (java.lang.reflect.TypeVariable) type;

				return (Class<E>) pt.getClass();
			}

			return cl;
		}

		public static <E> Class getTypeComponentClass(java.lang.reflect.Type type) {
			//java.util.List<ca.oson.json.test.Dataset>
			String className = type.getTypeName();
			try {
				int idx = className.indexOf('<');
				if (idx > 0) {
					className = className.substring(idx + 1, className.length()-1);
				}

				return (Class<E>) Class.forName(className);
			} catch (ClassNotFoundException e) {
				//e.printStackTrace();
			}

			Class cl = type.getClass();

			// Collection<String>, return String.class
			if (ParameterizedType.class.isAssignableFrom(cl)) {
				java.lang.reflect.ParameterizedType pt = (java.lang.reflect.ParameterizedType) type;

				if (pt.getActualTypeArguments().length > 0) {
					//return pt.getActualTypeArguments()[0].getClass();
					className = pt.getActualTypeArguments()[0].getTypeName();
					try {
						return Class.forName(className);
					} catch (ClassNotFoundException e) {
						// e.printStackTrace();
					}
				}

				// GenericArrayType represents an array type whose component
				// type is either a parameterized type or a type variable.
			} else if (java.lang.reflect.GenericArrayType.class
					.isAssignableFrom(cl)) {
				java.lang.reflect.GenericArrayType pt = (java.lang.reflect.GenericArrayType) type;

				return getTypeClass(pt.getGenericComponentType());

			} else if (java.lang.reflect.TypeVariable.class
					.isAssignableFrom(cl)) {
				java.lang.reflect.TypeVariable pt = (java.lang.reflect.TypeVariable) type;

				java.lang.reflect.Type[] types = pt.getBounds();

				if (types != null && types.length > 0) {
					return getTypeClass(types[0]);
				}
			}


			return null;
		}


		/**
		 * Returns a list containing one parameter name for each argument accepted
		 * by the given constructor. If the class was compiled with debugging
		 * symbols, the parameter names will match those provided in the Java source
		 * code. Otherwise, a generic "arg" parameter name is generated ("arg0" for
		 * the first argument, "arg1" for the second...).
		 *
		 * This method relies on the constructor's class loader to locate the
		 * bytecode resource that defined its class.
		 *
		 * @param constructor
		 * @return
		 * @throws IOException
		 */
		public static List<String> getParameterNames(Constructor<?> constructor) throws IOException {
		    Class<?> declaringClass = constructor.getDeclaringClass();
		    ClassLoader declaringClassLoader = declaringClass.getClassLoader();

		    if (declaringClassLoader == null) {
		    	return null;
		    }

		    org.objectweb.asm.Type declaringType = org.objectweb.asm.Type.getType(declaringClass);
		    String constructorDescriptor = org.objectweb.asm.Type.getConstructorDescriptor(constructor);
		    String url = declaringType.getInternalName() + ".class";

		    InputStream classFileInputStream = declaringClassLoader.getResourceAsStream(url);
		    if (classFileInputStream == null) {
		        // throw new IllegalArgumentException("The constructor's class loader cannot find the bytecode that defined the constructor's class (URL: " + url + ")");
		    	return null;
		    }

		    ClassNode classNode;
		    try {
		        classNode = new ClassNode();
		        ClassReader classReader = new ClassReader(classFileInputStream);
		        classReader.accept(classNode, 0);
		    } finally {
		        classFileInputStream.close();
		    }

		    @SuppressWarnings("unchecked")
		    List<MethodNode> methods = classNode.methods;
		    for (MethodNode method : methods) {
		        if (method.name.equals("<init>") && method.desc.equals(constructorDescriptor)) {
		        	org.objectweb.asm.Type[] argumentTypes = org.objectweb.asm.Type.getArgumentTypes(method.desc);
		            List<String> parameterNames = new ArrayList<String>(argumentTypes.length);

		            @SuppressWarnings("unchecked")
		            List<LocalVariableNode> localVariables = method.localVariables;
		            for (int i = 0; i < argumentTypes.length; i++) {
		                // The first local variable actually represents the "this" object
		                parameterNames.add(localVariables.get(i + 1).name);
		            }

		            return parameterNames;
		        }
		    }

		    return null;
		}

		private static class VariableReader extends EmptyVisitor {
			  private Map<String, Map<Integer, String>> methodParameters =
			    new HashMap<String, Map<Integer, String>>();
			  private String currentMethod;

			  public MethodVisitor visitMethod(int access, String name,
			      String desc, String signature, String[] exceptions) {
			    currentMethod = name + desc;
			    return this;
			  }

			  public void visitLocalVariable(String name, String desc,
			      String signature, Label start, Label end, int index) {
			    Map<Integer, String> parameters = methodParameters.get(currentMethod);
			    if(parameters==null) {
			      parameters = new HashMap<Integer, String>();
			      methodParameters.put(currentMethod, parameters);
			    }
			    parameters.put(index, name);
			  }

			  public Map<Integer, String> getVariableNames(Method m) {
			    return methodParameters.get(m.getName() + org.objectweb.asm.Type.getMethodDescriptor(m));
			  }

			}

		public static String[] getParameterNames(Method m) throws IOException {
		    Class<?> declaringClass = m.getDeclaringClass();
		    String resourceName = "/"+declaringClass.getName().replace('.', '/')+".class";
		    InputStream classData = declaringClass.getResourceAsStream(resourceName);

		    VariableReader variableDiscoverer = new VariableReader();

		    ClassReader r = new ClassReader(classData);
		    r.accept(variableDiscoverer, 0);

		    Map<Integer, String> variableNames = variableDiscoverer.getVariableNames(m);
		    String[] parameterNames = new String[m.getParameterTypes().length];
		    for(int i = 0; i < parameterNames.length; i++) {
		      parameterNames[i] = variableNames.get(i);
		    }
		    return parameterNames;
		  }


		public static String[] getParameterNames(Parameter[] parameters) {
			int length = parameters.length;
			String[] parameterNames = new String[length];

			for (int i = 0; i < length; i++) {
				Parameter parameter = parameters[i];

				String parameterName = null;
				for (Annotation annotation: parameter.getAnnotations()) { //getDeclaredAnnotations
					if (parameterName == null) {
						parameterName = getName(annotation);
					}
				}

				if (parameterName == null) {
					parameterName = parameter.getName();
				}

				parameterNames[i] = parameterName;
			}

			return parameterNames;
		}

		public static boolean isinstanceof(Annotation a, Class<? extends Annotation> c) {
			if (a.annotationType().equals(c)
					|| a.getClass().isAnnotationPresent(c)
					|| c.isAssignableFrom(a.annotationType()))
				return true;

			return false;
		}

		public static String getName(Annotation annotation) {
			switch(annotation.annotationType().getName()) {
			case "ca.oson.json.FieldMapper":
				ca.oson.json.FieldMapper fieldMapper = (ca.oson.json.FieldMapper)annotation;
				return fieldMapper.name();
				
			case "com.fasterxml.jackson.annotation.JsonProperty":
				JsonProperty jsonProperty = (JsonProperty)annotation;
				return jsonProperty.value();
				
			case "com.fasterxml.jackson.annotation.JsonSetter":
				return ((JsonSetter) annotation).value();

			case "org.codehaus.jackson.annotate.JsonSetter":
				return ((org.codehaus.jackson.annotate.JsonSetter) annotation).value();
				
			case "com.google.gson.annotations.SerializedName":
				return ((SerializedName) annotation).value();
				
			case "org.springframework.web.bind.annotation.RequestParam":
				RequestParam requestParam = (RequestParam) annotation;
				String name = requestParam.value();
				if (name == null) {
					name = requestParam.name();
				}
				return name;
				
			case "javax.persistence.Column":
				return ((Column) annotation).name();
				
			case "com.fasterxml.jackson.databind.util.Named":
				return ((com.fasterxml.jackson.databind.util.Named)annotation).getName();
				
			case "com.google.inject.name.Named":
				return ((com.google.inject.name.Named) annotation).value();
				
			case "javax.inject.Named":
				return ((javax.inject.Named)annotation).value();
				
			case "org.codehaus.jackson.map.util.Named":
				return ((org.codehaus.jackson.map.util.Named)annotation).getName();
				
			case "org.codehaus.jackson.annotate.JsonProperty":
				return ((org.codehaus.jackson.annotate.JsonProperty) annotation).value();
			}

			return null;
		}

		/**
		 * Changes the annotation value for the given key of the given annotation to newValue and returns
		 * the previous value.
		 * @author: Balder
		 */
		@SuppressWarnings("unchecked")
		public static Object changeAnnotationValue(Annotation annotation, String key, Object newValue){
		    Object handler = Proxy.getInvocationHandler(annotation);
		    Field f;
		    try {
		        f = handler.getClass().getDeclaredField("memberValues");
		    } catch (NoSuchFieldException | SecurityException e) {
		        throw new IllegalStateException(e);
		    }
		    f.setAccessible(true);
		    Map<String, Object> memberValues;
		    try {
		        memberValues = (Map<String, Object>) f.get(handler);
		    } catch (IllegalArgumentException | IllegalAccessException e) {
		        throw new IllegalStateException(e);
		    }
		    Object oldValue = memberValues.get(key);
		    if (oldValue == null || oldValue.getClass() != newValue.getClass()) {
		        throw new IllegalArgumentException();
		    }
		    memberValues.put(key,newValue);
		    return oldValue;
		}

		public static int hashCode(Object obj) {
			int hash = 7;

			try {
				hash = obj.hashCode();
			} catch (Exception ex) {
				// *
				String str = obj.toString();

				int strlen = str.length();

				if (strlen > 100) {
					strlen = 100;
				}

				for (int i = 0; i < strlen; i++) {
					hash = hash * 31 + str.charAt(i);
				}
				// */
			}

			return hash;
		}

		public static boolean isTypeOf(String myClass, Class<?> superClass) {
			boolean isSubclassOf = false;
			try {
				Class<?> clazz = Class.forName(myClass);
				if (!clazz.equals(superClass)) {
					clazz = clazz.getSuperclass();
					isSubclassOf = isTypeOf(clazz.getName(), superClass);
				} else {
					isSubclassOf = true;
				}

			} catch (ClassNotFoundException e) {
				/* Ignore */
			}
			return isSubclassOf;
		}

		public static boolean inArray(String name, String[] names) {
			if (name == null || names == null) {
				return false;
			}

			return Arrays.binarySearch(names, name) >= 0;
		}
		
		
		public static boolean inSet(String name, Set<String> names) {
			if (name == null || names == null) {
				return false;
			}

			return names.contains(name);
		}
	}

	public static class ArrayToJsonMap {
		public static Map<String, Object> array2Map(Object[] array) {
			Map<String, Object> map = new HashMap<>();

			int length = array.length;

			for (int i = 0; i < length; i++) {
				map.put(array[i].toString(), array[++i]);
			}

			return map;
		}

		public static JSONObject array2Json(Object[] array) {
			JSONObject obj = new JSONObject();

			int length = array.length;

			for (int i = 0; i < length; i++) {
				obj.put(array[i].toString(), array[++i]);
			}

			return obj;
		}

		public static Map<String, Object> json2Map(JSONObject json) {
			Map<String, Object> names = new HashMap<>();
			JSONArray array = json.names();
			int length = array.length();
			for (int i = 0; i < length; i++) {
				String key = array.get(i).toString();
				if (key != null && key.length() > 0) {
					names.put(key.trim(), json.get(key));
				}
			}

			return names;
		}
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
	
	
	public static interface OsonFunction extends Function {
		@Override
		public default Object apply(Object t) {
			return t;
		}
	}
	
	@FunctionalInterface
	public static interface Integer2JsonFunction extends OsonFunction {
		public String apply(Integer t);
	}
	
	@FunctionalInterface
	public static interface Json2IntegerFunction extends OsonFunction {
		public Integer apply(String t);
	}
	
	@FunctionalInterface
	public static interface Long2JsonFunction extends OsonFunction {
		public String apply(Long t);
	}
	
	@FunctionalInterface
	public static interface Json2LongFunction extends OsonFunction {
		public Long apply(String t);
	}
	
	@FunctionalInterface
	public static interface Double2JsonFunction extends OsonFunction {
		public String apply(Double t);
	}
	
	@FunctionalInterface
	public static interface Json2DoubleFunction extends OsonFunction {
		public Double apply(String t);
	}
	
	@FunctionalInterface
	public static interface Short2JsonFunction extends OsonFunction {
		public String apply(Short t);
	}
	
	@FunctionalInterface
	public static interface Json2ShortFunction extends OsonFunction {
		public Short apply(String t);
	}
	
	@FunctionalInterface
	public static interface Float2JsonFunction extends OsonFunction {
		public String apply(Float t);
	}
	
	@FunctionalInterface
	public static interface Json2FloatFunction extends OsonFunction {
		public Float apply(String t);
	}
	
	@FunctionalInterface
	public static interface BigDecimal2JsonFunction extends OsonFunction {
		public String apply(BigDecimal t);
	}
	
	@FunctionalInterface
	public static interface Json2BigDecimalFunction extends OsonFunction {
		public BigDecimal apply(String t);
	}
	
	@FunctionalInterface
	public static interface BigInteger2JsonFunction extends OsonFunction {
		public String apply(BigInteger t);
	}
	
	@FunctionalInterface
	public static interface Json2BigIntegerFunction extends OsonFunction {
		public BigInteger apply(String t);
	}
	
	@FunctionalInterface
	public static interface Character2JsonFunction extends OsonFunction {
		public String apply(Character t);
	}
	
	@FunctionalInterface
	public static interface Json2CharacterFunction extends OsonFunction {
		public Character apply(String t);
	}
	
	@FunctionalInterface
	public static interface Byte2JsonFunction extends OsonFunction {
		public String apply(Byte t);
	}
	
	@FunctionalInterface
	public static interface Json2ByteFunction extends OsonFunction {
		public Byte apply(String t);
	}
	
	@FunctionalInterface
	public static interface Boolean2JsonFunction extends OsonFunction {
		public String apply(Boolean t);
	}
	
	@FunctionalInterface
	public static interface Json2BooleanFunction extends OsonFunction {
		public Boolean apply(String t);
	}
	
	@FunctionalInterface
	public static interface Date2JsonFunction extends OsonFunction {
		public String apply(Date t);
	}
	
	@FunctionalInterface
	public static interface Json2DateFunction extends OsonFunction {
		public Date apply(String t);
	}
	
	@FunctionalInterface
	public static interface Date2LongFunction extends OsonFunction {
		public Long apply(Date t);
	}
	
	@FunctionalInterface
	public static interface Long2DateFunction extends OsonFunction {
		public Date apply(Long t);
	}
	
	@FunctionalInterface
	public static interface Enum2JsonFunction extends OsonFunction {
		public String apply(Enum t);
	}
	
	@FunctionalInterface
	public static interface Json2EnumFunction extends OsonFunction {
		public Enum apply(String t);
	}
	
	@FunctionalInterface
	public static interface Collection2JsonFunction extends OsonFunction {
		public String apply(Collection t);
	}
	
	@FunctionalInterface
	public static interface Json2CollectionFunction extends OsonFunction {
		public Collection apply(String t);
	}
	
	@FunctionalInterface
	public static interface Map2JsonFunction extends OsonFunction {
		public String apply(Map t);
	}
	
	@FunctionalInterface
	public static interface Json2MapFunction extends OsonFunction {
		public Map apply(String t);
	}
	
	@FunctionalInterface
	public static interface Array2JsonFunction extends OsonFunction {
		public String apply(Object[] t);
	}
	
	@FunctionalInterface
	public static interface Json2ArrayFunction extends OsonFunction {
		public Object[] apply(String t);
	}

	@FunctionalInterface
	public static interface AtomicInteger2JsonFunction extends OsonFunction {
		public String apply(AtomicInteger t);
	}
	
	@FunctionalInterface
	public static interface Json2AtomicIntegerFunction extends OsonFunction {
		public AtomicInteger apply(String t);
	}
	
	@FunctionalInterface
	public static interface AtomicLong2JsonFunction extends OsonFunction {
		public String apply(AtomicLong t);
	}
	
	@FunctionalInterface
	public static interface Json2AtomicLongFunction extends OsonFunction {
		public AtomicLong apply(String t);
	}
	
	@FunctionalInterface
	public static interface ClassData2JsonFunction extends OsonFunction {
		public String apply(ClassData t);
	}
	
	@FunctionalInterface
	public static interface Json2ClassDataFunction <T> extends OsonFunction {
		public T apply(String t);
	}
	
}
