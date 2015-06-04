
package com.efficio.fieldbook.web.common.bean;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class Value {

	private int rowIndex;
	private boolean isSelected;
	private String newValue;
	private String action;
	private final Map<String, Object> additionalProperties = new HashMap<String, Object>();

	/**
	 *
	 * @return The rowIndex
	 */
	public int getRowIndex() {
		return this.rowIndex;
	}

	/**
	 *
	 * @param rowIndex The rowIndex
	 */
	public void setRowIndex(int rowIndex) {
		this.rowIndex = rowIndex;
	}

	/**
	 *
	 * @return The isSelected
	 */
	public boolean isSelected() {
		return this.isSelected;
	}

	/**
	 *
	 * @param isSelected The isSelected
	 */
	public void setIsSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	/**
	 *
	 * @return The newValue
	 */
	public String getNewValue() {
		return this.newValue;
	}

	/**
	 *
	 * @param newValue The newValue
	 */
	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	/**
	 *
	 * @return The action
	 */
	public String getAction() {
		return this.action;
	}

	/**
	 *
	 * @param action The action
	 */
	public void setAction(String action) {
		this.action = action;
	}

	public Map<String, Object> getAdditionalProperties() {
		return this.additionalProperties;
	}

	public void setAdditionalProperty(String name, Object value) {
		this.additionalProperties.put(name, value);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

}
