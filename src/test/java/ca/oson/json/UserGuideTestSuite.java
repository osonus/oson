package ca.oson.json;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ca.oson.json.userguide.ArrayTest;
import ca.oson.json.userguide.CollectionsTest;
import ca.oson.json.userguide.ConfigInheritanceTest;
import ca.oson.json.userguide.DeserializeFunctionTest;
import ca.oson.json.userguide.NewInstanceTest;
import ca.oson.json.userguide.ObjectTest;
import ca.oson.json.userguide.OptionalTest;
import ca.oson.json.userguide.PrimitivesTest;
import ca.oson.json.userguide.SerializeCarTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	PrimitivesTest.class,
	ObjectTest.class,
	ArrayTest.class,
	CollectionsTest.class,
	SerializeCarTest.class,
	NewInstanceTest.class,
	DeserializeFunctionTest.class,
	ConfigInheritanceTest.class,
	OptionalTest.class
})
public class UserGuideTestSuite {

}
