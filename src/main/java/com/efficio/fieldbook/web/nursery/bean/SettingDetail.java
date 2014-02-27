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

import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.dms.VariableConstraints;


public class SettingDetail {

	private SettingVariable variable;
	private List<ValueReference> possibleValues;
	private String possibleValuesJson;
	private String value;
	private boolean isDelete;

	public SettingDetail(){
		super();
	}

	public SettingDetail(SettingVariable variable,
			List<ValueReference> possibleValues, String value, boolean isDelete) {
		super();
		this.variable = variable;
		this.possibleValues = possibleValues;
		this.value = value;
		this.isDelete = isDelete;
	}
	public SettingVariable getVariable() {
		return variable;
	}
	public void setVariable(SettingVariable variable) {
		this.variable = variable;
	}
	public List<ValueReference> getPossibleValues() {
		return possibleValues;
	}
	public void setPossibleValues(List<ValueReference> possibleValues) {
		this.possibleValues = possibleValues;
	}
	public String getPossibleValuesJson() {
	    return possibleValuesJson;
	}
	public void setPossibleValuesJson(String possibleValuesJson) {
	    this.possibleValuesJson = possibleValuesJson;
	}
	public void setPossibleValuesToJson(List<ValueReference> possibleValues) {
	    try {
                ObjectMapper om = new ObjectMapper();
                setPossibleValuesJson(om.writeValueAsString(possibleValues));
            }
            catch(Exception e) {
                setPossibleValuesJson("err");
            }
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
