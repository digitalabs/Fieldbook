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

/**
 * The Class ExportTrialInstanceBean.
 */
public class ExportTrialInstanceBean {

	/** The instance num. */
	private String instanceNum;

	/** The location name instance. */
	private String locationName;

	/** The instance id. */
	private Integer instanceId;

	/**
	 * Instantiates a new export trial instance bean.
	 *
	 * @param instanceNum the instance num
	 * @param locationName the location Name
	 * @param instanceId the instanceId
	 */
	public ExportTrialInstanceBean(final String instanceNum, final String locationName, final Integer instanceId) {
		super();
		this.instanceNum = instanceNum;
		this.setLocationName(locationName);
		this.setInstanceId(instanceId);

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
	public void setInstanceNum(final String instanceNum) {
		this.instanceNum = instanceNum;
	}

	/** The Location name. */
	public String getLocationName() {
		return this.locationName;
	}

	public void setLocationName(final String locationName) {
		this.locationName = locationName;
	}

	public Integer getInstanceId() {
		return this.instanceId;
	}

	public void setInstanceId(final Integer instanceId) {
		this.instanceId = instanceId;
	}
}
