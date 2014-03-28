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
package com.efficio.fieldbook.web.trial.form;

import java.util.List;

import org.generationcp.middleware.domain.oms.TraitClassReference;

import com.efficio.fieldbook.web.nursery.bean.SettingDetail;
import com.efficio.fieldbook.web.nursery.bean.SettingVariable;

// TODO: Auto-generated Javadoc
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
	private List<SettingDetail> trialLevelVariables;
	
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
    //convert to json 1 level for the property and standard variable
    /** The search tree data. */
    private String searchTreeData;
    /** The trait reference list. */
    private List<TraitClassReference> traitClassReferenceList;
    
    private String locationId;
    private String breedingMethodId;
    private String locationUrl;
    private String breedingMethodUrl;
    private String idNameVariables;
    
    private String numberOfInstances;
    
    

	public String getNumberOfInstances() {
		return numberOfInstances;
	}

	public void setNumberOfInstances(String numberOfInstances) {
		this.numberOfInstances = numberOfInstances;
	}

	/**
	 * Gets the selected setting id.
	 *
	 * @return the selected setting id
	 */
	public int getSelectedSettingId() {
	    return selectedSettingId;
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
            return settingName;
        }
        
        /**
         * Sets the setting name.
         *
         * @param settingName the new setting name
         */
        public void setSettingName(String settingName) {
            this.settingName = settingName;
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
         * @return the plot level variables
         */
        public List<SettingDetail> getPlotLevelVariables() {
            return plotLevelVariables;
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
            return baselineTraitVariables;
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
            return isDefault;
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

		/**
		 * Gets the selected variables.
		 *
		 * @return the selected variables
		 */
		public List<SettingVariable> getSelectedVariables() {
			return selectedVariables;
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
			return initialLoad;
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
			return treeData;
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
			return searchTreeData;
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
			return traitClassReferenceList;
		}

		/**
		 * Sets the trait class reference list.
		 *
		 * @param traitClassReferenceList the new trait class reference list
		 */
		public void setTraitClassReferenceList(
				List<TraitClassReference> traitClassReferenceList) {
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
		}

		public String getLocationId() {
			return locationId;
		}

		public void setLocationId(String locationId) {
			this.locationId = locationId;
		}

		public String getBreedingMethodId() {
			return breedingMethodId;
		}

		public void setBreedingMethodId(String breedingMethodId) {
			this.breedingMethodId = breedingMethodId;
		}

		public String getLocationUrl() {
			return locationUrl;
		}

		public void setLocationUrl(String locationUrl) {
			this.locationUrl = locationUrl;
		}

		public String getBreedingMethodUrl() {
			return breedingMethodUrl;
		}

		public void setBreedingMethodUrl(String breedingMethodUrl) {
			this.breedingMethodUrl = breedingMethodUrl;
		}
		
		public String getIdNameVariables() {
		        return this.idNameVariables;
		}
		
		public void setIdNameVariables(String idNameVariables) {
		        this.idNameVariables = idNameVariables;
		}

		public List<SettingDetail> getTrialLevelVariables() {
			return trialLevelVariables;
		}

		public void setTrialLevelVariables(List<SettingDetail> trialLevelVariables) {
			this.trialLevelVariables = trialLevelVariables;
		}
		
}
