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

package com.efficio.fieldbook.web.nursery.form;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.SettingVariable;

/**
 * The Class CreateNurseryForm.
 */
public class CreateNurseryForm {
	
	private String experimentTypeId;

	/** The project id. */
	private String projectId;

	/** The selected setting id. */
	private int selectedSettingId;

	/** The study level variables. */
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

	/** The folder name label. */
	private String folderNameLabel;

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

	/** The open germplasm url. */
	private String openGermplasmUrl;

	/** The load settings. */
	private String loadSettings;

	/** The tree data. */
	private String treeData;

	// convert to json 1 level for the property and standard variable
	/** The search tree data. */
	private String searchTreeData;

	/** The selected variables. */
	private List<SettingVariable> selectedVariables;

	/** The basic details. */
	private List<SettingDetail> basicDetails;

	/** The study conditions. */
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

	/** The is measurement data existing. */
	private boolean measurementDataExisting;

	/** The char limit. */
	private int charLimit;

	/** The name type. */
	private int nameType;

	/** The import date. */
	private String importDate;

	/** The import method id. */
	private int importMethodId;

	/** The import location id. */
	private int importLocationId;

	/** The created by. */
	private String createdBy;

	/** The breeding method code. */
	private String breedingMethodCode;

	/** The has fieldmap. */
	private boolean hasFieldmap;

	/** The error message. */
	private String errorMessage;

	private String columnOrders;

	private Integer germplasmListId;

	private String description;

	private String startDate;

	private String endDate;

	private String studyUpdate;

	private String objective;

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * Gets the project id.
	 * 
	 * @return the projectId
	 */
	public String getProjectId() {
		return this.projectId;
	}

	/**
	 * Sets the project id.
	 * 
	 * @param projectId the projectId to set
	 */
	public void setProjectId(final String projectId) {
		this.projectId = projectId;
	}

	/**
	 * Gets the selected setting id.
	 * 
	 * @return the selectedSettingId
	 */
	public int getSelectedSettingId() {
		return this.selectedSettingId;
	}

	/**
	 * Sets the selected setting id.
	 * 
	 * @param selectedSettingId the selectedSettingId to set
	 */
	public void setSelectedSettingId(final int selectedSettingId) {
		this.selectedSettingId = selectedSettingId;
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
	public void setStudyLevelVariables(final List<SettingDetail> studyLevelVariables) {
		this.studyLevelVariables = studyLevelVariables;
	}

	/**
	 * Gets the plot level variables.
	 * 
	 * @return the plotLevelVariables
	 */
	public List<SettingDetail> getPlotLevelVariables() {
		return this.plotLevelVariables;
	}

	/**
	 * Sets the plot level variables.
	 * 
	 * @param plotLevelVariables the plotLevelVariables to set
	 */
	public void setPlotLevelVariables(final List<SettingDetail> plotLevelVariables) {
		this.plotLevelVariables = plotLevelVariables;
	}

	/**
	 * Gets the baseline trait variables.
	 * 
	 * @return the baselineTraitVariables
	 */
	public List<SettingDetail> getBaselineTraitVariables() {
		return this.baselineTraitVariables;
	}

	/**
	 * Sets the baseline trait variables.
	 * 
	 * @param baselineTraitVariables the baselineTraitVariables to set
	 */
	public void setBaselineTraitVariables(final List<SettingDetail> baselineTraitVariables) {
		this.baselineTraitVariables = baselineTraitVariables;
	}

	/**
	 * Gets the folder id.
	 * 
	 * @return the folderId
	 */
	public Integer getFolderId() {
		return this.folderId;
	}

	/**
	 * Sets the folder id.
	 * 
	 * @param folderId the folderId to set
	 */
	public void setFolderId(final Integer folderId) {
		this.folderId = folderId;
	}

	/**
	 * Gets the folder name.
	 * 
	 * @return the folderName
	 */
	public String getFolderName() {
		return this.folderName;
	}

	/**
	 * Sets the folder name.
	 * 
	 * @param folderName the folderName to set
	 */
	public void setFolderName(final String folderName) {
		this.folderName = folderName;
	}

	/**
	 * Checks if is field layout random.
	 * 
	 * @return the fieldLayoutRandom
	 */
	public boolean isFieldLayoutRandom() {
		return this.fieldLayoutRandom;
	}

	/**
	 * Sets the field layout random.
	 * 
	 * @param fieldLayoutRandom the fieldLayoutRandom to set
	 */
	public void setFieldLayoutRandom(final boolean fieldLayoutRandom) {
		this.fieldLayoutRandom = fieldLayoutRandom;
	}

	/**
	 * Gets the required fields.
	 * 
	 * @return the required fields
	 */
	public String getRequiredFields() {
		return this.requiredFields;
	}

	/**
	 * Sets the required fields.
	 * 
	 * @param requiredFields the new required fields
	 */
	public void setRequiredFields(final String requiredFields) {
		this.requiredFields = requiredFields;
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
	public void setLocationId(final String locationId) {
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
	public void setBreedingMethodId(final String breedingMethodId) {
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
	public void setLocationUrl(final String locationUrl) {
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
	public void setBreedingMethodUrl(final String breedingMethodUrl) {
		this.breedingMethodUrl = breedingMethodUrl;
	}

	/**
	 * Gets the load settings.
	 * 
	 * @return the load settings
	 */
	public String getLoadSettings() {
		return this.loadSettings;
	}

	/**
	 * Sets the load settings.
	 * 
	 * @param loadSettings the new load settings
	 */
	public void setLoadSettings(final String loadSettings) {
		this.loadSettings = loadSettings;
	}

	/**
	 * Gets the open germplasm url.
	 * 
	 * @return the open germplasm url
	 */
	public String getOpenGermplasmUrl() {
		return this.openGermplasmUrl;
	}

	/**
	 * Sets the open germplasm url.
	 * 
	 * @param openGermplasmUrl the new open germplasm url
	 */
	public void setOpenGermplasmUrl(final String openGermplasmUrl) {
		this.openGermplasmUrl = openGermplasmUrl;
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
	public void setTreeData(final String treeData) {
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
	public void setSearchTreeData(final String searchTreeData) {
		this.searchTreeData = searchTreeData;
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
	public void setSelectedVariables(final List<SettingVariable> selectedVariables) {
		this.selectedVariables = selectedVariables;
	}

	/**
	 * Gets the basic details.
	 * 
	 * @return the basicDetails
	 */
	public List<SettingDetail> getBasicDetails() {
		return this.basicDetails;
	}

	/**
	 * Sets the basic details.
	 * 
	 * @param basicDetails the basicDetails to set
	 */
	public void setBasicDetails(final List<SettingDetail> basicDetails) {
		this.basicDetails = basicDetails;
	}

	/**
	 * Gets the selection variates variables.
	 * 
	 * @return the selectionVariatesVariables
	 */
	public List<SettingDetail> getSelectionVariatesVariables() {
		return this.selectionVariatesVariables;
	}

	/**
	 * Sets the selection variates variables.
	 * 
	 * @param selectionVariatesVariables the selectionVariatesVariables to set
	 */
	public void setSelectionVariatesVariables(final List<SettingDetail> selectionVariatesVariables) {
		this.selectionVariatesVariables = selectionVariatesVariables;
	}

	/**
	 * Gets the id name variables.
	 * 
	 * @return the idNameVariables
	 */
	public String getIdNameVariables() {
		return this.idNameVariables;
	}

	/**
	 * Sets the id name variables.
	 * 
	 * @param idNameVariables the idNameVariables to set
	 */
	public void setIdNameVariables(final String idNameVariables) {
		this.idNameVariables = idNameVariables;
	}

	/**
	 * Gets the study conditions.
	 * 
	 * @return the studyConditions
	 */
	public List<SettingDetail> getNurseryConditions() {
		return this.nurseryConditions;
	}

	/**
	 * Sets the study conditions.
	 * 
	 * @param studyConditions the nurseryConditions to set
	 */
	public void setNurseryConditions(final List<SettingDetail> nurseryConditions) {
		this.nurseryConditions = nurseryConditions;
	}

	/**
	 * Gets the measurement row list.
	 * 
	 * @return the measurementRowList
	 */
	public List<MeasurementRow> getMeasurementRowList() {
		return this.measurementRowList;
	}

	/**
	 * Sets the measurement row list.
	 * 
	 * @param measurementRowList the measurementRowList to set
	 */
	public void setMeasurementRowList(final List<MeasurementRow> measurementRowList) {
		this.measurementRowList = measurementRowList;
	}

	/**
	 * Gets the measurement variables.
	 * 
	 * @return the measurementVariables
	 */
	public List<MeasurementVariable> getMeasurementVariables() {
		return this.measurementVariables;
	}

	/**
	 * Sets the measurement variables.
	 * 
	 * @param measurementVariables the measurementVariables to set
	 */
	public void setMeasurementVariables(final List<MeasurementVariable> measurementVariables) {
		this.measurementVariables = measurementVariables;
	}

	/**
	 * Gets the study name.
	 * 
	 * @return the studyName
	 */
	public String getStudyName() {
		return this.studyName;
	}

	/**
	 * Sets the study name.
	 * 
	 * @param studyName the studyName to set
	 */
	public void setStudyName(final String studyName) {
		this.studyName = studyName;
	}

	/**
	 * Change page.
	 * 
	 * @param currentPage the current page
	 */
	public void changePage(final int currentPage) {
		if (this.measurementRowList != null && !this.measurementRowList.isEmpty()) {
			final int totalItemsPerPage = this.getResultPerPage();
			final int start = (currentPage - 1) * totalItemsPerPage;
			int end = start + totalItemsPerPage;
			if (this.measurementRowList.size() < end) {
				end = this.measurementRowList.size();
			}
			this.setPaginatedMeasurementRowList(this.measurementRowList.subList(start, end));
			this.setCurrentPage(currentPage);
		} else {
			this.setCurrentPage(0);
		}
	}

	/**
	 * Gets the paginated measurement row list.
	 * 
	 * @return the paginatedMeasurementRowList
	 */
	public List<MeasurementRow> getPaginatedMeasurementRowList() {
		return this.paginatedMeasurementRowList;
	}

	/**
	 * Sets the paginated measurement row list.
	 * 
	 * @param paginatedMeasurementRowList the paginatedMeasurementRowList to set
	 */
	public void setPaginatedMeasurementRowList(final List<MeasurementRow> paginatedMeasurementRowList) {
		this.paginatedMeasurementRowList = paginatedMeasurementRowList;
	}

	/**
	 * Gets the current page.
	 * 
	 * @return the currentPage
	 */
	public int getCurrentPage() {
		return this.currentPage;
	}

	/**
	 * Sets the current page.
	 * 
	 * @param currentPage the currentPage to set
	 */
	public void setCurrentPage(final int currentPage) {
		this.currentPage = currentPage;
	}

	/**
	 * Gets the total pages.
	 * 
	 * @return the totalPages
	 */
	public int getTotalPages() {
		if (this.measurementRowList != null && !this.measurementRowList.isEmpty()) {
			this.totalPages = (int) Math.ceil(this.measurementRowList.size() * 1f / this.getResultPerPage());
		} else {
			this.totalPages = 0;
		}
		return this.totalPages;
	}

	/**
	 * Sets the total pages.
	 * 
	 * @param totalPages the totalPages to set
	 */
	public void setTotalPages(final int totalPages) {
		this.totalPages = totalPages;
	}

	/**
	 * Gets the result per page.
	 * 
	 * @return the resultPerPage
	 */
	public int getResultPerPage() {
		return this.resultPerPage;
	}

	/**
	 * Sets the result per page.
	 * 
	 * @param resultPerPage the resultPerPage to set
	 */
	public void setResultPerPage(final int resultPerPage) {
		this.resultPerPage = resultPerPage;
	}

	/**
	 * Gets the file.
	 * 
	 * @return the file
	 */
	public MultipartFile getFile() {
		return this.file;
	}

	/**
	 * Sets the file.
	 * 
	 * @param file the file to set
	 */
	public void setFile(final MultipartFile file) {
		this.file = file;
	}

	/**
	 * Gets the checks for error.
	 * 
	 * @return the hasError
	 */
	public String getHasError() {
		return this.hasError;
	}

	/**
	 * Sets the checks for error.
	 * 
	 * @param hasError the hasError to set
	 */
	public void setHasError(final String hasError) {
		this.hasError = hasError;
	}

	/**
	 * Gets the import val.
	 * 
	 * @return the importVal
	 */
	public int getImportVal() {
		return this.importVal;
	}

	/**
	 * Sets the import val.
	 * 
	 * @param importVal the importVal to set
	 */
	public void setImportVal(final int importVal) {
		this.importVal = importVal;
	}

	/**
	 * Gets the number of instances.
	 * 
	 * @return the numberOfInstances
	 */
	public int getNumberOfInstances() {
		return this.numberOfInstances;
	}

	/**
	 * Sets the number of instances.
	 * 
	 * @param numberOfInstances the numberOfInstances to set
	 */
	public void setNumberOfInstances(final int numberOfInstances) {
		this.numberOfInstances = numberOfInstances;
	}

	/**
	 * Gets the export instance type.
	 * 
	 * @return the exportInstanceType
	 */
	public String getExportInstanceType() {
		return this.exportInstanceType;
	}

	/**
	 * Sets the export instance type.
	 * 
	 * @param exportInstanceType the exportInstanceType to set
	 */
	public void setExportInstanceType(final String exportInstanceType) {
		this.exportInstanceType = exportInstanceType;
	}

	/**
	 * Gets the export trial instance number.
	 * 
	 * @return the exportTrialInstanceNumber
	 */
	public String getExportTrialInstanceNumber() {
		return this.exportTrialInstanceNumber;
	}

	/**
	 * Sets the export trial instance number.
	 * 
	 * @param exportTrialInstanceNumber the exportTrialInstanceNumber to set
	 */
	public void setExportTrialInstanceNumber(final String exportTrialInstanceNumber) {
		this.exportTrialInstanceNumber = exportTrialInstanceNumber;
	}

	/**
	 * Gets the export trial instance start.
	 * 
	 * @return the exportTrialInstanceStart
	 */
	public String getExportTrialInstanceStart() {
		return this.exportTrialInstanceStart;
	}

	/**
	 * Sets the export trial instance start.
	 * 
	 * @param exportTrialInstanceStart the exportTrialInstanceStart to set
	 */
	public void setExportTrialInstanceStart(final String exportTrialInstanceStart) {
		this.exportTrialInstanceStart = exportTrialInstanceStart;
	}

	/**
	 * Gets the export trial instance end.
	 * 
	 * @return the exportTrialInstanceEnd
	 */
	public String getExportTrialInstanceEnd() {
		return this.exportTrialInstanceEnd;
	}

	/**
	 * Sets the export trial instance end.
	 * 
	 * @param exportTrialInstanceEnd the exportTrialInstanceEnd to set
	 */
	public void setExportTrialInstanceEnd(final String exportTrialInstanceEnd) {
		this.exportTrialInstanceEnd = exportTrialInstanceEnd;
	}

	/**
	 * Gets the study id.
	 * 
	 * @return the study id
	 */
	public Integer getStudyId() {
		return this.studyId;
	}

	/**
	 * Sets the study id.
	 * 
	 * @param studyId the new study id
	 */
	public void setStudyId(final Integer studyId) {
		this.studyId = studyId;
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
	public void setTrialEnvironmentValues(final List<List<ValueReference>> trialEnvironmentValues) {
		this.trialEnvironmentValues = trialEnvironmentValues;
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
	public void setTrialLevelVariables(final List<SettingDetail> trialLevelVariables) {
		this.trialLevelVariables = trialLevelVariables;
	}

	/**
	 * Gets the baseline traits segment.
	 * 
	 * @return the baselineTraitsSegment
	 */
	public String getBaselineTraitsSegment() {
		return this.baselineTraitsSegment;
	}

	/**
	 * Sets the baseline traits segment.
	 * 
	 * @param baselineTraitsSegment the baselineTraitsSegment to set
	 */
	public void setBaselineTraitsSegment(final String baselineTraitsSegment) {
		this.baselineTraitsSegment = baselineTraitsSegment;
	}

	/**
	 * Gets the selection variates segment.
	 * 
	 * @return the selectionVariatesSegment
	 */
	public String getSelectionVariatesSegment() {
		return this.selectionVariatesSegment;
	}

	/**
	 * Sets the selection variates segment.
	 * 
	 * @param selectionVariatesSegment the selectionVariatesSegment to set
	 */
	public void setSelectionVariatesSegment(final String selectionVariatesSegment) {
		this.selectionVariatesSegment = selectionVariatesSegment;
	}

	/**
	 * Gets the arrange measurement variables.
	 * 
	 * @return the arrange measurement variables
	 */
	public List<MeasurementVariable> getArrangeMeasurementVariables() {
		return this.getMeasurementVariables() != null ? this.getMeasurementVariables() : new ArrayList<MeasurementVariable>();
	}

	/**
	 * Checks if is measurement data existing.
	 * 
	 * @return true, if is measurement data existing
	 */
	public boolean isMeasurementDataExisting() {
		return this.measurementDataExisting;
	}

	/**
	 * Sets the measurement data existing.
	 * 
	 * @param isMeasurementDataExisting the new measurement data existing
	 */
	public void setMeasurementDataExisting(final boolean isMeasurementDataExisting) {
		this.measurementDataExisting = isMeasurementDataExisting;
	}

	/**
	 * Gets the char limit.
	 * 
	 * @return the char limit
	 */
	public int getCharLimit() {
		return this.charLimit;
	}

	/**
	 * Sets the char limit.
	 * 
	 * @param charLimit the new char limit
	 */
	public void setCharLimit(final int charLimit) {
		this.charLimit = charLimit;
	}

	/**
	 * Gets the folder name label.
	 * 
	 * @return the folder name label
	 */
	public String getFolderNameLabel() {
		return this.folderNameLabel;
	}

	/**
	 * Sets the folder name label.
	 * 
	 * @param folderNameLabel the new folder name label
	 */
	public void setFolderNameLabel(final String folderNameLabel) {
		this.folderNameLabel = folderNameLabel;
	}

	/**
	 * Gets the name type.
	 * 
	 * @return the name type
	 */
	public int getNameType() {
		return this.nameType;
	}

	/**
	 * Sets the name type.
	 * 
	 * @param nameType the new name type
	 */
	public void setNameType(final int nameType) {
		this.nameType = nameType;
	}

	/**
	 * Gets the import method id.
	 * 
	 * @return the import method id
	 */
	public int getImportMethodId() {
		return this.importMethodId;
	}

	/**
	 * Sets the import method id.
	 * 
	 * @param importMethodId the new import method id
	 */
	public void setImportMethodId(final int importMethodId) {
		this.importMethodId = importMethodId;
	}

	/**
	 * Gets the import location id.
	 * 
	 * @return the import location id
	 */
	public int getImportLocationId() {
		return this.importLocationId;
	}

	/**
	 * Sets the import location id.
	 * 
	 * @param importLocationId the new import location id
	 */
	public void setImportLocationId(final int importLocationId) {
		this.importLocationId = importLocationId;
	}

	/**
	 * Gets the import date.
	 * 
	 * @return the import date
	 */
	public String getImportDate() {
		return this.importDate;
	}

	/**
	 * Sets the import date.
	 * 
	 * @param importDate the new import date
	 */
	public void setImportDate(final String importDate) {
		this.importDate = importDate;
	}

	/**
	 * Gets the created by.
	 * 
	 * @return the created by
	 */
	public String getCreatedBy() {
		return this.createdBy;
	}

	/**
	 * Sets the created by.
	 * 
	 * @param createdBy the new created by
	 */
	public void setCreatedBy(final String createdBy) {
		this.createdBy = createdBy;
	}

	/**
	 * Gets the breeding method code.
	 * 
	 * @return the breeding method code
	 */
	public String getBreedingMethodCode() {
		return this.breedingMethodCode;
	}

	/**
	 * Sets the breeding method code.
	 * 
	 * @param breedingMethodCode the new breeding method code
	 */
	public void setBreedingMethodCode(final String breedingMethodCode) {
		this.breedingMethodCode = breedingMethodCode;
	}

	/**
	 * Checks if is checks for fieldmap.
	 * 
	 * @return true, if is checks for fieldmap
	 */
	public boolean isHasFieldmap() {
		return this.hasFieldmap;
	}

	/**
	 * Sets the checks for fieldmap.
	 * 
	 * @param hasFieldmap the new checks for fieldmap
	 */
	public void setHasFieldmap(final boolean hasFieldmap) {
		this.hasFieldmap = hasFieldmap;
	}

	/**
	 * Gets the error message.
	 * 
	 * @return the error message
	 */
	public String getErrorMessage() {
		return this.errorMessage;
	}

	/**
	 * Sets the error message.
	 * 
	 * @param errorMessage the new error message
	 */
	public void setErrorMessage(final String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getColumnOrders() {
		return this.columnOrders;
	}

	public void setColumnOrders(final String columnOrders) {
		this.columnOrders = columnOrders;
	}

	public String getExperimentTypeId() {
		return this.experimentTypeId;
	}

	public void setExperimentTypeId(final String experimentTypeId) {
		this.experimentTypeId = experimentTypeId;
	}

	public void setGermplasmListId(final Integer germplasmListId) {
		this.germplasmListId = germplasmListId;
	}

	public Integer getGermplasmListId() {
		return germplasmListId;
	}

	public String getStartDate() {
		return this.startDate;
	}

	public void setStartDate(final String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return this.endDate;
	}

	public void setEndDate(final String endDate) {
		this.endDate = endDate;
	}

	public String getStudyUpdate() {
		return this.studyUpdate;
	}

	public void setStudyUpdate(final String studyUpdate) {
		this.studyUpdate = studyUpdate;
	}

	public String getObjective() {
		return objective;
	}

	public void setObjective(final String objective) {
		this.objective = objective;
	}
}
