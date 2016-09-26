package ca.oson.json;

import java.util.Map;

public class MapData <T> {
	public Map<String, T> map;
	public String key;
	public T value;
	
	public MapData(Map<String, T> map, String key, T value) {
		this.map = map;
		this.key = key;
		this.value = value;
	}
}
