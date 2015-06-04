
package com.efficio.fieldbook.web.common.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.service.api.InventoryService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
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
		this.dut.displayAdvanceGermplasmList(GermplasmListControllerTest.TEST_LIST_ID, Mockito.mock(HttpServletRequest.class), model);
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
		this.dut.displayCrossGermplasmList(GermplasmListControllerTest.TEST_LIST_ID, Mockito.mock(HttpServletRequest.class), model);
		Mockito.verify(model).addAttribute("totalNumberOfGermplasms", testList.size());
		Mockito.verify(model).addAttribute("listId", GermplasmListControllerTest.TEST_LIST_ID);
		Mockito.verify(model).addAttribute("listName", GermplasmListControllerTest.TEST_LIST_NAME);
		Mockito.verify(model).addAttribute(Matchers.eq(GermplasmListController.TABLE_HEADER_LIST), Matchers.anyListOf(TableHeader.class));
		Mockito.verify(model).addAttribute(GermplasmListController.GERMPLASM_LIST, testList);

		Mockito.verify(this.germplasmListManager).retrieveSnapshotListDataWithParents(Matchers.anyInt());

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
}
