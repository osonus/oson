package ca.oson.json.domain;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class BascicDateType {
	public int pint;
	public boolean pbool;
	public char pchar = '0';
	public byte pbyte;
	public short pshort;
	public long plong;
	public float pfloat;
	public double pdouble;

	public Integer integer = new Integer(0);
	public BigInteger bigInteger = BigInteger.ZERO;
	public BigDecimal bigDecimal = BigDecimal.ZERO;
	public Boolean bool = false;
	public String string = "";
	public Character character = '\u0000';
	public Short dshort = 0;
	public Byte dbyte = 0;
	public Long dlong = 0l;
	public Float dfloat = 0f;
	public Double ddouble = 0d;
	public String simpleDateFormat = "yyyy-MM-dd'T'HH:mm:ss.SS'Z'";
	public Date date = new Date(0);
	public AtomicInteger atomicInteger = new AtomicInteger();
	public AtomicLong atomicLong = new AtomicLong();

}
