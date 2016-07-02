package ca.oson.json.userguide;

import java.util.ArrayList;

import org.junit.Test;

import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.domain.Volume;
import ca.oson.json.domain.VolumeContainer;
import ca.oson.json.support.TestCaseBase;

public class ConfigInheritanceTest extends TestCaseBase {

	
	@Test
	public void testSerializationObject() {
		VolumeContainer container = new VolumeContainer();
		Volume volume = new Volume();
		volume.size = 200;
		
		container.volumes = new ArrayList<Volume>();
		container.volumes.add(volume);
		
		String expected = "{\"volumes\":[{\"size\":12}]}";
		
		String json = oson.setDefaultType(JSON_INCLUDE.NON_NULL).serialize(container);
		
		//System.err.println(json);

		assertEquals(expected, json);
	}
	
}
