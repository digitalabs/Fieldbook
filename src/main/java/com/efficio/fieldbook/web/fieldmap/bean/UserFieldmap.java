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
import java.util.List;

import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;


// TODO: Auto-generated Javadoc
/**
 * The Class UserFieldmap.
 *
 * @author Efficio.Daniel
 */
public class UserFieldmap  implements Serializable {
    
    /** The selected name. */
    private String selectedName;
    
    /** The number of entries. */
    private Long numberOfEntries;
    
    /** The number of reps. */
    private Long numberOfReps;
    
    /** The total number of plots. */
    private Long totalNumberOfPlots;
    
    /** The field location id. */
    private int fieldLocationId;
    
    /** The field name. */
    private String fieldName;
    
    /** The block name. */
    private String blockName;
    
    /** The number of rows in block. */
    private int numberOfRowsInBlock;
    
    /** The number of ranges in block. */
    private int numberOfRangesInBlock;
    
    /** The number of rows per plot. */
    private int numberOfRowsPerPlot;
    
    /** The planting order. */
    private int plantingOrder;
    
    /** The is trial. */
    private boolean isTrial;
    
    /** The entry numbers. */
    private List<String> entryNumbers;
    
    /** The germplasm names. */
    private List<String> germplasmNames;
    
    /** The reps. */
    private List<Integer> reps;
    
    /** The starting row. */
    private int startingRow;
    
    /** The starting range. */
    private int startingRange;
    
    public UserFieldmap(){
        
    }
    
    /**
     * Instantiates a new user fieldmap.
     *
     * @param fieldMapInfo the field map info
     * @param isTrial the is trial
     */
    public UserFieldmap(FieldMapInfo fieldMapInfo, boolean isTrial){
        setSelectedName(fieldMapInfo.getFieldbookName());
        setNumberOfEntries(fieldMapInfo.getEntryCount());
        setNumberOfReps(fieldMapInfo.getRepCount());
        setTotalNumberOfPlots(fieldMapInfo.getPlotCount());
        setEntryNumbers(fieldMapInfo.getEntryNumbers());
        setGermplasmNames(fieldMapInfo.getGermplasmNames());
        setReps(fieldMapInfo.getReps());
        setTrial(isTrial);
        if(isTrial){
            setNumberOfRowsPerPlot(2);
        }else{
            setNumberOfRowsPerPlot(1);
        }
        
    }
    
    public void setUserFieldmapInfo(FieldMapInfo fieldMapInfo, boolean isTrial){
        setSelectedName(fieldMapInfo.getFieldbookName());
        setNumberOfEntries(fieldMapInfo.getEntryCount());
        setNumberOfReps(fieldMapInfo.getRepCount());
        setTotalNumberOfPlots(fieldMapInfo.getPlotCount());
        setEntryNumbers(fieldMapInfo.getEntryNumbers());
        setGermplasmNames(fieldMapInfo.getGermplasmNames());
        setReps(fieldMapInfo.getReps());
        setTrial(isTrial);
        if(isTrial){
            setNumberOfRowsPerPlot(2);
        }else{
            setNumberOfRowsPerPlot(1);
        }
    }
    
    
    
    /**
     * Gets the entry numbers.
     *
     * @return the entry numbers
     */
    public List<String> getEntryNumbers() {
        return entryNumbers;
    }


    /**
     * Sets the entry numbers.
     *
     * @param entryNumbers the new entry numbers
     */
    public void setEntryNumbers(List<String> entryNumbers) {
        this.entryNumbers = entryNumbers;
    }

    /**
     * Gets the germplasm names.
     *
     * @return the germplasm names
     */
    public List<String> getGermplasmNames() {
        return germplasmNames;
    }


    /**
     * Sets the germplasm names.
     *
     * @param germplasmNames the new germplasm names
     */
    public void setGermplasmNames(List<String> germplasmNames) {
        this.germplasmNames = germplasmNames;
    }


    /**
     * Gets the reps.
     *
     * @return the reps
     */
    public List<Integer> getReps() {
        return reps;
    }

    /**
     * Sets the reps.
     *
     * @param reps the new reps
     */
    public void setReps(List<Integer> reps) {
        this.reps = reps;
    }





    /**
     * Checks if is trial.
     *
     * @return true, if is trial
     */
    public boolean isTrial() {
        return isTrial;
    }

    
    /**
     * Sets the trial.
     *
     * @param isTrial the new trial
     */
    public void setTrial(boolean isTrial) {
        this.isTrial = isTrial;
    }

    /**
     * Gets the selected name.
     *
     * @return the selected name
     */
    public String getSelectedName() {
        return selectedName;
    }
    
    /**
     * Sets the selected name.
     *
     * @param selectedName the new selected name
     */
    public void setSelectedName(String selectedName) {
        this.selectedName = selectedName;
    }
    
    /**
     * Gets the number of entries.
     *
     * @return the number of entries
     */
    public Long getNumberOfEntries() {
        return numberOfEntries;
    }
    
    /**
     * Sets the number of entries.
     *
     * @param numberOfEntries the new number of entries
     */
    public void setNumberOfEntries(Long numberOfEntries) {
        this.numberOfEntries = numberOfEntries;
    }
    
    /**
     * Gets the number of reps.
     *
     * @return the number of reps
     */
    public Long getNumberOfReps() {
        return numberOfReps;
    }
    
    /**
     * Sets the number of reps.
     *
     * @param numberOfReps the new number of reps
     */
    public void setNumberOfReps(Long numberOfReps) {
        this.numberOfReps = numberOfReps;
    }
    
    /**
     * Gets the total number of plots.
     *
     * @return the total number of plots
     */
    public Long getTotalNumberOfPlots() {
        return totalNumberOfPlots;
    }
    
    /**
     * Sets the total number of plots.
     *
     * @param totalNumberOfPlots the new total number of plots
     */
    public void setTotalNumberOfPlots(Long totalNumberOfPlots) {
        this.totalNumberOfPlots = totalNumberOfPlots;
    }
    
    /**
     * Gets the field location id.
     *
     * @return the field location id
     */
    public int getFieldLocationId() {
        return fieldLocationId;
    }
    
    /**
     * Sets the field location id.
     *
     * @param fieldLocationId the new field location id
     */
    public void setFieldLocationId(int fieldLocationId) {
        this.fieldLocationId = fieldLocationId;
    }
    
    /**
     * Gets the field name.
     *
     * @return the field name
     */
    public String getFieldName() {
        return fieldName;
    }
    
    /**
     * Sets the field name.
     *
     * @param fieldName the new field name
     */
    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
    
    /**
     * Gets the block name.
     *
     * @return the block name
     */
    public String getBlockName() {
        return blockName;
    }
    
    /**
     * Sets the block name.
     *
     * @param blockName the new block name
     */
    public void setBlockName(String blockName) {
        this.blockName = blockName;
    }
    
    /**
     * Gets the number of rows in block.
     *
     * @return the number of rows in block
     */
    public int getNumberOfRowsInBlock() {
        return numberOfRowsInBlock;
    }
    
    /**
     * Sets the number of rows in block.
     *
     * @param numberOfRowsInBlock the new number of rows in block
     */
    public void setNumberOfRowsInBlock(int numberOfRowsInBlock) {
        this.numberOfRowsInBlock = numberOfRowsInBlock;
    }
    
    /**
     * Gets the number of ranges in block.
     *
     * @return the number of ranges in block
     */
    public int getNumberOfRangesInBlock() {
        return numberOfRangesInBlock;
    }
    
    /**
     * Sets the number of ranges in block.
     *
     * @param numberOfRangesInBlock the new number of ranges in block
     */
    public void setNumberOfRangesInBlock(int numberOfRangesInBlock) {
        this.numberOfRangesInBlock = numberOfRangesInBlock;
    }
    
    /**
     * Gets the number of rows per plot.
     *
     * @return the number of rows per plot
     */
    public int getNumberOfRowsPerPlot() {
        return numberOfRowsPerPlot;
    }
    
    /**
     * Sets the number of rows per plot.
     *
     * @param numberOfRowsPerPlot the new number of rows per plot
     */
    public void setNumberOfRowsPerPlot(int numberOfRowsPerPlot) {
        this.numberOfRowsPerPlot = numberOfRowsPerPlot;
    }
    
    /**
     * Gets the planting order.
     *
     * @return the planting order
     */
    public int getPlantingOrder() {
        return plantingOrder;
    }
    
    /**
     * Sets the planting order.
     *
     * @param plantingOrder the new planting order
     */
    public void setPlantingOrder(int plantingOrder) {
        this.plantingOrder = plantingOrder;
    }
    
    /**
     * Gets the starting row.
     *
     * @return the starting row
     */
    public int getStartingRow() {
        return startingRow;
    }
    
    /**
     * Sets the starting row.
     *
     * @param startingRow the new starting row
     */
    public void setStartingRow(int startingRow) {
        this.startingRow = startingRow;
    }
    
    /**
     * Gets the starting range.
     *
     * @return the starting range
     */
    public int getStartingRange() {
        return startingRange;
    }
    
    /**
     * Sets the starting range.
     *
     * @param startingRange the new starting range
     */
    public void setStartingRange(int startingRange) {
        this.startingRange = startingRange;
    }
}
