package com.efficio.fieldbook.web.common.controller;

import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.efficio.fieldbook.web.common.bean.TableHeader;
import com.efficio.fieldbook.web.nursery.bean.ImportedCrosses;

import static org.mockito.Mockito.*;

public class ImportCrossesControllerTest {
	
	@Mock
	private OntologyDataManager ontologyDataManager;
	@Mock
	private ImportedCrosses importedCrosses;
	
	private ImportCrossesController importCrossesController;
	
	
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this);
		
		importCrossesController = spy(new ImportCrossesController());
		importCrossesController.setOntologyDataManager(ontologyDataManager);
	} 
	
	@Test
	public void testGenerateDatatableDataMap_returnsTheValueFromOntology() throws MiddlewareQueryException{
		String dummyString = "DUMMY STRING";
		when(importedCrosses.getEntryId()).thenReturn(Integer.MIN_VALUE);
		when(importedCrosses.getCross()).thenReturn(dummyString);
		when(importedCrosses.getEntryCode()).thenReturn(dummyString);
		when(importedCrosses.getFemaleDesig()).thenReturn(dummyString);
		when(importedCrosses.getFemaleGid()).thenReturn(dummyString);
		when(importedCrosses.getMaleDesig()).thenReturn(dummyString);
		when(importedCrosses.getMaleGid()).thenReturn(dummyString);
		when(importedCrosses.getSource()).thenReturn(dummyString);
		
		Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		when(ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.ENTRY_CODE.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.FEMALE_PARENT.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.FGID.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.MALE_PARENT.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.MGID.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);
		
		Map<String, Object> tableHeaderList = importCrossesController.generateDatatableDataMap(importedCrosses);
		
		for(Map.Entry<String, Object> tableHeader : tableHeaderList.entrySet()){		
			Assert.assertEquals("Expecting name from ontology but didn't.", fromOntology.getName(),tableHeader.getKey());
		}
	}
	
	@Test
	public void testGenerateDatatableDataMap_returnsTheValueFromColumLabelDefaultName() throws MiddlewareQueryException{
		String dummyString = "DUMMY STRING";
		when(importedCrosses.getEntryId()).thenReturn(Integer.MIN_VALUE);
		when(importedCrosses.getCross()).thenReturn(dummyString);
		when(importedCrosses.getEntryCode()).thenReturn(dummyString);
		when(importedCrosses.getFemaleDesig()).thenReturn(dummyString);
		when(importedCrosses.getFemaleGid()).thenReturn(dummyString);
		when(importedCrosses.getMaleDesig()).thenReturn(dummyString);
		when(importedCrosses.getMaleGid()).thenReturn(dummyString);
		when(importedCrosses.getSource()).thenReturn(dummyString);
		
		Term fromOntology = new Term();
		when(ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.ENTRY_CODE.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.FEMALE_PARENT.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.FGID.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.MALE_PARENT.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.MGID.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);
		
		Map<String, Object> tableHeaderList = importCrossesController.generateDatatableDataMap(importedCrosses);
		
		Assert.assertTrue("Expecting to have a column name ENTRY_ID.", hasColumnHeader(tableHeaderList,"ENTRY_ID"));
		Assert.assertTrue("Expecting to have a column name PARENTAGE.", hasColumnHeader(tableHeaderList,"PARENTAGE"));
		Assert.assertTrue("Expecting to have a column name ENTRY CODE.", hasColumnHeader(tableHeaderList,"ENTRY CODE"));
		Assert.assertTrue("Expecting to have a column name Female Parent.", hasColumnHeader(tableHeaderList,"Female Parent"));
		Assert.assertTrue("Expecting to have a column name FGID.", hasColumnHeader(tableHeaderList,"FGID"));
		Assert.assertTrue("Expecting to have a column name Male Parent.", hasColumnHeader(tableHeaderList,"Male Parent"));
		Assert.assertTrue("Expecting to have a column name MGID.", hasColumnHeader(tableHeaderList,"MGID"));
		Assert.assertTrue("Expecting to have a column name SEED SOURCE.", hasColumnHeader(tableHeaderList,"SEED SOURCE"));
		
	}
	
	private boolean hasColumnHeader(Map<String, Object> tableHeaderList,
			String columnName) {
		for(Map.Entry<String, Object> tableHeader: tableHeaderList.entrySet()){
			if(tableHeader.getKey().equals(columnName)){
				return true;
			}
		}
		return false;
	}
}
