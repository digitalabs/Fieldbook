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

public class LabelPrintingForm{
    private boolean isTrial;
    private UserLabelPrinting userLabelPrinting;
        
    
    
    public UserLabelPrinting getUserLabelPrinting() {
        return userLabelPrinting;
    }

    
    public void setUserLabelPrinting(UserLabelPrinting userLabelPrinting) {
        this.userLabelPrinting = userLabelPrinting;
    }

    public boolean getIsTrial() {
        return isTrial;
    }
    
    public void setIsTrial(boolean isTrial) {
        this.isTrial = isTrial;
    }
}
