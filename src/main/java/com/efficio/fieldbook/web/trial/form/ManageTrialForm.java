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
    private List<StudyDetails> trialDetailsList;
    
    //for pagination
    private List<StudyDetails> paginatedTrialDetailsList;     
    private int currentPage;
    private int totalPages;
    private int resultPerPage = 10;                 
    
    public int getResultPerPage() {
        return resultPerPage;
    }
    
    
    public void setResultPerPage(int resultPerPage) {
        this.resultPerPage = resultPerPage;
    }
    
    public int getTotalPages(){
        if(trialDetailsList != null && !trialDetailsList.isEmpty()){           
            totalPages = (int) Math.ceil((trialDetailsList.size() * 1f) / getResultPerPage()); 
        }else{
            totalPages = 0;
        }
        return totalPages;
    }
        
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
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }


    
    public List<StudyDetails> getTrialDetailsList() {
        return trialDetailsList;
    }


    
    public void setTrialDetailsList(List<StudyDetails> trialDetailsList) {
        this.trialDetailsList = trialDetailsList;
    }


    
    public List<StudyDetails> getPaginatedTrialDetailsList() {
        return paginatedTrialDetailsList;
    }


    
    public void setPaginatedTrialDetailsList(
            List<StudyDetails> paginatedTrialDetailsList) {
        this.paginatedTrialDetailsList = paginatedTrialDetailsList;
    }
    
    
}
