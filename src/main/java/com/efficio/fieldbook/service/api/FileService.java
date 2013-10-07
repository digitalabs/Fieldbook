package com.efficio.fieldbook.service.api;

import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Workbook;

public interface FileService {

	/**
    *
    * @param userFile The input stream of the file to be saved
    * @return
    */
   public String saveTemporaryFile(InputStream userFile) throws IOException;
   public Workbook retrieveWorkbook(String currentFilename) throws IOException;
}
