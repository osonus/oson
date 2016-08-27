package ca.oson.json.domain;

public abstract class Animal implements Eukaryote {
	protected double weight;

	
	public int getAge() {
		return 1;
	}

	public void setWeight(double weight) {
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
