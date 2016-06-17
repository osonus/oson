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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
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

import com.fasterxml.jackson.databind.introspect.AnnotatedField;
import com.fasterxml.jackson.databind.introspect.AnnotatedMethod;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonSetter;
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
import com.google.gson.annotations.SerializedName;

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
	public static enum JSON_PROCESSOR {
		JACKSON, // use Jacksopn's implementation
		GSON, // use google's gson implementation
		OSON // Oson Json processor in Java
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

	public static enum DEFAULT_VALUE {
		ALWAYS, NON_NULL, NON_NULL_EMPTY, DEFAULT
	}

	public static enum ANNOTATION_SUPPORT {
		NONE, // no annotation is used, annotation support is disabled
		BASIC, // ignore and notnull will be used
		NAME, // basic, plus use annotation for attribute/field name, of either
			// JsonProperty, SerializedName, or Column, if not exist, use FIELD
		FULL // all the above, plus min value, min length, default value, etc
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
		Volatile
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
		
		public static Date date = Calendar.getInstance().getTime(); // new Date();
		
		public static Date getDate() {
			date = Calendar.getInstance().getTime();
			
			return date;
		}
	}

	// make sure options have non null values
	public static class Options {
		private String simpleDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'";
		public DateFormat dateFormat = new SimpleDateFormat(simpleDateFormat);
		private JSON_PROCESSOR jsonProcessor = JSON_PROCESSOR.OSON;
		private FIELD_NAMING fieldNaming = FIELD_NAMING.FIELD;
		private DEFAULT_VALUE defaultValue = DEFAULT_VALUE.NON_NULL;
		private Boolean prettyPrinting = true;
		private int indentation = 2;
		private ANNOTATION_SUPPORT annotationSupport = ANNOTATION_SUPPORT.BASIC;
		private Boolean orderByKeys = false;
		private Boolean includeClassTypeInJson = false;
		private Boolean printErrorUseOsonInFailure = true;
		private String jsonClassType = "@class";
		private Set<FieldStrategy> fieldStrategies = null;
		private Set<Class> ignoreFieldsWithAnnotations = null;
		private Set<Class> ignoreClassWithAnnotations = null;
		private Set<MODIFIER> includeFieldsWithModifiers = null;
		
		

		public Set<Class> getIgnoreClassWithAnnotations() {
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
		
		
		public Set<Class> getIgnoreFieldsWithAnnotations() {
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
		
		
		public Set<MODIFIER> getIncludeFieldsWithModifiers() {
			return includeFieldsWithModifiers;
		}

		public void includeFieldsWithModifiers(
				Set<MODIFIER> includeFieldsWithModifiers) {
			this.includeFieldsWithModifiers = includeFieldsWithModifiers;
		}
		
		public void includeFieldsWithModifiers(
				MODIFIER[] includeFieldsWithModifiers) {
			this.includeFieldsWithModifiers = new HashSet<MODIFIER>(Arrays.asList(includeFieldsWithModifiers));
		}
		
		public void includeFieldsWithModifier(
				MODIFIER includeFieldsWithModifier) {
			if (this.includeFieldsWithModifiers == null) {
				this.includeFieldsWithModifiers = new HashSet<MODIFIER>();
			}
			
			this.includeFieldsWithModifiers.add(includeFieldsWithModifier);
		}
		
		
		public Set<FieldStrategy> getFieldStrategies() {
			return fieldStrategies;
		}

		public void setFieldStrategies(Set<FieldStrategy> fieldStrategies) {
			for (FieldStrategy fieldStrategy: fieldStrategies) {
				addFieldStrategy(fieldStrategy);
			}
		}

		public void setFieldStrategies(FieldStrategy[] fieldStrategies) {
			for (FieldStrategy fieldStrategy: fieldStrategies) {
				addFieldStrategy(fieldStrategy);
			}
		}

		public void addFieldStrategy(FieldStrategy fieldStrategy) {
			if (!fieldStrategy.isValidFieldStrategy()) {
				return;
			}
			
			if (this.fieldStrategies == null) {
				this.fieldStrategies = new HashSet<FieldStrategy>();
			}
			if (!this.fieldStrategies.contains(fieldStrategy)) {
				this.fieldStrategies.add(fieldStrategy);
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

			if (fieldStrategies == null) {
				return name;
			}

			for (FieldStrategy fieldStrategy: fieldStrategies) {
				String java = fieldStrategy.java;

				if (java != null && lname.equals(java.toLowerCase())) {
					// if is null, ignore it
					return fieldStrategy.json;
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

			if (fieldStrategies == null) {
				return name;
			}

			for (FieldStrategy fieldStrategy: fieldStrategies) {
				String json = fieldStrategy.json;

				if (json != null && lname.equals(json.toLowerCase())) {
					// if is null, ignore it
					return fieldStrategy.java;
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

			if (fieldStrategies == null) {
				return name;
			}

			List<String> names = new ArrayList<>();

			for (FieldStrategy fieldStrategy: fieldStrategies) {
				String java = fieldStrategy.java;

				if (java != null && lname.equals(java.toLowerCase())) {
					Class<?> type = fieldStrategy.type;

					if (type != null) {
						Field fld;
						try {
							fld = type.getDeclaredField(name);

							if (fld != null && fld.equals(field)) {
								return fieldStrategy.json;
							}

						} catch (NoSuchFieldException | SecurityException e) {
							// e.printStackTrace();
						}

					} else {
						names.add(fieldStrategy.json);
					}
				}
			}

			if (names.size() > 0) {
				return names.get(0);
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

			if (fieldStrategies == null) {
				return name;
			}

			List<String> names = new ArrayList<>();

			for (FieldStrategy fieldStrategy: fieldStrategies) {
				String json = fieldStrategy.json;

				if (json != null && lname.equals(json.toLowerCase())) {
					Class<?> type = fieldStrategy.type;

					if (type != null) {
						Field fld;
						try {
							fld = type.getDeclaredField(name);

							if (fld != null && fld.equals(field)) {
								return fieldStrategy.java;
							}

						} catch (NoSuchFieldException | SecurityException e) {
							// e.printStackTrace();
						}

					} else {
						names.add(fieldStrategy.java);
					}
				}
			}

			if (names.size() > 0) {
				return names.get(0);
			}

			return name;
		}


		private Function getJson2JavaFunction(String name, Class enclosingType) {
			if (name == null) {
				return null;
			}
			name = name.trim();
			if (name.isEmpty()) {
				return null;
			}
			String lname = name.toLowerCase();

			if (fieldStrategies == null) {
				return null;
			}

			List<Function> functions = new ArrayList<>();

			for (FieldStrategy fieldStrategy: fieldStrategies) {
				String java = fieldStrategy.java;
				String json = fieldStrategy.json;

				if ((java != null && lname.equals(java.toLowerCase())) ||
						(json != null && lname.equals(json.toLowerCase())) ) {
					Class<?> type = fieldStrategy.type;

					if (fieldStrategy.type == enclosingType) {
						return fieldStrategy.json2JavaFunction;

					} else if (fieldStrategy.json2JavaFunction != null) {
						functions.add(fieldStrategy.json2JavaFunction);
					}
				}
			}

			if (functions.size() > 0) {
				return functions.get(0);
			}

			return null;
		}


		private Function getJava2JsonFunction(String name, Class enclosingType) {
			if (name == null) {
				return null;
			}
			name = name.trim();
			if (name.isEmpty()) {
				return null;
			}
			String lname = name.toLowerCase();

			if (fieldStrategies == null) {
				return null;
			}

			List<Function> functions = new ArrayList<>();

			for (FieldStrategy fieldStrategy: fieldStrategies) {
				String java = fieldStrategy.java;
				String json = fieldStrategy.json;

				if ((java != null && lname.equals(java.toLowerCase())) ||
						(json != null && lname.equals(json.toLowerCase())) ) {
					Class<?> type = fieldStrategy.type;

					if (fieldStrategy.type == enclosingType) {
						return fieldStrategy.java2JsonFunction;

					} else if (fieldStrategy.java2JsonFunction != null) {
						functions.add(fieldStrategy.java2JsonFunction);
					}
				}
			}

			if (functions.size() > 0) {
				return functions.get(0);
			}

			return null;
		}


		public int getIndentation() {
			return indentation;
		}

		public void setIndentation(int indentation) {
			if (indentation >= 0 && indentation < 10) {
				this.indentation = indentation;
			}
		}

		public String getSimpleDateFormat() {
			return simpleDateFormat;
		}

		public void setSimpleDateFormat(String simpleDateFormat) {
			if (simpleDateFormat != null) {
				this.simpleDateFormat = simpleDateFormat;

				this.dateFormat = new SimpleDateFormat(simpleDateFormat);
			}
		}

		public DateFormat getDateFormat() {
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

		public DEFAULT_VALUE getDefaultValue() {
			return defaultValue;
		}

		public void setDefaultValue(DEFAULT_VALUE defaultValue) {
			if (defaultValue != null) {
				this.defaultValue = defaultValue;
			}
		}

		public Boolean getPrettyPrinting() {
			return prettyPrinting;
		}

		public void setPrettyPrinting(Boolean prettyPrinting) {
			if (prettyPrinting != null) {
				this.prettyPrinting = prettyPrinting;
			}
		}

		public JSON_PROCESSOR getJsonProcessor() {
			return jsonProcessor;
		}

		public void setJsonProcessor(JSON_PROCESSOR jsonProcessor) {
			if (jsonProcessor != null) {
				this.jsonProcessor = jsonProcessor;
			}
		}

		public ANNOTATION_SUPPORT getAnnotationSupport() {
			return annotationSupport;
		}

		public void setAnnotationSupport(ANNOTATION_SUPPORT annotationSupport) {
			this.annotationSupport = annotationSupport;
		}

		public Boolean getOrderByKeys() {
			return orderByKeys;
		}

		public void setOrderByKeys(Boolean orderByKeys) {
			this.orderByKeys = orderByKeys;
		}

		public Boolean getIncludeClassTypeInJson() {
			return includeClassTypeInJson;
		}

		public void setIncludeClassTypeInJson(Boolean includeClassTypeInJson) {
			this.includeClassTypeInJson = includeClassTypeInJson;
		}

		public Boolean getPrintErrorUseOsonInFailure() {
			return printErrorUseOsonInFailure;
		}

		public void setPrintErrorUseOsonInFailure(
				Boolean printErrorUseOsonInFailure) {
			this.printErrorUseOsonInFailure = printErrorUseOsonInFailure;
		}

		public String getJsonClassType() {
			return jsonClassType;
		}

		public void setJsonClassType(String jsonClassType) {
			this.jsonClassType = jsonClassType;
		}

		public FIELD_NAMING getFieldNaming() {
			return fieldNaming;
		}

		public void setFieldNaming(FIELD_NAMING fieldNaming) {
			this.fieldNaming = fieldNaming;
		}

	}

	public static class FieldStrategy<E, T, R> {
		/*
		 * type for the class enclosing the field, should be in full path format
		 * such as ca.oson.json.Oson
		 * for modifying class object before loading
		 */
		private Class<E> type = null;
		public String className;
		
		/*
		 * java: java field name. json: json property name
		 * There are 5 cases for naming:
		 * case 1, if java is null, json value is ignored during deserialization;
		 * 2, if json is null, java field is ignored during serializatio;
		 * 3. if java and json is the same, no change of naming;
		 * 4. if different, serialization: map java field to json property;
		 * 5. deserialization: map json property to java field
		 *
		 */
		public String java;
		public String json;
		
		/*
		 * a flag to indicate being annotated or mixined
		 */
		private boolean processed = false;
		private boolean isProcessed() {
			return processed;
		}
		private void setProcessed(boolean processed) {
			this.processed = processed;
		}

		/*
		 * The java function that transforms the field value to json property value during serialization
		 */
		public Function<T, R> java2JsonFunction = null;

		/*
		 * The json function that reverses back the property value to java field value
		 * during deserialization
		 */
		public Function<R, T> json2JavaFunction = null;

		public boolean isValidFieldStrategy() {
			if (className == null && java == null && json == null) {
				return false;
			}
			
			return true;
		}
		
		public FieldStrategy (String java, String json) {
			if (java != null) {
				this.java = java.trim();
				if (this.java.length() == 0) {
					this.java = null;
				}
			}
			if (json != null) {
				this.json = json.trim();
				if (this.json.length() == 0) {
					this.json = null;
				}
			}
		}

		public FieldStrategy (String java, String json, String className) {
			this(java, json);
			if (className != null) {
				this.className = className.trim();
				if (this.className.length() == 0) {
					this.className = null;
				}
			}
		}

		public FieldStrategy (String java, String json, String className,
				Function<T, R> java2JsonFunction, Function<R, T> json2JavaFunction) {
			this(java, json, className);
			this.java2JsonFunction = java2JsonFunction;
			this.json2JavaFunction = json2JavaFunction;
		}
		
		
		public FieldStrategy (String java, String json, String className,
				Function<T, R> java2JsonFunction) {
			this(java, json, className);
			this.java2JsonFunction = java2JsonFunction;
		}
		
		public Class<E> getType() {
			if (type != null) {
				return type;
			}
			
			if (className != null) {
				try {
					type = (Class<E>) Class.forName(className);
				} catch (ClassNotFoundException e) {
					// e.printStackTrace();
				}
			}
			
			return type;
		}
	}

	private static class FieldData<T, E> {
		public T enclosingObj;
		public Field field;
		public String defaultName;
		public Object valueToProcess;
		public Class<E> returnType;
		public E returnObj = null;
		public Type erasedType = null;
		public boolean json2Java = true;
		public Method getter;

		public EnumType enumType = null;
		public boolean notNull = false;// not nullable
		public Integer length = null; // Default: 255
		public Integer scale = null; // Default: 0
		public Integer min = null; // default (int) 0;
		public Integer max = null; // default (int) 2147483647;
		public Object defaultValue = null; // default ""

		public FieldData(T object, Field field, String defaultName, Object value,
				Class<E> returnType, EnumType enumType, Boolean notNull,
				Integer length, Integer scale, Integer min,
				Integer max, Object defaultValue, boolean json2Java) {
			this(value, returnType, enumType);
			this.enclosingObj = object; // enclosing object
			this.field = field;
			this.defaultName = defaultName;
			this.notNull = notNull;
			this.length = length;
			this.scale = scale;
			this.min = min;
			this.max = max;
			this.defaultValue = defaultValue;
			this.json2Java = json2Java;
		}

		public FieldData(Object value, Class<E> returnType, EnumType enumType) {
			this(value, returnType);
			this.enumType = enumType;
		}

		public FieldData(Object value, Class<E> returnType) {
			this.valueToProcess = value;
			this.returnType = returnType;
		}

		public FieldData(Object value, Class<E> returnType, Boolean notNull) {
			this(value, returnType);
			this.notNull = notNull;
		}

		public FieldData(T object, Object value, Class<E> returnType) {
			this(value, returnType);
			this.enclosingObj = object;
		}

		public FieldData(T object, Object value, Class<E> returnType, E obj, boolean json2Java) {
			this(object, value, returnType);
			this.returnObj = obj;
			this.json2Java = json2Java;
		}

		public FieldData(T object, Object value, Type type, boolean json2Java) {
			this.valueToProcess = value; // value to interpret
			this.enclosingObj = object; // enclosing object
			this.erasedType = type; // generic type information
			this.json2Java = json2Java;
		}

		public FieldData(T object, Object value, E obj) {
			this.valueToProcess = value; // value to interpret
			this.enclosingObj = object; // enclosing object
			this.returnObj = obj; // object to return
		}


		public String getDefaultName() {
			if (defaultName == null) {
				if (field != null) {
					defaultName = field.getName();
				}
			}

			return defaultName;
		}


		public Class getEnclosingType() {
			Class enclosingtype = null;
			if (enclosingObj != null) {
				enclosingtype = enclosingObj.getClass();
			}
			if (enclosingtype == null && erasedType != null) {
				enclosingtype = ObjectUtil.getTypeClass(erasedType);
			}

			return enclosingtype;
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
					}
				}
			}

			return defaultValue;
		}
	}

	//private static Oson converter = null;

	private Options options = new Options();
	private ObjectMapper mapper = null;
	private Gson gson = null;

	private static Map<Class, Field[]> cachedFields = new ConcurrentHashMap<>();
	private static Map<Class, Method[]> cachedSetters = new ConcurrentHashMap<>();
	private static Map<Class, Method[]> cachedGetters = new ConcurrentHashMap<>();

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
		Options options = this.fromMap(map, Options.class);

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

	private DEFAULT_VALUE getDefaultValue() {
		return options.getDefaultValue();
	}

	public Oson setDefaultValue(DEFAULT_VALUE defaultValue) {
		if (defaultValue != null) {
			options.setDefaultValue(defaultValue);
			reset();
		}

		return this;
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

	public Oson setPrettyPrinting(Boolean prettyPrinting) {
		if (prettyPrinting != null) {
			options.setPrettyPrinting(prettyPrinting);
			reset();
		}

		return this;
	}

	private String getPrettySpace() {
		if (getPrettyPrinting()) {
			return String.valueOf(SPACE);
		}

		return "";
	}
	private String getPrettyIndentation(int level) {
		if (options.getPrettyPrinting()) {
			return StringUtil.repeatSpace(level * getIndentation());
		}

		return "";
	}
	private String getPrettyIndentationln(int level) {
		if (options.getPrettyPrinting()) {
			return "\n" + StringUtil.repeatSpace(level * getIndentation());
		}

		return "";
	}

	private ANNOTATION_SUPPORT getAnnotationSupport() {
		return options.getAnnotationSupport();
	}

	public Oson setAnnotationSupport(ANNOTATION_SUPPORT annotationSupport) {
		if (annotationSupport != null) {
			options.setAnnotationSupport(annotationSupport);
			reset();
		}

		return this;
	}

	private int getIndentation() {
		return options.getIndentation();
	}

	public Oson setIndentation(int indentation) {
		if (indentation != getIndentation() && indentation > 0) {
			options.setIndentation(indentation);
			reset();
		}

		return this;
	}

	private Set<FieldStrategy> getFieldStrategies() {
		return options.fieldStrategies;
	}

	public Oson setFieldStrategies(FieldStrategy[] fieldStrategies) {
		options.setFieldStrategies(fieldStrategies);

		return this;
	}

	public Oson addFieldStrategies(FieldStrategy fieldStrategy) {
		options.addFieldStrategy(fieldStrategy);

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
		options.includeFieldsWithModifier(includeFieldsWithModifier);

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

	private Function getJava2JsonFunction(String defaultName, Class enclosingType) {
		return options.getJava2JsonFunction(defaultName, enclosingType);
	}

	private Function getJson2JavaFunction(String defaultName, Class enclosingType) {
		return options.getJson2JavaFunction(defaultName, enclosingType);
	}
	
	private boolean ignoreClass(Class valueType) {
		Set<FieldStrategy> fieldStrategies = getFieldStrategies();
		if (fieldStrategies == null || valueType == null) {
			return false;
		}
		
		for (FieldStrategy fieldStrategy: fieldStrategies) {
			if (fieldStrategy.java == null && fieldStrategy.json == null
					&& valueType == fieldStrategy.getType()) {
				return true;
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
	

	private boolean ignoreField(Annotation annotation) {
		Set<Class> annotations = getIgnoreFieldsWithAnnotations();
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

	
	private boolean ignoreModifiers(int modifiers) {
		Set<MODIFIER> includeFieldsWithModifiers = getIncludeFieldsWithModifiers();
		if (includeFieldsWithModifiers == null || includeFieldsWithModifiers.size() == 0) {
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
	

	private Boolean getOrderByKeys() {
		return options.getOrderByKeys();
	}

	public Oson setOrderByKeys(Boolean orderByKeys) {
		if (orderByKeys != null) {
			options.setOrderByKeys(orderByKeys);
			reset();
		}

		return this;
	}

	private Boolean getIncludeClassTypeInJson() {
		return options.getIncludeClassTypeInJson();
	}

	public Oson setIncludeClassTypeInJson(Boolean includeClassTypeInJson) {
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


	private void reset() {
		mapper = null;
		gson = null;
	}

	private ObjectMapper getJackson() {
		if (mapper == null) {
			prefixProcessing();
			
			mapper = new ObjectMapper();
			// do not fail for funny reason
			mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
			mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

			switch (getDefaultValue()) {
			case ALWAYS:
				mapper.setSerializationInclusion(Include.ALWAYS);
				break;
			case NON_NULL:
				mapper.setSerializationInclusion(Include.NON_NULL);
				break;
			case NON_NULL_EMPTY:
				mapper.setSerializationInclusion(Include.NON_EMPTY);
				break;
			case DEFAULT:
				mapper.setSerializationInclusion(Include.USE_DEFAULTS);
				break;
			default:
				mapper.setSerializationInclusion(Include.NON_NULL);
				break;
			}

			if (getPrettyPrinting()) {
				mapper.enable(SerializationFeature.INDENT_OUTPUT);
				mapper.configure(SerializationFeature.INDENT_OUTPUT, true);
			}

			if (getOrderByKeys()) {
				mapper.configure(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS, true);
			}

			Set<FieldStrategy> fieldStrategies = getFieldStrategies();

			if (fieldStrategies != null) {
				//defines how names of JSON properties ("external names") are derived from names of POJO methods and fields ("internal names"),
				// in cases where they are not auto-detected and no explicit annotations exist for naming
				// config - Configuration in used: either SerializationConfig or DeserializationConfig,
				// depending on whether method is called during serialization or deserialization
				mapper.setPropertyNamingStrategy(new PropertyNamingStrategy() {
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
							for (FieldStrategy fieldStrategy: fieldStrategies) {
								String java = fieldStrategy.java;

								if (java != null && lname.equals(java.toLowerCase())) {
									Class<?> type = fieldStrategy.getType();
									String fullName = field.getFullName(); // ca.oson.json.Artist#age;
									int index = fullName.indexOf('#');
									if (index > -1) {
										fullName = fullName.substring(0, index);
									}
									
									if (type != null) {
										try {
											// fieldStrategy.type.getTypeName() is the same as fieldStrategy.type.getName()
											if (type.getTypeName().equals(fullName)) {
												return fieldStrategy.json;
											}

										} catch (SecurityException e) {
											// e.printStackTrace();
										}

									} else { // just by name
										return fieldStrategy.json;
									}
								}
							}

						}

						if (config instanceof DeserializationConfig || !(config instanceof SerializationConfig)) {
							for (FieldStrategy fieldStrategy: fieldStrategies) {
								String json = fieldStrategy.json;

								if (json != null && lname.equals(json.toLowerCase())) {
									Class<?> type = fieldStrategy.getType();
									String fullName = field.getFullName(); // ca.oson.json.Artist#age;
									int index = fullName.indexOf('#');
									if (index > -1) {
										fullName = fullName.substring(0, index);
									}
									
									if (type != null) {
										try {
											// fieldStrategy.type.getTypeName() is the same as fieldStrategy.type.getName()
											if (type.getTypeName().equals(fullName)) {
												return fieldStrategy.java;
											}

										} catch (SecurityException e) {
											// e.printStackTrace();
										}

									} else { // just by name
										return fieldStrategy.java;
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
							for (FieldStrategy fieldStrategy: fieldStrategies) {
								String java = fieldStrategy.java;

								if (java != null && lname.equals(java.toLowerCase())) {
									Class<?> type = fieldStrategy.getType();
									String fullName = method.getFullName(); // java.util.Date#getTime(0 params)
									int index = fullName.indexOf('#');
									if (index > -1) {
										fullName = fullName.substring(0, index);
									}
									
									if (type != null) {
										try {
											// fieldStrategy.type.getTypeName() is the same as fieldStrategy.type.getName()
											if (type.getTypeName().equals(fullName)) {
												return fieldStrategy.json;
											}

										} catch (SecurityException e) {
											// e.printStackTrace();
										}

									} else { // just by name
										return fieldStrategy.json;
									}
								}
							}

						}


						if (config instanceof DeserializationConfig || !(config instanceof SerializationConfig)) {
							for (FieldStrategy fieldStrategy: fieldStrategies) {
								String json = fieldStrategy.json;

								if (json != null && lname.equals(json.toLowerCase())) {
									Class<?> type = fieldStrategy.getType();
									String fullName = method.getFullName(); // java.util.Date#getTime(0 params)
									int index = fullName.indexOf('#');
									if (index > -1) {
										fullName = fullName.substring(0, index);
									}
									
									if (type != null) {
										try {
											// fieldStrategy.type.getTypeName() is the same as fieldStrategy.type.getName()
											if (type.getTypeName().equals(fullName)) {
												return fieldStrategy.java;
											}

										} catch (SecurityException e) {
											// e.printStackTrace();
										}

									} else { // just by name
										return fieldStrategy.java;
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
							for (FieldStrategy fieldStrategy: fieldStrategies) {
								String java = fieldStrategy.java;

								if (java != null && lname.equals(java.toLowerCase())) {
									Class<?> type = fieldStrategy.getType();
									String fullName = method.getFullName(); // java.util.Date#getTime(0 params)
									int index = fullName.indexOf('#');
									if (index > -1) {
										fullName = fullName.substring(0, index);
									}
									
									if (type != null) {
										try {
											// fieldStrategy.type.getTypeName() is the same as fieldStrategy.type.getName()
											if (type.getTypeName().equals(fullName)) {
												return fieldStrategy.json;
											}

										} catch (SecurityException e) {
											// e.printStackTrace();
										}

									} else { // just by name
										return fieldStrategy.json;
									}
								}
							}

						}


						if (config instanceof DeserializationConfig || !(config instanceof SerializationConfig)) {
							for (FieldStrategy fieldStrategy: fieldStrategies) {
								String json = fieldStrategy.json;

								if (json != null && lname.equals(json.toLowerCase())) {
									Class<?> type = fieldStrategy.getType();
									String fullName = method.getFullName(); // java.util.Date#getTime(0 params)
									int index = fullName.indexOf('#');
									if (index > -1) {
										fullName = fullName.substring(0, index);
									}
									
									if (type != null) {
										try {
											// fieldStrategy.type.getTypeName() is the same as fieldStrategy.type.getName()
											if (type.getTypeName().equals(fullName)) {
												return fieldStrategy.java;
											}

										} catch (SecurityException e) {
											// e.printStackTrace();
										}

									} else { // just by name
										return fieldStrategy.java;
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
							for (FieldStrategy fieldStrategy: fieldStrategies) {
								String java = fieldStrategy.java;

								if (java != null && lname.equals(java.toLowerCase())) {
									Class<?> type = fieldStrategy.getType();
									String fullName = ctorParam.getName(); // java.util.Date#
									int index = fullName.indexOf('#');
									if (index > -1) {
										fullName = fullName.substring(0, index);
									}
									
									if (type != null) {
										try {
											// fieldStrategy.type.getTypeName() is the same as fieldStrategy.type.getName()
											if (type.getTypeName().equals(fullName)) {
												return fieldStrategy.json;
											}

										} catch (SecurityException e) {
											// e.printStackTrace();
										}

									} else { // just by name
										String json = fieldStrategy.json;

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
							for (FieldStrategy fieldStrategy: fieldStrategies) {
								String json = fieldStrategy.json;

								if (json != null && lname.equals(json.toLowerCase())) {
									Class<?> type = fieldStrategy.getType();
									String fullName = ctorParam.getName(); // java.util.Date#
									int index = fullName.indexOf('#');
									if (index > -1) {
										fullName = fullName.substring(0, index);
									}
									
									if (type != null) {
										try {
											// fieldStrategy.type.getTypeName() is the same as fieldStrategy.type.getName()
											if (type.getTypeName().equals(fullName)) {
												return fieldStrategy.java;
											}

										} catch (SecurityException e) {
											// e.printStackTrace();
										}

									} else { // just by name
										return fieldStrategy.java;
									}
								}
							}

						}

						return super.nameForConstructorParameter(config, ctorParam, defaultName);
					}

				});
			}



			mapper.setDateFormat(getDateFormat());
		}



		return mapper;
	}

	
	private void prefixProcessing() {
		if (getJsonProcessor() != JSON_PROCESSOR.JACKSON) {
			return;
		}
		
		Set<FieldStrategy> fieldStrategies = getFieldStrategies();
		if (fieldStrategies == null) {
			return;
		}
		
		for (FieldStrategy fieldStrategy: fieldStrategies) {
			if (fieldStrategy.isProcessed()) {
				continue;
			}
			
			String json = fieldStrategy.json;
			String java = fieldStrategy.java;
			String className = fieldStrategy.className;
			if (json == null || java == null) {
				continue;
			}
			
			if (className != null) {
				if (json == null) {
					// serialize
					// String methodName = "get" + StringUtil.capitalize(java);
					try {
						ObjectUtil.addAnnotationToField(className, java,
						        "com.fasterxml.jackson.annotation.JsonIgnore");
						fieldStrategy.setProcessed(true);
					} catch (Exception e) {
						e.printStackTrace();
						if (!fieldStrategy.isProcessed()) {
							// should ignore it, have to use mixin abstract class
							try {
								ObjectUtil.addAnnotationToField(className, java,
								        "com.fasterxml.jackson.annotation.JsonIgnore", mixin);

								mapper.addMixInAnnotations(fieldStrategy.getClass(), Class.forName(className + mixin));
								
								fieldStrategy.setProcessed(true);
								
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
						fieldStrategy.setProcessed(true);
					} catch (Exception e) {
						e.printStackTrace();
						if (!fieldStrategy.isProcessed()) {
							// should ignore it, have to use mixin abstract class
							try {
								ObjectUtil.addAnnotationToField(className, json,
								        "com.fasterxml.jackson.annotation.JsonIgnore", mixin);

								mapper.addMixInAnnotations(fieldStrategy.getClass(), Class.forName(className + mixin));
								
								fieldStrategy.setProcessed(true);
								
							} catch (Exception ex) {
								ex.printStackTrace();
							}
							
						}
					}
				}
			}

		}
	}

	
	private Gson getGson() {
		if (gson == null) {
			GsonBuilder gsonBuilder = new GsonBuilder();

			switch (getDefaultValue()) {
			case ALWAYS:
				gsonBuilder.serializeNulls();
				break;
			case NON_NULL:
				break;
			case NON_NULL_EMPTY:
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


			if (getPrettyPrinting()) {
				gsonBuilder.setPrettyPrinting();
			}

			gsonBuilder.setDateFormat(options.getSimpleDateFormat());

			Set<FieldStrategy> fieldStrategies = getFieldStrategies();
			
			if (fieldStrategies != null) {
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
				
				List<ExclusionStrategy> strategies = new ArrayList<>();
				for (FieldStrategy fieldStrategy: fieldStrategies) {
					if (fieldStrategy.java == null || fieldStrategy.json == null) {
						strategies.add(new ExclusionStrategy() {

							@Override
							public boolean shouldSkipField(FieldAttributes f) {
								String name = f.getName();
								Class cls = f.getClass();
								
								if (fieldStrategy.java == null) {
									if (fieldStrategy.json == null) {
										if (cls.equals(fieldStrategy.getType())) {
											return true;
										}
									
									} else if (fieldStrategy.json.equals(name)) {
										if (fieldStrategy.getType() == null || cls.equals(fieldStrategy.getType())) {
											return true;
										}
									}
									
								} else if (fieldStrategy.json == null) {
									if (fieldStrategy.java.equals(name)) {
										if (fieldStrategy.getType() == null || cls.equals(fieldStrategy.getType())) {
											return true;
										}
									}
								}
								
								return false;
							}

							@Override
							public boolean shouldSkipClass(Class<?> clazz) {
								if (fieldStrategy.java == null && fieldStrategy.json == null && clazz.equals(fieldStrategy.getType())) {
									return true;
								}
								return false;
							}});
					}
				}
				
				gsonBuilder.setExclusionStrategies(strategies.toArray(new ExclusionStrategy[strategies.size()]));
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

	private <E> Object getDouble(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean notNull = objectDTO.notNull;
		Integer min = objectDTO.min;
		Integer max = objectDTO.max;
		Integer scale = objectDTO.scale;
		boolean json2Java = objectDTO.json2Java;

		if (value != null && value.toString().trim().length() > 0) {
			Function function = null;
			String str = value.toString().trim();
			try {
				Double valueToReturn = null;
				if (value instanceof Double) {
					valueToReturn = (Double)value;
				} else {
					valueToReturn = Double.valueOf(str);
				}

				if (json2Java) {
					function = getJson2JavaFunction(objectDTO.getDefaultName(), objectDTO.getEnclosingType());
					if (function != null) {
						valueToReturn = (Double)function.apply(valueToReturn);
					}
					
				} else {
					function = getJava2JsonFunction(objectDTO.getDefaultName(), objectDTO.getEnclosingType());
					if (function != null) {
						return function.apply(valueToReturn);
					}
				}
	
				if (min != null && min > valueToReturn.doubleValue()) {
					valueToReturn = min.doubleValue();
					
				} else if (max != null && valueToReturn.doubleValue() > max) {
					valueToReturn = max.doubleValue();
				}
				
				if (scale != null) {
					valueToReturn = new BigDecimal(valueToReturn).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
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
		
		if (returnType == double.class
				|| getDefaultValue() == DEFAULT_VALUE.DEFAULT || notNull) {
			Double defaultValue = (Double)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				if (min != null && min > defaultValue.doubleValue()) {
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

	private <E> Object getFloat(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean notNull = objectDTO.notNull;
		Integer min = objectDTO.min;
		Integer max = objectDTO.max;
		Integer scale = objectDTO.scale;
		boolean json2Java = objectDTO.json2Java;

		if (value != null && value.toString().trim().length() > 0) {
			Function function = null;
			String str = value.toString().trim();
			try {
				Float valueToReturn = null;
				if (value instanceof Float) {
					valueToReturn = (Float)value;
				} else {
					valueToReturn = Float.valueOf(str);
				}

				if (json2Java) {
					function = getJson2JavaFunction(objectDTO.getDefaultName(), objectDTO.getEnclosingType());
					if (function != null) {
						valueToReturn = (Float)function.apply(valueToReturn);
					}
					
				} else {
					function = getJava2JsonFunction(objectDTO.getDefaultName(), objectDTO.getEnclosingType());
					if (function != null) {
						return function.apply(valueToReturn);
					}
				}
	
				if (min != null && min > valueToReturn.floatValue()) {
					valueToReturn = min.floatValue();
					
				} else if (max != null && valueToReturn.floatValue() > max) {
					valueToReturn = max.floatValue();
				}
				
				if (scale != null) {
					valueToReturn = new BigDecimal(valueToReturn).setScale(scale, BigDecimal.ROUND_HALF_UP).floatValue();
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
		
		if (returnType == float.class
				|| getDefaultValue() == DEFAULT_VALUE.DEFAULT || notNull) {
			Float defaultValue = (Float)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				if (min != null && min > defaultValue.floatValue()) {
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

	private <E> Object getBigDecimal(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean notNull = objectDTO.notNull;
		Integer min = objectDTO.min;
		Integer max = objectDTO.max;
		Integer scale = objectDTO.scale;
		boolean json2Java = objectDTO.json2Java;

		if (value != null && value.toString().trim().length() > 0) {
			Function function = null;
			String str = value.toString().trim();
			try {
				BigDecimal valueToReturn = null;
				if (value instanceof BigDecimal) {
					valueToReturn = (BigDecimal)value;
				} else {
					valueToReturn = new BigDecimal(str);
				}

				if (json2Java) {
					function = getJson2JavaFunction(objectDTO.getDefaultName(), objectDTO.getEnclosingType());
					if (function != null) {
						valueToReturn = (BigDecimal)function.apply(valueToReturn);
					}
					
				} else {
					function = getJava2JsonFunction(objectDTO.getDefaultName(), objectDTO.getEnclosingType());
					if (function != null) {
						return function.apply(valueToReturn);
					}
				}
				
				if (min != null && min > valueToReturn.intValue()) {
					valueToReturn = BigDecimal.valueOf(min);
				}
				
				if (max != null && valueToReturn.compareTo(BigDecimal.valueOf(max)) > 0) {
					valueToReturn = BigDecimal.valueOf(max);
				}
				
				if (scale != null) {
					valueToReturn.setScale(scale, BigDecimal.ROUND_HALF_EVEN);
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
		
		if (getDefaultValue() == DEFAULT_VALUE.DEFAULT || notNull) {
			BigDecimal defaultValue = (BigDecimal)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				if (min != null && min > defaultValue.longValue()) {
					return BigDecimal.valueOf(min);
				}

				return defaultValue;
			}


			if (min != null && DefaultValue.bigDecimal.compareTo(BigDecimal.valueOf(min)) < 0) {
				return BigDecimal.valueOf(min);
			}

			return DefaultValue.bigDecimal;
		}

		return null;
	}

	private <E> Object getBigInteger(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean notNull = objectDTO.notNull;
		
		Integer min = objectDTO.min;
		Integer max = objectDTO.max;
		boolean json2Java = objectDTO.json2Java;

		if (value != null && value.toString().trim().length() > 0) {
			Function function = null;
			String str = value.toString().trim();
			try {
				BigInteger valueToReturn = null;
				if (value instanceof BigInteger) {
					valueToReturn = (BigInteger)value;
				} else {
					valueToReturn = new BigInteger(str);
				}
	
				// processing
				if (json2Java) {
					function = getJson2JavaFunction(objectDTO.getDefaultName(), objectDTO.getEnclosingType());
					if (function != null) {
						valueToReturn = (BigInteger)function.apply(valueToReturn);
					}
					
				} else {
					function = getJava2JsonFunction(objectDTO.getDefaultName(), objectDTO.getEnclosingType());
					if (function != null) {
						return function.apply(valueToReturn);
					}
				}
				
				if (min != null && min > valueToReturn.longValue()) {
					return BigInteger.valueOf(min.intValue());
				}
	
				if (max != null && valueToReturn.compareTo(BigInteger.valueOf(max)) > 0) {
					valueToReturn = BigInteger.valueOf(max);
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
		
		
		if (getDefaultValue() == DEFAULT_VALUE.DEFAULT || notNull) {
			BigInteger defaultValue = (BigInteger)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				if (min != null && min > defaultValue.longValue()) {
					return BigInteger.valueOf(min);
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

	private <E> Object getLong(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean notNull = objectDTO.notNull;
		
		Integer min = objectDTO.min;
		Integer max = objectDTO.max;
		boolean json2Java = objectDTO.json2Java;

		if (value != null && value.toString().trim().length() > 0) {
			Function function = null;
			String str = value.toString().trim();
			try {
				Long valueToReturn = null;
				if (value instanceof Long) {
					return (Long)value;
				} else {
					valueToReturn = Long.valueOf(value.toString().trim());
				}
	
				// processing jsonFunction
				if (json2Java) {
					function = getJson2JavaFunction(objectDTO.getDefaultName(), objectDTO.getEnclosingType());
					if (function != null) {
						valueToReturn = (Long)function.apply(valueToReturn);
					}
					
				} else {
					function = getJava2JsonFunction(objectDTO.getDefaultName(), objectDTO.getEnclosingType());
					if (function != null) {
						return function.apply(valueToReturn);
					}
				}
	
				if (min != null && min > valueToReturn.longValue()) {
					return min.longValue();
				}
	
				if (max != null && valueToReturn.compareTo(Long.valueOf(max)) > 0) {
					valueToReturn = Long.valueOf(max);
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
		
		
		if (returnType == long.class
				|| getDefaultValue() == DEFAULT_VALUE.DEFAULT || notNull) {
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


	private <E> Object getInteger(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean notNull = objectDTO.notNull;
		
		Integer min = objectDTO.min;
		Integer max = objectDTO.max;
		boolean json2Java = objectDTO.json2Java;

		if (value != null && value.toString().trim().length() > 0) {
			Function function = null;
			String str = value.toString().trim();
			try {
				Integer valueToReturn = null;
				if (value instanceof Integer) {
					valueToReturn = (Integer)value;
				} else {
					valueToReturn = Integer.valueOf(str);
				}
	
				// processing jsonFunction
				if (json2Java) {
					function = getJson2JavaFunction(objectDTO.getDefaultName(), objectDTO.getEnclosingType());
					if (function != null) {
						valueToReturn = (Integer)function.apply(valueToReturn);
					}
					
				} else {
					function = getJava2JsonFunction(objectDTO.getDefaultName(), objectDTO.getEnclosingType());
					if (function != null) {
						return function.apply(valueToReturn);
					}
				}
	
				if (min != null && min > valueToReturn.intValue()) {
					return min;
				}
				
				if (max != null && valueToReturn.compareTo(Integer.valueOf(max)) > 0) {
					valueToReturn = Integer.valueOf(max);
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
		
		if (returnType == int.class
				|| getDefaultValue() == DEFAULT_VALUE.DEFAULT || notNull) {
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

	private <E> Object getByte(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean notNull = objectDTO.notNull;
		
		Integer min = objectDTO.min;
		Integer max = objectDTO.max;
		boolean json2Java = objectDTO.json2Java;
		
		if (value != null && value.toString().trim().length() > 0) {
			Function function = null;
			String str = value.toString().trim();
			try {
				Byte valueToReturn = null;
				if (value instanceof Byte) {
					valueToReturn = (Byte)value;
				} else {
					valueToReturn = Byte.valueOf(str);
				}
				
				// processing jsonFunction
				if (json2Java) {
					function = getJson2JavaFunction(objectDTO.getDefaultName(), objectDTO.getEnclosingType());
					if (function != null) {
						valueToReturn = (Byte)function.apply(valueToReturn);
					}
					
				} else {
					function = getJava2JsonFunction(objectDTO.getDefaultName(), objectDTO.getEnclosingType());
					if (function != null) {
						return function.apply(valueToReturn);
					}
				}
				
				if (min != null && min > valueToReturn.byteValue()) {
					return min.byteValue();
				}
				
				if (max != null && valueToReturn.byteValue() > max) {
					valueToReturn = max.byteValue();
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
		
		
		if (returnType == byte.class
				|| getDefaultValue() == DEFAULT_VALUE.DEFAULT || notNull) {
			Byte defaultValue = (Byte)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				if (min != null && min > defaultValue.byteValue()) {
					return min.byteValue();
				}

				return defaultValue;
			}

			if (min != null && min > DefaultValue.dbyte) {
				return min.byteValue();
			}

			return DefaultValue.dbyte;
		}

		return null;
	}

	private <E> Object getChar(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean notNull = objectDTO.notNull;
		
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
					function = getJson2JavaFunction(objectDTO.getDefaultName(), objectDTO.getEnclosingType());
					if (function != null) {
						valueToReturn = (Character)function.apply(valueToReturn);
					}
					
				} else {
					function = getJava2JsonFunction(objectDTO.getDefaultName(), objectDTO.getEnclosingType());
					if (function != null) {
						return function.apply(valueToReturn);
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
				|| getDefaultValue() == DEFAULT_VALUE.DEFAULT || notNull) {
			Character defaultValue = (Character)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				return defaultValue;
			}
			
			return DefaultValue.character;
		}

		return null;
	}


	private <E> Object getShort(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean notNull = objectDTO.notNull;
		
		Integer min = objectDTO.min;
		Integer max = objectDTO.max;
		boolean json2Java = objectDTO.json2Java;

		if (value != null && value.toString().trim().length() > 0) {
			Function function = null;
			String str = value.toString().trim();
			try {
				Short valueToReturn = null;
				if (value instanceof Short) {
					valueToReturn = (Short)value;
				} else {
					valueToReturn = Short.valueOf(str);
				}
	
				if (json2Java) {
					function = getJson2JavaFunction(objectDTO.getDefaultName(), objectDTO.getEnclosingType());
					if (function != null) {
						valueToReturn = (Short)function.apply(valueToReturn);
					}
					
				} else {
					function = getJava2JsonFunction(objectDTO.getDefaultName(), objectDTO.getEnclosingType());
					if (function != null) {
						return function.apply(valueToReturn);
					}
				}
				
				if (min != null && min > valueToReturn.shortValue()) {
					return min.shortValue();
				}
				
				if (max != null && valueToReturn.shortValue() > max) {
					valueToReturn = max.shortValue();
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
		
		
		if (returnType == short.class
				|| getDefaultValue() == DEFAULT_VALUE.DEFAULT || notNull) {
			Short defaultValue = (Short)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				if (min != null && min > defaultValue) {
					return min.shortValue();
				}

				return defaultValue;
			}

			if (min != null && min > DefaultValue.dshort) {
				return min.shortValue();
			}

			return DefaultValue.dshort;
		}

		return null;
	}

	private <E> String getString(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean notNull = objectDTO.notNull;
		String defaultValue = (String)objectDTO.getDefaultValue();
		Integer length = objectDTO.length; // not handled yet, some dummy string might be good for testing?
		
		if (value == null || value.toString().trim().length() == 0 || value.toString().trim().equalsIgnoreCase("null")) {
			if (getDefaultValue() == DEFAULT_VALUE.DEFAULT || notNull) {

				if (defaultValue != null) {
					return defaultValue;
				}

				return DefaultValue.string;
			}

			return null;
		}
		
		if (value instanceof String) {
			return ((String)value).trim();
		}

		return value.toString().trim();
	}

	private <E> Object getBoolean(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean notNull = objectDTO.notNull;
		
		boolean json2Java = objectDTO.json2Java;

		if (value != null && value.toString().trim().length() > 0) {
			Function function = null;
			String str = value.toString().trim();
			try {
				Boolean valueToReturn = null;
				if (value instanceof Boolean) {
					valueToReturn = (Boolean)value;
				} else {
					valueToReturn = Boolean.valueOf(str);
				}
				
				if (json2Java) {
					function = getJson2JavaFunction(objectDTO.getDefaultName(), objectDTO.getEnclosingType());
					if (function != null) {
						valueToReturn = (Boolean)function.apply(valueToReturn);
					}
					
				} else {
					function = getJava2JsonFunction(objectDTO.getDefaultName(), objectDTO.getEnclosingType());
					if (function != null) {
						return function.apply(valueToReturn);
					}
				}
				
				return valueToReturn;
				
			} catch (Exception err) {
				if (function != null && !json2Java) {
					try {
						return function.apply(str);
					} catch (Exception e) {}
				}
				
				if (str.equalsIgnoreCase("false") || str.equals("0")) {
					return false;
				}
	
				if (str.equalsIgnoreCase("true") || str.equals("1")) {
					return true;
				}
			}
		}

		if (returnType == boolean.class
				|| getDefaultValue() == DEFAULT_VALUE.DEFAULT || notNull) {
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
		boolean notNull = objectDTO.notNull;
		
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
					function = getJson2JavaFunction(objectDTO.getDefaultName(), objectDTO.getEnclosingType());
					if (function != null) {
						valueToReturn = (Date)function.apply(valueToReturn);
					}
					
				} else {
					function = getJava2JsonFunction(objectDTO.getDefaultName(), objectDTO.getEnclosingType());
					if (function != null) {
						return function.apply(valueToReturn);
					}
				}
				
			} catch (Exception err) {
				if (function != null && !json2Java) {
					try {
						return function.apply(str);
					} catch (Exception e) {}
				}
			}
		}
		
		if (getDefaultValue() == DEFAULT_VALUE.DEFAULT || notNull) {
			Date defaultValue = (Date)objectDTO.getDefaultValue();
			if (defaultValue != null) {
				return defaultValue;
			}

			return DefaultValue.getDate();
		}

		return null;
	}


	// deserialize only
	private <E> Enum<?> getEnum(FieldData objectDTO) {
		Object valueToProcess = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		boolean notNull = objectDTO.notNull;
		Enum defaultValue = (Enum)objectDTO.defaultValue;
		boolean json2Java = objectDTO.json2Java;

		if (returnType == null || valueToProcess == null) {
			if (notNull) {
				return defaultValue;
			}
			
			return null;
		}

		String value = valueToProcess.toString();
		value = value.trim();

		Class<Enum> enumType = (Class<Enum>) returnType;

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
		Integer ordinal = (Integer)getInteger(fieldData);

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
		boolean notNull = objectDTO.notNull;
		Map defaultValue = (Map)objectDTO.defaultValue;

		if (value == null) {
			if (notNull) {
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

		for (Entry<String, Object> entry: values.entrySet()) {
			Object obj = entry.getValue();
			FieldData newFieldData = new FieldData(obj, obj.getClass());
			newFieldData.json2Java = objectDTO.json2Java;
			returnObj.put(entry.getKey(), string2Object(newFieldData));
		}
		
		return returnObj;
	}


	private <E> Collection<E> getCollection(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		Collection<E> returnObj = (Collection<E>)objectDTO.returnObj;
		Class<Collection<E>> returnType = objectDTO.returnType;
		boolean notNull = objectDTO.notNull;
		Collection<E> defaultValue = (Collection<E>)objectDTO.defaultValue;
		Type erasedType = objectDTO.erasedType;

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
					returnObj.add(string2Object(newFieldData));
				}
				
				return returnObj;
			}
		}
		
		
		if (notNull) {
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
	private <E> E[] getArray(FieldData objectDTO) {
		Object value = objectDTO.valueToProcess;
		E[] returnObj = (E[])objectDTO.returnObj;
		Class<E[]> returnType = objectDTO.returnType;
		boolean notNull = objectDTO.notNull;
		
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
					E[] arr = (E[]) Array.newInstance(componentType, values.size());
	
					int i = 0;
					for (Object val: values) {
						FieldData newFieldData = new FieldData(val, componentType);
						newFieldData.json2Java = objectDTO.json2Java;
						arr[i++] = string2Object(newFieldData);
					}
					
					return arr;
				}
			}
		}


		if (notNull) {
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
	private <E> E string2Object(FieldData objectDTO) {
		// String value, Class<E> returnType
		Object value = objectDTO.valueToProcess;
		Class<E> returnType = objectDTO.returnType;
		if (returnType == null) {
			returnType = (Class<E>) value.getClass();
		}

		if (returnType == String.class) {
			return (E) getString(objectDTO);

		} else if (returnType == Character.class || returnType == char.class) {
			return (E) getChar(objectDTO);

		} else if (returnType == Long.class || returnType == long.class) {
			return (E) getLong(objectDTO);

		} else if (returnType == Integer.class || returnType == int.class) {
			return (E) getInteger(objectDTO);

		} else if (returnType == Double.class || returnType == double.class) {
			return (E) getDouble(objectDTO);

		} else if (returnType == Boolean.class || returnType == boolean.class) {
			return (E) getBoolean(objectDTO);

		} else if (returnType == Date.class || Date.class.isAssignableFrom(returnType)) {
			return (E) getDate(objectDTO);

		} else if (returnType == Byte.class || returnType == byte.class) {
			return (E) getByte(objectDTO);

		} else if (returnType == Short.class || returnType == short.class) {
			return (E) getShort(objectDTO);

		} else if (returnType == Float.class || returnType == float.class) {
			return (E) getFloat(objectDTO);

		} else if (returnType == BigDecimal.class) {
			return (E) getBigDecimal(objectDTO);

		} else if (returnType == BigInteger.class) {
			return (E) getBigInteger(objectDTO);

		} else if (returnType.isEnum() || Enum.class.isAssignableFrom(returnType)) {
			return (E) getEnum(objectDTO);

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
				return (E) fromMap(mvalue, returnType);
			} else {
				return (E) fromMap(mvalue, returnType, obj);
			}
		}
	}

	private <E> String enumToJson(Object value, Class<E> valueType,
			EnumType enumType) {
		if (value == null) {
			return null;
		}
		if (enumType == null) {
			return "\"" + StringUtil.escapeDoublequote(value.toString()) + "\"";
		}

		switch (enumType) {
		case STRING:
			return "\"" + StringUtil.escapeDoublequote(value.toString()) + "\"";
		case ORDINAL:
		default:
			FieldData newFieldData = new FieldData(value, valueType, true);
			newFieldData.json2Java = false;
			
			Enum e = getEnum(newFieldData);
			return "" + e.ordinal();
		}
	}

	private <E> String object2String(FieldData objectDTO, int level, Set set) {
		Object value = objectDTO.valueToProcess;
		Class<?> returnType = objectDTO.returnType;
		boolean notNull = objectDTO.notNull;

		if (returnType == null) {
			if (value == null) {
				return null;
			} else {
				return "\"" + StringUtil.escapeDoublequote(value.toString())
						+ "\"";
			}

		} else if (returnType == String.class) {
			String string = getString(objectDTO);

			if (string == null) {
				return null;
			} else {
				return "\"" + StringUtil.escapeDoublequote(string)
						+ "\"";
			}

		} else if (returnType == Integer.class || returnType == int.class) {
			Object valueToReturn = getInteger(objectDTO);
			
			if (valueToReturn == null) {
				return null;
			}
			
			if (valueToReturn instanceof String) {
				return "\"" + StringUtil.escapeDoublequote(valueToReturn) + "\"";
			}
			
			return String.valueOf(valueToReturn);
			

		} else if (returnType == Double.class || returnType == double.class) {
			Object valueToReturn = getDouble(objectDTO);
			
			if (valueToReturn == null) {
				return null;
			}
			
			if (valueToReturn instanceof String) {
				return "\"" + StringUtil.escapeDoublequote(valueToReturn) + "\"";
			}
			
			return String.valueOf(valueToReturn);

			
		} else if (returnType == Short.class || returnType == short.class) {
			Object valueToReturn = getShort(objectDTO);
			
			if (valueToReturn == null) {
				return null;
			}
			
			if (valueToReturn instanceof String) {
				return "\"" + StringUtil.escapeDoublequote(valueToReturn) + "\"";
			}
			
			return String.valueOf(valueToReturn);

		} else if (returnType == Float.class || returnType == float.class) {
			Object valueToReturn = getFloat(objectDTO);
			
			if (valueToReturn == null) {
				return null;
			}
			
			if (valueToReturn instanceof String) {
				return "\"" + StringUtil.escapeDoublequote(valueToReturn) + "\"";
			}
			
			return String.valueOf(valueToReturn);

			
		} else if (returnType == BigDecimal.class) {
			Object valueToReturn = getBigDecimal(objectDTO);
			
			if (valueToReturn == null) {
				return null;
			}
			
			if (valueToReturn instanceof String) {
				return "\"" + StringUtil.escapeDoublequote(valueToReturn) + "\"";
			}
			
			return String.valueOf(valueToReturn);
			
			
		} else if (returnType == BigInteger.class) {
			Object valueToReturn = getBigInteger(objectDTO);
			
			if (valueToReturn == null) {
				return null;
			}
			
			if (valueToReturn instanceof String) {
				return "\"" + StringUtil.escapeDoublequote(valueToReturn) + "\"";
			}
			
			return String.valueOf(valueToReturn);
			
			
		} else if (Character.class.isAssignableFrom(returnType)
				|| char.class.isAssignableFrom(returnType)) {
			Object valueToReturn = getChar(objectDTO);

			if (valueToReturn == null) {
				return null;
			}

			if (valueToReturn instanceof String) {
				return "\"" + StringUtil.escapeDoublequote(valueToReturn) + "\"";
			}
			
			return "\"" + StringUtil.escapeDoublequote(valueToReturn.toString()) + "\"";

		} else if (returnType == Byte.class || returnType == byte.class) {
			Object valueToReturn = getByte(objectDTO);
			
			if (valueToReturn == null) {
				return null;
			}
			
			if (valueToReturn instanceof String) {
				return "\"" + StringUtil.escapeDoublequote(valueToReturn) + "\"";
			}
			
			return String.valueOf(valueToReturn);
			
		} else if (returnType == Long.class || returnType == long.class) {
			Object valueToReturn = getLong(objectDTO);
			
			if (valueToReturn == null) {
				return null;
			}
			
			if (valueToReturn instanceof String) {
				return "\"" + StringUtil.escapeDoublequote(valueToReturn) + "\"";
			}
			
			return String.valueOf(valueToReturn);

		} else if (returnType == Boolean.class || returnType == boolean.class) {
			Object valueToReturn = getBoolean(objectDTO);
			
			if (valueToReturn == null) {
				return null;
			}
			
			if (valueToReturn instanceof String) {
				return "\"" + StringUtil.escapeDoublequote(valueToReturn) + "\"";
			}
			
			return String.valueOf(valueToReturn);

			// the primitive types byte, double, float, int, long, and short
//			} else if (Number.class.isAssignableFrom(returnType) || returnType.isPrimitive()) {
//				if (value == null) {
//					if (returnType.isPrimitive()
//							|| getDefaultValue() == DEFAULT_VALUE.DEFAULT || notNull) {
//						return "0";
//					}
//					return null;
//				}
//				return String.valueOf(value);
			
		} else if (returnType == Date.class || Date.class.isAssignableFrom(returnType)) {
			Object valueToReturn = getDate(objectDTO);
			
			if (valueToReturn == null) {
				return null;
			}
			
			if (valueToReturn instanceof String) {
				return "\"" + StringUtil.escapeDoublequote(valueToReturn) + "\"";
			}

			try {
				DateFormat format = getDateFormat();

				return "\"" + format.format((Date)valueToReturn) + "\"";
			} catch (Exception err) {
				return null;
			}

		} else if (returnType.isEnum() || Enum.class.isAssignableFrom(returnType) || value instanceof Enum<?>) {
			return enumToJson(value, returnType, objectDTO.enumType);

		} else if (Collection.class.isAssignableFrom(returnType)) {
			if (value == null) {
				return null;
			}

			Collection collection = (Collection) value;

			Class componentType = CollectionArrayTypeGuesser
					.guessElementType(collection, (Class<Collection<E>>) returnType, getJsonClassType());

			level++;
			String repeated = getPrettyIndentationln(level);
			String repeatedItem = getPrettyIndentationln(level+1);

			StringBuilder sbuilder = new StringBuilder();
			for (Object s : collection) {
				FieldData newFieldData = new FieldData(s, componentType);
				newFieldData.json2Java = objectDTO.json2Java;
				
				String str = object2String(newFieldData, level, set);
				if (str != null && str.length() > 0) {
					sbuilder.append(repeatedItem + str + ",");
				}
			}

			String str = sbuilder.toString();
			int size = str.length();
			if (size == 0) {
				switch (getDefaultValue()) {
				case ALWAYS:
					return "[]";
				case NON_NULL:
					return "[]";
				case NON_NULL_EMPTY:
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

			level++;
			String repeated = getPrettyIndentationln(level);
			String repeatedItem = getPrettyIndentationln(level+1);
			StringBuilder sbuilder = new StringBuilder();
			for (int i = 0; i < size; i++) {
				FieldData newFieldData = new FieldData(Array.get(value, i), componentType);
				newFieldData.json2Java = objectDTO.json2Java;
				String str = object2String(newFieldData, level, set);
				if (str != null && str.length() > 0) {
					sbuilder.append(repeatedItem + str + ",");
				}
			}

			String str = sbuilder.toString();
			size = str.length();
			if (size == 0) {
				switch (getDefaultValue()) {
				case ALWAYS:
					return "[]";
				case NON_NULL:
					return "[]";
				case NON_NULL_EMPTY:
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

			Map<String, Object> map = (Map) value;

			StringBuilder sbuilder = new StringBuilder();

			level++;
			String repeated = getPrettyIndentationln(level), pretty = getPrettySpace();

			level++;
			String repeatedItem = getPrettyIndentationln(level);

			//for (Map.Entry<Object, ?> entry : map.entrySet()) {
			Set<String> names = map.keySet();
			try {
				if (getOrderByKeys()) {//LinkedHashSet
					names = new TreeSet(names);
				}

				for (String name : names) {
					Object v = map.get(name);

					if (name != null) {
						sbuilder.append(repeatedItem + "\"" + name + "\":" + pretty);
						
						FieldData newFieldData = new FieldData(v, v.getClass());
						newFieldData.json2Java = objectDTO.json2Java;
						sbuilder.append(object2String(newFieldData, level, set));
						sbuilder.append(",");
					}
				}
			} catch (ClassCastException e) {}

			String str = sbuilder.toString();
			int size = str.length();
			if (size == 0) {
				return "";
			} else {
				return "{" + str.substring(0, size - 1) + repeated + "}";
			}

		} else {
			if (value == null) {
				return null;
			}

			// return "\"" + escapeDoublequote(value.toString()) +
			// "\"";//toJson(value); // null; //
			Class valueType = value.getClass();
			if (returnType == null) {
				returnType = valueType;
			} else if (valueType != null && returnType.isAssignableFrom(valueType)) {
				returnType = valueType;
			}

			return toJson((E) value, returnType, level + 1, set); //level + 1
		}
	}
	
	
	private static <T> Method getSetterByName(String name, T obj) {
		Class<T> valueType = (Class<T>) obj.getClass();

		return getSetterByName(name, valueType);
	}

	private static <T> Method getSetterByName(String name, Class<T> valueType) {
		Method[] sets = getSetters(valueType);

		name = "set" + name.toLowerCase();
		
		for (Method method: sets) {
			if (name.equals(method.getName().toLowerCase())) {
				return method;
			}
		}

		return null;
	}
	
	private static <T> Method getGetterByName(String name, T obj) {
		Class<T> valueType = (Class<T>) obj.getClass();

		return getGetterByName(name, valueType);
	}

	private static <T> Method getGetterByName(String name, Class<T> valueType) {
		Method[] gets = getGetters(valueType);

		name = "get" + name.toLowerCase();
		
		for (Method method: gets) {
			if (name.equals(method.getName().toLowerCase())) {
				return method;
			}
		}

		return null;
	}
	
	private static <T> Method[] getSetters(T obj) {
		Class<T> valueType = (Class<T>) obj.getClass();

		return getSetters(valueType);
	}
	private static <T> Method[] getSetters(Class<T> valueType) {
		return getMethods(valueType, false);
	}
	private static <T> Method[] getGetters(T obj) {
		Class<T> valueType = (Class<T>) obj.getClass();

		return getGetters(valueType);
	}
	private static <T> Method[] getGetters(Class<T> valueType) {
		return getMethods(valueType, true);
	}
	private static <T> Method[] getMethods(Class<T> valueType, boolean isGetter) {
		
		if (isGetter) {
			if (cachedGetters.containsKey(valueType)) {
				return cachedGetters.get(valueType);
			}
		} else {
			if (cachedSetters.containsKey(valueType)) {
				return cachedSetters.get(valueType);
			}
		}

		Stream<Method> stream = Arrays.stream(valueType.getDeclaredMethods());
		while (valueType != null && valueType != Object.class) {
			stream = Stream.concat(stream, Arrays.stream(valueType
					.getSuperclass().getDeclaredMethods()));
			valueType = (Class<T>) valueType.getSuperclass();
		}

		List<Method> uniqueSetters = new ArrayList<>();
		List<Method> uniqueGetters = new ArrayList<>();
		Set<String> sets = new HashSet<>();
		Set<String> gets = new HashSet<>();
		
		String name;
		for (Method method: stream.collect(Collectors.toList())) {
			name = method.getName();
			
			if (name.startsWith("set")) {
				if (name.length() > 3) {
					if (method.getParameterCount() == 1 && !sets.contains(name)) {
						sets.add(name);
						uniqueSetters.add(method);
					}
				}
			} else if (name.startsWith("get")) {
				if (name.length() > 3) {
					if (method.getParameterCount() == 0 && !gets.contains(name)) {
						gets.add(name);
						uniqueGetters.add(method);
					}
				}
			}
		}

		cachedSetters.put(valueType, uniqueSetters.toArray(new Method[uniqueSetters.size()]));
		cachedSetters.put(valueType, uniqueGetters.toArray(new Method[uniqueGetters.size()]));
		
		
		if (isGetter) {
			return cachedGetters.get(valueType);
		} else {
			return cachedSetters.get(valueType);
		}
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

		Stream<Field> stream = Arrays.stream(valueType.getDeclaredFields());
		while (valueType != null && valueType != Object.class) {
			stream = Stream.concat(stream, Arrays.stream(valueType
					.getSuperclass().getDeclaredFields()));
			valueType = (Class<T>) valueType.getSuperclass();
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


	/*
	 * Object to string, serialize
	 */
	// private <T> String toJson(T source, Class<T> valueType, int level, Set
	// set) {
	private <T> String toJson(Object obj, Class<T> valueType, int level, Set set) {
		if (obj == null) {
			return "";
		}
		StringBuffer sb = new StringBuffer();

		int hash = ObjectUtil.hashCode(obj);
		if (set.contains(hash)) {
			return "";
		} else {
			set.add(ObjectUtil.hashCode(obj));
		}

		
		if (ignoreClass(valueType)) {
			return "";
		}
		
		
		FIELD_NAMING format = getFieldNaming();

		String repeated = getPrettyIndentationln(level), pretty = getPrettySpace();
		String repeatedItem = getPrettyIndentationln(level+1);

		ANNOTATION_SUPPORT annotationSupport = getAnnotationSupport();
		Annotation[] annotations = null;
		String[] names = null;

		if (annotationSupport != ANNOTATION_SUPPORT.NONE) {
			annotations = valueType.getAnnotations();
			for (Annotation annotation : annotations) {
				if (ignoreClass(annotation)) {
					return "";
				}
				
				if (annotation instanceof JsonIgnoreProperties) {
					JsonIgnoreProperties jsonIgnoreProperties = (JsonIgnoreProperties) annotation;
					names = jsonIgnoreProperties.value();
					Arrays.sort(names);
				}
			}
		}
		
		Set<String> processedNameSet = new HashSet<>();

		try {
			DEFAULT_VALUE defaultVal = getDefaultValue();
			//boolean nonNUll = (defaultVal == DEFAULT_VALUE.NON_NULL);
			//boolean nonNUllEmpty = (defaultVal == DEFAULT_VALUE.NON_NULL_EMPTY);
			
			// get all getters, keep by field names
			Map<String, Method> gettersByNames = new HashMap<>();
			Method[] methods = getGetters(obj);
			if (methods != null) {
				for (Method getter: methods) {
					gettersByNames.put(getter.getName().substring(3).toLowerCase(), getter);
				}
			}


			Field[] fields = getFields(obj);

			for (Field f : fields) {
				f.setAccessible(true);

				String name = f.getName();
				String fieldName = name;

				// in the ignored list
				if (ObjectUtil.inArray(name, names)) {
					continue;
				}

				Object value = f.get(obj);

				boolean ignored = false;
				EnumType enumType = null;
				boolean notNull = false;
				Integer length = null;
				Integer scale = null;
				Integer min = null;
				Integer max = null;
				String defaultValue = null;
				boolean json2Java = false;
				
				Method getterMethod = gettersByNames.remove(name);

				// in case the value is returned from the getter method only
				if (getterMethod != null) {
					getterMethod.setAccessible(true);
					try {
						Object mvalue = getterMethod.invoke(obj, null);
	
						if (mvalue != null) {
							if (value == null || value.toString().length() == 0) {
								value = mvalue;
							}
						}
						
					} catch (InvocationTargetException e) {
						// e.printStackTrace();
					}
				}


				if (ignoreModifiers(f.getModifiers())) {
					if (getterMethod != null) {
						if (ignoreModifiers(getterMethod.getModifiers())) {
							continue;
						}
						
					} else {
						continue;
					}
				}


//				if ((nonNUll || nonNUllEmpty) && value == null) {
//					continue;
//				}

				if (annotationSupport != ANNOTATION_SUPPORT.NONE) {
					annotations = f.getDeclaredAnnotations();//.getAnnotations();
					// check get method
					// if it exists, combine both into 1 array
					if (getterMethod != null) {
						annotations = getterMethod.getDeclaredAnnotations();//.getAnnotations();
						annotations = Stream
								.concat(Arrays.stream(annotations),
										Arrays.stream(getterMethod
												.getAnnotations()))
								.toArray(Annotation[]::new);
					}


					for (Annotation annotation : annotations) {
						if (annotation instanceof JsonAnyGetter) {
							ignored = true;
							break; // handle in next section, in case there are multiple @JsonAnyGetter methods
							
						} else if (annotation instanceof JsonIgnore || ObjectUtil.isinstanceof(annotation, JsonIgnore.class)) {
							ignored = true;
							break;

						} else if (annotation instanceof org.codehaus.jackson.annotate.JsonIgnore) {
							ignored = true;
							break;

						} else if (ignoreField(annotation)) {
							ignored = true;
							break;
							
						} else if (annotation instanceof Enumerated) {
							enumType = ((Enumerated) annotation).value();

						} else if (annotation instanceof NotNull) {
							notNull = true;

						} else if (annotation instanceof JsonProperty) {
							JsonProperty jsonProperty = (JsonProperty) annotation;
							switch (annotationSupport) {
							case FULL:

							case NAME:
								String dvalue = jsonProperty.value();
								if (dvalue != null && dvalue.length() > 0) {
									name = dvalue;
								}

							case BASIC:
								Access access = jsonProperty.access();
								if (access == Access.WRITE_ONLY) {
									ignored = true;
									break;
								}

								boolean required = jsonProperty.required();
								if (required) {
									notNull = true;
								}

								defaultValue = jsonProperty.defaultValue();

							case NONE:
							}

						} else if (annotation instanceof Size
								&& annotationSupport == ANNOTATION_SUPPORT.FULL) {
							Size size = (Size) annotation;
							min = size.min();
							max = size.max();

						} else if (annotation instanceof Column) {
							Column column = (Column) annotation;

							switch (annotationSupport) {
							case FULL:
								length = column.length();
								scale = column.scale();

							case NAME:
								// String dvalue = column.name(); // may not be
								// used
								// if (dvalue != null && dvalue.length() > 0) {
								// name = dvalue;
								// }

							case BASIC:
								boolean nullable = column.nullable();
								if (!nullable) {
									notNull = true;
								}

							case NONE:
							}

							// String dvalue = ().name();

							// nullable

						} else if (annotationSupport == ANNOTATION_SUPPORT.NAME || annotationSupport == ANNOTATION_SUPPORT.FULL) {
							String dvalue = ObjectUtil.getName(annotation);
							if (dvalue != null && dvalue.length() > 0) {
								name = dvalue;
							}
						}

					}

				}

				if (ignored) {
					continue;
				}

				// might be renamed by strategy
				// here naming strategy configuration takes precedence
				String jname = java2Json(name);

				if (jname == null) {
					continue;
				} else if (!jname.equals(name)) {
					name = jname;
				} else if (!name.equals(fieldName)) {
					jname = java2Json(fieldName);
					
					if (jname == null) {
						continue;
					} else if (!jname.equals(fieldName)) {
						name = jname;
					}
				}

				// only if the name is still the same as the field name
				// format it based on the naming settings
				// otherwise, it is set on purpose
				if (fieldName.equals(name)) {
					name = StringUtil.formatName(name, format);
				}

				String str;
				//if (value == null && (defaultVal == DEFAULT_VALUE.ALWAYS)) {
				//	str = "null";

				//} else {
					Class<?> returnType = f.getType(); // value.getClass();
					FieldData objectDTO = new FieldData(obj, f, name, value,
							returnType, enumType, notNull, length,
							scale, min, max, defaultValue, json2Java);
					str = object2String(objectDTO, level, set);

					if (str == null) {
						if (defaultVal == DEFAULT_VALUE.NON_NULL || defaultVal == DEFAULT_VALUE.NON_NULL_EMPTY) {
							continue;
						} else {
							str = "null";
						}

					} else if (str.length() == 0 || str.equals("\"\"") || str.equals("''") || str.equals("[]") || str.equals("{}")) {
						if (defaultVal == DEFAULT_VALUE.NON_NULL_EMPTY)
							continue;
					}
				//}

				sb.append(repeatedItem);
				
				processedNameSet.add(name);

				sb.append("\"" + name + "\":" + pretty);
				sb.append(str);
				sb.append(",");
			}
			

			// some get methods might not be processed by using field names only
			for (Entry<String, Method> entry: gettersByNames.entrySet()) {
				String methodName = entry.getKey();
				Method method = entry.getValue();
				
				if (ignoreModifiers(method.getModifiers())) {
					continue;
				}
				
				String name = StringUtil.uncapitalize(methodName.substring(3));
				
				// in the ignored list
				if (ObjectUtil.inArray(name, names)) {
					continue;
				}
				
				if (processedNameSet.contains(name)) {
					continue;
				}
				
				// just use field name, even it might not be a field
				String fieldName = name;
				
				method.setAccessible(true);
				
				Object value = null;

				try {
					value = method.invoke(obj, null);
				} catch (InvocationTargetException e) {
					// e.printStackTrace();
				}
				
				boolean ignored = false;
				EnumType enumType = null;
				boolean notNull = false;
				Integer length = null;
				Integer scale = null;
				Integer min = null;
				Integer max = null;
				String defaultValue = null;
				boolean json2Java = false;
				
				
				for (Annotation annotation: method.getDeclaredAnnotations()) {
					if (annotation instanceof JsonAnyGetter) {
						ignored = true;
						break; // handle in next section, in case there are multiple @JsonAnyGetter methods
					} else if (annotation instanceof JsonIgnore || ObjectUtil.isinstanceof(annotation, JsonIgnore.class)) {
						ignored = true;
						break;

					} else if (annotation instanceof org.codehaus.jackson.annotate.JsonIgnore) {
						ignored = true;
						break;

					} else if (ignoreField(annotation)) {
						ignored = true;
						break;
						
					} else if (annotation instanceof Enumerated) {
						enumType = ((Enumerated) annotation).value();

					} else if (annotation instanceof NotNull) {
						notNull = true;

					} else if (annotation instanceof JsonProperty) {
						JsonProperty jsonProperty = (JsonProperty) annotation;
						switch (annotationSupport) {
						case FULL:

						case NAME:
							String dvalue = jsonProperty.value();
							if (dvalue != null && dvalue.length() > 0) {
								name = dvalue;
							}

						case BASIC:
							Access access = jsonProperty.access();
							if (access == Access.WRITE_ONLY) {
								ignored = true;
								break;
							}

							boolean required = jsonProperty.required();
							if (required) {
								notNull = true;
							}

							defaultValue = jsonProperty.defaultValue();

						case NONE:
						}

					} else if (annotation instanceof Size
							&& annotationSupport == ANNOTATION_SUPPORT.FULL) {
						Size size = (Size) annotation;
						min = size.min();
						max = size.max();

					} else if (annotation instanceof Column) {
						Column column = (Column) annotation;

						switch (annotationSupport) {
						case FULL:
							length = column.length();
							scale = column.scale();

						case NAME:
							// String dvalue = column.name(); // may not be
							// used
							// if (dvalue != null && dvalue.length() > 0) {
							// name = dvalue;
							// }

						case BASIC:
							boolean nullable = column.nullable();
							if (!nullable) {
								notNull = true;
							}

						case NONE:
						}

						// String dvalue = ().name();

						// nullable

					} else if (annotationSupport == ANNOTATION_SUPPORT.NAME || annotationSupport == ANNOTATION_SUPPORT.FULL) {
						String dvalue = ObjectUtil.getName(annotation);
						if (dvalue != null && dvalue.length() > 0) {
							name = dvalue;
						}
					}
					
				}
				
				if (ignored) {
					continue;
				}

				if (processedNameSet.contains(name)) {
					continue;
				}
				
				// might be renamed by strategy
				// here naming strategy configuration takes precedence
				String jname = java2Json(name);

				if (jname == null) {
					continue;
				} else if (!jname.equals(name)) {
					name = jname;
				} else if (!name.equals(fieldName)) {
					jname = java2Json(fieldName);
					
					if (jname == null) {
						continue;
					} else if (!jname.equals(fieldName)) {
						name = jname;
					}
				}
				
				if (processedNameSet.contains(name)) {
					continue;
				}
				
				
				// only if the name is still the same as the field name
				// format it based on the naming settings
				// otherwise, it is set on purpose
				if (fieldName.equals(name)) {
					name = StringUtil.formatName(name, format);
				}

				String str;
				//if (value == null && (defaultVal == DEFAULT_VALUE.ALWAYS)) {
				//	str = "null";

				//} else {
					Class<?> returnType = method.getReturnType();
					FieldData objectDTO = new FieldData(obj, null, name, value,
							returnType, enumType, notNull, length,
							scale, min, max, defaultValue, json2Java);
					objectDTO.getter = method;
					str = object2String(objectDTO, level, set);

					if (str == null) {
						if (defaultVal == DEFAULT_VALUE.NON_NULL || defaultVal == DEFAULT_VALUE.NON_NULL_EMPTY) {
							continue;
						} else {
							str = "null";
						}

					} else if (str.length() == 0 || str.equals("\"\"") || str.equals("''") || str.equals("[]") || str.equals("{}")) {
						if (defaultVal == DEFAULT_VALUE.NON_NULL_EMPTY)
							continue;
					}
				//}

				sb.append(repeatedItem);

				sb.append("\"" + name + "\":" + pretty);
				sb.append(str);
				sb.append(",");
			}


			// handle @JsonAnyGetter
			if (annotationSupport != ANNOTATION_SUPPORT.NONE) {
				for (Method method: valueType.getMethods()) {
					for (Annotation annotation: method.getAnnotations()) {
						if (annotation instanceof JsonAnyGetter) {
	
							if (ignoreModifiers(method.getModifiers())) {
								continue;
							}
							
							try {
								Object allValues = method.invoke(obj, null);
	
								if (allValues != null && allValues instanceof Map) {
									Map<String, Object> map = (Map)allValues;
									String str;
									for (String name: map.keySet()) {
										Object value = map.get(name);
										
										FieldData newFieldData = new FieldData(value, value.getClass());
										newFieldData.json2Java = false;
										str = object2String(newFieldData, level, set);
	
										if (str == null) {
											if (defaultVal == DEFAULT_VALUE.NON_NULL || defaultVal == DEFAULT_VALUE.NON_NULL_EMPTY) {
												continue;
											} else {
												str = "null";
											}
	
										} else if (defaultVal == DEFAULT_VALUE.NON_NULL_EMPTY && StringUtil.isEmpty(str)) {
											continue;
										}
	
										sb.append(repeatedItem);
	
										sb.append("\"" + name + "\":" + pretty);
										sb.append(str);
										sb.append(",");
									}
	
								}
							} catch (InvocationTargetException e) {
								// TODO Auto-generated catch block
								//e.printStackTrace();
							}
	
							break;
						}
					}
				}
			}

			String text = sb.toString();
			int size = text.length();
			if (size == 0) {
				return "{}"; // ""

			} else {
				String includeClassType = "";
				if (getIncludeClassTypeInJson()) {
					includeClassType = repeatedItem + "\"@class\":" + pretty + "\"" + valueType.getName() + "\",";
				}

				return "{" + includeClassType + text.substring(0, size - 1) + repeated + "}";
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

				FieldData fieldData = new FieldData(null, list, type, true);
				return string2Object(fieldData);

			} else if (source.startsWith("{")) {
				JSONObject obj = new JSONObject(source);

				Map<String, Object> map = (Map)fromJsonMap(obj);

				Class<T> valueType = ObjectUtil.getTypeClass(type);

				if (Iterable.class.isAssignableFrom(valueType) || Map.class.isAssignableFrom(valueType)) {
					FieldData fieldData = new FieldData(null, map, type, true);
					fieldData.returnType = valueType;

					return string2Object(fieldData);
				} else {
					return (T) fromMap(map, valueType);
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

				return string2Object(new FieldData(null, list, valueType, object, true));

			} else if (source.startsWith("{")) {
				JSONObject obj = new JSONObject(source);

				Map<String, Object> map = (Map)fromJsonMap(obj);

				if (valueType != null && Map.class.isAssignableFrom(valueType)) {
					return string2Object(new FieldData(null, map, valueType, object, true));

				} else {
					if (object == null) {
						return fromMap(map, valueType);
					} else {
						return fromMap(map, valueType, object);
					}
				}

			} else {
				return null;
			}

		} catch (JSONException ex) {
			//ex.printStackTrace();
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

	<T> T fromMap(Map<String, Object> map, Class<T> valueType) {
		if (map == null) {
			return null;
		}

		T obj = newInstance(map, valueType);

		return fromMap(map, valueType, obj);
	}

	/*
	 * create an initial object of valueType type to copy data into
	 */
	<T> T newInstance(Map<String, Object> map, Class<T> valueType) {
		if (map == null) {
			return null;
		}
		// @JsonTypeInfo(use = JsonTypeInfo.Id.MINIMAL_CLASS, include = As.PROPERTY, property = "@class")
		//@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = As.PROPERTY, property = "@class")
		String JsonClassType = null;

		if (valueType != null) {
			valueType.getAnnotations();
			ANNOTATION_SUPPORT annotationSupport = getAnnotationSupport();
			if (annotationSupport != ANNOTATION_SUPPORT.NONE) {
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

		T obj = null;
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
					if (annotation instanceof JsonCreator) {

						Parameter[] parameters = constructor.getParameters();
						String[] parameterNames = ObjectUtil.getParameterNames(parameters);
						//parameterCount = parameters.length;
						Object[] parameterValues = new Object[parameterCount];
						int i = 0;
						for (String parameterName: parameterNames) {
							Object parameterValue = getMapValue(map, parameterName);

							if (parameterValue == null) {
								FieldData objectDTO = new FieldData(null, parameters[i].getType());
								objectDTO.notNull = true;
								parameterValue = string2Object(objectDTO);
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
									objectDTO.notNull = true;
									parameterValue = string2Object(objectDTO);
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
					objectDTO.notNull = true;
					parameterValues[i] = string2Object(objectDTO);
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
									objectDTO.notNull = true;
									parameterValue = string2Object(objectDTO);
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
									objectDTO.notNull = true;
									parameterValue = string2Object(objectDTO);
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
	 */
	<T> T fromMap(Map<String, Object> map, Class<T> valueType, T obj) {

		Set<String> nameKeys = new HashSet(map.keySet());

		if (valueType == null) {
			valueType = (Class<T>)obj.getClass();
		}
		
		
		if (ignoreClass(valueType)) {
			return null;
		}

		try {
			ANNOTATION_SUPPORT annotationSupport = getAnnotationSupport();
			Annotation[] annotations = null;
			String[] names = null;

			if (annotationSupport != ANNOTATION_SUPPORT.NONE) {
				annotations = valueType.getAnnotations();
				for (Annotation annotation : annotations) {
					if (ignoreClass(annotation)) {
						return null;
					}
					
					if (annotation instanceof JsonIgnoreProperties) {
						JsonIgnoreProperties jsonIgnoreProperties = (JsonIgnoreProperties) annotation;
						names = jsonIgnoreProperties.value();
						Arrays.sort(names);
					}
				}
			}

			Field[] fields = getFields(obj);

			FIELD_NAMING format = getFieldNaming();

			for (Field f : fields) {
				f.setAccessible(true);

				String name = f.getName();
				String fieldName = name;

				if (ObjectUtil.inArray(name, names)) {
					nameKeys.remove(name);
					continue;
				}
				
				// get value for name in map
				Object value = getMapValue(map, name, nameKeys);

				Class<?> returnType = f.getType(); // value.getClass();

				Method setterMethod = null;

				boolean ignored = false;
				EnumType enumType = null;
				boolean notNull = false;
				Integer length = null;
				Integer scale = null;
				Integer min = null;
				Integer max = null;
				String defaultValue = null;
				boolean json2Java = true;

				String setterName = "set" + StringUtil.capitalize(name);

				try {
					setterMethod = obj.getClass().getMethod(setterName, returnType);
				} catch (NoSuchMethodException e) {
					// e.printStackTrace();
				}
				
				if (ignoreModifiers(f.getModifiers())) {
					if (setterMethod != null) {
						if (ignoreModifiers(setterMethod.getModifiers())) {
							continue;
						}
						
					} else {
						continue;
					}
				}
				
				if (annotationSupport != ANNOTATION_SUPPORT.NONE) {
					annotations = f.getAnnotations();
					// check set method
					// if it exits, get its annotations
					if (setterMethod != null) {
						annotations = Stream
								.concat(Arrays.stream(annotations),
										Arrays.stream(setterMethod
												.getAnnotations()))
								.toArray(Annotation[]::new);
					}

					if (annotations == null || annotations.length == 0) {
						String getterName = "get" + StringUtil.capitalize(name);

						try {
							Method getterMethod = obj.getClass().getMethod(getterName, null);

							if (getterMethod != null) {
								annotations = getterMethod.getAnnotations();
								annotations = Stream
										.concat(Arrays.stream(annotations),
												Arrays.stream(getterMethod
														.getAnnotations()))
										.toArray(Annotation[]::new);
							}

						} catch (NoSuchMethodException e) {
							// e.printStackTrace();
						}

					}

					for (Annotation annotation : annotations) {
						if (annotation instanceof JsonIgnore) {
							ignored = true;
							break;
							
						} else if (annotation instanceof org.codehaus.jackson.annotate.JsonIgnore) {
							ignored = true;
							break;
							
						} else if (ignoreField(annotation)) {
							ignored = true;
							break;

						} else if (annotation instanceof Enumerated) {
							enumType = ((Enumerated) annotation).value();

						} else if (annotation instanceof NotNull) {
							notNull = true;

						} else if (annotation instanceof JsonProperty) {
							JsonProperty jsonProperty = (JsonProperty) annotation;
							switch (annotationSupport) {
							case FULL:

							case NAME:
								String dvalue = jsonProperty.value();
								Object jvalue = getMapValue(map, dvalue, nameKeys);
								if (jvalue != null) {
									value = jvalue;
									name = dvalue;
								}

							case BASIC:
								Access access = jsonProperty.access();
								if (access == Access.READ_ONLY) {
									ignored = true;
									break;
								}

								boolean required = jsonProperty.required();
								if (required) {
									notNull = true;
								}

								defaultValue = jsonProperty.defaultValue();

							case NONE:
							}

						} else if (annotation instanceof Size
								&& annotationSupport == ANNOTATION_SUPPORT.FULL) {
							Size size = (Size) annotation;
							min = size.min();
							max = size.max();

						} else if (annotation instanceof Column) {
							Column column = (Column) annotation;

							switch (annotationSupport) {
							case FULL:
								length = column.length();
								scale = column.scale();

							case NAME:
								// String dvalue = column.name(); // may not be
								// used
								// if (dvalue != null && dvalue.length() > 0) {
								// name = dvalue;
								// }

							case BASIC:
								boolean nullable = column.nullable();
								if (!nullable) {
									notNull = true;
								}

							case NONE:
							}

							// String dvalue = ().name();

							// nullable

							
						} else if (annotationSupport == ANNOTATION_SUPPORT.NAME ||
								annotationSupport == ANNOTATION_SUPPORT.FULL) {
							String dvalue = ObjectUtil.getName(annotation);

							if (!name.equals(dvalue)) {
								if (value == null) {
									Object jvalue = getMapValue(map, dvalue, nameKeys);
									if (jvalue != null) {
										value = jvalue;
										name = dvalue;
									}
								}
							}

						}
					}
				}

				if (ignored) {
					nameKeys.remove(name);
					continue;
				}

				//if (value == null) {
				// remapped to other name
					String jname = json2Java(name);
					if (jname == null) { // ignored
						continue;
					}
					
					if (jname.equals(name)) {
						if (!fieldName.equals(name)) {
							String jfieldName = json2Java(fieldName);
							if (jfieldName == null) { // ignored
								continue;
								
							} else if (!fieldName.equals(jfieldName)) {
								// this is the new name now
								Object jvalue = getMapValue(map, jfieldName, nameKeys);
								if (jvalue != null) {
									value = jvalue;
									name = jfieldName;
								}
							}
						}
						
					} else {
						Object jvalue = getMapValue(map, jname, nameKeys);
						if (jvalue != null) {
							value = jvalue;
							name = jname;
						}
					}
				//}


				if (value != null) {
					FieldData objectDTO = new FieldData(obj, f, name, value,
							returnType, enumType, notNull, length,
							scale, min, max, defaultValue, json2Java);
					value = string2Object(objectDTO);

					if (value != null) {
						try {
							f.set(obj, value);
						} catch (IllegalAccessException
								| IllegalArgumentException ex) {
							if (setterMethod == null
									&& annotationSupport == ANNOTATION_SUPPORT.NONE) {
								setterName = "set" + StringUtil.capitalize(name);

								try {
									setterMethod = obj.getClass().getMethod(
											setterName, returnType);
								} catch (NoSuchMethodException e) {
									// e.printStackTrace();
								}
							}

							if (setterMethod != null) {
								try {
									setterMethod.invoke(obj, value);
								} catch (InvocationTargetException | IllegalArgumentException e) {
									// e.printStackTrace();
									Object value2 = f.get(obj);
									if (value2 != null
											&& !value.equals(value2)
											&& !Modifier.isStatic(f
													.getModifiers())) {
										boolean all = true;
										switch (getDefaultValue()) {
										case ALWAYS:
											all = true;
											break;
										case NON_NULL:
											all = false;
											break;
										case NON_NULL_EMPTY:
											all = false;
											break;
										case DEFAULT:
											all = false;
											break;
										default:
											all = true;
											break;
										}

										CopyObjects.copy(value, value2, all);
										f.set(obj, value2);
									}

								}
							}
						}
					}
				}
			}


			//@JsonAnySetter
			if (nameKeys.size() > 0) {
				for (Method method: valueType.getMethods()) {
					for (Annotation annotation: method.getAnnotations()) {
						if (annotation instanceof JsonAnySetter) {

							if (ignoreModifiers(method.getModifiers())) {
								continue;
							}
							
							Parameter[] parameters = method.getParameters();
							if (parameters != null && parameters.length == 2) {
								for (String name: nameKeys) {
									Object value = map.get(name);

									if (value != null) {
										try {
											method.invoke(obj, name, value);
										} catch (InvocationTargetException e) {
											//
										}
									}

								}
							}

							break;
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
	public <T> T fromJson(String source, T obj) {
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

	public <T> T fromJson(String source, Class<T> valueType) {
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
	
	public <T> T fromJson(String source, Type type) {
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

	public <T> String toJson(T source, Type type) {
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

		if (Iterable.class.isAssignableFrom(valueType) || Map.class.isAssignableFrom(valueType)) {
			return object2String(new FieldData(source, valueType, type, false), level, set);

		} else {
			return toJson(source, valueType, level, set);
		}
	}
	public <T> String toJson(T source) {
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

		return toJson(source, valueType, level, set);
	}

	
	public <T> T deserialize(String source, T obj) {
		return fromJson(source, obj);
	}
	public <T> T deserialize(String source, Class<T> valueType) {
		return fromJson(source, valueType);
	}
	public <T> T deserialize(String source, Type type) {
		return fromJson(source, type);
	}
	public <T> String serialize(T source, Type type) {
		return toJson(source, type);
	}
	public <T> String serialize(T source) {
		return toJson(source);
	}
	public <T> T readValue(String source, T obj) {
		return fromJson(source, obj);
	}
	public <T> T readValue(String source, Class<T> valueType) {
		return fromJson(source, valueType);
	}
	public <T> T readValue(String source, Type type) {
		return fromJson(source, type);
	}
	public <T> String writeValueAsString(T source, Type type) {
		return toJson(source, type);
	}
	public <T> String writeValueAsString(T source) {
		return toJson(source);
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
		public static boolean isEmpty(String str) {
			return (str == null || str.length() == 0 || str.equals("\"\"") || str.equals("''") || str.equals("[]") || str.equals("{}"));
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
			return str.replaceAll("\"", "\\\"");
		}
		public static String escapeDoublequote(Object obj) {
			return escapeDoublequote(obj.toString());
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
			String name = null;

			Object a;
			if (isinstanceof(annotation, JsonProperty.class)) {
				JsonProperty jsonProperty = (JsonProperty)annotation;
				name = jsonProperty.value();

			} else if (annotation instanceof JsonSetter) {
				JsonSetter jsonSetter = (JsonSetter) annotation;
				name = jsonSetter.value();

			} else if (annotation instanceof SerializedName) {
				name = ((SerializedName) annotation).value();


			} else if (annotation instanceof RequestParam) {
				RequestParam requestParam = (RequestParam) annotation;

				name = requestParam.value();
				if (name == null) {
					name = requestParam.name();
				}

			} else if (annotation instanceof Column) {
				name = ((Column) annotation).name();

			} else if (annotation instanceof com.fasterxml.jackson.databind.util.Named) {
				com.fasterxml.jackson.databind.util.Named named = (com.fasterxml.jackson.databind.util.Named)annotation;
				name = named.getName();

			} else if (annotation instanceof com.google.inject.name.Named) {
				com.google.inject.name.Named named = (com.google.inject.name.Named)annotation;
				name = named.value();

			} else if (annotation instanceof javax.inject.Named) {
				javax.inject.Named named = (javax.inject.Named)annotation;
				name = named.value();

			} else if (annotation instanceof org.codehaus.jackson.map.util.Named) {
				org.codehaus.jackson.map.util.Named named = (org.codehaus.jackson.map.util.Named)annotation;
				name = named.getName();
			}

			return name;
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
}

