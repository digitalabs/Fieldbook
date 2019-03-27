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

import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.middleware.domain.etl.Workbook;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.common.bean.AdvanceResult;
import com.efficio.fieldbook.web.trial.bean.AdvancingStudy;
import org.generationcp.commons.pojo.AdvancingSourceList;

/**
 *
 * Service for Rules Based Naming.
 * //TODO Remove advancing logic from this service
 *
 */
public interface NamingConventionService {

	/**
	 * Provides the service for advancing a study.
	 *
	 * @param info
	 * @return
	 * @throws RuleException
	 * @throws FieldbookException
	 */
	AdvanceResult advanceStudy(AdvancingStudy info, Workbook workbook) throws RuleException, FieldbookException;

	List<ImportedGermplasm> generateGermplasmList(AdvancingSourceList rows, AdvancingStudy advancingParameters, Workbook workbook)
			throws RuleException;

	/*
	* Generated the names for the list of crosses based on on rules setup for the breeding methods
	*/
	List<ImportedCrosses> generateCrossesList(List<ImportedCrosses> importedCrosses, AdvancingSourceList rows, AdvancingStudy
			advancingParameters, Workbook workbook, List<Integer> gids) throws RuleException;

}
