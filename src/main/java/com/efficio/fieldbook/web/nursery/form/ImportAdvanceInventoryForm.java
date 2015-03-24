package com.efficio.fieldbook.web.nursery.form;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte

 */
public class ImportAdvanceInventoryForm {
	private MultipartFile file;

	private int targetListId;

	public ImportAdvanceInventoryForm() {
	}

	public MultipartFile getFile() {
		return file;
	}

	public void setFile(MultipartFile file) {
		this.file = file;
	}

	public int getTargetListId() {
		return targetListId;
	}

	public void setTargetListId(int targetListId) {
		this.targetListId = targetListId;
	}
}
