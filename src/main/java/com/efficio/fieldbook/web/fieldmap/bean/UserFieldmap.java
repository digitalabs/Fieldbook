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
package com.efficio.fieldbook.web.fieldmap.bean;

import java.io.Serializable;


/**
 * @author Efficio.Daniel
 *
 */
public class UserFieldmap  implements Serializable {
    private String selectedName;
    private String numberOfEntries;
    private String numberOfReps;
    private String totalNumberOfPlots;
    private int fieldLocationId;
    private String fieldName;
    private String blockName;
    private int numberOfRowsInBlock;
    private int numberOfRangesInBlock;
    private int numberOfRowsPerPlot;
    private int plantingOrder;
    private boolean isTrial;
           
    
    public boolean isTrial() {
        return isTrial;
    }

    
    public void setTrial(boolean isTrial) {
        this.isTrial = isTrial;
    }

    public String getSelectedName() {
        return selectedName;
    }
    
    public void setSelectedName(String selectedName) {
        this.selectedName = selectedName;
    }
    
    public String getNumberOfEntries() {
        return numberOfEntries;
    }
    
    public void setNumberOfEntries(String numberOfEntries) {
        this.numberOfEntries = numberOfEntries;
    }
    
    public String getNumberOfReps() {
        return numberOfReps;
    }
    
    public void setNumberOfReps(String numberOfReps) {
        this.numberOfReps = numberOfReps;
    }
    
    public String getTotalNumberOfPlots() {
        return totalNumberOfPlots;
    }
    
    public void setTotalNumberOfPlots(String totalNumberOfPlots) {
        this.totalNumberOfPlots = totalNumberOfPlots;
    }
    
    public int getFieldLocationId() {
        return fieldLocationId;
    }
    
    public void setFieldLocationId(int fieldLocationId) {
        this.fieldLocationId = fieldLocationId;
    }
    
    public String getFieldName() {
        return fieldName;
    }
    
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    public String getBlockName() {
        return blockName;
    }
    
    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }
    
    public int getNumberOfRowsInBlock() {
        return numberOfRowsInBlock;
    }
    
    public void setNumberOfRowsInBlock(int numberOfRowsInBlock) {
        this.numberOfRowsInBlock = numberOfRowsInBlock;
    }
    
    public int getNumberOfRangesInBlock() {
        return numberOfRangesInBlock;
    }
    
    public void setNumberOfRangesInBlock(int numberOfRangesInBlock) {
        this.numberOfRangesInBlock = numberOfRangesInBlock;
    }
    
    public int getNumberOfRowsPerPlot() {
        return numberOfRowsPerPlot;
    }
    
    public void setNumberOfRowsPerPlot(int numberOfRowsPerPlot) {
        this.numberOfRowsPerPlot = numberOfRowsPerPlot;
    }
    
    public int getPlantingOrder() {
        return plantingOrder;
    }
    
    public void setPlantingOrder(int plantingOrder) {
        this.plantingOrder = plantingOrder;
    }
    
    
    
}
