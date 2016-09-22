package ca.oson.json.merge;

import java.io.File;
import java.net.URL;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import ca.oson.json.Oson.FIELD_NAMING;
import ca.oson.json.OsonConvert;
import ca.oson.json.OsonMerge;
import ca.oson.json.OsonMerge.NUMERIC_VALUE;
import ca.oson.json.OsonPath;
import ca.oson.json.support.TestCaseBase;
import ca.oson.json.util.ArrayToJsonMap;
import ca.oson.json.util.StringUtil;

public class GeolocationMergeTest extends TestCaseBase {
	OsonMerge merge;
	
	private static Map<String, String> getConversionMap() {
    	String[] names = new String[] {
  		  "geobytesremoteip", "ip",
  		  "geobytesipaddress", "ip",
  		  "ip_address", "ip",
  		  "geobytesinternet", "country_code",
  		  "geobytescountry", "country_name",
  		  "country", "country_code",
  		  "geobytesregionlocationcode", "region_code",
  		  "geobytesregion", "region",
  		  "geobytescode", "region_code",
  		  "geobyteslocationcode", "location",
  		  "geobytescity", "city",
  		  "geobytesfqcn", "location",
  		  "geobyteslatitude", "latitude",
  		  "geobyteslongitude", "longitude",
  		  "geobytescapital", "capital",
  		  "geobytestimezone", "time_zone",
  		  "geobytesnationalitysingular", "nationality",
  		  "geobytespopulation", "population",
  		  "geobytesnationalityplural", "nationality",
  		  "geobytesmapreference", "continent",
  		  "geobytescurrency", "currency",
  		  "geobytescurrencycode", "currency",
  		  "geobytestitle", "country_name",
  		  "currency_code", "currency",
  		  "region_name", "region",
  		  "geoplugin_request", "ip",
  		  "geoplugin_city", "city",
  		  "geoplugin_region", "region",
  		  "geoplugin_areacode", "zip_code",
  		  "geoplugin_countrycode", "country_code",
  		  "geoplugin_countryname", "country_name",
  		  "geoplugin_continentcode", "continent_code",
  		  "geoplugin_latitude", "latitude",
  		  "geoplugin_longitude", "longitude",
  		  "geoplugin_regioncode", "region_code",
  		  "geoplugin_regionname", "region",
  		  "geoplugin_currencycode", "currency",
  		  "geoplugin_currencysymbol", "currency",
  		  "geoplugin_currencySymbol_UTF8", "currency",
  		  "geoplugin_currencyconverter", "currency_rate",
  		  "as", "isp",
  		  "countrycode", "country_code",
  		  "lat", "latitude",
  		  "lon", "longitude",
  		  "org", "isp",
  		  "query", "ip",
  		  "regionname", "region",
  		  "timezone", "time_zone",
  		  "zip", "zip_code",
  		  //"region_code", "region",
  		  "postal", "zip_code",
  		  "ipaddress", "ip",
  		  "countryname", "country_name",
  		  "cityname", "city",
  		  "zipcode", "zip_code",
  		  "region_full", "region",
  		  "remote_address", "ip",
  		  "requested_address", "ip",
  		  "timezone_name", "time_zone",
  		  //"browser_name", "browser",
  		  //"operating_system", "os",
  		  "postalzip", "zip_code",
  		  "continent_name", "continent",
  		  "country_code_iso3166alpha2", "country_code",
  		  "country_code_iso3166alpha3", "country_code",
  		  "country_code_iso3166numeric", "country_code",
  		  "country_code_fips10-4", "country_code",
  		  "country_code3", "country_code",
  		  "q", "ip",
  		  "dma_code", "metro_code",
  		  "address", "ip",
  		  "city_name", "city",
  		"geoplugin_currencyCode", "currency",
  		"geoplugin_continentCode", "continent_code",
  		"geobytescityid", "",
  		"geoplugin_countryCode", "country_code",
  		"geoplugin_currencyConverter", "",
  		"geoplugin_currencySymbol", "",
  		"geoplugin_status", "",
  		"stateprov", "region",
  		"geoplugin_credit", "",
  		"geoplugin_dmaCode", "",
  		"geobytesdma", "",
  		"geobytescertainty", "",
  		"geoplugin_regionCode", "region_code",
  		"geoplugin_areaCode", "",
  		"geoplugin_regionName", "region",
  		"owner", "isp",
  		"organization", "isp",
  		"metro_code","region_code",
	  	  "name", "",
	  	  "statusCode", "",
	  	  "status", "",
	  	"area_code", "",
	  	"code","",
	  	"accuracy_radius","",
	  	"loc", "",
	  	"geoplugin_country_name", ""
    	};
	
    	return ArrayToJsonMap.array2Map(names);
	}
	
	
	   @Before 
	   protected void setUp() {
		   super.setUp();
		   merge = new OsonMerge();
	   }
	   
		@Test
		public void testSimpleMerge() {
			Object obj = oson.readValue("geolocation1.txt");
			
			//geolocation_data
			String json = OsonPath.search(oson.serialize(obj), "geolocation_data");
			String[] jsons = new String[] {
					oson.serialize(oson.readValue("geolocation2.txt")),
					oson.serialize(oson.readValue("geolocation3.txt")),
					oson.serialize(oson.readValue("geolocation4.txt")),
					oson.serialize(oson.readValue("geolocation5.txt")),
					oson.serialize(oson.readValue("geolocation6.txt")),
					OsonConvert.flatten(oson.serialize(oson.readValue("geolocation7.txt"))),
					oson.serialize(oson.readValue("geolocation8.txt")),
					oson.serialize(oson.readValue("geolocation9.txt"))
					
			};
			
			merge.setNames(getConversionMap());
			//merge.pretty = true;
			
			String merged = merge.merge(json, jsons);
			//System.err.println(merged);
			
			String expected = "{\"ip\":\"184.66.33.61\",\"country_code\":\"CA\",\"continent\":\"North America\",\"continent_code\":\"NA\",\"city\":\"Victoria\",\"region\":\"British Columbia\",\"region_code\":\"BC\",\"time_zone\":\"America/Vancouver\",\"isp\":\"Shaw Communications\",\"longitude\":-123.32633333333332,\"latitude\":48.461,\"zipCode\":\"V9Z 0A1\",\"capital\":\"Ottawa\",\"postal_code\":\"V8N\",\"population\":\"31592805\",\"hostname\":\"S010684948cd37c83.gv.shawcable.net\",\"nationality\":\"Canadians\",\"currency\":\"CAD\",\"location\":\"Victoria, BC, Canada\",\"countryName\":\"Canada\"}";
			
			assertEquals(expected, merged);
		}

}
