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
import ca.oson.json.gson.ObjectTypeAdapterTest;
import ca.oson.json.gson.OverrideCoreTypeAdaptersTest;
import ca.oson.json.gson.functional.ArrayTest;
import ca.oson.json.gson.functional.CircularReferenceTest;
import ca.oson.json.gson.functional.CollectionTest;
import ca.oson.json.gson.functional.ConcurrencyTest;
import ca.oson.json.gson.functional.CustomDeserializerTest;
import ca.oson.json.gson.functional.CustomTypeAdaptersTest;
import ca.oson.json.gson.functional.DefaultTypeAdaptersTest;
import ca.oson.json.gson.functional.EnumTest;

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
	LongSerializationPolicyTest.class,
	ObjectTypeAdapterTest.class,
	OverrideCoreTypeAdaptersTest.class,
	ArrayTest.class,
	CircularReferenceTest.class,
	CollectionTest.class,
	ConcurrencyTest.class,
	CustomDeserializerTest.class,
	CustomTypeAdaptersTest.class,
	DefaultTypeAdaptersTest.class,
	EnumTest.class
	
})
public class GsonTestSuite {

}
