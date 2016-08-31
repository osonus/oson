package ca.oson.json.domain;

public abstract class Animal implements Eukaryote {
	protected Double weight;

	
	public int getAge() {
		return 1;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}
	
	public void sleep() {
		System.out.println("Sleeping");
	}
	
	public void eat() {
		System.out.println("Eating");
	}
	
	public abstract void move();
	
}
