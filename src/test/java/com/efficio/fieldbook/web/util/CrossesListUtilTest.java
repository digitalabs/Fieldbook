
package com.efficio.fieldbook.web.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmParent;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.germplasm.GermplasmParent;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;


public class CrossesListUtilTest {

	public static final int TEST_ENTRY_ID_VALUE = 123;
	public static final String TEST_ENTRY_CODE_VALUE = "Test Entry code";
	public static final String TEST_FEMALE_PARENT_VALUE = "Test female parent";
	public static final Integer TEST_FGID_VALUE = 893;
	public static final String TEST_MALE_PARENT1_VALUE = "Test male parent " + RandomStringUtils.randomNumeric(3);
	public static final Integer TEST_MGID1_VALUE = 493;
	public static final String TEST_MALE_PARENT2_VALUE = "Test male parent " + RandomStringUtils.randomNumeric(3);;
	public static final Integer TEST_MGID2_VALUE = 495;
	public static final String TEST_SEED_SOURCE_VALUE = "Test seed source";
	public static final String UNKNOWN_PEDIGREE = "-";

	@Mock
	private OntologyDataManager ontologyDataManager;

	private ImportedCrosses importedCrosses;

	private CrossesListUtil crossesListUtil;
	
	private Map<Integer, String> headersMap = new HashMap<>();
	private List<TermId> terms = Arrays.asList(TermId.ENTRY_NO, TermId.CROSS, TermId.ENTRY_CODE, TermId.FEMALE_PARENT, TermId.FGID,
			TermId.MALE_PARENT, TermId.MGID, TermId.SEED_SOURCE);

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		this.crossesListUtil = new CrossesListUtil();
		this.crossesListUtil.setOntologyDataManager(this.ontologyDataManager);
		
		for (final TermId term : this.terms) {
			final String fromOntology = RandomStringUtils.random(10);
			Mockito.when(this.ontologyDataManager.getTermById(term.getId())).thenReturn(new Term(term.getId(), fromOntology, ""));
			headersMap.put(term.getId(), fromOntology);
		}
		
		final Random random = new Random();
		this.importedCrosses = new ImportedCrosses(random.nextInt(), "", null, RandomStringUtils.random(20), RandomStringUtils.random(20), RandomStringUtils.random(20), "");
		this.importedCrosses.setFemaleParent(new ImportedGermplasmParent(random.nextInt(), RandomStringUtils.random(20), RandomStringUtils.random(20)));
		// Setup 3 male parents
		this.importedCrosses.setMaleParents(Arrays.asList(new ImportedGermplasmParent(random.nextInt(), RandomStringUtils.random(20), RandomStringUtils.random(20)), new ImportedGermplasmParent(random.nextInt(), RandomStringUtils.random(20), RandomStringUtils.random(20)), new ImportedGermplasmParent(random.nextInt(), RandomStringUtils.random(20), RandomStringUtils.random(20))));

	}

	@Test
	public void testGenerateDatatableDataMap_returnsTheValueFromOntology() {

		final Map<String, Object> dataMap = this.crossesListUtil.generateCrossesTableDataMap(this.importedCrosses);
		// Verify that map keys are names of terms from Ontology
		Assert.assertEquals(this.importedCrosses.getEntryId(), dataMap.get(this.headersMap.get(TermId.ENTRY_NO.getId())));
		Assert.assertEquals(this.importedCrosses.getCross(), dataMap.get(this.headersMap.get(TermId.CROSS.getId())));
		Assert.assertEquals(this.importedCrosses.getEntryCode(), dataMap.get(this.headersMap.get(TermId.ENTRY_CODE.getId())));
		Assert.assertEquals(this.importedCrosses.getFemaleDesignation(), dataMap.get(this.headersMap.get(TermId.FEMALE_PARENT.getId())));
		Assert.assertEquals(this.importedCrosses.getFemaleGid(), dataMap.get(this.headersMap.get(TermId.FGID.getId())));
		Assert.assertEquals(this.importedCrosses.getMaleDesignations(), dataMap.get(this.headersMap.get(TermId.MALE_PARENT.getId())));
		Assert.assertEquals(this.importedCrosses.getMaleGids(), dataMap.get(this.headersMap.get(TermId.MGID.getId())));
		Assert.assertEquals(this.importedCrosses.getSource(), dataMap.get(this.headersMap.get(TermId.SEED_SOURCE.getId())));
	}

	@Test
	public void testGenerateDatatableDataMap_returnsTheValueFromColumLabelDefaultName() {
		Mockito.doReturn(new Term()).when(this.ontologyDataManager).getTermById(ArgumentMatchers.anyInt());
		final Map<String, Object> tableHeaderList = this.crossesListUtil.generateCrossesTableDataMap(this.importedCrosses);

		Assert.assertTrue("Expecting to have a column name ENTRY_ID.", tableHeaderList.containsKey("ENTRY_ID"));
		Assert.assertTrue("Expecting to have a column name PARENTAGE.", tableHeaderList.containsKey("PARENTAGE"));
		Assert.assertTrue("Expecting to have a column name ENTRY CODE.", tableHeaderList.containsKey("ENTRY CODE"));
		Assert.assertTrue("Expecting to have a column name Female Parent.", tableHeaderList.containsKey("Female Parent"));
		Assert.assertTrue("Expecting to have a column name FGID.", tableHeaderList.containsKey("FGID"));
		Assert.assertTrue("Expecting to have a column name Male Parent.", tableHeaderList.containsKey("Male Parent"));
		Assert.assertTrue("Expecting to have a column name MGID.", tableHeaderList.containsKey("MGID"));
		Assert.assertTrue("Expecting to have a column name SEED SOURCE.", tableHeaderList.containsKey("SEED SOURCE"));

	}

	@Test
	public void testGenerateDatatableDataMapWithDups_importedCrosses() {

		final List<String> tableHeaderList = this.crossesListUtil.getTableHeaders();
		final Map<String, Object> dataMap =
			this.crossesListUtil.generateCrossesTableWithDuplicationNotes(tableHeaderList, this.importedCrosses);

		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.ENTRY_INDEX) + ".",
			dataMap.containsKey(tableHeaderList.get(CrossesListUtil.ENTRY_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX) + ".",
			dataMap.containsKey(tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.FGID_INDEX) + ".",
			dataMap.containsKey(tableHeaderList.get(CrossesListUtil.FGID_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.MGID_INDEX) + ".",
			dataMap.containsKey(tableHeaderList.get(CrossesListUtil.MGID_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.SOURCE_INDEX) + ".",
			dataMap.containsKey(tableHeaderList.get(CrossesListUtil.SOURCE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.DUPLICATE_INDEX) + ".",
			dataMap.containsKey(tableHeaderList.get(CrossesListUtil.DUPLICATE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.FEMALE_CROSS) + ".",
			dataMap.containsKey(tableHeaderList.get(CrossesListUtil.FEMALE_CROSS)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.MALE_CROSS) + ".",
			dataMap.containsKey(tableHeaderList.get(CrossesListUtil.MALE_CROSS)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.BREEDING_METHOD_INDEX) + ".",
			dataMap.containsKey(tableHeaderList.get(CrossesListUtil.BREEDING_METHOD_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.MALE_PEDIGREE) + ".",
			dataMap.containsKey(tableHeaderList.get(CrossesListUtil.MALE_PEDIGREE)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.FEMALE_PEDIGREE) + ".",
			dataMap.containsKey(tableHeaderList.get(CrossesListUtil.FEMALE_PEDIGREE)));

	}

	@Test
	public void testGenerateDatatableDataMapWithDupsGermplasmListData() {
		final List<String> tableHeaderList = this.crossesListUtil.getTableHeaders();
		final Map<String, Object> dataMap = this.crossesListUtil.generateCrossesTableWithDuplicationNotes(tableHeaderList, this.importedCrosses);

		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.ENTRY_INDEX) + ".",
				dataMap.containsKey(tableHeaderList.get(CrossesListUtil.ENTRY_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX) + ".",
				dataMap.containsKey(tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.FGID_INDEX) + ".",
				dataMap.containsKey(tableHeaderList.get(CrossesListUtil.FGID_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.MGID_INDEX) + ".",
				dataMap.containsKey(tableHeaderList.get(CrossesListUtil.MGID_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.SOURCE_INDEX) + ".",
				dataMap.containsKey(tableHeaderList.get(CrossesListUtil.SOURCE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.DUPLICATE_INDEX) + ".",
				dataMap.containsKey(tableHeaderList.get(CrossesListUtil.DUPLICATE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.FEMALE_PEDIGREE) + ".",
			dataMap.containsKey(tableHeaderList.get(CrossesListUtil.FEMALE_PEDIGREE)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.MALE_PEDIGREE) + ".",
			dataMap.containsKey(tableHeaderList.get(CrossesListUtil.MALE_PEDIGREE)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.FEMALE_CROSS) + ".",
			dataMap.containsKey(tableHeaderList.get(CrossesListUtil.FEMALE_CROSS)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.MALE_CROSS) + ".",
			dataMap.containsKey(tableHeaderList.get(CrossesListUtil.MALE_CROSS)));
	}

	@Test
	public void testConvertGermplasmListDataToImportedCrosses() {
		final GermplasmListData germplasmListData = new GermplasmListData();
		germplasmListData.setEntryId(CrossesListUtilTest.TEST_ENTRY_ID_VALUE);
		germplasmListData.setEntryCode(CrossesListUtilTest.TEST_ENTRY_CODE_VALUE);
		germplasmListData.setFemaleParent(new GermplasmParent(CrossesListUtilTest.TEST_FGID_VALUE, CrossesListUtilTest.TEST_FEMALE_PARENT_VALUE, CrossesListUtilTest.UNKNOWN_PEDIGREE));
		germplasmListData.addMaleParent(new GermplasmParent(CrossesListUtilTest.TEST_MGID1_VALUE, CrossesListUtilTest.TEST_MALE_PARENT1_VALUE, CrossesListUtilTest.UNKNOWN_PEDIGREE));
		germplasmListData.setSeedSource(CrossesListUtilTest.TEST_SEED_SOURCE_VALUE);

		final ImportedCrosses testImportedCrosses = this.crossesListUtil.convertGermplasmListDataToImportedCrosses(germplasmListData, RandomStringUtils.random(20));
		Assert.assertEquals(Integer.valueOf(CrossesListUtilTest.TEST_ENTRY_ID_VALUE), testImportedCrosses.getEntryId());
		Assert.assertEquals(CrossesListUtilTest.TEST_ENTRY_CODE_VALUE, testImportedCrosses.getEntryCode());
		Assert.assertEquals(CrossesListUtilTest.TEST_FEMALE_PARENT_VALUE, testImportedCrosses.getFemaleDesignation());
		Assert.assertEquals(String.valueOf(CrossesListUtilTest.TEST_FGID_VALUE), testImportedCrosses.getFemaleGid());
		Assert.assertEquals(CrossesListUtilTest.TEST_MALE_PARENT1_VALUE, testImportedCrosses.getMaleDesignationsAsString());
		Assert.assertEquals(Arrays.asList(CrossesListUtilTest.TEST_MGID1_VALUE), testImportedCrosses.getMaleGids());
		Assert.assertEquals(CrossesListUtilTest.TEST_SEED_SOURCE_VALUE, testImportedCrosses.getSource());
		Assert.assertEquals(CrossesListUtilTest.TEST_FEMALE_PARENT_VALUE + "/" + CrossesListUtilTest.TEST_MALE_PARENT1_VALUE,
				testImportedCrosses.getCross());
		Assert.assertEquals(CrossesListUtilTest.UNKNOWN_PEDIGREE, testImportedCrosses.getFemalePedigree());
		Assert.assertEquals(Arrays.asList(CrossesListUtilTest.UNKNOWN_PEDIGREE), testImportedCrosses.getMalePedigree());

	}
	
	@Test
	public void testConvertGermplasmListDataToImportedCrossesWhenMultipleMaleParents() {
		final GermplasmListData germplasmListData = new GermplasmListData();
		germplasmListData.setEntryId(CrossesListUtilTest.TEST_ENTRY_ID_VALUE);
		germplasmListData.setEntryCode(CrossesListUtilTest.TEST_ENTRY_CODE_VALUE);
		germplasmListData.setFemaleParent(new GermplasmParent(CrossesListUtilTest.TEST_FGID_VALUE, CrossesListUtilTest.TEST_FEMALE_PARENT_VALUE, CrossesListUtilTest.UNKNOWN_PEDIGREE));
		final String malePedigree1 = RandomStringUtils.random(25);
		germplasmListData.addMaleParent(new GermplasmParent(CrossesListUtilTest.TEST_MGID1_VALUE, CrossesListUtilTest.TEST_MALE_PARENT1_VALUE, malePedigree1));
		final String malePedigree2 = RandomStringUtils.random(25);
		germplasmListData.addMaleParent(new GermplasmParent(CrossesListUtilTest.TEST_MGID2_VALUE, CrossesListUtilTest.TEST_MALE_PARENT2_VALUE, malePedigree2));
		germplasmListData.setSeedSource(CrossesListUtilTest.TEST_SEED_SOURCE_VALUE);

		final String studyName = RandomStringUtils.random(20);
		final ImportedCrosses testImportedCrosses = this.crossesListUtil.convertGermplasmListDataToImportedCrosses(germplasmListData, studyName);
		Assert.assertEquals(Integer.valueOf(CrossesListUtilTest.TEST_ENTRY_ID_VALUE), testImportedCrosses.getEntryId());
		Assert.assertEquals(CrossesListUtilTest.TEST_ENTRY_CODE_VALUE, testImportedCrosses.getEntryCode());
		Assert.assertEquals(CrossesListUtilTest.TEST_FEMALE_PARENT_VALUE, testImportedCrosses.getFemaleDesignation());
		Assert.assertEquals(String.valueOf(CrossesListUtilTest.TEST_FGID_VALUE), testImportedCrosses.getFemaleGid());
		final String concatenatedMaleDesignations = CrossesListUtil.MULTIPARENT_BEGIN_CHAR + CrossesListUtilTest.TEST_MALE_PARENT1_VALUE + ","
				+ CrossesListUtilTest.TEST_MALE_PARENT2_VALUE + CrossesListUtil.MULTIPARENT_END_CHAR;
		Assert.assertEquals(
				concatenatedMaleDesignations,
				testImportedCrosses.getMaleDesignationsAsString());
		Assert.assertEquals(Arrays.asList(CrossesListUtilTest.TEST_MGID1_VALUE, CrossesListUtilTest.TEST_MGID2_VALUE), testImportedCrosses.getMaleGids());
		Assert.assertEquals(CrossesListUtilTest.TEST_SEED_SOURCE_VALUE, testImportedCrosses.getSource());
		Assert.assertEquals(CrossesListUtilTest.TEST_FEMALE_PARENT_VALUE + "/" + concatenatedMaleDesignations,
				testImportedCrosses.getCross());
		Assert.assertEquals(CrossesListUtilTest.UNKNOWN_PEDIGREE, testImportedCrosses.getFemalePedigree());
		Assert.assertEquals(Arrays.asList(malePedigree1, malePedigree2), testImportedCrosses.getMalePedigree());
		Assert.assertEquals(studyName, testImportedCrosses.getFemaleParent().getStudyName());
		for (final ImportedGermplasmParent parent : testImportedCrosses.getMaleParents()) {
			Assert.assertEquals(studyName, parent.getStudyName());
		}
	}

}
