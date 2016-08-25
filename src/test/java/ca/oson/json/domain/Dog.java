package ca.oson.json.domain;

public class Dog extends Animal {
	private String name;

	public void bark() {
		System.out.println("Woof!");
	}
	
	public void move() {
		System.out.println("Running");
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
