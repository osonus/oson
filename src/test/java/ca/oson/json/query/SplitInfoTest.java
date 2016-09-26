package ca.oson.json.query;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import ca.oson.json.OsonConvert;
import ca.oson.json.support.TestCaseBase;

public class SplitInfoTest extends TestCaseBase {

	@Test
	public void testSplitInfo() {
		String json = "{\"meta\": {\"code\": 200}, \"data\": {\"username\": \"jon_perron\", \"bio\": \"George Brown\", \"website\": \"http://jonathanperron.ca\", \"profile_picture\": \"https://scontent.cdninstagram.com/t51.2885-19/10865203_1593678807517862_1617189064_a.jpg\", \"full_name\": \"Jonathan Perron\", \"counts\": {\"media\": 30, \"followed_by\": 51, \"follows\": 67}, \"id\": \"1510848960\"}}";
		
		String root = "data";
		Map<String, Object> filters = new HashMap<>();
		filters.put("id", null);
		filters.put("counts.media", null);
		filters.put("counts.follows", null);
		filters.put("counts.followed_by", "counts");
		
		String splitted = OsonConvert.filter(json, filters, root);
		
		String expected = "{\"username\":\"jon_perron\",\"bio\":\"George Brown\",\"website\":\"http://jonathanperron.ca\",\"profile_picture\":\"https://scontent.cdninstagram.com/t51.2885-19/10865203_1593678807517862_1617189064_a.jpg\",\"full_name\":\"Jonathan Perron\",\"counts\":{\"followed_by\":51}}";
		
		assertEquals(expected, splitted);
	}
}
