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

import java.util.List;

import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;

import com.efficio.fieldbook.web.common.bean.AdvanceResult;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSourceList;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;

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
     * @throws RuleException 
     */
    AdvanceResult advanceNursery(AdvancingNursery info, Workbook workbook) throws MiddlewareQueryException, RuleException;
    
    List<ImportedGermplasm> generateGermplasmList(AdvancingSourceList rows, boolean checkForUniqueness) throws MiddlewareQueryException, RuleException;
    
}
