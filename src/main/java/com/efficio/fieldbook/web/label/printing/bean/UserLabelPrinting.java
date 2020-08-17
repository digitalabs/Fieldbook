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

package com.efficio.fieldbook.web.label.printing.bean;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.inventory.InventoryDetails;

/**
 * The Class UserLabelPrinting.
 *
 * This is the session variable being use to hold information for the labels.
 */
public class UserLabelPrinting implements Serializable {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;
	private Integer studyId;
	/** The study. */
	private Study study;

	/** The field map info. */
	private FieldMapInfo fieldMapInfo;

	/** The field map info list. */
	private List<FieldMapInfo> fieldMapInfoList;

	/** The name. */
	private String name;

	private String owner;

	private String description;

	private String type;

	private String date;

	private String numberOfEntries;

	private String numberOfLotsWithReservations;

	private String numberOfCopies;

	/**
	 * Possible values for inventory lists: Entry, Designation, GID, StockId
	 */
	private String sorting;

	/** The title. */
	private String title;

	/** The objective. */
	private String objective;

	/** The number of instances. */
	private String numberOfInstances;

	/** The total number of label to print. */
	private String totalNumberOfLabelToPrint;

	/** The size of label sheet. */
	private String sizeOfLabelSheet;

	/** The number of label per row. */
	private String numberOfLabelPerRow;

	/** The number of rows per page of label. */
	private String numberOfRowsPerPageOfLabel;

	/** The barcode needed. */
	private String barcodeNeeded;
	
	/** The left selected label fields. */
	private String leftSelectedLabelFields;

	/** The right selected label fields. */
	private String rightSelectedLabelFields;

	private String mainSelectedLabelFields;

	/** The first barcode field. */
	private String firstBarcodeField;

	/** The second barcode field. */
	private String secondBarcodeField;

	/** The third barcode field. */
	private String thirdBarcodeField;

	/** The filename. */
	private String filename;

	/** The generate type. */
	// 1 - pdf, 2 - xls, 3- csv
	private String generateType;

	/** The order. */
	private String order;

	/** The filename with extension */
	private String filenameWithExtension;

	/** The filename dl location. */
	private String filenameDLLocation;

	/** The is field maps existing. */
	private boolean isFieldMapsExisting;

	private String settingsName;

	private String includeColumnHeadinginNonPdf;

	/** The automatically barcode. */
	private String barcodeGeneratedAutomatically;

	/**
	 * Gets the field map info.
	 *
	 * @return the field map info
	 */
	public FieldMapInfo getFieldMapInfo() {
		return this.fieldMapInfo;
	}

	/**
	 * Gets the filename with extension
	 *
	 * @return the filename with extension
	 */
	public String getFilenameWithExtension() {
		return this.filenameWithExtension;
	}

	/**
	 * Sets the filename with extension
	 *
	 * @param filenameWithExtension the new filename with extension
	 */
	public void setFilenameWithExtension(final String filenameWithExtension) {
		this.filenameWithExtension = filenameWithExtension;
	}

	/**
	 * Gets the filename dl location.
	 *
	 * @return the filename dl location
	 */
	public String getFilenameDLLocation() {
		return this.filenameDLLocation;
	}

	/**
	 * Sets the filename dl location.
	 *
	 * @param filenameDLLocation the new filename dl location
	 */
	public void setFilenameDLLocation(final String filenameDLLocation) {
		this.filenameDLLocation = filenameDLLocation;
	}

	/**
	 * Sets the field map info.
	 *
	 * @param fieldMapInfo the new field map info
	 */
	public void setFieldMapInfo(final FieldMapInfo fieldMapInfo) {
		this.fieldMapInfo = fieldMapInfo;
		int totalLabels = 0;
		if (fieldMapInfo != null) {
			if (fieldMapInfo.getDatasets() != null && !fieldMapInfo.getDatasets().isEmpty()) {
				final FieldMapDatasetInfo info = fieldMapInfo.getDatasets().get(0);
				if (info.getTrialInstances() != null) {
					this.numberOfInstances = Integer.toString(info.getTrialInstances().size());
					for (int i = 0; i < info.getTrialInstances().size(); i++) {
						FieldMapTrialInstanceInfo trialInstanceInfo = info.getTrialInstances().get(i);
						if (trialInstanceInfo.getFieldMapLabels() != null) {
							totalLabels += trialInstanceInfo.getFieldMapLabels().size();
						}
					}

				}
			}
			this.totalNumberOfLabelToPrint = Integer.toString(totalLabels);
		}
	}

	/**
	 * Gets the field map info list.
	 *
	 * @return the field map info list
	 */
	public List<FieldMapInfo> getFieldMapInfoList() {
		return this.fieldMapInfoList;
	}

	/**
	 * Sets the field map info list.
	 *
	 * @param fieldMapInfoList the new field map info list
	 */
	public void setFieldMapInfoList(final List<FieldMapInfo> fieldMapInfoList) {
		this.fieldMapInfoList = fieldMapInfoList;
		int totalLabels = 0;
		if (fieldMapInfoList != null) {
			for (final FieldMapInfo mapInfo : fieldMapInfoList) {
				for (FieldMapDatasetInfo dataset : mapInfo.getDatasets()) {
					for (FieldMapTrialInstanceInfo trialInstance : dataset.getTrialInstances()) {
						totalLabels += trialInstance.getFieldMapLabels().size();
					}
				}
			}
			this.totalNumberOfLabelToPrint = Integer.toString(totalLabels);
		}
	}

	/**
	 * Gets the generate type.
	 *
	 * @return the generate type
	 */
	public String getGenerateType() {
		return this.generateType;
	}

	/**
	 * Sets the generate type.
	 *
	 * @param generateType the new generate type
	 */
	public void setGenerateType(final String generateType) {
		this.generateType = generateType;
	}

	/**
	 * Gets the filename.
	 *
	 * @return the filename
	 */
	public String getFilename() {
		return this.filename;
	}

	/**
	 * Sets the filename.
	 *
	 * @param filename the new filename
	 */
	public void setFilename(final String filename) {
		this.filename = filename;
	}

	/**
	 * Gets the study.
	 *
	 * @return the study
	 */
	public Study getStudy() {
		return this.study;
	}

	/**
	 * Sets the study.
	 *
	 * @param study the new study
	 */
	public void setStudy(final Study study) {
		this.study = study;
		this.name = study.getName();
		this.title = study.getDescription();
		this.objective = study.getObjective();
	}

	/**
	 * Gets the first barcode field.
	 *
	 * @return the first barcode field
	 */
	public String getFirstBarcodeField() {
		return this.firstBarcodeField;
	}

	/**
	 * Sets the first barcode field.
	 *
	 * @param firstBarcodeField the new first barcode field
	 */
	public void setFirstBarcodeField(final String firstBarcodeField) {
		this.firstBarcodeField = firstBarcodeField;
	}

	/**
	 * Gets the second barcode field.
	 *
	 * @return the second barcode field
	 */
	public String getSecondBarcodeField() {
		return this.secondBarcodeField;
	}

	/**
	 * Sets the second barcode field.
	 *
	 * @param secondBarcodeField the new second barcode field
	 */
	public void setSecondBarcodeField(final String secondBarcodeField) {
		this.secondBarcodeField = secondBarcodeField;
	}

	/**
	 * Gets the third barcode field.
	 *
	 * @return the third barcode field
	 */
	public String getThirdBarcodeField() {
		return this.thirdBarcodeField;
	}

	/**
	 * Sets the third barcode field.
	 *
	 * @param thirdBarcodeField the new third barcode field
	 */
	public void setThirdBarcodeField(final String thirdBarcodeField) {
		this.thirdBarcodeField = thirdBarcodeField;
	}

	/**
	 * Gets the left selected label fields.
	 *
	 * @return the left selected label fields
	 */
	public String getLeftSelectedLabelFields() {
		return this.leftSelectedLabelFields;
	}

	/**
	 * Sets the left selected label fields.
	 *
	 * @param leftSelectedLabelFields the new left selected label fields
	 */
	public void setLeftSelectedLabelFields(final String leftSelectedLabelFields) {
		this.leftSelectedLabelFields = leftSelectedLabelFields;
	}

	/**
	 * Gets the right selected label fields.
	 *
	 * @return the right selected label fields
	 */
	public String getRightSelectedLabelFields() {
		return this.rightSelectedLabelFields;
	}

	/**
	 * Sets the right selected label fields.
	 *
	 * @param rightSelectedLabelFields the new right selected label fields
	 */
	public void setRightSelectedLabelFields(final String rightSelectedLabelFields) {
		this.rightSelectedLabelFields = rightSelectedLabelFields;
	}

	/**
	 * Gets the barcode needed.
	 *
	 * @return the barcode needed
	 */
	public String getBarcodeNeeded() {
		return this.barcodeNeeded;
	}

	/**
	 * Sets the barcode needed.
	 *
	 * @param barcodeNeeded the new barcode needed
	 */
	public void setBarcodeNeeded(final String barcodeNeeded) {
		this.barcodeNeeded = barcodeNeeded;
	}

	/**
	 * Gets the name.
	 *
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Sets the name.
	 *
	 * @param name the new name
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * Gets the title.
	 *
	 * @return the title
	 */
	public String getTitle() {
		return this.title;
	}

	/**
	 * Sets the title.
	 *
	 * @param title the new title
	 */
	public void setTitle(final String title) {
		this.title = title;
	}

	/**
	 * Gets the objective.
	 *
	 * @return the objective
	 */
	public String getObjective() {
		return this.objective;
	}

	/**
	 * Sets the objective.
	 *
	 * @param objective the new objective
	 */
	public void setObjective(final String objective) {
		this.objective = objective;
	}

	/**
	 * Gets the number of instances.
	 *
	 * @return the number of instances
	 */
	public String getNumberOfInstances() {
		return this.numberOfInstances;
	}

	/**
	 * Sets the number of instances.
	 *
	 * @param numberOfInstances the new number of instances
	 */
	public void setNumberOfInstances(final String numberOfInstances) {
		this.numberOfInstances = numberOfInstances;
	}

	/**
	 * Gets the total number of label to print.
	 *
	 * @return the total number of label to print
	 */
	public String getTotalNumberOfLabelToPrint() {
		return this.totalNumberOfLabelToPrint;
	}

	/**
	 * Sets the total number of label to print.
	 *
	 * @param totalNumberOfLabelToPrint the new total number of label to print
	 */
	public void setTotalNumberOfLabelToPrint(final String totalNumberOfLabelToPrint) {
		this.totalNumberOfLabelToPrint = totalNumberOfLabelToPrint;
	}

	/**
	 * Gets the size of label sheet.
	 *
	 * @return the size of label sheet
	 */
	public String getSizeOfLabelSheet() {
		return this.sizeOfLabelSheet;
	}

	/**
	 * Sets the size of label sheet.
	 *
	 * @param sizeOfLabelSheet the new size of label sheet
	 */
	public void setSizeOfLabelSheet(final String sizeOfLabelSheet) {
		this.sizeOfLabelSheet = sizeOfLabelSheet;
	}

	/**
	 * Gets the number of label per row.
	 *
	 * @return the number of label per row
	 */
	public String getNumberOfLabelPerRow() {
		return this.numberOfLabelPerRow;
	}

	/**
	 * Sets the number of label per row.
	 *
	 * @param numberOfLabelPerRow the new number of label per row
	 */
	public void setNumberOfLabelPerRow(String numberOfLabelPerRow) {
		this.numberOfLabelPerRow = numberOfLabelPerRow;
	}

	/**
	 * Gets the number of rows per page of label.
	 *
	 * @return the number of rows per page of label
	 */
	public String getNumberOfRowsPerPageOfLabel() {
		return this.numberOfRowsPerPageOfLabel;
	}

	/**
	 * Sets the number of rows per page of label.
	 *
	 * @param numberOfRowsPerPageOfLabel the new number of rows per page of label
	 */
	public void setNumberOfRowsPerPageOfLabel(final String numberOfRowsPerPageOfLabel) {
		this.numberOfRowsPerPageOfLabel = numberOfRowsPerPageOfLabel;
	}

	/**
	 * Gets the order.
	 *
	 * @return the order
	 */
	public String getOrder() {
		return this.order;
	}

	/**
	 * Sets the order.
	 *
	 * @param order the new order
	 */
	public void setOrder(final String order) {
		this.order = order;
	}

	/**
	 * Checks if is field maps existing.
	 *
	 * @return true, if is field maps existing
	 */
	public boolean isFieldMapsExisting() {
		return this.isFieldMapsExisting;
	}

	/**
	 * Sets the field maps existing.
	 *
	 * @param isFieldMapsExisting the new field maps existing
	 */
	public void setFieldMapsExisting(final boolean isFieldMapsExisting) {
		this.isFieldMapsExisting = isFieldMapsExisting;
	}

	public String getSettingsName() {
		return this.settingsName;
	}

	public void setSettingsName(final String settingsName) {
		this.settingsName = settingsName;
	}

	public String getMainSelectedLabelFields() {
		return this.mainSelectedLabelFields;
	}

	public void setMainSelectedLabelFields(final String mainSelectedLabelFields) {
		this.mainSelectedLabelFields = mainSelectedLabelFields;
	}

	public String getIncludeColumnHeadinginNonPdf() {
		return this.includeColumnHeadinginNonPdf;
	}

	public void setIncludeColumnHeadinginNonPdf(final String includeColumnHeadinginNonPdf) {
		this.includeColumnHeadinginNonPdf = includeColumnHeadinginNonPdf;
	}

	public Integer getStudyId() {
		return this.studyId;
	}

	public void setStudyId(final Integer studyId) {
		this.studyId = studyId;
	}

	public String getOwner() {
		return this.owner;
	}

	public void setOwner(final String owner) {
		this.owner = owner;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getType() {
		return this.type;
	}

	public void setType(final String type) {
		this.type = type;
	}

	public String getDate() {
		return this.date;
	}

	public void setDate(final String date) {
		this.date = date;
	}

	public String getNumberOfEntries() {
		return this.numberOfEntries;
	}

	public void setNumberOfEntries(final String numberOfEntries) {
		this.numberOfEntries = numberOfEntries;
	}

	public String getNumberOfLotsWithReservations() {
		return this.numberOfLotsWithReservations;
	}

	public void setNumberOfLotsWithReservations(final String numberOfLotsWithReservations) {
		this.numberOfLotsWithReservations = numberOfLotsWithReservations;
	}

	public String getNumberOfCopies() {
		return this.numberOfCopies;
	}

	public void setNumberOfCopies(final String numberOfCopies) {
		this.numberOfCopies = numberOfCopies;
	}

	public String getSorting() {
		return this.sorting;
	}

	public void setSorting(final String sorting) {
		this.sorting = sorting;
	}

	public String getBarcodeGeneratedAutomatically() {
		return barcodeGeneratedAutomatically;
	}

	public void setBarcodeGeneratedAutomatically(final String barcodeGeneratedAutomatically) {
		this.barcodeGeneratedAutomatically = barcodeGeneratedAutomatically;
	}

}
