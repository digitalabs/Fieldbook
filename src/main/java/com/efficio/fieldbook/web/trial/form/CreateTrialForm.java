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

import java.util.List;

import com.efficio.fieldbook.web.common.bean.SettingVariable;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.TreatmentFactorDetail;

/**
 * The Class CreateTrialForm.
 */
public class CreateTrialForm {

	/**
	 * The study level variables.
	 */
	private List<SettingDetail> studyLevelVariables;

	/**
	 * The plot level variables.
	 */
	private List<SettingDetail> plotLevelVariables;

	/**
	 * The baseline trait variables.
	 *
	 * Dan V - maps to items added in the measurements tab
	 */
	private List<SettingDetail> baselineTraitVariables;

	/**
	 * The trial level variables.
	 */
	private List<SettingDetail> trialLevelVariables;

	/**
	 * The treatment factors.
	 */
	private List<TreatmentFactorDetail> treatmentFactors;

	/**
	 * The folder id.
	 */
	private Integer folderId;

	/**
	 * The folder name.
	 */
	private String folderName;

	/**
	 * The folder name label.
	 */
	private String folderNameLabel;

	/**
	 * The required fields.
	 */
	private String requiredFields;

	/**
	 * The trial instances.
	 */
	private int trialInstances;

	/** The trial environment values.*/
	private List<List<ValueReference>> trialEnvironmentValues;

	/** The measurement variables. */
	private List<MeasurementVariable> measurementVariables;

	/** The is measurement data existing. */
	private boolean isMeasurementDataExisting;

	/** The study id. */
	private Integer studyId;

	/** The name type. */
	private int nameType;

	/** The import date. */
	private String importDate;

	/** The import method id. */
	private int importMethodId;

	/** The import location id. */
	private int importLocationId;

	/** The file. */
	private MultipartFile file;

	/** The has error. */
	private boolean hasError;

	/** The error message. */
	private String errorMessage;

	private String description;

	private String columnOrders;

	/** The Id of Germplasm List. */
	private Integer germplasmListId;

	/** The study type name. */
	private String studyTypeName;

	private String startDate;

	private String endDate;

	private String studyUpdate;

	/** The measurement row list. */
	private List<MeasurementRow> measurementRowList;

	/** The current page. */
	private int currentPage;

	/** The total pages. */
	private int totalPages;

	/** The result per page. */
	private int resultPerPage = 100;
	
	/** The paginated imported germplasm. */
	private List<MeasurementRow> paginatedMeasurementRowList;

	/** The created by. */
	private String createdBy;

	/** The study name. */
	private String studyName;

	/** The selected variables. */
	private List<SettingVariable> selectedVariables;

	/** The basic details. */
	private List<SettingDetail> basicDetails;

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

	public String getDescription() {
		return description;
	}

	public void setDescription(final String description) {
		this.description = description;
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
	public void setPlotLevelVariables(List<SettingDetail> plotLevelVariables) {
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
	public void setBaselineTraitVariables(List<SettingDetail> baselineTraitVariables) {
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
	public void setFolderId(Integer folderId) {
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
	public void setFolderName(String folderName) {
		this.folderName = folderName;
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
	public void setRequiredFields(String requiredFields) {
		this.requiredFields = requiredFields;
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
	 * Gets the treatment factors.
	 *
	 * @return the treatmentFactors
	 */
	public List<TreatmentFactorDetail> getTreatmentFactors() {
		return this.treatmentFactors;
	}

	/**
	 * Sets the treatment factors.
	 *
	 * @param treatmentFactors the treatmentFactors to set
	 */
	public void setTreatmentFactors(List<TreatmentFactorDetail> treatmentFactors) {
		this.treatmentFactors = treatmentFactors;
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
	public void setFolderNameLabel(String folderNameLabel) {
		this.folderNameLabel = folderNameLabel;
	}

	/**
	 * Gets the measurement variables.
	 *
	 * @return the measurement variables
	 */
	public List<MeasurementVariable> getMeasurementVariables() {
		return this.measurementVariables;
	}

	/**
	 * Sets the measurement variables.
	 *
	 * @param measurementVariables the new measurement variables
	 */
	public void setMeasurementVariables(List<MeasurementVariable> measurementVariables) {
		this.measurementVariables = measurementVariables;
	}

	/**
	 * Checks if is measurement data existing.
	 *
	 * @return true, if is measurement data existing
	 */
	public boolean isMeasurementDataExisting() {
		return this.isMeasurementDataExisting;
	}

	/**
	 * Sets the measurement data existing.
	 *
	 * @param isMeasurementDataExisting the new measurement data existing
	 */
	public void setMeasurementDataExisting(boolean isMeasurementDataExisting) {
		this.isMeasurementDataExisting = isMeasurementDataExisting;
	}

	/**
	 * Gets the arrange measurement variables.
	 *
	 * @return the arrange measurement variables
	 */
	public List<MeasurementVariable> getArrangeMeasurementVariables() {
		return this.getMeasurementVariables();
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
	public void setStudyId(Integer studyId) {
		this.studyId = studyId;
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
	public void setNameType(int nameType) {
		this.nameType = nameType;
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
	public void setImportDate(String importDate) {
		this.importDate = importDate;
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
	public void setImportMethodId(int importMethodId) {
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
	public void setImportLocationId(int importLocationId) {
		this.importLocationId = importLocationId;
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
	 * @param file the new file
	 */
	public void setFile(MultipartFile file) {
		this.file = file;
	}

	/**
	 * Checks if is checks for error.
	 *
	 * @return true, if is checks for error
	 */
	public boolean isHasError() {
		return this.hasError;
	}

	/**
	 * Sets the checks for error.
	 *
	 * @param hasError the new checks for error
	 */
	public void setHasError(boolean hasError) {
		this.hasError = hasError;
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
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public Integer getGermplasmListId() {
		return germplasmListId;
	}

	public void setGermplasmListId(Integer germplasmListId) {
		this.germplasmListId = germplasmListId;
	}

	public String getStudyTypeName() {
		return studyTypeName;
	}

	public void setStudyTypeName(String studyTypeName) {
		this.studyTypeName = studyTypeName;
	}

	public String getColumnOrders() {
		return columnOrders;
	}

	public void setColumnOrders(final String columnOrders) {
		this.columnOrders = columnOrders;
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
	 * Sets the result per page.
	 *
	 * @param resultPerPage the resultPerPage to set
	 */
	public void setResultPerPage(final int resultPerPage) {
		this.resultPerPage = resultPerPage;
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

}
