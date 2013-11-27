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


/**
 * @author Efficio.Daniel
 *
 */
public class UserLabelPrinting implements Serializable{
    private String name;
    private String title;
    private String objective;
    private String numberOfInstances;
    private String totalNumberOfLabelToPrint;
    private String sizeOfLabelSheet;
    private String numberOfLabelPerRow;
    private String numberOfRowsPerPageOfLabel;
    private String barcodeNeeded;
    
    private String selectedLabelFields;
    
    private String firstBarcodeField;
    private String secondBarcodeField;
    private String thirdBarcodeField;
    
    
    public String getFirstBarcodeField() {
        return firstBarcodeField;
    }



    
    public void setFirstBarcodeField(String firstBarcodeField) {
        this.firstBarcodeField = firstBarcodeField;
    }



    
    public String getSecondBarcodeField() {
        return secondBarcodeField;
    }



    
    public void setSecondBarcodeField(String secondBarcodeField) {
        this.secondBarcodeField = secondBarcodeField;
    }



    
    public String getThirdBarcodeField() {
        return thirdBarcodeField;
    }



    
    public void setThirdBarcodeField(String thirdBarcodeField) {
        this.thirdBarcodeField = thirdBarcodeField;
    }



    public String getSelectedLabelFields() {
        return selectedLabelFields;
    }


    
    public void setSelectedLabelFields(String selectedLabelFields) {
        this.selectedLabelFields = selectedLabelFields;
    }


    public String getBarcodeNeeded() {
        return barcodeNeeded;
    }

    
    public void setBarcodeNeeded(String barcodeNeeded) {
        this.barcodeNeeded = barcodeNeeded;
    }

    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getObjective() {
        return objective;
    }
    
    public void setObjective(String objective) {
        this.objective = objective;
    }
    
    public String getNumberOfInstances() {
        return numberOfInstances;
    }
    
    public void setNumberOfInstances(String numberOfInstances) {
        this.numberOfInstances = numberOfInstances;
    }
    
    public String getTotalNumberOfLabelToPrint() {
        return totalNumberOfLabelToPrint;
    }
    
    public void setTotalNumberOfLabelToPrint(String totalNumberOfLabelToPrint) {
        this.totalNumberOfLabelToPrint = totalNumberOfLabelToPrint;
    }
    
    public String getSizeOfLabelSheet() {
        return sizeOfLabelSheet;
    }
    
    public void setSizeOfLabelSheet(String sizeOfLabelSheet) {
        this.sizeOfLabelSheet = sizeOfLabelSheet;
    }
    
    public String getNumberOfLabelPerRow() {
        return numberOfLabelPerRow;
    }
    
    public void setNumberOfLabelPerRow(String numberOfLabelPerRow) {
        this.numberOfLabelPerRow = numberOfLabelPerRow;
    }
    
    public String getNumberOfRowsPerPageOfLabel() {
        return numberOfRowsPerPageOfLabel;
    }
    
    public void setNumberOfRowsPerPageOfLabel(String numberOfRowsPerPageOfLabel) {
        this.numberOfRowsPerPageOfLabel = numberOfRowsPerPageOfLabel;
    }
    
    
}
