package com.efficio.fieldbook.web.nursery.service;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasmMainInfo;

public interface ImportGermplasmFileService {
	/**
     * Takes in an input stream representing the Excel file to be read, and returns the temporary file name used to store it in the system
     *
     * @param in
     * @return
     */
    public ImportedGermplasmMainInfo storeImportGermplasmWorkbook(MultipartFile multipartFile) throws IOException;
    public ImportedGermplasmMainInfo processWorkbook(ImportedGermplasmMainInfo mainInfo);
}
