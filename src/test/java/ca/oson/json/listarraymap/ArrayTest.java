package ca.oson.json.listarraymap;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import ca.oson.json.domain.Car;
import ca.oson.json.support.TestCaseBase;

public class ArrayTest extends TestCaseBase {
	   @Test
	   public void testSerializeArray() {
		   String[] value = new String[]{"Chevrolet Malibu", "Ford Mondeo", "Toyota Camry"};
		   String expected = "[\"Chevrolet Malibu\",\"Ford Mondeo\",\"Toyota Camry\"]";
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeArray() {
		   String[] expected = new String[]{"Chevrolet Malibu", "Ford Mondeo", "Toyota Camry"};
		   String value = "[\"Chevrolet Malibu\",\"Ford Mondeo\",\"Toyota Camry\"]";
		   
		   String[] result = oson.deserialize(value, expected.getClass());

		   for (int i = 0; i < result.length; i++) {
			   assertEquals(expected[i], result[i]);
		   }
	   }
	   
	   
	   @Test
	   public void testSerializeArrayInt() {
		   int[] value = new int[]{1, 2, 3};
		   String expected = "[1,2,3]";
		   
		   String result = oson.serialize(value);

		   assertEquals(expected, result);
	   }
	   
	   @Test
	   public void testDeserializeArrayInt() {
		   int[] expected = new int[]{1, 2, 3};
		   String value = "[1,2,3]";
		   
		   int[] result = oson.deserialize(value, expected.getClass());

		   for (int i = 0; i < result.length; i++) {
			   assertEquals(expected[i], result[i]);
		   }
	   }
	   
	   
	   @Test
	   public void testDeserializeObjectWithArrayInt() {
		   Car expected = new Car("Toyota", 6);
		   expected.year = 2000;
		   expected.years = new int[]{1989,1998,2016};
		   
		   String value = "{\"brand\":\"Toyota\",\"doors\":6,\"years\":[1989,1998,2016],\"year\":2000}";

		   Car result = oson.deserialize(value, Car.class);

		   assertEquals(expected.year, result.year);
		   assertEquals(expected.doors, result.doors);
		   assertEquals(expected.brand, result.brand);
		   assertEquals(expected.years.length, result.years.length);
		   
	   }
	  
	   @Test
	   public void testDeserializeArrayboolean() {
		   boolean[] expected = new boolean[]{true,true,false};
		   String value = "[true,true,false]";
		   
		   boolean[] result = oson.deserialize(value, expected.getClass());

		   for (int i = 0; i < result.length; i++) {
			   assertEquals(expected[i], result[i]);
		   }
	   }
	   
	   @Test
	   public void testDeserializeArraychar() {
		   char[] expected = new char[]{'a', 'b', 'c'};
		   String value = "[\"a\", \"b\", \"c\"]";
		   
		   char[] result = oson.deserialize(value, expected.getClass());

		   for (int i = 0; i < result.length; i++) {
			   assertEquals(expected[i], result[i]);
		   }
	   }
	   
	   @Test
	   public void testDeserializeArraybyte() {
		   byte[] expected = new byte[]{1,20,40};
		   String value = "[1,20,40]";
		   
		   byte[] result = oson.deserialize(value, expected.getClass());

		   for (int i = 0; i < result.length; i++) {
			   assertEquals(expected[i], result[i]);
		   }
	   }
	   
	   @Test
	   public void testDeserializeArrayfloat() {
		   float[] expected = new float[]{1.2f,20.5f,40.7f};
		   String value = "[1.2,20.5,40.7]";
		   
		   float[] result = oson.deserialize(value, expected.getClass());

		   for (int i = 0; i < result.length; i++) {
			   assertEquals(expected[i], result[i]);
		   }
	   }
	   
	   @Test
	   public void testDeserializeArraydouble() {
		   double[] expected = new double[]{1.2d,20.5d,40.7d};
		   String value = "[1.2,20.5,40.7]";
		   
		   double[] result = oson.deserialize(value, expected.getClass());

		   for (int i = 0; i < result.length; i++) {
			   assertEquals(expected[i], result[i]);
		   }
	   }
	   
	   @Test
	   public void testDeserializeArraylong() {
		   long[] expected = new long[]{12l,205l,407l};
		   String value = "[12,205,407]";
		   
		   long[] result = oson.deserialize(value, expected.getClass());

		   for (int i = 0; i < result.length; i++) {
			   assertEquals(expected[i], result[i]);
		   }
	   }
	   
	   @Test
	   public void testDeserializeArrayshort() {
		   short[] expected = new short[]{12,205,407};
		   String value = "[12,205,407]";
		   
		   short[] result = oson.deserialize(value, expected.getClass());

		   for (int i = 0; i < result.length; i++) {
			   assertEquals(expected[i], result[i]);
		   }
	   }
	   
}
