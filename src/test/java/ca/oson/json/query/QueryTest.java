package ca.oson.json.query;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import ca.oson.json.OsonSearch;
import ca.oson.json.domain.VolumeContainer;
import ca.oson.json.support.TestCaseBase;
import ca.oson.json.util.ArrayToJsonMap;

public class QueryTest extends TestCaseBase {
	@Test
	public void testQueryVolume() {
		URL url = getClass().getResource("../volume.txt");
		File file = new File(url.getPath());

		VolumeContainer vc = oson.readValue(file, VolumeContainer.class);
		String json = oson.serialize(vc);

		String attr = "volumes.support.status";
		
		String found = OsonSearch.query(json, attr);

		assertEquals("\"supported\"", found);
		
		found = OsonSearch.query(json, "storage_pool");
		assertEquals("[\"pfm9253_pfm9254_new\",\"KVM\",\"Chassis2_IBMi\"]", found);

		found = OsonSearch.query(json, "support.reasons");
		assertEquals("[\"This volume is not a candidate for management because it is already attached to a virtual machine.  To manage this volume with PowerVC, select the virtual machine to which the volume is attached for management. The attached volume will be automatically included for management.\"]", found);
	}
	
	@Test
	public void testQueryUsername() {
		String json = "[{\"id\":1, \"username\":\"user1\"},{\"id\":2, \"username\":\"user2\"},{\"id\":3, \"username\":\"user3\"}]";
		String found = OsonSearch.query(json, "username");
		String expected = "[\"user1\",\"user2\",\"user3\"]";
		assertEquals(expected, found);
		
		found = OsonSearch.query(json, "id.username");
		assertEquals(expected, found);
		
		found = OsonSearch.query(json, "id");
		expected = "[\"1\",\"2\",\"3\"]";
		assertEquals(expected, found);
		
		found = OsonSearch.query(json, "username.id");
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
		
		String found = OsonSearch.search(json, attr);

		assertEquals("[\"supported\",\"not_supported\"]", found);
		
		found = OsonSearch.search(json, "storage_pool");
		assertEquals("[\"pfm9253_pfm9254_new\",\"KVM\",\"Chassis2_IBMi\"]", found);

		found = OsonSearch.search(json, "support.reasons");
		assertEquals("[\"This volume is not a candidate for management because it is already attached to a virtual machine.  To manage this volume with PowerVC, select the virtual machine to which the volume is attached for management. The attached volume will be automatically included for management.\"]", found);

		found = OsonSearch.search(json, "mapped_wwpns");
		String expected = "[\"2101001B32BD4280\",\"2100001B329D4280\",\"2101001B32BD637E\",\"2100001B329D637E\"]";
		assertEquals(expected, found);
		
		found = OsonSearch.search(json, "storage_pool.mapped_wwpns");
		expected = null;
		assertEquals(expected, found);
		
		found = OsonSearch.search(json, "status");
		expected = "[\"available\",\"supported\",\"in-use\",\"not_supported\"]";
		assertEquals(expected, found);
		
		found = OsonSearch.search(json, "volumes.status");
		assertEquals(expected, found);
		
		found = OsonSearch.search(json, "volumes.status", true);
		expected = "[\"available\",\"in-use\"]";
		assertEquals(expected, found);
		
		found = OsonSearch.search(json, "status", true);
		expected = "[\"available\",\"in-use\"]";
		assertEquals(expected, found);
		
		found = OsonSearch.search(json, "volumes.name.status");
		expected = null;
		assertEquals(expected, found);
	}
	
	@Test
	public void testSearchUsername() {
		String json = "[{\"id\":1, \"username\":\"user1\"},{\"id\":2, \"username\":\"user2\"},{\"id\":3, \"username\":\"user3\"}]";
		String found = OsonSearch.search(json, "username");
		String expected = "[\"user1\",\"user2\",\"user3\"]";
		assertEquals(expected, found);
		
		found = OsonSearch.search(json, "id.username");
		assertEquals(null, found);
		
		found = OsonSearch.search(json, "id");
		expected = "[1,2,3]";
		assertEquals(expected, found);
		
		found = OsonSearch.search(json, "username.id");
		assertEquals(null, found);
	}
	
	@Test
	public void testGet() {
		URL url = getClass().getResource("../sample-program-details-data.json");
		File file = new File(url.getPath());

		String json = oson.readValue(file);

		String attr = "programQuestions";
		
		String found = OsonSearch.search(json, attr);
		
		System.err.println(found);
		
		if (found != null) {
			List<Map.Entry> list = null;
			Object obj = oson.getListMapObject(found);
			if (Map.class.isAssignableFrom(obj.getClass())) {
				list = ArrayToJsonMap.map2List((Map)obj);
				
				for (Map.Entry o: list) {
					System.err.println(o.getKey() + " => " + o.getValue());
				}
				
				System.err.println("done\n\n");
			}
		}
		
		Set set = OsonSearch.get(json, attr);
		
		List<Map.Entry> list = null;
		if (set != null) {
			list = new ArrayList();
			for (Object obj: set) {
				if (Map.class.isAssignableFrom(obj.getClass())) {
					list.addAll(ArrayToJsonMap.map2List((Map)obj));
				}
			}
			
			for (Map.Entry obj: list) {
				System.err.println(obj.getKey() + " => " + obj.getValue());
			}
		}
	}
	
}
