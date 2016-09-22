package ca.oson.json;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ca.oson.json.asserts.AssertTest;
import ca.oson.json.merge.GeolocationMergeTest;
import ca.oson.json.object.MapTest;
import ca.oson.json.object.SetTest;
import ca.oson.json.object.StringBuilderBufferTest;
import ca.oson.json.object.URLURITest;
import ca.oson.json.query.QueryTest;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	StringBuilderBufferTest.class,
	SetTest.class,
	MapTest.class,
	URLURITest.class,
	QueryTest.class,
	AssertTest.class,
	GeolocationMergeTest.class
})
public class ObjectTestSuite {

}
