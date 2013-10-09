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
package com.efficio.fieldbook.web.nursery.form;

import org.generationcp.middleware.domain.etl.Workbook;

public class NurseryDetailsForm {
    
    private Workbook workbook;
    
    private boolean fieldLayoutRandom = true;
       
    public Workbook getWorkbook() {
        return workbook;
    }
    
    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }

    public boolean getFieldLayoutRandom() {
        return fieldLayoutRandom;
    }
    
    public void setFieldLayoutRandom(boolean fieldLayoutRandom) {
        this.fieldLayoutRandom = fieldLayoutRandom;
    }
    
}
