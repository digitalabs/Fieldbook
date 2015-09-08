
package com.efficio.fieldbook.web.nursery.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.generationcp.commons.parsing.pojo.ImportedGermplasm;
import org.generationcp.commons.ruleengine.RuleException;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

import com.efficio.fieldbook.web.common.bean.AdvanceGermplasmChangeDetail;
import com.efficio.fieldbook.web.common.bean.AdvanceResult;
import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.bean.TableHeader;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.nursery.bean.AdvancingNursery;
import com.efficio.fieldbook.web.nursery.form.AdvancingNurseryForm;

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
	private final AdvancingNursery advancingNursery = new AdvancingNursery();

	@InjectMocks
	private final AdvancingController advancingController = Mockito.spy(new AdvancingController());

	@Test
	public void testGetAdvancedNurseryTableHeader_returnsTheValueFromColumLabelDefaultName() throws MiddlewareQueryException {
		Term fromOntology = new Term();
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);

		List<TableHeader> tableHeaderList = this.advancingController.getAdvancedNurseryTableHeader();
		Assert.assertEquals("Expecting to return 5 columns but returned " + tableHeaderList.size(), 5, tableHeaderList.size());

		Assert.assertTrue("Expecting to have a column name ENTRY_ID.", this.hasColumnHeader(tableHeaderList, "ENTRY_ID"));
		Assert.assertTrue("Expecting to have a column name DESIGNATION.", this.hasColumnHeader(tableHeaderList, "DESIGNATION"));
		Assert.assertTrue("Expecting to have a column name PARENTAGE.", this.hasColumnHeader(tableHeaderList, "PARENTAGE"));
		Assert.assertTrue("Expecting to have a column name GID.", this.hasColumnHeader(tableHeaderList, "GID"));
		Assert.assertTrue("Expecting to have a column name SEED SOURCE.", this.hasColumnHeader(tableHeaderList, "SEED SOURCE"));
	}

	private boolean hasColumnHeader(List<TableHeader> tableHeaderList, String columnName) {
		for (TableHeader tableHeader : tableHeaderList) {
			if (tableHeader.getColumnName().equals(columnName)) {
				return true;
			}
		}
		return false;
	}

	@Test
	public void testGetAdvancedNurseryTableHeader_returnsTheValueFromOntology() throws MiddlewareQueryException {
		Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);

		List<TableHeader> tableHeaderList = this.advancingController.getAdvancedNurseryTableHeader();
		Assert.assertEquals("Expecting to return 5 columns but returned " + tableHeaderList.size(), 5, tableHeaderList.size());

		for (TableHeader tableHeader : tableHeaderList) {
			Assert.assertEquals("Expecting name from ontology but didn't.", fromOntology.getName(), tableHeader.getColumnName());
		}
	}

	@Test
	public void testPostAdvanceNursery() throws RuleException, MiddlewareException {
		// setup

		AdvancingNurseryForm form = new AdvancingNurseryForm();
		ArrayList<ImportedGermplasm> importedGermplasm = new ArrayList<>();
		importedGermplasm.add(Mockito.mock(ImportedGermplasm.class));

		Method method = new Method();
		method.setMtype("DER");

		this.preparePostAdvanceNursery(form, method, importedGermplasm);

		// scenario 1, has a method choice and breeding method not a Generative
		Map<String, Object> output = this.advancingController.postAdvanceNursery(form, null, null);

		Assert.assertEquals("should be successful", "1", output.get("isSuccess"));
		Assert.assertEquals("should have at least 1 imported germplasm list", importedGermplasm.size(), output.get("listSize"));
		Assert.assertNotNull("should have advance germplasm change details", output.get("advanceGermplasmChangeDetails"));
		Assert.assertNotNull("should have generated unique id", output.get("uniqueId"));

		form.setMethodChoice(null);
		output = this.advancingController.postAdvanceNursery(form, null, null);
		Assert.assertEquals("should be successful", "1", output.get("isSuccess"));

	}

	@Test
	public void testPostAdvanceNurseryThrowsRuleException() throws MiddlewareException, RuleException {
		// setup
		AdvancingNurseryForm form = new AdvancingNurseryForm();
		ArrayList<ImportedGermplasm> importedGermplasm = new ArrayList<>();
		Method method = new Method();
		method.setMtype("DER");

		this.preparePostAdvanceNursery(form, method, importedGermplasm);

		Mockito.when(this.fieldbookService.advanceNursery(Matchers.eq(this.advancingNursery), Matchers.any(Workbook.class))).thenThrow(
				Mockito.mock(RuleException.class));

		Mockito.doNothing().when(this.paginationListSelection).addAdvanceDetails(Matchers.anyString(), Matchers.eq(form));

		// scenario 2, has a method throwing exception
		Map<String, Object> output = this.advancingController.postAdvanceNursery(form, null, null);

		Assert.assertEquals("should fail", "0", output.get("isSuccess"));
		Assert.assertEquals("should have at least 0 imported germplasm list", Integer.valueOf(0), output.get("listSize"));
	}

	@Test
	public void testPostAdvanceNurseryGenerativeMethodError() throws RuleException, MiddlewareException {
		// setup
		AdvancingNurseryForm form = new AdvancingNurseryForm();
		ArrayList<ImportedGermplasm> importedGermplasm = new ArrayList<>();

		Method method = new Method();
		method.setMtype("GEN");

		this.preparePostAdvanceNursery(form, method, importedGermplasm);

		Mockito.when(this.fieldbookService.advanceNursery(Matchers.eq(this.advancingNursery), Matchers.any(Workbook.class))).thenThrow(
				Mockito.mock(RuleException.class));

		// scenario 2, has a method throwing exception
		Map<String, Object> output = this.advancingController.postAdvanceNursery(form, null, null);

		Assert.assertEquals("should fail", "0", output.get("isSuccess"));
		Assert.assertEquals("should have at least 0 imported germplasm list", 0, output.get("listSize"));
		Assert.assertEquals("should have error message", "error.message", output.get("message"));
	}

	private void preparePostAdvanceNursery(AdvancingNurseryForm form, Method method, ArrayList<ImportedGermplasm> importedGermplasm)
			throws RuleException, MiddlewareException {
		// setup
		form.setMethodChoice("1");
		form.setAdvanceBreedingMethodId("10");

		AdvanceResult result = Mockito.mock(AdvanceResult.class);

		Mockito.when(result.getAdvanceList()).thenReturn(importedGermplasm);
		Mockito.when(result.getChangeDetails()).thenReturn(new ArrayList<AdvanceGermplasmChangeDetail>());
		Mockito.when(this.fieldbookMiddlewareService.getMethodById(Matchers.anyInt())).thenReturn(method);
		Mockito.when(this.fieldbookService.advanceNursery(Matchers.eq(this.advancingNursery), Matchers.any(Workbook.class))).thenReturn(
				result);
		Mockito.when(
				this.messageSource.getMessage(Matchers.eq("nursery.save.advance.error.row.list.empty.generative.method"),
						Matchers.any(String[].class), Matchers.eq(LocaleContextHolder.getLocale()))).thenReturn("error.message");

		Mockito.doNothing().when(this.paginationListSelection).addAdvanceDetails(Matchers.anyString(), Matchers.eq(form));

	}

	@Test
	public void testDeleteImportedGermplasmEntriesIfDeleted() {
		List<ImportedGermplasm> importedGermplasms = new ArrayList<ImportedGermplasm>();
		for (int i = 0; i < 10; i++) {
			ImportedGermplasm germplasm = new ImportedGermplasm();
			germplasm.setEntryId(i);
			importedGermplasms.add(germplasm);
		}
		String entries[] = {"1", "2", "3"};
		importedGermplasms = this.advancingController.deleteImportedGermplasmEntries(importedGermplasms, entries);
		Assert.assertEquals("Should have a total of 7 germplasms remaining", 7, importedGermplasms.size());
	}

	private List<ImportedGermplasm> generateGermplasm() {
		List<ImportedGermplasm> importedGermplasms = new ArrayList<ImportedGermplasm>();
		for (int i = 0; i < 10; i++) {
			ImportedGermplasm germplasm = new ImportedGermplasm();
			germplasm.setEntryId(i);
			importedGermplasms.add(germplasm);
		}
		return importedGermplasms;
	}

	@Test
	public void testDeleteImportedGermplasmEntriesIfNoneDeleted() {
		List<ImportedGermplasm> importedGermplasms = this.generateGermplasm();
		String entries[] = {};
		importedGermplasms = this.advancingController.deleteImportedGermplasmEntries(importedGermplasms, entries);
		Assert.assertEquals("Should have a total of 10 germplasms, since nothing is deleted", 10, importedGermplasms.size());
	}

	@Test
	public void testSetupAdvanceReviewDataList() {
		List<ImportedGermplasm> importedGermplasms = this.generateGermplasm();
		List<Map<String, Object>> mapInfos = this.advancingController.setupAdvanceReviewDataList(importedGermplasms);
		Assert.assertEquals("Should have the same number of records", importedGermplasms.size(), mapInfos.size());
	}
}
