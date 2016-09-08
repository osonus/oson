package ca.oson.json.domain;

import java.util.Date;

import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.Oson.JSON_PROCESSOR;
import ca.oson.json.annotation.FieldMapper;

public class RawValues {
	@com.fasterxml.jackson.annotation.JsonRawValue
	private String svalue;
	@org.codehaus.jackson.annotate.JsonRawValue
	private char cvalue;
	@FieldMapper(jsonRawValue = BOOLEAN.TRUE)
	private Character chvalue;
	@FieldMapper(jsonRawValue = BOOLEAN.TRUE)
	private Date dvalue;
	@FieldMapper(jsonRawValue = BOOLEAN.TRUE)
	private JSON_PROCESSOR jvalue;
	
	public RawValues(String svalue, char cvalue, Character chvalue, Date dvalue, JSON_PROCESSOR jvalue) {
		this.svalue = svalue;
		this.cvalue = cvalue;
		this.chvalue = chvalue;
		this.dvalue = dvalue;
		this.jvalue = jvalue;
		
	}
}
