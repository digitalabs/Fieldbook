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
package com.efficio.fieldbook.web.common.form;

import org.springframework.web.multipart.MultipartFile;

/**
 * The Class ImportDesignForm.
 */
public class ImportDesignForm {
		
	/** The file. */
	private MultipartFile file;
	
	/** The has error. */
	private String hasError;
	
	/** The file type **/
	private Integer fileType;

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

	public String getHasError() {
		return hasError;
	}

	public void setHasError(String hasError) {
		this.hasError = hasError;
	}
	
	public Integer getFileType() {
		return fileType;
	}

	public void setFileType(Integer fileType) {
		this.fileType = fileType;
	}
	
		
}
