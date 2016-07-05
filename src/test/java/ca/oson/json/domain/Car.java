package ca.oson.json.domain;

import java.util.Objects;

import ca.oson.json.Oson.BOOLEAN;
import ca.oson.json.annotation.FieldMapper;

public class Car {

    public String brand = "Chevron";

    public int doors = 4;

    public int year = 2016;
	
    public int[] years;

    public Car() {}
    public Car(String brand) {
    	this.brand = brand;
    }
    public Car(String brand, int doors) {
        this.brand = brand;
        this.doors = doors;
    }
    
    @Override
    @FieldMapper(ignore = BOOLEAN.TRUE)
    public int hashCode() {
    	return Objects.hashCode(brand) * 7 + Objects.hashCode(doors) * 3 + Objects.hashCode(year);
    }
    
    @Override
    @FieldMapper(ignore = BOOLEAN.TRUE)
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("{\"brand\":\"" + brand + "\",");
        stringBuilder.append("\"doors\":" + doors + ",");
        stringBuilder.append("\"year\":" + year + ",");
        
        if (years != null) {
        	stringBuilder.append("[");
        	boolean isfirst = true;
	        for (int y: this.years) {
	        	if (!isfirst) {
	        		stringBuilder.append(",");
	        	} else {
	        		isfirst = false;
	        	}
	        	stringBuilder.append(y);
	        }
	        stringBuilder.append("]");
        }
        stringBuilder.append("}");

        return stringBuilder.toString();
    }
}
