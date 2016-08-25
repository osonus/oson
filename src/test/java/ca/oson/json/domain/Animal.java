package ca.oson.json.domain;

public abstract class Animal {
	protected boolean isPet = true;
	protected Person owner;
	protected int age;
	protected double weight;
	
	public void sleep() {
		System.out.println("Sleeping");
	}
	
	public void eat() {
		System.out.println("Eating");
	}
	
	public abstract void move();
	
}
