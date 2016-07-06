package ca.oson.json;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ca.oson.json.gson.CommentsTest;
import ca.oson.json.gson.DefaultDateTypeAdapterTest;
import ca.oson.json.gson.DefaultInetAddressTypeAdapterTest;
import ca.oson.json.gson.GsonBuilderTest;
import ca.oson.json.gson.GsonTypeAdapterTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	CommentsTest.class,
	DefaultDateTypeAdapterTest.class,
	DefaultInetAddressTypeAdapterTest.class,
	GsonBuilderTest.class,
	GsonTypeAdapterTest.class
	
})
public class GsonTestSuite {

}
