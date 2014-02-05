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

/**
 * The Class AdvancingNurseryForm.
 */
public class AdvancingNurseryForm {
	private String namingConvention;
	private String suffixConvention;
	
	private String methodChoice;
	private String methodSelected;
	
	private String lineChoice;
	private String lineSelected;
	
	private List<Method> breedingMethods;
	
	private String harvestDate;
	private String harvestLocation;
	
	 /** The field location id all. */
    private String harvestLocationIdAll;
    
    /** The field location id favorite. */
    private String harvestLocationIdFavorite;
    private String harvestLocationId;
    private String harvestLocationName;
    
    private String defaultMethodId;
    
    
	
	
	public String getDefaultMethodId() {
		return defaultMethodId;
	}
	public void setDefaultMethodId(String defaultMethodId) {
		this.defaultMethodId = defaultMethodId;
	}
	public String getHarvestLocationIdAll() {
		return harvestLocationIdAll;
	}
	public void setHarvestLocationIdAll(String harvestLocationIdAll) {
		this.harvestLocationIdAll = harvestLocationIdAll;
	}
	public String getHarvestLocationIdFavorite() {
		return harvestLocationIdFavorite;
	}
	public void setHarvestLocationIdFavorite(String harvestLocationIdFavorite) {
		this.harvestLocationIdFavorite = harvestLocationIdFavorite;
	}
	public String getHarvestLocationId() {
		return harvestLocationId;
	}
	public void setHarvestLocationId(String harvestLocationId) {
		this.harvestLocationId = harvestLocationId;
	}
	public String getHarvestLocationName() {
		return harvestLocationName;
	}
	public void setHarvestLocationName(String harvestLocationName) {
		this.harvestLocationName = harvestLocationName;
	}
	public String getHarvestDate() {
		return harvestDate;
	}
	public void setHarvestDate(String harvestDate) {
		this.harvestDate = harvestDate;
	}
	public String getHarvestLocation() {
		return harvestLocation;
	}
	public void setHarvestLocation(String harvestLocation) {
		this.harvestLocation = harvestLocation;
	}
	public List<Method> getBreedingMethods() {
		return breedingMethods;
	}
	public void setBreedingMethods(List<Method> breedingMethods) {
		this.breedingMethods = breedingMethods;
	}
	public String getNamingConvention() {
		return namingConvention;
	}
	public void setNamingConvention(String namingConvention) {
		this.namingConvention = namingConvention;
	}
	public String getSuffixConvention() {
		return suffixConvention;
	}
	public void setSuffixConvention(String suffixConvention) {
		this.suffixConvention = suffixConvention;
	}
	public String getMethodChoice() {
		return methodChoice;
	}
	public void setMethodChoice(String methodChoice) {
		this.methodChoice = methodChoice;
	}
	public String getMethodSelected() {
		return methodSelected;
	}
	public void setMethodSelected(String methodSelected) {
		this.methodSelected = methodSelected;
	}
	public String getLineChoice() {
		return lineChoice;
	}
	public void setLineChoice(String lineChoice) {
		this.lineChoice = lineChoice;
	}
	public String getLineSelected() {
		return lineSelected;
	}
	public void setLineSelected(String lineSelected) {
		this.lineSelected = lineSelected;
	}
	
	
	
	
}
