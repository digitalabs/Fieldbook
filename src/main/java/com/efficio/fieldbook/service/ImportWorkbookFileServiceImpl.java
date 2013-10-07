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
package com.efficio.fieldbook.service;

import java.io.File;
import java.io.IOException;

import javax.annotation.Resource;

import com.efficio.fieldbook.service.api.ImportWorkbookFileService;
import com.efficio.fieldbook.web.bean.UserSelection;
import com.efficio.fieldbook.service.api.FileService;

/**
 * 
 * @author Joyce Avestro
 *
 */
public class ImportWorkbookFileServiceImpl implements ImportWorkbookFileService {

    @Resource
    private FileService fileService;

    /**
     * @param userSelection
     * @return
     * @throws IOException
     */
    @Override
    public File retrieveCurrentWorkbookAsFile(UserSelection userSelection) throws IOException {
        return getFileService().retrieveFileFromFileName(userSelection.getServerFileName());
    }


    public FileService getFileService() {
        return fileService;
    }

    public void setFileService(FileService fileService) {
        this.fileService = fileService;
    }

}
