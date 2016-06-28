package ca.oson.json;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ca.oson.json.listarraymap.ArrayTest;
import ca.oson.json.listarraymap.ListObjectTest;
import ca.oson.json.listarraymap.ListTest;
import ca.oson.json.listarraymap.MapTest;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	ListTest.class,
	ListObjectTest.class,
	ArrayTest.class,
	MapTest.class
})
public class ListArrayMapTestSuite {

}
