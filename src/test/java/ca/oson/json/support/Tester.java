package ca.oson.json.support;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import ca.oson.json.Oson.StringUtil;

public class Tester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		Class[] types = new Class[] {Integer.class, int.class,
//				Long.class, long.class,Byte.class,byte.class,Double.class,double.class,
//				Short.class,short.class,Float.class,float.class,BigDecimal.class,BigInteger.class,
//				AtomicInteger.class,AtomicLong.class, Number.class,
//				String.class, 
//				Boolean.class, boolean.class,Date.class,Collection.class, Array.class,
//				Enum.class, Character.class, char.class};
//		
//		for (Class type: types) {
//			System.out.println("	case \"" + type.getName() + "\":");
//		}
		
//		String test = "2.1E7";//2.1E-7
//		
//		if (StringUtil.isNumeric(test)) {
//			System.err.println(test + " is numeric");
//		} else {
//			System.err.println(test + " is not numeric");
//		}
		
		//int[] a = new int[]{1,2}; // [I@2a139a55
		//Integer[] a = new Integer[]{1,2}; // [Ljava.lang.Integer;@2a139a55

		// Long[] a = new Long[]{1l,2l}; // [Ljava.lang.Long;@1c655221
		// long[] a = new long[]{1l,2l}; // [J@2a139a55
		
		//Byte[] a = new Byte[]{1,2}; // [Ljava.lang.Byte;@2a139a55
		byte[] a = new byte[]{1,2}; // [B@2a139a55
		System.err.println(a.toString());
		
		if (Number.class.isInstance(Object.class)) {
			System.err.println("Number.class.isInstance(Integer.class)");
		} else {
			System.err.println("not: Number.class.isInstance(Integer.class)");
		}
		
		if (Integer.class.isInstance(Object.class)) {
			System.err.println("Integer.class.isInstance(Number.class)");
		} else {
			System.err.println("not Integer.class.isInstance(Number.class)");
		}
	}

}