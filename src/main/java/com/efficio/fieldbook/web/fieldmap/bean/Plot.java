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
 * The Class Plot. This would be use track the plots
 *
 * @author Efficio.Daniel
 */
public class Plot implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The range. */
	private int range;

	/** The column. */
	private int column;

	/** The display string. */
	private String displayString;

	/** The is plot deleted. */
	private boolean isPlotDeleted;

	/** The is not started. */
	private boolean isNotStarted;

	/** The is no more entries. */
	private boolean isNoMoreEntries;

	/** The dataset id. */
	private Integer datasetId;

	// TODO IBP-3305: rename to environmentId
	/** The geolocation id. */
	private Integer geolocationId;

	/** The is saved already. */
	private boolean isSavedAlready;

	/**
	 * Instantiates a new plot.
	 *
	 * @param column the column
	 * @param range the range
	 * @param displayString the display string
	 */
	public Plot(int column, int range, String displayString) {
		this.setRange(range);
		this.setColumn(column);
		this.setDisplayString(displayString);
	}

	/**
	 * Checks if is plot deleted.
	 *
	 * @return true, if is plot deleted
	 */
	public boolean isPlotDeleted() {
		return this.isPlotDeleted;
	}

	/**
	 * Sets the plot deleted.
	 *
	 * @param isPlotDeleted the new plot deleted
	 */
	public void setPlotDeleted(boolean isPlotDeleted) {
		this.isPlotDeleted = isPlotDeleted;
	}

	/**
	 * Checks if is not started.
	 *
	 * @return true, if is not started
	 */
	public boolean isNotStarted() {
		return this.isNotStarted;
	}

	/**
	 * Sets the not started.
	 *
	 * @param isNotStarted the new not started
	 */
	public void setNotStarted(boolean isNotStarted) {
		this.isNotStarted = isNotStarted;
	}

	/**
	 * Checks if is no more entries.
	 *
	 * @return true, if is no more entries
	 */
	public boolean isNoMoreEntries() {
		return this.isNoMoreEntries;
	}

	/**
	 * Sets the no more entries.
	 *
	 * @param isNoMoreEntries the new no more entries
	 */
	public void setNoMoreEntries(boolean isNoMoreEntries) {
		this.isNoMoreEntries = isNoMoreEntries;
	}

	/**
	 * Gets the display string.
	 *
	 * @return the display string
	 */
	public String getDisplayString() {
		return this.displayString;
	}

	/**
	 * Sets the display string.
	 *
	 * @param displayString the new display string
	 */
	public void setDisplayString(String displayString) {
		this.displayString = displayString;
	}

	/**
	 * Gets the range.
	 *
	 * @return the range
	 */
	public int getRange() {
		return this.range;
	}

	/**
	 * Sets the range.
	 *
	 * @param range the new range
	 */
	public void setRange(int range) {
		this.range = range;
	}

	/**
	 * Gets the column.
	 *
	 * @return the column
	 */
	public int getColumn() {
		return this.column;
	}

	/**
	 * Sets the column.
	 *
	 * @param column the new column
	 */
	public void setColumn(int column) {
		this.column = column;
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
	 * Gets the geolocation id.
	 *
	 * @return the geolocationId
	 */
	public Integer getGeolocationId() {
		return this.geolocationId;
	}

	/**
	 * Sets the geolocation id.
	 *
	 * @param geolocationId the geolocationId to set
	 */
	public void setGeolocationId(Integer geolocationId) {
		this.geolocationId = geolocationId;
	}

	/**
	 * Checks if is saved already.
	 *
	 * @return true, if is saved already
	 */
	public boolean isSavedAlready() {
		return this.isSavedAlready;
	}

	/**
	 * Sets the saved already.
	 *
	 * @param isSavedAlready the new saved already
	 */
	public void setSavedAlready(boolean isSavedAlready) {
		this.isSavedAlready = isSavedAlready;
	}

}
