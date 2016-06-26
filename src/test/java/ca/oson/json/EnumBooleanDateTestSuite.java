package ca.oson.json;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ca.oson.json.enumbooleandate.BooleanTest;
import ca.oson.json.enumbooleandate.DateTest;
import ca.oson.json.enumbooleandate.EnumTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	EnumTest.class,
	BooleanTest.class,
	DateTest.class
})
public class EnumBooleanDateTestSuite {

}
