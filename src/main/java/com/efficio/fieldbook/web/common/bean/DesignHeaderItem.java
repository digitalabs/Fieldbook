package com.efficio.fieldbook.web.common.bean;

import org.generationcp.middleware.domain.dms.StandardVariable;

public class DesignHeaderItem {

	private int id;
	private String name;
	private boolean hasError;
	private StandardVariable variable;
	private boolean required;
	private int	 columnIndex;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String headerName) {
		this.name = headerName;
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
		return "DesignHeaderItem [id=" + id + ", name=" + name + ", hasError="
				+ hasError + ", variable=" + variable + ", required=" + required + ", columnIndex="
				+ columnIndex + "]";
	}
	
}
