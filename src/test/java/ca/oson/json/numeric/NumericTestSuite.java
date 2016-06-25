package ca.oson.json.numeric;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

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
	AtomicLongTest.class
})
public class NumericTestSuite {

}
