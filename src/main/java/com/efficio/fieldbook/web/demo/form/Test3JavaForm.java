package com.efficio.fieldbook.web.demo.form;

import org.springframework.web.multipart.MultipartFile;

public class Test3JavaForm {
	private MultipartFile file;
    private String importType = "";
    public String fileName = "";
    
    public MultipartFile getFile() {

        return file;

    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

	public String getImportType() {
		return importType;
	}

	public void setImportType(String importType) {
		this.importType = importType;
	}
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String name) {
		this.fileName = name;
	}
}
