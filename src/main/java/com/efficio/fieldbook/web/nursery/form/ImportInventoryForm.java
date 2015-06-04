
package com.efficio.fieldbook.web.nursery.form;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public class ImportInventoryForm {

	private MultipartFile file;

	private int targetListId;

	private String importSource;

	public ImportInventoryForm() {
	}

	public MultipartFile getFile() {
		return this.file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

	public int getTargetListId() {
		return this.targetListId;
	}

	public void setTargetListId(int targetListId) {
		this.targetListId = targetListId;
	}

	public String getImportSource() {
		return this.importSource;
	}

	public void setImportSource(String importSource) {
		this.importSource = importSource;
	}
}
