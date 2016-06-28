package ca.oson.json;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ca.oson.json.listarraymap.ListObjectTest;
import ca.oson.json.listarraymap.ListTest;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	ListTest.class,
	ListObjectTest.class
})
public class ListArrayMapTestSuite {

}
