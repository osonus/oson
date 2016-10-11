package ca.oson.json.query;

import org.junit.Test;

import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.path.Path;
import ca.oson.json.support.TestCaseBase;

public class JpathTest extends TestCaseBase {

	@Test
	public void testJPath() {
		oson.pretty().setDefaultType(JSON_INCLUDE.NON_DEFAULT);
		String json;
		String jpath;
		Path path;
		
//		jpath = "$.store.book[*].author";
//		path = new Path(jpath);
//		json = oson.print(path);
//
//		jpath = "$..book[-1:]";
//		path = new Path(jpath);
//		json = oson.print(path);
		
		jpath = "$..book.length()";
		jpath = "$..author";
		jpath = "$.store.*";
		jpath = "$.store..price";
		jpath = "$..book[2]";
		jpath = "$..book[0,1]";
		jpath = "$..book[:2]";
		jpath = "$..book[1:2]";
		jpath = "$..book[-2:]";
		jpath = "$..book[2:]";
		jpath = "$..book[?(@.isbn)]";
		jpath = "$.store.book[?(@.price < 10)]";
		jpath = "$..book[?(@.price <= $['expensive'])]";
		
		jpath = "$..book[?(@.author =~ /.*REES/i)]";
		path = new Path(jpath);
		json = oson.print(path);
		
		jpath = "$..*";
		path = new Path(jpath);
		json = oson.print(path);
	}
	
}
