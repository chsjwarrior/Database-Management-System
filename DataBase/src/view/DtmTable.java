package view;

import javax.swing.table.DefaultTableModel;

public class DtmTable extends DefaultTableModel {
	private static final long serialVersionUID = 1L;
	private static DtmTable instance;

	private DtmTable() {
		super();
	}

	public static DtmTable getInstance() {
		if (instance == null)
			instance = new DtmTable();
		return instance;
	}

	public void clearTable() {
		this.dataVector.removeAllElements();
		this.setColumnCount(0);
		this.setRowCount(0);
	}
}