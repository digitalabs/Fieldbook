package com.efficio.fieldbook.service.api;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Resource;

import com.efficio.fieldbook.service.FileService;
import com.efficio.fieldbook.service.FieldbookService;

public class FieldbookServiceImpl implements FieldbookService{
	
	@Resource
    private FileService fileService;

	@Override
    public String storeUserWorkbook(InputStream in) throws IOException {
        return getFileService().saveTemporaryFile(in);
    }
	
	public FileService getFileService() {
        return fileService;
    }
}
