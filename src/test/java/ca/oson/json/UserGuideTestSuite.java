package ca.oson.json;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ca.oson.json.userguide.ArrayTest;
import ca.oson.json.userguide.CollectionsTest;
import ca.oson.json.userguide.ObjectTest;
import ca.oson.json.userguide.PrimitivesTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	PrimitivesTest.class,
	ObjectTest.class,
	ArrayTest.class,
	CollectionsTest.class
})
public class UserGuideTestSuite {

}
