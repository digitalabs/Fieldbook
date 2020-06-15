
package com.efficio.fieldbook.web.util;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.commons.parsing.pojo.ImportedCross;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmParent;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.germplasm.GermplasmParent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CrossesListUtil {

	public static final String MULTIPARENT_BEGIN_CHAR = "[";
	public static final String MULTIPARENT_END_CHAR = "]";
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

	public Map<String, Object> generateCrossesTableDataMap(final ImportedCross importedCross) {

		final Map<String, Object> dataMap = new HashMap<>();

		dataMap.put(this.getTermNameFromOntology(ColumnLabels.ENTRY_ID), importedCross.getEntryNumber());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.PARENTAGE), importedCross.getCross());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.ENTRY_CODE), importedCross.getEntryCode());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.FEMALE_PARENT), importedCross.getFemaleDesignation());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.FGID), importedCross.getFemaleGid());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.MALE_PARENT), importedCross.getMaleDesignations());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.MGID), importedCross.getMaleGids());
		dataMap.put(this.getTermNameFromOntology(ColumnLabels.SEED_SOURCE), importedCross.getSource());

		return dataMap;
	}

	public Map<String, Object> generateCrossesTableWithDuplicationNotes(final List<String> tableHeaderList,
		final GermplasmListData crossesData) {
		final Map<String, Object> dataMap = new HashMap<>();
		dataMap.put(tableHeaderList.get(CrossesListUtil.ENTRY_INDEX), crossesData.getEntryId());
		final List<String> maleDesignations = this.getDesignationsList(crossesData.getMaleParents());
		final String concatenatedMaleDesignations = this.concatenateMaleParentsValue(maleDesignations);
		dataMap.put(tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX),
			crossesData.getFemaleParentDesignation() + CrossesListUtil.DEFAULT_SEPARATOR + concatenatedMaleDesignations);
		dataMap.put(ColumnLabels.DUPLICATE.name(), "");
		dataMap.put(tableHeaderList.get(CrossesListUtil.FEMALE_PEDIGREE), crossesData.getFemaleParent().getPedigree());
		dataMap.put(ColumnLabels.FEMALE_PARENT.name(), crossesData.getFemaleParentDesignation());
		dataMap.put(tableHeaderList.get(CrossesListUtil.MALE_PEDIGREE), this.concatenateMaleParentsValue(this.getPedigreeList(crossesData.getMaleParents()))); // MALE PEDIGREE
		dataMap.put(ColumnLabels.MALE_PARENT.name(), maleDesignations);
		dataMap.put(tableHeaderList.get(CrossesListUtil.BREEDING_METHOD_INDEX), crossesData.getBreedingMethodName());
		dataMap.put(tableHeaderList.get(CrossesListUtil.SOURCE_INDEX), crossesData.getSeedSource());
		dataMap.put(ColumnLabels.FGID.name(), crossesData.getFemaleGid());
		dataMap.put(ColumnLabels.MGID.name(), this.getGids(crossesData.getMaleParents()));
		return dataMap;
	}
	
	public ImportedCross convertGermplasmListDataToImportedCrosses(final GermplasmListData crossesData, final String studyName) {
		final ImportedCross importedCross = new ImportedCross();
		importedCross.setCrossListId(crossesData.getId());
		importedCross.setEntryNumber(crossesData.getEntryId());
		importedCross.setGid(crossesData.getGid() != null ? Integer.toString(crossesData.getGid()) : null);
		importedCross.setEntryCode(crossesData.getEntryCode());
		importedCross.setSource(crossesData.getSeedSource());
		
		final GermplasmParent femaleParentFromDB = crossesData.getFemaleParent();
		final ImportedGermplasmParent femaleParent = new ImportedGermplasmParent(femaleParentFromDB.getGid(), femaleParentFromDB.getDesignation(), femaleParentFromDB.getPedigree());
		femaleParent.setCross(femaleParent.getDesignation());
		femaleParent.setStudyName(studyName);
		importedCross.setFemaleParent(femaleParent);
		
		final List<ImportedGermplasmParent> maleParents = new ArrayList<>();
		for (final GermplasmParent maleParentFromDB : crossesData.getMaleParents()) {
			final ImportedGermplasmParent maleParent = new ImportedGermplasmParent(maleParentFromDB.getGid(), maleParentFromDB.getDesignation(), maleParentFromDB.getPedigree());
			maleParent.setCross(maleParent.getDesignation());
			maleParent.setStudyName(studyName);
			maleParents.add(maleParent);
		}
		importedCross.setMaleParents(maleParents);
		importedCross.setCross(femaleParent.getDesignation() + CrossesListUtil.DEFAULT_SEPARATOR + this.concatenateMaleParentsValue(this.getDesignationsList(crossesData.getMaleParents())));
		
		return importedCross;
	}

	public Map<String, Object> generateCrossesTableWithDuplicationNotes(final List<String> tableHeaderList, final ImportedCross importedCross) {

		final Map<String, Object> dataMap = new HashMap<>();

		dataMap.put(tableHeaderList.get(CrossesListUtil.ENTRY_INDEX), importedCross.getEntryNumber());
		dataMap.put(tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX), importedCross.getCross());
		dataMap.put(ColumnLabels.DUPLICATE.name(), importedCross.getDuplicate());
		dataMap.put(tableHeaderList.get(CrossesListUtil.FEMALE_PEDIGREE), importedCross.getFemalePedigree());
		dataMap.put(ColumnLabels.FEMALE_PARENT.name(), importedCross.getFemaleCross());
		dataMap.put(tableHeaderList.get(CrossesListUtil.MALE_PEDIGREE), this.concatenateMaleParentsValue(importedCross.getMalePedigree()));
		dataMap.put(ColumnLabels.MALE_PARENT.name(), importedCross.getMaleCross());

		//shows BREEDING_METHOD as "Pending" if method is not defined in import crossing file
		String breedingMethodName = importedCross.getBreedingMethodName();
		if (StringUtils.isBlank(breedingMethodName)) {
			breedingMethodName = importedCross.getRawBreedingMethod();
		}
		//shows BREEDING_METHOD as "Pending" if method is not defined in import crossing file
		if (StringUtils.isBlank(breedingMethodName)) {
			breedingMethodName = BREEDING_METHOD_PENDING;
		}

		dataMap.put(tableHeaderList.get(CrossesListUtil.BREEDING_METHOD_INDEX), breedingMethodName);
		dataMap.put(tableHeaderList.get(CrossesListUtil.SOURCE_INDEX), importedCross.getSource());
		dataMap.put(ColumnLabels.FGID.name(), importedCross.getFemaleGid());
		dataMap.put(ColumnLabels.MGID.name(), importedCross.getMaleGids());

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
