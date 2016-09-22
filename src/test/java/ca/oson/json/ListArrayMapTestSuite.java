package ca.oson.json;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ca.oson.json.listarraymap.ArrayTest;
import ca.oson.json.listarraymap.FooBarTest;
import ca.oson.json.listarraymap.ListObjectTest;
import ca.oson.json.listarraymap.ListTest;
import ca.oson.json.listarraymap.MapTest;
import ca.oson.json.listarraymap.SurveyResultTest;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	ListTest.class,
	ListObjectTest.class,
	ArrayTest.class,
	MapTest.class,
	FooBarTest.class,
	SurveyResultTest.class
})
public class ListArrayMapTestSuite {

}
