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

package com.efficio.fieldbook.web.trial.service.impl;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;

import org.generationcp.commons.service.FileService;

import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.trial.service.ImportWorkbookFileService;

/**
 * The Class ImportWorkbookFileServiceImpl.
 *
 * @author Joyce Avestro
 */
public class ImportWorkbookFileServiceImpl implements ImportWorkbookFileService {

	/** The file service. */
	@Resource
	private FileService fileService;

	/**
	 * Retrieve current workbook as file.
	 *
	 * @param userSelection the user selection
	 * @return the file
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	@Override
	public File retrieveCurrentWorkbookAsFile(UserSelection userSelection) throws IOException {
		return this.getFileService().retrieveFileFromFileName(userSelection.getServerFileName());
	}

	/**
	 * Gets the file service.
	 *
	 * @return the file service
	 */
	public FileService getFileService() {
		return this.fileService;
	}

	/**
	 * Sets the file service.
	 *
	 * @param fileService the new file service
	 */
	public void setFileService(FileService fileService) {
		this.fileService = fileService;
	}

}
