package com.efficio.fieldbook.web.stock;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.interfaces.GermplasmExportSource;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.dms.StockModel;
import org.generationcp.middleware.pojos.dms.StockProperty;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StockModelTransformer {

	public List<ImportedGermplasm> tranformToImportedGermplasm(final List<StockModel> stockModelList,
		final Map<Integer, String> inventoryStockIdMap) {

		final List<ImportedGermplasm> importedGermplasmList = new ArrayList<>();
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
				importedGermplasm.setEntryCode(stockModel.getValue());
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

	public List<GermplasmExportSource> tranformToGermplasmExportSource(final List<StockModel> stockModelList,
		final Map<Integer, String> inventoryStockIdMap) {

		final List<GermplasmExportSource> germplasmExportSourceList = new ArrayList<>();
		if (stockModelList != null && !stockModelList.isEmpty()) {
			for (final StockModel stockModel : stockModelList) {

				final Germplasm germplasm = stockModel.getGermplasm();
				final String entryTypeId = this.findStockPropValue(TermId.ENTRY_TYPE.getId(), stockModel.getProperties());

				final GermplasmExportSource germplasmExportSource = new GermplasmExportSource() {

					@Override
					public Integer getGermplasmId() {
						return germplasm.getGid();
					}

					@Override
					public Integer getCheckType() {
						return Integer.valueOf(entryTypeId);
					}

					@Override
					public String getCheckTypeDescription() {
						// TODO: IBP-3697 Check where to get the check type description
						return null;
					}

					@Override
					public Integer getEntryId() {
						return Integer.valueOf(stockModel.getUniqueName());
					}

					@Override
					public String getEntryCode() {
						return stockModel.getValue();
					}

					@Override
					public String getSeedSource() {
						// TODO: IBP-3697 Check where to get the source name
						return germplasm.getCrossName();
					}

					@Override
					public String getDesignation() {
						return stockModel.getName();
					}

					@Override
					public String getGroupName() {
						// TODO: IBP-3697 Check where to get the group name
						return germplasm.getMgid().toString();
					}

					@Override
					public String getFemaleParentDesignation() {
						return null;
					}

					@Override
					public Integer getFemaleGid() {
						return null;
					}

					@Override
					public String getMaleParentDesignation() {
						return null;
					}

					@Override
					public Integer getMaleGid() {
						return null;
					}

					@Override
					public String getStockIDs() {
						return inventoryStockIdMap.getOrDefault(germplasm.getGid(), "");
					}

					@Override
					public String getSeedAmount() {
						return null;
					}

					@Override
					public Integer getGroupId() {
						return germplasm.getMgid();
					}

					@Override
					public String getNotes() {
						return null;
					}

					@Override
					public Integer getListDataId() {
						return null;
					}
				};

				germplasmExportSourceList.add(germplasmExportSource);
			}
		}

		return germplasmExportSourceList;
	}

	public List<StockModel> transformToStockModels(final int studyId, final List<ImportedGermplasm> importedGermplasmList) {
		final List<StockModel> stockModelList = new ArrayList<>();
		for (final ImportedGermplasm importedGermplasm : importedGermplasmList) {
			final StockModel stockModel = new StockModel();
			stockModel.setProjectId(studyId);
			stockModel.setName(importedGermplasm.getDesig());
			stockModel.setGermplasm(new Germplasm(Integer.valueOf(importedGermplasm.getGid())));
			stockModel.setTypeId(TermId.ENTRY_CODE.getId());
			stockModel.setValue(importedGermplasm.getEntryCode());
			stockModel.setUniqueName(importedGermplasm.getEntryId().toString());
			stockModel.setIsObsolete(false);

			final Set<StockProperty> stockProperties = new HashSet<>();
			final StockProperty entryTypeProperty = new StockProperty();
			entryTypeProperty.setStock(stockModel);
			entryTypeProperty.setRank(1);
			entryTypeProperty.setTypeId(TermId.ENTRY_TYPE.getId());
			entryTypeProperty.setValue(importedGermplasm.getEntryTypeCategoricalID().toString());
			stockProperties.add(entryTypeProperty);
			stockModel.setProperties(stockProperties);

			stockModelList.add(stockModel);
		}
		return stockModelList;
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
