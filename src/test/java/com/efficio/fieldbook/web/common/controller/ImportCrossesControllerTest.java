
package com.efficio.fieldbook.web.common.controller;

import java.util.Map;

import junit.framework.Assert;

import org.generationcp.commons.parsing.pojo.ImportedCrosses;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ImportCrossesControllerTest {

	@Mock
	private OntologyDataManager ontologyDataManager;
	@Mock
	private ImportedCrosses importedCrosses;

	private ImportCrossesController importCrossesController;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);

		this.importCrossesController = Mockito.spy(new ImportCrossesController());
		this.importCrossesController.setOntologyDataManager(this.ontologyDataManager);
	}

	@Test
	public void testGenerateDatatableDataMap_returnsTheValueFromOntology() throws MiddlewareQueryException {
		String dummyString = "DUMMY STRING";
		Mockito.when(this.importedCrosses.getEntryId()).thenReturn(Integer.MIN_VALUE);
		Mockito.when(this.importedCrosses.getCross()).thenReturn(dummyString);
		Mockito.when(this.importedCrosses.getEntryCode()).thenReturn(dummyString);
		Mockito.when(this.importedCrosses.getFemaleDesig()).thenReturn(dummyString);
		Mockito.when(this.importedCrosses.getFemaleGid()).thenReturn(dummyString);
		Mockito.when(this.importedCrosses.getMaleDesig()).thenReturn(dummyString);
		Mockito.when(this.importedCrosses.getMaleGid()).thenReturn(dummyString);
		Mockito.when(this.importedCrosses.getSource()).thenReturn(dummyString);

		Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_CODE.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.FEMALE_PARENT.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.FGID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.MALE_PARENT.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.MGID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);

		Map<String, Object> tableHeaderList = this.importCrossesController.generateDatatableDataMap(this.importedCrosses);

		for (Map.Entry<String, Object> tableHeader : tableHeaderList.entrySet()) {
			Assert.assertEquals("Expecting name from ontology but didn't.", fromOntology.getName(), tableHeader.getKey());
		}
	}

	@Test
	public void testGenerateDatatableDataMap_returnsTheValueFromColumLabelDefaultName() throws MiddlewareQueryException {
		String dummyString = "DUMMY STRING";
		Mockito.when(this.importedCrosses.getEntryId()).thenReturn(Integer.MIN_VALUE);
		Mockito.when(this.importedCrosses.getCross()).thenReturn(dummyString);
		Mockito.when(this.importedCrosses.getEntryCode()).thenReturn(dummyString);
		Mockito.when(this.importedCrosses.getFemaleDesig()).thenReturn(dummyString);
		Mockito.when(this.importedCrosses.getFemaleGid()).thenReturn(dummyString);
		Mockito.when(this.importedCrosses.getMaleDesig()).thenReturn(dummyString);
		Mockito.when(this.importedCrosses.getMaleGid()).thenReturn(dummyString);
		Mockito.when(this.importedCrosses.getSource()).thenReturn(dummyString);

		Term fromOntology = new Term();
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_CODE.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.FEMALE_PARENT.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.FGID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.MALE_PARENT.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.MGID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);

		Map<String, Object> tableHeaderList = this.importCrossesController.generateDatatableDataMap(this.importedCrosses);

		Assert.assertTrue("Expecting to have a column name ENTRY_ID.", this.hasColumnHeader(tableHeaderList, "ENTRY_ID"));
		Assert.assertTrue("Expecting to have a column name PARENTAGE.", this.hasColumnHeader(tableHeaderList, "PARENTAGE"));
		Assert.assertTrue("Expecting to have a column name ENTRY CODE.", this.hasColumnHeader(tableHeaderList, "ENTRY CODE"));
		Assert.assertTrue("Expecting to have a column name Female Parent.", this.hasColumnHeader(tableHeaderList, "Female Parent"));
		Assert.assertTrue("Expecting to have a column name FGID.", this.hasColumnHeader(tableHeaderList, "FGID"));
		Assert.assertTrue("Expecting to have a column name Male Parent.", this.hasColumnHeader(tableHeaderList, "Male Parent"));
		Assert.assertTrue("Expecting to have a column name MGID.", this.hasColumnHeader(tableHeaderList, "MGID"));
		Assert.assertTrue("Expecting to have a column name SEED SOURCE.", this.hasColumnHeader(tableHeaderList, "SEED SOURCE"));

	}

	private boolean hasColumnHeader(Map<String, Object> tableHeaderList, String columnName) {
		for (Map.Entry<String, Object> tableHeader : tableHeaderList.entrySet()) {
			if (tableHeader.getKey().equals(columnName)) {
				return true;
			}
		}
		return false;
	}
}
