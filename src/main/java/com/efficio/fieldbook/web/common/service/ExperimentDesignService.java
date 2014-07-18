/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/
package com.efficio.fieldbook.web.common.service;


import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.etl.Workbook;

import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;


public interface ExperimentDesignService {	
	
	List<MeasurementRow> generateDesign(List<ImportedGermplasm> germplasmList, Map<String, String> parameterMap, List<MeasurementVariable> nonTrialFactors, List<MeasurementVariable> variates, List<TreatmentVariable> treatmentVariables, Map<String, List<String>> treatmentFactorValues);
	List<StandardVariable> getRequiredVariable();
}
