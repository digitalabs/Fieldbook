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
package com.efficio.fieldbook.web.nursery.service;

import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;

import com.efficio.fieldbook.web.nursery.bean.UserSelection;

// TODO: Auto-generated Javadoc
/**
 * The Interface MeasurementsGeneratorService.
 */
public interface MeasurementsGeneratorService {

	/**
	 * Generate measurement rows.
	 *
	 * @param userSelection the user selection
	 * @return the list
	 */
	List<MeasurementRow> generateMeasurementRows(UserSelection userSelection);
	
	/**
	 * Generate real measurement rows.
	 *
	 * @param userSelection the user selection
	 * @return the list
	 */
	List<MeasurementRow> generateRealMeasurementRows(UserSelection userSelection) throws MiddlewareQueryException;
	
}
