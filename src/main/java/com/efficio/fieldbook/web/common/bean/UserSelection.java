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

import com.efficio.fieldbook.web.trial.bean.ExpDesignParameterUi;
import org.generationcp.commons.parsing.pojo.ImportedCrossesList;
import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmMainInfo;
import org.generationcp.commons.settings.CrossSetting;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.TraitClassReference;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * This bean models the various input that the user builds up over time
 * to perform the actual loading operation.
 */
public class UserSelection implements Serializable {
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The actual file name. */
    private String actualFileName;
    
    /** The server file name. */
    private String serverFileName;
        
    /** The field layout random. */
    private boolean fieldLayoutRandom;
    
    /** The imported germplasm main info. */
    private ImportedGermplasmMainInfo importedGermplasmMainInfo;
    
    /** The imported check germplasm main info. */
    private ImportedGermplasmMainInfo importedCheckGermplasmMainInfo;

    /** The is import valid. */
    private boolean isImportValid;
    
    /** The study details list.*/
    private transient List<StudyDetails> studyDetailsList;
    
    /** The current page germplasm list. */
    private int currentPageGermplasmList;
    
    /** The current page check germplasm list. */
    private int currentPageCheckGermplasmList;
    
    /**  Nursery level conditions in Manage Settings. */
    private List<SettingDetail> studyLevelConditions;
    
    /**  Plot level factors in Manage Settings. */
    private List<SettingDetail> plotsLevelList;
    
    /**  Baseline Traits in Manage Settings. */
    private List<SettingDetail> baselineTraitsList;
    
    /** The trial level variable list. */
    private List<SettingDetail> trialLevelVariableList;

    /** The trial environment values. */
    private List<List<ValueReference>> trialEnvironmentValues;
        
    /** The imported advanced germplasm list. */
    private List<ImportedGermplasm> importedAdvancedGermplasmList;
    
    /** The standard variable map. */
    private Map<String, StandardVariable> standardVariableMap = new HashMap<String, StandardVariable>(); //for easy access
    
    /** The trait ref list. */
    private List<TraitClassReference> traitRefList;
    
    /** The treatment factors. */
    private List<SettingDetail> treatmentFactors;
    
    /** The selection variates. */
    private List<SettingDetail> selectionVariates;
    
    /** The basic details. */
    private List<SettingDetail> basicDetails;
    
    /** The nursery conditions. */
    private List<SettingDetail> nurseryConditions;
    
    /** The deleted study level conditions. */
    private List<SettingDetail> deletedStudyLevelConditions;
    
    /** The deleted plot level list. */
    private List<SettingDetail> deletedPlotLevelList;
    
    /** The deleted baseline traits list. */
    private List<SettingDetail> deletedBaselineTraitsList;
    
    /** The deleted nursery conditions. */
    private List<SettingDetail> deletedNurseryConditions;
    
    /** The deleted trial level variables. */
    private List<SettingDetail> deletedTrialLevelVariables;
    
    /** The deleted treatment factors. */
    private List<SettingDetail> deletedTreatmentFactors;
    
    /** The change details. */
    private List<GermplasmChangeDetail> changeDetails;
    
    /** The removed factors. */
    private List<SettingDetail> removedFactors;
    
    /** The removed conditions. */
    private List<SettingDetail> removedConditions;
    
    /** The new traits. */
    private List<SettingDetail> newTraits;
    
    /** The new selection variates. */
    private List<SettingDetail> newSelectionVariates;
    
    /** The workbook. */
    private Workbook workbook;
    
    /** The temporary workbook. */
    private Workbook temporaryWorkbook;
    
    /** The current page. */
    private int currentPage;
    
    /** The measurement row list. */
    private List<MeasurementRow> measurementRowList;
    private List<MeasurementVariable> measurementDatasetVariable;
    
    private List<MeasurementVariable> constantsWithLabels;
    
    private ExpDesignParameterUi expDesignParams;
    private List<Integer> expDesignVariables;
    /** The List of experimental design variables (new, edit, or deleted) since the workbook was retrieved from the db, will be reset upon save  */
    private List<MeasurementVariable> experimentalDesignVariables;
    private ImportedCrossesList importedCrossesList;
    private List<Integer> importedCrossesId;

    private CrossSetting crossSettings;

    /**
     * Gets the current page germplasm list.
     *
     * @return the current page germplasm list
     */
    public int getCurrentPageGermplasmList() {
		return currentPageGermplasmList;
	}

	/**
	 * Sets the current page germplasm list.
	 *
	 * @param currentPageGermplasmList the new current page germplasm list
	 */
	public void setCurrentPageGermplasmList(int currentPageGermplasmList) {
		this.currentPageGermplasmList = currentPageGermplasmList;
	}
	
	

	/**
	 * Gets the current page check germplasm list.
	 *
	 * @return the current page check germplasm list
	 */
	public int getCurrentPageCheckGermplasmList() {
		return currentPageCheckGermplasmList;
	}

	/**
	 * Sets the current page check germplasm list.
	 *
	 * @param currentPageCheckGermplasmList the new current page check germplasm list
	 */
	public void setCurrentPageCheckGermplasmList(int currentPageCheckGermplasmList) {
		this.currentPageCheckGermplasmList = currentPageCheckGermplasmList;
	}

	/**
     * Checks if is import valid.
     *
     * @return true, if is import valid
     */
    public boolean isImportValid() {
        return isImportValid;
    }
    
    /**
     * Sets the import valid.
     *
     * @param isImportValid the new import valid
     */
    public void setImportValid(boolean isImportValid) {
        this.isImportValid = isImportValid;
    }

    /**
     * Gets the imported germplasm main info.
     *
     * @return the imported germplasm main info
     */
    public ImportedGermplasmMainInfo getImportedGermplasmMainInfo() {
        return importedGermplasmMainInfo;
    }

    /**
     * Sets the imported germplasm main info.
     *
     * @param importedGermplasmMainInfo the new imported germplasm main info
     */
    public void setImportedGermplasmMainInfo(
            ImportedGermplasmMainInfo importedGermplasmMainInfo) {
        this.importedGermplasmMainInfo = importedGermplasmMainInfo;
    }

    /**
     * Gets the actual file name.
     *
     * @return the actual file name
     */
    public String getActualFileName() {
        return actualFileName;
    }

    /**
     * Sets the actual file name.
     *
     * @param actualFileName the new actual file name
     */
    public void setActualFileName(String actualFileName) {
        this.actualFileName = actualFileName;
    }

    /**
     * Gets the server file name.
     *
     * @return the server file name
     */
    public String getServerFileName() {
        return serverFileName;
    }

    /**
     * Sets the server file name.
     *
     * @param serverFileName the new server file name
     */
    public void setServerFileName(String serverFileName) {
        this.serverFileName = serverFileName;
    }

    /**
     * Gets the workbook.
     *
     * @return the workbook
     */
    public Workbook getWorkbook() {
        return workbook;
    }

    /**
     * Sets the workbook.
     *
     * @param workbook the new workbook
     */
    public void setWorkbook(Workbook workbook) {
        this.workbook = workbook;
    }

    /**
     * Gets the field layout random.
     *
     * @return the field layout random
     */
    public Boolean getFieldLayoutRandom() {
        return fieldLayoutRandom;
    }

    /**
     * Sets the field layout random.
     *
     * @param fieldLayoutRandom the new field layout random
     */
    public void setFieldLayoutRandom(Boolean fieldLayoutRandom) {
        this.fieldLayoutRandom = fieldLayoutRandom;
    }
    
    /**
     * Gets the study details list.
     *
     * @return the study details list
     */
    public List<StudyDetails> getStudyDetailsList(){
        return studyDetailsList;
    }

    /**
     * Sets the study details list.
     *
     * @param studyDetailsList the new study details list
     */
    public void setStudyDetailsList(List<StudyDetails> studyDetailsList) {
        this.studyDetailsList = studyDetailsList;
    }

	/**
	 * Gets the study level conditions.
	 *
	 * @return the study level conditions
	 */
	public List<SettingDetail> getStudyLevelConditions() {
		return studyLevelConditions;
	}

	/**
	 * Sets the study level conditions.
	 *
	 * @param studyLevelConditions the new study level conditions
	 */
	public void setStudyLevelConditions(List<SettingDetail> studyLevelConditions) {
		this.studyLevelConditions = studyLevelConditions;
	}

	/**
	 * Gets the plots level list.
	 *
	 * @return the plotsLevelList
	 */
	public List<SettingDetail> getPlotsLevelList() {
		return plotsLevelList;
	}

	/**
	 * Sets the plots level list.
	 *
	 * @param plotsLevelList the plotsLevelList to set
	 */
	public void setPlotsLevelList(List<SettingDetail> plotsLevelList) {
		this.plotsLevelList = plotsLevelList;
	}

	/**
	 * Gets the baseline traits list.
	 *
	 * @return the baselineTraitsList
	 */
	public List<SettingDetail> getBaselineTraitsList() {
		return baselineTraitsList;
	}

	/**
	 * Sets the baseline traits list.
	 *
	 * @param baselineTraitsList the baselineTraitsList to set
	 */
	public void setBaselineTraitsList(List<SettingDetail> baselineTraitsList) {
		this.baselineTraitsList = baselineTraitsList;
	}

	/**
	 * Gets the cache standard variable.
	 *
	 * @param id the id
	 * @return the cache standard variable
	 */
	public StandardVariable getCacheStandardVariable(int id){
		if(standardVariableMap != null && standardVariableMap.containsKey(Integer.toString(id))){
			return standardVariableMap.get(Integer.toString(id));
		}
		return null;
	}
	
	/**
	 * Put standard variable in cache.
	 *
	 * @param variable the variable
	 */
	public void putStandardVariableInCache(StandardVariable variable) {
		standardVariableMap.put(Integer.toString(variable.getId()), variable);
	}
	
	/**
	 * Gets the imported advanced germplasm list.
	 *
	 * @return the imported advanced germplasm list
	 */
	public List<ImportedGermplasm> getImportedAdvancedGermplasmList() {
		return importedAdvancedGermplasmList;
	}

	/**
	 * Sets the imported advanced germplasm list.
	 *
	 * @param importedAdvancedGermplasmList the new imported advanced germplasm list
	 */
	public void setImportedAdvancedGermplasmList(
			List<ImportedGermplasm> importedAdvancedGermplasmList) {
		this.importedAdvancedGermplasmList = importedAdvancedGermplasmList;
	}

	/**
	 * Gets the trait ref list.
	 *
	 * @return the trait ref list
	 */
	public List<TraitClassReference> getTraitRefList() {
		return traitRefList;
	}

	/**
	 * Sets the trait ref list.
	 *
	 * @param traitRefList the new trait ref list
	 */
	public void setTraitRefList(List<TraitClassReference> traitRefList) {
		this.traitRefList = traitRefList;
	}
	

	/**
	 * Gets the trial level variable list.
	 *
	 * @return the trial level variable list
	 */
	public List<SettingDetail> getTrialLevelVariableList() {
		return trialLevelVariableList;
	}

	/**
	 * Sets the trial level variable list.
	 *
	 * @param trialLevelVariableList the new trial level variable list
	 */
	public void setTrialLevelVariableList(List<SettingDetail> trialLevelVariableList) {
		this.trialLevelVariableList = trialLevelVariableList;
	}

	/**
	 * Checks if is trial.
	 *
	 * @return true, if is trial
	 */
	public boolean isTrial() {
		if (this.workbook != null) {
			return !this.workbook.isNursery();
		}
		return false;
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
		 * Gets the imported check germplasm main info.
		 *
		 * @return the imported check germplasm main info
		 */
		public ImportedGermplasmMainInfo getImportedCheckGermplasmMainInfo() {
			return importedCheckGermplasmMainInfo;
		}

		/**
		 * Sets the imported check germplasm main info.
		 *
		 * @param importedCheckGermplasmMainInfo the new imported check germplasm main info
		 */
		public void setImportedCheckGermplasmMainInfo(
				ImportedGermplasmMainInfo importedCheckGermplasmMainInfo) {
			this.importedCheckGermplasmMainInfo = importedCheckGermplasmMainInfo;
		}

		/**
		 * Gets the treatment factors.
		 *
		 * @return the treatment factors
		 */
		public List<SettingDetail> getTreatmentFactors() {
			return treatmentFactors;
		}

		/**
		 * Sets the treatment factors.
		 *
		 * @param treatmentFactors the new treatment factors
		 */
		public void setTreatmentFactors(List<SettingDetail> treatmentFactors) {
			this.treatmentFactors = treatmentFactors;
		}

        /**
         * Gets the selection variates.
         *
         * @return the selectionVariates
         */
        public List<SettingDetail> getSelectionVariates() {
            return selectionVariates;
        }

        /**
         * Sets the selection variates.
         *
         * @param selectionVariates the selectionVariates to set
         */
        public void setSelectionVariates(List<SettingDetail> selectionVariates) {
            this.selectionVariates = selectionVariates;
        }

        /**
         * Gets the basic details.
         *
         * @return the basicDetails
         */
        public List<SettingDetail> getBasicDetails() {
            return basicDetails;
        }

        /**
         * Sets the basic details.
         *
         * @param basicDetails the basicDetails to set
         */
        public void setBasicDetails(List<SettingDetail> basicDetails) {
            this.basicDetails = basicDetails;
        }

        /**
         * Gets the nursery conditions.
         *
         * @return the nurseryConditions
         */
        public List<SettingDetail> getNurseryConditions() {
            return nurseryConditions;
        }

        /**
         * Sets the nursery conditions.
         *
         * @param nurseryConditions the nurseryConditions to set
         */
        public void setNurseryConditions(List<SettingDetail> nurseryConditions) {
            this.nurseryConditions = nurseryConditions;
        }

        /**
         * Gets the deleted study level conditions.
         *
         * @return the deletedStudyLevelConditions
         */
        public List<SettingDetail> getDeletedStudyLevelConditions() {
            return deletedStudyLevelConditions;
        }

        /**
         * Sets the deleted study level conditions.
         *
         * @param deletedStudyLevelConditions the deletedStudyLevelConditions to set
         */
        public void setDeletedStudyLevelConditions(
                List<SettingDetail> deletedStudyLevelConditions) {
            this.deletedStudyLevelConditions = deletedStudyLevelConditions;
        }

        /**
         * Gets the deleted baseline traits list.
         *
         * @return the deletedBaselineTraitsList
         */
        public List<SettingDetail> getDeletedBaselineTraitsList() {
            return deletedBaselineTraitsList;
        }

        /**
         * Sets the deleted baseline traits list.
         *
         * @param deletedBaselineTraitsList the deletedBaselineTraitsList to set
         */
        public void setDeletedBaselineTraitsList(
                List<SettingDetail> deletedBaselineTraitsList) {
            this.deletedBaselineTraitsList = deletedBaselineTraitsList;
        }

        /**
         * Gets the deleted nursery conditions.
         *
         * @return the deletedNurseryConditions
         */
        public List<SettingDetail> getDeletedNurseryConditions() {
            return deletedNurseryConditions;
        }

        /**
         * Sets the deleted nursery conditions.
         *
         * @param deletedNurseryConditions the deletedNurseryConditions to set
         */
        public void setDeletedNurseryConditions(
                List<SettingDetail> deletedNurseryConditions) {
            this.deletedNurseryConditions = deletedNurseryConditions;
        }

        /**
         * Gets the deleted plot level list.
         *
         * @return the deletedPlotLevelList
         */
        public List<SettingDetail> getDeletedPlotLevelList() {
            return deletedPlotLevelList;
        }

        /**
         * Sets the deleted plot level list.
         *
         * @param deletedPlotLevelList the deletedPlotLevelList to set
         */
        public void setDeletedPlotLevelList(List<SettingDetail> deletedPlotLevelList) {
            this.deletedPlotLevelList = deletedPlotLevelList;
        }

		/**
		 * Gets the change details.
		 *
		 * @return the changeDetails
		 */
		public List<GermplasmChangeDetail> getChangeDetails() {
			return changeDetails;
		}

		/**
		 * Sets the change details.
		 *
		 * @param changeDetails the changeDetails to set
		 */
		public void setChangeDetails(List<GermplasmChangeDetail> changeDetails) {
			this.changeDetails = changeDetails;
		}

		/**
		 * Gets the removed factors.
		 *
		 * @return the removed factors
		 */
		public List<SettingDetail> getRemovedFactors() {
			return removedFactors;
		}

		/**
		 * Sets the removed factors.
		 *
		 * @param removedFactors the new removed factors
		 */
		public void setRemovedFactors(List<SettingDetail> removedFactors) {
			this.removedFactors = removedFactors;
		}

        /**
         * Gets the removed conditions.
         *
         * @return the removed conditions
         */
        public List<SettingDetail> getRemovedConditions() {
            return removedConditions;
        }

        /**
         * Sets the removed conditions.
         *
         * @param removedConditions the new removed conditions
         */
        public void setRemovedConditions(List<SettingDetail> removedConditions) {
            this.removedConditions = removedConditions;
        }

		/**
		 * Gets the new traits.
		 *
		 * @return the newTraits
		 */
		public List<SettingDetail> getNewTraits() {
			return newTraits;
		}

		/**
		 * Sets the new traits.
		 *
		 * @param newTraits the newTraits to set
		 */
		public void setNewTraits(List<SettingDetail> newTraits) {
			this.newTraits = newTraits;
		}

		/**
		 * Gets the new selection variates.
		 *
		 * @return the newSelectionVariates
		 */
		public List<SettingDetail> getNewSelectionVariates() {
			return newSelectionVariates;
		}

		/**
		 * Sets the new selection variates.
		 *
		 * @param newSelectionVariates the newSelectionVariates to set
		 */
		public void setNewSelectionVariates(List<SettingDetail> newSelectionVariates) {
			this.newSelectionVariates = newSelectionVariates;
		}
		
		/**
		 * Gets the current page.
		 *
		 * @return the currentPage
		 */
	    public int getCurrentPage() {
	        return currentPage;
	    }

	    /**
    	 * Sets the current page.
    	 *
    	 * @param currentPage the currentPage to set
    	 */
	    public void setCurrentPage(int currentPage) {
	        this.currentPage = currentPage;
	    }

	    /**
    	 * Gets the measurement row list.
    	 *
    	 * @return the measurementRowList
    	 */
	    public List<MeasurementRow> getMeasurementRowList() {
	        return measurementRowList;
	    }

	    /**
    	 * Sets the measurement row list.
    	 *
    	 * @param measurementRowList the measurementRowList to set
    	 */
	    public void setMeasurementRowList(List<MeasurementRow> measurementRowList) {
	        this.measurementRowList = measurementRowList;
	    }

		/**
		 * Gets the temporary workbook.
		 *
		 * @return the temporary workbook
		 */
		public Workbook getTemporaryWorkbook() {
			return temporaryWorkbook;
		}

		/**
		 * Sets the temporary workbook.
		 *
		 * @param temporaryWorkbook the new temporary workbook
		 */
		public void setTemporaryWorkbook(Workbook temporaryWorkbook) {
			this.temporaryWorkbook = temporaryWorkbook;
		}
		
        /**
         * Gets the deleted trial level variables.
         *
         * @return the deleted trial level variables
         */
        public List<SettingDetail> getDeletedTrialLevelVariables() {
            return deletedTrialLevelVariables;
        }

        /**
         * Sets the deleted trial level variables.
         *
         * @param deletedTrialLevelVariables the new deleted trial level variables
         */
        public void setDeletedTrialLevelVariables(List<SettingDetail> deletedTrialLevelVariables) {
            this.deletedTrialLevelVariables = deletedTrialLevelVariables;
        }

		public List<MeasurementVariable> getMeasurementDatasetVariable() {
			return measurementDatasetVariable;
		}

		public void setMeasurementDatasetVariable(
				List<MeasurementVariable> measurementDatasetVariable) {
			this.measurementDatasetVariable = measurementDatasetVariable;
		}
        
        /**
         * Gets the deleted treatment factors.
         *
         * @return the deleted treatment factors
         */
        public List<SettingDetail> getDeletedTreatmentFactors() {
            return deletedTreatmentFactors;
        }

        /**
         * Sets the deleted treatment factors.
         *
         * @param deletedTreatmentFactors the new deleted treatment factors
         */
        public void setDeletedTreatmentFactors(List<SettingDetail> deletedTreatmentFactors) {
            this.deletedTreatmentFactors = deletedTreatmentFactors;
        }

		/**
		 * @return the constantsWithLabels
		 */
		public List<MeasurementVariable> getConstantsWithLabels() {
			return constantsWithLabels;
		}

		/**
		 * @param constantsWithLabels the constantsWithLabels to set
		 */
		public void setConstantsWithLabels(List<MeasurementVariable> constantsWithLabels) {
			this.constantsWithLabels = constantsWithLabels;
		}

		/**
		 * @return the expDesignParams
		 */
		public ExpDesignParameterUi getExpDesignParams() {
			return expDesignParams;
		}

		/**
		 * @param expDesignParams the expDesignParams to set
		 */
		public void setExpDesignParams(ExpDesignParameterUi expDesignParams) {
			this.expDesignParams = expDesignParams;
		}

		/**
		 * @return the expDesignVariables
		 */
		public List<Integer> getExpDesignVariables() {
			return expDesignVariables;
		}

		/**
		 * @param expDesignVariables the expDesignVariables to set
		 */
		public void setExpDesignVariables(List<Integer> expDesignVariables) {
			this.expDesignVariables = expDesignVariables;
		}

		/**
		 * @return the experimentalDesignVariables
		 */
		public List<MeasurementVariable> getExperimentalDesignVariables() {
			return experimentalDesignVariables;
		}

		/**
		 * @param experimentalDesignVariables the experimentalDesignVariables to set
		 */
		public void setExperimentalDesignVariables(
				List<MeasurementVariable> experimentalDesignVariables) {
			this.experimentalDesignVariables = experimentalDesignVariables;
		}

    public ImportedCrossesList getImportedCrossesList() {
        return importedCrossesList;
    }

    public void setimportedCrossesList(ImportedCrossesList importedCrossesList) {
        this.importedCrossesList = importedCrossesList;
    }

    public CrossSetting getCrossSettings() {
        return crossSettings;
    }

    public void setCrossSettings(CrossSetting crossSettings) {
        this.crossSettings = crossSettings;
    }

	public List<Integer> getImportedCrossesId() {
		return importedCrossesId;
	}

	public void setImportedCrossesId(List<Integer> importedCrossesId) {
		this.importedCrossesId = importedCrossesId;
	}
    
    public void addImportedCrossesId(Integer crossesId){
    	if(importedCrossesId == null){
    		importedCrossesId = new ArrayList<Integer>();
    	}
    	if(crossesId != null){
    		importedCrossesId.add(crossesId);
    	}
    }
}
