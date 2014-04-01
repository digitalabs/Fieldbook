package com.efficio.fieldbook.web.common.bean;

import java.util.List;

import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.Workbook;

public abstract class StudySelection {

	protected Workbook workbook;
	
	protected int currentPage;

	protected List<MeasurementRow> measurementRowList;

	public abstract boolean isTrial();

	/**
	 * @return the workbook
	 */
	public Workbook getWorkbook() {
		return workbook;
	}

	/**
	 * @param workbook the workbook to set
	 */
	public void setWorkbook(Workbook workbook) {
		this.workbook = workbook;
	}

	/**
	 * @return the currentPage
	 */
	public int getCurrentPage() {
		return currentPage;
	}

	/**
	 * @param currentPage the currentPage to set
	 */
	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	/**
	 * @return the measurementRowList
	 */
	public List<MeasurementRow> getMeasurementRowList() {
		return measurementRowList;
	}

	/**
	 * @param measurementRowList the measurementRowList to set
	 */
	public void setMeasurementRowList(List<MeasurementRow> measurementRowList) {
		this.measurementRowList = measurementRowList;
	}
	
	
}
