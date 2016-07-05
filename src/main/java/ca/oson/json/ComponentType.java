package ca.oson.json;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;

/*
 * Make it as simple as possible
 */
public class ComponentType implements Type {
	// just keep for reference
	private Type erasuredType;
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
			int idx = typeName.indexOf('<');

			if (idx > -1) {
				String className = typeName.substring(0, idx);
				String componentTypeName = typeName.substring(idx + 1, typeName.length() - 1);
				
				try {
					this.type = Class.forName(className);
					Class componentClass = Class.forName(componentTypeName);
					if (this.componentTypes == null || this.componentTypes.length == 0) {
						this.componentTypes = new Class[] {componentClass};
					} else {
						add(componentClass);
					}
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			} else if (this.type == null) {
				try {
					this.type = Class.forName(typeName);
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
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
