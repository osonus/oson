package ca.oson.json.domain;

import ca.oson.json.FieldMapper;
import ca.oson.json.Oson.BOOLEAN;

public class AnyBean {
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



