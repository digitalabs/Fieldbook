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

package com.efficio.fieldbook.web.trial.bean;

// TODO: Auto-generated Javadoc
/**
 * The Class ExportTrialInstanceBean.
 */
public class ExportTrialInstanceBean {

	/** The instance num. */
	private String instanceNum;

	/** The has fieldmap. */
	private boolean hasFieldmap;

	/**
	 * Instantiates a new export trial instance bean.
	 *
	 * @param instanceNum the instance num
	 * @param hasFieldmap the has fieldmap
	 */
	public ExportTrialInstanceBean(String instanceNum, boolean hasFieldmap) {
		super();
		this.instanceNum = instanceNum;
		this.hasFieldmap = hasFieldmap;
	}

	/**
	 * Gets the instance num.
	 *
	 * @return the instance num
	 */
	public String getInstanceNum() {
		return this.instanceNum;
	}

	/**
	 * Sets the instance num.
	 *
	 * @param instanceNum the new instance num
	 */
	public void setInstanceNum(String instanceNum) {
		this.instanceNum = instanceNum;
	}

	/**
	 * Checks if is checks for fieldmap.
	 *
	 * @return true, if is checks for fieldmap
	 */
	public boolean isHasFieldmap() {
		return this.hasFieldmap;
	}

	/**
	 * Sets the checks for fieldmap.
	 *
	 * @param hasFieldmap the new checks for fieldmap
	 */
	public void setHasFieldmap(boolean hasFieldmap) {
		this.hasFieldmap = hasFieldmap;
	}

	/**
	 * Gets the checks for fieldmap display.
	 *
	 * @return the checks for fieldmap display
	 */
	public String getHasFieldmapDisplay() {
		if (this.hasFieldmap) {
			return "Yes";
		}
		return "No";
	}
}
