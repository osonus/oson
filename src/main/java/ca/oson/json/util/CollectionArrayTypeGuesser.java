package ca.oson.json.util;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/*
 * Helper classes
 */
public class CollectionArrayTypeGuesser {
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
	
	public static <E> Class<E> guessElementType(Map<String, Object> map, Class valueType, String jsonClassType) {
		if (map == null) {
			return null;
		}

		Class componentType = null;
		
		if (valueType != null) {
			componentType = valueType.getComponentType();
			
			if (componentType != null) {
				return componentType;
			}
		}
		
		valueType = map.getClass();

		componentType = valueType.getComponentType();
		if (componentType != null) {
			return componentType;
		}

		componentType = ObjectUtil.getTypeComponentClass(valueType.getGenericSuperclass());
		if (componentType != null) {
			return componentType;
		}
		
		Class<?> guess = null;
		for (String key: map.keySet()) {
			Object o = map.get(key);
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

	//<Collection<E>>
	public static <E> Class<E> guessElementType(Collection<E> collection, Class valueType, String jsonClassType) {
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
		
		Class valType = (Class<Collection<E>>) collection.getClass();

		componentType = valType.getComponentType();
		if (componentType != null) {
			return componentType;
		}

		componentType = ObjectUtil.getTypeComponentClass(valType.getGenericSuperclass());
		if (componentType != null) {
			return componentType;
		}
		
		
		componentType = ObjectUtil.getTypeComponentClass(valueType.getGenericSuperclass());
		if (componentType != null) {
			return componentType;
		}
		

//		TypeVariable<?>[] typeParameters = valueType.getTypeParameters();
//		for (TypeVariable type: valueType.getTypeParameters()) {
//			System.err.println("type.getClass(): ");
//			System.err.println(type.getClass());
//			System.err.println("type.getBounds(): ");
//			System.err.println(Arrays.toString(type.getBounds()));
//		}

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

	public static <E> Class<E> guessElementType(E[] array, Class valueType) { // Class<E[]>
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

//		TypeVariable<?>[] typeParameters = valueType.getTypeParameters();
//		for (TypeVariable type: valueType.getTypeParameters()) {
//			System.err.println("type.getClass(): ");
//			System.err.println(type.getClass());
//			System.err.println("type.getBounds(): ");
//			System.err.println(Arrays.toString(type.getBounds()));
//		}

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
		
		while (Collection.class.isAssignableFrom(type) || type.isArray()) {
			try {
				if (obj.getClass().isArray()) {
					obj = Array.get(obj, 0);
				} else {
					Collection collection = (Collection)obj;
					if (collection != null && collection.size() > 0) {
						obj = collection.iterator().next();
					}
				}
				
				if (obj != null) {
					Class nexttype = obj.getClass();
					if (nexttype != null) {
						type = nexttype;
					} else {
						break;
					}
				} else {
					break;
				}
			} catch (Exception e) {}
		}
		
		return type;
	}
	
	public static final Class<?> getBaseType(Class type) {
		while (type.isArray() || Collection.class.isAssignableFrom(type)) {
			Class nexttype = type.getComponentType();
			if (nexttype == null) {
				break;
			} else {
				type = nexttype;
			}
		}
		
		return type;
	}
	
	public static final int getDepth(Object obj) {
		int depth = 0;
		while (obj != null && (Collection.class.isAssignableFrom(obj.getClass()) || obj.getClass().isArray())) {
			depth++; // getDepth(obj.getClass());
			
			try {
				if (obj.getClass().isArray()) {
					obj = Array.get(obj, 0);
				} else {
					Collection collection = (Collection)obj;
					if (collection != null && collection.size() > 0) {
						obj = collection.iterator().next();
					}
				}
			} catch (Exception e) {}
		}

		return depth;
	}
	
	public static final int getDepth(Class type) {
		int depth = 0;
		while (type != null && (type.isArray() || Collection.class.isAssignableFrom(type))) {
			type = type.getComponentType();
			depth++;
		}
		
		return depth;
	}
}
