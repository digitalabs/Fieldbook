package com.efficio.fieldbook.web.common.controller;

import com.efficio.fieldbook.web.common.bean.TableHeader;
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
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * Created by Daniel Villafuerte on 5/8/2015.
 */
@RunWith(MockitoJUnitRunner.class)
public class GermplasmListControllerTest {

    public static final String TEST_LIST_NAME  = "listName";
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
    private GermplasmListController dut = spy(new GermplasmListController());

    @Test
    public void testDisplayAdvancedGermplasmList() throws MiddlewareException{
        GermplasmList list = mock(GermplasmList.class);
        when(list.getName()).thenReturn(TEST_LIST_NAME);
        when(list.getType()).thenReturn(GermplasmListType.ADVANCED.name());
        doReturn(list).when(germplasmListManager).getGermplasmListById(TEST_LIST_ID);

        List<ListDataProject> testList = generateTestListDataProject();
        doReturn(testList).when(germplasmListManager).retrieveSnapshotListData(TEST_LIST_ID);

        Model model = mock(Model.class);
        dut.displayAdvanceGermplasmList(TEST_LIST_ID, mock(HttpServletRequest.class), model);
        verify(model).addAttribute("totalNumberOfGermplasms", testList.size());
        verify(model).addAttribute("listId", TEST_LIST_ID);
        verify(model).addAttribute("listName", TEST_LIST_NAME);
        verify(model).addAttribute(eq(GermplasmListController.TABLE_HEADER_LIST), anyListOf(TableHeader.class));
        verify(model).addAttribute(GermplasmListController.GERMPLASM_LIST, testList);

    }

    @Test
    public void testDisplayCrossList() throws MiddlewareException{
        GermplasmList list = mock(GermplasmList.class);
        when(list.getName()).thenReturn(TEST_LIST_NAME);
        when(list.getType()).thenReturn(GermplasmListType.CROSSES.name());
        doReturn(list).when(germplasmListManager).getGermplasmListById(TEST_LIST_ID);

        List<ListDataProject> testList = generateTestListDataProject();
        doReturn(testList).when(germplasmListManager).retrieveSnapshotListDataWithParents(TEST_LIST_ID);

        Model model = mock(Model.class);
        dut.displayCrossGermplasmList(TEST_LIST_ID, mock(HttpServletRequest.class), model);
        verify(model).addAttribute("totalNumberOfGermplasms", testList.size());
        verify(model).addAttribute("listId", TEST_LIST_ID);
        verify(model).addAttribute("listName", TEST_LIST_NAME);
        verify(model).addAttribute(eq(GermplasmListController.TABLE_HEADER_LIST), anyListOf(TableHeader.class));
        verify(model).addAttribute(GermplasmListController.GERMPLASM_LIST, testList);

        verify(germplasmListManager).retrieveSnapshotListDataWithParents(anyInt());

    }

    protected List<ListDataProject> generateTestListDataProject() {
        List<ListDataProject> dataProjectList = new ArrayList<>();

        for (int i = 0; i < TEST_DATA_SIZE; i++) {
            ListDataProject data = new ListDataProject();
            data.setEntryId(i);
            data.setGermplasmId(i);
            dataProjectList.add(data);

        }

        return dataProjectList;
    }
}
