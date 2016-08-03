
package com.efficio.etl.web.bean;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public class SelectRowsForm {

	// Sheet Name field
	private Integer selectedSheetIndex = null;

	// Header Row field
	private Integer headerRow = null;
	private Integer contentRow;
	private Integer indexColumn = null;
	private Integer observationRows;

	private String headerRowDisplayText = "";
	private String contentRowDisplayText = "";
	private Boolean noObservationComputation;

	// Study Detail fields
	private String studyName;
	private String studyTitle;
	private String pmKey;
	private String objective;
	private String startDate;
	private String endDate;
	private String studyType;

	public Integer getSelectedSheetIndex() {
		return this.selectedSheetIndex;
	}

	public void setSelectedSheetIndex(Integer selectedSheetIndex) {
		this.selectedSheetIndex = selectedSheetIndex;
	}

	public Integer getHeaderRow() {
		return this.headerRow;
	}

	public void setHeaderRow(Integer headerRow) {
		this.headerRow = headerRow;
	}

	public Integer getContentRow() {
		return this.contentRow;
	}

	public void setContentRow(Integer contentRow) {
		this.contentRow = contentRow;
	}

	public Integer getIndexColumn() {
		return this.indexColumn;
	}

	public void setIndexColumn(Integer indexColumn) {
		this.indexColumn = indexColumn;
	}

	public Integer getObservationRows() {
		return this.observationRows;
	}

	public void setObservationRows(Integer observationRows) {
		this.observationRows = observationRows;
	}

	public String getHeaderRowDisplayText() {
		return this.headerRowDisplayText;
	}

	public void setHeaderRowDisplayText(String headerRowDisplayText) {
		this.headerRowDisplayText = headerRowDisplayText;
	}

	public String getContentRowDisplayText() {
		return this.contentRowDisplayText;
	}

	public void setContentRowDisplayText(String contentRowDisplayText) {
		this.contentRowDisplayText = contentRowDisplayText;
	}

	public Boolean getNoObservationComputation() {
		return this.noObservationComputation;
	}

	public void setNoObservationComputation(Boolean noObservationComputation) {
		this.noObservationComputation = noObservationComputation;
	}

	public String getStudyName() {
		return this.studyName;
	}

	public void setStudyName(String studyName) {
		this.studyName = studyName;
	}

	public String getStudyTitle() {
		return this.studyTitle;
	}

	public void setStudyTitle(String studyTitle) {
		this.studyTitle = studyTitle;
	}

	public String getPmKey() {
		return this.pmKey;
	}

	public void setPmKey(String pmKey) {
		this.pmKey = pmKey;
	}

	public String getObjective() {
		return this.objective;
	}

	public void setObjective(String objective) {
		this.objective = objective;
	}

	public String getStartDate() {
		return this.startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return this.endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public String getStudyType() {
		return this.studyType;
	}

	public void setStudyType(String studyType) {
		this.studyType = studyType;
	}
}
