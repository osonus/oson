package ca.oson.json.object;

import java.util.List;
import java.lang.reflect.Type;

import org.junit.Test;

import ca.oson.json.ComponentType;
import ca.oson.json.OsonAssert;
import ca.oson.json.OsonAssert.MODE;
import ca.oson.json.annotation.FieldMapper;
import ca.oson.json.support.TestCaseBase;

public class LoginRstTest extends TestCaseBase {

	@Test
	public void testDeserializeLoginRst() {
		String json = "{\n    \"user\": {\n        \"id\": \"1\",\n        \"username\": \"admin\",\n        \"role_id\": \"1\",\n        \"Employee\": [{\n            \"id\": \"1\",\n            \"user_id\": \"1\",\n            \"signature\": \"\",\n            \"last_view\": null\n        }]\n    },\n    \"isLogin\": \"1\"\n}";

		Type listType = new ComponentType(LoginRst.class);
		
		LoginRst loginRst =  oson.fromJson(json, listType);
		
		String actual = oson.toJson(loginRst);
		
		OsonAssert.assertEquals(actual, json, MODE.KEY_SORT);
	}
}


class LoginRst {
    @FieldMapper(name = "user")
    public User user;

    @FieldMapper(name = "isLogin")
    public String isLogin;

}

class User {
	@FieldMapper(name = "id")
    public String id;

    @FieldMapper(name = "username")
    public String username;

    @FieldMapper(name = "role_id")
    public String role_id;

    @FieldMapper(name = "Employee")
    List<Employee> employee;
}

class Employee{
    @FieldMapper(name = "id")
    public String id;

    @FieldMapper(name = "user_id")
    public String user_id;

    @FieldMapper(name = "signature")
    public String signature;

    @FieldMapper(name = "last_view")
    public String last_view;
}