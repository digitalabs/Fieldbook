
package com.efficio.fieldbook.web.common.bean;

public class TableHeader {

	private String columnName;
	private String dataCol;

	public TableHeader(String columnName, String dataCol) {
		super();
		this.columnName = columnName;
		this.dataCol = dataCol;
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

}
