
package com.efficio.fieldbook.web.common.bean;

public class TableHeader {

	private String columnName;
	private String dataCol;
	private boolean visible = true;

	public TableHeader(String columnName, String dataCol) {
		super();
		this.columnName = columnName;
		this.dataCol = dataCol;
	}

	public TableHeader(final String columnName, final String dataCol, final boolean visible) {
		this.columnName = columnName;
		this.dataCol = dataCol;
		this.visible = visible;
	}

	public String getColumnName() {
		return this.columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public String getDataCol() {
		return this.dataCol;
	}

	public void setDataCol(String dataCol) {
		this.dataCol = dataCol;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(final boolean visible) {
		this.visible = visible;
	}

}
