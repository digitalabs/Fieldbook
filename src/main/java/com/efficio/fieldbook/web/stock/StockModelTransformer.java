package com.efficio.fieldbook.web.stock;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.dms.StockModel;
import org.generationcp.middleware.pojos.dms.StockProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StockModelTransformer {

	public List<ImportedGermplasm> tranformToImportedGermplasm(final List<StockModel> stockModelList,
		final Map<Integer, String> inventoryStockIdMap) {

		List<ImportedGermplasm> importedGermplasmList = new ArrayList<>();
		int index = 1;
		if (stockModelList != null && !stockModelList.isEmpty()) {
			for (final StockModel stockModel : stockModelList) {
				final Germplasm germplasm = stockModel.getGermplasm();
				final ImportedGermplasm importedGermplasm = new ImportedGermplasm();
				final String entryTypeId = this.findStockPropValue(TermId.ENTRY_TYPE.getId(), stockModel.getProperties());
				importedGermplasm.setEntryTypeValue(entryTypeId);
				importedGermplasm.setEntryTypeCategoricalID(entryTypeId != null ? Integer.valueOf(entryTypeId) : null);
				importedGermplasm.setCross(germplasm.getCrossName());
				importedGermplasm.setDesig(stockModel.getName());
				importedGermplasm.setEntryCode(stockModel.getUniqueName());
				importedGermplasm.setEntryId(Integer.valueOf(stockModel.getUniqueName()));
				importedGermplasm.setGid(germplasm.getGid().toString());
				importedGermplasm.setMgid(germplasm.getMgid());
				// TODO: IBP-3697 Check where to get the source name
				importedGermplasm.setSource(germplasm.getCrossName());
				// TODO: IBP-3697 Check where to get the group name
				importedGermplasm.setGroupName(germplasm.getMgid().toString());
				importedGermplasm.setGroupId(germplasm.getMgid());
				importedGermplasm.setStockIDs(inventoryStockIdMap.getOrDefault(germplasm.getGid(), ""));
				importedGermplasm.setIndex(index++);
				importedGermplasmList.add(importedGermplasm);
			}
		}
		return importedGermplasmList;

	}

	private String findStockPropValue(final int termId, final Set<StockProperty> properties) {
		if (properties != null) {
			for (final StockProperty property : properties) {
				if (termId == property.getTypeId()) {
					return property.getValue();
				}
			}
		}
		return null;
	}

}
