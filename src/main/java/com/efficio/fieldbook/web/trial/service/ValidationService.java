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

package com.efficio.fieldbook.web.trial.service;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.exceptions.WorkbookParserException;

public interface ValidationService {

	boolean isValidValue(MeasurementVariable var, String value, boolean validateDateForDB);

	void validateObservationValues(Workbook workbook) throws WorkbookParserException;

	String validateConditionAndConstantValues(Workbook workbook) throws MiddlewareQueryException;

	void validateObservationValues(Workbook workbook, MeasurementRow row) throws MiddlewareQueryException;

	boolean validateObservationValue(final Variable variable, String value);
}
