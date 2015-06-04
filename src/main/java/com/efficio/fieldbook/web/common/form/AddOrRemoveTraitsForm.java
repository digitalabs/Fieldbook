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

package com.efficio.fieldbook.web.common.form;

import java.util.List;

import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.web.common.bean.SettingDetail;

/**
 * The Class AddOrRemoveTraitsForm.
 */
public class AddOrRemoveTraitsForm {

	/** The measurement row list. */
	private List<MeasurementRow> measurementRowList;

	/** The measurement variables. */
	private List<MeasurementVariable> measurementVariables;

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

	/** The study name. */
	private String studyName;

	/** The export instance type. */
	private String exportInstanceType;

	/** The export trial instance start. */
	private String exportTrialInstanceNumber;
	/** The export trial instance start. */
	private String exportTrialInstanceStart;

	/** The export trial instance end. */
	private String exportTrialInstanceEnd;

	/** The number of instances. */
	private int numberOfInstances;

	/** The trial environment values. */
	private List<List<ValueReference>> trialEnvironmentValues;

	/** The trial level variables. */
	private List<SettingDetail> trialLevelVariables;

	/** The location id. */
	private String locationId;
	/** The location url. */
	private String locationUrl;

	private MeasurementRow updateObservation;
	private Integer experimentIndex;

	/**
	 * Gets the study name.
	 *
	 * @return the study name
	 */
	public String getStudyName() {
		return this.studyName;
	}

	/**
	 * Sets the study name.
	 *
	 * @param studyName the new study name
	 */
	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

	/**
	 * Gets the measurement row list.
	 *
	 * @return the measurement row list
	 */
	public List<MeasurementRow> getMeasurementRowList() {
		return this.measurementRowList;
	}

	/**
	 * Sets the measurement row list.
	 *
	 * @param measurementRowList the new measurement row list
	 */
	public void setMeasurementRowList(List<MeasurementRow> measurementRowList) {
		this.measurementRowList = measurementRowList;
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
	 * Gets the paginated measurement row list.
	 *
	 * @return the paginated measurement row list
	 */
	public List<MeasurementRow> getPaginatedMeasurementRowList() {
		return this.paginatedMeasurementRowList;
	}

	/**
	 * Sets the paginated measurement row list.
	 *
	 * @param paginatedMeasurementRowList the new paginated measurement row list
	 */
	public void setPaginatedMeasurementRowList(List<MeasurementRow> paginatedMeasurementRowList) {
		this.paginatedMeasurementRowList = paginatedMeasurementRowList;
	}

	/**
	 * Gets the current page.
	 *
	 * @return the current page
	 */
	public int getCurrentPage() {
		return this.currentPage;
	}

	/**
	 * Sets the current page.
	 *
	 * @param currentPage the new current page
	 */
	public void setCurrentPage(int currentPage) {
		// assumption is there is an imported germplasm already
		this.currentPage = 0;
	}

	/**
	 * Change page.
	 *
	 * @param currentPage the current page
	 */
	public void changePage(int currentPage) {
		if (this.measurementRowList != null && !this.measurementRowList.isEmpty()) {
			int totalItemsPerPage = this.getResultPerPage();
			int start = (currentPage - 1) * totalItemsPerPage;
			int end = start + totalItemsPerPage;
			if (this.measurementRowList.size() < end) {
				end = this.measurementRowList.size();
			}
			this.paginatedMeasurementRowList = this.measurementRowList.subList(start, end);
			this.currentPage = currentPage;
		} else {
			this.currentPage = 0;
		}
	}

	/**
	 * Gets the total pages.
	 *
	 * @return the total pages
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
	 * @param totalPages the new total pages
	 */
	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	/**
	 * Gets the result per page.
	 *
	 * @return the result per page
	 */
	public int getResultPerPage() {
		return this.resultPerPage;
	}

	/**
	 * Sets the result per page.
	 *
	 * @param resultPerPage the new result per page
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
	 * Gets the checks for error.
	 *
	 * @return the checks for error
	 */
	public String getHasError() {
		return this.hasError;
	}

	/**
	 * Sets the checks for error.
	 *
	 * @param hasError the new checks for error
	 */
	public void setHasError(String hasError) {
		this.hasError = hasError;
	}

	/**
	 * Gets the import val.
	 *
	 * @return the import val
	 */
	public int getImportVal() {
		return this.importVal;
	}

	/**
	 * Sets the import val.
	 *
	 * @param importVal the new import val
	 */
	public void setImportVal(int importVal) {
		this.importVal = importVal;
	}

	/**
	 * Gets the export instance type.
	 *
	 * @return the export instance type
	 */
	public String getExportInstanceType() {
		return this.exportInstanceType;
	}

	/**
	 * Sets the export instance type.
	 *
	 * @param exportInstanceType the new export instance type
	 */
	public void setExportInstanceType(String exportInstanceType) {
		this.exportInstanceType = exportInstanceType;
	}

	/**
	 * Gets the export trial instance start.
	 *
	 * @return the export trial instance start
	 */
	public String getExportTrialInstanceStart() {
		return this.exportTrialInstanceStart;
	}

	/**
	 * Sets the export trial instance start.
	 *
	 * @param exportTrialInstanceStart the new export trial instance start
	 */
	public void setExportTrialInstanceStart(String exportTrialInstanceStart) {
		this.exportTrialInstanceStart = exportTrialInstanceStart;
	}

	/**
	 * Gets the export trial instance end.
	 *
	 * @return the export trial instance end
	 */
	public String getExportTrialInstanceEnd() {
		return this.exportTrialInstanceEnd;
	}

	/**
	 * Sets the export trial instance end.
	 *
	 * @param exportTrialInstanceEnd the new export trial instance end
	 */
	public void setExportTrialInstanceEnd(String exportTrialInstanceEnd) {
		this.exportTrialInstanceEnd = exportTrialInstanceEnd;
	}

	/**
	 * Gets the export trial instance number.
	 *
	 * @return the export trial instance number
	 */
	public String getExportTrialInstanceNumber() {
		return this.exportTrialInstanceNumber;
	}

	/**
	 * Sets the export trial instance number.
	 *
	 * @param exportTrialInstanceNumber the new export trial instance number
	 */
	public void setExportTrialInstanceNumber(String exportTrialInstanceNumber) {
		this.exportTrialInstanceNumber = exportTrialInstanceNumber;
	}

	/**
	 * Gets the number of instances.
	 *
	 * @return the number of instances
	 */
	public int getNumberOfInstances() {
		return this.numberOfInstances;
	}

	/**
	 * Sets the number of instances.
	 *
	 * @param numberOfInstances the new number of instances
	 */
	public void setNumberOfInstances(int numberOfInstances) {
		this.numberOfInstances = numberOfInstances;
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

	public MeasurementRow getUpdateObservation() {
		return this.updateObservation;
	}

	public void setUpdateObservation(MeasurementRow updateObservation) {
		this.updateObservation = updateObservation;
	}

	public Integer getExperimentIndex() {
		return this.experimentIndex;
	}

	public void setExperimentIndex(Integer experimentIndex) {
		this.experimentIndex = experimentIndex;
	}

}
