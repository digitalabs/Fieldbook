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
package com.efficio.fieldbook.web.nursery.form;

/**
 * Form for the Nursery Wizard - Step 4.
 * 
 */
public class SaveNurseryForm {
	
    /** The folderId */
    private Integer folderId;
    
    /** The folder name */
    private String folderName;
    
	/** The title. */
	private String title;
	
	/** The objective. */
	private String objective;
	
	/** The nursery book name. */
	private String nurseryBookName;

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
	 * Gets the nursery book name.
	 *
	 * @return the nursery book name
	 */
	public String getNurseryBookName() {
		return nurseryBookName;
	}

	/**
	 * Sets the nursery book name.
	 *
	 * @param nurseryBookName the new nursery book name
	 */
	public void setNurseryBookName(String nurseryBookName) {
		this.nurseryBookName = nurseryBookName;
	}

    
    /**
     * @return the folderId
     */
    public Integer getFolderId() {
        return folderId;
    }

    
    /**
     * @param folderId the folderId to set
     */
    public void setFolderId(Integer folderId) {
        this.folderId = folderId;
    }

    
    /**
     * @return the folderName
     */
    public String getFolderName() {
        return folderName;
    }

    
    /**
     * @param folderName the folderName to set
     */
    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

}
