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
 * This is used for import list inventory (Stocks).
 */
public class ImportStockForm {

	/** The file. */
	private MultipartFile file;
	private Integer stockListId;

	public MultipartFile getFile() {
		return this.file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

	public Integer getStockListId() {
		return this.stockListId;
	}

	public void setStockListId(Integer stockListId) {
		this.stockListId = stockListId;
	}

}
