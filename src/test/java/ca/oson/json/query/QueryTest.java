package ca.oson.json.query;

import java.io.File;
import java.net.URL;

import org.junit.Test;

import ca.oson.json.OsonQuery;
import ca.oson.json.domain.VolumeContainer;
import ca.oson.json.support.TestCaseBase;

public class QueryTest extends TestCaseBase {
	@Test
	public void testQueryVolume() {
		URL url = getClass().getResource("../volume.txt");
		File file = new File(url.getPath());

		VolumeContainer vc = oson.readValue(file, VolumeContainer.class);
		String json = oson.serialize(vc);

		String attr = "volumes.support.status";
		
		String found = OsonQuery.query(json, attr);

		assertEquals("\"supported\"", found);
		
		found = OsonQuery.query(json, "storage_pool");
		assertEquals("[\"pfm9253_pfm9254_new\",\"KVM\",\"Chassis2_IBMi\"]", found);

		found = OsonQuery.query(json, "support.reasons");
		assertEquals("[\"This volume is not a candidate for management because it is already attached to a virtual machine.  To manage this volume with PowerVC, select the virtual machine to which the volume is attached for management. The attached volume will be automatically included for management.\"]", found);
	}
	
	@Test
	public void testQueryUsername() {
		String json = "[{\"id\":1, \"username\":\"user1\"},{\"id\":2, \"username\":\"user2\"},{\"id\":3, \"username\":\"user3\"}]";
		String found = OsonQuery.query(json, "username");
		String expected = "[\"user1\",\"user2\",\"user3\"]";
		assertEquals(expected, found);
		
		found = OsonQuery.query(json, "id.username");
		assertEquals(expected, found);
		
		found = OsonQuery.query(json, "id");
		expected = "[\"1\",\"2\",\"3\"]";
		assertEquals(expected, found);
		
		found = OsonQuery.query(json, "username.id");
		expected = "[\"2\",\"3\"]";
		assertEquals(expected, found);
	}
	
	@Test
	public void testSearchVolume() {
		URL url = getClass().getResource("../volume.txt");
		File file = new File(url.getPath());

		VolumeContainer vc = oson.readValue(file, VolumeContainer.class);
		String json = oson.serialize(vc);

		String attr = "volumes.support.status";
		
		String found = OsonQuery.search(json, attr);

		assertEquals("[\"supported\",\"not_supported\"]", found);
		
		found = OsonQuery.search(json, "storage_pool");
		assertEquals("[\"pfm9253_pfm9254_new\",\"KVM\",\"Chassis2_IBMi\"]", found);

		found = OsonQuery.search(json, "support.reasons");
		assertEquals("[\"This volume is not a candidate for management because it is already attached to a virtual machine.  To manage this volume with PowerVC, select the virtual machine to which the volume is attached for management. The attached volume will be automatically included for management.\"]", found);

		found = OsonQuery.search(json, "mapped_wwpns");
		String expected = "[\"2101001B32BD4280\",\"2100001B329D4280\",\"2101001B32BD637E\",\"2100001B329D637E\"]";
		assertEquals(expected, found);
		
		found = OsonQuery.search(json, "storage_pool.mapped_wwpns");
		expected = null;
		assertEquals(expected, found);
		
		found = OsonQuery.search(json, "status");
		expected = "[\"supported\",\"available\",\"not_supported\",\"in-use\"]";
		assertEquals(expected, found);
		
		found = OsonQuery.search(json, "volumes.status");
		assertEquals(expected, found);
		
		found = OsonQuery.search(json, "volumes.status", true);
		expected = "[\"available\",\"in-use\"]";
		assertEquals(expected, found);
		
		found = OsonQuery.search(json, "volumes.name.status");
		expected = null;
		assertEquals(expected, found);
	}
	
	@Test
	public void testSearchUsername() {
		String json = "[{\"id\":1, \"username\":\"user1\"},{\"id\":2, \"username\":\"user2\"},{\"id\":3, \"username\":\"user3\"}]";
		String found = OsonQuery.search(json, "username");
		String expected = "[\"user1\",\"user2\",\"user3\"]";
		assertEquals(expected, found);
		
		found = OsonQuery.search(json, "id.username");
		assertEquals(null, found);
		
		found = OsonQuery.search(json, "id");
		expected = "[1,2,3]";
		assertEquals(expected, found);
		
		found = OsonQuery.search(json, "username.id");
		assertEquals(null, found);
	}
	
}
