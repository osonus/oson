package ca.oson.json.userguide;

import org.junit.Test;

import ca.oson.json.FieldMapper;
import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.support.TestCaseBase;

public class NewInstanceTest extends TestCaseBase {

	@Test
	public void testSerializeAnyBean() {
		//oson.asGson();
		
		AnyBean anyBean = new AnyBean("Any Name", 35);
		anyBean.setType("Java");

		String json = oson.serialize(anyBean);

		String expected = "{\"name\":\"Any Name\",\"type\":\"Java\",\"age\":35}";

		assertEquals(expected, json);
	}
	
	@Test
	public void testDeserializeAnyBean() {
		//oson.asGson();
		
		AnyBean expected = new AnyBean("Any Name", 35);
		expected.setType("Java");

		String json = "{\"name\":\"Any Name\",\"type\":\"Java\",\"age\":35}";
		
		AnyBean result = oson.deserialize(json, AnyBean.class);

		assertEquals(expected.toString(), result.toString());
	}
	
	
	@Test
	public void testSerializeAnyPoint() {
		//oson.asGson();
		Point point = new Point(12.5d, 35.5d);
		
		AnyPoint anyPoint = new AnyPoint(point);

		String json = oson.serialize(anyPoint);
		
		System.err.println(json);

		String expected = "{\"name\":\"Any Name\",\"type\":\"Java\",\"age\":35}";

		//assertEquals(expected, json);
	}
	
	
	@Test
	public void testDeserializeAnyPoint() {
		//oson.asGson();
		Point point = new Point(12.5d, 35.5d);
		
		AnyPoint expected = new AnyPoint(point);

		String json = "{\"point\":{\"x\":12.5,\"y\":35.5}}";
		
		AnyPoint result = oson.deserialize(json, AnyPoint.class);

		assertEquals(expected.toString(), result.toString());
	}
}


class AnyBean {
    private final String name;
    private final int age;
    private String type;

    public AnyBean(String name, int age)
    {
      this.name = name;
      this.age = age;
    }

    public void setType(String type) {
      this.type = type;
    }
    
    @FieldMapper(ignore = BOOLEAN.TRUE)
    public String toString() {
    	return "{\"name\":\"" + name + "\",\"type\":\"" + type + "\",\"age\":" + age + "}";
    }
  }


class AnyPoint {
    private final Point point;

    public AnyPoint(Point point)
    {
      this.point = point;
    }

    @FieldMapper(ignore = BOOLEAN.TRUE)
    public String toString() {
    	return "{\"x\":" + point.x + ",\"y\":" + point.y + "}";
    }
}

class Point {
	double x;
	double y;

	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
}