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

import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.web.common.bean.SettingDetail;
import com.efficio.fieldbook.web.common.bean.TreatmentFactorDetail;

/**
 * The Class CreateNurseryForm.
 */
public class CreateTrialForm {

	/**
	 * The nursery level variables.
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

	/**
	 * The trial environment values.
	 */
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

	private String startDate;

	private String endDate;

	private String studyUpdate;

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
}
