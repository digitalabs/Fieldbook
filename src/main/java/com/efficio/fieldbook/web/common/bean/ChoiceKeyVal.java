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

// TODO: Auto-generated Javadoc
/**
 * The Class ChoiceKeyVal.
 */
public class ChoiceKeyVal {

	/** The name. */
	private String name;

	/** The val. */
	private String val;

	/**
	 * Instantiates a new choice key val.
	 *
	 * @param name the name
	 * @param val the val
	 */
	public ChoiceKeyVal(String name, String val) {
		this.name = name;
		this.val = val;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the val.
	 *
	 * @return the val
	 */
	public String getVal() {
		return this.val;
	}

	/**
	 * Sets the val.
	 *
	 * @param val the new val
	 */
	public void setVal(String val) {
		this.val = val;
	}

}
