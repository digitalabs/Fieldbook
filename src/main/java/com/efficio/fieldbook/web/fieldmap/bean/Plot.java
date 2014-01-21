/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package com.efficio.fieldbook.web.fieldmap.bean;

import java.io.Serializable;


/**
 * The Class Plot.  This would be use track the plots 
 *
 * @author Efficio.Daniel
 */
public class Plot implements Serializable{
    
    private static final long serialVersionUID = 1L;

    /** The range. */
    private int range;
    
    /** The column. */
    private int column;
    
    /** The is upward. The direction of seed distribution */
    private boolean isUpward;
    
    /** The display string. */
    private String displayString;
    
    /** The is plot deleted. */
    private boolean isPlotDeleted;
    
    /** The is not started. */
    private boolean isNotStarted;
    
    /** The is no more entries. */
    private boolean isNoMoreEntries;

    private Integer datasetId;
    
    private Integer geolocationId;

    /**
     * Instantiates a new plot.
     *
     * @param column the column
     * @param range the range
     * @param displayString the display string
     */
    public Plot( int column, int range, String displayString){
        setRange(range);
        setColumn(column);
        setDisplayString(displayString);
    }

    
    
    /**
     * Checks if is plot deleted.
     *
     * @return true, if is plot deleted
     */
    public boolean isPlotDeleted() {
        return isPlotDeleted;
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
        return isNotStarted;
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
        return isNoMoreEntries;
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
        return displayString;
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
     * Checks if is upward.
     *
     * @return true, if is upward
     */
    /*
    public boolean isUpward() {
        return isUpward;
    }
	*/
    /**
     * Sets the upward.
     *
     * @param upward the new upward
     */
    /*
    public void setUpward(boolean upward) {
        isUpward = upward;
    }
	*/
    /**
     * Gets the range.
     *
     * @return the range
     */
    public int getRange() {
        return range;
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
        return column;
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
     * @return the datasetId
     */
    public Integer getDatasetId() {
        return datasetId;
    }



    
    /**
     * @param datasetId the datasetId to set
     */
    public void setDatasetId(Integer datasetId) {
        this.datasetId = datasetId;
    }



    
    /**
     * @return the geolocationId
     */
    public Integer getGeolocationId() {
        return geolocationId;
    }



    
    /**
     * @param geolocationId the geolocationId to set
     */
    public void setGeolocationId(Integer geolocationId) {
        this.geolocationId = geolocationId;
    }

}
