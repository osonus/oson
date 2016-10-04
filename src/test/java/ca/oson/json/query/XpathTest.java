package ca.oson.json.query;

import org.junit.Test;

import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.path.Path;
import ca.oson.json.support.TestCaseBase;

public class XpathTest extends TestCaseBase {
	
	@Test
	public void testXPath() {
		String xpath = "/bookstore/book[1]";
		Path path = new Path(xpath);
		
		oson.pretty().setDefaultType(JSON_INCLUDE.NON_NULL);
		
		String json;// = oson.print(path);

		xpath = "//book[position()<3]";
		path = new Path(xpath);
		
		json = oson.print(path);
		
		//contains(.,"foo")
		// xpath = "*[contains('X Y Z', local-name())]/AABBCC";
		// xpath = "*[self::X | self::Y | self::Z]/AABBCC";
		// xpath = "//(X|Y|Z)/AABBCC";
		xpath = "(book/author)[last()]";
		// xpath = "//parent[(childA[contains(.,\"foo\")] or childB[contains(.,\"foo\")]) AND (childA[contains(.,\"bar\")] or childB[contains(.,\"bar\")])]";
		path = new Path(xpath);
		
		json = oson.print(path);
		
		
	}

}
