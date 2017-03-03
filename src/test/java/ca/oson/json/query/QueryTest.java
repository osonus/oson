package ca.oson.json.query;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import ca.oson.json.ComponentType;
import ca.oson.json.Oson;
import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.Oson.JSON_INCLUDE;
import ca.oson.json.OsonConvert;
import ca.oson.json.OsonSearch;
import ca.oson.json.domain.AdmissionsApplicationQuestionType;
import ca.oson.json.domain.VolumeContainer;
import ca.oson.json.support.TestCaseBase;
import ca.oson.json.util.ArrayToJsonMap;
import javafx.util.Pair;

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
	
	
	private String getFileContent(String fileName) {
		URL url = getClass().getResource(fileName);
		File file = new File(url.getPath());

		return oson.readValue(file);
	}
	
	@Test
	public void testGet() {
		String json = getFileContent("../sample-program-details-data.json");

		String attr = "programQuestions";
		
		String found = OsonSearch.search(json, attr);

		List<AdmissionsApplicationQuestionType> list = null;
		list = (List<AdmissionsApplicationQuestionType>) (((Map<String, Object>) Oson.getListMapObject(found))
				.entrySet().stream().map(x -> {
					AdmissionsApplicationQuestionType type = new AdmissionsApplicationQuestionType();
					type.setQuestionID(x.getKey());
					type.setAnswerText(x.getValue() + "");
					return type;
				}).collect(java.util.stream.Collectors.toList()));

		String expected = "[{\"questionID\":\"ShortAnswer1\",\"answerText\":\"Never\"},{\"questionID\":\"ShortAnswer2\",\"answerText\":\"Never\"},{\"questionID\":\"ShortAnswer3\",\"answerText\":\"Never\"},{\"questionID\":\"ShortAnswer4\",\"answerText\":\"Never\"},{\"questionID\":\"AdmitStatus\",\"answerText\":\"1\"}]";
		assertEquals(expected, oson.setDefaultType(JSON_INCLUDE.NON_EMPTY).serialize(list));
	}
	
	
	@Test
	public void testGet2() {
		String json = getFileContent("../sample-program-details-data.json");

		String attr = "programQuestions";

		Set found = OsonSearch.get(json, attr);

		List<AdmissionsApplicationQuestionType> list = null;
		list = (List<AdmissionsApplicationQuestionType>) (((Map<String, Object>) (found.iterator().next()))
				.entrySet().stream().map(x -> {
					AdmissionsApplicationQuestionType type = new AdmissionsApplicationQuestionType();
					type.setQuestionID(x.getKey());
					type.setAnswerText(x.getValue() + "");
					return type;
				}).collect(java.util.stream.Collectors.toList()));

		String expected = "[{\"questionID\":\"ShortAnswer1\",\"answerText\":\"Never\"},{\"questionID\":\"ShortAnswer2\",\"answerText\":\"Never\"},{\"questionID\":\"ShortAnswer3\",\"answerText\":\"Never\"},{\"questionID\":\"ShortAnswer4\",\"answerText\":\"Never\"},{\"questionID\":\"AdmitStatus\",\"answerText\":\"1\"}]";
		assertEquals(expected, oson.setDefaultType(JSON_INCLUDE.NON_EMPTY).serialize(list));
	}
	
	
	@Test
	public void testGet3() {
		String json = getFileContent("../sample-program-details-data.json");

		String attr = "programQuestions";

		Set found = OsonSearch.get(json, attr);

		List<AdmissionsApplicationQuestionType> list = null;
		
		List<Pair> keyValues = ArrayToJsonMap.map2Pairs(((Map<String, Object>) (found.iterator().next())));

		json = oson.clearAll().setDefaultType(JSON_INCLUDE.NON_EMPTY).useAttribute(false).serialize(keyValues);
		
		list = oson.setFieldMappers(AdmissionsApplicationQuestionType.class, BOOLEAN.TRUE, "questionID", "key", "answerText", "value")
			.deserialize(json, new ComponentType(List.class, AdmissionsApplicationQuestionType.class));
		json = oson.serialize(list);

		String expected = "[{\"questionID\":\"ShortAnswer1\",\"answerText\":\"Never\"},{\"questionID\":\"ShortAnswer2\",\"answerText\":\"Never\"},{\"questionID\":\"ShortAnswer3\",\"answerText\":\"Never\"},{\"questionID\":\"ShortAnswer4\",\"answerText\":\"Never\"},{\"questionID\":\"AdmitStatus\",\"answerText\":\"1\"}]";
		assertEquals(expected, oson.serialize(list));
	}
	
	
	@Test
	public void testFlatten() {
		String json = getFileContent("../sample-additional-info-data.json");

		json = OsonConvert.flatten(json);

		List<AdmissionsApplicationQuestionType> list = (List<AdmissionsApplicationQuestionType>) (((Map<String, Object>) Oson.getListMapObject(json))
				.entrySet().stream().map(x -> {
					AdmissionsApplicationQuestionType type = new AdmissionsApplicationQuestionType();
					type.setQuestionID(x.getKey());
					type.setAnswerText(x.getValue() + "");
					return type;
				}).collect(java.util.stream.Collectors.toList()));

		oson.clearAll().setDefaultType(JSON_INCLUDE.NON_EMPTY);
		json = oson.serialize(list);

		String expected = "[{\"questionID\":\"Aboriginal\",\"answerText\":\"false\"},{\"questionID\":\"PrevAttend\",\"answerText\":\"false\"},{\"questionID\":\"PrevEmpl\",\"answerText\":\"false\"},{\"questionID\":\"EdInterrupt\",\"answerText\":\"false\"},{\"questionID\":\"Withdraw\",\"answerText\":\"false\"},{\"questionID\":\"Agent\",\"answerText\":\"false\"},{\"questionID\":\"Scholar\",\"answerText\":\"false\"},{\"questionID\":\"application_additional_information\",\"answerText\":\"C856F028-4E6F-4BFD-8942-F3EF35C40541\"}]";
		assertEquals(expected, oson.serialize(list));
	}
	
	
	@Test
	public void testFlattenNotNullFilter() {
		String json = getFileContent("../additional_information.json");
		
		Map<String, Object> filters = new HashMap();
		// null means removing this attribute
		filters.put("formDataSchemaVersions", null);
		// put any name changes for attributes, dot-notation supported
		
		json = OsonConvert.flatten(OsonConvert.filter(json, filters));

		List<AdmissionsApplicationQuestionType> list = (List<AdmissionsApplicationQuestionType>) (((Map<String, Object>) Oson.getListMapObject(json))
				.entrySet().stream().map(x -> {
					AdmissionsApplicationQuestionType type = new AdmissionsApplicationQuestionType();
					type.setQuestionID(x.getKey());
					type.setAnswerText(x.getValue() + "");
					return type;
				}).collect(java.util.stream.Collectors.toList()));

		Oson oson = (new Oson()).setDefaultType(JSON_INCLUDE.NON_EMPTY);
		json = oson.serialize(list);

		String expected = "[{\"questionID\":\"Aboriginal\",\"answerText\":\"false\"},{\"questionID\":\"Agent\",\"answerText\":\"false\"},{\"questionID\":\"DisabilitySvcs\",\"answerText\":\"false\"}]";
		assertEquals(expected, oson.serialize(list));
	}
	
}
