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
	 * Gets the required variable.
	 *
	 * @return the required variable
	 */
	List<StandardVariable> getRequiredVariable();

	/**
	 * Validate.
	 *
	 * @param expDesignParameter the exp design parameter
	 * @return the exp design validation output
	 */
	ExpDesignValidationOutput validate(ExpDesignParameterUi expDesignParameter, List<ImportedGermplasm> germplasmList);

	/**
	 * Return the list of variables in exp design used by this design.
	 * 
	 * @return
	 */
	List<Integer> getExperimentalDesignVariables(ExpDesignParameterUi params);
}
