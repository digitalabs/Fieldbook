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
import org.generationcp.middleware.domain.dms.ValueReference;

public class SettingDetail implements Serializable {

	private static final long serialVersionUID = 1L;
	private SettingVariable variable;
	private List<ValueReference> possibleValues;
	private List<ValueReference> possibleValuesFavorite;
	private String possibleValuesJson;
	private String possibleValuesFavoriteJson;
	private String value;
	private boolean isDeletable;
	private boolean isFavorite;
	private boolean isHidden;
	private int order;
	private Integer group;
	private PairedVariable pairedVariable;

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
		return "SettingDetail [variable=" + this.variable + ", possibleValues=" + this.possibleValues + ", possibleValuesFavorite="
				+ this.possibleValuesFavorite + ", value=" + this.value + ", isDeletable=" + this.isDeletable + ", isFavorite="
				+ this.isFavorite + ", isHidden=" + this.isHidden + ", order=" + this.order + ", group=" + this.group + ", pairedVariable="
				+ this.pairedVariable + "]";
	}

}
