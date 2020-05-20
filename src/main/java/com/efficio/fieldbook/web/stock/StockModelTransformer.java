package com.efficio.fieldbook.web.stock;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.interfaces.GermplasmExportSource;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.dms.StockModel;
import org.generationcp.middleware.pojos.dms.StockProperty;
import org.generationcp.middleware.service.api.OntologyService;
import org.springframework.beans.factory.annotation.Configurable;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Configurable
// FIXME: IBP-3697 Is it possible to use ModelMapper instead???
public class StockModelTransformer {

	public StockModelTransformer() {
		// this constructor is necessary for aop proxy
	}

	@Resource
	private OntologyService ontologyService;

	@Resource
	private ContextUtil contextUtil;

	public List<ImportedGermplasm> tranformToImportedGermplasm(final List<StockModel> stockModelList,
		final Map<Integer, String> inventoryStockIdMap) {

		final Map<Integer, String> checkTypesDescriptionMap =
			this.ontologyService.getStandardVariable(TermId.CHECK.getId(), this.contextUtil.getCurrentProgramUUID())
				.getEnumerations().stream().collect(Collectors.toMap(Enumeration::getId, Enumeration::getDescription));

		final List<ImportedGermplasm> importedGermplasmList = new ArrayList<>();
		int index = 1;
		if (stockModelList != null && !stockModelList.isEmpty()) {
			for (final StockModel stockModel : stockModelList) {
				final Germplasm germplasm = stockModel.getGermplasm();
				final ImportedGermplasm importedGermplasm = new ImportedGermplasm();
				final String entryTypeId = this.findStockPropValue(TermId.ENTRY_TYPE.getId(), stockModel.getProperties());
				final String seedSource = this.findStockPropValue(TermId.SEED_SOURCE.getId(), stockModel.getProperties());
				importedGermplasm.setEntryTypeName(checkTypesDescriptionMap.getOrDefault(Integer.valueOf(entryTypeId), ""));
				importedGermplasm.setEntryTypeValue(entryTypeId);
				importedGermplasm.setEntryTypeCategoricalID(Integer.valueOf(entryTypeId));
				importedGermplasm.setCross(germplasm.getCrossName());
				importedGermplasm.setDesig(stockModel.getName());
				importedGermplasm.setEntryCode(stockModel.getValue());
				importedGermplasm.setEntryId(Integer.valueOf(stockModel.getUniqueName()));
				importedGermplasm.setGid(germplasm.getGid().toString());
				importedGermplasm.setMgid(germplasm.getMgid());
				importedGermplasm.setSource(seedSource);
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

		final Map<Integer, String> checkTypesDescriptionMap =
			this.ontologyService.getStandardVariable(TermId.CHECK.getId(), this.contextUtil.getCurrentProgramUUID())
				.getEnumerations().stream().collect(Collectors.toMap(Enumeration::getId, Enumeration::getDescription));

		final List<GermplasmExportSource> germplasmExportSourceList = new ArrayList<>();
		if (stockModelList != null && !stockModelList.isEmpty()) {
			for (final StockModel stockModel : stockModelList) {

				final Germplasm germplasm = stockModel.getGermplasm();
				final String checkTypeId = this.findStockPropValue(TermId.ENTRY_TYPE.getId(), stockModel.getProperties());
				final String seedSource = this.findStockPropValue(TermId.SEED_SOURCE.getId(), stockModel.getProperties());

				final GermplasmExportSource germplasmExportSource = new GermplasmExportSource() {

					@Override
					public Integer getGermplasmId() {
						return germplasm.getGid();
					}

					@Override
					public Integer getCheckType() {
						return Integer.valueOf(checkTypeId);
					}

					@Override
					public String getCheckTypeDescription() {
						return checkTypesDescriptionMap.getOrDefault(Integer.valueOf(checkTypeId), "");
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
						return seedSource;
					}

					@Override
					public String getDesignation() {
						return stockModel.getName();
					}

					@Override
					public String getGroupName() {
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

			final StockProperty seedSourceProperty = new StockProperty();
			seedSourceProperty.setStock(stockModel);
			seedSourceProperty.setRank(2);
			seedSourceProperty.setTypeId(TermId.SEED_SOURCE.getId());
			seedSourceProperty.setValue(importedGermplasm.getSource());
			stockProperties.add(seedSourceProperty);

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
