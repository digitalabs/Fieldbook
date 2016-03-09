package com.efficio.fieldbook.web.util;

import java.util.HashMap;
import java.util.Map;

import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CrossesListUtil {

	public static final String ENTRY = "ENTRY";
	public static final String PARENTAGE = "PARENTAGE";
	public static final String ENTRY_CODE = "ENTRY CODE";
	public static final String FEMALE_PARENT = "FEMALE PARENT";
	public static final String FGID = "FGID";
	public static final String MALE_PARENT = "MALE PARENT";
	public static final String MGID = "MGID";
	public static final String SOURCE = "SOURCE";
	public static final String DUPLICATE = "DUPLICATE";

	@Autowired
	private OntologyDataManager ontologyDataManager;

	public static final String DEFAULT_SEPARATOR = "/";

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
		dataMap.put(ENTRY, crossesData.getEntryId());
		dataMap.put(PARENTAGE, crossesData.getFemaleParent() + DEFAULT_SEPARATOR + crossesData.getMaleParent());
		dataMap.put(ENTRY_CODE, crossesData.getEntryCode());
		dataMap.put(FEMALE_PARENT, crossesData.getFemaleParent());
		dataMap.put(FGID, crossesData.getFgid());
		dataMap.put(MALE_PARENT, crossesData.getMaleParent());
		dataMap.put(MGID, crossesData.getMgid());
		dataMap.put(SOURCE, crossesData.getSeedSource());
		dataMap.put(DUPLICATE, "");
		return dataMap;
	}

	public ImportedCrosses convertGermplasmListData2ImportedCrosses(final GermplasmListData crossesData) {
		final ImportedCrosses importedCrosses = new ImportedCrosses();
		importedCrosses.setCrossListId(crossesData.getId());
		importedCrosses.setEntryId(crossesData.getEntryId());
		importedCrosses.setGid(crossesData.getGid() != null ? Integer.toString(crossesData.getGid()) : null);
		importedCrosses.setCross(crossesData.getFemaleParent() + DEFAULT_SEPARATOR + crossesData.getMaleParent());
		importedCrosses.setEntryCode(crossesData.getEntryCode());
		importedCrosses.setFemaleDesig(crossesData.getFemaleParent());
		importedCrosses.setFemaleGid(String.valueOf(crossesData.getFgid()));
		importedCrosses.setMaleDesig(crossesData.getMaleParent());
		importedCrosses.setMaleGid(String.valueOf(crossesData.getMgid()));
		importedCrosses.setSource(crossesData.getSeedSource());
		return importedCrosses;
	}

	public Map<String, Object> generateDatatableDataMapWithDups(final ImportedCrosses importedCrosses) {

		final Map<String, Object> dataMap = new HashMap<>();

		dataMap.put(ENTRY, importedCrosses.getEntryId());
		dataMap.put(PARENTAGE, importedCrosses.getCross());
		dataMap.put(ENTRY_CODE, importedCrosses.getEntryCode());
		dataMap.put(FEMALE_PARENT, importedCrosses.getFemaleDesig());
		dataMap.put(FGID, importedCrosses.getFemaleGid());
		dataMap.put(MALE_PARENT, importedCrosses.getMaleDesig());
		dataMap.put(MGID, importedCrosses.getMaleGid());
		dataMap.put(SOURCE, importedCrosses.getSource());
		dataMap.put(DUPLICATE, importedCrosses.getDuplicate());
		return dataMap;

	}

	public String getTermNameFromOntology(final ColumnLabels columnLabels) {
		return columnLabels.getTermNameFromOntology(this.ontologyDataManager);
	}

	public void setOntologyDataManager(final OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}

}
