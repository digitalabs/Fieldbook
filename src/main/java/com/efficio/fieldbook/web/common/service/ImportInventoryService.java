package com.efficio.fieldbook.web.common.service;

import java.util.List;

import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedInventoryList;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte

 */
public interface ImportInventoryService {
	ImportedInventoryList parseFile(MultipartFile file) throws FileParsingException;

	boolean mergeImportedData(List<InventoryDetails> originalList, ImportedInventoryList imported);
}
