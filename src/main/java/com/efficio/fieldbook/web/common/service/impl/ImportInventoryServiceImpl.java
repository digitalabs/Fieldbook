
package com.efficio.fieldbook.web.common.service.impl;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.common.service.ImportInventoryService;
import com.efficio.fieldbook.web.util.parsing.InventoryImportParser;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedInventoryList;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.springframework.context.MessageSource;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by IntelliJ IDEA. User: Daniel Villafuerte
 */
public class ImportInventoryServiceImpl implements ImportInventoryService {

	private static final String DEFAULT_AMOUNT_HEADER = "SEED_AMOUNT_G";

	@Resource
	private InventoryImportParser parser;

	@Resource
	private MessageSource messageSource;

	@Override
	public ImportedInventoryList parseFile(final MultipartFile file, final Map<String, Object> additionalParams) throws FileParsingException {
		return this.parser.parseFile(file, additionalParams);
	}

	@Override
	public boolean mergeImportedData(final List<InventoryDetails> originalList, final ImportedInventoryList importedDataObject) {
		final List<InventoryDetails> importedList = importedDataObject.getImportedInventoryDetails();

		final Map<Integer, InventoryDetails> originalDetailMap = this.convertToMap(originalList);
		final Map<Integer, InventoryDetails> importedDetailMap = this.convertToMap(importedList);

		boolean possibleOverwrite = false;

		for (final Map.Entry<Integer, InventoryDetails> detailsEntry : originalDetailMap.entrySet()) {
			final InventoryDetails original = detailsEntry.getValue();
			final InventoryDetails imported = importedDetailMap.get(detailsEntry.getKey());

			if (imported == null) {
				continue;
			}

			possibleOverwrite |= this.mergeIndividualDetailData(original, imported);
		}

		return possibleOverwrite;
	}

	boolean mergeIndividualDetailData(final InventoryDetails original, final InventoryDetails imported) {

		boolean possibleOverwrite = false;

		if (!(this.isEmpty(imported.getAmount()) || this.isEmpty(imported.getScaleName()) || this.isEmpty(imported.getLocationName()))) {
			possibleOverwrite = !(this.isEmpty(original.getAmount()) || this.isEmpty(original.getComment())
					|| this.isEmpty(original.getLocationName()) || this.isEmpty(original.getScaleName()));
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

	protected boolean isEmpty(final Number numberValue) {
		return numberValue == null || numberValue.intValue() == 0 && numberValue.doubleValue() == 0.0;
	}

	protected boolean isEmpty(final String stringValue) {
		return stringValue == null || stringValue.isEmpty();
	}

	private Map<Integer, InventoryDetails> convertToMap(final List<InventoryDetails> detailList) {
		final Map<Integer, InventoryDetails> detailMap = new HashMap<>();

		for (final InventoryDetails inventoryDetails : detailList) {
			detailMap.put(inventoryDetails.getGid(), inventoryDetails);
		}

		return detailMap;
	}

	@Override
	public void validateInventoryDetails(final List<InventoryDetails> inventoryDetailListFromDB, final ImportedInventoryList importedInventoryList,
			final GermplasmListType germplasmListType) throws FieldbookException {
		final List<InventoryDetails> inventoryDetailListFromImport = importedInventoryList.getImportedInventoryDetails();
		this.validateImportedInventoryDetails(inventoryDetailListFromImport);
		this.checkNumberOfEntries(inventoryDetailListFromDB, inventoryDetailListFromImport);
		this.checkEntriesIfTheyMatch(inventoryDetailListFromImport, inventoryDetailListFromDB);
	}

	private void validateImportedInventoryDetails(final List<InventoryDetails> inventoryDetailListFromImport) throws FieldbookException {
		String amountHeader = null;
		for (final InventoryDetails inventoryDetailsFromImport : inventoryDetailListFromImport) {
			amountHeader = (amountHeader == null ? inventoryDetailsFromImport.getScaleName() : amountHeader);
			if (inventoryDetailsFromImport.getLocationId() != null && inventoryDetailsFromImport.getScaleId() != null
					&& inventoryDetailsFromImport.getAmount() != null) {
				continue;
			} else if (inventoryDetailsFromImport.getLocationId() != null || inventoryDetailsFromImport.getScaleId() != null
					|| (inventoryDetailsFromImport.getAmount() != null && inventoryDetailsFromImport.getAmount() != 0)) {
				throw new FieldbookException(this.messageSource.getMessage("common.error.import.missing.inventory.values.for.row",
						new Object[] {this.getAmountHeader(amountHeader, inventoryDetailListFromImport), inventoryDetailsFromImport.getEntryId()}, Locale.getDefault()));
			}
		}
	}

	private String getAmountHeader(final String amountHeader, final List<InventoryDetails> inventoryDetailsList) {
		if(amountHeader==null){
			for(final InventoryDetails inventoryDetails: inventoryDetailsList){
				if(inventoryDetails.getScaleName() != null) {
					return inventoryDetails.getScaleName().toUpperCase();
				}
			}
			return DEFAULT_AMOUNT_HEADER;
		}
		return amountHeader.toUpperCase();
	}

	private void checkEntriesIfTheyMatch(final List<InventoryDetails> inventoryDetailListFromImport,
			final List<InventoryDetails> inventoryDetailListFromDB) throws FieldbookException {
		final Map<Integer, InventoryDetails> entryIdInventoryMap = new HashMap<>();

		for (final InventoryDetails inventoryDetailsFromDB : inventoryDetailListFromDB) {
			entryIdInventoryMap.put(inventoryDetailsFromDB.getEntryId(), inventoryDetailsFromDB);
		}
		for (final InventoryDetails inventoryDetailsFromImport : inventoryDetailListFromImport) {
			final InventoryDetails inventoryDetailsFromDB = entryIdInventoryMap.get(inventoryDetailsFromImport.getEntryId());
			if (inventoryDetailsFromDB == null) {
				throw new FieldbookException(this.messageSource.getMessage("common.error.import.entry.id.does.not.exist",
						new Object[] {inventoryDetailsFromImport.getEntryId().toString()}, Locale.getDefault()));
			} else if (!inventoryDetailsFromDB.getGid().equals(inventoryDetailsFromImport.getGid())) {
				throw new FieldbookException(this.messageSource.getMessage(
						"common.error.import.gid.does.not.match", new Object[] {inventoryDetailsFromDB.getEntryId().toString(),
								inventoryDetailsFromDB.getGid().toString(), inventoryDetailsFromImport.getGid().toString()},
						Locale.getDefault()));
			}
		}
	}

	void updateInventoryDetailsFromImport(final InventoryDetails inventoryDetailsFromDB, final InventoryDetails inventoryDetailsFromImport,
			final GermplasmListType germplasmListType) {
		if (GermplasmListType.isCrosses(germplasmListType)) {
			if (inventoryDetailsFromImport.getDuplicate() != null) {
				inventoryDetailsFromDB.setDuplicate(inventoryDetailsFromImport.getDuplicate());
			}
			if (inventoryDetailsFromImport.getBulkWith() != null) {
				inventoryDetailsFromDB.setBulkWith(inventoryDetailsFromImport.getBulkWith());
			}
			if (inventoryDetailsFromImport.getBulkCompl() != null) {
				inventoryDetailsFromDB.setBulkCompl(inventoryDetailsFromImport.getBulkCompl());
			}
		}

		if (inventoryDetailsFromImport.getLocationId() != null && inventoryDetailsFromImport.getScaleId() != null
				&& inventoryDetailsFromImport.getAmount() != null) {
			inventoryDetailsFromDB.setLocationId(inventoryDetailsFromImport.getLocationId());
			inventoryDetailsFromDB.setScaleId(inventoryDetailsFromImport.getScaleId());
			inventoryDetailsFromDB.setAmount(inventoryDetailsFromImport.getAmount());
		}
		inventoryDetailsFromDB.setComment(inventoryDetailsFromImport.getComment());
	}

	private void checkNumberOfEntries(final List<InventoryDetails> inventoryDetailListFromDB,
			final List<InventoryDetails> inventoryDetailListFromImport) throws FieldbookException {
		if (inventoryDetailListFromImport.size() > inventoryDetailListFromDB.size()) {
			throw new FieldbookException(this.messageSource.getMessage("common.error.import.incorrect.number.of.entries",
					new Object[] {inventoryDetailListFromDB.size()}, Locale.getDefault()));
		}
	}

	@Override
	public boolean hasConflict(final List<InventoryDetails> inventoryDetailListFromDB, final ImportedInventoryList importedInventoryList) {
		final Map<Integer, InventoryDetails> entryIdInventoryMap = new HashMap<>();
		final List<InventoryDetails> inventoryDetailListFromImport = importedInventoryList.getImportedInventoryDetails();
		for (final InventoryDetails inventoryDetailsFromDB : inventoryDetailListFromDB) {
			entryIdInventoryMap.put(inventoryDetailsFromDB.getEntryId(), inventoryDetailsFromDB);
		}
		for (final InventoryDetails inventoryDetailsFromImport : inventoryDetailListFromImport) {
			final InventoryDetails inventoryDetailsFromDB = entryIdInventoryMap.get(inventoryDetailsFromImport.getEntryId());
			if (this.checkConflict(inventoryDetailsFromImport, inventoryDetailsFromDB)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void mergeInventoryDetails(final List<InventoryDetails> inventoryDetailListFromDB, final ImportedInventoryList importedInventoryList,
			final GermplasmListType germplasmListType) {
		final Map<Integer, InventoryDetails> entryIdInventoryMap = new HashMap<>();
		final List<InventoryDetails> inventoryDetailListFromImport = importedInventoryList.getImportedInventoryDetails();

		for (final InventoryDetails inventoryDetailsFromDB : inventoryDetailListFromDB) {
			entryIdInventoryMap.put(inventoryDetailsFromDB.getEntryId(), inventoryDetailsFromDB);
		}
		for (final InventoryDetails inventoryDetailsFromImport : inventoryDetailListFromImport) {
			final InventoryDetails inventoryDetailsFromDB = entryIdInventoryMap.get(inventoryDetailsFromImport.getEntryId());
			this.updateInventoryDetailsFromImport(inventoryDetailsFromDB, inventoryDetailsFromImport, germplasmListType);
		}
	}

	private boolean checkConflict(final InventoryDetails inventoryDetailsFromImport, final InventoryDetails inventoryDetailsFromDB) {
		final boolean isLocationNotEq = inventoryDetailsFromImport.getLocationName() != null && inventoryDetailsFromDB.getLocationName() != null
				&& !inventoryDetailsFromImport.getLocationName().equals(inventoryDetailsFromDB.getLocationName());

		final boolean isAmountConflict = this.checkConflictAmount(inventoryDetailsFromImport, inventoryDetailsFromDB);

		final boolean isInventoryScaleNotEq = inventoryDetailsFromImport.getScaleName() != null && inventoryDetailsFromDB.getScaleName() != null
				&& !inventoryDetailsFromImport.getScaleName().equals(inventoryDetailsFromDB.getScaleName());

		return isLocationNotEq || isAmountConflict || isInventoryScaleNotEq;
	}

	private boolean checkConflictAmount(final InventoryDetails inventoryDetailsFromImport, final InventoryDetails inventoryDetailsFromDB) {
		return inventoryDetailsFromDB.getAmount() != null && inventoryDetailsFromDB.getAmount() != 0
				&& inventoryDetailsFromImport.getAmount() != null && inventoryDetailsFromImport.getAmount() != 0
				&& !inventoryDetailsFromImport.getAmount().toString().equals(inventoryDetailsFromDB.getAmount().toString());
	}
}
