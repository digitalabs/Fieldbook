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
package com.efficio.fieldbook.web.common.bean;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.etl.ExperimentalDesignVariable;

/**
 * The Class StudyDetails.
 *
 * @author Chezka Camille Arevalo
 */
public class StudyDetails{
    
	private Integer id;
	
    /** The name. */
    private String name;
    
    /** The title. */
    private String title;
    
    /** The objective. */
    private String objective;
    
    /** The start date. */
    private String startDate;
    
    /** The end date. */
    private String endDate;
    
    /** The principal investigator. */
    private String principalInvestigator;
    
    /** The site name. */
    private String siteName;
    
    private List<SettingDetail> basicStudyDetails;
    
    private List<SettingDetail> managementDetails;
    
    private List<SettingDetail> nurseryConditionDetails;
    
    private List<SettingDetail> factorDetails;
    
    private List<SettingDetail> variateDetails;
    
    private List<SettingDetail> selectionVariateDetails;
    
    private List<SettingDetail> environmentManagementDetails;
    
    private List<SettingDetail> germplasmDescriptors;
    
    private List<TreatmentFactorDetail> treatmentFactorDetails;
    
    private boolean hasMeasurements;
    
    private int numberOfEnvironments;
    
    private ExperimentalDesignVariable experimentalDesignDetails;
    
    private Map<Integer, SettingDetail> factorsMap;

    /**
     * Instantiates a new study details.
     */
    public StudyDetails(){
        
    }
    
    /**
     * Instantiates a new study details.
     *
     * @param name the name
     * @param title the title
     * @param objective the objective
     * @param startDate the start date
     * @param endDate the end date
     * @param principalInvestigator the principal investigator
     * @param siteName the site name
     */
    public StudyDetails(String name, String title, String objective, 
            String startDate, String endDate, String principalInvestigator, String siteName){
        this.name = name;
        this.title = title;
        this.objective = objective;
        this.startDate = startDate;
        this.endDate = endDate;
        this.principalInvestigator = principalInvestigator;
        this.siteName = siteName;
    }
    
    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName(){
        return name;
    }
    
    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name){
        this.name = name;
    }
    
    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle(){
        return title;
    }
    
    /**
     * Sets the title.
     *
     * @param title the new title
     */
    public void setTitle(String title){
        this.title= title;
    }
    
    /**
     * Gets the objective.
     *
     * @return the objective
     */
    public String getObjective(){
        return objective;
    }
    
    /**
     * Sets the objective.
     *
     * @param objective the new objective
     */
    public void setObjective(String objective){
        this.objective = objective;
    }

    /**
     * Gets the start date.
     *
     * @return the start date
     */
    public String getStartDate(){
        return startDate;
    }
    
    /**
     * Sets the start date.
     *
     * @param startDate the new start date
     */
    public void setStartDate(String startDate){
        this.startDate = startDate;
    }
    
    /**
     * Gets the end date.
     *
     * @return the end date
     */
    public String getEndDate(){
        return endDate;
    }
    
    /**
     * Sets the end date.
     *
     * @param endDate the new end date
     */
    public void setEndDate(String endDate){
        this.endDate = endDate;
    }
    
    /**
     * Gets the principal investigator.
     *
     * @return the principal investigator
     */
    public String getPrincipalInvestigator(){
        return principalInvestigator;
    }
    
    /**
     * Sets the principal investigator.
     *
     * @param principalInvestigator the new principal investigator
     */
    public void setPrincipalInvestigator(String principalInvestigator){
        this.principalInvestigator = principalInvestigator;
    }
    
    /**
     * Gets the site name.
     *
     * @return the site name
     */
    public String getSiteName(){
        return siteName;
    }
    
    /**
     * Sets the site name.
     *
     * @param siteName the new site name
     */
    public void setSiteName(String siteName){
        this.siteName = siteName;
    }

	/**
	 * @return the basicStudyDetails
	 */
	public List<SettingDetail> getBasicStudyDetails() {
		return basicStudyDetails;
	}

	/**
	 * @param basicStudyDetails the basicStudyDetails to set
	 */
	public void setBasicStudyDetails(List<SettingDetail> basicStudyDetails) {
		this.basicStudyDetails = basicStudyDetails;
	}

	/**
	 * @return the managementDetails
	 */
	public List<SettingDetail> getManagementDetails() {
		return managementDetails;
	}

	/**
	 * @param managementDetails the managementDetails to set
	 */
	public void setManagementDetails(List<SettingDetail> managementDetails) {
		this.managementDetails = managementDetails;
	}

	/**
	 * @return the factorDetails
	 */
	public List<SettingDetail> getFactorDetails() {
		return factorDetails;
	}

	/**
	 * @param factorDetails the factorDetails to set
	 */
	public void setFactorDetails(List<SettingDetail> factorDetails) {
		this.factorDetails = factorDetails;
	}

	/**
	 * @return the variateDetails
	 */
	public List<SettingDetail> getVariateDetails() {
		return variateDetails;
	}

	/**
	 * @param variateDetails the variateDetails to set
	 */
	public void setVariateDetails(List<SettingDetail> variateDetails) {
		this.variateDetails = variateDetails;
	}

	/**
	 * @return the id
	 */
	public Integer getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the nurseryConditionDetails
	 */
	public List<SettingDetail> getNurseryConditionDetails() {
		return nurseryConditionDetails;
	}

	/**
	 * @param nurseryConditionDetails the nurseryConditionDetails to set
	 */
	public void setNurseryConditionDetails(
			List<SettingDetail> nurseryConditionDetails) {
		this.nurseryConditionDetails = nurseryConditionDetails;
	}

	/**
	 * @return the selectionVariateDetails
	 */
	public List<SettingDetail> getSelectionVariateDetails() {
		return selectionVariateDetails;
	}

	/**
	 * @param selectionVariateDetails the selectionVariateDetails to set
	 */
	public void setSelectionVariateDetails(
			List<SettingDetail> selectionVariateDetails) {
		this.selectionVariateDetails = selectionVariateDetails;
	}

	/**
	 * @return the hasMeasurements
	 */
	public boolean isHasMeasurements() {
		return hasMeasurements;
	}

	/**
	 * @param hasMeasurements the hasMeasurements to set
	 */
	public void setHasMeasurements(boolean hasMeasurements) {
		this.hasMeasurements = hasMeasurements;
	}

	/**
	 * @return the environmentManagementDetails
	 */
	public List<SettingDetail> getEnvironmentManagementDetails() {
		return environmentManagementDetails;
	}

	/**
	 * @param environmentManagementDetails the environmentManagementDetails to set
	 */
	public void setEnvironmentManagementDetails(
			List<SettingDetail> environmentManagementDetails) {
		this.environmentManagementDetails = environmentManagementDetails;
	}

	/**
	 * @return the germplasmDescriptors
	 */
	public List<SettingDetail> getGermplasmDescriptors() {
		return germplasmDescriptors;
	}

	/**
	 * @param germplasmDescriptors the germplasmDescriptors to set
	 */
	public void setGermplasmDescriptors(List<SettingDetail> germplasmDescriptors) {
		this.germplasmDescriptors = germplasmDescriptors;
	}

	/**
	 * @return the treatmentFactorDetails
	 */
	public List<TreatmentFactorDetail> getTreatmentFactorDetails() {
		return treatmentFactorDetails;
	}

	/**
	 * @param treatmentFactorDetails the treatmentFactorDetails to set
	 */
	public void setTreatmentFactorDetails(
			List<TreatmentFactorDetail> treatmentFactorDetails) {
		this.treatmentFactorDetails = treatmentFactorDetails;
	}

	/**
	 * @return the numberOfEnvironments
	 */
	public int getNumberOfEnvironments() {
		return numberOfEnvironments;
	}

	/**
	 * @param numberOfEnvironments the numberOfEnvironments to set
	 */
	public void setNumberOfEnvironments(int numberOfEnvironments) {
		this.numberOfEnvironments = numberOfEnvironments;
	}

	/**
	 * @return the experimentalDesignDetails
	 */
	public ExperimentalDesignVariable getExperimentalDesignDetails() {
		return experimentalDesignDetails;
	}

	/**
	 * @param experimentalDesignDetails the experimentalDesignDetails to set
	 */
	public void setExperimentalDesignDetails(
			ExperimentalDesignVariable experimentalDesignDetails) {
		this.experimentalDesignDetails = experimentalDesignDetails;
	}
	
	public Map<Integer, SettingDetail> getFactorsMap() {
		if (this.factorsMap == null) {
			this.factorsMap = new HashMap<Integer, SettingDetail>();
			if (factorDetails != null) {
				for (SettingDetail factorDetail : factorDetails) {
					if (factorDetail.getVariable() != null) {
						this.factorsMap.put(factorDetail.getVariable().getCvTermId(), factorDetail);
					}
				}
			}
			if (germplasmDescriptors != null) {
				for (SettingDetail factorDetail : germplasmDescriptors) {
					if (factorDetail.getVariable() != null) {
						this.factorsMap.put(factorDetail.getVariable().getCvTermId(), factorDetail);
					}
				}
			}
		}
		return this.factorsMap;
	}

}
