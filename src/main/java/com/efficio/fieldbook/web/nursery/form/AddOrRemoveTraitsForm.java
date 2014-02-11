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

import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;

/**
 * The Class AddOrRemoveTraitsForm.
 */
public class AddOrRemoveTraitsForm {
	
	/** The measurement row list. */
	private List<MeasurementRow> measurementRowList;
	
	/** The measurement variables. */
	private List<MeasurementVariable> measurementVariables;
	
	/** The paginated imported germplasm. */
	private List<MeasurementRow> paginatedMeasurementRowList;	
	
	/** The current page. */
	private int currentPage;
		
	/** The total pages. */
	private int totalPages;
	
	/** The result per page. */
	private int resultPerPage = 100;	
	
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

	public List<MeasurementRow> getPaginatedMeasurementRowList() {
		return paginatedMeasurementRowList;
	}

	public void setPaginatedMeasurementRowList(
			List<MeasurementRow> paginatedMeasurementRowList) {
		this.paginatedMeasurementRowList = paginatedMeasurementRowList;
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public void setCurrentPage(int currentPage) {
		 //assumption is there is an imported germplasm already
		 this.currentPage = 0;
	}
	
	public void changePage(int currentPage){
		if(measurementRowList != null && !measurementRowList.isEmpty()){
            int totalItemsPerPage = getResultPerPage();
            int start = (currentPage - 1) * totalItemsPerPage;
            int end = start + totalItemsPerPage;
            if(measurementRowList.size() < end){
                end = measurementRowList.size();
            }
            paginatedMeasurementRowList = measurementRowList.subList(start, end);
            this.currentPage = currentPage;
        }else{
            this.currentPage = 0;
        }
	}

	public int getTotalPages() {
		 if(measurementRowList != null && !measurementRowList.isEmpty()){           
	            totalPages = (int) Math.ceil((measurementRowList.size() * 1f) / getResultPerPage()); 
	        }else{
	            totalPages = 0;
	        }
		    return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public int getResultPerPage() {
		return resultPerPage;
	}

	public void setResultPerPage(int resultPerPage) {
		this.resultPerPage = resultPerPage;
	}

    
	
	
}
