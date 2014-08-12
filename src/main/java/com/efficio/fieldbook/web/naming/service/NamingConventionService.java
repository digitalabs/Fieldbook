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
package com.efficio.fieldbook.web.naming.service;

import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;

import com.efficio.fieldbook.web.common.bean.AdvanceResult;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;

/**
 * 
 * Service for Advancing Nursery.
 *
 */
public interface NamingConventionService {

    /**
     * Provides the service for advancing a nursery.
     * 
     * @param info
     * @return
     * @throws MiddlewareQueryException
     */
    AdvanceResult advanceNursery(AdvancingNursery info, Workbook workbook) throws MiddlewareQueryException;
}
