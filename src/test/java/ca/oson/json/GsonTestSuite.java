package ca.oson.json;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import ca.oson.json.gson.*;
import ca.oson.json.gson.functional.*;

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
	EnumTest.class,
	EscapingTest.class,
	ExclusionStrategyFunctionalTest.class,
	ExposeFieldsTest.class,
	FieldExclusionTest.class,
	FieldNamingTest.class,
	InheritanceTest.class,
	InstanceCreatorTest.class,
	InterfaceTest.class,
	InternationalizationTest.class,
	JavaUtilConcurrentAtomicTest.class,
	JavaUtilTest.class,
	JsonAdapterAnnotationOnClassesTest.class,
	JsonAdapterAnnotationOnFieldsTest.class,
	JsonParserTest.class,
	LeniencyTest.class,
	MapAsArrayTypeAdapterTest.class,
	MapTest.class,
	MoreSpecificTypeSerializationTest.class,
	NamingPolicyTest.class,
	NullObjectAndFieldTest.class,
	ObjectTest.class,
	ParameterizedTypesTest.class,
	PrintFormattingTest.class,
	RawSerializationTest.class,
	ReadersWritersTest.class,
	SerializedNameTest.class,
	StringTest.class,
	ThrowableFunctionalTest.class,
	TypeAdapterPrecedenceTest.class,
	TypeHierarchyAdapterTest.class,
	TypeVariableTest.class,
	UncategorizedTest.class,
	VersioningTest.class
})
public class GsonTestSuite {

}
