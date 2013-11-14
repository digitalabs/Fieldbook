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
 * @author Efficio.Daniel
 *
 */
public class Plot implements Serializable{
    private int range;
    private int column;
    private boolean isUpward;
    private String displayString;
    private boolean isPlotDeleted;
    private boolean isNotStarted;
    private boolean isNoMoreEntries;
    

    public Plot( int column, int range, String displayString){
        setRange(range);
        setColumn(column);
        setDisplayString(displayString);
    }

    
    
    public boolean isPlotDeleted() {
        return isPlotDeleted;
    }


    
    public void setPlotDeleted(boolean isPlotDeleted) {
        this.isPlotDeleted = isPlotDeleted;
    }


    
    public boolean isNotStarted() {
        return isNotStarted;
    }


    
    public void setNotStarted(boolean isNotStarted) {
        this.isNotStarted = isNotStarted;
    }


    
    public boolean isNoMoreEntries() {
        return isNoMoreEntries;
    }


    
    public void setNoMoreEntries(boolean isNoMoreEntries) {
        this.isNoMoreEntries = isNoMoreEntries;
    }


    public String getDisplayString() {
        return displayString;
    }

    public void setDisplayString(String displayString) {
        this.displayString = displayString;
    }

    public boolean isUpward() {
        return isUpward;
    }

    public void setUpward(boolean upward) {
        isUpward = upward;
    }

    public int getRange() {
        return range;
    }

    public void setRange(int range) {
        this.range = range;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }
}
