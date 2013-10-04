package com.efficio.fieldbook.service.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.BeanInitializationException;

import com.efficio.fieldbook.service.FileService;

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

}
