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

/**
 * The Class NurseryDetailsForm.
 */
public class NurseryDetailsForm {
    
    /** The workbook. */
    private Workbook workbook;
    
    /** The field layout random. */
    private boolean fieldLayoutRandom = true;
       
    /**
     * Gets the workbook.
     *
     * @return the workbook
     */
    public Workbook getWorkbook() {
        return workbook;
    }
    
    /**
     * Sets the workbook.
     *
     * @param workbook the new workbook
     */
    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }

    /**
     * Gets the field layout random.
     *
     * @return the field layout random
     */
    public boolean getFieldLayoutRandom() {
        return fieldLayoutRandom;
    }
    
    /**
     * Sets the field layout random.
     *
     * @param fieldLayoutRandom the new field layout random
     */
    public void setFieldLayoutRandom(boolean fieldLayoutRandom) {
        this.fieldLayoutRandom = fieldLayoutRandom;
    }
    
}
