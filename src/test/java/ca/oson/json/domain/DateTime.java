package ca.oson.json.domain;

import java.sql.Timestamp;
import java.util.Date;

import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.annotation.ClassMapper;
import ca.oson.json.annotation.FieldMapper;

@ClassMapper(date2Long = BOOLEAN.TRUE)
public class DateTime {

	private Date myDate;
	
	@FieldMapper(date2Long = BOOLEAN.FALSE, simpleDateFormat="yyyy/MM/dd")
	private java.sql.Date sqlDate;
	
	@FieldMapper(date2Long = BOOLEAN.FALSE, simpleDateFormat="yyyy-MM-dd HH:mm:ss.SSS")
	private Timestamp myTime;
	
	public DateTime(Date myDate, java.sql.Date sqlDate, Timestamp myTime) {
		this.myDate = myDate;
		this.sqlDate = sqlDate;
		this.myTime = myTime;
	}
}
