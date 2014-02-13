/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.web.nursery.bean;

import java.util.List;

import org.generationcp.middleware.domain.dms.Reference;


public class SettingDetail {

	private SettingVariable variable;
	private List<Reference> possibleValues;
	private String value;
	private boolean isDelete;

	public SettingVariable getVariable() {
		return variable;
	}
	public void setVariable(SettingVariable variable) {
		this.variable = variable;
	}
	public List<Reference> getPossibleValues() {
		return possibleValues;
	}
	public void setPossibleValues(List<Reference> possibleValues) {
		this.possibleValues = possibleValues;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public boolean isDelete() {
		return isDelete;
	}
	public void setDelete(boolean isDelete) {
		this.isDelete = isDelete;
	}
	
	
}
