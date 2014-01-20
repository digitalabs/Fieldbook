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
package com.efficio.fieldbook.web.label.printing.bean;

import java.io.Serializable;

import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;


/**
 * The Class StudyTrialInstanceInfo.
 */
public class StudyTrialInstanceInfo implements Serializable{
    
    /** The trial instance. */
    private FieldMapTrialInstanceInfo trialInstance;
    
    /** The fieldbook name. */
    private String fieldbookName;
    
    /**
     * Instantiates a new study trial instance info.
     *
     * @param trialInstance the trial instance
     * @param fieldbookName the fieldbook name
     */
    public StudyTrialInstanceInfo(FieldMapTrialInstanceInfo trialInstance, String fieldbookName) {
        this.trialInstance = trialInstance;
        this.fieldbookName = fieldbookName; 
    }
    
    /**
     * Gets the trial instance.
     *
     * @return the trial instance
     */
    public FieldMapTrialInstanceInfo getTrialInstance() {
        return trialInstance;
    }
    
    /**
     * Sets the trial instance.
     *
     * @param trialInstance the new trial instance
     */
    public void setTrialInstance(FieldMapTrialInstanceInfo trialInstance){
        this.trialInstance = trialInstance;
    }
    
    /**
     * Gets the fieldbook name.
     *
     * @return the fieldbook name
     */
    public String getFieldbookName() {
        return fieldbookName;
    }
    
    /**
     * Sets the fieldbook name.
     *
     * @param fieldbookName the new fieldbook name
     */
    public void setFieldbookName(String fieldbookName) {
        this.fieldbookName = fieldbookName;
    }
}
