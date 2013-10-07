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
package com.efficio.fieldbook.service.api;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Workbook;

public interface FileService{

    /**
     * 
     * @param userFile
     *            The input stream of the file to be saved
     * @return
     */
    public String saveTemporaryFile(InputStream userFile) throws IOException;

    public Workbook retrieveWorkbook(String currentFilename) throws IOException;

    /**
     * Retrieves a File object based on the given file name
     * 
     * @param currentFilename
     * @return
     * @throws IOException
     */
    public File retrieveFileFromFileName(String currentFilename) throws IOException;


}
