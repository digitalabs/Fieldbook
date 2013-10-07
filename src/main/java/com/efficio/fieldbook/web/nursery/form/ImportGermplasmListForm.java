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

import org.springframework.web.multipart.MultipartFile;

// TODO: Auto-generated Javadoc
/**
 * The Class ImportGermplasmListForm.
 */
public class ImportGermplasmListForm {
	
	/** The file. */
	private MultipartFile file;
	
	/** The has error. */
	private String hasError;
	
	/**
	 * Instantiates a new import germplasm list form.
	 */
	public ImportGermplasmListForm(){
		setHasError("0");
	}
	
    /**
     * Gets the checks for error.
     *
     * @return the checks for error
     */
    public String getHasError() {
		return hasError;
	}

	/**
	 * Sets the checks for error.
	 *
	 * @param hasError the new checks for error
	 */
	public void setHasError(String hasError) {
		this.hasError = hasError;
	}

	/**
	 * Gets the file.
	 *
	 * @return the file
	 */
	public MultipartFile getFile() {

        return file;

    }

    /**
     * Sets the file.
     *
     * @param file the new file
     */
    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
