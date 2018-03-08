/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.trial.form;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;
import org.generationcp.middleware.domain.oms.TraitClassReference;

import java.util.List;

/**
 * The Class AddOrRemoveTraitsForm.
 */
public class ManageSettingsForm {

	/** The selected setting id. */
	private int selectedSettingId;

	/** The setting name. */
	private String settingName;

	/** The nursery level variables. */
	private List<SettingDetail> studyLevelVariables;

	/** The plot level variables. */
	private List<SettingDetail> plotLevelVariables;

	/** The baseline trait variables. */
	private List<SettingDetail> baselineTraitVariables;

	/** The trial level variables. */
	private List<SettingDetail> trialLevelVariables;

	/** The treatment factors. */
	private List<SettingDetail> treatmentFactors;

	/** The is default. */
	private boolean isDefault;

	/** The selected variables. */
	private List<SettingVariable> selectedVariables;

	/** The project id. */
	private String projectId;

	/** The initial load. */
	private String initialLoad;

	/** The tree data. */
	private String treeData;
	// convert to json 1 level for the property and standard variable
	/** The search tree data. */
	private String searchTreeData;
	/** The trait reference list. */
	private List<TraitClassReference> traitClassReferenceList;

	/** The location id. */
	private String locationId;

	/** The breeding method id. */
	private String breedingMethodId;

	/** The location url. */
	private String locationUrl;

	/** The breeding method url. */
	private String breedingMethodUrl;

	/** The id name variables. */
	private String idNameVariables;

	/** The number of instances. */
	private String numberOfInstances;

	/**
	 * Gets the number of instances.
	 *
	 * @return the number of instances
	 */
	public String getNumberOfInstances() {
		return this.numberOfInstances;
	}

	/**
	 * Sets the number of instances.
	 *
	 * @param numberOfInstances the new number of instances
	 */
	public void setNumberOfInstances(String numberOfInstances) {
		this.numberOfInstances = numberOfInstances;
	}

	/**
	 * Gets the selected setting id.
	 *
	 * @return the selected setting id
	 */
	public int getSelectedSettingId() {
		return this.selectedSettingId;
	}

	/**
	 * Sets the selected setting id.
	 *
	 * @param selectedSettingId the new selected setting id
	 */
	public void setSelectedSettingId(int selectedSettingId) {
		this.selectedSettingId = selectedSettingId;
	}

	/**
	 * Gets the setting name.
	 *
	 * @return the setting name
	 */
	public String getSettingName() {
		return this.settingName;
	}

	/**
	 * Sets the setting name.
	 *
	 * @param settingName the new setting name
	 */
	public void setSettingName(String settingName) {
		this.settingName = settingName;
	}

	/**
	 * Gets the study level variables.
	 *
	 * @return the study level variables
	 */
	public List<SettingDetail> getStudyLevelVariables() {
		return this.studyLevelVariables;
	}

	/**
	 * Sets the study level variables.
	 *
	 * @param studyLevelVariables the new study level variables
	 */
	public void setStudyLevelVariables(List<SettingDetail> studyLevelVariables) {
		this.studyLevelVariables = studyLevelVariables;
	}

	/**
	 * Gets the plot level variables.
	 *
	 * @return the plot level variables
	 */
	public List<SettingDetail> getPlotLevelVariables() {
		return this.plotLevelVariables;
	}

	/**
	 * Sets the plot level variables.
	 *
	 * @param plotLevelVariables the new plot level variables
	 */
	public void setPlotLevelVariables(List<SettingDetail> plotLevelVariables) {
		this.plotLevelVariables = plotLevelVariables;
	}

	/**
	 * Gets the baseline trait variables.
	 *
	 * @return the baseline trait variables
	 */
	public List<SettingDetail> getBaselineTraitVariables() {
		return this.baselineTraitVariables;
	}

	/**
	 * Sets the baseline trait variables.
	 *
	 * @param baselineTraitVariables the new baseline trait variables
	 */
	public void setBaselineTraitVariables(List<SettingDetail> baselineTraitVariables) {
		this.baselineTraitVariables = baselineTraitVariables;
	}

	/**
	 * Gets the checks if is default.
	 *
	 * @return the checks if is default
	 */
	public boolean getIsDefault() {
		return this.isDefault;
	}

	/**
	 * Sets the checks if is default.
	 *
	 * @param isDefault the new checks if is default
	 */
	public void setIsDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	/**
	 * Gets the project id.
	 *
	 * @return the project id
	 */
	public String getProjectId() {
		return this.projectId;
	}

	/**
	 * Sets the project id.
	 *
	 * @param projectId the new project id
	 */
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	/**
	 * Gets the selected variables.
	 *
	 * @return the selected variables
	 */
	public List<SettingVariable> getSelectedVariables() {
		return this.selectedVariables;
	}

	/**
	 * Sets the selected variables.
	 *
	 * @param selectedVariables the new selected variables
	 */
	public void setSelectedVariables(List<SettingVariable> selectedVariables) {
		this.selectedVariables = selectedVariables;
	}

	/**
	 * Gets the initial load.
	 *
	 * @return the initial load
	 */
	public String getInitialLoad() {
		return this.initialLoad;
	}

	/**
	 * Sets the initial load.
	 *
	 * @param initialLoad the new initial load
	 */
	public void setInitialLoad(String initialLoad) {
		this.initialLoad = initialLoad;
	}

	/**
	 * Gets the tree data.
	 *
	 * @return the tree data
	 */
	public String getTreeData() {
		return this.treeData;
	}

	/**
	 * Sets the tree data.
	 *
	 * @param treeData the new tree data
	 */
	public void setTreeData(String treeData) {
		this.treeData = treeData;
	}

	/**
	 * Gets the search tree data.
	 *
	 * @return the search tree data
	 */
	public String getSearchTreeData() {
		return this.searchTreeData;
	}

	/**
	 * Sets the search tree data.
	 *
	 * @param searchTreeData the new search tree data
	 */
	public void setSearchTreeData(String searchTreeData) {
		this.searchTreeData = searchTreeData;
	}

	/**
	 * Gets the trait class reference list.
	 *
	 * @return the trait class reference list
	 */
	public List<TraitClassReference> getTraitClassReferenceList() {
		return this.traitClassReferenceList;
	}

	/**
	 * Sets the trait class reference list.
	 *
	 * @param traitClassReferenceList the new trait class reference list
	 */
	public void setTraitClassReferenceList(List<TraitClassReference> traitClassReferenceList) {
		this.traitClassReferenceList = traitClassReferenceList;
	}

	/**
	 * Clear.
	 */
	public void clear() {
		this.selectedSettingId = 0;
		this.settingName = null;
		this.studyLevelVariables = null;
		this.plotLevelVariables = null;
		this.baselineTraitVariables = null;
		this.isDefault = false;
		this.selectedVariables = null;
		this.trialLevelVariables = null;
		this.treatmentFactors = null;
	}

	/**
	 * Gets the location id.
	 *
	 * @return the location id
	 */
	public String getLocationId() {
		return this.locationId;
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
		return this.breedingMethodId;
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
		return this.locationUrl;
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
		return this.breedingMethodUrl;
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
	 * Gets the id name variables.
	 *
	 * @return the id name variables
	 */
	public String getIdNameVariables() {
		return this.idNameVariables;
	}

	/**
	 * Sets the id name variables.
	 *
	 * @param idNameVariables the new id name variables
	 */
	public void setIdNameVariables(String idNameVariables) {
		this.idNameVariables = idNameVariables;
	}

	/**
	 * Gets the trial level variables.
	 *
	 * @return the trial level variables
	 */
	public List<SettingDetail> getTrialLevelVariables() {
		return this.trialLevelVariables;
	}

	/**
	 * Sets the trial level variables.
	 *
	 * @param trialLevelVariables the new trial level variables
	 */
	public void setTrialLevelVariables(List<SettingDetail> trialLevelVariables) {
		this.trialLevelVariables = trialLevelVariables;
	}

	/**
	 * @return the treatmentFactors
	 */
	public List<SettingDetail> getTreatmentFactors() {
		return this.treatmentFactors;
	}

	/**
	 * @param treatmentFactors the treatmentFactors to set
	 */
	public void setTreatmentFactors(List<SettingDetail> treatmentFactors) {
		this.treatmentFactors = treatmentFactors;
	}

}
