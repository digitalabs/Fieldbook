package com.efficio.fieldbook.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.BeanInitializationException;

import com.efficio.fieldbook.service.api.FileService;


public class FileServiceImpl implements FileService {
	
	private String uploadDirectory;

    public FileServiceImpl(String uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
    }

	@Override
    public String saveTemporaryFile(InputStream userFile) throws IOException {
        String tempFileName = RandomStringUtils.randomAlphanumeric(15);

        File file = null;
        FileOutputStream fos = null;
        try {
            file = new File(getFilePath(tempFileName));
            file.createNewFile();
            fos = new FileOutputStream(file);
            int bytes = IOUtils.copy(userFile, fos);

        } finally {
            IOUtils.closeQuietly(fos);
        }

        return tempFileName;


    }
	
	protected String getFilePath(String tempFilename) {
        return uploadDirectory + File.separator + tempFilename;
    }
	
	public void init() {
        File file = new File(uploadDirectory);

        if (!file.exists()) {
            throw new BeanInitializationException("Specified upload directory does not exist : " + uploadDirectory);
        }
    }
	
	 @Override
    public Workbook retrieveWorkbook(String currentFilename) throws IOException {
        InputStream is = new FileInputStream(getFilePath(currentFilename));

        try {
            Workbook workbook = WorkbookFactory.create(is);

            return workbook;
        } catch (InvalidFormatException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            return null;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

}
