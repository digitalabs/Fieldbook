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

import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;

import com.efficio.fieldbook.web.fieldmap.bean.UserFieldmap;
import com.efficio.fieldbook.web.label.printing.bean.UserLabelPrinting;


public interface LabelPrintingService {
        
    public String generatePDFLabels(FieldMapDatasetInfo datasetInfo, UserLabelPrinting userLabelPrinting, ByteArrayOutputStream baos) throws MiddlewareQueryException;
    public String generateXlSLabels(FieldMapDatasetInfo datasetInfo, UserLabelPrinting userLabelPrinting, ByteArrayOutputStream baos) throws MiddlewareQueryException;
}
