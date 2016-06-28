package ca.oson.json.domain;

import java.util.Objects;

public class Car {

    public String brand = "Chevron";

    public int doors = 4;

    public int year = 2016;
	
    public Integer[] years;

    public Car() {}
    public Car(String brand) {
    	this.brand = brand;
    }
    public Car(String brand, int doors) {
        this.brand = brand;
        this.doors = doors;
    }
    
    @Override
    public int hashCode() {
    	return Objects.hashCode(brand) * 7 + Objects.hashCode(doors) * 3 + Objects.hashCode(year);
    }
}
