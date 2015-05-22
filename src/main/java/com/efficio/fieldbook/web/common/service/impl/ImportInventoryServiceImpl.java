package com.efficio.fieldbook.web.common.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedInventoryList;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.common.service.ImportInventoryService;
import com.efficio.fieldbook.web.util.parsing.InventoryImportParser;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte

 */
public class ImportInventoryServiceImpl implements ImportInventoryService{

	@Resource
	private InventoryImportParser parser;
	
	@Resource
	private MessageSource messageSource;

	@Override public ImportedInventoryList parseFile(MultipartFile file, Map<String,Object> additionalParams) 
			throws FileParsingException{
		return parser.parseFile(file,additionalParams);
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
	
	@Override 
	public void mergeInventoryDetails(List<InventoryDetails> inventoryDetailListFromDB,
			ImportedInventoryList importedInventoryList, GermplasmListType germplasmListType) throws FieldbookException {
		List<InventoryDetails> inventoryDetailListFromImport =
				importedInventoryList.getImportedInventoryDetails();
		checkNumberOfEntries(inventoryDetailListFromDB,inventoryDetailListFromImport);
		checkEntriesIfTheyMatchThenUpdate(inventoryDetailListFromImport,
				inventoryDetailListFromDB,germplasmListType);
	}

	private void checkEntriesIfTheyMatchThenUpdate(
			List<InventoryDetails> inventoryDetailListFromImport,
			List<InventoryDetails> inventoryDetailListFromDB,
			GermplasmListType germplasmListType) throws FieldbookException {
		Map<Integer,InventoryDetails> entryIdInventoryMap = new HashMap<Integer,InventoryDetails>();
		for (InventoryDetails inventoryDetailsFromDB : inventoryDetailListFromDB) {
			entryIdInventoryMap.put(inventoryDetailsFromDB.getEntryId(), inventoryDetailsFromDB);
		}
		for (InventoryDetails inventoryDetailsFromImport : inventoryDetailListFromImport) {
			InventoryDetails inventoryDetailsFromDB = 
					entryIdInventoryMap.get(inventoryDetailsFromImport.getEntryId());
			if(inventoryDetailsFromDB==null) {
				throw new FieldbookException(messageSource.getMessage(
						"common.error.import.entry.id.does.not.exist", new Object[]{
								inventoryDetailsFromImport.getEntryId().toString()},Locale.getDefault()));
			} else if(!inventoryDetailsFromDB.getGid().equals(inventoryDetailsFromImport.getGid())){
				throw new FieldbookException(messageSource.getMessage(
						"common.error.import.gid.does.not.match", new Object[]{
								inventoryDetailsFromDB.getEntryId().toString(),
								inventoryDetailsFromDB.getGid().toString(),
								inventoryDetailsFromImport.getGid().toString()},
								Locale.getDefault()));
			} else {
				updateInventoryDetailsFromImport(inventoryDetailsFromDB,inventoryDetailsFromImport,
						germplasmListType);
			}
		}
		
	}

	protected void updateInventoryDetailsFromImport(
			InventoryDetails inventoryDetailsFromDB,
			InventoryDetails inventoryDetailsFromImport,
			GermplasmListType germplasmListType) {
		if(germplasmListType == GermplasmListType.CROSSES) {
			inventoryDetailsFromDB.setBulkWith(inventoryDetailsFromImport.getBulkWith());
			inventoryDetailsFromDB.setBulkCompl(inventoryDetailsFromImport.getBulkCompl());
		}
		inventoryDetailsFromDB.setLocationId(inventoryDetailsFromImport.getLocationId());
		inventoryDetailsFromDB.setScaleId(inventoryDetailsFromImport.getScaleId());
		inventoryDetailsFromDB.setAmount(inventoryDetailsFromImport.getAmount());
		inventoryDetailsFromDB.setComment(inventoryDetailsFromImport.getComment());
	}

	private void checkNumberOfEntries(
			List<InventoryDetails> inventoryDetailListFromDB,
			List<InventoryDetails> inventoryDetailListFromImport) throws 
			FieldbookException {
		if(inventoryDetailListFromImport.size() > inventoryDetailListFromDB.size()) {
			throw new FieldbookException(messageSource.getMessage(
					"common.error.import.incorrect.number.of.entries", new Object[]{
							inventoryDetailListFromDB.size()},Locale.getDefault()));
		}
	}
}
