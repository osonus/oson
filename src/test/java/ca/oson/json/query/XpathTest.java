package ca.oson.json.query;

import org.junit.Test;

import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.path.Path;
import ca.oson.json.path.Step;
import ca.oson.json.path.Type;
import ca.oson.json.support.TestCaseBase;

public class XpathTest extends TestCaseBase {
	
	@Test
	public void testXPath() {
//		String xpath = "/bookstore/book[1]";
//		Path path = new Path(xpath);
//		
		oson.pretty().setDefaultType(JSON_INCLUDE.NON_DEFAULT);
//		
//		String json;// = oson.print(path);
//
//		xpath = "//book[position()<3]";
//		path = new Path(xpath);
//		
//		json = oson.print(path);
		
		//contains(.,"foo")
		// xpath = "*[contains('X Y Z', local-name())]/AABBCC";
		// xpath = "*[self::X | self::Y | self::Z]/AABBCC";
		// xpath = "//(X|Y|Z)/AABBCC";
		// xpath = "(book/author)[last()]";
		String xpath = "//parent[(childA[contains(.,\"foo\")] or childB[contains(.,\"foo\")]) AND (childA[contains(.,\"bar\")] or childB[contains(.,\"bar\")])]";
		Path path = new Path(xpath);
		
		String json = oson.print(path);
		
		
	}
	
	
	//
	@Test
	public void testAnyPath() {
		oson.pretty().setDefaultType(JSON_INCLUDE.NON_DEFAULT);
		String xpath;
		Path path;
		String json;
		
		//xpath = "//exercise[note>5]/title";
		//xpath = "//solutions/item[@val=\"low\"]";
		//xpath = "author [((last() - 4) <= position()) and (position() <= last())]";
		//xpath = "/project/participants/participant[2]/FirstName";
		// xpath = "//Participant[string-length(FirstName)>=8]";
		// xpath = "/outputTree/command/pivotTable/dimension//category[@text='Measurement']/dimension/category/cell[@text='Nominal']";
		// "//problems[count(//problem) >= 2]";
		// "//Participant[starts-with(Firstname,'Berna')]";
		// xpath = "//Participant[contains(FirstName,'nat')]";
		// xpath = "//conference[(sum(.//participant/qualification) div count(.//participant/qualification)) > 2]";
		// xpath = "//employee/descendant::*";
		// xpath = "child::node()";
		
		xpath = "*[self::X | self::Y | self::Z]/AABBCC";
		
		xpath = "//name/self::*";
		path = new Path(xpath);
		json = oson.print(path);
		
		xpath = "child::*";
		path = new Path(xpath);
		json = oson.print(path);
		
		xpath = "child::node()";
		path = new Path(xpath);
		json = oson.print(path);
	}
	
	
	@Test
	public void testMathOperations() {
		oson.pretty().setDefaultType(JSON_INCLUDE.NON_DEFAULT);
		String xpath;
		Path path;
		String json;
		
		//xpath = "//math/operation[(a + b) * c > 100]";
		//xpath = "//math/operation[a + b * c > 100]";
		//xpath = "//math/operation[a mod b DIV c > 100]";
		// xpath = "//math/operation[(a + b) + c - d > 100]";
		//xpath = "//math/operation[(a + b) + c - d * e - f div g + h - i - (j - k) > 100]";
		//xpath = "//math/operation[(a + b) + (c - d) - (e - f) > 100]";
		
		// xpath = "//math/operation[(a * b) * (c * d) * (e * f) > 100]";
		
		// xpath = "//math/operation[((a + b) div ((c * d) + t)) * (e div f) > 100]";
		
		xpath = "//math/operation[((a + b) div ((c * d) div t)) div (e div f) > 100]";
		
		path = new Path(xpath);
		json = oson.print(path);
		
	}
	

}
