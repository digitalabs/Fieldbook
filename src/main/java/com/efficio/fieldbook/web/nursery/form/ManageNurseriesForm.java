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

import org.generationcp.middleware.domain.etl.StudyDetails;

/**
 * The Class ManageNurseriesForm.
 */
public class ManageNurseriesForm {
    private List<StudyDetails> nurseryDetailsList;
    
    //for pagination
    private List<StudyDetails> paginatedNurseryDetailsList;     
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
        if(nurseryDetailsList != null && !nurseryDetailsList.isEmpty()){           
            totalPages = (int) Math.ceil((nurseryDetailsList.size() * 1f) / getResultPerPage()); 
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
        if(nurseryDetailsList != null && !nurseryDetailsList.isEmpty()){
            int totalItemsPerPage = getResultPerPage();
            int start = (currentPage - 1) * totalItemsPerPage;
            int end = start + totalItemsPerPage;
            if(nurseryDetailsList.size() < end){
                end = nurseryDetailsList.size();
            }
            this.paginatedNurseryDetailsList = nurseryDetailsList.subList(start, end);
            this.currentPage = currentPage;
        }else{
            this.currentPage = 0;
        }
    }
    
    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }
    
    public List<StudyDetails> getPaginatedNurseryDetailsList() {
        return paginatedNurseryDetailsList;
    }
    
    public void setPaginatedNurseryDetailsList(List<StudyDetails> paginatedNurseryDetailsList) {
        this.paginatedNurseryDetailsList = paginatedNurseryDetailsList;
    }
    //end of pagination code

    public List<StudyDetails> getNurseryDetailsList() {        
        return nurseryDetailsList;
    }
         
    public void setNurseryDetailsList(List<StudyDetails> nurseryDetailsList){
        this.nurseryDetailsList = nurseryDetailsList;
    }
}
