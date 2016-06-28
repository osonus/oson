package ca.oson.json.listarraymap;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

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
		   
		   // there is no way to pass int type expected.getClass() in and out, without throwing casting exception
		   Integer[] result = oson.deserialize(value, (new Integer[]{}).getClass());

		   for (int i = 0; i < result.length; i++) {
			   assertEquals(expected[i], result[i].intValue());
		   }
	   }
	   
}
