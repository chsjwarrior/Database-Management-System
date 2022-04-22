package model;

public class Condition {
	public Attribute attribute;
	public String relationalOperator;
	public String valor;
	public String logicOperator;

	public Condition(Attribute attribute, String relationalOperator) {
		this.attribute = attribute;
		this.relationalOperator = relationalOperator;
		valor = null;
		logicOperator = null;
	}

	@Override
	public String toString() {
		return attribute.name + " " + relationalOperator + " " + valor + " " + logicOperator;
	}
}