package com.efficio.fieldbook.web.inventory.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.InventoryService;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ui.Model;

import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.inventory.bean.SeedSelection;
import com.efficio.fieldbook.web.inventory.form.SeedStoreForm;

public class SeedStoreManagerControllerTest {
	private InventoryService inventoryService;
	private SeedStoreManagerController seedStoreManager;
	private FieldbookService fiedbookService;
	private OntologyService ontologyService;
	private WorkbenchDataManager workbenchDataManager;
	
	private Integer listId;
	@Before
	public void setUp() throws MiddlewareQueryException {
		inventoryService = Mockito.mock(InventoryService.class);
		fiedbookService = Mockito.mock(FieldbookService.class);
		ontologyService = Mockito.mock(OntologyService.class);
		workbenchDataManager = Mockito.mock(WorkbenchDataManager.class);
		seedStoreManager = new SeedStoreManagerController();
		seedStoreManager.setInventoryMiddlewareService(inventoryService);
		seedStoreManager.setPaginationListSelection(Mockito.mock(PaginationListSelection.class));
		seedStoreManager.setSeedSelection(Mockito.mock(SeedSelection.class));
		seedStoreManager.setFieldbookMiddlewareService(fiedbookService);
		seedStoreManager.setOntologyService(ontologyService);
		seedStoreManager.setHttpRequest(Mockito.mock(HttpServletRequest.class));
		seedStoreManager.setWorkbenchDataManager(workbenchDataManager);
		
		Project testProject = new Project();
		testProject.setUniqueID(UUID.randomUUID().toString());
		Mockito.when(workbenchDataManager.getLastOpenedProjectAnyUser()).thenReturn(testProject);
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
		List<Long> locationsIds = new ArrayList<Long>();
		locationsIds.add(new Long(1));
		Mockito.when(fiedbookService.getFavoriteProjectLocationIds(Mockito.anyString())).thenReturn(locationsIds);
		
		seedStoreManager.getFavoriteLocationList();
		
		Mockito.verify(fiedbookService, Mockito.times(1)).getFavoriteProjectLocationIds(Mockito.anyString());
		Mockito.verify(fiedbookService, Mockito.times(1)).getFavoriteLocationByProjectId(locationsIds);
	}
	
	@Test
	public void testGetScaleList() throws MiddlewareQueryException{
		seedStoreManager.getScaleList();
		Mockito.verify(ontologyService, Mockito.times(1)).getAllInventoryScales();
	}
}
