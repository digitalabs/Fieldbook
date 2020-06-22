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

package com.efficio.fieldbook.web.fieldmap.bean;

import java.io.Serializable;

/**
 * The Class SelectedFieldmapRow.
 */
public class SelectedFieldmapRow implements Comparable<SelectedFieldmapRow>, Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1435511995297357029L;

	/** The order. */
	private Integer order;

	/** The study name. */
	private String studyName;

	/** The trial instance no. */
	private String trialInstanceNo;

	/** The rep count. */
	private Long repCount;

	/** The entry count. */
	private Long entryCount;

	/** The plot count. */
	private Long plotCount;

	/** The dataset name. */
	private String datasetName;

	/** The study id. */
	private Integer studyId;

	/** The dataset id. */
	private Integer datasetId;

	/** The instance id. */
	private Integer instanceId;

	/**
	 * Gets the order.
	 *
	 * @return the order
	 */
	public Integer getOrder() {
		return this.order;
	}

	/**
	 * Sets the order.
	 *
	 * @param order the order to set
	 */
	public void setOrder(Integer order) {
		this.order = order;
	}

	/**
	 * Gets the study name.
	 *
	 * @return the studyName
	 */
	public String getStudyName() {
		return this.studyName;
	}

	/**
	 * Sets the study name.
	 *
	 * @param studyName the studyName to set
	 */
	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

	/**
	 * Gets the trial instance no.
	 *
	 * @return the trialInstanceNo
	 */
	public String getTrialInstanceNo() {
		return this.trialInstanceNo;
	}

	/**
	 * Sets the trial instance no.
	 *
	 * @param trialInstanceNo the trialInstanceNo to set
	 */
	public void setTrialInstanceNo(String trialInstanceNo) {
		this.trialInstanceNo = trialInstanceNo;
	}

	/**
	 * Gets the rep count.
	 *
	 * @return the repCount
	 */
	public Long getRepCount() {
		return this.repCount;
	}

	/**
	 * Sets the rep count.
	 *
	 * @param repCount the repCount to set
	 */
	public void setRepCount(Long repCount) {
		this.repCount = repCount;
	}

	/**
	 * Gets the entry count.
	 *
	 * @return the entryCount
	 */
	public Long getEntryCount() {
		return this.entryCount;
	}

	/**
	 * Sets the entry count.
	 *
	 * @param entryCount the entryCount to set
	 */
	public void setEntryCount(Long entryCount) {
		this.entryCount = entryCount;
	}

	/**
	 * Gets the plot count.
	 *
	 * @return the plotCount
	 */
	public Long getPlotCount() {
		return this.plotCount;
	}

	/**
	 * Sets the plot count.
	 *
	 * @param plotCount the plotCount to set
	 */
	public void setPlotCount(Long plotCount) {
		this.plotCount = plotCount;
	}

	/**
	 * Gets the study id.
	 *
	 * @return the studyId
	 */
	public Integer getStudyId() {
		return this.studyId;
	}

	/**
	 * Sets the study id.
	 *
	 * @param studyId the studyId to set
	 */
	public void setStudyId(Integer studyId) {
		this.studyId = studyId;
	}

	/**
	 * Gets the dataset id.
	 *
	 * @return the datasetId
	 */
	public Integer getDatasetId() {
		return this.datasetId;
	}

	/**
	 * Sets the dataset id.
	 *
	 * @param datasetId the datasetId to set
	 */
	public void setDatasetId(Integer datasetId) {
		this.datasetId = datasetId;
	}

	/**
	 * Gets the instance id.
	 *
	 * @return the instanceId
	 */
	public Integer getInstanceId() {
		return this.instanceId;
	}

	/**
	 * Sets the instance id.
	 *
	 * @param instanceId the instanceId to set
	 */
	public void setInstanceId(Integer instanceId) {
		this.instanceId = instanceId;
	}

	/**
	 * Gets the dataset name.
	 *
	 * @return the datasetName
	 */
	public String getDatasetName() {
		return this.datasetName;
	}

	/**
	 * Sets the dataset name.
	 *
	 * @param datasetName the datasetName to set
	 */
	public void setDatasetName(String datasetName) {
		this.datasetName = datasetName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(SelectedFieldmapRow o) {
		if (this.order != null && o != null) {
			return this.getOrder().compareTo(o.getOrder());
		}
		return 0;
	}

}
