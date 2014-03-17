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

import java.io.Serializable;

import org.generationcp.middleware.domain.dms.Study;

/**
 * This bean models the various input that the user builds up over time
 * to perform the actual loading operation.
 */
public class AdvancingNursery implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
    
    private Study study;
    
    /** The naming convention. */
    private String namingConvention;
    
    /** The suffix convention. */
    private String suffixConvention;
    
    /** The method choice. */
    private String methodChoice;
    
    /** The line choice. */
    private String lineChoice;
    
    /** The line selected. */
    private String lineSelected;
    
    /** The harvest date. */
    private String harvestDate;
    
    /** The harvest location id. */
    private String harvestLocationId;
    
    /** The harvest location abbreviation. */
    private String harvestLocationAbbreviation;
    
    /** The breeding method id. */
    private String breedingMethodId;
    
    private String putBrackets;
    
    public Study getStudy() {
        return study;
    }
    
    public void setStudy(Study study) {
        this.study = study;
    }	   
 
    /**
     * Gets the harvest location abbreviation.
     *
     * @return the harvest location abbreviation
     */
    public String getHarvestLocationAbbreviation() {
            return harvestLocationAbbreviation;
    }
    
    /**
     * Sets the harvest location abbreviation.
     *
     * @param harvestLocationAbbreviation the new harvest location abbreviation
     */
    public void setHarvestLocationAbbreviation(String harvestLocationAbbreviation) {
            this.harvestLocationAbbreviation = harvestLocationAbbreviation;
    }
    
    /**
     * Gets the harvest location id.
     *
     * @return the harvest location id
     */
    public String getHarvestLocationId() {
            return harvestLocationId;
    }
    
    /**
     * Sets the harvest location id.
     *
     * @param harvestLocationId the new harvest location id
     */
    public void setHarvestLocationId(String harvestLocationId) {
            this.harvestLocationId = harvestLocationId;
    }
    
    /**
     * Gets the harvest date.
     *
     * @return the harvest date
     */
    public String getHarvestDate() {
            return harvestDate;
    }
    
    /**
     * Sets the harvest date.
     *
     * @param harvestDate the new harvest date
     */
    public void setHarvestDate(String harvestDate) {
            this.harvestDate = harvestDate;
    }
    
    /**
     * Gets the breeding method id.
     *
     * @return the breeding method id
     */
    public String getBreedingMethodId() {
            return breedingMethodId;
    }

    /**
     * Sets the breeding method id.
     *
     * @param breedingMethodId the new breeding method id
     */
    public void setBreedingMethodId(String breedingMethodId) {
            this.breedingMethodId = breedingMethodId;
    }

    /**
     * Gets the naming convention.
     *
     * @return the naming convention
     */
    public String getNamingConvention() {
            return namingConvention;
    }
    
    /**
     * Sets the naming convention.
     *
     * @param namingConvention the new naming convention
     */
    public void setNamingConvention(String namingConvention) {
            this.namingConvention = namingConvention;
    }
    
    /**
     * Gets the suffix convention.
     *
     * @return the suffix convention
     */
    public String getSuffixConvention() {
            return suffixConvention;
    }
    
    /**
     * Sets the suffix convention.
     *
     * @param suffixConvention the new suffix convention
     */
    public void setSuffixConvention(String suffixConvention) {
            this.suffixConvention = suffixConvention;
    }
    
    /**
     * Gets the method choice.
     *
     * @return the method choice
     */
    public String getMethodChoice() {
            return methodChoice;
    }
    
    /**
     * Sets the method choice.
     *
     * @param methodChoice the new method choice
     */
    public void setMethodChoice(String methodChoice) {
            this.methodChoice = methodChoice;
    }
    
    
    
    /**
     * Gets the line choice.
     *
     * @return the line choice
     */
    public String getLineChoice() {
            return lineChoice;
    }
    
    /**
     * Sets the line choice.
     *
     * @param lineChoice the new line choice
     */
    public void setLineChoice(String lineChoice) {
            this.lineChoice = lineChoice;
    }
    
    /**
     * Gets the line selected.
     *
     * @return the line selected
     */
    public String getLineSelected() {
            return lineSelected;
    }
    
    
    
    public String getPutBrackets() {
		return putBrackets;
	}

	public void setPutBrackets(String putBrackets) {
		this.putBrackets = putBrackets;
	}

	/**
     * Sets the line selected.
     *
     * @param lineSelected the new line selected
     */
    public void setLineSelected(String lineSelected) {
            this.lineSelected = lineSelected;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("AdvancingNursery [study=");
        builder.append(study);
        builder.append(", namingConvention=");
        builder.append(namingConvention);
        builder.append(", suffixConvention=");
        builder.append(suffixConvention);
        builder.append(", methodChoice=");
        builder.append(methodChoice);
        builder.append(", lineChoice=");
        builder.append(lineChoice);
        builder.append(", lineSelected=");
        builder.append(lineSelected);
        builder.append(", harvestDate=");
        builder.append(harvestDate);
        builder.append(", harvestLocationId=");
        builder.append(harvestLocationId);
        builder.append(", harvestLocationAbbreviation=");
        builder.append(harvestLocationAbbreviation);
        builder.append(", breedingMethodId=");
        builder.append(breedingMethodId);
        builder.append("]");
        return builder.toString();
    }
}
