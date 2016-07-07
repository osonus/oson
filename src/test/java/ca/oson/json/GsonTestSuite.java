package ca.oson.json;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ca.oson.json.gson.CommentsTest;
import ca.oson.json.gson.DefaultDateTypeAdapterTest;
import ca.oson.json.gson.DefaultInetAddressTypeAdapterTest;
import ca.oson.json.gson.GsonBuilderTest;
import ca.oson.json.gson.GsonTypeAdapterTest;
import ca.oson.json.gson.InnerClassExclusionStrategyTest;
import ca.oson.json.gson.JavaSerializationTest;
import ca.oson.json.gson.JsonArrayTest;
import ca.oson.json.gson.LongSerializationPolicyTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	CommentsTest.class,
	DefaultDateTypeAdapterTest.class,
	DefaultInetAddressTypeAdapterTest.class,
	GsonBuilderTest.class,
	GsonTypeAdapterTest.class,
	InnerClassExclusionStrategyTest.class,
	JavaSerializationTest.class,
	JsonArrayTest.class,
	LongSerializationPolicyTest.class
	
})
public class GsonTestSuite {

}
