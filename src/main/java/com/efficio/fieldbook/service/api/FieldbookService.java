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
import java.util.Map;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StandardVariableReference;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;

// TODO: Auto-generated Javadoc
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
     * @param advanceInfo the advance info
     * @param workbook the workbook
     * @return the list
     * @throws MiddlewareQueryException the middleware query exception
     */
    List<ImportedGermplasm> advanceNursery(AdvancingNursery advanceInfo, Workbook workbook) throws MiddlewareQueryException;
    
    /**
     * Filters the variables based on the current setting mode and excludes the selected ones.
     *
     * @param mode the mode
     * @param selectedList the selected list
     * @return the list
     * @throws MiddlewareQueryException the middleware query exception
     */
    List<StandardVariableReference> filterStandardVariablesForSetting(int mode, Collection<SettingDetail> selectedList) throws MiddlewareQueryException;
    
    /**
     * Filter standard variables for trial setting.
     *
     * @param mode the mode
     * @param selectedList the selected list
     * @return the list
     * @throws MiddlewareQueryException the middleware query exception
     */
    List<StandardVariableReference> filterStandardVariablesForTrialSetting(int mode, Collection<SettingDetail> selectedList) throws MiddlewareQueryException;
    
    /**
     * Get all possible values.
     *
     * @param id the id
     * @return the all possible values
     * @throws MiddlewareQueryException the middleware query exception
     */
    List<ValueReference> getAllPossibleValues(int id) throws MiddlewareQueryException;
    
    /**
     * Gets the all possible values favorite.
     *
     * @param id the id
     * @param projectId the project id
     * @return the all possible values favorite
     * @throws MiddlewareQueryException the middleware query exception
     */
    List<ValueReference> getAllPossibleValuesFavorite(int id, String projectId) throws MiddlewareQueryException;    
    
    /**
     * Gets the all possible values by psmr.
     *
     * @param property the property
     * @param scale the scale
     * @param method the method
     * @param phenotypeType the phenotype type
     * @return the all possible values by psmr
     * @throws MiddlewareQueryException the middleware query exception
     */
    List<ValueReference> getAllPossibleValuesByPSMR(String property, String scale, String method, PhenotypicType phenotypeType) throws MiddlewareQueryException;

    /**
     * Gets the value.
     *
     * @param id the id
     * @param valueOrId the value or id
     * @param isCategorical the is categorical
     * @return the value
     * @throws MiddlewareQueryException the middleware query exception
     */
    String getValue(int id, String valueOrId, boolean isCategorical) throws MiddlewareQueryException;

    /**
     * Gets the person by id.
     *
     * @param id the id
     * @return the person by id
     * @throws MiddlewareQueryException the middleware query exception
     */
    String getPersonById(int id) throws MiddlewareQueryException;
    
    /**
     * Gets the term by id.
     *
     * @param termId the term id
     * @return the term by id
     * @throws MiddlewareQueryException the middleware query exception
     */
    Term getTermById(int termId) throws MiddlewareQueryException;
    
    /**
     * Gets the all breeding methods.
     *
     * @return the all breeding methods
     * @throws MiddlewareQueryException the middleware query exception
     */
    List<ValueReference> getAllBreedingMethods(boolean isFilterOutGenerative) throws MiddlewareQueryException;

    /**
     * Sets the all possible values in workbook.
     *
     * @param workbook the new all possible values in workbook
     * @throws MiddlewareQueryException the middleware query exception
     */
    void setAllPossibleValuesInWorkbook(Workbook workbook) throws MiddlewareQueryException;
    
    void createIdNameVariablePairs(Workbook workbook, List<SettingDetail> settingDetails, String idNamePairs, boolean deleteIdWhenNameExists) throws MiddlewareQueryException;
    Map<String,String> getIdNamePairForRetrieveAndSave();
}
