
package com.efficio.etl.web.bean;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public class FileUploadForm {

	private MultipartFile file;
	private String importType = "";

	public MultipartFile getFile() {

		return this.file;

	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

	public String getImportType() {
		return this.importType;
	}

	public void setImportType(String importType) {
		this.importType = importType;
	}
}
