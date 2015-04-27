package com.efficio.fieldbook.web.nursery.bean;

import com.efficio.fieldbook.util.parsing.ImportedDescriptionDetails;

/**
 * Created by cyrus on 4/27/15.
 */
public class ImportedInventoryListWithDescription extends ImportedInventoryList {
	public final ImportedDescriptionDetails importedDescriptionDetails = new ImportedDescriptionDetails();

	public ImportedInventoryListWithDescription() {
	}

	@Override
	public void setFilename(String filename) {
		super.setFilename(filename);
		importedDescriptionDetails.setFilename(filename);
	}

	public ImportedDescriptionDetails getImportedDescriptionDetails() {
		return importedDescriptionDetails;
	}
}
