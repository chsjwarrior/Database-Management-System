package controller;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;

import model.Attribute;
import model.Restriction;
import model.Table;
import view.DtmTable;

public class Metadata {
	private final Path METADATA;
	private final Path DATA;

	private Path database;
	private HashMap<String, Path> databases;
	private HashMap<String, Table> tables;

	public Metadata() throws IOException {
		METADATA = Paths.get(System.getProperty("user.dir") + "\\BancoDeDados\\Metadados");
		DATA = Paths.get(System.getProperty("user.dir") + "\\BancoDeDados\\Dados");

		database = null;
		databases = new HashMap<String, Path>();
		tables = null;
		DirectoryStream<Path> entries = Files.newDirectoryStream(METADATA);
		entries.forEach(entry -> databases.put(entry.getFileName().toString().toLowerCase(), entry));
	}

	public void createDatabase(String database) throws IOException {
		Path temp = METADATA.resolve(database);
		Files.createDirectory(temp);
		databases.put(database.toLowerCase(), temp);
	}

	public void setDatabase(String database) throws IOException {
		this.database = databases.get(database.toLowerCase());
		loadTables();
	}

	public void describeDatabase(String database) throws IOException {
		setDatabase(database);

		DtmTable d = DtmTable.getInstance();
		d.clearTable();
		d.addColumn("Tabela");
		d.addColumn("Qtd Atributos");

		tables.values().forEach(t -> d.addRow(new String[] { t.getName(), String.valueOf(t.getAttributes().size()) }));

		this.database = null;
		tables.clear();
	}

	public void deleteDatabase(String database) throws IOException {
		Path temp = databases.remove(database.toLowerCase());
		Files.delete(temp);
	}

	public void createTable(Table table) throws IOException {
		Path path = database.resolve(table.getName() + ".tab");
		Path data = DATA.resolve(database.getFileName() + "." + table.getName() + ".dat");
		Files.createFile(path);
		table.setPath(path);
		Files.createFile(data);
		table.setData(data);
		createStruct(table);
		tables.put(table.getName().toLowerCase(), table);
	}

	private void createStruct(Table table) throws IOException {
		try (BufferedWriter writer = new BufferedWriter(new FileWriter(table.getPath().toFile()))) {
			final String ln = System.getProperty("line.separator");
			writer.write(table.getData().toString() + ln);
			writer.write(table.getAttributes().size() + ln);
			for (Attribute a : table.getAttributes())
				writer.write(a.toString() + ln);
			writer.write(table.getRestrictions().size() + ln);
			for (Restriction r : table.getRestrictions()) {
				writer.write(r.getConstraint() + ln);
				writer.write(r.isNotNull() + ln);
				writer.write(r.isUniqueKey() + ln);
				writer.write(r.isPrimaryKey() + ln);
				writer.write(r.isForeignKey() + ln);
				writer.write(r.atributosToString() + ln);
			}
			writer.flush();
		}
	}

	public void describeTable(Table table) {
		DtmTable d = DtmTable.getInstance();
		d.clearTable();
		d.addColumn("Campo");
		d.addColumn("Tipo");
		d.addColumn("not Null");

		Attribute pk = table.getPrimaryKey();
		LinkedList<Attribute> fks = table.getForeignsKey();
		LinkedList<Attribute> notNulls = table.getNotNulls();

		String[] s = new String[3];
		table.getAttributes().forEach(a -> {
			s[0] = a.name;
			s[1] = a.type + "(" + a.size + ")";
			s[2] = String.valueOf(notNulls.contains(a));
			if (a.equals(pk))
				s[0] = s[0] + "(pk)";
			if (fks.contains(a))
				s[0] = s[0] + "(fk)";
			d.addRow(s);
		});
	}

	private void loadTables() throws IOException {
		DirectoryStream<Path> entries = Files.newDirectoryStream(database);
		tables = new HashMap<String, Table>();

		String name;
		Table tab;
		for (Path entry : entries) {
			name = entry.getFileName().toString();
			name = name.substring(0, name.length() - 4);
			tab = new Table(name);
			tab.setPath(entry);
			loadStruct(tab);
			tables.put(name.toLowerCase(), tab);
		}
	}

	private void loadStruct(Table table) throws IOException {
		try (BufferedReader reader = new BufferedReader(new FileReader(table.getPath().toFile()))) {
			table.setData(Paths.get(reader.readLine()));
			int length = Integer.parseInt(reader.readLine());
			String[] as = null;
			for (int i = 0; i < length; ++i) {
				as = reader.readLine().split("#");
				Attribute a = new Attribute(as[0], as[1], Integer.parseInt(as[2]));
				table.getAttributes().add(a);
				as = null;
			}
			length = Integer.parseInt(reader.readLine());
			Restriction r = null;
			for (int i = 0; i < length; ++i) {
				r = new Restriction(reader.readLine());
				r.setNotNull(Boolean.parseBoolean(reader.readLine()));
				r.setUniqueKey(Boolean.parseBoolean(reader.readLine()));
				r.setPrimaryKey(Boolean.parseBoolean(reader.readLine()));
				r.setForeignKey(Boolean.parseBoolean(reader.readLine()));
				as = reader.readLine().split("#");
				for (String s : as)
					r.getAttributes().add(s);
				table.getRestrictions().add(r);
			}
		}
	}

	public void updateTableAddColumn(Table table) throws IOException {
		updateTable(table);
	}

	public void updateTableAddConstraint(Table table) throws IOException {
		updateTable(table);
	}

	public void updateTableDeleteColumn(Table table) throws IOException {
		updateTable(table);
	}

	public void updateTableDeleteConstraint(Table table) throws IOException {
		updateTable(table);
	}

	public void updateTableUpdateColumn(Table table) throws IOException {
		updateTable(table);
	}

	private void updateTable(Table table) throws IOException {
		Files.delete(table.getPath());
		createTable(table);
	}

	public void deleteTable(Table table) throws IOException {
		Files.delete(table.getPath());
		Files.delete(table.getData());
		tables.remove(table.getName().toLowerCase(), table);
	}

	public String getDatabase() {
		if (database == null)
			return "";
		return database.getFileName().toString();
	}

	public HashMap<String, Path> getDatabases() {
		return databases;
	}

	public HashMap<String, Table> getTables() {
		return tables;
	}
}