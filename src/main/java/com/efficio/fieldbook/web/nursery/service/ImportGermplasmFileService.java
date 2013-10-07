package com.efficio.fieldbook.web.nursery.service;

import java.io.IOException;
import java.io.InputStream;

public interface ImportGermplasmFileService {
	/**
     * Takes in an input stream representing the Excel file to be read, and returns the temporary file name used to store it in the system
     *
     * @param in
     * @return
     */
    public String storeImportGermplasmWorkbook(InputStream in) throws IOException;
}
