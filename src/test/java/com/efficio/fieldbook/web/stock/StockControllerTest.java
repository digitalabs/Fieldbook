package com.efficio.fieldbook.web.stock;

import org.apache.commons.lang3.tuple.Pair;
import org.generationcp.commons.service.StockService;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.InventoryService;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by Daniel Villafuerte on 5/8/2015.
 */

@RunWith(MockitoJUnitRunner.class)
public class StockControllerTest {

    public static final String TEST_BREEDER_IDENTIFIER = "AB";
    public static final String TEST_SEPARATOR = "-";
    public static final int GERMPLASM_DATA_COUNT = 5;
    public static final int TEST_GERMPLASM_LIST_DATA_LIST_ID = 1;
    public static final int TEST_LISTDATA_PROJECT_LIST_ID = 2;
    public static final int TEST_CURRENT_USER_ID = 1;

    public static final String TEST_LIST_NAME = "MyListName";


    @Mock
    private StockService stockService;

    @Mock
    private MessageSource messageSource;

    @Mock
    private StockListGenerationSettings generationSettings;

    @Mock
    private InventoryService inventoryService;

	@Mock
	private FieldbookService fiedbookService;
	
	@Mock
	private OntologyService ontologyService;

    @Mock
    private InventoryDataManager inventoryDataManager;

    @Mock
    private GermplasmListManager germplasmListManager;

    @InjectMocks
    private StockController dut = spy(new StockController());

	@Test
	public void testGetLocationList() throws MiddlewareQueryException{
		dut.getLocationList();
		Mockito.verify(fiedbookService, Mockito.times(1)).getAllSeedLocations();
	}
	
	@Test
	public void testGetFavoriteLocationList() throws MiddlewareQueryException{		
		List<Long> locationsIds = new ArrayList<Long>();
		locationsIds.add(new Long(1));
		Mockito.when(fiedbookService.getFavoriteProjectLocationIds(Mockito.anyString())).thenReturn(locationsIds);
		
		dut.getFavoriteLocationList();
		
		Mockito.verify(fiedbookService, Mockito.times(1)).getFavoriteProjectLocationIds(Mockito.anyString());
		Mockito.verify(fiedbookService, Mockito.times(1)).getFavoriteLocationByProjectId(locationsIds);
	}
	
	@Test
	public void testGetScaleList() throws MiddlewareQueryException{
		dut.getScaleList();
		Mockito.verify(ontologyService, Mockito.times(1)).getAllInventoryScales();
	}
	
    @Test
    public void testGenerateValidPrefix() throws MiddlewareException{
        StockListGenerationSettings param = new StockListGenerationSettings(TEST_BREEDER_IDENTIFIER, TEST_SEPARATOR);
        String validPrefix = TEST_BREEDER_IDENTIFIER + 1 + TEST_SEPARATOR;
        doReturn(validPrefix).when(stockService).calculateNextStockIDPrefix(TEST_BREEDER_IDENTIFIER, TEST_SEPARATOR);

        Map<String, String> results = dut.retrieveNextStockIDPrefix(param);
        assertEquals(StockController.SUCCESS, results.get(StockController.IS_SUCCESS));
        assertEquals(TEST_BREEDER_IDENTIFIER + 1, results.get("prefix"));
    }

    @Test
    public void testCalculatePrefixMiddlewareExceptionThrown() throws MiddlewareException {
        StockListGenerationSettings param = new StockListGenerationSettings(TEST_BREEDER_IDENTIFIER, TEST_SEPARATOR);
        doThrow(MiddlewareException.class).when(stockService).calculateNextStockIDPrefix(TEST_BREEDER_IDENTIFIER, TEST_SEPARATOR);

        Map<String, String> results = dut.retrieveNextStockIDPrefix(param);
        assertEquals(StockController.FAILURE, results.get(StockController.IS_SUCCESS));

    }

    @Test
    public void testCalculatePrefixValidationError() throws MiddlewareException {
        StockListGenerationSettings param = new StockListGenerationSettings("AB12", TEST_SEPARATOR);
        doReturn("ABC").when(messageSource).getMessage(anyString(), any(Object[].class), any(Locale.class));
        Map<String, String> results = dut.retrieveNextStockIDPrefix(param);
        assertEquals(StockController.FAILURE, results.get(StockController.IS_SUCCESS));
        assertNotNull(results.get(StockController.ERROR_MESSAGE));
        assertTrue(results.get(StockController.ERROR_MESSAGE).length() > 0);

    }

    @Test
    public void testGenerateStockListForAdvanceList() throws MiddlewareException {
        StockListGenerationSettings param = new StockListGenerationSettings(TEST_BREEDER_IDENTIFIER, TEST_SEPARATOR);
        String validPrefix = TEST_BREEDER_IDENTIFIER + 1 + TEST_SEPARATOR;
        doReturn(validPrefix).when(stockService).calculateNextStockIDPrefix(TEST_BREEDER_IDENTIFIER, TEST_SEPARATOR);
        doReturn(TEST_GERMPLASM_LIST_DATA_LIST_ID).when(germplasmListManager).retrieveDataListIDFromListDataProjectListID(TEST_LISTDATA_PROJECT_LIST_ID);

        Pair<List<ListDataProject>, List<GermplasmListData>> testData = generateTestLists();
        List<ListDataProject> dataProjectList = testData.getLeft();
        List<GermplasmListData> germplasmListDatas = testData.getRight();

        doReturn(germplasmListDatas).when(germplasmListManager).getGermplasmListDataByListId(TEST_GERMPLASM_LIST_DATA_LIST_ID, 0, Integer.MAX_VALUE);
        doReturn(dataProjectList).when(germplasmListManager).retrieveSnapshotListData(TEST_LISTDATA_PROJECT_LIST_ID);

        doReturn(TEST_CURRENT_USER_ID).when(dut).getCurrentIbdbUserId();

        Map<String, String> resultMap = dut.generateStockList(param, TEST_LISTDATA_PROJECT_LIST_ID);

        ArgumentCaptor<InventoryDetails> detailParam = ArgumentCaptor.forClass(InventoryDetails.class);
        ArgumentCaptor<ListDataProject> listDataParam = ArgumentCaptor.forClass(ListDataProject.class);
        verify(inventoryService, atMost(GERMPLASM_DATA_COUNT)).addLotAndTransaction(detailParam.capture(), any(GermplasmListData.class), listDataParam.capture());

        assertEquals(StockController.SUCCESS, resultMap.get(StockController.IS_SUCCESS));

        assertEquals(validPrefix + listDataParam.getValue().getEntryId(), detailParam.getValue().getInventoryID());
        assertEquals(listDataParam.getValue().getGermplasmId(), detailParam.getValue().getGid());
        assertEquals(Location.UNKNOWN_LOCATION_ID, detailParam.getValue().getLocationId().intValue());
        assertEquals(TEST_GERMPLASM_LIST_DATA_LIST_ID, detailParam.getValue().getSourceId().intValue());

    }

    @Test
    public void testGenerateGermplasmMap() throws MiddlewareException{
        Pair<List<ListDataProject>, List<GermplasmListData>> testData = generateTestLists();
        List<ListDataProject> dataProjectList = testData.getLeft();
        List<GermplasmListData> germplasmListDatas = testData.getRight();

        doReturn(germplasmListDatas).when(germplasmListManager).getGermplasmListDataByListId(TEST_GERMPLASM_LIST_DATA_LIST_ID, 0, Integer.MAX_VALUE);
        doReturn(dataProjectList).when(germplasmListManager).retrieveSnapshotListData(TEST_LISTDATA_PROJECT_LIST_ID);

        Map<ListDataProject, GermplasmListData> germplasmMap = dut.generateGermplasmMap(TEST_GERMPLASM_LIST_DATA_LIST_ID,TEST_LISTDATA_PROJECT_LIST_ID);

        assertEquals(GERMPLASM_DATA_COUNT, germplasmMap.size());
        for (Map.Entry<ListDataProject, GermplasmListData> entry : germplasmMap.entrySet()) {
            ListDataProject ldp = entry.getKey();
            assertTrue(ldp.getEntryId().equals(entry.getValue().getEntryId()));
        }

    }

    protected Pair<List<ListDataProject>, List<GermplasmListData>> generateTestLists() {
        List<ListDataProject> dataProjectList = new ArrayList<>();
        List<GermplasmListData> germplasmListDatas = new ArrayList<>();

        for (int i =0; i < GERMPLASM_DATA_COUNT; i++) {
            ListDataProject ldp = new ListDataProject();
            ldp.setEntryId(i);
            ldp.setGermplasmId(i);

            dataProjectList.add(ldp);

            GermplasmListData datum = new GermplasmListData();
            datum.setEntryId(i);
            datum.setGid(i);
            germplasmListDatas.add(datum);
        }

        return Pair.of(dataProjectList, germplasmListDatas);
    }

    @Test
    public void testGenerateStockTabIfNecessaryAvailable() throws MiddlewareException{
        doReturn(true).when(inventoryDataManager).transactionsExistForListProjectDataListID(TEST_LISTDATA_PROJECT_LIST_ID);

        GermplasmList list = new GermplasmList();
        list.setName(TEST_LIST_NAME);
        list.setId(TEST_LISTDATA_PROJECT_LIST_ID);

        doReturn(list).when(germplasmListManager).getGermplasmListById(TEST_LISTDATA_PROJECT_LIST_ID);

        Model model = mock(Model.class);
        String returnVal = dut.generateStockTabIfNecessary(TEST_LISTDATA_PROJECT_LIST_ID, model);
        verify(model).addAttribute("listName", TEST_LIST_NAME);
        verify(model).addAttribute("listId", TEST_LISTDATA_PROJECT_LIST_ID);

        assertEquals("/NurseryManager/stockTab", returnVal);
    }

    @Test
    public void testGenerateStockTabIfNecessaryNotAvailable() throws MiddlewareException {
        doReturn(false).when(inventoryDataManager).transactionsExistForListProjectDataListID(TEST_LISTDATA_PROJECT_LIST_ID);

        Model model = mock(Model.class);
        String returnVal = dut.generateStockTabIfNecessary(TEST_LISTDATA_PROJECT_LIST_ID, model);

        assertEquals("/NurseryManager/blank", returnVal);
    }


}
