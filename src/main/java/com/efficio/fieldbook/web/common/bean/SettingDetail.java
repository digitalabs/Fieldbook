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
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.util.Debug;

public class SettingDetail implements Serializable {

	private static final long serialVersionUID = 1L;
	private SettingVariable variable;
	private List<ValueReference> possibleValues;
	private List<ValueReference> possibleValuesFavorite;
	private List<ValueReference> filteredValues;
	private List<ValueReference> filteredFavoriteValues;
	private String possibleValuesJson;
	private String possibleValuesFavoriteJson;
	private String filteredValuesJson;
	private String filteredFavoriteValuesJson;
	private String value;
	private boolean isDeletable;
	private boolean isFavorite;
	private boolean isHidden;
	private boolean isFiltered;
	private int order;
	private Integer group;
	private PairedVariable pairedVariable;
	private PhenotypicType role;
	private VariableType variableType;
	

	public SettingDetail() {
		super();
	}

	public SettingDetail(SettingVariable variable, List<ValueReference> possibleValues, String value, boolean isDeletable) {
		super();
		this.variable = variable;
		this.possibleValues = possibleValues;
		this.value = value;
		this.isDeletable = isDeletable;
	}

	public SettingVariable getVariable() {
		return this.variable;
	}

	public void setVariable(SettingVariable variable) {
		this.variable = variable;
	}

	public List<ValueReference> getPossibleValues() {
		return this.possibleValues;
	}

	public void setPossibleValues(List<ValueReference> possibleValues) {
		this.possibleValues = possibleValues;
	}

	public List<ValueReference> getPossibleValuesFavorite() {
		return this.possibleValuesFavorite;
	}

	public void setPossibleValuesFavorite(List<ValueReference> possibleValuesFavorite) {
		this.possibleValuesFavorite = possibleValuesFavorite;
	}

	public String getPossibleValuesJson() {
		return this.possibleValuesJson;
	}

	public void setPossibleValuesJson(String possibleValuesJson) {
		this.possibleValuesJson = possibleValuesJson;
	}

	public void setPossibleValuesToJson(List<ValueReference> possibleValues) {
		try {
			ObjectMapper om = new ObjectMapper();
			this.setPossibleValuesJson(om.writeValueAsString(possibleValues));
		} catch (Exception e) {
			this.setPossibleValuesJson("err");
		}
	}

	public String getPossibleValuesFavoriteJson() {
		return this.possibleValuesFavoriteJson;
	}

	public void setPossibleValuesFavoriteJson(String possibleValuesFavoriteJson) {
		this.possibleValuesFavoriteJson = possibleValuesFavoriteJson;
	}

	public void setPossibleValuesFavoriteToJson(List<ValueReference> possibleValuesFavorite) {
		try {
			ObjectMapper om = new ObjectMapper();
			this.setPossibleValuesFavoriteJson(om.writeValueAsString(possibleValuesFavorite));
		} catch (Exception e) {
			this.setPossibleValuesFavoriteJson("err");
		}
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public boolean isDeletable() {
		return this.isDeletable;
	}

	public void setDeletable(boolean isDeletable) {
		this.isDeletable = isDeletable;
	}

	public boolean isFavorite() {
		return this.isFavorite;
	}

	public void setFavorite(boolean isFavorite) {
		this.isFavorite = isFavorite;
	}

	public int getOrder() {
		return this.order;
	}

	public void setOrder(int order) {
		this.order = order;
	}

	public Integer getGroup() {
		return this.group;
	}

	public void setGroup(Integer group) {
		this.group = group;
	}

	/**
	 * @return the pairedVariable
	 */
	public PairedVariable getPairedVariable() {
		return this.pairedVariable;
	}

	/**
	 * @param pairedVariable the pairedVariable to set
	 */
	public void setPairedVariable(PairedVariable pairedVariable) {
		this.pairedVariable = pairedVariable;
	}

	public boolean isHidden() {
		return this.isHidden;
	}

	public void setHidden(boolean isHidden) {
		this.isHidden = isHidden;
	}

	public PhenotypicType getRole() {
		return role;
	}

	public void setRole(PhenotypicType role) {
		this.role = role;
	}

	public VariableType getVariableType() {
		return variableType;
	}

	public void setVariableType(VariableType variableType) {
		this.variableType = variableType;
	}
	
	public List<ValueReference> getFilteredValues() {
		return filteredValues;
	}

	public void setFilteredValues(List<ValueReference> filteredValues) {
		this.filteredValues = filteredValues;
	}

	public boolean isFiltered() {
		return isFiltered;
	}

	public void setFiltered(boolean isFiltered) {
		this.isFiltered = isFiltered;
	}

	public List<ValueReference> getFilteredFavoriteValues() {
		return filteredFavoriteValues;
	}

	public void setFilteredFavoriteValues(List<ValueReference> filteredFavoriteValues) {
		this.filteredFavoriteValues = filteredFavoriteValues;
	}

	public String getDisplayValue() {
		if (this.getPossibleValues() != null && !this.getPossibleValues().isEmpty() && this.value != null) {

			List<ValueReference> possibleValues = this.getPossibleValues();
			for (ValueReference possibleValue : possibleValues) {
				if (possibleValue.getName().equalsIgnoreCase(this.value)) {
					return possibleValue.getDescription();
				}
			}
			return this.value; // this would return the value from the db
		}
		return this.value;
	}

	@Override
	public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SettingDetail [variable=");
        builder.append(this.variable);
        builder.append(", possibleValues=");
        builder.append(this.possibleValues);
        builder.append(", possibleValuesFavorite=");
        builder.append(this.possibleValuesFavorite);
        builder.append(", possibleValuesJson=");
        builder.append(this.possibleValuesJson);
        builder.append(", possibleValuesFavoriteJson=");
        builder.append(this.possibleValuesFavoriteJson);
        builder.append(", value=");
        builder.append(this.value);
        builder.append(", isDeletable=");
        builder.append(this.isDeletable);
        builder.append(", isFavorite=");
        builder.append(this.isFavorite);
        builder.append(", isHidden=");
        builder.append(this.isHidden);
        builder.append(", order=");
        builder.append(this.order);
        builder.append(", group=");
        builder.append(this.group);
        builder.append(", pairedVariable=");
        builder.append(this.pairedVariable);
        builder.append(", role=");
        builder.append(this.role);
        builder.append(", variableType=");
        builder.append(this.variableType);
        return builder.toString();
	}

	public void print(int indent) {
		Debug.println(indent, "Setting Detail: ");
		Debug.println(indent + 3, "variable: " + this.variable);
		Debug.println(indent + 3, "possibleValues: " + this.possibleValues);
		Debug.println(indent + 3, "possibleValuesFavorite: " + this.possibleValuesFavorite);
		Debug.println(indent + 3, "possibleValuesJson: " + this.possibleValuesJson);
		Debug.println(indent + 3, "possibleValuesFavoriteJson: " + this.possibleValuesFavoriteJson);
		Debug.println(indent + 3, "value: " + this.value);
		Debug.println(indent + 3, "isDeletable: " + this.isDeletable);
		Debug.println(indent + 3, "isFavorite: " + this.isFavorite);
		Debug.println(indent + 3, "isHidden: " + this.isHidden);
		Debug.println(indent + 3, "order: " + this.order);
		Debug.println(indent + 3, "group: " + this.group);
		Debug.println(indent + 3, "pairedVariable: " + this.pairedVariable);
		Debug.println(indent + 3, "role: " + this.role);
		Debug.println(indent + 3, "variableType: " + this.variableType);
	}

	public String getFilteredValuesJson() {
		return filteredValuesJson;
	}

	public void setFilteredValuesToJson(List<ValueReference> filteredValues) {
		try {
			ObjectMapper om = new ObjectMapper();
			this.setFilteredValuesJson(om.writeValueAsString(filteredValues));
		} catch (Exception e) {
			this.setPossibleValuesJson("err");
		}
	}
	
	public void setFilteredValuesJson(String filteredValuesJson) {
		this.filteredValuesJson = filteredValuesJson;
	}

	public String getFilteredFavoriteValuesJson() {
		return filteredFavoriteValuesJson;
	}

	public void setFilteredFavoriteValuesToJson(List<ValueReference> filteredFavoriteValues) {
		try {
			ObjectMapper om = new ObjectMapper();
			this.setFilteredFavoriteValuesJson(om.writeValueAsString(filteredFavoriteValues));
		} catch (Exception e) {
			this.setPossibleValuesJson("err");
		}
	}
	
	public void setFilteredFavoriteValuesJson(String filteredFavoriteValuesJson) {
		this.filteredFavoriteValuesJson = filteredFavoriteValuesJson;
	}


}
