package ca.oson.json.domain;

import java.util.ArrayList;

import ca.oson.json.FieldMapper;
import ca.oson.json.Oson.BOOLEAN;

public class Customer {
    private Car[] vehicles;
    ArrayList<Car> carList = new ArrayList<Car>();
    public Customer(){
        vehicles = new Car[2];
        vehicles[0] = new Car("Audi");
        vehicles[1] = new Car("Mercedes");
 
        carList.add(new Car("BMW"));
        carList.add(new Car("Chevy"));
    }
    
    
    @Override
    @FieldMapper(ignore = BOOLEAN.TRUE)
    public String toString(){
        StringBuilder stringBuilder = new StringBuilder();

        if (vehicles != null) {
	        for (Car car: this.vehicles) {
	            stringBuilder.append(car.toString());
	        }
        }
        
        if (carList != null) {
	        for (Car car: this.carList) {
	            stringBuilder.append(car.toString());
	        }
        }

        return stringBuilder.toString();
    }
}
