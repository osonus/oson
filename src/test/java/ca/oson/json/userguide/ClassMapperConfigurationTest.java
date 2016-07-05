package ca.oson.json.userguide;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Test;

import ca.oson.json.support.TestCaseBase;
import ca.oson.json.ClassMapper;

public class ClassMapperConfigurationTest extends TestCaseBase {

	@Test
	public void testClassMapperSettings() {
		oson.setSimpleDateFormat(MyClass.class, "E, dd MMM yyyy HH:mm:ss Z")
			.setMax(Integer.class, 1000l)
			.setLength(MyCustomerClass.class, 6)
			.setMax(MyCustomerClass.class, 500l);
	
		MyClass myClass = new MyClass();
		myClass.date = new Date();
		myClass.str = "This is a testing of String";
		myClass.integer = 3000000;
		
		String json = oson.serialize(myClass);
		
		Format formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z");
	    String mydate = formatter.format(myClass.date);
	    
	    assertTrue(json.contains(mydate));

		MyClass result = oson.deserialize(json, MyClass.class);

		assertEquals(1000, result.integer.intValue());
		
		assertEquals(myClass.str, result.str);
		
		
		MyCustomerClass myCustomerClass = new MyCustomerClass();
		myCustomerClass.mydate = new Date();
		myCustomerClass.mystr = "This is a testing of String";
		myCustomerClass.myint = 3000000;
		
		String myjson = oson.serialize(myCustomerClass);

		MyCustomerClass myresult = oson.deserialize(myjson, MyCustomerClass.class);

		assertEquals(1000, myresult.myint.intValue());
		
		assertEquals(6, myresult.mystr.length());
	}
	
	
	@Test
	public void testClassMapperSettings2() {
		oson.clear().setClassMappers(MyClass.class, new ClassMapper().setSimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z"))
			.setClassMappers(new ClassMapper(Integer.class).setMax(1000l))
			.setClassMappers(new ClassMapper[] {new ClassMapper(MyCustomerClass.class).setLength(64), 
												new ClassMapper(Integer.class).setMax(500l)
												});
	
		MyClass myClass = new MyClass();
		myClass.date = new Date();
		myClass.str = "This is a testing of String";
		myClass.integer = 3000000;
		
		String json = oson.serialize(myClass);
		
		Format formatter = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z");
	    String mydate = formatter.format(myClass.date);
	    
	    assertTrue(json.contains(mydate));

		MyClass result = oson.deserialize(json, MyClass.class);

		assertEquals(1000, result.integer.intValue());
		
		assertEquals(myClass.str, result.str);
		
		
		MyCustomerClass myCustomerClass = new MyCustomerClass();
		myCustomerClass.mydate = new Date();
		myCustomerClass.mystr = "This is a testing of String";
		myCustomerClass.myint = 3000000;
		
		String myjson = oson.serialize(myCustomerClass);

		MyCustomerClass myresult = oson.deserialize(myjson, MyCustomerClass.class);

		assertEquals(1000, myresult.myint.intValue());
		
		assertEquals(myCustomerClass.mystr, myresult.mystr);
	}
}


class MyClass {
	Date date;
	String str;
	Integer integer;
}


class MyCustomerClass {
	Date mydate;
	String mystr;
	Integer myint;
}