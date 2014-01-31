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
package com.efficio.fieldbook.web.nursery.form;

import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;

// TODO: Auto-generated Javadoc
/**
 * The Class AddOrRemoveTraitsForm.
 */
public class AddOrRemoveTraitsForm {
	
	/** The measurement row list. */
	private List<MeasurementRow> measurementRowList;
	
	/** The measurement variables. */
	private List<MeasurementVariable> measurementVariables;
	
	private boolean isUpdated;

	/**
	 * Gets the measurement row list.
	 *
	 * @return the measurement row list
	 */
	public List<MeasurementRow> getMeasurementRowList() {
		return measurementRowList;
	}

	/**
	 * Sets the measurement row list.
	 *
	 * @param measurementRowList the new measurement row list
	 */
	public void setMeasurementRowList(List<MeasurementRow> measurementRowList) {
		this.measurementRowList = measurementRowList;
	}

	/**
	 * Gets the measurement variables.
	 *
	 * @return the measurement variables
	 */
	public List<MeasurementVariable> getMeasurementVariables() {
		return measurementVariables;
	}

	/**
	 * Sets the measurement variables.
	 *
	 * @param measurementVariables the new measurement variables
	 */
	public void setMeasurementVariables(
			List<MeasurementVariable> measurementVariables) {
		this.measurementVariables = measurementVariables;
	}

    
    public boolean getIsUpdated() {
        return isUpdated;
    }

    
    public void setIsUpdated(boolean isUpdated) {
        this.isUpdated = isUpdated;
    }
	
	
	
}
