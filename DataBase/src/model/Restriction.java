package model;

import java.util.ArrayList;

public class Restriction {
	private String constraint;
	private ArrayList<String> attributes;
	boolean notNull, uniqueKey, primaryKey, foreignKey;

	public Restriction(String constraint) {
		this.constraint = constraint;
		attributes = new ArrayList<String>();
		notNull = false;
		uniqueKey = false;
		primaryKey = false;
		foreignKey = false;
	}

	public String getConstraint() {
		return constraint;
	}

	public void setConstraint(String constraint) {
		this.constraint = constraint;
	}

	public ArrayList<String> getAttributes() {
		return attributes;
	}

	public void setAttributes(ArrayList<String> attributes) {
		this.attributes = attributes;
	}

	public boolean isNotNull() {
		return notNull;
	}

	public void setNotNull(boolean notNull) {
		this.notNull = notNull;
	}

	public boolean isUniqueKey() {
		return uniqueKey;
	}

	public void setUniqueKey(boolean uniqueKey) {
		this.uniqueKey = uniqueKey;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	public boolean isForeignKey() {
		return foreignKey;
	}

	public void setForeignKey(boolean foreignKey) {
		this.foreignKey = foreignKey;
	}

	public String atributosToString() {
		StringBuilder lista = new StringBuilder();
		for (int i = 0; i < attributes.size() - 1; i++)
			lista.append(attributes.get(i) + "#");
		lista.append(attributes.get(attributes.size() - 1));
		return lista.toString();
	}
}