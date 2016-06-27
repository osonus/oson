package ca.oson.json.domain;

public class Car {

    public String brand = "Chevron";

    public int doors = 4;

    public int year = 2016;
	

    public Car() {}
    public Car(String brand) {
    	this.brand = brand;
    }
    public Car(String brand, int doors) {
        this.brand = brand;
        this.doors = doors;
    }
}
