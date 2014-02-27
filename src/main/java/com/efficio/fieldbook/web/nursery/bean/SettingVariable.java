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
import org.generationcp.middleware.domain.oms.TermId;


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
	private Integer dataTypeId;
	private Double minRange;
	private Double maxRange;
	private WidgetType widgetType;
	
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

	public SettingVariable(String name, String description, String property,
			String scale, String method, String role, String dataType, Integer dataTypeId, 
			Double minRange, Double maxRange) {
		super();
		this.name = name;
		this.description = description;
		this.property = property;
		this.scale = scale;
		this.method = method;
		this.role = role;
		this.dataType = dataType;
		this.dataTypeId = dataTypeId;
		this.setWidgetType();
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
			this.dataType = getDataType(standardVariable.getDataType().getId());
			this.dataTypeId = standardVariable.getDataType().getId();
			this.minRange = standardVariable.getConstraints() != null && standardVariable.getConstraints().getMinValue() != null
					? standardVariable.getConstraints().getMinValue() : null;
			this.maxRange = standardVariable.getConstraints() != null && standardVariable.getConstraints().getMaxValue() != null
					? standardVariable.getConstraints().getMaxValue() : null;
			setWidgetType();
		}
	}

	private String getDataType(int dataTypeId) {
	    //datatype ids: 1120, 1125, 1128, 1130
	    if (dataTypeId == TermId.CHARACTER_VARIABLE.getId() || dataTypeId == TermId.TIMESTAMP_VARIABLE.getId() || 
	            dataTypeId == TermId.CHARACTER_DBID_VARIABLE.getId() || dataTypeId == TermId.CATEGORICAL_VARIABLE.getId()) {
	        return "C";
	    } else {
	        return "N";
	    }
	}

	/**
	 * @return the minRange
	 */
	public Double getMinRange() {
		return minRange;
	}

	/**
	 * @param minRange the minRange to set
	 */
	public void setMinRange(Double minRange) {
		this.minRange = minRange;
	}

	/**
	 * @return the maxRange
	 */
	public Double getMaxRange() {
		return maxRange;
	}

	/**
	 * @param maxRange the maxRange to set
	 */
	public void setMaxRange(Double maxRange) {
		this.maxRange = maxRange;
	}

	/**
	 * @return the dataTypeId
	 */
	public Integer getDataTypeId() {
		return dataTypeId;
	}

	/**
	 * @param dataTypeId the dataTypeId to set
	 */
	public void setDataTypeId(Integer dataTypeId) {
		this.dataTypeId = dataTypeId;
	}
	
	public void setWidgetType() {
		if (cvTermId != null) {
			if (cvTermId.equals(TermId.TRIAL_LOCATION.getId()) || cvTermId.equals(TermId.PI_NAME.getId()) 
					|| cvTermId.equals(TermId.BREEDING_METHOD.getId()) || cvTermId.equals(TermId.NURSERY_TYPE.getId())) {
				this.widgetType = WidgetType.DROPDOWN;
			}
		}
		else if (dataTypeId != null) {
			if (dataTypeId.equals(TermId.DATE_VARIABLE.getId())) {
				this.widgetType = WidgetType.DATE;
			}
			else if (dataTypeId.equals(TermId.CATEGORICAL_VARIABLE.getId())) {
				this.widgetType = WidgetType.DROPDOWN;
			}
			else if (minRange != null && maxRange != null) {
				this.widgetType = WidgetType.SLIDER;
			}
			else if (dataTypeId.equals(TermId.NUMERIC_VARIABLE.getId()) 
					|| dataTypeId.equals(TermId.NUMERIC_DBID_VARIABLE.getId())) {
				this.widgetType = WidgetType.NTEXT;
			}
			else {
				this.widgetType = WidgetType.CTEXT;
			}
		}
		else {
			this.widgetType = WidgetType.CTEXT;
		}
	}
	
	public WidgetType getWidgetType() {
		return this.widgetType;
	}
	
}
