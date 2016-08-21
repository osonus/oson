package ca.oson.json;

import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextServices;
import java.beans.beancontext.BeanContextServicesSupport;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.jar.Attributes;

import javax.management.AttributeList;
import javax.management.relation.RoleList;
import javax.management.relation.RoleUnresolvedList;
import javax.print.attribute.standard.JobStateReasons;
import javax.print.attribute.standard.PrinterStateReasons;
import javax.script.Bindings;
import javax.script.SimpleBindings;
import javax.swing.UIDefaults;

import ca.oson.json.util.BooleanUtil;
import ca.oson.json.util.NumberUtil;
import ca.oson.json.util.StringUtil;

/*
 * Defines system-level default values for various Java types.
 * These values are mostly the default values of Java language types.
 * They can be overwritten for specific processing needs.
 * Once they are changed, the default behavior changes.
 *
 * In case the value is required,
 * the processing steps are:
 * 1. local value exists, use it;
 * 2. otherwise, if default value exists in FieldData, use it;
 * 3. otherwise, if can be constructed from newInstance function, use it;
 * 4. finally, use this system default value.
 */
public class DefaultValue {
	public static Integer integer = new Integer(0);
	public static BigInteger bigInteger = BigInteger.ZERO;
	public static BigDecimal bigDecimal = BigDecimal.ZERO;
	public static Boolean bool = false;
	public static String string = "";
	public static Character character = '\u0000';
	public static Short dshort = 0;
	public static Byte dbyte = 0;
	public static Long dlong = 0l;
	public static Float dfloat = 0f;
	public static Double ddouble = 0d;
	public static String simpleDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'"; // null; // 
	public static Date date = Calendar.getInstance().getTime(); // new Date();
	public static AtomicInteger atomicInteger = new AtomicInteger();
	public static AtomicLong atomicLong = new AtomicLong();
	
	public static String simpleDateFormat() {
		if (simpleDateFormat == null) {
			SimpleDateFormat dateFormat = new SimpleDateFormat();
			simpleDateFormat = dateFormat.toLocalizedPattern();
		}
		return simpleDateFormat;
	}
	
	public static Collection collection(Class type) {
		if (List.class.isAssignableFrom(type)) {
			if (type == LinkedList.class) {
				return new LinkedList();
			} else if (type == ArrayList.class) {
				return new ArrayList();
			} else if (type == Vector.class) {
				return new Vector();
			} else if (type == Stack.class) {
				return new Stack();
			} else if (type == AttributeList.class) {
				return new AttributeList();
			} else if (type == CopyOnWriteArrayList.class) {
				return new CopyOnWriteArrayList();
			} else if (type == RoleList.class) {
				return new RoleList();
			} else if (type == RoleUnresolvedList.class) {
				return new RoleUnresolvedList();
			} 
			
			return new ArrayList();
			
			
		} else if (SortedSet.class.isAssignableFrom(type) || NavigableSet.class.isAssignableFrom(type)) {
			if (type == ConcurrentSkipListSet.class) {
				return new ConcurrentSkipListSet();
			} else if (type == TreeSet.class) {
				return new TreeSet();
				
			}
			
			return new TreeSet();
			
			
		} else if (Set.class.isAssignableFrom(type)) {
			if (type == HashSet.class) {
				return new HashSet();
			} else if (type == TreeSet.class) {
				return new TreeSet();
			} else if (type == ConcurrentSkipListSet.class) {
				return new ConcurrentSkipListSet();
			} else if (type == CopyOnWriteArraySet.class) {
				return new CopyOnWriteArraySet();
			} else if (type == LinkedHashSet.class) {
				return new LinkedHashSet();
			} else if (type == JobStateReasons.class) {
				return new JobStateReasons();
			}
			
			return new HashSet();
			
		} else if (Queue.class.isAssignableFrom(type)) {
			if (type == PriorityQueue.class) {
				return new PriorityQueue();
			} else if (type == LinkedBlockingDeque.class) {
				return new LinkedBlockingDeque();
			} else if (type == ArrayBlockingQueue.class) {
				return new ArrayBlockingQueue(0);
			} else if (type == DelayQueue.class) {
				return new DelayQueue();
			} else if (type == LinkedBlockingQueue.class) {
				return new LinkedBlockingQueue();
			} else if (type == LinkedTransferQueue.class) {
				return new LinkedTransferQueue();
			} else if (type == PriorityBlockingQueue.class) {
				return new PriorityBlockingQueue();
			} else if (type == SynchronousQueue.class) {
				return new SynchronousQueue();
			} else if (type == ArrayDeque.class) {
				return new ArrayDeque();
			} else if (type == ConcurrentLinkedDeque.class) {
				return new ConcurrentLinkedDeque();
			} else if (type == ConcurrentLinkedQueue.class) {
				return new ConcurrentLinkedQueue();
			} else if (type == LinkedList.class) {
				return new LinkedList();
			}
			
			return new PriorityQueue();
			
		} else if (Deque.class.isAssignableFrom(type)) {
			if (type == ArrayDeque.class) {
				return new ArrayDeque();
			} else if (type == ConcurrentLinkedDeque.class) {
				return new ConcurrentLinkedDeque();
			} else if (type == LinkedBlockingDeque.class) {
				return new LinkedBlockingDeque();
			} else if (type == LinkedList.class) {
				return new LinkedList();
			}
			
			return new ArrayDeque();
			
		} else if (BlockingQueue.class.isAssignableFrom(type)) {
			if (type == ArrayBlockingQueue.class) {
				return new ArrayBlockingQueue(0);
			} else if (type == DelayQueue.class) {
				return new DelayQueue();
			} else if (type == LinkedBlockingDeque.class) {
				return new LinkedBlockingDeque();
			} else if (type == LinkedBlockingQueue.class) {
				return new LinkedBlockingQueue();
			} else if (type == LinkedTransferQueue.class) {
				return new LinkedTransferQueue();
			} else if (type == PriorityBlockingQueue.class) {
				return new PriorityBlockingQueue();
			} else if (type == SynchronousQueue.class) {
				return new SynchronousQueue();
				
			}
			
			return new LinkedBlockingDeque();
			
		} else if (BlockingDeque.class.isAssignableFrom(type)) {
			
			return new LinkedBlockingDeque();
			
		} else if (BeanContextServices.class.isAssignableFrom(type)) {
			
			return new BeanContextServicesSupport();
			
		} else if (BeanContext.class.isAssignableFrom(type)) {
			
			return new BeanContextServicesSupport();

		}
		
		return new ArrayList();
	}
	
	public static Map map(Class type) {
		
		if (type == HashMap.class) {
			return new HashMap();
		} else if (type == ConcurrentHashMap.class) {
			return new ConcurrentHashMap();
		} else if (type == ConcurrentSkipListMap.class) {
			return new ConcurrentSkipListMap();
		} else if (type == IdentityHashMap.class) {
			return new IdentityHashMap();
		} else if (type == TreeMap.class) {
			return new TreeMap();
		} else if (type == WeakHashMap.class) {
			return new WeakHashMap();
		} else if (type == Attributes.class) {
			return new Attributes();
		} else if (type == Hashtable.class) {
			return new Hashtable();
		} else if (type == IdentityHashMap.class) {
			return new IdentityHashMap();
		} else if (type == LinkedHashMap.class) {
			return new LinkedHashMap();
		} else if (type == PrinterStateReasons.class) {
			return new PrinterStateReasons();
		} else if (type == Properties.class) {
			return new Properties();
		} else if (type == SimpleBindings.class || type == Bindings.class) {
			return new SimpleBindings();
		} else if (type == UIDefaults.class) {
			return new UIDefaults();
		} else if (type == ConcurrentMap.class) {
			return new ConcurrentHashMap();
		} else if (type == ConcurrentNavigableMap.class || type == NavigableMap.class) {
			return new ConcurrentSkipListMap();
		} else if (type == SortedMap.class) {
			return new TreeMap();
		} else if (type == AbstractMap.class) {
			return new HashMap();
			
		}
		
		try {
			return (Map) type.newInstance();
		} catch (Exception e) {
			//e.printStackTrace();
			Constructor[] constructors = type.getDeclaredConstructors();//.getConstructors();
			if (constructors != null && constructors.length > 0) {
				Constructor constructor = constructors[0];
				try {
					constructor.setAccessible(true);
					return (Map) constructor.newInstance(null);
				} catch (Exception ex) {
					//ex.printStackTrace();
				}
				
			}
		}
		
		return new HashMap();
	}
	public static Object[] array() {
		return new Object[0];
	}
	public static Date getDate() {
		date = Calendar.getInstance().getTime();
		
		return date;
	}

	
	public static Object getSystemDefault(Class type) {
		if (type == String.class) {
			return DefaultValue.string;
		} else if (Collection.class.isAssignableFrom(type)) {
			return DefaultValue.collection(type);
		} else if (Map.class.isAssignableFrom(type)) {
			return DefaultValue.map(type);
		} else if (type.isArray()) {
			return DefaultValue.array();

		} else if (type == Integer.class || type == int.class) {
			return DefaultValue.integer;
			
		} else if (type == BigInteger.class) {
			return DefaultValue.bigInteger;
			
		} else if (type == BigDecimal.class) {
			return DefaultValue.bigDecimal;
			
		} else if (type == Character.class || type == char.class) {
			return DefaultValue.character;
			
		} else if (type == Short.class || type == short.class) {
			return DefaultValue.dshort;
			
		} else if (type == Byte.class || type == byte.class) {
			return DefaultValue.dbyte;
			
		} else if (type == Long.class || type == long.class) {
			return DefaultValue.dlong;
			
		} else if (type == Float.class || type == float.class) {
			return DefaultValue.dfloat;
			
		} else if (type == Float.class || type == float.class) {
			return DefaultValue.dfloat;
			
		} else if (type == Double.class || type == double.class) {
			return DefaultValue.ddouble;
			
		} else if (type == Short.class) {
			return DefaultValue.dshort;
			
		}
		
		return null;
	}
	
	public static boolean isDefault(Object obj) {
		if (StringUtil.isEmpty(obj)) {
			return true;
		}
		
		Class type = obj.getClass();
		
		return isDefault(obj, type);
	}

	public static boolean isDefault(Object obj, Class type) {
		if (StringUtil.isEmpty(obj)) {
			return true;
		}
		
		if (type == null) {
			type = obj.getClass();
		}

		if (type == String.class) {
			String str = obj.toString().trim();
			return (str.equals("[]") || str.equals("{}"));
			
		} else if (Collection.class.isAssignableFrom(type)) {
			Collection list = (Collection)obj;
			if (list.size() == 0) {
				return true;
			}

			for (Object item: list) {
				if (!isDefault(item)) {
					return false;
				}
			}
			
			return true;
			
		} else if (Map.class.isAssignableFrom(type)) {
			Map map = (Map)obj;
			return (map.size() == 0);
			
		} else if (type.isArray()) {
			if (Array.getLength(obj) == 0) {
				return true;
			}
			
			return false;

		} else if (type == Character.class || type == char.class) {
			return DefaultValue.character.equals(obj);
			
		} else if (type == Boolean.class || type == boolean.class || type == AtomicBoolean.class) {
			return (DefaultValue.bool == BooleanUtil.string2Boolean(obj.toString()));
			
		} else if (Number.class.isAssignableFrom(type) || type.isPrimitive()) {
			obj = NumberUtil.getNumber(obj, type);

			if (type == Integer.class || type == int.class) {
				return DefaultValue.integer.equals(obj);
				
			} else if (type == BigInteger.class) {
				return DefaultValue.bigInteger.equals(obj);
				
			} else if (type == BigDecimal.class) {
				return DefaultValue.bigDecimal.equals(obj);
				
			} else if (type == Short.class || type == short.class) {
				return DefaultValue.dshort.equals(obj);
				
			} else if (type == Byte.class || type == byte.class) {
				return DefaultValue.dbyte.equals(obj);
				
			} else if (type == Long.class || type == long.class) {
				return DefaultValue.dlong.equals(obj);
				
			} else if (type == Float.class || type == float.class) {
				return DefaultValue.dfloat.equals(obj);
				
			} else if (type == AtomicInteger.class) {
				return (DefaultValue.atomicInteger.intValue() == ((AtomicInteger)obj).intValue());
				
			} else if (type == AtomicLong.class) {
				return (DefaultValue.atomicLong.longValue() == ((AtomicLong)obj).longValue());
				
			} else if (type == Double.class || type == double.class) {
				return DefaultValue.ddouble.equals(obj);
				
			} else if (type == Short.class || type == short.class) {
				return DefaultValue.dshort.equals(obj);
			} else {
			}
		
		}
		
		return false;
	}
}