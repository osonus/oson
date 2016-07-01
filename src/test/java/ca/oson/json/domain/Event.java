package ca.oson.json.domain;

import ca.oson.json.FieldMapper;
import ca.oson.json.Oson.BOOLEAN;

public class Event {
	public String name;
	public String source;

	public Event(String name, String source) {
		this.name = name;
		this.source = source;
	}
	
    @Override
    @FieldMapper(ignore = BOOLEAN.TRUE)
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("{");
        stringBuilder.append("\"name\":\"" + name + "\",");
        stringBuilder.append("\"source\":\"" + source + "\"");
        stringBuilder.append("}");

        return stringBuilder.toString();
    }
}
