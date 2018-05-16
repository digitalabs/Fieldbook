/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package com.efficio.fieldbook.web.common.form;

import org.springframework.web.multipart.MultipartFile;

/**
 * The Class ImportCrossesForm.
 */
public class ImportCrossesForm {

	/** The file. */
	private MultipartFile file;

	/** The has error. */
	private String hasError;

	public MultipartFile getFile() {
		return this.file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

	public String getHasError() {
		return this.hasError;
	}

	public void setHasError(String hasError) {
		this.hasError = hasError;
	}

}
