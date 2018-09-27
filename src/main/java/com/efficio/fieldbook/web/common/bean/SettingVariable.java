/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.common.bean;

import java.io.Serializable;
import java.util.Set;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.FormulaDto;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.Operation;
import org.springframework.web.util.HtmlUtils;

import com.efficio.fieldbook.web.trial.bean.WidgetType;
import com.efficio.fieldbook.web.util.AppConstants;

public class SettingVariable implements Serializable {

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
	private Integer storedInId;

	private FormulaDto formula;

	private Set<VariableType> variableTypes;

	public SettingVariable() {
		super();
	}

	public SettingVariable(String name, String description, String property, String scale, String method, String role, String dataType) {
		super();
		this.name = name;
		this.description = description;
		this.property = property;
		this.scale = scale;
		this.method = method;
		this.role = role;
		this.dataType = dataType;
	}

	public SettingVariable(String name, String description, String property, String scale, String method, String role, String dataType,
			Integer dataTypeId, Double minRange, Double maxRange) {
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
		return this.traitClass;
	}

	public void setTraitClass(String traitClass) {
		this.traitClass = traitClass;
	}

	public String getCropOntologyId() {
		return this.cropOntologyId;
	}

	public void setCropOntologyId(String cropOntologyId) {
		this.cropOntologyId = cropOntologyId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getProperty() {
		return this.property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getScale() {
		return this.scale;
	}

	public void setScale(String scale) {
		this.scale = scale;
	}

	public String getMethod() {
		return this.method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getRole() {
		return this.role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getDataType() {
		return this.dataType;
	}

	public void setDataType(String dataType) {
		this.dataType = dataType;
	}

	/**
	 * @return the cvTermId
	 */
	public Integer getCvTermId() {
		return this.cvTermId;
	}

	/**
	 * @param cvTermId the cvTermId to set
	 */
	public void setCvTermId(Integer cvTermId) {
		this.cvTermId = cvTermId;
	}

	public void setPSMRFromStandardVariable(StandardVariable standardVariable, String role) {
		if (standardVariable != null) {
			this.property = HtmlUtils.htmlEscape(standardVariable.getProperty().getName());
			this.scale = HtmlUtils.htmlEscape(standardVariable.getScale().getName());
			this.method = HtmlUtils.htmlEscape(standardVariable.getMethod().getName());
			this.role = role;
			standardVariable.setPhenotypicType(PhenotypicType.getPhenotypicTypeByName(role));
			this.description = HtmlUtils.htmlEscape(standardVariable.getDescription());
			this.dataType = this.getDataType(standardVariable.getDataType().getId());
			this.dataTypeId = standardVariable.getDataType().getId();
			this.minRange =
					standardVariable.getConstraints() != null && standardVariable.getConstraints().getMinValue() != null ? standardVariable
							.getConstraints().getMinValue() : null;
					this.maxRange =
					standardVariable.getConstraints() != null && standardVariable.getConstraints().getMaxValue() != null ? standardVariable
							.getConstraints().getMaxValue() : null;
							this.setWidgetType();

		}
	}

	private String getDataType(int dataTypeId) {
		// datatype ids: 1120, 1125, 1128, 1130
		if (dataTypeId == TermId.CHARACTER_VARIABLE.getId() || dataTypeId == TermId.TIMESTAMP_VARIABLE.getId()
				|| dataTypeId == TermId.CHARACTER_DBID_VARIABLE.getId() || dataTypeId == TermId.CATEGORICAL_VARIABLE.getId()) {
			return "C";
		} else {
			return "N";
		}
	}

	/**
	 * @return the minRange
	 */
	public Double getMinRange() {
		return this.minRange;
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
		return this.maxRange;
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
		return this.dataTypeId;
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
		if (this.dataTypeId != null) {
			if (this.dataTypeId.equals(TermId.DATE_VARIABLE.getId())) {
				this.widgetType = WidgetType.DATE;
			} else if (this.dataTypeId.equals(TermId.CATEGORICAL_VARIABLE.getId())) {
				this.widgetType = WidgetType.DROPDOWN;
			} else if (this.minRange != null && this.maxRange != null) {
				this.widgetType = WidgetType.SLIDER;
			} else if (this.dataTypeId.equals(TermId.NUMERIC_VARIABLE.getId())
					|| this.dataTypeId.equals(TermId.NUMERIC_DBID_VARIABLE.getId())) {
				this.widgetType = WidgetType.NTEXT;
			} else {
				this.widgetType = WidgetType.CTEXT;
				if (this.cvTermId.toString().equalsIgnoreCase(AppConstants.OBJECTIVE_ID.getString())) {
					this.widgetType = WidgetType.TEXTAREA;
				}
			}
		} else {
			this.widgetType = WidgetType.CTEXT;
			if (this.cvTermId.toString().equalsIgnoreCase(AppConstants.OBJECTIVE_ID.getString())) {
				this.widgetType = WidgetType.TEXTAREA;
			}
		}

		if (this.cvTermId != null) {
			if (this.cvTermId.equals(TermId.LOCATION_ID.getId()) || this.cvTermId.equals(TermId.PI_ID.getId())
					|| this.cvTermId.equals(TermId.BREEDING_METHOD_ID.getId()) || this.cvTermId.equals(TermId.BREEDING_METHOD_CODE.getId())
					|| this.cvTermId.equals(TermId.NURSERY_TYPE.getId())
					|| this.cvTermId.toString().equalsIgnoreCase(AppConstants.COOPERATOR_ID.getString())) {
				this.widgetType = WidgetType.DROPDOWN;
			}
		}
		return this.widgetType;
	}

	/**
	 * @return the operation
	 */
	public Operation getOperation() {
		return this.operation;
	}

	/**
	 * @param operation the operation to set
	 */
	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	/**
	 * @return the storedInId
	 */
	public Integer getStoredInId() {
		return this.storedInId;
	}

	/**
	 * @param storedInId the storedInId to set
	 */
	public void setStoredInId(Integer storedInId) {
		this.storedInId = storedInId;
	}

	@Override
	public String toString() {
		return "SettingVariable [cvTermId=" + this.cvTermId + ", name=" + this.name + ", description=" + this.description + ", property="
				+ this.property + ", scale=" + this.scale + ", method=" + this.method + "]";
	}

	public void setVariableTypes(Set<VariableType> variableTypes) {
		this.variableTypes = variableTypes;
	}
	
	public Set<VariableType> getVariableTypes() {
		return this.variableTypes;
	}

	public FormulaDto getFormula() {
		return formula;
	}

	public void setFormula(final FormulaDto formula) {
		this.formula = formula;
	}

}
