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

import com.efficio.fieldbook.web.common.bean.SettingDetail;


/**
 * The Class CreateNurseryForm.
 */
public class CreateNurseryForm {

	/** The project id. */
	private String projectId;
	
	/** The selected setting id. */
	private int selectedSettingId;

	/** The nursery level variables. */
	private List<SettingDetail> studyLevelVariables;
	
	/** The plot level variables. */
	private List<SettingDetail> plotLevelVariables;
	
	/** The baseline trait variables. */
	private List<SettingDetail> baselineTraitVariables;

    /** The folder id. */
	private Integer folderId;
    
    /** The folder name. */
    private String folderName;
    
    /** The field layout random. */
    private boolean fieldLayoutRandom = true;

    /** The required fields. */
    private String requiredFields;
    
    /** The location id. */
    private String locationId;
    
    /** The breeding method id. */
    private String breedingMethodId;
    
    /** The location url. */
    private String locationUrl;
    
    /** The breeding method url. */
    private String breedingMethodUrl;
    
    /** The import location url. */
    private String importLocationUrl;
    private String openGermplasmUrl;
    
    /** The load settings. */
    private String loadSettings;
    
    private String studyNameTermId;
    
    private String startDateId;
    private String endDateId;

    /**
     * Gets the project id.
     *
     * @return the projectId
     */
	public String getProjectId() {
		return projectId;
	}

	/**
	 * Sets the project id.
	 *
	 * @param projectId the projectId to set
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	/**
	 * Gets the selected setting id.
	 *
	 * @return the selectedSettingId
	 */
	public int getSelectedSettingId() {
		return selectedSettingId;
	}

	/**
	 * Sets the selected setting id.
	 *
	 * @param selectedSettingId the selectedSettingId to set
	 */
	public void setSelectedSettingId(int selectedSettingId) {
		this.selectedSettingId = selectedSettingId;
	}

	

	public List<SettingDetail> getStudyLevelVariables() {
		return studyLevelVariables;
	}

	public void setStudyLevelVariables(List<SettingDetail> studyLevelVariables) {
		this.studyLevelVariables = studyLevelVariables;
	}

	/**
	 * Gets the plot level variables.
	 *
	 * @return the plotLevelVariables
	 */
	public List<SettingDetail> getPlotLevelVariables() {
		return plotLevelVariables;
	}

	/**
	 * Sets the plot level variables.
	 *
	 * @param plotLevelVariables the plotLevelVariables to set
	 */
	public void setPlotLevelVariables(List<SettingDetail> plotLevelVariables) {
		this.plotLevelVariables = plotLevelVariables;
	}

	/**
	 * Gets the baseline trait variables.
	 *
	 * @return the baselineTraitVariables
	 */
	public List<SettingDetail> getBaselineTraitVariables() {
		return baselineTraitVariables;
	}

	/**
	 * Sets the baseline trait variables.
	 *
	 * @param baselineTraitVariables the baselineTraitVariables to set
	 */
	public void setBaselineTraitVariables(List<SettingDetail> baselineTraitVariables) {
		this.baselineTraitVariables = baselineTraitVariables;
	}

	/**
	 * Gets the folder id.
	 *
	 * @return the folderId
	 */
	public Integer getFolderId() {
		return folderId;
	}

	/**
	 * Sets the folder id.
	 *
	 * @param folderId the folderId to set
	 */
	public void setFolderId(Integer folderId) {
		this.folderId = folderId;
	}

	/**
	 * Gets the folder name.
	 *
	 * @return the folderName
	 */
	public String getFolderName() {
		return folderName;
	}

	/**
	 * Sets the folder name.
	 *
	 * @param folderName the folderName to set
	 */
	public void setFolderName(String folderName) {
		this.folderName = folderName;
	}

	/**
	 * Checks if is field layout random.
	 *
	 * @return the fieldLayoutRandom
	 */
	public boolean isFieldLayoutRandom() {
		return fieldLayoutRandom;
	}

	/**
	 * Sets the field layout random.
	 *
	 * @param fieldLayoutRandom the fieldLayoutRandom to set
	 */
	public void setFieldLayoutRandom(boolean fieldLayoutRandom) {
		this.fieldLayoutRandom = fieldLayoutRandom;
	}

	/**
	 * Gets the required fields.
	 *
	 * @return the required fields
	 */
	public String getRequiredFields() {
		return requiredFields;
	}

	/**
	 * Sets the required fields.
	 *
	 * @param requiredFields the new required fields
	 */
	public void setRequiredFields(String requiredFields) {
		this.requiredFields = requiredFields;
	}

	/**
	 * Gets the location id.
	 *
	 * @return the location id
	 */
	public String getLocationId() {
		return locationId;
	}

	/**
	 * Sets the location id.
	 *
	 * @param locationId the new location id
	 */
	public void setLocationId(String locationId) {
		this.locationId = locationId;
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
	 * Gets the location url.
	 *
	 * @return the location url
	 */
	public String getLocationUrl() {
		return locationUrl;
	}

	/**
	 * Sets the location url.
	 *
	 * @param locationUrl the new location url
	 */
	public void setLocationUrl(String locationUrl) {
		this.locationUrl = locationUrl;
	}

	/**
	 * Gets the breeding method url.
	 *
	 * @return the breeding method url
	 */
	public String getBreedingMethodUrl() {
		return breedingMethodUrl;
	}

	/**
	 * Sets the breeding method url.
	 *
	 * @param breedingMethodUrl the new breeding method url
	 */
	public void setBreedingMethodUrl(String breedingMethodUrl) {
		this.breedingMethodUrl = breedingMethodUrl;
	}

	/**
	 * Gets the import location url.
	 *
	 * @return the import location url
	 */
	public String getImportLocationUrl() {
		return importLocationUrl;
	}

	/**
	 * Sets the import location url.
	 *
	 * @param importLocationUrl the new import location url
	 */
	public void setImportLocationUrl(String importLocationUrl) {
		this.importLocationUrl = importLocationUrl;
	}

	/**
	 * Gets the load settings.
	 *
	 * @return the load settings
	 */
	public String getLoadSettings() {
		return loadSettings;
	}

	/**
	 * Sets the load settings.
	 *
	 * @param loadSettings the new load settings
	 */
	public void setLoadSettings(String loadSettings) {
		this.loadSettings = loadSettings;
	}

	/**
	 * @return the studyNameTermId
	 */
	public String getStudyNameTermId() {
		return studyNameTermId;
	}

	/**
	 * @param studyNameTermId the studyNameTermId to set
	 */
	public void setStudyNameTermId(String studyNameTermId) {
		this.studyNameTermId = studyNameTermId;
	}

	public String getStartDateId() {
		return startDateId;
	}

	public void setStartDateId(String startDateId) {
		this.startDateId = startDateId;
	}

	public String getEndDateId() {
		return endDateId;
	}

	public void setEndDateId(String endDateId) {
		this.endDateId = endDateId;
	}

	public String getOpenGermplasmUrl() {
		return openGermplasmUrl;
	}

	public void setOpenGermplasmUrl(String openGermplasmUrl) {
		this.openGermplasmUrl = openGermplasmUrl;
	}

	
}
