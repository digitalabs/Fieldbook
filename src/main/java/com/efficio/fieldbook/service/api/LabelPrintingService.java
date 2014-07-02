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
package com.efficio.fieldbook.service.api;

import java.io.ByteArrayOutputStream;
import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;

import com.efficio.fieldbook.web.common.exception.LabelPrintingException;
import com.efficio.fieldbook.web.label.printing.bean.StudyTrialInstanceInfo;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;

/**
 * The Interface LabelPrintingService.
 */
public interface LabelPrintingService {
        
    /**
     * Generate pdf labels.
     *
     * @param trialInstances the trial instances
     * @param userLabelPrinting the user label printing
     * @param baos the baos
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    String generatePDFLabels(List<StudyTrialInstanceInfo> trialInstances, 
            UserLabelPrinting userLabelPrinting, ByteArrayOutputStream baos) 
                    throws MiddlewareQueryException, LabelPrintingException;
    
    /**
     * Generate xl s labels.
     *
     * @param trialInstances the trial instances
     * @param userLabelPrinting the user label printing
     * @param baos the baos
     * @return the string
     * @throws MiddlewareQueryException the middleware query exception
     */
    String generateXlSLabels(List<StudyTrialInstanceInfo> trialInstances, 
            UserLabelPrinting userLabelPrinting, ByteArrayOutputStream baos) 
                    throws MiddlewareQueryException;
}
