package ca.oson.json;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ca.oson.json.annotation.ClassMapperSeDeseTest;
import ca.oson.json.annotation.FieldMapperSeDeseTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	ClassMapperSeDeseTest.class,
	FieldMapperSeDeseTest.class
})
public class AnnotationTestSuite {

}
