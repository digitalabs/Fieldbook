
package com.efficio.fieldbook.web.common.service;

import java.util.List;
import java.util.Map;

import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedInventoryList;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.util.FieldbookException;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public interface ImportInventoryService {

	ImportedInventoryList parseFile(MultipartFile file, Map<String, Object> additionalParams) throws FileParsingException;

	boolean mergeImportedData(List<InventoryDetails> originalList, ImportedInventoryList imported);

	void mergeInventoryDetails(List<InventoryDetails> originalList, ImportedInventoryList imported, GermplasmListType germplasmListType)
			throws FieldbookException;
}
