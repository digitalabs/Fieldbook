
package com.efficio.fieldbook.web.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmParent;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.germplasm.CrossListData;
import org.generationcp.middleware.pojos.germplasm.GermplasmParent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

@Component
public class CrossesListUtil {

	public static final String MULTIPARENT_BEGIN_CHAR = "[";
	public static final String MULTIPARENT_END_CHAR = "[";
	public static final String TABLE_HEADER_LIST = "tableHeaderList";
	public static final String LIST_DATA_TABLE = "listDataTable";
	public static final String IS_IMPORT = "isImport";
	public static final String BREEDING_METHOD_PENDING = "Pending";
	public static final int ENTRY_INDEX = 0;
	public static final int PARENTAGE_INDEX = 1;
	public static final int DUPLICATE_INDEX = 2;
	public static final int FEMALE_PEDIGREE = 3;
	public static final int FEMALE_CROSS = 4;
	public static final int MALE_PEDIGREE = 5;
	public static final int MALE_CROSS = 6;
	public static final int BREEDING_METHOD_INDEX = 7;
	public static final int SOURCE_INDEX = 8;
	public static final int FGID_INDEX = 9;
	public static final int MGID_INDEX = 10;

	@Autowired
	private OntologyDataManager ontologyDataManager;
	
	@Resource
	private MessageSource messageSource;

	public static final String DEFAULT_SEPARATOR = "/";

	public Map<String, Object> generateCrossesTableDataMap(final ImportedCrosses importedCrosses) {

		final Map<String, Object> dataMap = new HashMap<>();

		dataMap.put(this.getTermNameFromOntology(ColumnLabels.ENTRY_ID), importedCrosses.getEntryId());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.PARENTAGE), importedCrosses.getCross());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.ENTRY_CODE), importedCrosses.getEntryCode());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.FEMALE_PARENT), importedCrosses.getFemaleDesignation());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.FGID), importedCrosses.getFemaleGid());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.MALE_PARENT), importedCrosses.getMaleDesignations());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.MGID), importedCrosses.getMaleGids());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.SEED_SOURCE), importedCrosses.getSource());

		return dataMap;
	}

	public Map<String, Object> generateCrossesTableWithDuplicationNotes(final List<String> tableHeaderList,
		final CrossListData crossesData) {
		final Map<String, Object> dataMap = new HashMap<>();
		dataMap.put(tableHeaderList.get(CrossesListUtil.ENTRY_INDEX), crossesData.getEntryId());
		final String concatenatedMaleDesignations = this.concatenateMaleParentsValue(this.getDesignationsList(crossesData.getMaleParents()));
		dataMap.put(tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX),
			crossesData.getFemaleParent().getDesignation() + CrossesListUtil.DEFAULT_SEPARATOR + concatenatedMaleDesignations);
		dataMap.put(tableHeaderList.get(CrossesListUtil.DUPLICATE_INDEX), "");
		dataMap.put(tableHeaderList.get(CrossesListUtil.FEMALE_PEDIGREE), crossesData.getFemaleParent().getPedigree());
		dataMap.put(tableHeaderList.get(CrossesListUtil.FEMALE_CROSS), crossesData.getFemaleParent().getDesignation());
		dataMap.put(tableHeaderList.get(CrossesListUtil.MALE_PEDIGREE), this.concatenateMaleParentsValue(this.getPedigreeList(crossesData.getMaleParents())));
		dataMap.put(tableHeaderList.get(CrossesListUtil.MALE_CROSS), concatenatedMaleDesignations);
		dataMap.put(tableHeaderList.get(CrossesListUtil.BREEDING_METHOD_INDEX), crossesData.getBreedingMethodName());
		dataMap.put(tableHeaderList.get(CrossesListUtil.SOURCE_INDEX), crossesData.getSeedSource());
		dataMap.put(tableHeaderList.get(CrossesListUtil.FGID_INDEX), crossesData.getFemaleParent().getGid());
		dataMap.put(tableHeaderList.get(CrossesListUtil.MGID_INDEX), this.getGids(crossesData.getMaleParents()));
		return dataMap;
	}
	
	public ImportedCrosses convertCrossListDataToImportedCrosses(final CrossListData crossesData, final String studyName) {
		final ImportedCrosses importedCrosses = new ImportedCrosses();
		importedCrosses.setCrossListId(crossesData.getId());
		importedCrosses.setEntryId(crossesData.getEntryId());
		importedCrosses.setGid(crossesData.getGid() != null ? Integer.toString(crossesData.getGid()) : null);
		// TODO check if entry code is needed. IF so, add to CrossListData pojo and uncomment/fix code below
		// importedCrosses.setEntryCode(crossesData.getEntryCode());
		importedCrosses.setSource(crossesData.getSeedSource());
		
		final GermplasmParent femaleParentFromDB = crossesData.getFemaleParent();
		final ImportedGermplasmParent femaleParent = new ImportedGermplasmParent(femaleParentFromDB.getGid(), femaleParentFromDB.getDesignation(), femaleParentFromDB.getPedigree());
		femaleParent.setCross(femaleParent.getDesignation());
		femaleParent.setStudyName(studyName);
		importedCrosses.setFemaleParent(femaleParent);
		
		final List<ImportedGermplasmParent> maleParents = new ArrayList<>();
		for (final GermplasmParent maleParentFromDB : crossesData.getMaleParents()) {
			final ImportedGermplasmParent maleParent = new ImportedGermplasmParent(maleParentFromDB.getGid(), maleParentFromDB.getDesignation(), maleParentFromDB.getPedigree());
			maleParent.setCross(maleParent.getDesignation());
			maleParent.setStudyName(studyName);
			maleParents.add(maleParent);
		}
		importedCrosses.setMaleParents(maleParents);
		importedCrosses.setCross(femaleParent + CrossesListUtil.DEFAULT_SEPARATOR + this.concatenateMaleParentsValue(this.getDesignationsList(crossesData.getMaleParents())));
		
		return importedCrosses;
	}

	public Map<String, Object> generateCrossesTableWithDuplicationNotes(final List<String> tableHeaderList, final ImportedCrosses importedCrosses) {

		final Map<String, Object> dataMap = new HashMap<>();

		dataMap.put(tableHeaderList.get(CrossesListUtil.ENTRY_INDEX), importedCrosses.getEntryId());
		dataMap.put(tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX), importedCrosses.getCross());
		dataMap.put(tableHeaderList.get(CrossesListUtil.DUPLICATE_INDEX), importedCrosses.getDuplicate());
		dataMap.put(tableHeaderList.get(CrossesListUtil.FEMALE_PEDIGREE), importedCrosses.getFemalePedigree());
		dataMap.put(tableHeaderList.get(CrossesListUtil.FEMALE_CROSS), importedCrosses.getFemaleCross());
		dataMap.put(tableHeaderList.get(CrossesListUtil.MALE_PEDIGREE), importedCrosses.getMalePedigree());
		dataMap.put(tableHeaderList.get(CrossesListUtil.MALE_CROSS), importedCrosses.getMaleCross());

		//shows BREEDING_METHOD as "Pending" if method is not defined in import crossing file
		String breedingMethodName = importedCrosses.getBreedingMethodName();
		if (StringUtils.isBlank(breedingMethodName)) {
			breedingMethodName = importedCrosses.getRawBreedingMethod();
		}
		//shows BREEDING_METHOD as "Pending" if method is not defined in import crossing file
		if (StringUtils.isBlank(breedingMethodName)) {
			breedingMethodName = BREEDING_METHOD_PENDING;
		}

		dataMap.put(tableHeaderList.get(CrossesListUtil.BREEDING_METHOD_INDEX), breedingMethodName);
		dataMap.put(tableHeaderList.get(CrossesListUtil.SOURCE_INDEX), importedCrosses.getSource());
		dataMap.put(tableHeaderList.get(CrossesListUtil.FGID_INDEX), importedCrosses.getFemaleGid());
		dataMap.put(tableHeaderList.get(CrossesListUtil.MGID_INDEX), importedCrosses.getMaleGids());

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
		tableHeaderList.add("#");
		tableHeaderList.add("CROSS");
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.DUPLICATE));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.CROSS_FEMALE_GID));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.FEMALE_PARENT));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.CROSS_MALE_GID));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.MALE_PARENT));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.BREEDING_METHOD_NAME));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.SEED_SOURCE));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.FGID));
		tableHeaderList.add(this.getTermNameFromOntology(ColumnLabels.MGID));
		return tableHeaderList;
	}
	
	private List<Integer> getGids(final List<GermplasmParent> parents) {
		return com.google.common.collect.Lists.newArrayList(Iterables.transform(parents, new Function<GermplasmParent, Integer>() {

			public Integer apply(GermplasmParent data) {
				return data.getGid();
			}
		}));
	}
	
	private List<String> getDesignationsList(final List<GermplasmParent> parents) {
		return com.google.common.collect.Lists.newArrayList(Iterables.transform(parents, new Function<GermplasmParent, String>() {

			public String apply(GermplasmParent data) {
				return data.getDesignation();
			}
		}));
	}
	
	private List<String> getPedigreeList(final List<GermplasmParent> parents) {
		return com.google.common.collect.Lists.newArrayList(Iterables.transform(parents, new Function<GermplasmParent, String>() {

			public String apply(GermplasmParent data) {
				return data.getPedigree();
			}
		}));
	}
	
	private String concatenateMaleParentsValue(final List<String> list) {
		if (list.size() == 1) {
			return list.get(0);
		}
		return MULTIPARENT_BEGIN_CHAR + StringUtils.join(list, ",") + MULTIPARENT_END_CHAR;
	}
	
}
