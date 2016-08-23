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
package ca.oson.json.annotation;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import ca.oson.json.Oson;
import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.Oson.ENUM_TYPE;
import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.Oson.MODIFIER;

@Repeatable(ClassMappers.class)
@Target({ ElementType.TYPE})
@Retention(RUNTIME)
public @interface ClassMapper {
    
	/*
	 * Determine if the class mapper is applied to serialization, deserialization, or both, or none of them
	 */
	BOOLEAN serialize() default BOOLEAN.BOTH;
	
	/*
	 * Defautl value for a certain type, for now only String is allowed
	 */
	String defaultValue() default "";
	
	/*
	 * NULL means it has no value here
	 */
	JSON_INCLUDE defaultType() default JSON_INCLUDE.NONE;
	
	/*
	 * Use field first to get/set a field value
	 */
	BOOLEAN useField() default BOOLEAN.NONE;
	
	
	/*
	 * Use getter or setter method to get/set a field value
	 */
	BOOLEAN useAttribute() default BOOLEAN.NONE;
	
	/*
	 * Datetime format, for all Date fields inside this class
	 */
	String simpleDateFormat() default "";

	/*
	 * During serialization, order attribute names naturally if true
	 */
	BOOLEAN orderByKeyAndProperties() default BOOLEAN.NONE;
	
    /**
     * Specific ordered list of properties.
     */
    public String[] propertyOrders() default { };
	
	/*
	 * During serialization, include class name in the Json output if true
	 * default to @class attribute
	 */
    BOOLEAN includeClassTypeInJson() default BOOLEAN.NONE;
	
	/*
	 * the version number since a type has been present
	 */
	double since() default 0;
	
	/*
	 * the version number until a type should be present.
	 */
	double until() default 0;
	
    /**
     * Full names of annotations to ignore, for example, 
     * to exclude field using annotation of type ca.oson.json.Oson.ClassMapper, you can specify
     * \@ClassMapper(ignoreFieldsWithAnnotations = { "ca.oson.json.Oson" })
     */
    public String[] ignoreFieldsWithAnnotations() default { };
    
    /*
     * Specify a set of MODIFIER for a class to include properties
     */
    public MODIFIER[] includeFieldsWithModifiers() default { };

    
    /*
     * Ignore the list of attribute names directly
     */
    public String[] jsonIgnoreProperties() default { };
    
	/*
	 * Ignore this type if true
	 */
    BOOLEAN ignore() default BOOLEAN.NONE;

	/*
	 * convert a datetime to a long or not
	 */
	BOOLEAN date2Long() default BOOLEAN.NONE;
	
	/*
	 *  in case a enumType, define its type to (de)serialize
	 */
	ENUM_TYPE enumType() default ENUM_TYPE.NONE;
	
	
	/*
	 * Escape HTML characters, such as < and >
	 */
	BOOLEAN escapeHtml() default BOOLEAN.NONE;
	
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
}
