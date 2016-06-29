package ca.oson.json.support;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Tester {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Class[] types = new Class[] {Integer.class, int.class,
				Long.class, long.class,Byte.class,byte.class,Double.class,double.class,
				Short.class,short.class,Float.class,float.class,BigDecimal.class,BigInteger.class,
				AtomicInteger.class,AtomicLong.class, Number.class,
				String.class, 
				Boolean.class, boolean.class,Date.class,Collection.class, Array.class,
				Enum.class, Character.class, char.class};
		
		for (Class type: types) {
			System.out.println("	case \"" + type.getName() + "\":");
		}
		
	}

}