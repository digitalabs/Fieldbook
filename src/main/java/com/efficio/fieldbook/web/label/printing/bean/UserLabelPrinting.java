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
package com.efficio.fieldbook.web.label.printing.bean;

import java.io.Serializable;
import java.util.List;

import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;

// TODO: Auto-generated Javadoc
/**
 * The Class UserLabelPrinting.
 *
 * This is the session variable being use to hold information for the labels.
 */
public class UserLabelPrinting implements Serializable{
    
    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;
    
    /** The study. */
    private Study study;
    
    /** The field map info. */
    private FieldMapInfo fieldMapInfo;
    
    /** The field map info list. */
    private List<FieldMapInfo> fieldMapInfoList;
    
    /** The name. */
    private String name;
    
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
    private String generateType; //1 - pdf, 2 - xls
    
    /** The order. */
    private String order;
    
    /** The filename dl. */
    private String filenameDL;
    
    /** The filename dl location. */
    private String filenameDLLocation;
    
    /** The is field maps existing. */
    private boolean isFieldMapsExisting;
    
    private String settingsName;
    
    private String includeColumnHeadinginNonPdf;
    
    /**
     * Gets the field map info.
     *
     * @return the field map info
     */
    public FieldMapInfo getFieldMapInfo() {
        return fieldMapInfo;
    }
    
    /**
     * Gets the filename dl.
     *
     * @return the filename dl
     */
    public String getFilenameDL() {
		return filenameDL;
	}

	/**
	 * Sets the filename dl.
	 *
	 * @param filenameDL the new filename dl
	 */
	public void setFilenameDL(String filenameDL) {
		this.filenameDL = filenameDL;
	}

	/**
	 * Gets the filename dl location.
	 *
	 * @return the filename dl location
	 */
	public String getFilenameDLLocation() {
		return filenameDLLocation;
	}

	/**
	 * Sets the filename dl location.
	 *
	 * @param filenameDLLocation the new filename dl location
	 */
	public void setFilenameDLLocation(String filenameDLLocation) {
		this.filenameDLLocation = filenameDLLocation;
	}

	/**
	 * Sets the field map info.
	 *
	 * @param fieldMapInfo the new field map info
	 */
	public void setFieldMapInfo(FieldMapInfo fieldMapInfo) {
        this.fieldMapInfo = fieldMapInfo;
        int totalLabels = 0;
        if(fieldMapInfo != null){
            if(fieldMapInfo.getDatasets() != null && fieldMapInfo.getDatasets().size() > 0){
                FieldMapDatasetInfo info = fieldMapInfo.getDatasets().get(0);
                if(info.getTrialInstances() != null){
                    this.numberOfInstances = Integer.toString(info.getTrialInstances().size());
                    for(int i = 0 ; i < info.getTrialInstances().size(); i++){
                        FieldMapTrialInstanceInfo trialInstanceInfo = info.getTrialInstances().get(i);
                        if(trialInstanceInfo.getFieldMapLabels() != null) {
                            totalLabels+=trialInstanceInfo.getFieldMapLabels().size();
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
        return fieldMapInfoList;
    }
    
    /**
     * Sets the field map info list.
     *
     * @param fieldMapInfoList the new field map info list
     */
    public void setFieldMapInfoList(List<FieldMapInfo> fieldMapInfoList) {
        this.fieldMapInfoList = fieldMapInfoList;
        int totalLabels = 0;
        if(fieldMapInfoList != null){
            for (FieldMapInfo fieldMapInfo : fieldMapInfoList) {
                for (FieldMapDatasetInfo dataset : fieldMapInfo.getDatasets()) {
                    for (FieldMapTrialInstanceInfo trialInstance : dataset.getTrialInstances()) {
                        totalLabels+=trialInstance.getFieldMapLabels().size();
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
        return generateType;
    }
    
    /**
     * Sets the generate type.
     *
     * @param generateType the new generate type
     */
    public void setGenerateType(String generateType) {
        this.generateType = generateType;
    }

    /**
     * Gets the filename.
     *
     * @return the filename
     */
    public String getFilename() {
        return filename;
    }
    
    /**
     * Sets the filename.
     *
     * @param filename the new filename
     */
    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * Gets the study.
     *
     * @return the study
     */
    public Study getStudy() {
        return study;
    }

    /**
     * Sets the study.
     *
     * @param study the new study
     */
    public void setStudy(Study study) {
        this.study = study;
        this.name = study.getName();
        this.title = study.getTitle();
        this.objective = study.getObjective();
    }

    /**
     * Gets the first barcode field.
     *
     * @return the first barcode field
     */
    public String getFirstBarcodeField() {
        return firstBarcodeField;
    }
    
    /**
     * Sets the first barcode field.
     *
     * @param firstBarcodeField the new first barcode field
     */
    public void setFirstBarcodeField(String firstBarcodeField) {
        this.firstBarcodeField = firstBarcodeField;
    }
    
    /**
     * Gets the second barcode field.
     *
     * @return the second barcode field
     */
    public String getSecondBarcodeField() {
        return secondBarcodeField;
    }
    
    /**
     * Sets the second barcode field.
     *
     * @param secondBarcodeField the new second barcode field
     */
    public void setSecondBarcodeField(String secondBarcodeField) {
        this.secondBarcodeField = secondBarcodeField;
    }
    
    /**
     * Gets the third barcode field.
     *
     * @return the third barcode field
     */
    public String getThirdBarcodeField() {
        return thirdBarcodeField;
    }
    
    /**
     * Sets the third barcode field.
     *
     * @param thirdBarcodeField the new third barcode field
     */
    public void setThirdBarcodeField(String thirdBarcodeField) {
        this.thirdBarcodeField = thirdBarcodeField;
    }
    
    /**
     * Gets the left selected label fields.
     *
     * @return the left selected label fields
     */
    public String getLeftSelectedLabelFields() {
        return leftSelectedLabelFields;
    }
    
    /**
     * Sets the left selected label fields.
     *
     * @param leftSelectedLabelFields the new left selected label fields
     */
    public void setLeftSelectedLabelFields(String leftSelectedLabelFields) {
        this.leftSelectedLabelFields = leftSelectedLabelFields;
    }
    
    /**
     * Gets the right selected label fields.
     *
     * @return the right selected label fields
     */
    public String getRightSelectedLabelFields() {
        return rightSelectedLabelFields;
    }
    
    /**
     * Sets the right selected label fields.
     *
     * @param rightSelectedLabelFields the new right selected label fields
     */
    public void setRightSelectedLabelFields(String rightSelectedLabelFields) {
        this.rightSelectedLabelFields = rightSelectedLabelFields;
    }

    /**
     * Gets the barcode needed.
     *
     * @return the barcode needed
     */
    public String getBarcodeNeeded() {
        return barcodeNeeded;
    }
    
    /**
     * Sets the barcode needed.
     *
     * @param barcodeNeeded the new barcode needed
     */
    public void setBarcodeNeeded(String barcodeNeeded) {
        this.barcodeNeeded = barcodeNeeded;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }
    
    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }
    
    /**
     * Sets the title.
     *
     * @param title the new title
     */
    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * Gets the objective.
     *
     * @return the objective
     */
    public String getObjective() {
        return objective;
    }
    
    /**
     * Sets the objective.
     *
     * @param objective the new objective
     */
    public void setObjective(String objective) {
        this.objective = objective;
    }
    
    /**
     * Gets the number of instances.
     *
     * @return the number of instances
     */
    public String getNumberOfInstances() {
        return numberOfInstances;
    }
    
    /**
     * Sets the number of instances.
     *
     * @param numberOfInstances the new number of instances
     */
    public void setNumberOfInstances(String numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }
    
    /**
     * Gets the total number of label to print.
     *
     * @return the total number of label to print
     */
    public String getTotalNumberOfLabelToPrint() {
        return totalNumberOfLabelToPrint;
    }
    
    /**
     * Sets the total number of label to print.
     *
     * @param totalNumberOfLabelToPrint the new total number of label to print
     */
    public void setTotalNumberOfLabelToPrint(String totalNumberOfLabelToPrint) {
        this.totalNumberOfLabelToPrint = totalNumberOfLabelToPrint;
    }
    
    /**
     * Gets the size of label sheet.
     *
     * @return the size of label sheet
     */
    public String getSizeOfLabelSheet() {
        return sizeOfLabelSheet;
    }
    
    /**
     * Sets the size of label sheet.
     *
     * @param sizeOfLabelSheet the new size of label sheet
     */
    public void setSizeOfLabelSheet(String sizeOfLabelSheet) {
        this.sizeOfLabelSheet = sizeOfLabelSheet;
    }
    
    /**
     * Gets the number of label per row.
     *
     * @return the number of label per row
     */
    public String getNumberOfLabelPerRow() {
        return numberOfLabelPerRow;
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
        return numberOfRowsPerPageOfLabel;
    }
    
    /**
     * Sets the number of rows per page of label.
     *
     * @param numberOfRowsPerPageOfLabel the new number of rows per page of label
     */
    public void setNumberOfRowsPerPageOfLabel(String numberOfRowsPerPageOfLabel) {
        this.numberOfRowsPerPageOfLabel = numberOfRowsPerPageOfLabel;
    }
    
    /**
     * Gets the order.
     *
     * @return the order
     */
    public String getOrder() {
        return order;
    }
    
    /**
     * Sets the order.
     *
     * @param order the new order
     */
    public void setOrder(String order) {
        this.order = order;
    }

	/**
	 * Checks if is field maps existing.
	 *
	 * @return true, if is field maps existing
	 */
	public boolean isFieldMapsExisting() {
		return isFieldMapsExisting;
	}

	/**
	 * Sets the field maps existing.
	 *
	 * @param isFieldMapsExisting the new field maps existing
	 */
	public void setFieldMapsExisting(boolean isFieldMapsExisting) {
		this.isFieldMapsExisting = isFieldMapsExisting;
	}

	public String getSettingsName() {
		return settingsName;
	}

	public void setSettingsName(String settingsName) {
		this.settingsName = settingsName;
	}

	public String getMainSelectedLabelFields() {
		return mainSelectedLabelFields;
	}

	public void setMainSelectedLabelFields(String mainSelectedLabelFields) {
		this.mainSelectedLabelFields = mainSelectedLabelFields;
	}

	public String getIncludeColumnHeadinginNonPdf() {
		return includeColumnHeadinginNonPdf;
	}

	public void setIncludeColumnHeadinginNonPdf(String includeColumnHeadinginNonPdf) {
		this.includeColumnHeadinginNonPdf = includeColumnHeadinginNonPdf;
	}
		
}
