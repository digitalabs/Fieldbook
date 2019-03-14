
package com.efficio.fieldbook.web.util;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.germplasm.GermplasmParent;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import junit.framework.Assert;

public class CrossesListUtilTest {

	public static final int TEST_ENTRY_ID_VALUE = 123;
	public static final String TEST_ENTRY_CODE_VALUE = "Test Entry code";
	public static final String TEST_FEMALE_PARENT_VALUE = "Test female parent";
	public static final int TEST_FGID_VALUE = 893;
	public static final String TEST_MALE_PARENT_VALUE = "Test male parent";
	public static final int TEST_MGID_VALUE = 493;
	public static final String TEST_SEED_SOURCE_VALUE = "Test seed source";
	public static final String UNKNOWN_PEDIGREE = "-";
	final String dummyString = "DUMMY STRING";

	@Mock
	private OntologyDataManager ontologyDataManager;
	@Mock
	private ImportedCrosses importedCrosses;

	private CrossesListUtil crossesListUtil;

	private Term fromOntology = new Term();

	@Mock
	private GermplasmListData crossesData;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		this.crossesListUtil = new CrossesListUtil();
		this.crossesListUtil.setOntologyDataManager(this.ontologyDataManager);

		Mockito.when(this.importedCrosses.getEntryId()).thenReturn(Integer.MIN_VALUE);
		Mockito.when(this.importedCrosses.getCross()).thenReturn(this.dummyString);
		Mockito.when(this.importedCrosses.getEntryCode()).thenReturn(this.dummyString);
		Mockito.when(this.importedCrosses.getFemaleDesignation()).thenReturn(this.dummyString);
		Mockito.when(this.importedCrosses.getFemaleGid()).thenReturn(this.dummyString);
		Mockito.when(this.importedCrosses.getMaleDesignationsAsString()).thenReturn(this.dummyString);
		//FIXME - getMaleGids returns a list of integer so code below should be fixed
//		Mockito.when(this.importedCrosses.getMaleGid()).thenReturn(this.dummyString);
		Mockito.when(this.importedCrosses.getSource()).thenReturn(this.dummyString);

		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(this.fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(this.fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_CODE.getId())).thenReturn(this.fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.FEMALE_PARENT.getId())).thenReturn(this.fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.FGID.getId())).thenReturn(this.fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.MALE_PARENT.getId())).thenReturn(this.fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.MGID.getId())).thenReturn(this.fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(this.fromOntology);

	}

	@Test
	public void testGenerateDatatableDataMap_returnsTheValueFromOntology() throws MiddlewareQueryException {

		this.fromOntology.setName("Ontology Name");
		final Map<String, Object> tableHeaderList = this.crossesListUtil.generateCrossesTableDataMap(this.importedCrosses);

		for (final Map.Entry<String, Object> tableHeader : tableHeaderList.entrySet()) {
			Assert.assertEquals("Expecting name from ontology but didn't.", this.fromOntology.getName(), tableHeader.getKey());
		}
	}

	@Test
	public void testGenerateDatatableDataMap_returnsTheValueFromColumLabelDefaultName() throws MiddlewareQueryException {

		this.fromOntology = new Term();
		final Map<String, Object> tableHeaderList = this.crossesListUtil.generateCrossesTableDataMap(this.importedCrosses);

		Assert.assertTrue("Expecting to have a column name ENTRY_ID.", this.hasColumnHeader(tableHeaderList, "ENTRY_ID"));
		Assert.assertTrue("Expecting to have a column name PARENTAGE.", this.hasColumnHeader(tableHeaderList, "PARENTAGE"));
		Assert.assertTrue("Expecting to have a column name ENTRY CODE.", this.hasColumnHeader(tableHeaderList, "ENTRY CODE"));
		Assert.assertTrue("Expecting to have a column name Female Parent.", this.hasColumnHeader(tableHeaderList, "Female Parent"));
		Assert.assertTrue("Expecting to have a column name FGID.", this.hasColumnHeader(tableHeaderList, "FGID"));
		Assert.assertTrue("Expecting to have a column name Male Parent.", this.hasColumnHeader(tableHeaderList, "Male Parent"));
		Assert.assertTrue("Expecting to have a column name MGID.", this.hasColumnHeader(tableHeaderList, "MGID"));
		Assert.assertTrue("Expecting to have a column name SEED SOURCE.", this.hasColumnHeader(tableHeaderList, "SEED SOURCE"));

	}

	@Test
	public void testGenerateDatatableDataMapWithDups_importedCrosses() {

		final List<String> tableHeaderList = this.crossesListUtil.getTableHeaders();
		final Map<String, Object> dataMap =
			this.crossesListUtil.generateCrossesTableWithDuplicationNotes(tableHeaderList, this.importedCrosses);

		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.ENTRY_INDEX) + ".",
			this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.ENTRY_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX) + ".",
			this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.FGID_INDEX) + ".",
			this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.FGID_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.MGID_INDEX) + ".",
			this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.MGID_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.SOURCE_INDEX) + ".",
			this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.SOURCE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.DUPLICATE_INDEX) + ".",
			this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.DUPLICATE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.FEMALE_CROSS) + ".",
			this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.FEMALE_CROSS)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.MALE_CROSS) + ".",
			this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.MALE_CROSS)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.BREEDING_METHOD_INDEX) + ".",
			this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.BREEDING_METHOD_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.MALE_PEDIGREE) + ".",
			this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.MALE_PEDIGREE)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.FEMALE_PEDIGREE) + ".",
			this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.FEMALE_PEDIGREE)));

	}

	@Test
	public void testGenerateDatatableDataMapWithDupsGermplasmListData() {

		final List<String> tableHeaderList = this.crossesListUtil.getTableHeaders();
		final Map<String, Object> dataMap = this.crossesListUtil.generateCrossesTableWithDuplicationNotes(tableHeaderList, this.crossesData);

		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.ENTRY_INDEX) + ".",
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.ENTRY_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX) + ".",
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.FGID_INDEX) + ".",
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.FGID_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.MGID_INDEX) + ".",
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.MGID_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.SOURCE_INDEX) + ".",
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.SOURCE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.DUPLICATE_INDEX) + ".",
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.DUPLICATE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.FEMALE_PEDIGREE) + ".",
			this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.FEMALE_PEDIGREE)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.MALE_PEDIGREE) + ".",
			this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.MALE_PEDIGREE)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.FEMALE_CROSS) + ".",
			this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.FEMALE_CROSS)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.MALE_CROSS) + ".",
			this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.MALE_CROSS)));
	}

	@Test
	public void testConvertGermplasmListData2ImportedCrosses() {
		final GermplasmListData germplasmListData = new GermplasmListData();
		germplasmListData.setEntryId(CrossesListUtilTest.TEST_ENTRY_ID_VALUE);
		germplasmListData.setEntryCode(CrossesListUtilTest.TEST_ENTRY_CODE_VALUE);
		germplasmListData.setFemaleParent(new GermplasmParent(CrossesListUtilTest.TEST_FGID_VALUE, CrossesListUtilTest.TEST_FEMALE_PARENT_VALUE, CrossesListUtilTest.UNKNOWN_PEDIGREE));
		germplasmListData.addMaleParent(new GermplasmParent(CrossesListUtilTest.TEST_MGID_VALUE, CrossesListUtilTest.TEST_MALE_PARENT_VALUE, CrossesListUtilTest.UNKNOWN_PEDIGREE));
		germplasmListData.setSeedSource(CrossesListUtilTest.TEST_SEED_SOURCE_VALUE);

		final ImportedCrosses testImportedCrosses = this.crossesListUtil.convertGermplasmListDataToImportedCrosses(germplasmListData, RandomStringUtils.random(20));
		Assert.assertEquals(Integer.valueOf(CrossesListUtilTest.TEST_ENTRY_ID_VALUE), testImportedCrosses.getEntryId());
		Assert.assertEquals(CrossesListUtilTest.TEST_ENTRY_CODE_VALUE, testImportedCrosses.getEntryCode());
		Assert.assertEquals(CrossesListUtilTest.TEST_FEMALE_PARENT_VALUE, testImportedCrosses.getFemaleDesignation());
		Assert.assertEquals(String.valueOf(CrossesListUtilTest.TEST_FGID_VALUE), testImportedCrosses.getFemaleGid());
		Assert.assertEquals(CrossesListUtilTest.TEST_MALE_PARENT_VALUE, testImportedCrosses.getMaleDesignationsAsString());
		Assert.assertEquals(String.valueOf(CrossesListUtilTest.TEST_MGID_VALUE), testImportedCrosses.getMaleGids());
		Assert.assertEquals(CrossesListUtilTest.TEST_SEED_SOURCE_VALUE, testImportedCrosses.getSource());
		Assert.assertEquals(CrossesListUtilTest.TEST_FEMALE_PARENT_VALUE + "/" + CrossesListUtilTest.TEST_MALE_PARENT_VALUE,
				testImportedCrosses.getCross());
		Assert.assertEquals(CrossesListUtilTest.UNKNOWN_PEDIGREE, testImportedCrosses.getFemalePedigree());
		Assert.assertEquals(CrossesListUtilTest.UNKNOWN_PEDIGREE, testImportedCrosses.getMalePedigree());

	}

	private boolean hasColumnHeader(final Map<String, Object> tableHeaderList, final String columnName) {
		for (final Map.Entry<String, Object> tableHeader : tableHeaderList.entrySet()) {
			if (tableHeader.getKey().equals(columnName)) {
				return true;
			}
		}
		return false;
	}
}
