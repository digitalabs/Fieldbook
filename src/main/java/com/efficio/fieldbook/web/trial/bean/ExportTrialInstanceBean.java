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

	/** The environment id. */
	private Integer environmentId;

	/**
	 * Instantiates a new export trial instance bean.
	 *
	 * @param instanceNum the instance num
	 * @param locationName the location Name
	 * @param environmentId the geolocationId
	 */
	public ExportTrialInstanceBean(final String instanceNum, final String locationName, final Integer environmentId) {
		super();
		this.instanceNum = instanceNum;
		this.setLocationName(locationName);
		this.setEnvironmentId(environmentId);

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

	public Integer getEnvironmentId() {
		return this.environmentId;
	}

	public void setEnvironmentId(final Integer environmentId) {
		this.environmentId = environmentId;
	}
}
