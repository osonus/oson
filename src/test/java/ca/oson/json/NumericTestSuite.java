package ca.oson.json;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ca.oson.json.numeric.AtomicIntegerTest;
import ca.oson.json.numeric.AtomicLongTest;
import ca.oson.json.numeric.BigDecimalTest;
import ca.oson.json.numeric.BigIntegerTest;
import ca.oson.json.numeric.ByteTest;
import ca.oson.json.numeric.DoubleTest;
import ca.oson.json.numeric.FloatTest;
import ca.oson.json.numeric.IntegerTest;
import ca.oson.json.numeric.LongTest;
import ca.oson.json.numeric.PrecisionScaleTest;
import ca.oson.json.numeric.ScaleTest;
import ca.oson.json.numeric.ShortTest;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	IntegerTest.class,
	LongTest.class,
	ShortTest.class,
	DoubleTest.class,
	FloatTest.class,
	BigIntegerTest.class,
	BigDecimalTest.class,
	ByteTest.class,
	AtomicIntegerTest.class,
	AtomicLongTest.class,
	ScaleTest.class,
	PrecisionScaleTest.class
})
public class NumericTestSuite {

}
