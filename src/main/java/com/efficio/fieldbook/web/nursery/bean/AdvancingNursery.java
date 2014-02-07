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
package com.efficio.fieldbook.web.nursery.bean;

import java.io.Serializable;
import java.util.List;

import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;

/**
 * This bean models the various input that the user builds up over time
 * to perform the actual loading operation.
 */
public class AdvancingNursery implements Serializable {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
    private Study study;
    
    /** The naming convention. */
    private String namingConvention;
    
    /** The suffix convention. */
    private String suffixConvention;
    
    /** The method choice. */
    private String methodChoice;
    
    /** The line choice. */
    private String lineChoice;
    
    /** The line selected. */
    private String lineSelected;
    
    /** The harvest date. */
    private String harvestDate;
    
    /** The harvest location. */
    private String harvestLocation;
    
    public Study getStudy() {
        return study;
    }
    
    public void setStudy(Study study) {
        this.study = study;
    }	   
    
}
