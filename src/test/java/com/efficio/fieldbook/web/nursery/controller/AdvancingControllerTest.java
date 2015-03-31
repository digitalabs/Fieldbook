package com.efficio.fieldbook.web.nursery.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.efficio.fieldbook.web.common.bean.*;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.bean.ImportedGermplasm;
import com.efficio.fieldbook.web.nursery.form.AdvancingNurseryForm;

import junit.framework.Assert;

import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

@RunWith(MockitoJUnitRunner.class)
public class AdvancingControllerTest {
    
	@Mock
    private OntologyDataManager ontologyDataManager;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private com.efficio.fieldbook.service.api.FieldbookService fieldbookService;

	@Mock
	private PaginationListSelection paginationListSelection;

	@Mock
	private UserSelection userSelection;

	@Mock
	private MessageSource messageSource;

	@Spy
	private AdvancingNursery advancingNursery = new AdvancingNursery();

	@InjectMocks
	private AdvancingController advancingController = spy(new AdvancingController());

	@Test
	public void testGetAdvancedNurseryTableHeader_returnsTheValueFromColumLabelDefaultName() throws MiddlewareQueryException{
		Term fromOntology = new Term();
		when(ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);
		
		List<TableHeader> tableHeaderList = advancingController.getAdvancedNurseryTableHeader();
		assertEquals("Expecting to return 5 columns but returned " + tableHeaderList.size(), 5,
				tableHeaderList.size());
		
		Assert.assertTrue("Expecting to have a column name ENTRY_ID.", hasColumnHeader(tableHeaderList,"ENTRY_ID"));
		Assert.assertTrue("Expecting to have a column name DESIGNATION.", hasColumnHeader(tableHeaderList,"DESIGNATION"));
		Assert.assertTrue("Expecting to have a column name PARENTAGE.",
				hasColumnHeader(tableHeaderList, "PARENTAGE"));
		Assert.assertTrue("Expecting to have a column name GID.", hasColumnHeader(tableHeaderList,"GID"));
		Assert.assertTrue("Expecting to have a column name SEED SOURCE.",
				hasColumnHeader(tableHeaderList, "SEED SOURCE"));
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
		assertEquals("Expecting to return 5 columns but returned " + tableHeaderList.size(),
				5, tableHeaderList.size());
		
		for(TableHeader tableHeader : tableHeaderList){		
			assertEquals("Expecting name from ontology but didn't.", fromOntology.getName(),
					tableHeader.getColumnName());
		}
	}

	@Test
	public void testPostAdvanceNursery() throws MiddlewareQueryException, RuleException {
		// setup

		AdvancingNurseryForm form = new AdvancingNurseryForm();
		ArrayList<ImportedGermplasm> importedGermplasm = new ArrayList<>();
		importedGermplasm.add(mock(ImportedGermplasm.class));

		Method method = new Method();
		method.setMtype("DER");

		preparePostAdvanceNursery(form,method, importedGermplasm);

		// scenario 1, has a method choice and breeding method not a Generative
		Map<String,Object> output = advancingController.postAdvanceNursery(form,null,null);

		assertEquals("should be successful", "1", output.get("isSuccess"));
		assertEquals("should have at least 1 imported germplasm list", importedGermplasm.size(),
				output.get("listSize"));
		assertNotNull("should have advance germplasm change details",
				output.get("advanceGermplasmChangeDetails"));
		assertNotNull("should have generated unique id", output.get("uniqueId"));

	}

	@Test
	public void testPostAdvanceNurseryThrowsRuleException() throws MiddlewareQueryException,
			RuleException {
		// setup
		AdvancingNurseryForm form = new AdvancingNurseryForm();
		ArrayList<ImportedGermplasm> importedGermplasm = new ArrayList<>();
		Method method = new Method();
		method.setMtype("DER");

		preparePostAdvanceNursery(form,method, importedGermplasm);

		when(fieldbookService.advanceNursery(eq(advancingNursery), any(Workbook.class))).thenThrow(
				mock(RuleException.class));

		doNothing().when(paginationListSelection).addAdvanceDetails(anyString(), eq(form));

		// scenario 2, has a method throwing exception
		Map<String,Object> output = advancingController.postAdvanceNursery(form, null, null);

		assertEquals("should fail", "0", output.get("isSuccess"));
		assertEquals("should have at least 0 imported germplasm list", Integer.valueOf(0),
				output.get("listSize"));
	}

	@Test
	public void testPostAdvanceNurseryGenerativeMethodError() throws MiddlewareQueryException,
			RuleException {
		// setup
		AdvancingNurseryForm form = new AdvancingNurseryForm();
		ArrayList<ImportedGermplasm> importedGermplasm = new ArrayList<>();

		Method method = new Method();
		method.setMtype("GEN");

		preparePostAdvanceNursery(form,method, importedGermplasm);

		when(fieldbookService.advanceNursery(eq(advancingNursery), any(Workbook.class))).thenThrow(
				mock(RuleException.class));

		// scenario 2, has a method throwing exception
		Map<String,Object> output = advancingController.postAdvanceNursery(form, null, null);

		assertEquals("should fail", "0", output.get("isSuccess"));
		assertEquals("should have at least 0 imported germplasm list", 0,
				output.get("listSize"));
		assertEquals("should have error message","error.message",output.get("message"));
	}

	private void preparePostAdvanceNursery(AdvancingNurseryForm form,Method method,ArrayList<ImportedGermplasm> importedGermplasm) throws MiddlewareQueryException, RuleException {
		// setup
		form.setMethodChoice("1");
		form.setAdvanceBreedingMethodId("10");

		AdvanceResult result = mock(AdvanceResult.class);

		when(result.getAdvanceList()).thenReturn(importedGermplasm);
		when(result.getChangeDetails()).thenReturn(new ArrayList<AdvanceGermplasmChangeDetail>());
		when(fieldbookMiddlewareService.getMethodById(anyInt())).thenReturn(method);
		when(fieldbookService.advanceNursery(eq(advancingNursery), any(Workbook.class))).thenReturn(
				result);
		when(messageSource.getMessage(
				eq("nursery.save.advance.error.row.list.empty.generative.method"),
				any(String[].class), eq(
				LocaleContextHolder.getLocale())))
				.thenReturn("error.message");

		doNothing().when(paginationListSelection).addAdvanceDetails(anyString(), eq(form));

	}
}
