package ca.oson.json;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.persistence.EnumType;

import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.Oson.ENUM_TYPE;
import ca.oson.json.Oson.JSON_INCLUDE;

@Repeatable(FieldMappers.class)
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER, ElementType.CONSTRUCTOR})
@Retention(RUNTIME)
public @interface FieldMapper {
	
	/*
	 * Determine if the field mapper is applied to serialization, deserialization, or both, or none of them
	 */
	BOOLEAN serialize() default BOOLEAN.BOTH;

	/*
	 * field or attribute name in a Java class.
	 * During serialization, the name in Java object will be changed to this name in the outputed Json document;
	 * During deserialization, this name in Input Json document will be mapped to this field.
	 */
	String name() default "";

	/*
	 * Defautl value for a certain type, for now only String is allowed
	 */
	String defaultValue() default "";
	
	/*
	 * Use field first to get/set a field value
	 */
	BOOLEAN useField() default BOOLEAN.NONE;
	/*
	 * Use getter or setter method to get/set a field value
	 */
	BOOLEAN useAttribute() default BOOLEAN.NONE;
	
	/*
	 * Datetime format, for this Date field
	 */
	String simpleDateFormat() default "";

	/*
	 *  in case a enumType, define its type to (de)serialize
	 */
	ENUM_TYPE enumType() default ENUM_TYPE.NONE;
	
	/*
	 * convert a datetime to a long or not
	 */
	BOOLEAN date2Long() default BOOLEAN.NONE;
	
	/*
	 * Require this field must have some value, instead of being null
	 */
	BOOLEAN required() default BOOLEAN.NONE;
	
	/*
	 * Maximum length this string field can have
	 */
	int length() default 0;
	
	/*
	 * scale value for this BigDecimal field
	 */
	int scale() default 0;
	
	/*
	 * number of digits for this BigDecimal field
	 */
	int precision() default 0;
	
	/*
	 * minimum value this field should have
	 */
	long min() default 0;
	
	/*
	 * maximum value this field should have
	 */
	long max() default 0;
	
	/*
	 * Specify the default value handling principal, such as to allow null value 
	 * or not in its output
	 */
	JSON_INCLUDE defaultType() default JSON_INCLUDE.NONE;

	/*
	 * Specify this field should not use double quotes in serialization, if true
	 */
	BOOLEAN jsonRawValue() default BOOLEAN.NONE;
	
	/*
	 * in a class, only one method returning a String value is allowed to set this value to true
	 */
	BOOLEAN jsonValue() default BOOLEAN.NONE;
	
	/*
	 * Ignore this field if true
	 */
	BOOLEAN ignore() default BOOLEAN.NONE;
	
	
	/*
	 * This is the version to ignore
	 */
	double ignoreVersionsAfter() default 0;
	
	/*
	 * method with this value set to true will get all properties not specified earlier.
	 * It will normally return a Map<String , Object>
	 */
	BOOLEAN jsonAnyGetter() default BOOLEAN.NONE;
	
	/*
	 * method with this value set to true will set all properties not consumed earlier.
	 * It will normally store all the other data into a Map<String , Object>
	 */
	BOOLEAN jsonAnySetter() default BOOLEAN.NONE;
	
	/*
	 * Mark a constructor as a Json constructor, no effect on any other types.
	 * Combined with name() to specify the parameter names for the constructor.
	 */
	BOOLEAN jsonCreator() default BOOLEAN.NONE;
	
	
}
