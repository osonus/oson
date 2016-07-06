package ca.oson.json.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
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

import javax.persistence.Column;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.EmptyVisitor;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import ca.oson.json.ComponentType;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.gson.annotations.SerializedName;

public class ObjectUtil {

	public static boolean isBasicDataType(Class valueType) {
		if (valueType == null) { // no idea, just assume
			return true;
		}
		
		if (valueType.isPrimitive() || valueType.isEnum()) {
			return false;
		}

		if (Number.class.isAssignableFrom(valueType) || Date.class.isAssignableFrom(valueType)) {
			return false;
		}
		
		if (valueType == String.class
			|| valueType == Character.class
			|| valueType == Boolean.class) {
			return false;
		}
		
		return true;
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

    public static Class getComponentType(String toGenericString) {
    	Class componentType = null;
		String className = null;
		int idx = toGenericString.indexOf("<");
		if (idx > -1) {
			int idex2 = toGenericString.indexOf(">");
			if (idex2 > idx) {
				className = toGenericString.substring(idx + 1, idex2);
				String[] classNames = className.split(",");
				className = classNames[0];
			}
		}
		
		if (!StringUtil.isEmpty(className)) {
			try {
				
				
				componentType = Class.forName(className);
			} catch (ClassNotFoundException e) {
				//e.printStackTrace();
			}
		}
	
		return componentType;
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
			
		case "com.fasterxml.jackson.annotation.JsonProperty":
			JsonProperty jsonProperty = (JsonProperty)annotation;
			return jsonProperty.value();
			
		case "com.fasterxml.jackson.annotation.JsonSetter":
			return ((JsonSetter) annotation).value();

		case "org.codehaus.jackson.annotate.JsonSetter":
			return ((org.codehaus.jackson.annotate.JsonSetter) annotation).value();
			
		case "com.google.gson.annotations.SerializedName":
			return ((SerializedName) annotation).value();
			
//		case "org.springframework.web.bind.annotation.RequestParam":
//			RequestParam requestParam = (RequestParam) annotation;
//			String name = requestParam.value();
//			if (name == null) {
//				name = requestParam.name();
//			}
//			return name;
			
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
