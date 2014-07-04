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

import java.util.List;

import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;

/**
 * 
 * The POJO containing information needed for Advancing.
 *
 */
public class AdvancingSource {

    private ImportedGermplasm germplasm;
    private List<Name> names;
    private Integer plantsSelected;
    private Method breedingMethod;
    private boolean isCheck;
    private boolean isBulk;
    private String nurseryName;
    private String season;
    private String locationAbbreviation;
    private String rootName;
    private Method sourceMethod;

    public AdvancingSource(ImportedGermplasm germplasm, List<Name> names,
			Integer plantsSelected, Method breedingMethod, boolean isCheck,
			String nurseryName, String season, String locationAbbreviation) {
		super();
		this.germplasm = germplasm;
		this.names = names;
		this.plantsSelected = plantsSelected;
		this.breedingMethod = breedingMethod;
		this.isCheck = isCheck;
		this.nurseryName = nurseryName;
		this.season = season;
		this.locationAbbreviation = locationAbbreviation;
	}

	/**
     * @return the germplasm
     */
    public ImportedGermplasm getGermplasm() {
        return germplasm;
    }
    
    /**
     * @param germplasm the germplasm to set
     */
    public void setGermplasm(ImportedGermplasm germplasm) {
        this.germplasm = germplasm;
    }
    
    /**
     * @return the plantsSelected
     */
    public Integer getPlantsSelected() {
        return plantsSelected;
    }
    
    /**
     * @param plantsSelected the plantsSelected to set
     */
    public void setPlantsSelected(Integer plantsSelected) {
        this.plantsSelected = plantsSelected;
    }
    
    /**
     * @return the isCheck
     */
    public boolean isCheck() {
        return isCheck;
    }
    
    /**
     * @param isCheck the isCheck to set
     */
    public void setCheck(boolean isCheck) {
        this.isCheck = isCheck;
    }

	/**
	 * @return the isBulk
	 */
	public boolean isBulk() {
		return getBreedingMethod() != null ? getBreedingMethod().isBulkingMethod() : false;
	}

	/**
	 * @param isBulk the isBulk to set
	 */
	public void setBulk(boolean isBulk) {
		this.isBulk = isBulk;
	}

	/**
	 * @return the names
	 */
	public List<Name> getNames() {
		return names;
	}

	/**
	 * @param names the names to set
	 */
	public void setNames(List<Name> names) {
		this.names = names;
	}

	/**
	 * @return the breedingMethod
	 */
	public Method getBreedingMethod() {
		return breedingMethod;
	}

	/**
	 * @param breedingMethod the breedingMethod to set
	 */
	public void setBreedingMethod(Method breedingMethod) {
		this.breedingMethod = breedingMethod;
	}

	/**
	 * @return the nurseryName
	 */
	public String getNurseryName() {
		return nurseryName;
	}

	/**
	 * @param nurseryName the nurseryName to set
	 */
	public void setNurseryName(String nurseryName) {
		this.nurseryName = nurseryName;
	}

	/**
	 * @return the season
	 */
	public String getSeason() {
		return season;
	}

	/**
	 * @param season the season to set
	 */
	public void setSeason(String season) {
		this.season = season;
	}

	/**
	 * @return the locationAbbreviation
	 */
	public String getLocationAbbreviation() {
		return locationAbbreviation;
	}

	/**
	 * @param locationAbbreviation the locationAbbreviation to set
	 */
	public void setLocationAbbreviation(String locationAbbreviation) {
		this.locationAbbreviation = locationAbbreviation;
	}

	/**
	 * @return the rootName
	 */
	public String getRootName() {
		return rootName;
	}

	/**
	 * @param rootName the rootName to set
	 */
	public void setRootName(String rootName) {
		this.rootName = rootName;
	}

	/**
	 * @return the sourceMethod
	 */
	public Method getSourceMethod() {
		return sourceMethod;
	}

	/**
	 * @param sourceMethod the sourceMethod to set
	 */
	public void setSourceMethod(Method sourceMethod) {
		this.sourceMethod = sourceMethod;
	}
    
    
}
