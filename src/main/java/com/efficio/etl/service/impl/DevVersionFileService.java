
package com.efficio.etl.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.efficio.etl.service.FileService;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 *
 * This is a temporary implementation of the file service that only retrieves a pre-configured file, ignoring user parameters
 */
public class DevVersionFileService implements FileService {

	private String fileLocation;

    private static final Logger LOG = LoggerFactory.getLogger(DevVersionFileService.class);

	@Override
	public String saveTemporaryFile(InputStream userFile) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public Workbook retrieveWorkbook(String currentFilename) throws IOException {
		InputStream is = new FileInputStream(this.fileLocation);

		try {
			Workbook workbook = WorkbookFactory.create(is);

			return workbook;
		} catch (InvalidFormatException e) {
		  	DevVersionFileService.LOG.error(e.getMessage(), e);
			return null;
		} finally {
			IOUtils.closeQuietly(is);
		}
	}

	@Override
	public void deleteTempFile(String currentFilename) throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public File retrieveWorkbookFile(String currentFilename) throws IOException {
		return null;
	}

	@Override
	public Workbook retrieveWorkbookWithValidation(String serverFileName) throws IOException, WorkbookParserException {
		// TODO Auto-generated method stub
		return null;
	}
}
