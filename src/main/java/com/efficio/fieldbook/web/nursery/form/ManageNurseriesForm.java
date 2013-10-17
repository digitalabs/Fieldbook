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
    /*
    private List<NurseryDetails> createDummyData(){
        List<NurseryDetails> tempnurseryDetailsList = new ArrayList<NurseryDetails>();
        
        tempnurseryDetailsList.add(new NurseryDetails("testName1", "testTitle1", "testObjective1", 
                "testStartDate1", "testEndDate1", "testPrincipalInvestigator1", "testSiteName1"));
        tempnurseryDetailsList.add(new NurseryDetails("testName2", "testTitle2", "testObjective2", 
                "testStartDate2", "testEndDate2", "testPrincipalInvestigator2", "testSiteName2"));
        tempnurseryDetailsList.add(new NurseryDetails("testName3", "testTitle3", "testObjective3", 
                "testStartDate3", "testEndDate3", "testPrincipalInvestigator3", "testSiteName3"));
        tempnurseryDetailsList.add(new NurseryDetails("testName4", "testTitle4", "testObjective4", 
                "testStartDate4", "testEndDate4", "testPrincipalInvestigator4", "testSiteName4"));
        tempnurseryDetailsList.add(new NurseryDetails("testName5", "testTitle5", "testObjective5", 
                "testStartDate5", "testEndDate5", "testPrincipalInvestigator5", "testSiteName5"));
        tempnurseryDetailsList.add(new NurseryDetails("testName6", "testTitle6", "testObjective6", 
                "testStartDate6", "testEndDate6", "testPrincipalInvestigator6", "testSiteName6"));
        tempnurseryDetailsList.add(new NurseryDetails("testName7", "testTitle7", "testObjective7", 
                "testStartDate7", "testEndDate7", "testPrincipalInvestigator7", "testSiteName7"));
        tempnurseryDetailsList.add(new NurseryDetails("testName8", "testTitle8", "testObjective8", 
                "testStartDate8", "testEndDate8", "testPrincipalInvestigator8", "testSiteName8"));
        tempnurseryDetailsList.add(new NurseryDetails("testName9", "testTitle9", "testObjective9", 
                "testStartDate9", "testEndDate9", "testPrincipalInvestigator9", "testSiteName9"));
        tempnurseryDetailsList.add(new NurseryDetails("testName10", "testTitle10", "testObjective10", 
                "testStartDate10", "testEndDate10", "testPrincipalInvestigator10", "testSiteName10"));
        tempnurseryDetailsList.add(new NurseryDetails("testName11", "testTitle11", "testObjective11", 
                "testStartDate11", "testEndDate11", "testPrincipalInvestigator11", "testSiteName11"));
        tempnurseryDetailsList.add(new NurseryDetails("testName12", "testTitle12", "testObjective12", 
                "testStartDate12", "testEndDate12", "testPrincipalInvestigator12", "testSiteName12"));
        
        return tempnurseryDetailsList;
    }
    */
}
