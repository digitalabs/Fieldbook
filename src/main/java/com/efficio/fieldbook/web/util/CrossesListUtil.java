package com.efficio.fieldbook.web.util;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ListDataProject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.efficio.fieldbook.web.common.service.CrossingService;

@Component
public class CrossesListUtil {

	@Autowired
	private OntologyDataManager ontologyDataManager;

	@Resource
	private CrossingService crossingService;

	public Map<String, Object> generateDatatableDataMap(final ImportedCrosses importedCrosses) {

		final Map<String, Object> dataMap = new HashMap<>();

		dataMap.put(getTermNameFromOntology(ColumnLabels.ENTRY_ID), importedCrosses.getEntryId());
		dataMap.put(getTermNameFromOntology(ColumnLabels.PARENTAGE), importedCrosses.getCross());
		dataMap.put(getTermNameFromOntology(ColumnLabels.ENTRY_CODE), importedCrosses.getEntryCode());
		dataMap.put(getTermNameFromOntology(ColumnLabels.FEMALE_PARENT), importedCrosses.getFemaleDesig());
		dataMap.put(getTermNameFromOntology(ColumnLabels.FGID), importedCrosses.getFemaleGid());
		dataMap.put(getTermNameFromOntology(ColumnLabels.MALE_PARENT), importedCrosses.getMaleDesig());
		dataMap.put(getTermNameFromOntology(ColumnLabels.MGID), importedCrosses.getMaleGid());
		dataMap.put(getTermNameFromOntology(ColumnLabels.SEED_SOURCE), importedCrosses.getSource());

		return dataMap;
	}

	public Map<String, Object> generateDatatableDataMapWithDups(final GermplasmListData crossesData) {

		final Map<String, Object> dataMap = new HashMap<>();

		dataMap.put("ENTRY", crossesData.getEntryId());
		//TODO Fix getting parentage from Crossing Service
		dataMap.put("PARENTAGE", crossesData.getFemaleParent() + "/" + crossesData.getMaleParent());
		dataMap.put("ENTRY CODE", crossesData.getEntryCode());
		dataMap.put("FEMALE PARENT", crossesData.getFemaleParent());
		dataMap.put("FGID", crossesData.getFgid());
		dataMap.put("MALE PARENT", crossesData.getMaleParent());
		dataMap.put("MGID", crossesData.getMgid());
		dataMap.put("SOURCE", crossesData.getSeedSource());
		dataMap.put("DUPLICATE", "");
		return dataMap;

	}

	public Map<String, Object> generateDatatableDataMapWithDups(final ImportedCrosses importedCrosses) {

		final Map<String, Object> dataMap = new HashMap<>();

		dataMap.put("ENTRY", importedCrosses.getEntryId());
		dataMap.put("PARENTAGE", importedCrosses.getCross());
		dataMap.put("ENTRY CODE", importedCrosses.getEntryCode());
		dataMap.put("FEMALE PARENT", importedCrosses.getFemaleDesig());
		dataMap.put("FGID", importedCrosses.getFemaleGid());
		dataMap.put("MALE PARENT", importedCrosses.getMaleDesig());
		dataMap.put("MGID", importedCrosses.getMaleGid());
		dataMap.put("SOURCE", importedCrosses.getSource());
		dataMap.put("DUPLICATE", importedCrosses.getDuplicate());
		return dataMap;

	}

	public String getTermNameFromOntology(final ColumnLabels columnLabels) {
		return columnLabels.getTermNameFromOntology(this.ontologyDataManager);
	}

	public void setOntologyDataManager(final OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}

}
