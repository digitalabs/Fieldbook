package com.efficio.fieldbook.web.nursery.bean;

import org.generationcp.middleware.domain.inventory.InventoryDetails;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */
public class ImportedInventoryList {

	private static final long serialVersionUID = 1L;
	private List<InventoryDetails> importedInventoryDetails;

	private String filename;

	public ImportedInventoryList(String filename) {
		this.filename = filename;
	}

	public ImportedInventoryList(
			List<InventoryDetails> importedInventoryDetails, String filename) {
		this.importedInventoryDetails = importedInventoryDetails;
		this.filename = filename;
	}

	public List<InventoryDetails> getImportedInventoryDetails() {
		return importedInventoryDetails;
	}

	public void setImportedInventoryDetails(List<InventoryDetails> importedInventoryDetails) {
		this.importedInventoryDetails = importedInventoryDetails;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}
