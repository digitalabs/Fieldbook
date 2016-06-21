
package com.efficio.fieldbook.web.util;

import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class CrossesListUtilTest {

	public static final int TEST_ENTRY_ID_VALUE = 123;
	public static final String TEST_ENTRY_CODE_VALUE = "Test Entry code";
	public static final String TEST_FEMALE_PARENT_VALUE = "Test female parent";
	public static final int TEST_FGID_VALUE = 893;
	public static final String TEST_MALE_PARENT_VALUE = "Test male parent";
	public static final int TEST_MGID_VALUE = 493;
	public static final String TEST_SEED_SOURCE_VALUE = "Test seed source";
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
		Mockito.when(this.importedCrosses.getFemaleDesig()).thenReturn(this.dummyString);
		Mockito.when(this.importedCrosses.getFemaleGid()).thenReturn(this.dummyString);
		Mockito.when(this.importedCrosses.getMaleDesig()).thenReturn(this.dummyString);
		Mockito.when(this.importedCrosses.getMaleGid()).thenReturn(this.dummyString);
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
		final Map<String, Object> tableHeaderList = this.crossesListUtil.generateDatatableDataMap(this.importedCrosses);

		for (final Map.Entry<String, Object> tableHeader : tableHeaderList.entrySet()) {
			Assert.assertEquals("Expecting name from ontology but didn't.", this.fromOntology.getName(), tableHeader.getKey());
		}
	}

	@Test
	public void testGenerateDatatableDataMap_returnsTheValueFromColumLabelDefaultName() throws MiddlewareQueryException {

		this.fromOntology = new Term();
		final Map<String, Object> tableHeaderList = this.crossesListUtil.generateDatatableDataMap(this.importedCrosses);

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
		final Map<String, Object> dataMap = this.crossesListUtil.generateDatatableDataMapWithDups(
				tableHeaderList, this.importedCrosses);

		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.ENTRY_INDEX) + ".", 
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.ENTRY_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX) + ".", 
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.ENTRY_CODE_INDEX) + ".", 
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.ENTRY_CODE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.FEMALE_PARENT_INDEX) + ".", 
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.FEMALE_PARENT_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.FGID_INDEX) + ".", 
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.FGID_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.MALE_PARENT_INDEX) + ".", 
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.MALE_PARENT_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.MGID_INDEX) + ".", 
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.MGID_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.SOURCE_INDEX) + ".", 
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.SOURCE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.DUPLICATE_INDEX) + ".", 
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.DUPLICATE_INDEX)));
	}

	@Test
	public void testGenerateDatatableDataMapWithDups_germplasmListData() {
		
		final List<String> tableHeaderList = this.crossesListUtil.getTableHeaders();
		final Map<String, Object> dataMap = this.crossesListUtil.generateDatatableDataMapWithDups(tableHeaderList, this.crossesData);

		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.ENTRY_INDEX) + ".", 
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.ENTRY_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX) + ".", 
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.PARENTAGE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.ENTRY_CODE_INDEX) + ".", 
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.ENTRY_CODE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.FEMALE_PARENT_INDEX) + ".", 
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.FEMALE_PARENT_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.FGID_INDEX) + ".", 
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.FGID_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.MALE_PARENT_INDEX) + ".", 
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.MALE_PARENT_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.MGID_INDEX) + ".", 
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.MGID_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.SOURCE_INDEX) + ".", 
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.SOURCE_INDEX)));
		Assert.assertTrue("Expecting to have a column name " + tableHeaderList.get(CrossesListUtil.DUPLICATE_INDEX) + ".", 
				this.hasColumnHeader(dataMap, tableHeaderList.get(CrossesListUtil.DUPLICATE_INDEX)));
	}

	@Test
	public void testConvertGermplasmListData2ImportedCrosses() {
		final GermplasmListData germplasmListData = new GermplasmListData();
		germplasmListData.setEntryId(TEST_ENTRY_ID_VALUE);
		germplasmListData.setEntryCode(TEST_ENTRY_CODE_VALUE);
		germplasmListData.setFemaleParent(TEST_FEMALE_PARENT_VALUE);
		germplasmListData.setFgid(TEST_FGID_VALUE);
		germplasmListData.setMaleParent(TEST_MALE_PARENT_VALUE);
		germplasmListData.setMgid(TEST_MGID_VALUE);
		germplasmListData.setSeedSource(TEST_SEED_SOURCE_VALUE);

		final ImportedCrosses testImportedCrosses = this.crossesListUtil.convertGermplasmListData2ImportedCrosses(germplasmListData);

		Assert.assertEquals(Integer.valueOf(TEST_ENTRY_ID_VALUE), testImportedCrosses.getEntryId());
		Assert.assertEquals(TEST_ENTRY_CODE_VALUE, testImportedCrosses.getEntryCode());
		Assert.assertEquals(TEST_FEMALE_PARENT_VALUE, testImportedCrosses.getFemaleDesig());
		Assert.assertEquals(String.valueOf(TEST_FGID_VALUE), testImportedCrosses.getFemaleGid());
		Assert.assertEquals(TEST_MALE_PARENT_VALUE, testImportedCrosses.getMaleDesig());
		Assert.assertEquals(String.valueOf(TEST_MGID_VALUE), testImportedCrosses.getMaleGid());
		Assert.assertEquals(TEST_SEED_SOURCE_VALUE, testImportedCrosses.getSource());
		Assert.assertEquals(TEST_FEMALE_PARENT_VALUE + "/" + TEST_MALE_PARENT_VALUE, testImportedCrosses.getCross());
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
