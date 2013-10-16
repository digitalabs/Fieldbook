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
package com.efficio.fieldbook.web.nursery.bean;


/**
 * @author Chezka Camille Arevalo
 *
 */
public class NurseryDetails{
    private String name;
    private String title;
    private String objective;
    private String startDate;
    private String endDate;
    private String principalInvestigator;
    private String siteName;
    
    public NurseryDetails(){
        
    }
    
    public NurseryDetails(String name, String title, String objective, 
            String startDate, String endDate, String principalInvestigator, String siteName){
        this.name = name;
        this.title = title;
        this.objective = objective;
        this.startDate = startDate;
        this.endDate = endDate;
        this.principalInvestigator = principalInvestigator;
        this.siteName = siteName;
    }
    
    public String getName(){
        return name;
    }
    
    public void setName(String name){
        this.name = name;
    }
    
    public String getTitle(){
        return title;
    }
    
    public void setTitle(String title){
        this.title= title;
    }
    
    public String getObjective(){
        return objective;
    }
    
    public void setObjective(String objective){
        this.objective = objective;
    }

    public String getStartDate(){
        return startDate;
    }
    
    public void setStartDate(String startDate){
        this.startDate = startDate;
    }
    
    public String getEndDate(){
        return endDate;
    }
    
    public void setEndDate(String endDate){
        this.endDate = endDate;
    }
    
    public String getPrincipalInvestigator(){
        return principalInvestigator;
    }
    
    public void setPrincipalInvestigator(String principalInvestigator){
        this.principalInvestigator = principalInvestigator;
    }
    
    public String getSiteName(){
        return siteName;
    }
    
    public void setSiteName(String siteName){
        this.siteName = siteName;
    }    
}
