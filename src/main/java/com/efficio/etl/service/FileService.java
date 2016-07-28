
package com.efficio.etl.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.apache.poi.ss.usermodel.Workbook;
import org.generationcp.middleware.exceptions.WorkbookParserException;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public interface FileService {

	/**
	 *
	 * @param userFile The input stream of the file to be saved
	 * @return
	 */
	public String saveTemporaryFile(InputStream userFile) throws IOException;

	public Workbook retrieveWorkbook(String currentFilename) throws IOException;

	public File retrieveWorkbookFile(String currentFilename) throws IOException;

	public void deleteTempFile(String currentFilename) throws IOException;

	public Workbook retrieveWorkbookWithValidation(String serverFileName) throws IOException, WorkbookParserException;
}
