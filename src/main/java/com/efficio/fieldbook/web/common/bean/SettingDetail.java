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
	private List<ValueReference> allValues;
	private List<ValueReference> allFavoriteValues;
	private String possibleValuesJson;
	private String possibleValuesFavoriteJson;
	private String allValuesJson;
	private String allFavoriteValuesJson;
	private String value;
	private boolean isDeletable;
	private boolean isFavorite;
	private boolean isHidden;
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

	public void setPossibleValuesFavoriteJson(final String possibleValuesFavoriteJson) {
		this.possibleValuesFavoriteJson = possibleValuesFavoriteJson;
	}

	public void setPossibleValuesFavoriteToJson(final List<ValueReference> possibleValuesFavorite) {
		try {
			final ObjectMapper om = new ObjectMapper();
			this.setPossibleValuesFavoriteJson(om.writeValueAsString(possibleValuesFavorite));
		} catch (final Exception e) {
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

	public List<ValueReference> getAllValues() {
		return this.allValues;
	}

	public void setAllValues(final List<ValueReference> allValues) {
		this.allValues = allValues;
	}

	public String getAllValuesJson() {
		return this.allValuesJson;
	}

	public void setAllValuesJson(final String allValuesJson) {
		this.allValuesJson = allValuesJson;
	}

	public void setAllValuesToJson(final List<ValueReference> allValues) {
		try {
			final ObjectMapper om = new ObjectMapper();
			this.setAllValuesJson(om.writeValueAsString(allValues));
		} catch (final Exception e) {
			this.setPossibleValuesFavoriteJson("err");
		}
	}

	public List<ValueReference> getAllFavoriteValues() {
		return this.allFavoriteValues;
	}

	public void setAllFavoriteValues(final List<ValueReference> allFavoriteValues) {
		this.allFavoriteValues = allFavoriteValues;
	}

	public void setAllFavoriteValuesToJson(final List<ValueReference> allFavoriteValuesJson) {
		try {
			final ObjectMapper om = new ObjectMapper();
			this.setAllFavoriteValuesJson(om.writeValueAsString(allFavoriteValuesJson));
		} catch (final Exception e) {
			this.setPossibleValuesFavoriteJson("err");
		}

	}

	public String getAllFavoriteValuesJson() {
		return this.allFavoriteValuesJson;
	}

	public void setAllFavoriteValuesJson(final String allFavoriteValuesJson) {
		this.allFavoriteValuesJson = allFavoriteValuesJson;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.variable.getCvTermId() == null ? 0 : this.variable.getCvTermId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		SettingDetail other = (SettingDetail) obj;
		if (this.variable.getCvTermId() == null) {
			if (other.variable.getCvTermId() != null) {
				return false;
			}
		} else if (!this.variable.getCvTermId().equals(other.getVariable().getCvTermId())) {
			return false;
		}
		return true;
	}
}
