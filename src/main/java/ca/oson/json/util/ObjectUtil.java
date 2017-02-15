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
package ca.oson.json.util;

import java.beans.Expression;
import java.beans.Statement;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONObject;

import ca.oson.json.ComponentType;


public class ObjectUtil {
	@SuppressWarnings("unchecked")
	public static <E> void setMethodValue(E obj, Method method, Object... args) {
		try {
			method.setAccessible(true);
			
			method.invoke(obj, args);
			
		} catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
			// e.printStackTrace();
			try {
				if (obj != null) {
					Statement stmt = new Statement(obj, method.getName(), args);
					stmt.execute();
				}
				
			} catch (Exception e1) {
				// e1.printStackTrace();
			}
		}
	}
	
	
	@SuppressWarnings("unchecked")
	public static <E,R> R getMethodValue(E obj, Method method, Object... args) {
		R value = null;

		try {
			method.setAccessible(true);
			
			value = (R) method.invoke(obj, args);
		} catch (InvocationTargetException | IllegalAccessException | IllegalArgumentException e) {
			// e.printStackTrace();
			try {
				if (obj != null) {
					Expression expr = new Expression(obj, method.getName(), args);
					expr.execute();
					value = (R) expr.getValue();
				}
				
				if (value == null) {
					value = (R) method.getDefaultValue();
				}
				
			} catch (Exception e1) {
				// e1.printStackTrace();
			}
		}
		
		return value;
	}
	

	public static boolean isBasicDataType(Class valueType) {
		if (valueType == null) { // no idea, just assume
			return true;
		}
		
		if (valueType.isPrimitive() || valueType.isEnum()) {
			return true;
		}

		if (Number.class.isAssignableFrom(valueType) || Date.class.isAssignableFrom(valueType)) {
			return true;
		}
		
		if (valueType == String.class
			|| valueType == Character.class
			|| valueType == Boolean.class) {
			return true;
		}
		
		return false;
	}
	
	public static boolean isArrayOrCollection(Class valueType) {
		if (valueType == null) { // no idea, just assume
			return true;
		}
		
		if (valueType.isArray()) {
			return true;
		}
		
		if (Collection.class.isAssignableFrom(valueType)) {
			return true;
		}
		
		if (Iterable.class.isAssignableFrom(valueType)) {
			return true;
		}
		
		return false;
	}
	 
	public static boolean isMapOrObject(Class valueType) {
		if (valueType == null) { // no idea, just assume
			return true;
		}
		
		if (Map.class.isAssignableFrom(valueType)) {
			return true;
		}
		
		if (valueType.isPrimitive() || valueType.isEnum()) {
			return false;
		}

		if (Number.class.isAssignableFrom(valueType) || Date.class.isAssignableFrom(valueType)) {
			return false;
		}
		
		if (isArrayOrCollection(valueType)) {
			return false;
		}
		
		if (valueType == String.class
			|| valueType == Character.class
			|| valueType == Boolean.class) {
			return false;
		}
		
		
		return true;
	}
	
	public static boolean isObject(Class valueType) {
		if (valueType == null) { // no idea, just assume
			return false;
		}
		
		if (valueType.isPrimitive() || valueType.isEnum()) {
			return false;
		}

		if (Number.class.isAssignableFrom(valueType) || Date.class.isAssignableFrom(valueType)) {
			return false;
		}
		
		if (Map.class.isAssignableFrom(valueType)) {
			return false;
		}
		
		if (isArrayOrCollection(valueType)) {
			return false;
		}
		
		if (valueType == String.class
			|| valueType == Character.class
			|| valueType == Boolean.class) {
			return false;
		}
		
		
		return true;
	}
	
	
	public static <T> T unwraponce(T obj) {
		if (obj != null && obj instanceof Optional) {
			Optional<T> opt = (Optional)obj;
			obj = opt.orElse(null);
		}
	
		return obj;
	}
	
	public static <T> T unwrap(T obj) {
		while (obj != null && obj instanceof Optional) {
			Optional<T> opt = (Optional)obj;
			obj = opt.orElse(null);
		}
	
		return obj;
	}
	
	public static Class getObjectType(Class type) {
		if (type.isPrimitive()) {
			if (type == int.class) {
				return Integer.class;
			} else if (type == char.class) {
				return Character.class;
			} else if (type == byte.class) {
				return Byte.class;
			} else if (type == float.class) {
				return Float.class;
			} else if (type == double.class) {
				return Double.class;
			} else if (type == long.class) {
				return Long.class;
			} else if (type == short.class) {
				return Short.class;
			} else if (type == boolean.class) {
				return Boolean.class;
			}
		}
	
		return type;
	}
	
	
	public static boolean isSameDataType(Class ftype, Class mtype) {
		if (ftype == null || mtype == null) {
			return false;
		}
		
		if (mtype == java.lang.Integer.class) {
			if (ftype == int.class
					|| ftype == byte.class
					|| ftype == short.class
					|| ftype == long.class
					|| ftype == float.class
					|| ftype == double.class
					|| ftype == Integer.class
					|| ftype == BigInteger.class
					|| ftype == BigDecimal.class
					|| ftype == Short.class
					|| ftype == Byte.class
					|| ftype == Long.class
					|| ftype == Float.class
					|| ftype == Double.class
					|| ftype == AtomicInteger.class
					|| ftype == AtomicLong.class
					|| ftype.isEnum()
					|| Date.class.isAssignableFrom(ftype)) {
				return true;
			}
			
		} else if (mtype == String.class) {
			if (ftype == String.class
					|| ftype == char.class
					|| ftype == Character.class
					|| ftype == Date.class
					|| ftype.isEnum()) {
				return true;
			}
			
		} else if (mtype == Boolean.class) {
			if (ftype == Boolean.class
					|| ftype == boolean.class) {
				return true;
			}
			
		} else if (mtype == Double.class) {
			if (ftype == double.class
					|| ftype == float.class
					|| ftype == Float.class
					|| ftype == Double.class) {
				return true;
			}
			
		} else if (mtype.isAssignableFrom(ftype) || ftype.isAssignableFrom(mtype)) {
			return true;
		}
		
		return false;
	}
	
	public static boolean isSameType(Class aType, Class bType) {
		if (aType == null || bType == null) {
			return false;
		}
		
		if (aType.isPrimitive() || bType.isPrimitive()) {
			if (aType.isPrimitive()) {
				if (bType.isPrimitive()) {
					return (aType == bType);
				}
				
			} else {
				Class c = bType;
				bType = aType;
				aType = c;
			}
			
			if (aType == int.class && bType == Integer.class) {
				return true;
			} else if (aType == long.class && bType == Long.class) {
				return true;
			} else if (aType == byte.class && bType == Byte.class) {
				return true;
			} else if (aType == double.class && bType == Double.class) {
				return true;
			} else if (aType == short.class && bType == Short.class) {
				return true;
			} else if (aType == float.class && bType == Float.class) {
				return true;
			} else if (aType == char.class && bType == Character.class) {
				return true;
			} else if (aType == boolean.class && bType == Boolean.class) {
				return true;
			}
			
			
		} else if (aType.isAssignableFrom(bType) || bType.isAssignableFrom(aType)) {
			return true;
		}
		
		return false;
	}
	
	public static boolean isPackage(int modifiers) {
		if (modifiers == 0) {
			return true;
		}
//		if (Modifier.isPrivate(modifiers) || Modifier.isProtected(modifiers)
//				|| Modifier.isPublic(modifiers)) {
//			return false;
//		}

		return false;
	}


    public static Class getComponentType(String toGenericString) {
    	Class[] componentTypes = getComponentTypes(toGenericString);
    	
    	if (componentTypes == null || componentTypes.length < 1) {
    		return null;
    	}
	
		return componentTypes[0];
    }
    
    public static Class[] getComponentTypes(String toGenericString) {
		int idx = toGenericString.indexOf("<");
		if (idx > -1) {
			int idex2 = toGenericString.indexOf(">");
			if (idex2 > idx) {
				String className = toGenericString.substring(idx + 1, idex2);
				String[] classNames = className.split(",");
				
				Class[] componentTypes = new Class[classNames.length];

				for (int i = 0; i < classNames.length; i++) {
					className = classNames[i];
					if (!StringUtil.isEmpty(className)) {
						try {
							componentTypes[i] = Class.forName(className.trim());
						} catch (ClassNotFoundException e) {
							//e.printStackTrace();
						}
					}
				}
				
				return componentTypes;
			}
		}

		return null;
    }

	public static <E> Class<E> getTypeClass(java.lang.reflect.Type type) {
		Class cl = type.getClass();
		
		if (ComponentType.class.isAssignableFrom(cl)) {
			ComponentType componentType = (ComponentType)type;
			return componentType.getClassType();
		}
		
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
		Class cl = type.getClass();
		
		if (ComponentType.class.isAssignableFrom(cl)) {
			ComponentType componentType = (ComponentType)type;
			return componentType.getMainComponentType();
		}
		
		//java.util.List<ca.oson.json.test.Dataset>
		String className = type.getTypeName();
		Class ctype = getComponentType(className);
		
		if (ctype != null) {
			return ctype;
		}

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
	 * @param constructor the constructor to get a list of parameter names
	 * @return a list of parameter names
	 * @throws IOException io exception to throw
	 */
	public static List<String> getParameterNames(Constructor<?> constructor) throws IOException {
	    return null;
	}


	public static String[] getParameterNames(Parameter[] parameters) {
		int length = parameters.length;
		String[] parameterNames = new String[length];

		for (int i = 0; i < length; i++) {
			Parameter parameter = parameters[i];

			String parameterName = null;
			for (Annotation annotation: parameter.getAnnotations()) { //getDeclaredAnnotations
				String name = getName(annotation);
				if (name != null) {
					parameterName = name;
					if (annotation instanceof ca.oson.json.annotation.FieldMapper) {
						break;
					}
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
		case "ca.oson.json.annotation.FieldMapper":
			ca.oson.json.annotation.FieldMapper fieldMapper = (ca.oson.json.annotation.FieldMapper)annotation;
			return fieldMapper.name();
		}

		return null;
	}

	/**
	 * Changes the annotation value for the given key of the given annotation to newValue and returns
	 * the previous value.
	 * @author: Balder
	 * @param annotation the annotation to change its value
	 * @param key the key in the value map
	 * @param newValue the new value to change to
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

	public static int hashCode(Object obj, Class valueType) {
		int hash = 7;

		try {
			if (obj == null) {
				hash = valueType.hashCode();
			} else {
				hash = obj.hashCode();
			}
			
		} catch (Exception ex) {}
		
		if (obj != null && Math.abs(hash) < 100) {
			String str = obj.toString();
			hash += str.hashCode();
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
	
	public static JSONObject getJSONObject(String source) {
		if (source == null) {
			return new JSONObject();
		} else {
			return new JSONObject(source);
		}
	}
}
