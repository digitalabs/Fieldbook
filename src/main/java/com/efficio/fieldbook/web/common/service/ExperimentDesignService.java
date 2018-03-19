/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.common.service;

import java.util.List;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;

import com.efficio.fieldbook.web.common.exception.BVDesignException;
import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import com.efficio.fieldbook.web.trial.bean.ExpDesignValidationOutput;

// TODO: Auto-generated Javadoc
/**
 * The Interface ExperimentDesignService.
 */
public interface ExperimentDesignService {
	
	Integer MAX_ENTRY_NO = 99999;
	Integer MAX_PLOT_NO = 99999999;
	/**
	 * Generate design.
	 *
	 * @param germplasmList the germplasm list
	 * @param parameterMap the parameter map
	 * @param nonTrialFactors the non trial factors
	 * @param variates the variates
	 * @param treatmentVariables the treatment variables
	 * @param treatmentFactorValues the treatment factor values
	 * @return the list
	 */
	List<MeasurementRow> generateDesign(List<ImportedGermplasm> germplasmList, ExpDesignParameterUi parameter,
			List<MeasurementVariable> trialVariables, List<MeasurementVariable> factors, List<MeasurementVariable> nonTrialFactors,
			List<MeasurementVariable> variates, List<TreatmentVariable> treatmentVariables) throws BVDesignException;

	/**
	 * Gets the list of  variables necessary for generating a design (e.g. PLOT_NO, ENTRY_NO, REP_NO).
	 *
	 * @return list of standard variables.
	 */
	List<StandardVariable> getRequiredDesignVariables();

	/**
	 * Validates the design parameters and germplasm list entries.
	 *
	 * @param expDesignParameter the exp design parameter
	 * @return the exp design validation output
	 */
	ExpDesignValidationOutput validate(ExpDesignParameterUi expDesignParameter, List<ImportedGermplasm> germplasmList);

	/**
	 * Gets the list of variables in experimental design (e.g. NUMBER_OF_REPLICATES, NUMBER_OF_REPLICATES, NBLKS)
	 * 
	 * @return
	 */
	List<Integer> getExperimentalDesignVariables(ExpDesignParameterUi params);

	/**
	 * Defines if the experimental design requires breeding view licence to run
	 * @return
	 */
	Boolean requiresBreedingViewLicence();
}
