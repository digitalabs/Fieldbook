package com.efficio.fieldbook.web.common.bean;

import org.generationcp.middleware.domain.dms.StandardVariable;

public class DesignHeaderItem {

	private int id;
	private String headerName;
	private boolean hasError;
	private StandardVariable variable;
	private boolean required;
	private int columnIndex;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getHeaderName() {
		return headerName;
	}
	
	public void setHeaderName(String headerName) {
		this.headerName = headerName;
	}

	public boolean getHasError() {
		return hasError;
	}

	public void setHasError(boolean hasError) {
		this.hasError = hasError;
	}

	public StandardVariable getVariable() {
		return variable;
	}

	public void setVariable(StandardVariable variable) {
		this.variable = variable;
	}

	public boolean isRequired() {
		return required;
	}

	public void setRequired(boolean required) {
		this.required = required;
	}

	public int getColumnIndex() {
		return columnIndex;
	}

	public void setColumnIndex(int columnIndex) {
		this.columnIndex = columnIndex;
	}
	
	@Override
	public String toString() {
		return "DesignHeaderItem [id=" + id + ", headerName=" + headerName + ", hasError="
				+ hasError + ", variable=" + variable + ", required=" + required + ", columnIndex="
				+ columnIndex + "]";
	}
	
}
