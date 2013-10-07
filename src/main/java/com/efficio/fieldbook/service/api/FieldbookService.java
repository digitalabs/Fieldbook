package com.efficio.fieldbook.service.api;

import java.io.IOException;
import java.io.InputStream;

public interface FieldbookService {
	/**
     * Takes in an input stream representing the Excel file to be read, and returns the temporary file name used to store it in the system
     *
     * @param in
     * @return
     */
    public String storeUserWorkbook(InputStream in) throws IOException;
}
