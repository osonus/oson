package ca.oson.json.userguide;

import org.junit.Test;

import ca.oson.json.support.TestCaseBase;

public class ArrayTest extends TestCaseBase {

	@Test
	public void testSerializationArrayInt() {
		int[] ints = {1, 2, 3, 4, 5};
		
		
		String json = oson.serialize(ints);

		String expected = "[1,2,3,4,5]";

		assertEquals(expected, json);
	}
	
	@Test
	public void testSerializationArrayIntMultidimentional() {
		int[][] ints = {{1, 2}, {3, 4}, {5, 6}};

		String json = oson.serialize(ints);
		
		//System.err.println(json);

		String expected = "[[1,2],[3,4],[5,6]]";

		assertEquals(expected, json);
	}
	
	@Test
	public void testSerializationArrayInt3dimentional() {
		int[][][] ints = {{{1, 2}, {3, 24}}, {{5, 6}, {7, 8}}, {{9, 10}, {11, 12}}};

		String json = oson.serialize(ints);
		
		//System.err.println(json);

		String expected = "[[[1,2],[3,24]],[[5,6],[7,8]],[[9,10],[11,12]]]";

		assertEquals(expected, json);
	}
	
	
	@Test
	public void testSerializationArrayString() {
		String[] strings = {"abc", "def", "ghi"};
		
		
		String json = oson.serialize(strings);

		String expected = "[\"abc\",\"def\",\"ghi\"]";

		assertEquals(expected, json);
	}
	
	@Test
	public void testDeserializationArrayInt() {
		String intstr = "[1,2,3,4,5]";
		int[] expected = {1, 2, 3, 4, 5};
		
		int[] ints1 = oson.fromJson(intstr, int[].class); 

		assertEquals(expected.length, ints1.length);
		
		for (int i = 0; i < ints1.length; i++) {
			assertEquals(expected[i], ints1[i]);
		}
	}
	
	@Test
	public void testDeserializationArrayIntMultidimentional() {
		int[][] expected = {{1, 2}, {3, 4}, {5, 6}};

		String intstr = "[[1,2],[3,4],[5,6]]";
		
		int[][] ints2 = oson.fromJson(intstr, int[][].class); 
		
		assertEquals(expected.length, ints2.length);

		for (int i = 0; i < ints2.length; i++) {
			for (int j = 0; j < ints2[0].length; j++) {
				assertEquals(expected[i][j], ints2[i][j]);
			}
		}
	}
	
	@Test
	public void testDeserializationArrayInt3dimentional() {
		int[][][] expected = {{{1, 2}, {3, 24}}, {{5, 6}, {7, 8}}, {{9, 10}, {11, 12}}};

		String intstr = "[[[1,2],[3,24]],[[5,6],[7,8]],[[9,10],[11,12]]]";
		
		int[][][] ints3 = oson.fromJson(intstr, int[][][].class); 
		
		assertEquals(expected.length, ints3.length);

		for (int i = 0; i < ints3.length; i++) {
			for (int j = 0; j < ints3[0].length; j++) {
				for (int k = 0; k < ints3[0][0].length; k++) {
					assertEquals(expected[i][j][k], ints3[i][j][k]);
				}
			}
		}
	}
}
