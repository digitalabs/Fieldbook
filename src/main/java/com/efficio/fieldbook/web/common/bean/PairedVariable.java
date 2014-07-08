package com.efficio.fieldbook.web.common.bean;

public class PairedVariable {

	private String label;
	private String value;
	
	public PairedVariable(String label, String value) {
		super();
		this.label = label;
		this.value = value;
	}
	
	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
	/**
	 * @param label the label to set
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	/**
	 * @return the value
	 */
	public String getValue() {
		return value;
	}
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	
}
