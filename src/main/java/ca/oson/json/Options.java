package ca.oson.json;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.persistence.EnumType;

import ca.oson.json.Oson.FIELD_NAMING;
import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.Oson.JSON_PROCESSOR;
import ca.oson.json.Oson.MODIFIER;
import ca.oson.json.util.*;


public class Options {
	public static final int MAX_LEVEL = 100;
	
	
	// global level configurations
	
	/*
	 * a flag to determine if a date should be converted to a long number, or a formatted text
	 */
	private Boolean date2Long = null;
	/*
	 * Date formatter for all Date and its sub classes
	 */
	private String simpleDateFormat = DefaultValue.simpleDateFormat();
	private DateFormat dateFormat = new SimpleDateFormat(simpleDateFormat);
	
	/*
	 * a flag to indicate if the Oson tool should act as an interface to Gson, Jackson, or simply itself
	 */
	private JSON_PROCESSOR jsonProcessor = JSON_PROCESSOR.OSON;
	
	/*
	 * If either Gson or Jackson fails and throws exception
	 * turn this flag on to print out stack trace message
	 * and continue to use Oson's own implementation
	 */
	private Boolean printErrorUseOsonInFailure = false;
	
	/*
	 * Field naming convention, to make a field name: lowercase, uppercase, camelcase, space-delimited,
	 * dash-delimited, and more
	 */
	private FIELD_NAMING fieldNaming = FIELD_NAMING.FIELD;
	
	/*
	 * Attibute value convention, to allow NULL, empty, default values or not
	 */
	private JSON_INCLUDE defaultType = JSON_INCLUDE.NONE;
	
	/*
	 * Space indent the Json output, and with newline for each item, or not
	 */
	private Boolean prettyPrinting = false;
	
	/*
	 * How many spaces you want for each level of indentation, can be as high as 100 spaces apart for each level,
	 * or just use the default: 2 spaces. It it is set to 0, then no indentation is used, 
	 * the same effect like setting prettyPrinting to false
	 */
	private int indentation = 2;
	
	/*
	 * Turn annotation support on or off, normally should be on
	 */
	private boolean annotationSupport = true;
	
	/*
	 * sort the presentation of attributes in a Map or class by nature order or not
	 */
	private Boolean orderByKeyAndProperties = false;
	
	/*
	 * sort arrays and lists by nature order or not
	 */
	private Boolean orderArrayAndList = false;
	
	/*
	 * Include class name as metadata in the Json output or not
	 */
	private Boolean includeClassTypeInJson = false;
	
	/*
	 * Use this value to act as the attribute for class name metadata, or change to anything you want to use
	 * simply specify it during deserialization. If not specified, will try to interpret the Json document
	 * using this default value: @class
	 */
	private String jsonClassType = "@class";
	
	/*
	 * As the name goes, ignore any versioned attributes bigger than this double value
	 */
	private Double version = 10000d; // max allowed
	
	/*
	 * Ignore any fields with any of the annotations set here
	 */
	private Set<Class> ignoreFieldsWithAnnotations = null;
	
	/*
	 * Ignore any classes with any of the annotations set here
	 */
	private Set<Class> ignoreClassWithAnnotations = null;
	
	/*
	 * Include all the fields or attributes with these modifiers
	 * if not specified, then will use all of them, except:
	 * Transient and Volatile, Synthetic class is also ignored automatically
	 * unless specified otherwise
	 */
	private Set<MODIFIER> includeFieldsWithModifiers = null;
	
	/*
	 * Determine to use field to retrieve or set value, or not
	 */
	private Boolean useField = null;
	
	/*
	 * Determine to use attribute (get or set methods) to retrieve or set value, or not
	 */
	private Boolean useAttribute = null;
	
	/*
	 * Maximum level of depth the processing should go, can be up to 100 levels.
	 * A level means go from a class to its field or atributes. 
	 * There are cases a Java objects can contain other objects, and keeps on going.
	 * Endless loop is carefully prevented, using hashcode, but need more testing to prove.
	 * This setting can also be used for this purpose.
	 */
	private int level = MAX_LEVEL;
	
	/*
	 * Combined with precision and scale, to format the output of decimal values.
	 * Used for float, double, and BigDecimal data types. In the deserialzation process,
	 * only used for BigDecimal
	 */
	private RoundingMode roundingMode = RoundingMode.HALF_UP;
	
	/*
	 * Default enum output type, either int as ordinal, and string as enum name
	 */
	private EnumType enumType = null;
	
	/*
	 * Only used for String data type. It specifies the maximun length a string can hold
	 * or to output. Certain databases requires length limit. Might also used for
	 * testing purpose
	 */
	private Integer length = null;
	
	/*
	 * front-end number of digits in a numeric value
	 */
	private Integer precision = null;
	
	/*
	 * digits after decimal point in a numeric value.
	 * Mostly used for output
	 */
	private Integer scale = null;
	
	/*
	 * Minimum value a number can be, if required, or use default setting
	 */
	private Long min = null;
	
	/*
	 * Maximum value a number can be, if required, or use default setting
	 */
	private Long max = null;
	
	/*
	 * When process methods of a class, only use methods starting with "get"
	 * or "set", otherwise, any no-arg method returning values are considered as get,
	 * any method that accepts 1 value are considered set, excluding constructors
	 */
	private boolean setGetOnly = false;
	
	/*
	 * Determine if a field object should inherit its configuration from a higher level enclosing class
	 */
	private boolean inheritMapping = true;

	/*
	 * Dertermine if Oson should use @Expose annotation from Gson.
	 * Once Oson FieldMapper annotation is used, this flag will be disabled
	 * Full support in seriaze, partial support in deserialize
	 */
	private boolean useGsonExpose = false;
	
	/*
	 * Set the toString method as the default serializer.
	 * If this is set to be true, toString method will be used for serialization
	 */
	private boolean toStringAsSerializer = false;
	
	/*
	 * Escape HTML characters, such as < and >
	 */
	private boolean escapeHtml = true;
	
	/*
	 * Patterns of comments in Java regular expressions
	 * User can define custom comment regex patterns
	 * The default comments are: single-line //, 
	 * single-line #, 
	 * and multiple lines /* .... *\/
	 */
	public static final String[] defaultPatterns = new String[] {"//[^\n\r]*\n?", "#[^\n\r]*\n?", "/\\*[^\\*/]*\\*/"};

	private Pattern[] patterns = null; // StringUtil.compilePatterns(commentPatterns);

	/*
	 * class level configurations
	 */
	private Map<Class, ClassMapper> classMappers = null;

	/*
	 * field level configurations
	 */
	private Set<FieldMapper> fieldMappers = null;
	
	
	public void excludeFieldsWithModifiers(int... modifiers) {
		if (modifiers == null || modifiers.length == 0) {
			return;
		}
		
		if (includeFieldsWithModifiers == null) {
			includeFieldsWithModifiers = new HashSet(Arrays.asList(MODIFIER.values()));
			includeFieldsWithModifiers.remove(MODIFIER.Synthetic);
			includeFieldsWithModifiers.remove(MODIFIER.Transient);
			includeFieldsWithModifiers.remove(MODIFIER.Volatile);
			includeFieldsWithModifiers.remove(MODIFIER.All);
		}
		
		for (int modifier: modifiers) {
			if (Modifier.isAbstract(modifier)) {
				includeFieldsWithModifiers.remove(MODIFIER.Abstract);
			}
			if (Modifier.isFinal(modifier)) {
				includeFieldsWithModifiers.remove(MODIFIER.Final);
			}
			if (Modifier.isInterface(modifier)) {
				includeFieldsWithModifiers.remove(MODIFIER.Interface);
			}
			if (Modifier.isNative(modifier)) {
				includeFieldsWithModifiers.remove(MODIFIER.Native);
			}
			if (Modifier.isPrivate(modifier)) {
				includeFieldsWithModifiers.remove(MODIFIER.Private);
			}
			if (Modifier.isProtected(modifier)) {
				includeFieldsWithModifiers.remove(MODIFIER.Protected);
			}
			if (Modifier.isPublic(modifier)) {
				includeFieldsWithModifiers.remove(MODIFIER.Public);
			}
			if (Modifier.isStatic(modifier)) {
				includeFieldsWithModifiers.remove(MODIFIER.Static);
			}
			if (Modifier.isStrict(modifier)) {
				includeFieldsWithModifiers.remove(MODIFIER.Strict);
			}
			if (Modifier.isSynchronized(modifier)) {
				includeFieldsWithModifiers.remove(MODIFIER.Synchronized);
			}
			if (Modifier.isTransient(modifier)) {
				includeFieldsWithModifiers.remove(MODIFIER.Transient);
			}
			if (Modifier.isVolatile(modifier)) {
				includeFieldsWithModifiers.remove(MODIFIER.Volatile);
			}
			if (ObjectUtil.isPackage(modifier)) {
				includeFieldsWithModifiers.remove(MODIFIER.Package);
			}
		}
	}


	public Pattern[] getPatterns() {
		return patterns;
	}

	public void setCommentPatterns(String[] commentPatterns) {
		this.patterns = StringUtil.compilePatterns(commentPatterns);
		if (this.patterns == null) {
			this.patterns = StringUtil.compilePatterns(defaultPatterns);
		}
	}
	
	public boolean isUseGsonExpose() {
		return useGsonExpose;
	}

	public void setUseGsonExpose(boolean useGsonExpose) {
		this.useGsonExpose = useGsonExpose;
	}
	
	public boolean isInheritMapping() {
		return inheritMapping;
	}

	public void setInheritMapping(boolean inheritMapping) {
		this.inheritMapping = inheritMapping;
	}
	
	public boolean getSetGetOnly() {
		return setGetOnly;
	}

	public void setSetGetOnly(boolean setGetOnly) {
		this.setGetOnly = setGetOnly;
	}
	
	
	public Integer getLength() {
		return length;
	}

	public void setLength(Integer length) {
		this.length = length;
	}

	public Integer getPrecision() {
		return precision;
	}

	public void setPrecision(Integer precision) {
		this.precision = precision;
	}

	public Integer getScale() {
		return scale;
	}

	public void setScale(Integer scale) {
		this.scale = scale;
	}

	public Long getMin() {
		return min;
	}

	public void setMin(Long min) {
		this.min = min;
	}

	public Long getMax() {
		return max;
	}

	public void setMax(Long max) {
		this.max = max;
	}
	
	public RoundingMode getRoundingMode() {
		return roundingMode;
	}

	public void setRoundingMode(RoundingMode roundingMode) {
		this.roundingMode = roundingMode;
	}
	
	public Boolean getDate2Long() {
		return date2Long;
	}

	public void setDate2Long(Boolean date2Long) {
		this.date2Long = date2Long;
	}

	public EnumType getEnumType() {
		return enumType;
	}

	public void setEnumType(EnumType enumType) {
		this.enumType = enumType;
	}

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
	public String java2Json(String name) {
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

		for (FieldMapper fieldMapper: fieldMappers) {
			String java = fieldMapper.java;

			if (java != null && lname.equals(java.toLowerCase())) {
				// if is null, ignore it
				if (fieldMapper.ignore != null && fieldMapper.ignore) {
					return null;
				} else {
					return fieldMapper.json;
				}
			}
		}

		return name;
	}



	/*
	 * get java name during deserialization
	 */
	public String json2Java(String name) {
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

		for (FieldMapper fieldMapper: fieldMappers) {
			String json = fieldMapper.json;

			if (json != null && lname.equals(json.toLowerCase())) {
				// if is null, ignore it
				if (fieldMapper.ignore != null && fieldMapper.ignore) {
					return null;
				} else {
					return fieldMapper.java;
				}
			}
		}

		return name;
	}


	/*
	 * get json name during serialization
	 */
	public String java2Json(Field field) {
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

		for (FieldMapper fieldMapper: fieldMappers) {
			String java = fieldMapper.java;

			if (java != null && lname.equals(java.toLowerCase())) {
				Class<?> type = fieldMapper.getType();

				if (type != null) {
					Field fld;
					try {
						fld = type.getDeclaredField(name);

						if (fld != null && fld.equals(field)) {
							if (fieldMapper.ignore != null && fieldMapper.ignore) {
								return null;
							} else {
								return fieldMapper.json;
							}
						}

					} catch (NoSuchFieldException | SecurityException e) {
						// e.printStackTrace();
					}

				} else {
					names.add(fieldMapper);
				}
			}
		}

		for (FieldMapper fieldMapper: names) {
			if (fieldMapper.ignore != null && fieldMapper.ignore) {
				return null;
			} else {
				return fieldMapper.json;
			}
		}

		return name;
	}


	/*
	 * get java name during deserialization
	 */
	public String json2Java(Field field) {
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

		for (FieldMapper fieldMapper: fieldMappers) {
			String json = fieldMapper.json;

			if (json != null && lname.equals(json.toLowerCase())) {
				Class<?> type = fieldMapper.getType();

				if (type != null) {
					Field fld;
					try {
						fld = type.getDeclaredField(name);

						if (fld != null && fld.equals(field)) {
							if (fieldMapper.ignore != null && fieldMapper.ignore) {
								return null;
							}
							return fieldMapper.java;
						}

					} catch (NoSuchFieldException | SecurityException e) {
						// e.printStackTrace();
					}

				} else {
					names.add(fieldMapper);
				}
			}
		}

		for (FieldMapper fieldMapper: names) {
			if (fieldMapper.ignore != null && fieldMapper.ignore) {
				return null;
			} else {
				return fieldMapper.java;
			}
		}

		return name;
	}


	public Function getDeserializer(String name, Class valueType, Class enclosingType) {
		if (StringUtil.isEmpty(name) || fieldMappers == null) {
			return getDeserializer(valueType);
		}

		name = name.trim();
		String lname = name.toLowerCase();

		List<Function> functions = new ArrayList<>();

		for (FieldMapper fieldMapper: fieldMappers) {
			String java = fieldMapper.java;
			String json = fieldMapper.json;
			Class<?> type = fieldMapper.getType();

			if ((java != null && lname.equals(java.toLowerCase())) ||
					(json != null && lname.equals(json.toLowerCase())) ) {

				if (type == enclosingType) {
					if (fieldMapper.deserializer != null) {
						return fieldMapper.deserializer;
					}

				} else if ((type == null || enclosingType == null) && fieldMapper.deserializer != null) {
					functions.add(fieldMapper.deserializer);
				}
				
			}
		}

		if (functions.size() > 0) {
			return functions.get(0);
		}

		return getDeserializer(valueType);
	}

	
	public Function getDeserializer(Class valueType) {
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
	

	public Function getSerializer(String name, Class valueType, Class enclosingType) {
		if (StringUtil.isEmpty(name) || fieldMappers == null) {
			return getSerializer(valueType);
		}
		
		name = name.trim();
		String lname = name.toLowerCase();

		List<Function> functions = new ArrayList<>();

		for (FieldMapper fieldMapper: fieldMappers) {
			String java = fieldMapper.java;
			String json = fieldMapper.json;
			Class<?> type = fieldMapper.getType();

			if ((java != null && lname.equals(java.toLowerCase())) ||
					(json != null && lname.equals(json.toLowerCase())) ) {
				
				if (fieldMapper.getType() == enclosingType) {
					if (fieldMapper.serializer != null) {
						return fieldMapper.serializer;
					}
				} else if ((type == null || enclosingType == null) && fieldMapper.serializer != null) {
					functions.add(fieldMapper.serializer);
				}
				
			}
		}

		if (functions.size() > 0) {
			return functions.get(0);
		}
		
		return getSerializer(valueType);
	}

	
	public Function getSerializer(Class valueType) {
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
	
	
	public Object getDefaultValue(Class valueType) {
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
		if (indentation >= 0 && indentation < 100) {
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

	public boolean getAnnotationSupport() {
		return annotationSupport;
	}

	public void setAnnotationSupport(boolean annotationSupport) {
		this.annotationSupport = annotationSupport;
	}

	public Boolean getOrderByKeyAndProperties() {
		return orderByKeyAndProperties;
	}

	public void setOrderByKeyAndProperties(Boolean orderByKeyAndProperties) {
		this.orderByKeyAndProperties = orderByKeyAndProperties;
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

	public Double getVersion() {
		return version;
	}

	public void setVersion(Double version) {
		this.version = version;
	}

	/*
	 * The basic strategy:
	 * if set with Collection or Set, either reset if null, or merge
	 */
	public Map<Class, ClassMapper> getClassMappers() {
		return classMappers;
	}

	public ClassMapper getClassMappers(Class type) {
		if (this.classMappers == null) {
			this.classMappers = new HashMap<Class, ClassMapper>();
		}
		
		if (this.classMappers.containsKey(type)) {
			return this.classMappers.get(type);
		}
		
		ClassMapper classMapper = new ClassMapper(type);
		
		this.classMappers.put(type, classMapper);
		
		return classMapper;
	}
	
	public void setClassMappers(Map<Class, ClassMapper> classMappers) {
		if (this.classMappers == null || classMappers == null) {
			this.classMappers = classMappers;
		} else {
			for (Entry<Class, ClassMapper> entry: classMappers.entrySet()) {
				setClassMappers(entry.getKey(), entry.getValue());
			}
		}
	}
	
	
	public void setClassMappers(ClassMapper[] classMappers) {
		if (classMappers == null) {
			this.classMappers = null;
			return;
		}
		
		for (ClassMapper classMapper: classMappers) {
			if (classMapper.getType() != null) {
				setClassMappers(classMapper.getType(), classMapper);
			}
		}
	}
	public void setClassMappers(List<ClassMapper> classMappers) {
		if (classMappers == null) {
			this.classMappers = null;
			return;
		}
		
		for (ClassMapper classMapper: classMappers) {
			if (classMapper.getType() != null) {
				setClassMappers(classMapper.getType(), classMapper);
			}
		}
	}
	public void setClassMappers(ClassMapper classMapper) {
		if (classMapper == null) {
			this.classMappers = null;
			return;
		}
		
		if (classMapper.getType() == null) {
			return;
		}
		setClassMappers(classMapper.getType(), classMapper);
	}
	
	public void setClassMappers(Class type, ClassMapper classMapper) {
		if (type == null) {
			type = classMapper.getType();
			
			if (type == null) {
				return;
			}

		} else {
			classMapper.setType(type);
		}

		if (this.classMappers == null) {
			this.classMappers = new HashMap<Class, ClassMapper>();
		}
		
		ClassMapper oldClassMapper = this.classMappers.get(type);
		
		if (oldClassMapper == null) {
			this.classMappers.put(type, classMapper);
		} else {
			// merge this two, new overwrites the old
			this.classMappers.put(type, Oson.overwriteBy(classMapper, oldClassMapper));
		}
	}
	
	public Set<FieldMapper> getFieldMappers() {
		return fieldMappers;
	}

	public void setFieldMappers(Set<FieldMapper> fieldMappers) {
		if (this.fieldMappers == null || fieldMappers == null) {
			this.fieldMappers = fieldMappers;
		} else {
			for (FieldMapper fieldMapper: fieldMappers) {
				if (fieldMapper.isValid() && !this.fieldMappers.contains(fieldMapper)) {
					setFieldMappers(fieldMapper);
				}
			}
		}
	}
	
	public void setFieldMappers(Collection<FieldMapper> fieldMappers) {
		if (fieldMappers == null) {
			this.fieldMappers = null;
		} else {
			for (FieldMapper fieldMapper: fieldMappers) {
				if (fieldMapper.isValid() && !this.fieldMappers.contains(fieldMapper)) {
					setFieldMappers(fieldMapper);
				}
			}
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
					setFieldMappers(fieldMapper);
				}
			}
		}
	}
	
	public FieldMapper getFieldMapper(String java, String json, Class type) {
		// 4 cases:
		// 1. have java, class
		// 2. json, class
		// 3. have java only
		// 4. have json only
		if (type != null) {
			if (!StringUtil.isEmpty(java)) {
				return getFieldMapper(java, type);
			} else if (!StringUtil.isEmpty(json)) {
				return getFieldMapper(type, json);
			}
			
		} else if (!StringUtil.isEmpty(java)) {
			return getFieldMapperForJava(java);
		} else if (!StringUtil.isEmpty(json)) {
			return getFieldMapperForJson(json);
		}
		
		return null;
	}
	
	public void setFieldMappers(FieldMapper fieldMapper) {
		if (fieldMapper == null || !fieldMapper.isValid()) {
			return;
		}

		if (this.fieldMappers == null) {
			this.fieldMappers = new HashSet<>();
		}

		FieldMapper map = getFieldMapper(fieldMapper.java, fieldMapper.json, fieldMapper.getType());
		
		if (map == null) {
			this.fieldMappers.add(fieldMapper);
		} else {
			Oson.overwriteBy (map, fieldMapper);
		}
	}
	
	public FieldMapper getFieldMapperForJava(String javaName) {
		if (fieldMappers != null && !StringUtil.isEmpty(javaName)) {
			javaName = javaName.trim();
			String lname = javaName.toLowerCase();

			for (FieldMapper mapper: fieldMappers) {
				String java = mapper.java;

				if (mapper.getType() == null && java != null && lname.equals(java.toLowerCase())) {
					return mapper;
				}
			}
		}
		
		return null;
	}
	public FieldMapper getFieldMapperForJson(String jsonName) {
		if (fieldMappers != null && !StringUtil.isEmpty(jsonName)) {
			jsonName = jsonName.trim();
			String lname = jsonName.toLowerCase();

			for (FieldMapper mapper: fieldMappers) {
				String json = mapper.json;
	
				if (mapper.getType() == null && json != null && lname.equals(json.toLowerCase())) {
					return mapper;
				}
			}
		}
		
		return null;
	}
	
	public FieldMapper getFieldMapper(String javaName, Class classtype) {
		if (fieldMappers != null && !StringUtil.isEmpty(javaName)) {
			javaName = javaName.trim();
			String lname = javaName.toLowerCase();
			
			classtype = ObjectUtil.getObjectType(classtype);

			for (FieldMapper mapper: fieldMappers) {
				String java = mapper.java;
	
				if (java != null && lname.equals(java.toLowerCase())) {
					if (mapper.getType() == classtype) {
						return mapper;
					}
					
				}
			}
		}
		
		return null;
	}
	
	public FieldMapper getFieldMapper(Class classtype, String jsonName) {
		if (fieldMappers != null && !StringUtil.isEmpty(jsonName)) {
			jsonName = jsonName.trim();
			String lname = jsonName.toLowerCase();
			
			classtype = ObjectUtil.getObjectType(classtype);

			for (FieldMapper mapper: fieldMappers) {
				String json = mapper.json;
	
				if (json != null && lname.equals(json.toLowerCase())) {
					if (mapper.getType() == classtype) {
						return mapper;
					}
					
				}
			}
		}
		
		return null;
	}
	
	public Set<FieldMapper> getFieldMappers(Class type) {
		Set <FieldMapper> fldMappers = new HashSet<>();
		if (type == null || fieldMappers == null) {
			return fldMappers;
		}
		
		for (FieldMapper fieldMapper: fieldMappers) {
			if (fieldMapper.getType() != null && fieldMapper.getType() == type) {
				fldMappers.add(fieldMapper);
			}
		}
		
		return fldMappers;
	}
	

	public Boolean isUseField() {
		return useField;
	}

	public void setUseField(Boolean useField) {
		this.useField = useField;
	}

	public Boolean isUseAttribute() {
		return useAttribute;
	}

	public void setUseAttribute(Boolean useAttribute) {
		this.useAttribute = useAttribute;
	}
	
	public int getLevel() {
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


	public boolean isToStringAsSerializer() {
		return toStringAsSerializer;
	}


	public void setToStringAsSerializer(boolean toStringAsSerializer) {
		this.toStringAsSerializer = toStringAsSerializer;
	}


	public boolean isEscapeHtml() {
		return escapeHtml;
	}


	public void setEscapeHtml(boolean escapeHtml) {
		this.escapeHtml = escapeHtml;
	}


	public Boolean getOrderArrayAndList() {
		return orderArrayAndList;
	}


	public void setOrderArrayAndList(Boolean orderArrayAndList) {
		this.orderArrayAndList = orderArrayAndList;
	}
}
