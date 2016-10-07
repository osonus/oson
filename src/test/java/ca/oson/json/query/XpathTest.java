package ca.oson.json.query;

import org.junit.Test;

import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.path.Path;
import ca.oson.json.support.TestCaseBase;

public class XpathTest extends TestCaseBase {
	
	@Test
	public void testXPath() {
//		String xpath = "/bookstore/book[1]";
//		Path path = new Path(xpath);
//		
		oson.pretty().setDefaultType(JSON_INCLUDE.NON_NULL);
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
		//xpath = "//exercise[note>5]/title";
		//xpath = "//solutions/item[@val=\"low\"]";
		//xpath = "author [((last() - 4) <= position()) and (position() <= last())]";
		//xpath = "/project/participants/participant[2]/FirstName";
		String xpath = "//Participant[string-length(FirstName)>=8]";

		Path path = new Path(xpath);
		
		String json = oson.print(path);
	}
	

}
