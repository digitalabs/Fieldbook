package com.efficio.fieldbook.web.nursery.controller;

import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.List;

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

public class AdvancingControllerTest {
    
	@Mock
    private OntologyDataManager ontologyDataManager;
    
	private AdvancingController advancingController;
	
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this);
		advancingController = spy(new AdvancingController());
		advancingController.setOntologyDataManager(ontologyDataManager);
	}
	
	@Test
	public void testGetAdvancedNurseryTableHeader_returnsTheValueFromColumLabelDefaultName() throws MiddlewareQueryException{
		Term fromOntology = new Term();
		when(ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);
		
		List<TableHeader> tableHeaderList = advancingController.getAdvancedNurseryTableHeader();
		Assert.assertEquals("Expecting to return 5 columns but returned " + tableHeaderList.size(), 5, tableHeaderList.size());
		
		Assert.assertTrue("Expecting to have a column name ENTRY_ID.", hasColumnHeader(tableHeaderList,"ENTRY_ID"));
		Assert.assertTrue("Expecting to have a column name DESIGNATION.", hasColumnHeader(tableHeaderList,"DESIGNATION"));
		Assert.assertTrue("Expecting to have a column name PARENTAGE.", hasColumnHeader(tableHeaderList,"PARENTAGE"));
		Assert.assertTrue("Expecting to have a column name GID.", hasColumnHeader(tableHeaderList,"GID"));
		Assert.assertTrue("Expecting to have a column name SEED SOURCE.", hasColumnHeader(tableHeaderList,"SEED SOURCE"));
	}
	
	private boolean hasColumnHeader(List<TableHeader> tableHeaderList,
			String columnName) {
		for(TableHeader tableHeader: tableHeaderList){
			if(tableHeader.getColumnName().equals(columnName)){
				return true;
			}
		}
		return false;
	}
	
	@Test
	public void testGetAdvancedNurseryTableHeader_returnsTheValueFromOntology() throws MiddlewareQueryException{
		Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		when(ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);
		
		List<TableHeader> tableHeaderList = advancingController.getAdvancedNurseryTableHeader();
		Assert.assertEquals("Expecting to return 5 columns but returned " + tableHeaderList.size(), 5, tableHeaderList.size());
		
		for(TableHeader tableHeader : tableHeaderList){		
			Assert.assertEquals("Expecting name from ontology but didn't.", fromOntology.getName(),tableHeader.getColumnName());
		}
	}
}
