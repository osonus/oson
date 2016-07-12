package ca.oson.json;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ca.oson.json.object.MapTest;
import ca.oson.json.object.SetTest;
import ca.oson.json.object.StringBuilderBufferTest;
import ca.oson.json.object.URLURITest;


@RunWith(Suite.class)
@Suite.SuiteClasses({
	StringBuilderBufferTest.class,
	SetTest.class,
	MapTest.class,
	URLURITest.class
})
public class ObjectTestSuite {

}
