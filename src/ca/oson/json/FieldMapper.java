package ca.oson.json;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import javax.persistence.EnumType;

import ca.oson.json.Oson.JSON_INCLUDE;

@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RUNTIME)
public @interface FieldMapper {
    
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
	boolean useField() default true;
	/*
	 * Use getter or setter method to get/set a field value
	 */
	boolean useAttribute() default true;
	
	/*
	 * Datetime format, for this Date field
	 */
	String simpleDateFormat() default "";

	/*
	 *  in case a enumType, define its type to (de)serialize
	 */
	EnumType enumType() default EnumType.STRING;
	
	/*
	 * Require this field must have some value, instead of being null
	 */
	boolean required() default false;
	
	/*
	 * Maximum length this string field can have
	 */
	int length() default 0;
	
	/*
	 * scale value for this BigDecimal field
	 */
	int scale() default 0;

	/*
	 * minimum value this field should have
	 */
	int min() default 0;
	
	/*
	 * maximum value this field should have
	 */
	int max() default 0;
	
	/*
	 * Specify the default value handling principal, such as to allow null value 
	 * or not in its output
	 */
	JSON_INCLUDE defaultType() default JSON_INCLUDE.NONE;

	/*
	 * Specify this field should not use double quotes in serialization, if true
	 */
	boolean jsonRawValue() default false;
	
	/*
	 * in a class, only one method returning a String value is allowed to set this value to true
	 */
	boolean JsonValue() default false;
	
	/*
	 * Ignore this field if true
	 */
	boolean ignore() default false;
	
	
	/*
	 * This is the version to ignore
	 */
	double ignoreVersionsAfter() default 0;
}
