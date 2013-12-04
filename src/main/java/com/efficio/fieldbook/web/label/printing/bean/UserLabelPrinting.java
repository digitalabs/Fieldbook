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


/**
 * @author Efficio.Daniel
 *
 */
public class UserLabelPrinting implements Serializable{
    
    private static final long serialVersionUID = 1L;
    
    private Study study;
    private FieldMapInfo fieldMapInfo;
    private List<FieldMapInfo> fieldMapInfoList;
    
    private String name;
    private String title;
    private String objective;
    private String numberOfInstances;
    private String totalNumberOfLabelToPrint;
    private String sizeOfLabelSheet;
    private String numberOfLabelPerRow;
    private String numberOfRowsPerPageOfLabel;
    private String barcodeNeeded;
    
    private String leftSelectedLabelFields;
    private String rightSelectedLabelFields;
    
    private String firstBarcodeField;
    private String secondBarcodeField;
    private String thirdBarcodeField;
    
    private String filename;
    
    private String generateType; //1 - pdf, 2 - xls
    
    private String order;
    
    
    public FieldMapInfo getFieldMapInfo() {
        return fieldMapInfo;
    }


    
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
                        if(trialInstanceInfo.getFieldMapLabels() != null)
                            totalLabels+=trialInstanceInfo.getFieldMapLabels().size();
                    }
                    
                }
            }
            this.totalNumberOfLabelToPrint = Integer.toString(totalLabels);
            
        }
    }

    public List<FieldMapInfo> getFieldMapInfoList() {
        return fieldMapInfoList;
    }


    
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
    
    public String getGenerateType() {
        return generateType;
    }



    
    public void setGenerateType(String generateType) {
        this.generateType = generateType;
    }



    public String getFilename() {
        return filename;
    }



    
    public void setFilename(String filename) {
        this.filename = filename;
    }



    public Study getStudy() {
        return study;
    }


    public void setStudy(Study study) {
        this.study = study;
        this.name = study.getName();
        this.title = study.getTitle();
        this.objective = study.getObjective();
    }




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


    
    public String getLeftSelectedLabelFields() {
        return leftSelectedLabelFields;
    }



    
    public void setLeftSelectedLabelFields(String leftSelectedLabelFields) {
        this.leftSelectedLabelFields = leftSelectedLabelFields;
    }



    
    public String getRightSelectedLabelFields() {
        return rightSelectedLabelFields;
    }



    
    public void setRightSelectedLabelFields(String rightSelectedLabelFields) {
        this.rightSelectedLabelFields = rightSelectedLabelFields;
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
    
    public String getOrder() {
        return order;
    }
    
    public void setOrder(String order) {
        this.order = order;
    }
}
