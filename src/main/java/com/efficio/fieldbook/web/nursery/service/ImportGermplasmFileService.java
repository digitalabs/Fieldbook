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
package com.efficio.fieldbook.web.nursery.service;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;

// TODO: Auto-generated Javadoc
/**
 * The Interface ImportGermplasmFileService.
 */
public interface ImportGermplasmFileService {
	
	/**
	 * Takes in an input stream representing the Excel file to be read, and returns the temporary file name used to store it in the system.
	 *
	 * @param multipartFile the multipart file
	 * @return the imported germplasm main info
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
    public ImportedGermplasmMainInfo storeImportGermplasmWorkbook(MultipartFile multipartFile) throws IOException;
    
    /**
     * Process workbook.
     *
     * @param mainInfo the main info
     * @return the imported germplasm main info
     */
    public ImportedGermplasmMainInfo processWorkbook(ImportedGermplasmMainInfo mainInfo);
    
    /**
     * Do process now.
     *
     * @param workbook the workbook
     * @param mainInfo the main info
     * @throws Exception the exception
     */
    public void doProcessNow(Workbook workbook, ImportedGermplasmMainInfo mainInfo) throws Exception;
}
