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

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * The Interface FileService.
 */
public interface FileService{

    /**
     * Save temporary file.
     *
     * @param userFile The input stream of the file to be saved
     * @return the string
     * @throws IOException Signals that an I/O exception has occurred.
     */
    String saveTemporaryFile(InputStream userFile) throws IOException;

    /**
     * Retrieve workbook.
     *
     * @param currentFilename the current filename
     * @return the workbook
     * @throws IOException Signals that an I/O exception has occurred.
     */
    Workbook retrieveWorkbook(String currentFilename) throws IOException, InvalidFormatException;

    /**
     * Retrieves a File object based on the given file name.
     *
     * @param currentFilename the current filename
     * @return the file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    File retrieveFileFromFileName(String currentFilename) throws IOException;
    
    String getFilePath(String tempFilename);
}
