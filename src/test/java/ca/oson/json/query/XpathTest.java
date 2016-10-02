package ca.oson.json.query;

import org.junit.Test;

import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.path.Path;
import ca.oson.json.support.TestCaseBase;

public class XpathTest extends TestCaseBase {
	
	@Test
	public void testPath() {
		String xpath = "/bookstore/book[1]";
		Path path = new Path(xpath);
		
		oson.pretty().setDefaultType(JSON_INCLUDE.NON_NULL);
		
		String json;// = oson.print(path);

		xpath = "//book[position()<3]";
		path = new Path(xpath);
		
		json = oson.print(path);
	}

}
