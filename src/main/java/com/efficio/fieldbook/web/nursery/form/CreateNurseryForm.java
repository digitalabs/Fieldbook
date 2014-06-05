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

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;


// TODO: Auto-generated Javadoc
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
    
    /** The selection variates variables. */
    private List<SettingDetail> selectionVariatesVariables;

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
    
    /** The open germplasm url. */
    private String openGermplasmUrl;
    
    /** The load settings. */
    private String loadSettings;
    
    /** The study name term id. */
    private String studyNameTermId;
    
    /** The start date id. */
    private String startDateId;
    
    /** The end date id. */
    private String endDateId;
    
    /** The tree data. */
    private String treeData;
    
    //convert to json 1 level for the property and standard variable
    /** The search tree data. */
    private String searchTreeData;
    
    /** The selected variables. */
    private List<SettingVariable> selectedVariables;

    /** The basic details. */
    private List<SettingDetail> basicDetails;
    
    /** The nursery conditions. */
    private List<SettingDetail> nurseryConditions;
    
    /** The id name variables. */
    private String idNameVariables;
    
    /** The measurement row list. */
    private List<MeasurementRow> measurementRowList;
    
    /** The measurement variables. */
    private List<MeasurementVariable> measurementVariables;
    
    /** The study name. */
    private String studyName;
    
    /** The paginated imported germplasm. */
    private List<MeasurementRow> paginatedMeasurementRowList;       
    
    /** The current page. */
    private int currentPage;
            
    /** The total pages. */
    private int totalPages;
    
    /** The result per page. */
    private int resultPerPage = 100;
    
    /** The file. */
    private MultipartFile file;
    
    /** The has error. */
    private String hasError;
    
    /** The import val. */
    private int importVal;
    
    /** The number of instances. */
    private int numberOfInstances;
    
    /** The export instance type. */
    private String exportInstanceType;
    
    /** The export trial instance start. */
    private String exportTrialInstanceNumber;
    
    /** The export trial instance start. */
    private String exportTrialInstanceStart;
    
    /** The export trial instance end. */
    private String exportTrialInstanceEnd;
    
    /** The study id. */
    private Integer studyId;
    
    /** The trial environment values. */
    private List<List<ValueReference>> trialEnvironmentValues;
    
    /** The trial level variables. */
    private List<SettingDetail> trialLevelVariables;
    
    /** The selection variates segment. */
    private String selectionVariatesSegment;
    
    /** The baseline traits segment. */
    private String baselineTraitsSegment;
    
    private boolean isMeasurementDataExisting;
    
    private int charLimit;
    
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
	 * Gets the open germplasm url.
	 *
	 * @return the open germplasm url
	 */
	public String getOpenGermplasmUrl() {
		return openGermplasmUrl;
	}

	/**
	 * Sets the open germplasm url.
	 *
	 * @param openGermplasmUrl the new open germplasm url
	 */
	public void setOpenGermplasmUrl(String openGermplasmUrl) {
		this.openGermplasmUrl = openGermplasmUrl;
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
         * Gets the selection variates variables.
         *
         * @return the selectionVariatesVariables
         */
        public List<SettingDetail> getSelectionVariatesVariables() {
            return selectionVariatesVariables;
        }

        /**
         * Sets the selection variates variables.
         *
         * @param selectionVariatesVariables the selectionVariatesVariables to set
         */
        public void setSelectionVariatesVariables(
                List<SettingDetail> selectionVariatesVariables) {
            this.selectionVariatesVariables = selectionVariatesVariables;
        }

        /**
         * Gets the id name variables.
         *
         * @return the idNameVariables
         */
        public String getIdNameVariables() {
            return idNameVariables;
        }

        /**
         * Sets the id name variables.
         *
         * @param idNameVariables the idNameVariables to set
         */
        public void setIdNameVariables(String idNameVariables) {
            this.idNameVariables = idNameVariables;
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
         * Gets the measurement variables.
         *
         * @return the measurementVariables
         */
        public List<MeasurementVariable> getMeasurementVariables() {
            return measurementVariables;
        }

        /**
         * Sets the measurement variables.
         *
         * @param measurementVariables the measurementVariables to set
         */
        public void setMeasurementVariables(List<MeasurementVariable> measurementVariables) {
            this.measurementVariables = measurementVariables;
        }

        /**
         * Gets the study name.
         *
         * @return the studyName
         */
        public String getStudyName() {
            return studyName;
        }

        /**
         * Sets the study name.
         *
         * @param studyName the studyName to set
         */
        public void setStudyName(String studyName) {
            this.studyName = studyName;
        }
        
        /**
         * Change page.
         *
         * @param currentPage the current page
         */
        public void changePage(int currentPage){
                if(measurementRowList != null && !measurementRowList.isEmpty()){
            int totalItemsPerPage = getResultPerPage();
            int start = (currentPage - 1) * totalItemsPerPage;
            int end = start + totalItemsPerPage;
            if(measurementRowList.size() < end){
                end = measurementRowList.size();
            }
            setPaginatedMeasurementRowList(measurementRowList.subList(start, end));
            this.setCurrentPage(currentPage);
        }else{
            this.setCurrentPage(0);
        }
        }

        /**
         * Gets the paginated measurement row list.
         *
         * @return the paginatedMeasurementRowList
         */
        public List<MeasurementRow> getPaginatedMeasurementRowList() {
            return paginatedMeasurementRowList;
        }

        /**
         * Sets the paginated measurement row list.
         *
         * @param paginatedMeasurementRowList the paginatedMeasurementRowList to set
         */
        public void setPaginatedMeasurementRowList(
                List<MeasurementRow> paginatedMeasurementRowList) {
            this.paginatedMeasurementRowList = paginatedMeasurementRowList;
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
         * Gets the total pages.
         *
         * @return the totalPages
         */
        public int getTotalPages() {
            if(measurementRowList != null && !measurementRowList.isEmpty()){           
                totalPages = (int) Math.ceil((measurementRowList.size() * 1f) / getResultPerPage()); 
            }else{
                totalPages = 0;
            }
            return totalPages;
        }

        /**
         * Sets the total pages.
         *
         * @param totalPages the totalPages to set
         */
        public void setTotalPages(int totalPages) {
            this.totalPages = totalPages;
        }

        /**
         * Gets the result per page.
         *
         * @return the resultPerPage
         */
        public int getResultPerPage() {
            return resultPerPage;
        }

        /**
         * Sets the result per page.
         *
         * @param resultPerPage the resultPerPage to set
         */
        public void setResultPerPage(int resultPerPage) {
            this.resultPerPage = resultPerPage;
        }

        /**
         * Gets the file.
         *
         * @return the file
         */
        public MultipartFile getFile() {
            return file;
        }

        /**
         * Sets the file.
         *
         * @param file the file to set
         */
        public void setFile(MultipartFile file) {
            this.file = file;
        }

        /**
         * Gets the checks for error.
         *
         * @return the hasError
         */
        public String getHasError() {
            return hasError;
        }

        /**
         * Sets the checks for error.
         *
         * @param hasError the hasError to set
         */
        public void setHasError(String hasError) {
            this.hasError = hasError;
        }

        /**
         * Gets the import val.
         *
         * @return the importVal
         */
        public int getImportVal() {
            return importVal;
        }

        /**
         * Sets the import val.
         *
         * @param importVal the importVal to set
         */
        public void setImportVal(int importVal) {
            this.importVal = importVal;
        }

        /**
         * Gets the number of instances.
         *
         * @return the numberOfInstances
         */
        public int getNumberOfInstances() {
            return numberOfInstances;
        }

        /**
         * Sets the number of instances.
         *
         * @param numberOfInstances the numberOfInstances to set
         */
        public void setNumberOfInstances(int numberOfInstances) {
            this.numberOfInstances = numberOfInstances;
        }

        /**
         * Gets the export instance type.
         *
         * @return the exportInstanceType
         */
        public String getExportInstanceType() {
            return exportInstanceType;
        }

        /**
         * Sets the export instance type.
         *
         * @param exportInstanceType the exportInstanceType to set
         */
        public void setExportInstanceType(String exportInstanceType) {
            this.exportInstanceType = exportInstanceType;
        }

        /**
         * Gets the export trial instance number.
         *
         * @return the exportTrialInstanceNumber
         */
        public String getExportTrialInstanceNumber() {
            return exportTrialInstanceNumber;
        }

        /**
         * Sets the export trial instance number.
         *
         * @param exportTrialInstanceNumber the exportTrialInstanceNumber to set
         */
        public void setExportTrialInstanceNumber(
                String exportTrialInstanceNumber) {
            this.exportTrialInstanceNumber = exportTrialInstanceNumber;
        }

        /**
         * Gets the export trial instance start.
         *
         * @return the exportTrialInstanceStart
         */
        public String getExportTrialInstanceStart() {
            return exportTrialInstanceStart;
        }

        /**
         * Sets the export trial instance start.
         *
         * @param exportTrialInstanceStart the exportTrialInstanceStart to set
         */
        public void setExportTrialInstanceStart(String exportTrialInstanceStart) {
            this.exportTrialInstanceStart = exportTrialInstanceStart;
        }

        /**
         * Gets the export trial instance end.
         *
         * @return the exportTrialInstanceEnd
         */
        public String getExportTrialInstanceEnd() {
            return exportTrialInstanceEnd;
        }

        /**
         * Sets the export trial instance end.
         *
         * @param exportTrialInstanceEnd the exportTrialInstanceEnd to set
         */
        public void setExportTrialInstanceEnd(String exportTrialInstanceEnd) {
            this.exportTrialInstanceEnd = exportTrialInstanceEnd;
        }

		/**
		 * Gets the study id.
		 *
		 * @return the study id
		 */
		public Integer getStudyId() {
			return studyId;
		}

		/**
		 * Sets the study id.
		 *
		 * @param studyId the new study id
		 */
		public void setStudyId(Integer studyId) {
			this.studyId = studyId;
		}

		/**
		 * Gets the trial environment values.
		 *
		 * @return the trial environment values
		 */
		public List<List<ValueReference>> getTrialEnvironmentValues() {
			return trialEnvironmentValues;
		}

		/**
		 * Sets the trial environment values.
		 *
		 * @param trialEnvironmentValues the new trial environment values
		 */
		public void setTrialEnvironmentValues(
				List<List<ValueReference>> trialEnvironmentValues) {
			this.trialEnvironmentValues = trialEnvironmentValues;
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
         * Gets the baseline traits segment.
         *
         * @return the baselineTraitsSegment
         */
        public String getBaselineTraitsSegment() {
            return baselineTraitsSegment;
        }

        /**
         * Sets the baseline traits segment.
         *
         * @param baselineTraitsSegment the baselineTraitsSegment to set
         */
        public void setBaselineTraitsSegment(String baselineTraitsSegment) {
            this.baselineTraitsSegment = baselineTraitsSegment;
        }

        /**
         * Gets the selection variates segment.
         *
         * @return the selectionVariatesSegment
         */
        public String getSelectionVariatesSegment() {
            return selectionVariatesSegment;
        }

        /**
         * Sets the selection variates segment.
         *
         * @param selectionVariatesSegment the selectionVariatesSegment to set
         */
        public void setSelectionVariatesSegment(String selectionVariatesSegment) {
            this.selectionVariatesSegment = selectionVariatesSegment;
        }

		public List<MeasurementVariable> getArrangeMeasurementVariables(){
			List<MeasurementVariable> measureList = new ArrayList<MeasurementVariable>();
			List<MeasurementVariable> newMeasureList = new ArrayList<MeasurementVariable>();
			if(getMeasurementVariables() != null && !getMeasurementVariables().isEmpty()){
				//we arrange and make GID and Designation always first
				MeasurementVariable gidVariable = null;
				MeasurementVariable desigVariable = null;
				for(MeasurementVariable var : getMeasurementVariables()){
					measureList.add(var);
					if(var.getTermId() == TermId.GID.getId()){
						gidVariable = var;
					}else if(var.getTermId() == TermId.DESIG.getId()){
						desigVariable = var;
					}
				}
				if(!measureList.isEmpty()){
					if(gidVariable != null) {
						measureList.remove(gidVariable);
					}
					if(desigVariable != null) {
						measureList.remove(desigVariable);
					}
				}
				newMeasureList.add(gidVariable);
				newMeasureList.add(desigVariable);
				newMeasureList.addAll(measureList);
			}
			return newMeasureList;
		}
		
        public boolean isMeasurementDataExisting() {
            return isMeasurementDataExisting;
        }

        public void setMeasurementDataExisting(boolean isMeasurementDataExisting) {
            this.isMeasurementDataExisting = isMeasurementDataExisting;
        }

        public int getCharLimit() {
            return charLimit;
        }

        public void setCharLimit(int charLimit) {
            this.charLimit = charLimit;
        }

        
}
