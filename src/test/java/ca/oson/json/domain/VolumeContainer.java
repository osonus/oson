package ca.oson.json.domain;

import java.util.List;

import ca.oson.json.annotation.ClassMapper;

@ClassMapper(max=12)
public class VolumeContainer {
	public List<Volume> volumes;
}
