package com.efficio.fieldbook.web.inventory.controller;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.service.api.InventoryService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import com.efficio.fieldbook.web.common.bean.PaginationListSelection;
import com.efficio.fieldbook.web.inventory.bean.SeedSelection;
import com.efficio.fieldbook.web.inventory.controller.SeedStoreManagerController;
import com.efficio.fieldbook.web.inventory.form.SeedStoreForm;

public class SeedStoreManagerControllerTest {
	private InventoryService inventoryService;
	private SeedStoreManagerController seedStoreManager;
	private Integer listId;
	@Before
	public void setUp(){
		inventoryService = Mockito.mock(InventoryService.class);
		seedStoreManager = new SeedStoreManagerController();
		seedStoreManager.setInventoryMiddlewareService(inventoryService);
		seedStoreManager.setPaginationListSelection(Mockito.mock(PaginationListSelection.class));
		seedStoreManager.setSeedSelection(Mockito.mock(SeedSelection.class));
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
}
