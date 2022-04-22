package analyzer;

import java.io.IOException;
import java.util.LinkedList;

import controller.Metadata;
import model.Attribute;
import model.Condition;
import model.Restriction;
import model.OrderBy;
import model.Table;
import view.DtmTable;

public class Semantic implements Constants {
	private Metadata metadata;
	private Table table;
	private LinkedList<Table> listTables;
	private Attribute attribute;
	private LinkedList<Attribute> listAttributes;
	private Restriction restriction;
	private LinkedList<Token> listTokens;
	private LinkedList<Condition> listConditions;
	private OrderBy orderBy;

	public Semantic(Metadata metadata) {
		this.metadata = metadata;
		resetVariables();
	}

	private void resetVariables() {
		table = null;
		listTables = new LinkedList<Table>();
		attribute = null;
		listAttributes = new LinkedList<Attribute>();
		restriction = null;
		listTokens = new LinkedList<Token>();
		listConditions = new LinkedList<Condition>();
		orderBy = null;
	}

	private boolean existsDatabase(Token token) {
		return metadata.getDatabases().containsKey(token.getLexeme().toLowerCase());
	}

	private Table existsTable(Token token) throws SemanticError {
		if (metadata.getDatabase().isEmpty())
			throw new SemanticError("Nenhuma base de dados Selecionada.", token.getPosition());
		return metadata.getTables().get(token.getLexeme().toLowerCase());
	}

	private Attribute existsAttribute(Table tabela, String attribute) throws SemanticError {
		for (Attribute a : tabela.getAttributes())
			if (a.name.equalsIgnoreCase(attribute))
				return a;
		return null;
	}

	private Restriction existsRestriction(Table table, Token token) throws SemanticError {
		for (Restriction r : table.getRestrictions())
			if (r.getConstraint().equalsIgnoreCase(token.getLexeme()))
				return r;
		return null;
	}

	private void identifierAux(Token token) {
		listTokens.addLast(token);
	}

	/* create table */
	private void newTable(Token token) throws SemanticError {
		table = existsTable(token);
		if (table != null)
			throw new SemanticError("Tabela '" + token.getLexeme() + "' já existe.", token.getPosition());

		table = new Table(token.getLexeme());
		listTables.addLast(table);
	}

	/* alter table, insert table, update, drop table */
	private void informationTable(Token token) throws SemanticError {
		table = existsTable(token);
		if (table == null)
			throw new SemanticError("Tabela '" + token.getLexeme() + "' não existe ou não foi encontrada.",
					token.getPosition());

		listTables.addLast(table);
	}

	/* create table */
	private void newAttribute(Token token) throws SemanticError {
		attribute = existsAttribute(table, token.getLexeme());
		if (attribute != null)
			throw new SemanticError("Atributo '" + token.getLexeme() + "' já existe.", token.getPosition());

		attribute = new Attribute(token.getLexeme());
		table.getAttributes().add(attribute);
	}

	/* alter table, insert table, update */
	private void informationAttribute(Token token) throws SemanticError {
		attribute = existsAttribute(table, token.getLexeme());
		if (attribute == null)
			throw new SemanticError("Atributo '" + token.getLexeme() + "' não existe.", token.getPosition());

		listAttributes.addLast(attribute);
	}

	/* select */
	private void informationQuery(Token token) throws SemanticError {
		if (table != null) {
			informationAttribute(token);
			table = listTables.removeFirst();
			table = null;
		} else {
			for (Attribute a : listAttributes)
				if (a.type == null && a.name.equalsIgnoreCase(token.getLexeme()))
					throw new SemanticError("Duplicidade de Atributos encontrada '" + token.getLexeme() + "'.",
							token.getPosition());

			Attribute a = new Attribute(token.getLexeme());
			a.contents = String.valueOf(token.getPosition());
			listAttributes.addLast(a);
		}
	}

	private void listAttributeAdd() {
		listAttributes.addLast(attribute);
	}

	private void setTypeAttribute(Token token) {
		attribute.type = token.getLexeme().toLowerCase();
		attribute.size = Integer.MAX_VALUE;
	}

	private void setSizeAttribute(Token token) {
		attribute.size = Integer.parseInt(token.getLexeme());
	}

	private void constraint(Token token) throws SemanticError {
		restriction = existsRestriction(table, token);
		if (restriction != null)
			throw new SemanticError("Constraint '" + token.getLexeme() + "' já existe.", token.getPosition());

		restriction = new Restriction(token.getLexeme());
	}

	private void restrictionNotNull() {
		if (restriction == null)
			restriction = new Restriction("nk" + listTables.getFirst().getName() + "_"
					+ (listTables.getFirst().getRestrictions().size() + 1));

		for (int i = 0; i < listAttributes.size(); ++i)
			restriction.getAttributes().add(listAttributes.removeFirst().name);
		restriction.getAttributes().trimToSize();
		restriction.setNotNull(true);
		table.getRestrictions().add(restriction);
		restriction = null;
	}

	private void restrictionPrimarykey(Token token) throws SemanticError {
		for (Restriction r : listTables.getFirst().getRestrictions())
			if (r.isPrimaryKey())
				throw new SemanticError("Tabela já contém uma Chave primária declarada.", token.getPosition());

		if (restriction == null)
			restriction = new Restriction("pk" + listTables.getFirst().getName() + "_"
					+ (listTables.getFirst().getRestrictions().size() + 1));

		for (int i = 0; i < listAttributes.size(); ++i)
			restriction.getAttributes().add(listAttributes.removeFirst().name);
		restriction.getAttributes().trimToSize();
		restriction.setNotNull(true);
		restriction.setUniqueKey(true);
		restriction.setPrimaryKey(true);
		table.getRestrictions().add(restriction);
		restriction = null;
	}

	private void restrictionUniqueKey() {
		if (restriction == null)
			restriction = new Restriction("uk" + listTables.getFirst().getName() + "_"
					+ (listTables.getFirst().getRestrictions().size() + 1));

		for (int i = 0; i < listAttributes.size(); ++i)
			restriction.getAttributes().add(listAttributes.removeFirst().name);
		restriction.getAttributes().trimToSize();
		restriction.setUniqueKey(true);
		table.getRestrictions().add(restriction);
		restriction = null;
		listAttributes.clear();
	}

	private void restrictionForeignKey(Token token) throws SemanticError {
		if (restriction == null)
			restriction = new Restriction("fk" + listTables.getFirst().getName() + attribute.name + "_"
					+ (listTables.getFirst().getRestrictions().size() + 1));

		attribute = listAttributes.removeFirst();
		Attribute a;
		restriction.getAttributes().add(attribute.name);
		for (int i = 0; i < listAttributes.size(); ++i) {
			a = listAttributes.removeFirst();
			if ((!attribute.type.equalsIgnoreCase(a.type)) || (attribute.size != a.size))
				throw new SemanticError("Tipo incompátivel de dados (" + attribute.type + "(" + attribute.size + ") , "
						+ a.type + "(" + a.size + ")).", token.getPosition());

			restriction.getAttributes().add(table.getName() + "." + a.name + ";");
		}
		restriction.getAttributes().trimToSize();
		restriction.setForeignKey(true);

		listTables.remove(table);
		table = listTables.getFirst();
		table.getRestrictions().add(restriction);
		restriction = null;
	}

	/* insert table, update */
	private void setValor(Token token) throws SemanticError {
		if (listAttributes.isEmpty())
			listAttributes.addAll(table.getAttributes());

		LinkedList<Attribute> notNulls = table.getNotNulls();
		for (Attribute a : notNulls)
			if (!listAttributes.contains(a))
				throw new SemanticError("Campo '" + a.name + "' (not null) não foi encontrado.", token.getPosition());

		Token t;
		for (int i = 0; i < listTokens.size(); ++i) {
			t = new Token(listTokens.get(i).getId(), listTokens.get(i).getLexeme().replaceAll("\"", ""),
					listTokens.get(i).getPosition());
			validateValor(t, listAttributes.get(i));
			listAttributes.get(i).contents = t.getLexeme();
		}
	}

	private void validateValor(Token token, Attribute attribute) throws SemanticError {
		boolean notNull = table.getNotNulls().contains(attribute);

		if (notNull && token.getId() == t_null) {
			throw new SemanticError("Campo '" + attribute.name + "' não pode ser nulo.", token.getPosition());
		} else {
			switch (attribute.type) {
			case "integer":
				if (token.getId() != t_constInteger && token.getId() != t_null)
					throw new SemanticError("Tipo incompátivel de dados (numérico , " + token.getClasse() + ").",
							token.getPosition());

				int value = Integer.parseInt(token.getLexeme());
				if (value > attribute.size)
					throw new SemanticError(
							"Integer '" + token.getLexeme() + "' muito extenso, limite de: " + attribute.size,
							token.getPosition());
				break;
			case "float":
				if (token.getId() != t_constFloat && token.getId() != t_null)
					throw new SemanticError("Tipo incompátivel de dados (numérico , " + token.getClasse() + ").",
							token.getPosition());
				break;
			case "varchar":
				if (token.getId() != t_constLiteral && token.getId() != t_null)
					throw new SemanticError("Tipo incompátivel de dados (varchar , " + token.getClasse() + ").",
							token.getPosition());

				if (token.getLexeme().length() > attribute.size)
					throw new SemanticError(
							"Varchar '" + token.getLexeme() + "' muito extenso, limite de: " + attribute.size,
							token.getPosition());
				break;
			case "date":
				if (token.getId() != t_data && token.getId() != t_null)
					throw new SemanticError("Tipo incompátivel de dados (date , " + token.getClasse() + ").",
							token.getPosition());

				String[] data = token.getLexeme().split("/");
				value = Integer.parseInt(data[0]);
				if (value < 1 || value > 31)
					throw new SemanticError("Data inválida.", token.getPosition());

				value = Integer.parseInt(data[1]);
				if (value < 1 || value > 12)
					throw new SemanticError("Data inválida.", token.getPosition());
			}
		}
	}

	private void validateQuery() throws SemanticError {
		boolean found = false;
		for (int i = 0; i < listTokens.size(); ++i, found = false) {
			for (int j = 0; j < listTables.size() && !found; ++j)
				found = listTables.get(j).getName().equalsIgnoreCase(listTokens.get(i).getLexeme());

			if (!found)
				throw new SemanticError("Tabela '" + listTokens.get(i).getLexeme() + "' não referenciado.",
						listTokens.get(i).getPosition());
		}

		Attribute b = null;
		for (Attribute a : listAttributes) {
			if (a.type == null) {
				found = false;
				for (Table t : listTables) {
					b = existsAttribute(t, a.name);
					if (b != null && !found)
						found = true;
					else if (b != null && found)
						throw new SemanticError("Ambiguidade de Atributo encontrada '" + a.name + "'.",
								Integer.parseInt(a.contents));
				}
				if (found) {
					if (!listAttributes.contains(b))
						a = b;
					else
						throw new SemanticError("Ambiguidade de Atributo encontrada '" + a.name + "'.",
								Integer.parseInt(a.contents));
				} else
					throw new SemanticError("Atributo '" + a.name + "' não encontrado em nenhuma Tabela.",
							Integer.parseInt(a.contents));
			}
		}
		listTokens.clear();
	}

	/* guarda o operador relacional */
	private void relationalOperator(Token token) {
		Attribute a = listAttributes.removeLast();
		listConditions.addLast(new Condition(a, token.getLexeme()));
	}

	private void validateCondition(Token token) throws SemanticError {
		Condition c = listConditions.getLast();
		Token t = new Token(token.getId(), token.getLexeme().replaceAll("\"", ""), token.getPosition());
		validateValor(t, c.attribute);
		c.valor = t.getLexeme();
	}

	private void logicOperator(Token token) {
		listConditions.getLast().logicOperator = token.getLexeme();
	}

	private void orderByCampo() {
		Attribute a = listAttributes.removeLast();
		orderBy = new OrderBy(a);
	}

	private void orderByOrdem(Token token) {
		orderBy.setAsc(token.getLexeme().equalsIgnoreCase("asc"));
	}

	/* create database */
	private void createDatabase() throws SemanticError, IOException {
		Token t = listTokens.removeFirst();
		if (existsDatabase(t))
			throw new SemanticError("DataBase '" + t.getLexeme() + "' já Existe.", t.getPosition());
		metadata.createDatabase(t.getLexeme());
	}

	/* set database */
	private void setDatabase() throws SemanticError, IOException {
		Token t = listTokens.removeFirst();
		if (!existsDatabase(t))
			throw new SemanticError("DataBase '" + t.getLexeme() + "' não Existe ou não foi encontrada.",
					t.getPosition());
		metadata.setDatabase(t.getLexeme());
	}

	private void createTable() throws SemanticError, IOException {
		metadata.createTable(table);
	}

	private void updateTableAddColumn() throws IOException {
		metadata.updateTableAddColumn(table);
	}

	private void updateTableAddConstraint() throws IOException {
		metadata.updateTableAddConstraint(table);
	}

	private void updateTableDropColumn() throws IOException {
		metadata.updateTableDeleteColumn(table);
	}

	private void updateTableDropConstraint() throws IOException {
		metadata.updateTableDeleteConstraint(table);
	}

	private void updateTableUpdateColumn() throws IOException {
		metadata.updateTableUpdateColumn(table);
	}

	private void insertTable() {
		table.insertTable();
	}

	private void updateTable() {
		table.update(listConditions);
	}

	private void deleteTable() {
		table.delete(listConditions);
	}

	private void select() {
		DtmTable.getInstance().clearTable();
		if (listAttributes.isEmpty()) {
			for (Table t : listTables)
				t.selectTableAll();
		} else {
			for (Attribute a : listAttributes)
				System.out.println(a.name);

			if (!listConditions.isEmpty()) {
				System.out.println("Where");
				for (Condition c : listConditions)
					System.out.println(
							c.attribute.name + " " + c.relationalOperator + " " + c.valor + "\n" + c.logicOperator);
			}
			if (orderBy != null)
				System.out.println("order by " + orderBy.getAttribute() + " " + orderBy.isAsc());
			for (Table t : listTables)
				t.selectTable(listConditions.isEmpty() ? null : listConditions);
		}
	}

	private void dropDatabase() throws SemanticError, IOException {
		Token t = listTokens.removeFirst();
		if (!existsDatabase(t))
			throw new SemanticError("DataBase '" + t.getLexeme() + "' não Existe ou não foi encontrada.",
					t.getPosition());
		metadata.deleteDatabase(t.getLexeme());
	}

	private void dropTable() throws IOException {
		metadata.deleteTable(table);
	}

	private void describeObject() throws SemanticError, IOException {
		Token t = listTokens.removeFirst();
		if (!metadata.getDatabase().isEmpty()) {
			table = existsTable(t);
			if (table != null) {
				metadata.describeTable(table);
				return;
			}
		}
		if (existsDatabase(t))
			metadata.describeDatabase(t.getLexeme());
		else
			throw new SemanticError("Não foi encontado nenhuma tabela ou database correspondente a " + t.getLexeme(),
					t.getPosition());
	}

	public void executeAction(int action, Token token) throws SemanticError, IOException {
		System.out.println("Ação # " + action + " '" + token.getLexeme() + "'");
		switch (action) {
		case 0:
			resetVariables();
			break;
		case 1:
			identifierAux(token);
			break;
		case 2:
			newTable(token);
			break;
		case 3:
			informationTable(token);
			break;
		case 4:
			newAttribute(token);
			break;
		case 5:
			informationAttribute(token);
			break;
		case 6:
			informationQuery(token);
			break;
		case 7:
			listAttributeAdd();
		case 10:
			setTypeAttribute(token);
			break;
		case 11:
			setSizeAttribute(token);
			break;
		case 12:
			constraint(token);
			break;
		case 13:
			restrictionPrimarykey(token);
			break;
		case 14:
			restrictionUniqueKey();
			break;
		case 15:
			restrictionForeignKey(token);
			break;
		case 16:
			restrictionNotNull();
			break;
		case 21:
			setValor(token);
			break;
		case 30:
			validateQuery();
			break;
		case 31:
			relationalOperator(token);
			break;
		case 32:
			validateCondition(token);
			break;
		case 33:
			logicOperator(token);
			break;
		case 40:
			orderByCampo();
			break;
		case 41:
			orderByOrdem(token);
			break;
		case 100:
			setDatabase();
			break;
		case 101:
			createDatabase();
			break;
		case 102:
			createTable();
			break;
		case 103:
			updateTableAddColumn();
			break;
		case 104:
			updateTableAddConstraint();
			break;
		case 105:
			updateTableDropColumn();
			break;
		case 106:
			updateTableDropConstraint();
			break;
		case 107:
			updateTableUpdateColumn();
			break;
		case 108:
			insertTable();
			break;
		case 109:
			updateTable();
			break;
		case 110:
			deleteTable();
			break;
		case 111:
			select();
			break;
		case 112:
			dropDatabase();
			break;
		case 113:
			dropTable();
			break;
		case 114:
			describeObject();
			break;
		default:
			System.out.println("Ação # " + action + " '" + token.getLexeme() + "' não encontrada.");
		}
	}
}