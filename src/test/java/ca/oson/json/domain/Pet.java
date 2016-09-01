package ca.oson.json.domain;

public abstract class Pet extends Animal {
	private Person owner;

	public Person getOwner() {
		return owner;
	}

	public void setOwner(Person owner) {
		this.owner = owner;
	}

}
