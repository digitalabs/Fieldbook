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
package com.efficio.fieldbook.web.trial.form;

import java.util.List;

import org.generationcp.middleware.domain.etl.StudyDetails;

/**
 * The Class ManageNurseriesForm.
 */
public class ManageTrialForm {
    
    /** The trial details list. */
    private List<StudyDetails> trialDetailsList;
    
    /** For pagination. The paginated trial details list. */
    private List<StudyDetails> paginatedTrialDetailsList;     
    
    /** The current page. */
    private int currentPage;
    
    /** The total pages. */
    private int totalPages;
    
    /** The result per page. */
    private int resultPerPage = 10;                 
    
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
     * Gets the total pages.
     *
     * @return the total pages
     */
    public int getTotalPages(){
        if(trialDetailsList != null && !trialDetailsList.isEmpty()){           
            totalPages = (int) Math.ceil((trialDetailsList.size() * 1f) / getResultPerPage()); 
        }else{
            totalPages = 0;
        }
        return totalPages;
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
        
        //assumption is there are nursery list already
        if(trialDetailsList != null && !trialDetailsList.isEmpty()){
            int totalItemsPerPage = getResultPerPage();
            int start = (currentPage - 1) * totalItemsPerPage;
            int end = start + totalItemsPerPage;
            if(trialDetailsList.size() < end){
                end = trialDetailsList.size();
            }
            this.paginatedTrialDetailsList = trialDetailsList.subList(start, end);
            this.currentPage = currentPage;
        }else{
            this.currentPage = 0;
        }
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
     * Gets the trial details list.
     *
     * @return the trial details list
     */
    public List<StudyDetails> getTrialDetailsList() {
        return trialDetailsList;
    }
    
    /**
     * Sets the trial details list.
     *
     * @param trialDetailsList the new trial details list
     */
    public void setTrialDetailsList(List<StudyDetails> trialDetailsList) {
        this.trialDetailsList = trialDetailsList;
    }

    /**
     * Gets the paginated trial details list.
     *
     * @return the paginated trial details list
     */
    public List<StudyDetails> getPaginatedTrialDetailsList() {
        return paginatedTrialDetailsList;
    }

    /**
     * Sets the paginated trial details list.
     *
     * @param paginatedTrialDetailsList the new paginated trial details list
     */
    public void setPaginatedTrialDetailsList(
            List<StudyDetails> paginatedTrialDetailsList) {
        this.paginatedTrialDetailsList = paginatedTrialDetailsList;
    }
    
}
