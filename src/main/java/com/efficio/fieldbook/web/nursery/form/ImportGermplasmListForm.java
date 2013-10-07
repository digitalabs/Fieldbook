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

public class ImportGermplasmListForm {
	private MultipartFile file;
	private String hasError;
	
	public ImportGermplasmListForm(){
		setHasError("0");
	}
	
    public String getHasError() {
		return hasError;
	}

	public void setHasError(String hasError) {
		this.hasError = hasError;
	}

	public MultipartFile getFile() {

        return file;

    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }
}
