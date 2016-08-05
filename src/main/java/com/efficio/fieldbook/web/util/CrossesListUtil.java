
package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.constant.ColumnLabels;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CrossesListUtil {

	public static final String TABLE_HEADER_LIST = "tableHeaderList";
	public static final String LIST_DATA_TABLE = "listDataTable";
	public static final String IS_IMPORT = "isImport";
	public static final String BREEDING_METHOD_PENDING = "Pending";
	public static final int ENTRY_INDEX = 0;
	public static final int PARENTAGE_INDEX = 1;
	public static final int DUPLICATE_INDEX = 2;
	public static final int BREEDING_METHOD_INDEX = 3;
	public static final int FGID_INDEX = 4;
	public static final int FEMALE_PLOT_INDEX = 5;
	public static final int MGID_INDEX = 6;
	public static final int MALE_NURSERY_INDEX = 7;
	public static final int MALE_PLOT_INDEX = 8;
	public static final int CROSSING_DATE_INDEX = 9;
	public static final int NOTES_INDEX = 10;
	public static final int SOURCE_INDEX = 11;



	@Autowired
	private OntologyDataManager ontologyDataManager;

	public static final String DEFAULT_SEPARATOR = "/";

	public Map<String, Object> generateDatatableDataMap(final ImportedCrosses importedCrosses) {

		final Map<String, Object> dataMap = new HashMap<>();

		dataMap.put(this.getTermNameFromOntology(ColumnLabels.ENTRY_ID), importedCrosses.getEntryId());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.PARENTAGE), importedCrosses.getCross());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.ENTRY_CODE), importedCrosses.getEntryCode());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.FEMALE_PARENT), importedCrosses.getFemaleDesig());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.FGID), importedCrosses.getFemaleGid());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.MALE_PARENT), importedCrosses.getMaleDesig());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.MGID), importedCrosses.getMaleGid());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.SEED_SOURCE), importedCrosses.getSource());

		return dataMap;
	}

	public Map<String, Object> generateDatatableDataMapWithDups(final List<String> tableHeaderList, final GermplasmListData crossesData) {
		final Map<String, Object> dataMap = new HashMap<>();
		dataMap.put(tableHeaderList.get(CrossesListUtil.ENTRY_INDEX), crossesData.getEntryId());
		dataMap.put(tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX), crossesData.getFemaleParent() + CrossesListUtil.DEFAULT_SEPARATOR
				+ crossesData.getMaleParent());
		dataMap.put(tableHeaderList.get(CrossesListUtil.DUPLICATE_INDEX), "");
		dataMap.put(tableHeaderList.get(CrossesListUtil.FGID_INDEX), crossesData.getFgid());
		dataMap.put(tableHeaderList.get(CrossesListUtil.MGID_INDEX), crossesData.getMgid());
		dataMap.put(tableHeaderList.get(CrossesListUtil.SOURCE_INDEX), crossesData.getSeedSource());
		dataMap.put(tableHeaderList.get(CrossesListUtil.BREEDING_METHOD_INDEX), crossesData.getBreedingMethodName());
		return dataMap;
	}

	public ImportedCrosses convertGermplasmListData2ImportedCrosses(final GermplasmListData crossesData) {
		final ImportedCrosses importedCrosses = new ImportedCrosses();
		importedCrosses.setCrossListId(crossesData.getId());
		importedCrosses.setEntryId(crossesData.getEntryId());
		importedCrosses.setGid(crossesData.getGid() != null ? Integer.toString(crossesData.getGid()) : null);
		importedCrosses.setCross(crossesData.getFemaleParent() + CrossesListUtil.DEFAULT_SEPARATOR + crossesData.getMaleParent());
		importedCrosses.setEntryCode(crossesData.getEntryCode());
		importedCrosses.setFemaleDesig(crossesData.getFemaleParent());
		importedCrosses.setFemaleGid(String.valueOf(crossesData.getFgid()));
		importedCrosses.setMaleDesig(crossesData.getMaleParent());
		importedCrosses.setMaleGid(String.valueOf(crossesData.getMgid()));
		importedCrosses.setSource(crossesData.getSeedSource());
		return importedCrosses;
	}

	public Map<String, Object> generateDatatableDataMapWithDups(final List<String> tableHeaderList, final ImportedCrosses importedCrosses) {

		final Map<String, Object> dataMap = new HashMap<>();

		dataMap.put(tableHeaderList.get(CrossesListUtil.ENTRY_INDEX), importedCrosses.getEntryId());
		dataMap.put(tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX), importedCrosses.getCross());
		dataMap.put(tableHeaderList.get(CrossesListUtil.DUPLICATE_INDEX), importedCrosses.getDuplicate());
		//shows BREEDING_METHOD as "Pending" if method is not defined in import crossing file
		String breedingMethodName = importedCrosses.getRawBreedingMethod();
		if(StringUtils.isEmpty(breedingMethodName)){
			breedingMethodName = BREEDING_METHOD_PENDING;
		}
		dataMap.put(tableHeaderList.get(CrossesListUtil.BREEDING_METHOD_INDEX),breedingMethodName);
		dataMap.put(tableHeaderList.get(CrossesListUtil.FGID_INDEX), importedCrosses.getFemaleGid());
		dataMap.put(tableHeaderList.get(CrossesListUtil.FEMALE_PLOT_INDEX), importedCrosses.getFemalePlotNo());
		dataMap.put(tableHeaderList.get(CrossesListUtil.MGID_INDEX), importedCrosses.getMaleGid());
		dataMap.put(tableHeaderList.get(CrossesListUtil.MALE_NURSERY_INDEX), importedCrosses.getMaleStudyName());
		dataMap.put(tableHeaderList.get(CrossesListUtil.MALE_PLOT_INDEX), importedCrosses.getMalePlotNo());
		dataMap.put(tableHeaderList.get(CrossesListUtil.CROSSING_DATE_INDEX), importedCrosses.getCrossingDate());
		dataMap.put(tableHeaderList.get(CrossesListUtil.NOTES_INDEX), importedCrosses.getNotes());
		dataMap.put(tableHeaderList.get(CrossesListUtil.SOURCE_INDEX), importedCrosses.getSource());
		return dataMap;

	}

	public String getTermNameFromOntology(final ColumnLabels columnLabels) {
		return columnLabels.getTermNameFromOntology(this.ontologyDataManager);
	}

	public void setOntologyDataManager(final OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}

	public List<String> getTableHeaders() {

		final List<String> tableHeaderList = new ArrayList<>();

		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.ENTRY_ID));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.PARENTAGE));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.DUPLICATE));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.BREEDING_METHOD_NAME));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.FGID));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.FEMALE_PLOT));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.MGID));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.MALE_NURSERY_NAME));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.MALE_PLOT));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.CROSSING_DATE));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.NOTES));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.SEED_SOURCE));

		return tableHeaderList;
	}

}
