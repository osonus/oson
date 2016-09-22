package ca.oson.json.object;

import java.lang.reflect.Type;
import java.util.List;

import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import ca.oson.json.OsonAssert;
import ca.oson.json.OsonAssert.MODE;
import ca.oson.json.support.TestCaseBase;

public class LoginRstTest extends TestCaseBase {

	@Test
	public void testDeserializeLoginRst() {
		String json = "{\n    \"user\": {\n        \"id\": \"1\",\n        \"username\": \"admin\",\n        \"role_id\": \"1\",\n        \"Employee\": [{\n            \"id\": \"1\",\n            \"user_id\": \"1\",\n            \"signature\": \"\",\n            \"last_view\": null\n        }]\n    },\n    \"isLogin\": \"1\"\n}";

		Type listType = new TypeToken<LoginRst>(){}.getType();
		
		LoginRst loginRst =  oson.asGson().fromJson(json, listType);
		
		String actual = oson.toJson(loginRst);
		
		OsonAssert.assertEquals(actual, json, MODE.KEY_SORT);
	}
}


class LoginRst {
    @SerializedName("user")
    public User user;

    @SerializedName("isLogin")
    public String isLogin;

}

class User {
    @SerializedName("id")
    public String id;

    @SerializedName("username")
    public String username;

    @SerializedName("role_id")
    public String role_id;

    @SerializedName("Employee")
    List<Employee> employee;
}

class Employee{
    @SerializedName("id")
    public String id;

    @SerializedName("user_id")
    public String user_id;

    @SerializedName("signature")
    public String signature;

    @SerializedName("last_view")
    public String last_view;
}