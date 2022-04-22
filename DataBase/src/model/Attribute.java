package model;

public class Attribute {
	public String name, type, contents;
	public int size;

	public Attribute(String name) {
		this(name, null, 0);
	}

	public Attribute(String name, String type, int size) {
		this.name = name;
		this.type = type;
		this.contents = "null";
		this.size = size;
	}

	@Override
	public String toString() {
		return name + "#" + type + "#" + size;
	}
}