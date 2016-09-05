
package com.efficio.etl.web.bean;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte Date: 3/14/14 Time: 2:58 PM
 */
public class ConsolidatedStepForm {

	private StudyDetailsForm studyDetails;
	private Integer selectedSheetIndex;
	private Integer headerRowIndex;
	private String headerRowDisplayText;
	private Integer datasetType;

	public StudyDetailsForm getStudyDetails() {
		return this.studyDetails;
	}

	public void setStudyDetails(StudyDetailsForm studyDetails) {
		this.studyDetails = studyDetails;
	}

	public Integer getSelectedSheetIndex() {
		return this.selectedSheetIndex;
	}

	public void setSelectedSheetIndex(Integer selectedSheetIndex) {
		this.selectedSheetIndex = selectedSheetIndex;
	}

	public Integer getHeaderRowIndex() {
		return this.headerRowIndex;
	}

	public void setHeaderRowIndex(Integer headerRowIndex) {
		this.headerRowIndex = headerRowIndex;
	}

	public String getHeaderRowDisplayText() {
		return this.headerRowDisplayText;
	}

	public void setHeaderRowDisplayText(String headerRowDisplayText) {
		this.headerRowDisplayText = headerRowDisplayText;
	}

	public Integer getDatasetType() {
		return this.datasetType;
	}

	public void setDatasetType(Integer datasetType) {
		this.datasetType = datasetType;
	}

}
