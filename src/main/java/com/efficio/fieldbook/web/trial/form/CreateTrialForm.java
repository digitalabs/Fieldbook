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

import org.generationcp.middleware.domain.dms.ValueReference;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.TreatmentFactorDetail;

/**
 * The Class CreateNurseryForm.
 */
public class CreateTrialForm {

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
	
	/** The trial level variables. */
        private List<SettingDetail> trialLevelVariables;
        
        private List<TreatmentFactorDetail> treatmentFactors;
        

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
    
    /** The load settings. */
    private String loadSettings;
    
    /** The study name term id. */
    private String studyNameTermId;
    
    /** The start date id. */
    private String startDateId;
    
    /** The end date id. */
    private String endDateId;
    
    /** The trial instances. */
    private int trialInstances;
    
    /** The trial environment values. */
    private List<List<ValueReference>> trialEnvironmentValues;
    
    /** The trial instance factor. */
    private String trialInstanceFactor;
    
    /** The replicates. */
    private String replicates;
    
    /** The block size. */
    private String blockSize;
    
    /** The experimental design. */
    private String experimentalDesign;
    
    /** The experimental design for all. */
    private String experimentalDesignForAll;
    
    private String designLayout;
    
    private String file;
    
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

	

	/**
	 * Gets the study level variables.
	 *
	 * @return the study level variables
	 */
	public List<SettingDetail> getStudyLevelVariables() {
		return studyLevelVariables;
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
	 * Gets the study name term id.
	 *
	 * @return the studyNameTermId
	 */
	public String getStudyNameTermId() {
		return studyNameTermId;
	}

	/**
	 * Sets the study name term id.
	 *
	 * @param studyNameTermId the studyNameTermId to set
	 */
	public void setStudyNameTermId(String studyNameTermId) {
		this.studyNameTermId = studyNameTermId;
	}

	/**
	 * Gets the start date id.
	 *
	 * @return the start date id
	 */
	public String getStartDateId() {
		return startDateId;
	}

	/**
	 * Sets the start date id.
	 *
	 * @param startDateId the new start date id
	 */
	public void setStartDateId(String startDateId) {
		this.startDateId = startDateId;
	}

	/**
	 * Gets the end date id.
	 *
	 * @return the end date id
	 */
	public String getEndDateId() {
		return endDateId;
	}

	/**
	 * Sets the end date id.
	 *
	 * @param endDateId the new end date id
	 */
	public void setEndDateId(String endDateId) {
		this.endDateId = endDateId;
	}
	
	/**
	 * Gets the trial instances.
	 *
	 * @return the trial instances
	 */
	public int getTrialInstances() {
	    return this.trialInstances;
	}
	
	/**
	 * Sets the trial instances.
	 *
	 * @param trialInstances the new trial instances
	 */
	public void setTrialInstances(int trialInstances) {
	    this.trialInstances = trialInstances;
	}

	/**
         * Gets the trial level variables.
         *
         * @return the trial level variables
         */
        public List<SettingDetail> getTrialLevelVariables() {
                return trialLevelVariables;
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
         * Gets the trial environment values.
         *
         * @return the trial environment values
         */
        public List<List<ValueReference>> getTrialEnvironmentValues() {
            return this.trialEnvironmentValues;
        } 
        
        /**
         * Sets the trial environment values.
         *
         * @param trialEnvironmentValues the new trial environment values
         */
        public void setTrialEnvironmentValues(List<List<ValueReference>> trialEnvironmentValues) {
            this.trialEnvironmentValues = trialEnvironmentValues;
        }
        
        /**
         * Gets the trial instance factor.
         *
         * @return the trial instance factor
         */
        public String getTrialInstanceFactor() {
            return this.trialInstanceFactor;
        }
        
        /**
         * Sets the trial instance factor.
         *
         * @param trialInstanceFactor the new trial instance factor
         */
        public void setTrialInstanceFactor(String trialInstanceFactor) {
            this.trialInstanceFactor = trialInstanceFactor;
        }

        /**
         * Gets the replicates.
         *
         * @return the replicates
         */
        public String getReplicates() {
            return replicates;
        }

        /**
         * Sets the replicates.
         *
         * @param replicates the replicates to set
         */
        public void setReplicates(String replicates) {
            this.replicates = replicates;
        }

        /**
         * Gets the block size.
         *
         * @return the blockSize
         */
        public String getBlockSize() {
            return blockSize;
        }

        /**
         * Sets the block size.
         *
         * @param blockSize the blockSize to set
         */
        public void setBlockSize(String blockSize) {
            this.blockSize = blockSize;
        }

        /**
         * Gets the experimental design.
         *
         * @return the experimentalDesign
         */
        public String getExperimentalDesign() {
            return experimentalDesign;
        }

        /**
         * Sets the experimental design.
         *
         * @param experimentalDesign the experimentalDesign to set
         */
        public void setExperimentalDesign(String experimentalDesign) {
            this.experimentalDesign = experimentalDesign;
        }

        /**
         * Gets the experimental design for all.
         *
         * @return the experimentalDesignForAll
         */
        public String getExperimentalDesignForAll() {
            return experimentalDesignForAll;
        }

        /**
         * Sets the experimental design for all.
         *
         * @param experimentalDesignForAll the experimentalDesignForAll to set
         */
        public void setExperimentalDesignForAll(String experimentalDesignForAll) {
            this.experimentalDesignForAll = experimentalDesignForAll;
        }

        /**
         * @return the file
         */
        public String getFile() {
            return file;
        }

        /**
         * @param file the file to set
         */
        public void setFile(String file) {
            this.file = file;
        }

		public String getDesignLayout() {
			return designLayout;
		}

		public void setDesignLayout(String designLayout) {
			this.designLayout = designLayout;
		}

        /**
         * @return the treatmentFactors
         */
        public List<TreatmentFactorDetail> getTreatmentFactors() {
            return treatmentFactors;
        }

        /**
         * @param treatmentFactors the treatmentFactors to set
         */
        public void setTreatmentFactors(List<TreatmentFactorDetail> treatmentFactors) {
            this.treatmentFactors = treatmentFactors;
        }
        
}
