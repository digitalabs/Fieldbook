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

import org.generationcp.middleware.domain.dms.StandardVariable;


public class SettingVariable {

	private Integer cvTermId;
	private String name;
	private String description;
	private String property;
	private String scale;
	private String method;
	private String role;
	private String dataType;
	private String traitClass;
	private String cropOntologyId;
	
	public SettingVariable(){
		super();
	}
	
	
	
	public SettingVariable(String name, String description, String property,
			String scale, String method, String role, String dataType) {
		super();
		this.name = name;
		this.description = description;
		this.property = property;
		this.scale = scale;
		this.method = method;
		this.role = role;
		this.dataType = dataType;
	}



	public String getTraitClass() {
		return traitClass;
	}
	public void setTraitClass(String traitClass) {
		this.traitClass = traitClass;
	}
	public String getCropOntologyId() {
		return cropOntologyId;
	}
	public void setCropOntologyId(String cropOntologyId) {
		this.cropOntologyId = cropOntologyId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public String getScale() {
		return scale;
	}
	public void setScale(String scale) {
		this.scale = scale;
	}
	public String getMethod() {
		return method;
	}
	public void setMethod(String method) {
		this.method = method;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public String getDataType() {
		return dataType;
	}
	public void setDataType(String dataType) {
		this.dataType = dataType;
	}
	/**
	 * @return the cvTermId
	 */
	public Integer getCvTermId() {
		return cvTermId;
	}
	/**
	 * @param cvTermId the cvTermId to set
	 */
	public void setCvTermId(Integer cvTermId) {
		this.cvTermId = cvTermId;
	}
	
	public void setPSMRFromStandardVariable(StandardVariable standardVariable){
		if(standardVariable != null){
			this.property = standardVariable.getProperty().getName();
			this.scale = standardVariable.getScale().getName();
			this.method = standardVariable.getMethod().getName();
			this.role = standardVariable.getPhenotypicType().name();
			this.description = standardVariable.getDescription();
			this.dataType = standardVariable.getDataType().getName();			
		}
	}

}
