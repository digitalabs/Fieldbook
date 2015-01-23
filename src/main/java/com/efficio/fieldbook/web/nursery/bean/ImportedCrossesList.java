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
package com.efficio.fieldbook.web.nursery.bean;

import java.io.Serializable;
import java.util.*;

/**
 * The Class ImportedCrossesList.
 */
public class ImportedCrossesList implements Serializable {
	
    private static final long serialVersionUID = 1L;

    /** The filename. */
	private String filename;
    
    /** The name. */
    private String name;
    
    /** The title. */
    private String title;
    
    /** The type. */
    private String type;
    
    /** The date. */
    private Date date;


    /** The imported conditions. */
    private List<ImportedCondition> importedConditions;

    /** The imported factors. */
    private List<ImportedFactor> importedFactors;
    
    /** The imported variates. */
    private List<ImportedVariate> importedVariates;


    private List<ImportedConstant> importedConstants;

    /** The imported crosses. */
    private List<ImportedCrosses> importedCrosses;

    private Set<String> errorMessages = new HashSet<>();

    
    public ImportedCrossesList() {
    }

    /**
     * Instantiates a new imported crosses list.
     *
     * @param filename the filename
     * @param name the name
     * @param title the title
     * @param type the type
     * @param date the date
     */
    public ImportedCrossesList (String filename, String name, String title, String type, Date date){
        this.filename = filename;
        this.name = name;
        this.title = title;
        this.type = type;
        this.date = date;
        this.importedConditions = new ArrayList<>();
        this.importedFactors = new ArrayList<>();
        this.importedConstants = new ArrayList<>();
        this.importedVariates = new ArrayList<>();
        this.importedCrosses = new ArrayList<>();
    }
    
    /**
     * Instantiates a new imported crosses list.
     *
     * @param filename the filename
     * @param name the name
     * @param title the title
     * @param type the type
     * @param date the date
     * @param importedFactors the imported factors
     * @param importedVariates the imported variates
     * @param importedCrosses the imported crosses
     */
    public ImportedCrossesList (String filename, String name, String title, String type, Date date
            , List<ImportedFactor> importedFactors
            , List<ImportedVariate> importedVariates
            , List<ImportedCrosses> importedCrosses){
        this.filename = filename;
        this.name = name;
        this.title = title;
        this.type = type;
        this.date = date;
        this.importedFactors = importedFactors;
        this.importedVariates = importedVariates;
        this.importedCrosses = importedCrosses;
    }
    
    /**
     * Gets the filename.
     *
     * @return the filename
     */
    public String getFilename(){
        return filename;
    }
    
    /**
     * Sets the filename.
     *
     * @param filename the new filename
     */
    public void setFilename(String filename){
        this.filename = filename;
    }
    
    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName(){
        return name;
    }
    
    /**
     * Sets the name.
     *
     * @param name the new name
     */
    public void setName(String name){
        this.name = name;
    }
    
    /**
     * Gets the title.
     *
     * @return the title
     */
    public String getTitle(){
        return title;
    }
    
    /**
     * Sets the title.
     *
     * @param title the new title
     */
    public void setTitle(String title){
        this.title = title;
    }
    
    /**
     * Gets the type.
     *
     * @return the type
     */
    public String getType() {
        return type;
    }
    
    /**
     * Sets the type.
     *
     * @param type the new type
     */
    public void setType(String type){
        this.type = type;
    }
    
    /**
     * Gets the date.
     *
     * @return the date
     */
    public Date getDate() {
        return date;
    }
    
    /**
     * Sets the date.
     *
     * @param date the new date
     */
    public void setDate(Date date){
        this.date = date;
    }

    /**
     * Gets the imported factors.
     *
     * @return the imported factors
     */
    public List<ImportedFactor> getImportedFactors(){
        return importedFactors;
    }
    
    /**
     * Sets the imported factors.
     *
     * @param importedFactors the new imported factors
     */
    public void setImportedFactors(List<ImportedFactor> importedFactors){
        this.importedFactors = importedFactors;
    }

    /**
     * Adds the imported factor.
     *
     * @param importedFactor the imported factor
     */
    public void addImportedFactor(ImportedFactor importedFactor){
        this.importedFactors.add(importedFactor);
    }    
    
    
    /**
     * Gets the imported variates.
     *
     * @return the imported variates
     */
    public List<ImportedVariate> getImportedVariates(){
        return importedVariates;
    }
    
    /**
     * Sets the imported variates.
     *
     * @param importedVariates the new imported variates
     */
    public void setImportedVariates(List<ImportedVariate> importedVariates){
        this.importedVariates = importedVariates;
    }

    /**
     * Adds the imported variate.
     *
     * @param importedVariate the imported variate
     */
    public void addImportedVariate(ImportedVariate importedVariate){
        this.importedVariates.add(importedVariate);
    }    

    /**
     * Gets the imported crosses
     *
     * @return the imported crosses
     */
    public List<ImportedCrosses> getImportedCrosses(){
        return importedCrosses;
    }
    
    /**
     * Sets the imported crosses
     *
     * @param importedCrosses the new imported crosses
     */
    public void setImportedGermplasms(List<ImportedCrosses> importedCrosses){
        this.importedCrosses = importedCrosses;
    }

    /**
     * Adds the imported crosses
     *
     * @param importedGermplasm the imported crosses
     */
    public void addImportedCrosses(ImportedCrosses importedCrosses){
        this.importedCrosses.add(importedCrosses);
    }

    /**
     * Retrieves the list of imported conditions
     * @return
     */
    public List<ImportedCondition> getImportedConditions() {
        return importedConditions;
    }

    public void addImportedCondition(ImportedCondition importedCondition) {
        this.importedConditions.add(importedCondition);
    }

    public List<ImportedConstant> getImportedConstants() {
        return importedConstants;
    }

    public void addImportedConstant(ImportedConstant importedConstant) {
        this.importedConstants.add(importedConstant);
    }

    public Set<String> getErrorMessages() {
        return errorMessages;
    }

    public void addErrorMessages(String errorMsg) {
        errorMessages.add(errorMsg);
    }
}
