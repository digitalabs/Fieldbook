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
package com.efficio.fieldbook.web.demo.form;

import org.springframework.web.multipart.MultipartFile;

/**
 * The Class Test3JavaForm.
 */
public class Test3JavaForm {
    
	/** The file. */
	private MultipartFile file;
	
    /** The import type. */
    private String importType = "";
    
    /** The file name. */
    public String fileName = "";
    
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

	/**
	 * Gets the import type.
	 *
	 * @return the import type
	 */
	public String getImportType() {
		return importType;
	}

	/**
	 * Sets the import type.
	 *
	 * @param importType the new import type
	 */
	public void setImportType(String importType) {
		this.importType = importType;
	}
	
	/**
	 * Gets the file name.
	 *
	 * @return the file name
	 */
	public String getFileName() {
		return fileName;
	}
	
	/**
	 * Sets the file name.
	 *
	 * @param name the new file name
	 */
	public void setFileName(String name) {
		this.fileName = name;
	}
}
