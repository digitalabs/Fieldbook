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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The Class ImportedGermplasmList.
 */
public class ImportedGermplasmList implements Serializable {
	
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
    
    /** The imported constants. */
    private List<ImportedConstant> importedConstants;
    
    /** The imported variates. */
    private List<ImportedVariate> importedVariates;
    
    /** The imported germplasms. */
    private List<ImportedGermplasm> importedGermplasms;
    /** The imported germplasms. */
    private List<ImportedGermplasm> originalImportedGermplasms;
    
    public ImportedGermplasmList() {
    }

    /**
     * Instantiates a new imported germplasm list.
     *
     * @param filename the filename
     * @param name the name
     * @param title the title
     * @param type the type
     * @param date the date
     */
    public ImportedGermplasmList (String filename, String name, String title, String type, Date date){
        this.filename = filename;
        this.name = name;
        this.title = title;
        this.type = type;
        this.date = date;
        this.importedFactors = new ArrayList<ImportedFactor>();
        this.importedConstants = new ArrayList<ImportedConstant>();
        this.importedVariates = new ArrayList<ImportedVariate>();
        this.importedGermplasms = new ArrayList<ImportedGermplasm>();
        this.importedConditions = new ArrayList<ImportedCondition>();
    }
    
    /**
     * Instantiates a new imported germplasm list.
     *
     * @param filename the filename
     * @param name the name
     * @param title the title
     * @param type the type
     * @param date the date
     * @param importedConditions the imported conditions
     * @param importedFactors the imported factors
     * @param importedConstants the imported constants
     * @param importedVariates the imported variates
     * @param importedGermplasms the imported germplasms
     */
    public ImportedGermplasmList (String filename, String name, String title, String type, Date date
            , List<ImportedCondition> importedConditions, List<ImportedFactor> importedFactors
            , List<ImportedConstant> importedConstants, List<ImportedVariate> importedVariates
            , List<ImportedGermplasm> importedGermplasms){
        this.filename = filename;
        this.name = name;
        this.title = title;
        this.type = type;
        this.date = date;
        this.importedConditions = importedConditions;
        this.importedFactors = importedFactors;
        this.importedConstants = importedConstants;
        this.importedVariates = importedVariates;
        this.importedGermplasms = importedGermplasms;
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
     * Gets the imported conditions.
     *
     * @return the imported conditions
     */
    public List<ImportedCondition> getImportedConditions(){
        return importedConditions;
    }
    
    /**
     * Sets the imported conditions.
     *
     * @param importedConditions the new imported conditions
     */
    public void setImportedConditions(List<ImportedCondition> importedConditions){
        this.importedConditions = importedConditions;
    }
    
    /**
     * Adds the imported condition.
     *
     * @param importedCondition the imported condition
     */
    public void addImportedCondition(ImportedCondition importedCondition){
        this.importedConditions.add(importedCondition);
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
     * Gets the imported constants.
     *
     * @return the imported constants
     */
    public List<ImportedConstant> getImportedConstants(){
        return importedConstants;
    }
    
    /**
     * Sets the imported constants.
     *
     * @param importedConstants the new imported constants
     */
    public void setImportedConstants(List<ImportedConstant> importedConstants){
        this.importedConstants = importedConstants;
    }

    /**
     * Adds the imported constant.
     *
     * @param importedConstant the imported constant
     */
    public void addImportedConstant(ImportedConstant importedConstant){
        this.importedConstants.add(importedConstant);
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
     * Gets the imported germplasms.
     *
     * @return the imported germplasms
     */
    public List<ImportedGermplasm> getImportedGermplasms(){
        return importedGermplasms;
    }
    
    /**
     * Sets the imported germplasms.
     *
     * @param importedGermplasms the new imported germplasms
     */
    public void setImportedGermplasms(List<ImportedGermplasm> importedGermplasms){
        this.importedGermplasms = importedGermplasms;
    }

    /**
     * Adds the imported germplasm.
     *
     * @param importedGermplasm the imported germplasm
     */
    public void addImportedGermplasm(ImportedGermplasm importedGermplasm){
        this.importedGermplasms.add(importedGermplasm);
    }

	public List<ImportedGermplasm> getOriginalImportedGermplasms() {
		return originalImportedGermplasms;
	}

	public void setOriginalImportedGermplasms(
			List<ImportedGermplasm> originalImportedGermplasms) {
		this.originalImportedGermplasms = originalImportedGermplasms;
	}
	
	public void copyImportedGermplasms(){
		this.originalImportedGermplasms = this.importedGermplasms;
	}
}
