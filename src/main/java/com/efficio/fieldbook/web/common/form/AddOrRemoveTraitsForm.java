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
package com.efficio.fieldbook.web.common.form;

import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.springframework.web.multipart.MultipartFile;

// TODO: Auto-generated Javadoc
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
	
	/** The file. */
	private MultipartFile file;
	
	/** The has error. */
	private String hasError;
	
	/** The import val. */
	private int importVal;
	
	/** The study name. */
	private String studyName;
	
	
	
	/**
	 * Gets the study name.
	 *
	 * @return the study name
	 */
	public String getStudyName() {
		return studyName;
	}

	/**
	 * Sets the study name.
	 *
	 * @param studyName the new study name
	 */
	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

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

	/**
	 * Gets the paginated measurement row list.
	 *
	 * @return the paginated measurement row list
	 */
	public List<MeasurementRow> getPaginatedMeasurementRowList() {
		return paginatedMeasurementRowList;
	}

	/**
	 * Sets the paginated measurement row list.
	 *
	 * @param paginatedMeasurementRowList the new paginated measurement row list
	 */
	public void setPaginatedMeasurementRowList(
			List<MeasurementRow> paginatedMeasurementRowList) {
		this.paginatedMeasurementRowList = paginatedMeasurementRowList;
	}

	/**
	 * Gets the current page.
	 *
	 * @return the current page
	 */
	public int getCurrentPage() {
		return currentPage;
	}

	/**
	 * Sets the current page.
	 *
	 * @param currentPage the new current page
	 */
	public void setCurrentPage(int currentPage) {
		 //assumption is there is an imported germplasm already
		 this.currentPage = 0;
	}
	
	/**
	 * Change page.
	 *
	 * @param currentPage the current page
	 */
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

	/**
	 * Gets the total pages.
	 *
	 * @return the total pages
	 */
	public int getTotalPages() {
		 if(measurementRowList != null && !measurementRowList.isEmpty()){           
	            totalPages = (int) Math.ceil((measurementRowList.size() * 1f) / getResultPerPage()); 
	        }else{
	            totalPages = 0;
	        }
		    return totalPages;
	}

	/**
	 * Sets the total pages.
	 *
	 * @param totalPages the new total pages
	 */
	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	/**
	 * Gets the result per page.
	 *
	 * @return the result per page
	 */
	public int getResultPerPage() {
		return resultPerPage;
	}

	/**
	 * Sets the result per page.
	 *
	 * @param resultPerPage the new result per page
	 */
	public void setResultPerPage(int resultPerPage) {
		this.resultPerPage = resultPerPage;
	}

	/**
	 * Gets the file.
	 *
	 * @return the file
	 */
	public MultipartFile getFile() {
		return file;
	}

	/**
	 * Sets the file.
	 *
	 * @param file the new file
	 */
	public void setFile(MultipartFile file) {
		this.file = file;
	}

	/**
	 * Gets the checks for error.
	 *
	 * @return the checks for error
	 */
	public String getHasError() {
		return hasError;
	}

	/**
	 * Sets the checks for error.
	 *
	 * @param hasError the new checks for error
	 */
	public void setHasError(String hasError) {
		this.hasError = hasError;
	}

	/**
	 * Gets the import val.
	 *
	 * @return the import val
	 */
	public int getImportVal() {
		return importVal;
	}

	/**
	 * Sets the import val.
	 *
	 * @param importVal the new import val
	 */
	public void setImportVal(int importVal) {
		this.importVal = importVal;
	}

     
	
	
}
