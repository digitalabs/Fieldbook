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
package com.efficio.fieldbook.web.label.printing.form;

import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;

/**
 * The Class LabelPrintingForm.
 */
public class LabelPrintingForm{
    
    /** The is trial. */
    private boolean isTrial;
    
    /** The user label printing. */
    private UserLabelPrinting userLabelPrinting;
    
    /**
     * Gets the user label printing.
     *
     * @return the user label printing
     */
    public UserLabelPrinting getUserLabelPrinting() {
        return userLabelPrinting;
    }
    
    /**
     * Sets the user label printing.
     *
     * @param userLabelPrinting the new user label printing
     */
    public void setUserLabelPrinting(UserLabelPrinting userLabelPrinting) {
        this.userLabelPrinting = userLabelPrinting;
    }

    /**
     * Gets the checks if is trial.
     *
     * @return the checks if is trial
     */
    public boolean getIsTrial() {
        return isTrial;
    }
    
    /**
     * Sets the checks if is trial.
     *
     * @param isTrial the new checks if is trial
     */
    public void setIsTrial(boolean isTrial) {
        this.isTrial = isTrial;
    }
}
