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


public class StudyTrialInstanceInfo implements Serializable{
    private FieldMapTrialInstanceInfo trialInstance;
    private String fieldbookName;
    
    public StudyTrialInstanceInfo(FieldMapTrialInstanceInfo trialInstance, String fieldbookName) {
        this.trialInstance = trialInstance;
        this.fieldbookName = fieldbookName; 
    }
    
    public FieldMapTrialInstanceInfo getTrialInstance() {
        return trialInstance;
    }
    
    public void setTrialInstance(FieldMapTrialInstanceInfo trialInstance){
        this.trialInstance = trialInstance;
    }
    
    public String getFieldbookName() {
        return fieldbookName;
    }
    
    public void setFieldbookName(String fieldbookName) {
        this.fieldbookName = fieldbookName;
    }
}
