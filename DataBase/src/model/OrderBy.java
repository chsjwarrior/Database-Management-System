package model;

public class OrderBy {
	private Attribute attribute;
	private boolean asc;

	public OrderBy(Attribute attribute) {
		this.attribute = attribute;
		this.asc = true;
	}

	public Attribute getAttribute() {
		return attribute;
	}

	public void setAttribute(Attribute attribute) {
		this.attribute = attribute;
	}

	public boolean isAsc() {
		return asc;
	}

	public void setAsc(boolean asc) {
		this.asc = asc;
	}

	@Override
	public String toString() {
		return attribute.name + " " + isAsc();
	}
}