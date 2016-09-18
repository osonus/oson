package ca.oson.json.object;

import java.util.Arrays;
import java.util.Base64;
import java.util.Properties;

import javax.xml.bind.DatatypeConverter;

import ca.oson.json.Oson;
import ca.oson.json.OsonAssert;
import ca.oson.json.OsonAssert.MODE;
import ca.oson.json.support.TestCaseBase;

import org.bson.BsonArray;
import org.bson.BsonBoolean;
import org.bson.BsonDateTime;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.Document;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

public class DocumentTest extends TestCaseBase {
	@Test
	public void testBsonDocumentDeSerialize() {
		BsonDocument document = new BsonDocument().append("a", new BsonString("MongoDB"))
                .append("b", new BsonArray(Arrays.asList(new BsonInt32(1), new BsonInt32(2))))
                .append("c", new BsonBoolean(true))
                .append("d", new BsonDateTime(0));

		String json = oson.useAttribute(false).setValueOnly(true).serialize(document);

		String expected = "{\"a\":\"MongoDB\",\"b\":[1,2],\"c\":true,\"d\":0}";

		assertEquals(expected, json);

		BsonDocument document2 = oson.deserialize(json, BsonDocument.class);

		assertEquals(expected, oson.serialize(document2));
	}
	
	@Test
	public void testDocumentDeSerialize() {
		Document document = new Document("email", "osonus@gmail.com");
		document.append("name", "David He").append("emailVerified", false);

		String json = oson.useAttribute(false).setValueOnly(true).serialize(document);
		
		String expected = "{\"email\":\"osonus@gmail.com\",\"name\":\"David He\",\"emailVerified\":false}";

		assertEquals(expected, json);

		Document document2 = oson.deserialize(json, Document.class);
		OsonAssert.assertEquals(expected, oson.serialize(document2), MODE.KEY_SORT);
	}
	
	
//	   @Test
//	   public void testSerialize64String() {
//		   // base64 encoded string
//		   String encoded_string = "";
//		   
//		   String str = new String(DatatypeConverter.parseBase64Binary("user:123"));
//		   System.out.println(str);
//		   String res = DatatypeConverter.printBase64Binary(str.getBytes());
//		   System.out.println(res);
//		   
//		   byte[]   bytesEncoded = Base64.getEncoder().encode(str.getBytes());
//		   encoded_string = new String(bytesEncoded);
//		   System.out.println("ecncoded value is " + encoded_string);
//
//		   // Decode data on other side, by processing encoded data
//		   byte[] valueDecoded= Base64.getDecoder().decode(bytesEncoded );
//		   System.out.println("Decoded value is " + new String(valueDecoded));
//		   
//		   
//		   byte[] someByteArray = str.getBytes();
//		// encode with padding
//		   String encoded = Base64.getEncoder().encodeToString(someByteArray);
//		   
//		   System.out.println("encoded is " + encoded);
//
//		   // encode without padding
//		   String encoded2 = Base64.getEncoder().withoutPadding().encodeToString(someByteArray);
//
//		   System.out.println("encoded2 is " + encoded2);
//		   
//		   // decode a String
//		   byte [] barr = Base64.getDecoder().decode(encoded2); 
//		   
//		   System.out.println("Decoded value is " + new String(barr));
//		   
//		   
//		   String image_name="Hello.jpg";
//		   
//		   JSONObject jsonObject=new JSONObject();
//	        String jsonString=null;
//	        try {
//	            jsonObject.put("image_name",image_name);
//	            jsonObject.put("encoded_string",encoded_string);
//	            jsonString=jsonObject.toString();
//	        } catch (JSONException e) {
//	            e.printStackTrace();
//	        }
//	        
//		   System.err.println(jsonString);
//	   }

}
