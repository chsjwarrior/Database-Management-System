package model;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import view.DtmTable;

public class Table {
	private final Charset CHARSET = Charset.defaultCharset();
	private String name;
	private ArrayList<Attribute> attributes;
	private ArrayList<Restriction> restrictions;
	private Path path;
	private Path data;
	private LinkedList<String> contents;

	public Table(String name) {
		this.name = name;
		attributes = new ArrayList<Attribute>();
		restrictions = new ArrayList<Restriction>();
		path = null;
		data = null;
		contents = new LinkedList<String>();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(ArrayList<Attribute> attributes) {
		this.attributes = attributes;
	}

	public List<Restriction> getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(ArrayList<Restriction> restrictions) {
		this.restrictions = restrictions;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	public Path getData() {
		return data;
	}

	public void setData(Path data) {
		this.data = data;
	}

	public LinkedList<Attribute> getNotNulls() {
		LinkedList<Attribute> nn = new LinkedList<Attribute>();
		restrictions.forEach(r -> {
			if (r.isNotNull())
				attributes.forEach(a -> {
					r.getAttributes().forEach(s -> {
						if (a.name.equalsIgnoreCase(s))
							nn.addLast(a);
					});
				});
		});
		return nn;
	}

	public LinkedList<Attribute> getUniquesKey() {
		LinkedList<Attribute> uk = new LinkedList<Attribute>();
		restrictions.forEach(r -> {
			if (r.isUniqueKey())
				attributes.forEach(a -> {
					r.getAttributes().forEach(s -> {
						if (a.name.equalsIgnoreCase(s))
							uk.addLast(a);
					});
				});
		});
		return uk;
	}

	public Attribute getPrimaryKey() {
		for (Restriction r : restrictions)
			if (r.isPrimaryKey())
				for (Attribute a : attributes)
					if (a.name.equalsIgnoreCase(r.getAttributes().get(0)))
						return a;
		return null;
	}

	public LinkedList<Attribute> getForeignsKey() {
		LinkedList<Attribute> fks = new LinkedList<Attribute>();
		restrictions.forEach(r -> {
			if (r.isForeignKey())
				attributes.forEach(a -> {
					if (a.name.equalsIgnoreCase(r.getAttributes().get(0)))
						fks.addLast(a);
				});
		});
		return fks;
	}

	public void clearAttributes() {
		attributes.forEach(a -> a.contents = "null");
	}

	public void insertTable() {
		open();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < attributes.size() - 1; ++i)
			sb.append(attributes.get(i).contents + "#");
		sb.append(attributes.get(attributes.size() - 1).contents);
		contents.add(sb.toString());
		salve();
		clearAttributes();
	}

	public void update(List<Condition> lConditions) {

	}

	public void delete(List<Condition> lConditions) {

	}

	public void selectTable(List<Condition> lConditions) {
		contents.clear();
		open();
		DtmTable d = DtmTable.getInstance();
		for (Attribute a : attributes)
			if (a.contents.equalsIgnoreCase("select"))
				d.addColumn(a.name);

		String[] line = new String[d.getColumnCount()];
		for (String s : contents) {
			String[] ss = s.split("#");
			for (int i = 0, j = 0; i < ss.length; ++i)
				if (attributes.get(i).contents.equalsIgnoreCase("select")) {
					line[j] = ss[i];
					j++;
				}
			d.addRow(line);
		}
		clearAttributes();
	}

	public void selectTableAll() {
		DtmTable d = DtmTable.getInstance();
		for (Attribute a : attributes)
			d.addColumn(a.name);
		contents.clear();
		open();
		for (String s : contents)
			d.addRow(s.split("#"));
	}

	public void salve() {
		try (BufferedWriter bw = Files.newBufferedWriter(data, CHARSET)) {
			for (int i = 0; i < contents.size(); ++i)
				bw.write(contents.get(i) + System.getProperty("line.separator"));
			bw.flush();
			bw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void open() {
		contents.clear();
		try (BufferedReader br = Files.newBufferedReader(data, CHARSET)) {
			String line;
			while ((line = br.readLine()) != null)
				contents.add(line);
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}