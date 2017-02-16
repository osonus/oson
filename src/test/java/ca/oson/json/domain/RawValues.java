package ca.oson.json.domain;

import java.util.Date;

import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.annotation.FieldMapper;

public class RawValues {
	@FieldMapper(jsonRawValue = BOOLEAN.TRUE)
	private String svalue;
	@FieldMapper(jsonRawValue = BOOLEAN.TRUE)
	private char cvalue;
	@FieldMapper(jsonRawValue = BOOLEAN.TRUE)
	private Character chvalue;
	@FieldMapper(jsonRawValue = BOOLEAN.TRUE)
	private Date dvalue;

	public RawValues(String svalue, char cvalue, Character chvalue, Date dvalue) {
		this.svalue = svalue;
		this.cvalue = cvalue;
		this.chvalue = chvalue;
		this.dvalue = dvalue;

	}
}
