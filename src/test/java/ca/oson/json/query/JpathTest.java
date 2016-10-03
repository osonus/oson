package ca.oson.json.query;

import org.junit.Test;

import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.path.Path;
import ca.oson.json.support.TestCaseBase;

public class JpathTest extends TestCaseBase {

	@Test
	public void testJPath() {
		String jpath = "$.store.book[*].author";
		Path path = new Path(jpath);
		
		oson.pretty().setDefaultType(JSON_INCLUDE.NON_NULL);
		
		String json = oson.print(path);

		jpath = "$..book[-1:]";
		path = new Path(jpath);
		
		json = oson.print(path);
	}
	
}
