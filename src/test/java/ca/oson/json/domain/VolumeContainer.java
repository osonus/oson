package ca.oson.json.domain;

import java.util.List;

import ca.oson.json.ClassMapper;

@ClassMapper(max=12)
public class VolumeContainer {
	public List<Volume> volumes;
}
