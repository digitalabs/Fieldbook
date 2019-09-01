
package com.efficio.etl.web.bean;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public class FileUploadForm {

	private MultipartFile file;

	public MultipartFile getFile() {

		return this.file;

	}

	public void setFile(final MultipartFile file) {
		this.file = file;
	}
}
