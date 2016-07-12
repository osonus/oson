package ca.oson.json.object;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.BitSet;
import java.util.Locale;
import java.util.UUID;

import ca.oson.json.support.TestCaseBase;
import ca.oson.json.util.StringUtil;

public class URLURITest extends TestCaseBase {

	public void testURLSerialize() {
		String expected = "http://24.69.142.1";
		URL url = null;
		try {
			url = new URL(expected);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		
		String json = oson.serialize(url);
		
		assertEquals(StringUtil.doublequote(expected), json);
	}
	
	public void testURLDeserialize() {
		String json = "http://24.69.142.1";
		
		URL url = oson.deserialize(json, URL.class);
		
		assertEquals(json, url.toString());
	}
	
	public void testURISerialize() {
		String expected = "http://24.69.142.1";
		URI url = null;
		try {
			url = new URI(expected);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		
		String json = oson.serialize(url);
		
		assertEquals(StringUtil.doublequote(expected), json);
	}
	
	public void testURIDeserialize() {
		String json = "http://24.69.142.1";
		
		URI url = oson.deserialize(json, URI.class);
		
		assertEquals(json, url.toString());
	}
	
	
	public void testInetAddressSerialize() {
		String expected = "24.69.142.1";
		InetAddress url = null;
		try {
			url = InetAddress.getByName(expected);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}

		String json = oson.serialize(url);
		
		assertEquals(StringUtil.doublequote(expected), json);
	}
	
	public void testInetAddressDeserialize() {
		String json = "24.69.142.1";
		
		InetAddress url = oson.deserialize(json, InetAddress.class);
		
		// System.err.println(url.getHostAddress());
		
		assertEquals(json, url.getHostAddress());
	}
	
	
	
	public void testUUIDSerializeDeserialize() {
		UUID uid = UUID.randomUUID();

		//.setToStringAsSerializer(true)
		String json = oson.serialize(uid);
		
		// System.err.println(json);
		
		Object uid2 = oson.deserialize(json, UUID.class);
		
		assertEquals(UUID.class, uid2.getClass());
		
		assertEquals(json, StringUtil.doublequote(uid2.toString()));
	}
	
	
//	public void testLocaleSerializeDeserialize() {
//		Locale local = Locale.CANADA_FRENCH;
//
//		//.setToStringAsSerializer(true)
//		String json = oson.serialize(local);
//		
//		System.err.println(json);
//		
//		Object local2 = oson.deserialize(json, Locale.class);
//		
//		assertEquals(Locale.class, local2.getClass());
//		
//		assertEquals(json, StringUtil.doublequote(local2.toString()));
//	}
	
	
	public void testBitSetSerializeDeserialize() {
		BitSet bs = new BitSet();
	
		//.setToStringAsSerializer(true)
		//String json = oson.serialize(bs);
		
		//System.err.println(json);
		
//		Object bs2 = oson.deserialize(json, BitSet.class);
//		
//		assertEquals(BitSet.class, bs2.getClass());
//		
//		assertEquals(json, StringUtil.doublequote(bs2.toString()));
	}
	
}
