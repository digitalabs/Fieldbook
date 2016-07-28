
package com.efficio.etl.web.bean;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public class RowDTO {

	private int rowIndex;
	private String rowContent;

	public RowDTO(int rowIndex, String rowContent) {
		this.rowIndex = rowIndex;
		this.rowContent = rowContent;
	}

	public int getRowIndex() {
		return this.rowIndex;
	}

	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	public String getRowContent() {
		return this.rowContent;
	}

	public void setRowContent(String rowContent) {
		this.rowContent = rowContent;
	}
}
