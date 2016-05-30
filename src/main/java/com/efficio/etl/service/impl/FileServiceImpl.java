
package com.efficio.etl.service.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.Properties;

import javax.annotation.Resource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.OfficeXmlFileException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.generationcp.middleware.exceptions.WorkbookParserException;
import org.generationcp.middleware.util.Message;
import org.generationcp.middleware.util.PoiUtil;
import org.springframework.beans.factory.BeanInitializationException;

import com.efficio.etl.service.FileService;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */

public class FileServiceImpl implements FileService {

	public static final String TEMP_FILE_EXTENSION = ".dsi.tmp";

	private final String uploadDirectory;

	private TempFilenameFilter filter;
	private OldTempFilenameFilter oldFilter;

	@Resource
	private Properties configProperties;

	public FileServiceImpl(String uploadDirectory) {
		this.uploadDirectory = uploadDirectory;
	}

	@Override
	public void deleteTempFile(String currentFilename) throws IOException {
		String filePath = this.getFilePath(currentFilename);
		File file = new File(filePath);

		file.delete();
	}

	@Override
	public Workbook retrieveWorkbook(String currentFilename) throws IOException {
		String fileAbsolutePath = this.getFilePath(currentFilename);
		InputStream inp = new FileInputStream(fileAbsolutePath);
		InputStream inp2 = new FileInputStream(fileAbsolutePath);
		Workbook wb;
		try {
			wb = new HSSFWorkbook(inp);
		} catch (OfficeXmlFileException ee) {
			wb = new XSSFWorkbook(inp2);
		} finally {
			inp.close();
			inp2.close();
		}
		return wb;
	}

	@Override
	public Workbook retrieveWorkbookWithValidation(String currentFilename) throws IOException, WorkbookParserException {
		String fileAbsolutePath = this.getFilePath(currentFilename);
		InputStream inp = new FileInputStream(fileAbsolutePath);
		InputStream inp2 = new FileInputStream(fileAbsolutePath);
		Workbook wb;
		try {
			wb = new HSSFWorkbook(inp);
		} catch (OfficeXmlFileException ee) {
			// TODO: handle exception
			int maxLimit = 65000;
			Boolean overLimit = PoiUtil.isAnySheetRowsOverMaxLimit(fileAbsolutePath, maxLimit);
			if (overLimit) {
				WorkbookParserException workbookParserException = new WorkbookParserException("");
				workbookParserException
						.addMessage(new Message("error.file.is.too.large", new DecimalFormat("###,###,###").format(maxLimit)));
				throw workbookParserException;
			} else {
				wb = new XSSFWorkbook(inp2);
			}

		} finally {
			inp.close();
			inp2.close();
		}
		return wb;
	}

	public void init() {
		File file = new File(this.uploadDirectory);

		if (!file.isDirectory() && !file.exists()) {
			if (!file.mkdirs()) {
				throw new BeanInitializationException("Specified upload directory does not exist : " + this.uploadDirectory
						+ " isDirectory: " + file.isDirectory() + " exists: " + file.exists());
			}
		}

		this.filter = new TempFilenameFilter();
		this.oldFilter = new OldTempFilenameFilter();

		boolean deleteOldStyleTempFiles = Boolean.parseBoolean(this.configProperties.getProperty("delete.old.format.temp", "false"));

		this.clearTempDirectory(deleteOldStyleTempFiles);
	}

	@Override
	public String saveTemporaryFile(InputStream userFile) throws IOException {
		String tempFileName = RandomStringUtils.randomAlphanumeric(15) + FileServiceImpl.TEMP_FILE_EXTENSION;

		File file;
		FileOutputStream fos = null;
		try {
			file = new File(this.getFilePath(tempFileName));
			file.createNewFile();
			fos = new FileOutputStream(file);

		} finally {
			IOUtils.closeQuietly(fos);
		}

		return tempFileName;

	}

	protected String getFilePath(String tempFilename) {
		return this.uploadDirectory + File.separator + tempFilename;
	}

	public void clearTempDirectory(boolean includeOldStyleTempFiles) {
		File file = new File(this.uploadDirectory);

		File[] tempFiles = file.listFiles(this.filter);

		for (File tempFile : tempFiles) {
			tempFile.delete();
		}

		if (includeOldStyleTempFiles) {
			tempFiles = file.listFiles(this.oldFilter);

			for (File tempFile : tempFiles) {
				tempFile.delete();
			}
		}
	}

	@Override
	public File retrieveWorkbookFile(String currentFilename) throws IOException {
		return new File(this.getFilePath(currentFilename));

	}

	class TempFilenameFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) {
			return name.lastIndexOf(FileServiceImpl.TEMP_FILE_EXTENSION) != -1;
		}
	}

	class OldTempFilenameFilter implements FilenameFilter {

		@Override
		public boolean accept(File dir, String name) {
			return name.length() == 15 && !name.contains(".");
		}
	}
}
