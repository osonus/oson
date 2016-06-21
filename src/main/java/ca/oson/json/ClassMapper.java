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

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.Oson.MODIFIER;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

@Target({ ElementType.TYPE})
@Retention(RUNTIME)
public @interface ClassMapper {
    
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
	boolean useField() default true;
	
	
	/*
	 * Use getter or setter method to get/set a field value
	 */
	boolean useAttribute() default true;
	
	/*
	 * Datetime format, for all Date fields inside this class
	 */
	String simpleDateFormat() default "";

	/*
	 * During serialization, order attribute names naturally if true
	 */
	boolean orderByKeyAndProperties() default false;
	
    /**
     * Specific ordered list of properties.
     */
    public String[] propertyOrders() default { };
	
	/*
	 * During serialization, include class name in the Json output if true
	 * default to @class attribute
	 */
	boolean includeClassTypeInJson() default false;
	
	/*
	 * ignore attributes with version number greater than the provided value
	 */
	double ignoreVersionsAfter() default 0;
	
	
    /**
     * Full names of annotations to ignore, for example, 
     * to exclude field using annotation of type ca.oson.json.Oson.ClassMapper, you can specify
     * @ClassMapper(ignoreFieldsWithAnnotations = { "ca.oson.json.Oson" })
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
	boolean ignore() default false;

}
