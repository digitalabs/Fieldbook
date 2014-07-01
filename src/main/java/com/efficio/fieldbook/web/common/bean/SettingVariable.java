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
package com.efficio.fieldbook.web.common.bean;

import java.io.Serializable;

import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.Operation;
import org.springframework.web.util.HtmlUtils;

import com.efficio.fieldbook.web.nursery.bean.WidgetType;
import com.efficio.fieldbook.web.util.AppConstants;


public class SettingVariable implements Serializable{

	private static final long serialVersionUID = 1L;

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
	private Operation operation;
	
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
		this.minRange = minRange;
		this.maxRange = maxRange;
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
			this.property = HtmlUtils.htmlEscape(standardVariable.getProperty().getName());
			this.scale = HtmlUtils.htmlEscape(standardVariable.getScale().getName());
			this.method = HtmlUtils.htmlEscape(standardVariable.getMethod().getName());
			this.role = HtmlUtils.htmlEscape(standardVariable.getPhenotypicType().name());
			this.description = HtmlUtils.htmlEscape(standardVariable.getDescription());
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
		
	}


    // TODO : shift computation of widget type elsewhere, to avoid having it continuously recomputed
	public WidgetType getWidgetType() {

        if (dataTypeId != null) {
            if (dataTypeId.equals(TermId.DATE_VARIABLE.getId())) {
                this.widgetType = WidgetType.DATE;
            } else if (dataTypeId.equals(TermId.CATEGORICAL_VARIABLE.getId())) {
                this.widgetType = WidgetType.DROPDOWN;
            } else if (minRange != null && maxRange != null) {
                this.widgetType = WidgetType.SLIDER;
            } else if (dataTypeId.equals(TermId.NUMERIC_VARIABLE.getId())
                    || dataTypeId.equals(TermId.NUMERIC_DBID_VARIABLE.getId())) {
                this.widgetType = WidgetType.NTEXT;
            } else {
                this.widgetType = WidgetType.CTEXT;
                if (cvTermId.toString().equalsIgnoreCase(AppConstants.OBJECTIVE_ID.getString()))
                    this.widgetType = WidgetType.TEXTAREA;
            }
        } else {
            this.widgetType = WidgetType.CTEXT;
            if (cvTermId.toString().equalsIgnoreCase(AppConstants.OBJECTIVE_ID.getString()))
                this.widgetType = WidgetType.TEXTAREA;
        }

        if (cvTermId != null) {
            if (cvTermId.equals(TermId.LOCATION_ID.getId()) || cvTermId.equals(TermId.PI_ID.getId())
                    || cvTermId.equals(TermId.BREEDING_METHOD_ID.getId()) ||
                    cvTermId.equals(TermId.NURSERY_TYPE.getId())
                    || cvTermId.toString().equalsIgnoreCase(AppConstants.COOPERATOR_ID.getString())) {
                this.widgetType = WidgetType.DROPDOWN;
            }
        }
        return this.widgetType;
    }



    /**
     * @return the operation
     */
    public Operation getOperation() {
        return operation;
    }



    /**
     * @param operation the operation to set
     */
    public void setOperation(Operation operation) {
        this.operation = operation;
    }
	
}
