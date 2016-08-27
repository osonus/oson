package ca.oson.json.domain;

public abstract class Pet extends Animal {
	private Person owner;

	protected Person getOwner() {
		return owner;
	}

	protected void setOwner(Person owner) {
		this.owner = owner;
	}

}
