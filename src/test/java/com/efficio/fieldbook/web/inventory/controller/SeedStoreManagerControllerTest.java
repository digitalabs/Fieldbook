package com.efficio.fieldbook.web.inventory.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import junit.framework.Assert;

import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.InventoryService;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;

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
	
	private SeedStoreManagerController seedStoreManager;
	
	private Integer listId;
	
	@Before
	public void setUp(){
		MockitoAnnotations.initMocks(this);
		
		seedStoreManager = new SeedStoreManagerController();
		seedStoreManager.setInventoryMiddlewareService(inventoryService);
		seedStoreManager.setPaginationListSelection(Mockito.mock(PaginationListSelection.class));
		seedStoreManager.setSeedSelection(Mockito.mock(SeedSelection.class));
		seedStoreManager.setFieldbookMiddlewareService(fiedbookService);
		seedStoreManager.setOntologyService(ontologyService);
		seedStoreManager.setOntologyDataManager(ontologyDataManager);
		seedStoreManager.setMessageSource(messageSource);
		listId = 2;
	}
	
	@Test
	public void testGetInventoryGermplasmDetailsPageCrosses() throws MiddlewareQueryException{
		String germplasmListType = GermplasmListType.CROSSES.name();
		seedStoreManager.getInventoryGermplasmDetailsPage(Mockito.mock(SeedStoreForm.class), listId, Mockito.mock(Model.class), germplasmListType, "");
		Mockito.verify(inventoryService, Mockito.times(1)).getInventoryDetailsByGermplasmList(listId, germplasmListType);
	}
	

	@Test
	public void testDisplayCrossesGermplasmDetails() throws MiddlewareQueryException{
		String germplasmListType = GermplasmListType.CROSSES.name();
		seedStoreManager.displayCrossesGermplasmDetails(listId, Mockito.mock(SeedStoreForm.class), Mockito.mock(HttpServletRequest.class), Mockito.mock(Model.class));
		Mockito.verify(inventoryService, Mockito.times(1)).getInventoryDetailsByGermplasmList(listId, germplasmListType);
	}
	
	@Test
	public void testGetInventoryGermplasmDetailsPageAdvanced() throws MiddlewareQueryException{
		String germplasmListType = GermplasmListType.ADVANCED.name();
		seedStoreManager.getInventoryGermplasmDetailsPage(Mockito.mock(SeedStoreForm.class), listId, Mockito.mock(Model.class), germplasmListType, "");
		Mockito.verify(inventoryService, Mockito.times(1)).getInventoryDetailsByGermplasmList(listId, germplasmListType);
	}
	
	@Test
	public void testDisplayAdvancedGermplasmDetails() throws MiddlewareQueryException{
		String germplasmListType = GermplasmListType.ADVANCED.name();
		seedStoreManager.displayAdvanceGermplasmDetails(listId, Mockito.mock(SeedStoreForm.class), Mockito.mock(HttpServletRequest.class), Mockito.mock(Model.class));
		Mockito.verify(inventoryService, Mockito.times(1)).getInventoryDetailsByGermplasmList(listId, germplasmListType);
	}
	
	@Test
	public void testGetLocationList() throws MiddlewareQueryException{
		seedStoreManager.getLocationList();
		Mockito.verify(fiedbookService, Mockito.times(1)).getAllSeedLocations();
	}
	
	@Test
	public void testGetFavoriteLocationList() throws MiddlewareQueryException{		
		List<Long> locationsIds = new ArrayList();
		locationsIds.add(new Long(1));
		Mockito.when(fiedbookService.getFavoriteProjectLocationIds()).thenReturn(locationsIds);
		
		seedStoreManager.getFavoriteLocationList();
		
		Mockito.verify(fiedbookService, Mockito.times(1)).getFavoriteProjectLocationIds();
		Mockito.verify(fiedbookService, Mockito.times(1)).getFavoriteLocationByProjectId(locationsIds);
	}
	
	@Test
	public void testGetScaleList() throws MiddlewareQueryException{
		seedStoreManager.getScaleList();
		Mockito.verify(ontologyService, Mockito.times(1)).getAllInventoryScales();
	}
	
	@Test
	public void testGetSeedInventoryTableHeader_returnsTheValueFromOntology() throws MiddlewareQueryException{
		Term fromOntology = new Term();
		fromOntology.setName("Ontology Name");
		when(ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.LOT_LOCATION_INVENTORY.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.AMOUNT_INVENTORY.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.SCALE_INVENTORY.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.COMMENT_INVENTORY.getId())).thenReturn(fromOntology);
		
		Locale locale = LocaleContextHolder.getLocale();
		String dummyString = "Column Label";
		when(messageSource.getMessage("seed.entry.number", null, locale)).thenReturn(dummyString);
		when(messageSource.getMessage("seed.entry.designation", null, locale)).thenReturn(dummyString);
		when(messageSource.getMessage("seed.entry.parentage", null, locale)).thenReturn(dummyString);
		when(messageSource.getMessage("seed.inventory.gid", null, locale)).thenReturn(dummyString);
		when(messageSource.getMessage("seed.inventory.source", null, locale)).thenReturn(dummyString);
		when(messageSource.getMessage("seed.inventory.table.location", null, locale)).thenReturn(dummyString);
		when(messageSource.getMessage("seed.inventory.amount", null, locale)).thenReturn(dummyString);
		when(messageSource.getMessage("seed.inventory.table.scale", null, locale)).thenReturn(dummyString);
		when(messageSource.getMessage("seed.inventory.comment", null, locale)).thenReturn(dummyString);
		
		List<TableHeader> tableHeaderList = seedStoreManager.getSeedInventoryTableHeader();
		Assert.assertEquals("Expecting to return 9 columns but returned " + tableHeaderList.size(), 9, tableHeaderList.size());
		
		for(TableHeader tableHeader : tableHeaderList){		
			Assert.assertEquals("Expecting name from ontology but didn't.", fromOntology.getName(),tableHeader.getColumnName());
		}
	}
	
	@Test
	public void testGetSeedInventoryTableHeader_returnsTheValueFromColumLabelDefaultName() throws MiddlewareQueryException{
		Term fromOntology = new Term();
		when(ontologyDataManager.getTermById(TermId.ENTRY_NO.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.DESIG.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.CROSS.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.GID.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.SEED_SOURCE.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.LOT_LOCATION_INVENTORY.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.AMOUNT_INVENTORY.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.SCALE_INVENTORY.getId())).thenReturn(fromOntology);
		when(ontologyDataManager.getTermById(TermId.COMMENT_INVENTORY.getId())).thenReturn(fromOntology);
		
		Locale locale = LocaleContextHolder.getLocale();
		String dummyString = "Column Label";
		when(messageSource.getMessage("seed.entry.number", null, locale)).thenReturn(dummyString);
		when(messageSource.getMessage("seed.entry.designation", null, locale)).thenReturn(dummyString);
		when(messageSource.getMessage("seed.entry.parentage", null, locale)).thenReturn(dummyString);
		when(messageSource.getMessage("seed.inventory.gid", null, locale)).thenReturn(dummyString);
		when(messageSource.getMessage("seed.inventory.source", null, locale)).thenReturn(dummyString);
		when(messageSource.getMessage("seed.inventory.table.location", null, locale)).thenReturn(dummyString);
		when(messageSource.getMessage("seed.inventory.amount", null, locale)).thenReturn(dummyString);
		when(messageSource.getMessage("seed.inventory.table.scale", null, locale)).thenReturn(dummyString);
		when(messageSource.getMessage("seed.inventory.comment", null, locale)).thenReturn(dummyString);
		
		List<TableHeader> tableHeaderList = seedStoreManager.getSeedInventoryTableHeader();
		Assert.assertEquals("Expecting to return 9 columns but returned " + tableHeaderList.size(), 9, tableHeaderList.size());
		
		Assert.assertTrue("Expecting to have a column name ENTRY_ID.", hasColumnHeader(tableHeaderList,"ENTRY_ID"));
		Assert.assertTrue("Expecting to have a column name DESIGNATION.", hasColumnHeader(tableHeaderList,"DESIGNATION"));
		Assert.assertTrue("Expecting to have a column name PARENTAGE.", hasColumnHeader(tableHeaderList,"PARENTAGE"));
		Assert.assertTrue("Expecting to have a column name GID.", hasColumnHeader(tableHeaderList,"GID"));
		Assert.assertTrue("Expecting to have a column name SEED SOURCE.", hasColumnHeader(tableHeaderList,"SEED SOURCE"));
		Assert.assertTrue("Expecting to have a column name LOCATION.", hasColumnHeader(tableHeaderList,"LOCATION"));
		Assert.assertTrue("Expecting to have a column name AMOUNT.", hasColumnHeader(tableHeaderList,"AMOUNT"));
		Assert.assertTrue("Expecting to have a column name SCALE.", hasColumnHeader(tableHeaderList,"SCALE"));
		Assert.assertTrue("Expecting to have a column name COMMENT.", hasColumnHeader(tableHeaderList,"COMMENT"));
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
}
