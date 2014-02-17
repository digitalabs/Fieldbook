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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.dms.Phenotype;

import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.bean.SettingDetail;

/**
 * The Interface FieldbookService.
 */
public interface FieldbookService {
	
	/**
	 * Takes in an input stream representing the Excel file to be read, 
	 * and returns the temporary file name used to store it in the system.
	 *
	 * @param in the in
	 * @return the string
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
    String storeUserWorkbook(InputStream in) throws IOException;
    
    /**
     * Logic for advancing a nursery following a particular naming convention.
     * 
     * @param advanceInfo
     * @return
     * @throws MiddlewareQueryException
     */
    List<ImportedGermplasm> advanceNursery(AdvancingNursery advanceInfo) throws MiddlewareQueryException;
    
    /**
     * Filters the variables based on the current setting mode and excludes the selected ones.
     * 
     * @param sourceList
     * @param mode
     * @param selectedList
     * @return
     */
    List<StandardVariableReference> filterStandardVariablesForSetting(Collection<StandardVariable> sourceList, int mode, Collection<SettingDetail> selectedList);
    
    /**
     * Get all possible values.
     * 
     * @param id
     * @return
     * @throws MiddlewareQueryException
     */
    List<ValueReference> getAllPossibleValues(int id) throws MiddlewareQueryException;
    
    
    List<ValueReference> getAllPossibleValuesByPSMR(String property, String scale, String method, PhenotypicType phenotypeType) throws MiddlewareQueryException;

}
