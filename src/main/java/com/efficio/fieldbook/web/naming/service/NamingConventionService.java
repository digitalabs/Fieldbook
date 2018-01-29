/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.naming.service;

import java.util.List;
import java.util.Map;

import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.sample.PlantDTO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.common.bean.AdvanceResult;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.AdvancingSourceList;

/**
 *
 * Service for Rules Based Naming.
 * //TODO Remove advancing logic from this service
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
	 * @throws FieldbookException
	 */
	AdvanceResult advanceNursery(AdvancingNursery info, Workbook workbook) throws RuleException, MiddlewareQueryException, FieldbookException;

	List<ImportedGermplasm> generateGermplasmList(AdvancingSourceList rows, AdvancingNursery advancingParameters, Workbook workbook)
			throws RuleException;

	/*
	* Generated the names for the list of crosses based on on rules setup for the breeding methods
	*/
	List<ImportedCrosses> generateCrossesList(List<ImportedCrosses> importedCrosses, AdvancingSourceList rows, AdvancingNursery
			advancingParameters, Workbook workbook, List<Integer> gids) throws RuleException;

}
