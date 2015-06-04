
package com.efficio.fieldbook.web.inventory.controller;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.InventoryService;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.ui.Model;

import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.common.bean.TableHeader;
import com.efficio.fieldbook.web.inventory.bean.SeedSelection;
import com.efficio.fieldbook.web.inventory.form.SeedStoreForm;

public class SeedStoreManagerControllerTest {

	@Mock
	private InventoryService inventoryService;
	@Mock
	private FieldbookService fiedbookService;
	@Mock
	private OntologyService ontologyService;
	@Mock
	private OntologyDataManager ontologyDataManager;
	@Mock
	public MessageSource messageSource;
	private WorkbenchDataManager workbenchDataManager;

	private SeedStoreManagerController seedStoreManager;

	private Integer listId;

	@Before
	public void setUp() throws MiddlewareQueryException {
		MockitoAnnotations.initMocks(this);

		this.inventoryService = Mockito.mock(InventoryService.class);
		this.fiedbookService = Mockito.mock(FieldbookService.class);
		this.ontologyService = Mockito.mock(OntologyService.class);
		this.workbenchDataManager = Mockito.mock(WorkbenchDataManager.class);
		this.seedStoreManager = new SeedStoreManagerController();
		this.seedStoreManager.setInventoryMiddlewareService(this.inventoryService);
		this.seedStoreManager.setPaginationListSelection(Mockito.mock(PaginationListSelection.class));
		this.seedStoreManager.setSeedSelection(Mockito.mock(SeedSelection.class));
		this.seedStoreManager.setFieldbookMiddlewareService(this.fiedbookService);
		this.seedStoreManager.setOntologyService(this.ontologyService);
		this.seedStoreManager.setOntologyDataManager(this.ontologyDataManager);
		this.seedStoreManager.setMessageSource(this.messageSource);
		this.seedStoreManager.setHttpRequest(Mockito.mock(HttpServletRequest.class));
		this.seedStoreManager.setWorkbenchDataManager(this.workbenchDataManager);

		Project testProject = new Project();
		testProject.setUniqueID(UUID.randomUUID().toString());
		Mockito.when(this.workbenchDataManager.getLastOpenedProjectAnyUser()).thenReturn(testProject);
		this.listId = 2;
	}

	@Test
	public void testGetInventoryGermplasmDetailsPageCrosses() throws MiddlewareQueryException {
		String germplasmListType = GermplasmListType.CROSSES.name();
		this.seedStoreManager.getInventoryGermplasmDetailsPage(Mockito.mock(SeedStoreForm.class), this.listId, Mockito.mock(Model.class),
				germplasmListType, "");
		Mockito.verify(this.inventoryService, Mockito.times(1)).getInventoryDetailsByGermplasmList(this.listId, germplasmListType);
	}

	@Test
	public void testDisplayCrossesGermplasmDetails() throws MiddlewareQueryException {
		String germplasmListType = GermplasmListType.CROSSES.name();
		this.seedStoreManager.displayCrossesGermplasmDetails(this.listId, Mockito.mock(SeedStoreForm.class),
				Mockito.mock(HttpServletRequest.class), Mockito.mock(Model.class));
		Mockito.verify(this.inventoryService, Mockito.times(1)).getInventoryDetailsByGermplasmList(this.listId, germplasmListType);
	}

	@Test
	public void testGetInventoryGermplasmDetailsPageAdvanced() throws MiddlewareQueryException {
		String germplasmListType = GermplasmListType.ADVANCED.name();
		this.seedStoreManager.getInventoryGermplasmDetailsPage(Mockito.mock(SeedStoreForm.class), this.listId, Mockito.mock(Model.class),
				germplasmListType, "");
		Mockito.verify(this.inventoryService, Mockito.times(1)).getInventoryDetailsByGermplasmList(this.listId, germplasmListType);
	}

	@Test
	public void testDisplayAdvancedGermplasmDetails() throws MiddlewareQueryException {
		String germplasmListType = GermplasmListType.ADVANCED.name();
		this.seedStoreManager.displayAdvanceGermplasmDetails(this.listId, Mockito.mock(SeedStoreForm.class),
				Mockito.mock(HttpServletRequest.class), Mockito.mock(Model.class));
		Mockito.verify(this.inventoryService, Mockito.times(1)).getInventoryDetailsByGermplasmList(this.listId, germplasmListType);
	}

	@Test
	public void testGetSeedInventoryTableHeader_returnsTheValueFromOntology() throws MiddlewareQueryException {
		Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.LOT_LOCATION_INVENTORY.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.AMOUNT_INVENTORY.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SCALE_INVENTORY.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.COMMENT_INVENTORY.getId())).thenReturn(fromOntology);

		Locale locale = LocaleContextHolder.getLocale();
		String dummyString = "Column Label";
		Mockito.when(this.messageSource.getMessage("seed.entry.number", null, locale)).thenReturn(dummyString);
		Mockito.when(this.messageSource.getMessage("seed.entry.designation", null, locale)).thenReturn(dummyString);
		Mockito.when(this.messageSource.getMessage("seed.entry.parentage", null, locale)).thenReturn(dummyString);
		Mockito.when(this.messageSource.getMessage("seed.inventory.gid", null, locale)).thenReturn(dummyString);
		Mockito.when(this.messageSource.getMessage("seed.inventory.source", null, locale)).thenReturn(dummyString);
		Mockito.when(this.messageSource.getMessage("seed.inventory.table.location", null, locale)).thenReturn(dummyString);
		Mockito.when(this.messageSource.getMessage("seed.inventory.amount", null, locale)).thenReturn(dummyString);
		Mockito.when(this.messageSource.getMessage("seed.inventory.table.scale", null, locale)).thenReturn(dummyString);
		Mockito.when(this.messageSource.getMessage("seed.inventory.comment", null, locale)).thenReturn(dummyString);

		List<TableHeader> tableHeaderList = this.seedStoreManager.getSeedInventoryTableHeader();
		Assert.assertEquals("Expecting to return 9 columns but returned " + tableHeaderList.size(), 9, tableHeaderList.size());

		for (TableHeader tableHeader : tableHeaderList) {
			Assert.assertEquals("Expecting name from ontology but didn't.", fromOntology.getName(), tableHeader.getColumnName());
		}
	}

	@Test
	public void testGetSeedInventoryTableHeader_returnsTheValueFromColumLabelDefaultName() throws MiddlewareQueryException {
		Term fromOntology = new Term();
		Mockito.when(this.ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.LOT_LOCATION_INVENTORY.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.AMOUNT_INVENTORY.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.SCALE_INVENTORY.getId())).thenReturn(fromOntology);
		Mockito.when(this.ontologyDataManager.getTermById(TermId.COMMENT_INVENTORY.getId())).thenReturn(fromOntology);

		Locale locale = LocaleContextHolder.getLocale();
		String dummyString = "Column Label";
		Mockito.when(this.messageSource.getMessage("seed.entry.number", null, locale)).thenReturn(dummyString);
		Mockito.when(this.messageSource.getMessage("seed.entry.designation", null, locale)).thenReturn(dummyString);
		Mockito.when(this.messageSource.getMessage("seed.entry.parentage", null, locale)).thenReturn(dummyString);
		Mockito.when(this.messageSource.getMessage("seed.inventory.gid", null, locale)).thenReturn(dummyString);
		Mockito.when(this.messageSource.getMessage("seed.inventory.source", null, locale)).thenReturn(dummyString);
		Mockito.when(this.messageSource.getMessage("seed.inventory.table.location", null, locale)).thenReturn(dummyString);
		Mockito.when(this.messageSource.getMessage("seed.inventory.amount", null, locale)).thenReturn(dummyString);
		Mockito.when(this.messageSource.getMessage("seed.inventory.table.scale", null, locale)).thenReturn(dummyString);
		Mockito.when(this.messageSource.getMessage("seed.inventory.comment", null, locale)).thenReturn(dummyString);

		List<TableHeader> tableHeaderList = this.seedStoreManager.getSeedInventoryTableHeader();
		Assert.assertEquals("Expecting to return 9 columns but returned " + tableHeaderList.size(), 9, tableHeaderList.size());

		Assert.assertTrue("Expecting to have a column name ENTRY_ID.", this.hasColumnHeader(tableHeaderList, "ENTRY_ID"));
		Assert.assertTrue("Expecting to have a column name DESIGNATION.", this.hasColumnHeader(tableHeaderList, "DESIGNATION"));
		Assert.assertTrue("Expecting to have a column name PARENTAGE.", this.hasColumnHeader(tableHeaderList, "PARENTAGE"));
		Assert.assertTrue("Expecting to have a column name GID.", this.hasColumnHeader(tableHeaderList, "GID"));
		Assert.assertTrue("Expecting to have a column name SEED SOURCE.", this.hasColumnHeader(tableHeaderList, "SEED SOURCE"));
		Assert.assertTrue("Expecting to have a column name LOCATION.", this.hasColumnHeader(tableHeaderList, "LOCATION"));
		Assert.assertTrue("Expecting to have a column name AMOUNT.", this.hasColumnHeader(tableHeaderList, "AMOUNT"));
		Assert.assertTrue("Expecting to have a column name SCALE.", this.hasColumnHeader(tableHeaderList, "SCALE"));
		Assert.assertTrue("Expecting to have a column name COMMENT.", this.hasColumnHeader(tableHeaderList, "COMMENT"));
	}

	private boolean hasColumnHeader(List<TableHeader> tableHeaderList, String columnName) {
		for (TableHeader tableHeader : tableHeaderList) {
			if (tableHeader.getColumnName().equals(columnName)) {
				return true;
			}
		}
		return false;
	}
}
