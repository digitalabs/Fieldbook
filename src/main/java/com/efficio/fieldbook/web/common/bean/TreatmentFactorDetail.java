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

import org.generationcp.middleware.domain.oms.TermId;

import com.efficio.fieldbook.web.trial.bean.WidgetType;

// TODO: Auto-generated Javadoc
/**
 * The Class TreatmentFactorDetail.
 */
public class TreatmentFactorDetail implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The level id. */
	private Integer levelId;

	/** The amount id. */
	private Integer amountId;

	/** The level value. */
	private String levelValue;

	/** The amount value. */
	private String amountValue;

	/** The level name. */
	private String levelName;

	/** The amount name. */
	private String amountName;

	/** The amount data type id. */
	private Integer amountDataTypeId;

	/** The possible values json. */
	private String possibleValuesJson;

	/** The widget type. */
	private WidgetType widgetType;

	/** The min range. */
	private Double minRange;

	/** The max range. */
	private Double maxRange;

	private String levelDescription;

	/**
	 * Instantiates a new treatment factor detail.
	 */
	public TreatmentFactorDetail() {
		super();
	}

	/**
	 * Instantiates a new treatment factor detail.
	 *
	 * @param levelId the level id
	 * @param amountId the amount id
	 * @param levelValue the level value
	 * @param amountValue the amount value
	 * @param levelName the level name
	 * @param amountName the amount name
	 * @param amountDataTypeId the amount data type id
	 * @param possibleValuesJson the possible values json
	 */
	public TreatmentFactorDetail(Integer levelId, Integer amountId, String levelValue, String amountValue, String levelName,
			String amountName, Integer amountDataTypeId, String possibleValuesJson, Double minRange, Double maxRange) {
		super();
		this.setLevelId(levelId);
		this.setAmountId(amountId);
		this.setLevelValue(levelValue);
		this.amountValue = amountValue;
		this.levelName = levelName;
		this.amountName = amountName;
		this.amountDataTypeId = amountDataTypeId;
		this.possibleValuesJson = possibleValuesJson;
		this.minRange = minRange;
		this.maxRange = maxRange;
		this.setWidgetType();
	}

	/**
	 * Gets the amount value.
	 *
	 * @return the amountValue
	 */
	public String getAmountValue() {
		return this.amountValue;
	}

	/**
	 * Sets the amount value.
	 *
	 * @param amountValue the amountValue to set
	 */
	public void setAmountValue(String amountValue) {
		this.amountValue = amountValue;
	}

	/**
	 * Gets the level name.
	 *
	 * @return the levelName
	 */
	public String getLevelName() {
		return this.levelName;
	}

	/**
	 * Sets the level name.
	 *
	 * @param levelName the levelName to set
	 */
	public void setLevelName(String levelName) {
		this.levelName = levelName;
	}

	/**
	 * Gets the amount name.
	 *
	 * @return the amountName
	 */
	public String getAmountName() {
		return this.amountName;
	}

	/**
	 * Sets the amount name.
	 *
	 * @param amountName the amountName to set
	 */
	public void setAmountName(String amountName) {
		this.amountName = amountName;
	}

	/**
	 * Gets the level id.
	 *
	 * @return the levelId
	 */
	public Integer getLevelId() {
		return this.levelId;
	}

	/**
	 * Sets the level id.
	 *
	 * @param levelId the levelId to set
	 */
	public void setLevelId(Integer levelId) {
		this.levelId = levelId;
	}

	/**
	 * Gets the amount id.
	 *
	 * @return the amountId
	 */
	public Integer getAmountId() {
		return this.amountId;
	}

	/**
	 * Sets the amount id.
	 *
	 * @param amountId the amountId to set
	 */
	public void setAmountId(Integer amountId) {
		this.amountId = amountId;
	}

	/**
	 * Gets the level value.
	 *
	 * @return the levelValue
	 */
	public String getLevelValue() {
		return this.levelValue;
	}

	/**
	 * Sets the level value.
	 *
	 * @param levelValue the levelValue to set
	 */
	public void setLevelValue(String levelValue) {
		this.levelValue = levelValue;
	}

	/**
	 * Gets the widget type.
	 *
	 * @return the widgetType
	 */
	public WidgetType getWidgetType() {
		if (this.amountDataTypeId != null) {
			if (this.amountDataTypeId.equals(TermId.DATE_VARIABLE.getId())) {
				this.widgetType = WidgetType.DATE;
			} else if (this.minRange != null && this.maxRange != null) {
				this.widgetType = WidgetType.SLIDER;
			} else if (this.amountDataTypeId.equals(TermId.CATEGORICAL_VARIABLE.getId())) {
				this.widgetType = WidgetType.DROPDOWN;
			} else if (this.amountDataTypeId.equals(TermId.NUMERIC_VARIABLE.getId())
					|| this.amountDataTypeId.equals(TermId.NUMERIC_DBID_VARIABLE.getId())) {
				this.widgetType = WidgetType.NTEXT;
			} else {
				this.widgetType = WidgetType.CTEXT;
			}
		}

		return this.widgetType;
	}

	/**
	 * Sets the widget type.
	 *
	 */
	public void setWidgetType() {
	}

	/**
	 * Gets the amount data type id.
	 *
	 * @return the amountDataTypeId
	 */
	public Integer getAmountDataTypeId() {
		return this.amountDataTypeId;
	}

	/**
	 * Sets the amount data type id.
	 *
	 * @param amountDataTypeId the amountDataTypeId to set
	 */
	public void setAmountDataTypeId(Integer amountDataTypeId) {
		this.amountDataTypeId = amountDataTypeId;
	}

	/**
	 * Gets the possible values json.
	 *
	 * @return the possibleValuesJson
	 */
	public String getPossibleValuesJson() {
		return this.possibleValuesJson;
	}

	/**
	 * Sets the possible values json.
	 *
	 * @param possibleValuesJson the possibleValuesJson to set
	 */
	public void setPossibleValuesJson(String possibleValuesJson) {
		this.possibleValuesJson = possibleValuesJson;
	}

	/**
	 * Gets the min range.
	 *
	 * @return the min range
	 */
	public Double getMinRange() {
		return this.minRange;
	}

	/**
	 * Sets the min range.
	 *
	 * @param minRange the new min range
	 */
	public void setMinRange(Double minRange) {
		this.minRange = minRange;
	}

	/**
	 * Gets the max range.
	 *
	 * @return the max range
	 */
	public Double getMaxRange() {
		return this.maxRange;
	}

	/**
	 * Sets the max range.
	 *
	 * @param maxRange the new max range
	 */
	public void setMaxRange(Double maxRange) {
		this.maxRange = maxRange;
	}

	/**
	 * @return the levelDescription
	 */
	public String getLevelDescription() {
		return this.levelDescription;
	}

	/**
	 * @param levelDescription the levelDescription to set
	 */
	public void setLevelDescription(String levelDescription) {
		this.levelDescription = levelDescription;
	}

}
