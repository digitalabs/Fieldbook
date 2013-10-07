package com.efficio.fieldbook.web.nursery.service.impl;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Resource;

import com.efficio.fieldbook.service.api.FileService;
import com.efficio.fieldbook.web.nursery.service.ImportGermplasmFileService;

public class ImportGermplasmFileServiceImpl implements ImportGermplasmFileService{
	@Resource
    private FileService fileService;

	@Override
    public String storeImportGermplasmWorkbook(InputStream in) throws IOException {
        return getFileService().saveTemporaryFile(in);
    }
	
	public FileService getFileService() {
        return fileService;
    }
}
