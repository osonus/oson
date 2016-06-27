package ca.oson.json;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ca.oson.json.charstring.CharacterTest;
import ca.oson.json.charstring.StringTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	CharacterTest.class,
	StringTest.class
})
public class CharacterStringTestSuite {

}
