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

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.*;

import ca.oson.json.util.StringUtil;

/*
 * Make it as simple as possible
 */
public class ComponentType implements Type {
	
	public static final Map<String, String> primitiveTypeNames = new HashMap<>();
	{
		primitiveTypeNames.put("int", "I");
		primitiveTypeNames.put("long", "J");
		primitiveTypeNames.put("byte", "B");
		primitiveTypeNames.put("double", "D");
		primitiveTypeNames.put("float", "F");
		primitiveTypeNames.put("short", "S");
		primitiveTypeNames.put("char", "C");
		primitiveTypeNames.put("boolean", "Z");
	};
	public static final Map<String, Class> primitiveTypeClasses = new HashMap<>();
	{
		primitiveTypeClasses.put("int", int.class);
		primitiveTypeClasses.put("long", long.class);
		primitiveTypeClasses.put("byte", byte.class);
		primitiveTypeClasses.put("double", double.class);
		primitiveTypeClasses.put("float", float.class);
		primitiveTypeClasses.put("short", short.class);
		primitiveTypeClasses.put("char", char.class);
		primitiveTypeClasses.put("boolean", boolean.class);
	};
	
	
	
	public static final Map<String, Class> commonClasses = new HashMap<>();
	{
		commonClasses.put("int", int.class);
		commonClasses.put("long", long.class);
		commonClasses.put("byte", byte.class);
		commonClasses.put("double", double.class);
		commonClasses.put("float", float.class);
		commonClasses.put("short", short.class);
		commonClasses.put("char", char.class);
		commonClasses.put("boolean", boolean.class);
		commonClasses.put("String", String.class);
		commonClasses.put("Integer", Integer.class);
		commonClasses.put("Long", Long.class);
		commonClasses.put("Byte", Byte.class);
		commonClasses.put("Double", Double.class);
		commonClasses.put("Float", Float.class);
		commonClasses.put("Short", Short.class);
		commonClasses.put("Character", Character.class);
		commonClasses.put("Boolean", Boolean.class);
		commonClasses.put("Date", Date.class);
		commonClasses.put("Map", Map.class);
		commonClasses.put("List", List.class);
	};
	
	private static final String[] delimiters = new String[] {",", " extends "};
	
	// just keep for reference
	private Type erasuredType;
	
	String genericLeftName = null;
	String genericRightName = null;
	ComponentType componentType = null;
	
	private String typeName;
	private Class type;
	private Class[] componentTypes;
	
	private ComponentType() {
	}
	
	public ComponentType(String typeName) {
		this.typeName = typeName;
		fixTypeNames();
	}
	
	public ComponentType(Type type) {
		this.erasuredType = type;
		this.typeName = type.getTypeName();

		fixTypeNames();
	}
	
	public ComponentType(Class type, Class... componentTypes) {
		this.type = type;
		this.componentTypes = componentTypes;
		this.typeName = type.getName();
		fixTypeNames();
	}
	
	public ComponentType(Class type) {
		this.type = type;
		fixTypeNames();
	}
	
	
	
	public ComponentType add(Class componentClass) {
		if (componentClass == null || componentClass == type) {
			return this;
		}
		if (componentClass != null && type == null) {
			type = componentClass;
			return this;
		}
		
		if (componentTypes == null || componentTypes.length == 0) {
			componentTypes = new Class[]{componentClass};
		}
		
		if (!(new HashSet(Arrays.asList(this.componentTypes)).contains(componentClass))) {
			int length = this.componentTypes.length;
			this.componentTypes = Arrays.copyOf(this.componentTypes, length + 1);
			this.componentTypes[length] = componentClass;
		}
		
		return this;
	}
	
	
	private void processComponents(String componentTypeName) {
		int idx = componentTypeName.lastIndexOf(", ");
		if (idx > -1) {
			componentTypeName = componentTypeName.substring(idx + 2);
		}
		
		idx = componentTypeName.lastIndexOf(" ");
		if (idx > -1) {
			componentTypeName = componentTypeName.substring(idx + 1).trim();
		}
		
		try {
			Class componentClass = Class.forName(componentTypeName);
			if (this.componentTypes == null || this.componentTypes.length == 0) {
				this.componentTypes = new Class[] {componentClass};
			} else {
				add(componentClass);
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static Class forName(String className) {
		if (className == null) {
			return null;
		}
		className = className.trim();
		int idx = className.lastIndexOf(' ');
		if (idx > -1) {
			className = className.substring(idx+1);
		}
		if (commonClasses.containsKey(className)) {
			return commonClasses.get(className);
		}
		
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void fixTypeNames() {
		if (this.typeName == null) {
			if (type != null) {
				this.typeName = type.getName();
			}
			// simply use the first name, for nothing
			if (componentTypes != null && componentTypes.length > 0) {
				this.typeName = this.typeName + "<" + componentTypes[0].getName() + ">";
			}
			
		} else {
			
			int index = typeName.indexOf("/");
			if (index > -1) {
				typeName = typeName.replaceAll("/", ".");
			}

			int idx = typeName.indexOf("<");
			int lastidx = typeName.lastIndexOf(">");

			index = typeName.indexOf("[");
			
			// case 1: java.util.Collection<java.lang.Integer>
			// Collection<? extends Integer>
			// Collection<Collection<Integer>>
			if (idx > -1 && lastidx > idx && index == -1) {

				String className = typeName.substring(0, idx);
				this.type = this.forName(className);

				String componentTypeName = typeName.substring(idx + 1, lastidx);
				
				idx = -1;
				for (String delimiter: delimiters) {
					idx = componentTypeName.indexOf(delimiter);
					if (idx > -1) {
						this.genericLeftName = componentTypeName.substring(0, idx).trim();
						this.genericRightName = componentTypeName.substring(idx + delimiter.length()).trim();
						this.componentType = new ComponentType(this.genericRightName);
						
						return;
					}
				}

				this.componentType = new ComponentType(componentTypeName);
				
//				idx = newTypeName.indexOf("<");
//				lastidx = newTypeName.lastIndexOf(">");		
//				while (idx > -1 && lastidx > idx) {
//					processComponents(newTypeName.substring(0, idx));
//					
//					newTypeName = newTypeName.substring(idx + 1, lastidx);
//
//					idx = newTypeName.indexOf("<");
//					lastidx = newTypeName.lastIndexOf(">");
//				}
//				processComponents(newTypeName);
				
				
				
				
			} else {

				int repeat = 0;
				String newtypeName = typeName;
				if (index > -1) {
					String end = typeName.substring(index);
					repeat = end.length()/2;
					
					newtypeName = typeName.substring(0, index);
				}
				
				if (primitiveTypeNames.containsKey(newtypeName)) {
					if (repeat == 0) {
						this.type = primitiveTypeClasses.get(newtypeName);
					} else {
						String nickname = primitiveTypeNames.get(newtypeName);
						this.type =this.forName(StringUtil.repeatChar('[', repeat) + nickname);
					}
					
				} else {
					// java.util.Collection<java.lang.Integer>[]
					// with <>
					if (idx > -1 && lastidx > idx) {
						//java.util.Collection<java.lang.Integer>[]
						String className = newtypeName.substring(0, idx);
						String componentTypeName = newtypeName.substring(idx + 1, lastidx);

						Class type = this.forName(className);

						if (repeat == 0) {
							this.type = type;

						} else {
							String expression = StringUtil.repeatChar('[', repeat) + "L" + type.getName() + ";";
							this.type = this.forName(expression);
						}
						
						this.componentType = new ComponentType(componentTypeName);

						
					// without <>
					// java.lang.String[]
					} else {
						this.type = this.forName(newtypeName);
						if (repeat > 0) {
							String expression = StringUtil.repeatChar('[', repeat) + "L" + type.getName() + ";";
							this.type = this.forName(expression);
						}
					}

				}

				//java.lang.String, java.lang.Integer
				//java.lang.String, ca.oson.json.gson.functional.CustomTypeAdaptersTest$StringHolder
				if (this.type == null) {
					this.type = this.forName(newtypeName);
				}

			}

		}
	}
	
	@Override
	public String getTypeName() {
        return typeName;
    }
	
	public Class getClassType() {
		return type;
	}

	public Class[] getComponentClassType() {
		return componentTypes;
	}
	
	public Class getMainComponentType() {
		if (componentTypes != null && componentTypes.length > 0) {
			return componentTypes[0];
		} else {
			return null;
		}
	}
}
