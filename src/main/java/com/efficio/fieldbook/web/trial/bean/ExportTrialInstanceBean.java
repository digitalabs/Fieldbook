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

	/** The geolocation id. */
	private Integer geolocationId;

	/**
	 * Instantiates a new export trial instance bean.
	 *
	 * @param instanceNum the instance num
	 * @param locationName the location Name
	 * @param geolocationId the geolocationId
	 */
	public ExportTrialInstanceBean(final String instanceNum, final String locationName, final Integer geolocationId) {
		super();
		this.instanceNum = instanceNum;
		this.setLocationName(locationName);
		this.setGeolocationId(geolocationId);

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

	/** The Location name. */
	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public Integer getGeolocationId() {
		return geolocationId;
	}

	public void setGeolocationId(final Integer geolocationId) {
		this.geolocationId = geolocationId;
	}
}
