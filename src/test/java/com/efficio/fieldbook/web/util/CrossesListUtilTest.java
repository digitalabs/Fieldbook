
package com.efficio.fieldbook.web.util;

import java.util.*;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.parsing.pojo.ImportedCross;
import org.generationcp.commons.parsing.pojo.ImportedGermplasmParent;
import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.gms.SystemDefinedEntryType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.germplasm.GermplasmParent;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.generationcp.middleware.service.impl.study.StudyGermplasmServiceImplTest;
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

	private ImportedCross importedCross;

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
		this.importedCross = new ImportedCross(random.nextInt(), "", null, RandomStringUtils.random(20), RandomStringUtils.random(20), RandomStringUtils.random(20), "");
		this.importedCross.setFemaleParent(new ImportedGermplasmParent(random.nextInt(), RandomStringUtils.random(20), RandomStringUtils.random(20)));
		// Setup 3 male parents
		this.importedCross
			.setMaleParents(Arrays.asList(new ImportedGermplasmParent(random.nextInt(), RandomStringUtils.random(20), RandomStringUtils.random(20)), new ImportedGermplasmParent(random.nextInt(), RandomStringUtils.random(20), RandomStringUtils.random(20)), new ImportedGermplasmParent(random.nextInt(), RandomStringUtils.random(20), RandomStringUtils.random(20))));

	}

	@Test
	public void testGenerateDatatableDataMap_returnsTheValueFromOntology() {

		final Map<String, Object> dataMap = this.crossesListUtil.generateCrossesTableDataMap(this.importedCross);
		// Verify that map keys are names of terms from Ontology
		Assert.assertEquals(this.importedCross.getEntryNumber(), dataMap.get(this.headersMap.get(TermId.ENTRY_NO.getId())));
		Assert.assertEquals(this.importedCross.getCross(), dataMap.get(this.headersMap.get(TermId.CROSS.getId())));
		Assert.assertEquals(this.importedCross.getEntryCode(), dataMap.get(this.headersMap.get(TermId.ENTRY_CODE.getId())));
		Assert.assertEquals(this.importedCross.getFemaleDesignation(), dataMap.get(this.headersMap.get(TermId.FEMALE_PARENT.getId())));
		Assert.assertEquals(this.importedCross.getFemaleGid(), dataMap.get(this.headersMap.get(TermId.FGID.getId())));
		Assert.assertEquals(this.importedCross.getMaleDesignations(), dataMap.get(this.headersMap.get(TermId.MALE_PARENT.getId())));
		Assert.assertEquals(this.importedCross.getMaleGids(), dataMap.get(this.headersMap.get(TermId.MGID.getId())));
		Assert.assertEquals(this.importedCross.getSource(), dataMap.get(this.headersMap.get(TermId.SEED_SOURCE.getId())));
	}

	@Test
	public void testGenerateDatatableDataMap_returnsTheValueFromColumLabelDefaultName() {
		Mockito.doReturn(new Term()).when(this.ontologyDataManager).getTermById(ArgumentMatchers.anyInt());
		final Map<String, Object> tableHeaderList = this.crossesListUtil.generateCrossesTableDataMap(this.importedCross);

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
			this.crossesListUtil.generateCrossesTableWithDuplicationNotes(tableHeaderList, this.importedCross);

		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.ENTRY_INDEX) + ".",
			dataMap.containsKey(tableHeaderList.get(CrossesListUtil.ENTRY_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX) + ".",
			dataMap.containsKey(tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.FGID_INDEX) + ".",
			dataMap.containsKey(ColumnLabels.FGID.name()));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.MGID_INDEX) + ".",
			dataMap.containsKey(ColumnLabels.MGID.name()));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.SOURCE_INDEX) + ".",
			dataMap.containsKey(tableHeaderList.get(CrossesListUtil.SOURCE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.DUPLICATE_INDEX) + ".",
			dataMap.containsKey(tableHeaderList.get(CrossesListUtil.DUPLICATE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.FEMALE_CROSS) + ".",
			dataMap.containsKey(ColumnLabels.FEMALE_PARENT.name()));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.MALE_CROSS) + ".",
			dataMap.containsKey(ColumnLabels.MALE_PARENT.name()));
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
		final Map<String, Object> dataMap = this.crossesListUtil.generateCrossesTableWithDuplicationNotes(tableHeaderList, this.importedCross);

		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.ENTRY_INDEX) + ".",
				dataMap.containsKey(tableHeaderList.get(CrossesListUtil.ENTRY_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX) + ".",
				dataMap.containsKey(tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.FGID_INDEX) + ".",
				dataMap.containsKey(ColumnLabels.FGID.name()));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.MGID_INDEX) + ".",
				dataMap.containsKey(ColumnLabels.MGID.name()));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.SOURCE_INDEX) + ".",
				dataMap.containsKey(tableHeaderList.get(CrossesListUtil.SOURCE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.DUPLICATE_INDEX) + ".",
				dataMap.containsKey(tableHeaderList.get(CrossesListUtil.DUPLICATE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.FEMALE_PEDIGREE) + ".",
			dataMap.containsKey(tableHeaderList.get(CrossesListUtil.FEMALE_PEDIGREE)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.MALE_PEDIGREE) + ".",
			dataMap.containsKey(tableHeaderList.get(CrossesListUtil.MALE_PEDIGREE)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.FEMALE_CROSS) + ".",
			dataMap.containsKey(ColumnLabels.FEMALE_PARENT.name()));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.MALE_CROSS) + ".",
			dataMap.containsKey(ColumnLabels.MALE_PARENT.name()));
	}

	@Test
	public void testConvertGermplasmListDataToImportedCrosses() {
		final GermplasmListData germplasmListData = new GermplasmListData();
		germplasmListData.setEntryId(CrossesListUtilTest.TEST_ENTRY_ID_VALUE);
		germplasmListData.setEntryCode(CrossesListUtilTest.TEST_ENTRY_CODE_VALUE);
		germplasmListData.setFemaleParent(new GermplasmParent(CrossesListUtilTest.TEST_FGID_VALUE, CrossesListUtilTest.TEST_FEMALE_PARENT_VALUE, CrossesListUtilTest.UNKNOWN_PEDIGREE));
		germplasmListData.addMaleParent(new GermplasmParent(CrossesListUtilTest.TEST_MGID1_VALUE, CrossesListUtilTest.TEST_MALE_PARENT1_VALUE, CrossesListUtilTest.UNKNOWN_PEDIGREE));
		germplasmListData.setSeedSource(CrossesListUtilTest.TEST_SEED_SOURCE_VALUE);

		final ImportedCross
			testImportedCross = this.crossesListUtil.convertGermplasmListDataToImportedCrosses(germplasmListData, RandomStringUtils.random(20), Collections.emptyList());
		Assert.assertEquals(Integer.valueOf(CrossesListUtilTest.TEST_ENTRY_ID_VALUE), testImportedCross.getEntryNumber());
		Assert.assertEquals(CrossesListUtilTest.TEST_ENTRY_CODE_VALUE, testImportedCross.getEntryCode());
		Assert.assertEquals(CrossesListUtilTest.TEST_FEMALE_PARENT_VALUE, testImportedCross.getFemaleDesignation());
		Assert.assertEquals(String.valueOf(CrossesListUtilTest.TEST_FGID_VALUE), testImportedCross.getFemaleGid());
		Assert.assertEquals(CrossesListUtilTest.TEST_MALE_PARENT1_VALUE, testImportedCross.getMaleDesignationsAsString());
		Assert.assertEquals(Arrays.asList(CrossesListUtilTest.TEST_MGID1_VALUE), testImportedCross.getMaleGids());
		Assert.assertEquals(CrossesListUtilTest.TEST_SEED_SOURCE_VALUE, testImportedCross.getSource());
		Assert.assertEquals(CrossesListUtilTest.TEST_FEMALE_PARENT_VALUE + "/" + CrossesListUtilTest.TEST_MALE_PARENT1_VALUE,
				testImportedCross.getCross());
		Assert.assertEquals(CrossesListUtilTest.UNKNOWN_PEDIGREE, testImportedCross.getFemalePedigree());
		Assert.assertEquals(Arrays.asList(CrossesListUtilTest.UNKNOWN_PEDIGREE), testImportedCross.getMalePedigree());

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
		final ImportedCross testImportedCross = this.crossesListUtil.convertGermplasmListDataToImportedCrosses(germplasmListData, studyName, Collections.emptyList());
		Assert.assertEquals(Integer.valueOf(CrossesListUtilTest.TEST_ENTRY_ID_VALUE), testImportedCross.getEntryNumber());
		Assert.assertEquals(CrossesListUtilTest.TEST_ENTRY_CODE_VALUE, testImportedCross.getEntryCode());
		Assert.assertEquals(CrossesListUtilTest.TEST_FEMALE_PARENT_VALUE, testImportedCross.getFemaleDesignation());
		Assert.assertEquals(String.valueOf(CrossesListUtilTest.TEST_FGID_VALUE), testImportedCross.getFemaleGid());
		final String concatenatedMaleDesignations = CrossesListUtil.MULTIPARENT_BEGIN_CHAR + CrossesListUtilTest.TEST_MALE_PARENT1_VALUE + ","
				+ CrossesListUtilTest.TEST_MALE_PARENT2_VALUE + CrossesListUtil.MULTIPARENT_END_CHAR;
		Assert.assertEquals(
				concatenatedMaleDesignations,
				testImportedCross.getMaleDesignationsAsString());
		Assert.assertEquals(Arrays.asList(CrossesListUtilTest.TEST_MGID1_VALUE, CrossesListUtilTest.TEST_MGID2_VALUE), testImportedCross.getMaleGids());
		Assert.assertEquals(CrossesListUtilTest.TEST_SEED_SOURCE_VALUE, testImportedCross.getSource());
		Assert.assertEquals(CrossesListUtilTest.TEST_FEMALE_PARENT_VALUE + "/" + concatenatedMaleDesignations,
				testImportedCross.getCross());
		Assert.assertEquals(CrossesListUtilTest.UNKNOWN_PEDIGREE, testImportedCross.getFemalePedigree());
		Assert.assertEquals(Arrays.asList(malePedigree1, malePedigree2), testImportedCross.getMalePedigree());
		Assert.assertEquals(studyName, testImportedCross.getFemaleParent().getStudyName());
		for (final ImportedGermplasmParent parent : testImportedCross.getMaleParents()) {
			Assert.assertEquals(studyName, parent.getStudyName());
		}
	}

	@Test
	public void testGetParentPlotNumber() {
		final List<StudyGermplasmDto> studyList = this.createStudyGermplasmList(10);
		Assert.assertEquals(10, this.crossesListUtil.getParentPlotNo(101, studyList).intValue());
		Assert.assertEquals(3, this.crossesListUtil.getParentPlotNo(108, studyList).intValue());
		Assert.assertNull(this.crossesListUtil.getParentPlotNo(0, studyList));
		Assert.assertNull(this.crossesListUtil.getParentPlotNo(99, studyList));
	}

	private List<StudyGermplasmDto> createStudyGermplasmList(final Integer numberOfEntries) {
		final List<StudyGermplasmDto> studyList = new ArrayList<>();
		for (int i = 0; i < numberOfEntries; i++) {
			final StudyGermplasmDto germplasmDto = new StudyGermplasmDto();
			germplasmDto.setGermplasmId(100 + i + 1);
			germplasmDto.setEntryNumber(i + 1);
			germplasmDto.setPosition(String.valueOf(numberOfEntries - i));
			studyList.add(germplasmDto);
		}
		return studyList;

	}

}
