package com.efficio.fieldbook.web.common.service;

import com.efficio.fieldbook.web.common.exception.FileParsingException;
import com.efficio.fieldbook.web.nursery.bean.ImportedInventoryList;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte

 */
public interface ImportInventoryService {
	public ImportedInventoryList parseFile(MultipartFile file) throws FileParsingException;

	public boolean mergeImportedData(List<InventoryDetails> originalList, ImportedInventoryList imported);

	public boolean saveUpdatedInventoryDetails(List<InventoryDetails> updatedList, Integer currentUserID, Integer listID) throws
			MiddlewareQueryException;
}
