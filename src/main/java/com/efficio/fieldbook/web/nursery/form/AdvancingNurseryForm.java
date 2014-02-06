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
import org.generationcp.middleware.pojos.Method;

// TODO: Auto-generated Javadoc
/**
 * The Class AdvancingNurseryForm.
 */
public class AdvancingNurseryForm {
	
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
	
	/** The harvest location. */
	private String harvestLocation;
	
	 /** The field location id all. */
    private String harvestLocationIdAll;
    
    /** The field location id favorite. */
    private String harvestLocationIdFavorite;
    
    /** The harvest location id. */
    private String harvestLocationId;
    
    /** The harvest location name. */
    private String harvestLocationName;
    
    /** The harvest location abbreviation. */
    private String harvestLocationAbbreviation;
    
    /** The default method id. */
    private String defaultMethodId;
       
    /** The breeding method id. */
    private String breedingMethodId;
    
    /** The field location id all. */
    private String methodIdAll;
    
    /** The field location id favorite. */
    private String methodIdFavorite;
    
    /** The project id. */
    private String projectId;
    
    
	
	/**
	 * Gets the method id all.
	 *
	 * @return the method id all
	 */
	public String getMethodIdAll() {
		return methodIdAll;
	}

	/**
	 * Sets the method id all.
	 *
	 * @param methodIdAll the new method id all
	 */
	public void setMethodIdAll(String methodIdAll) {
		this.methodIdAll = methodIdAll;
	}

	/**
	 * Gets the method id favorite.
	 *
	 * @return the method id favorite
	 */
	public String getMethodIdFavorite() {
		return methodIdFavorite;
	}

	/**
	 * Sets the method id favorite.
	 *
	 * @param methodIdFavorite the new method id favorite
	 */
	public void setMethodIdFavorite(String methodIdFavorite) {
		this.methodIdFavorite = methodIdFavorite;
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
	 * Gets the default method id.
	 *
	 * @return the default method id
	 */
	public String getDefaultMethodId() {
		return defaultMethodId;
	}
	
	/**
	 * Sets the default method id.
	 *
	 * @param defaultMethodId the new default method id
	 */
	public void setDefaultMethodId(String defaultMethodId) {
		this.defaultMethodId = defaultMethodId;
	}
	
	/**
	 * Gets the harvest location id all.
	 *
	 * @return the harvest location id all
	 */
	public String getHarvestLocationIdAll() {
		return harvestLocationIdAll;
	}
	
	/**
	 * Sets the harvest location id all.
	 *
	 * @param harvestLocationIdAll the new harvest location id all
	 */
	public void setHarvestLocationIdAll(String harvestLocationIdAll) {
		this.harvestLocationIdAll = harvestLocationIdAll;
	}
	
	/**
	 * Gets the harvest location id favorite.
	 *
	 * @return the harvest location id favorite
	 */
	public String getHarvestLocationIdFavorite() {
		return harvestLocationIdFavorite;
	}
	
	/**
	 * Sets the harvest location id favorite.
	 *
	 * @param harvestLocationIdFavorite the new harvest location id favorite
	 */
	public void setHarvestLocationIdFavorite(String harvestLocationIdFavorite) {
		this.harvestLocationIdFavorite = harvestLocationIdFavorite;
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
	 * Gets the harvest location name.
	 *
	 * @return the harvest location name
	 */
	public String getHarvestLocationName() {
		return harvestLocationName;
	}
	
	/**
	 * Sets the harvest location name.
	 *
	 * @param harvestLocationName the new harvest location name
	 */
	public void setHarvestLocationName(String harvestLocationName) {
		this.harvestLocationName = harvestLocationName;
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
	 * Gets the harvest location.
	 *
	 * @return the harvest location
	 */
	public String getHarvestLocation() {
		return harvestLocation;
	}
	
	/**
	 * Sets the harvest location.
	 *
	 * @param harvestLocation the new harvest location
	 */
	public void setHarvestLocation(String harvestLocation) {
		this.harvestLocation = harvestLocation;
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
	
	/**
	 * Sets the line selected.
	 *
	 * @param lineSelected the new line selected
	 */
	public void setLineSelected(String lineSelected) {
		this.lineSelected = lineSelected;
	}
	
	/**
	 * Gets the project id.
	 *
	 * @return the project id
	 */
	public String getProjectId() {
            return projectId;
        }
	
	/**
	 * Sets the project id.
	 *
	 * @param projectId the new project id
	 */
	public void setProjectId(String projectId) {
            this.projectId = projectId;
        }
}
