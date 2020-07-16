
package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.middleware.constant.ColumnLabels;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.service.api.InventoryService;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.ui.Model;

import com.efficio.fieldbook.web.common.bean.TableHeader;

/**
 * Created by Daniel Villafuerte on 5/8/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class GermplasmListControllerTest {

	public static final String TEST_LIST_NAME = "listName";
	public static final int TEST_LIST_ID = 1;

	public static final int TEST_DATA_SIZE = 5;

	@Mock
	private GermplasmListManager germplasmListManager;

	@Mock
	private MessageSource messageSource;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private InventoryService inventoryService;

	@Mock
	private PlatformTransactionManager transactionManager;

	@InjectMocks
	private final GermplasmListController dut = Mockito.spy(new GermplasmListController());

	@Test
	public void testDisplayAdvancedGermplasmList() throws MiddlewareException {
		GermplasmList list = Mockito.mock(GermplasmList.class);
		Mockito.when(list.getName()).thenReturn(GermplasmListControllerTest.TEST_LIST_NAME);
		Mockito.when(list.getType()).thenReturn(GermplasmListType.ADVANCED.name());
		Mockito.doReturn(list).when(this.germplasmListManager).getGermplasmListById(GermplasmListControllerTest.TEST_LIST_ID);

		List<ListDataProject> testList = this.generateTestListDataProject();
		Mockito.doReturn(testList).when(this.germplasmListManager).retrieveSnapshotListData(GermplasmListControllerTest.TEST_LIST_ID);

		Model model = Mockito.mock(Model.class);
		this.dut.displayAdvanceGermplasmList(GermplasmListControllerTest.TEST_LIST_ID, model);
		Mockito.verify(model).addAttribute("totalNumberOfGermplasms", testList.size());
		Mockito.verify(model).addAttribute("listId", GermplasmListControllerTest.TEST_LIST_ID);
		Mockito.verify(model).addAttribute("listName", GermplasmListControllerTest.TEST_LIST_NAME);
		Mockito.verify(model).addAttribute(Matchers.eq(GermplasmListController.TABLE_HEADER_LIST), Matchers.anyListOf(TableHeader.class));
		Mockito.verify(model).addAttribute(GermplasmListController.GERMPLASM_LIST, testList);

	}

	@Test
	public void testDisplayCrossList() throws MiddlewareException {
		GermplasmList list = Mockito.mock(GermplasmList.class);
		Mockito.when(list.getName()).thenReturn(GermplasmListControllerTest.TEST_LIST_NAME);
		Mockito.when(list.getType()).thenReturn(GermplasmListType.CROSSES.name());
		Mockito.doReturn(list).when(this.germplasmListManager).getGermplasmListById(GermplasmListControllerTest.TEST_LIST_ID);

		List<ListDataProject> testList = this.generateTestListDataProject();
		Mockito.doReturn(testList).when(this.germplasmListManager)
				.retrieveSnapshotListDataWithParents(GermplasmListControllerTest.TEST_LIST_ID);

		Model model = Mockito.mock(Model.class);
		this.dut.displayCrossGermplasmList(GermplasmListControllerTest.TEST_LIST_ID, model);
		Mockito.verify(model).addAttribute("totalNumberOfGermplasms", testList.size());
		Mockito.verify(model).addAttribute("listId", GermplasmListControllerTest.TEST_LIST_ID);
		Mockito.verify(model).addAttribute("listName", GermplasmListControllerTest.TEST_LIST_NAME);
		Mockito.verify(model).addAttribute(Matchers.eq(GermplasmListController.TABLE_HEADER_LIST), Matchers.anyListOf(TableHeader.class));
		Mockito.verify(model).addAttribute(GermplasmListController.GERMPLASM_LIST, testList);

		Mockito.verify(this.germplasmListManager).retrieveSnapshotListDataWithParents(Matchers.anyInt());

	}

	@Test
	public void testGetCrossStockListTableHeaders() {
		final List<TableHeader> tableHeaders = this.dut.getCrossStockListTableHeaders();
		Mockito.verify(this.ontologyDataManager).getTermById(ColumnLabels.ENTRY_ID.getTermId().getId());
		Mockito.verify(this.ontologyDataManager).getTermById(ColumnLabels.DESIGNATION.getTermId().getId());
		Mockito.verify(this.ontologyDataManager).getTermById(ColumnLabels.PARENTAGE.getTermId().getId());
		Mockito.verify(this.ontologyDataManager).getTermById(ColumnLabels.GID.getTermId().getId());
		Mockito.verify(this.ontologyDataManager).getTermById(ColumnLabels.GROUP_ID.getTermId().getId());
		Mockito.verify(this.ontologyDataManager).getTermById(ColumnLabels.SEED_SOURCE.getTermId().getId());
		Mockito.verify(this.ontologyDataManager).getTermById(ColumnLabels.LOT_LOCATION.getTermId().getId());
		Mockito.verify(this.ontologyDataManager).getTermById(ColumnLabels.BULK_COMPL.getTermId().getId());
		Mockito.verify(this.ontologyDataManager).getTermById(ColumnLabels.BULK_WITH.getTermId().getId());
		Mockito.verify(this.ontologyDataManager).getTermById(ColumnLabels.DUPLICATE.getTermId().getId());
		Mockito.verify(this.ontologyDataManager).getTermById(ColumnLabels.AMOUNT.getTermId().getId());
		Mockito.verify(this.ontologyDataManager).getTermById(ColumnLabels.UNITS.getTermId().getId());
		Mockito.verify(this.ontologyDataManager).getTermById(ColumnLabels.STOCKID_INVENTORY.getTermId().getId());
		Mockito.verify(this.ontologyDataManager).getTermById(ColumnLabels.COMMENT.getTermId().getId());
		Assert.assertEquals(14, tableHeaders.size());
	}

	@Test
	public void testDisplayStockList(){
		GermplasmList list = Mockito.mock(GermplasmList.class);
		Mockito.when(list.getName()).thenReturn(GermplasmListControllerTest.TEST_LIST_NAME);
		Mockito.when(list.getType()).thenReturn(GermplasmListType.ADVANCED.name());
		Mockito.doReturn(list).when(this.germplasmListManager).getGermplasmListById(GermplasmListControllerTest.TEST_LIST_ID);

		List<InventoryDetails> inventoryDetailsList = this.generateInventoryDetails();
		Mockito.doReturn(inventoryDetailsList).when(this.inventoryService).getInventoryListByListDataProjectListId(GermplasmListControllerTest.TEST_LIST_ID);

		Model model = Mockito.mock(Model.class);
		this.dut.displayStockList(GermplasmListControllerTest.TEST_LIST_ID, Mockito.mock(HttpServletRequest.class), model);

		Mockito.verify(model).addAttribute("totalNumberOfGermplasms", inventoryDetailsList.size());
		Mockito.verify(model).addAttribute("listId", GermplasmListControllerTest.TEST_LIST_ID);
		Mockito.verify(model).addAttribute("listName", GermplasmListControllerTest.TEST_LIST_NAME);
		Mockito.verify(model).addAttribute(Matchers.eq(GermplasmListController.TABLE_HEADER_LIST), Matchers.anyListOf(TableHeader.class));
	}

	protected List<ListDataProject> generateTestListDataProject() {
		List<ListDataProject> dataProjectList = new ArrayList<>();

		for (int i = 0; i < GermplasmListControllerTest.TEST_DATA_SIZE; i++) {
			ListDataProject data = new ListDataProject();
			data.setEntryId(i);
			data.setGermplasmId(i);
			dataProjectList.add(data);

		}

		return dataProjectList;
	}

	protected List<InventoryDetails> generateInventoryDetails(){
		List<InventoryDetails> inventoryDetailsList = new ArrayList<>();
		InventoryDetails inventoryDetails = new InventoryDetails();

		inventoryDetails.setGid(100);
		inventoryDetails.setGermplasmName("Germplasm_Name");
		inventoryDetailsList.add(inventoryDetails);

		return inventoryDetailsList;
	}
}
