package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.web.common.service.ImportInventoryService;
import com.efficio.fieldbook.web.util.parsing.InventoryImportParser;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedInventoryList;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.InventoryService;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte

 */
public class ImportInventoryServiceImpl implements ImportInventoryService{

	@Resource
	private InventoryImportParser parser;

	@Resource
	private InventoryService inventoryService;

	@Override public ImportedInventoryList parseFile(MultipartFile file) throws
			FileParsingException{

		return parser.parseFile(file);
	}

	@Override public boolean mergeImportedData(List<InventoryDetails> originalList,
			ImportedInventoryList importedDataObject) {
		List<InventoryDetails> importedList = importedDataObject.getImportedInventoryDetails();

		Map<Integer, InventoryDetails> originalDetailMap = convertToMap(originalList);
		Map<Integer, InventoryDetails> importedDetailMap = convertToMap(importedList);

		boolean possibleOverwrite = false;

		for (Map.Entry<Integer, InventoryDetails> detailsEntry : originalDetailMap
				.entrySet()) {
			InventoryDetails original = detailsEntry.getValue();
			InventoryDetails imported = importedDetailMap.get(detailsEntry.getKey());

			if (imported == null) {
				continue;
			}

			possibleOverwrite |= mergeIndividualDetailData(original, imported);
		}

		return possibleOverwrite;
	}

	protected boolean mergeIndividualDetailData(InventoryDetails original, InventoryDetails imported) {


		boolean possibleOverwrite = false;

		if (! (isEmpty(imported.getAmount()) || isEmpty(imported.getScaleName()) || isEmpty(imported.getLocationName())) ) {
			possibleOverwrite = (! (isEmpty(original.getAmount()) || isEmpty(original.getComment())|| isEmpty(original.getLocationName())
					|| isEmpty(original.getScaleName())));
			original.setAmount(imported.getAmount());
			original.setLocationAbbr(imported.getLocationAbbr());
			original.setScaleName(imported.getScaleName());
			original.setScaleId(imported.getScaleId());
			original.setLocationName(imported.getLocationName());
			original.setLocationId(imported.getLocationId());
			original.setComment(imported.getComment());
		}

		return possibleOverwrite;
	}

	protected boolean isEmpty(Number numberValue) {
		return numberValue == null || (numberValue.intValue() == 0 && numberValue.doubleValue() == 0.0);
	}

	protected boolean isEmpty(String stringValue) {
		return stringValue == null || stringValue.isEmpty();
	}


	protected Map<Integer, InventoryDetails> convertToMap(List<InventoryDetails> detailList) {
		Map<Integer, InventoryDetails> detailMap = new HashMap<>();

		for (InventoryDetails inventoryDetails : detailList) {
			detailMap.put(inventoryDetails.getGid(), inventoryDetails);
		}

		return detailMap;
	}

	protected List<InventoryDetails> filterBlankDetails(List<InventoryDetails> originalList) {
		List<InventoryDetails> list = new ArrayList<>();

		for (InventoryDetails inventoryDetails : originalList) {
			if (inventoryDetails.getLocationId() != null) {
				list.add(inventoryDetails);
			}
		}

		return list;
	}
}
