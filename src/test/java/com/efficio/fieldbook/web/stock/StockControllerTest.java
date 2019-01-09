package com.efficio.fieldbook.web.stock;

import com.efficio.fieldbook.util.FieldbookException;
import com.efficio.fieldbook.web.common.bean.UserSelection;
import com.efficio.fieldbook.web.common.form.ImportStockForm;
import com.efficio.fieldbook.web.common.service.ImportInventoryService;
import org.apache.commons.lang3.tuple.Pair;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedInventoryList;
import org.generationcp.commons.service.StockService;
import org.generationcp.commons.spring.util.ContextUtil;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.InventoryService;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.ui.Model;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Daniel Villafuerte on 5/8/2015.
 */

@RunWith(MockitoJUnitRunner.class)
@Ignore(value = "BMS-1571. Ignoring temporarily. Please fix the failures and remove @Ignore.")
public class StockControllerTest {

	public static final String TEST_BREEDER_IDENTIFIER = "AB";
	public static final String TEST_SEPARATOR = "-";
	public static final int GERMPLASM_DATA_COUNT = 5;
	public static final int TEST_GERMPLASM_LIST_DATA_LIST_ID = 1;
	public static final int TEST_LISTDATA_PROJECT_LIST_ID = 2;
	public static final int TEST_CURRENT_USER_ID = 1;

	public static final String TEST_LIST_NAME = "MyListName";
	public static final String PROGRAM_UUID = "hsdkjflasf-783247-8347832";

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

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private ImportInventoryService importInventoryService;

	@Mock
	private UserSelection userSelection;

	@Mock
	private ContextUtil contextUtil;

	@InjectMocks
	private final StockController dut = Mockito.spy(new StockController());

	@Before
	public void init() {

		Mockito.when(contextUtil.getCurrentProgramUUID()).thenReturn(PROGRAM_UUID);
		dut.setContextUtil(contextUtil);

	}

	@Test
	public void testGetAllLocationList() {
		this.dut.getAllLocationList();
		Mockito.verify(this.fieldbookMiddlewareService, Mockito.times(1)).getLocationsByProgramUUID(PROGRAM_UUID);
	}

	@Test
	public void testGetLocationList() throws MiddlewareQueryException {
		this.dut.getLocationList();
		Mockito.verify(this.fiedbookService, Mockito.times(1)).getAllSeedLocations();
	}

	@Test
	public void testGetFavoriteLocationList() throws MiddlewareQueryException {
		final List<Integer> locationsIds = new ArrayList<>();
		locationsIds.add(1);
		Mockito.when(this.fiedbookService.getFavoriteProjectLocationIds(Matchers.anyString())).thenReturn(locationsIds);

		this.dut.getFavoriteLocationList();

		Mockito.verify(this.fiedbookService, Mockito.times(1)).getFavoriteProjectLocationIds(Matchers.anyString());
		Mockito.verify(this.fiedbookService, Mockito.times(1)).getFavoriteLocationByLocationIDs(locationsIds);
	}

	@Test
	public void testGetScaleList() throws MiddlewareQueryException {
		this.dut.getScaleList();
		Mockito.verify(this.ontologyService, Mockito.times(1)).getAllInventoryScales();
	}

	@Test
	public void testGenerateValidPrefix() throws MiddlewareException {
		final StockListGenerationSettings param =
				new StockListGenerationSettings(StockControllerTest.TEST_BREEDER_IDENTIFIER, StockControllerTest.TEST_SEPARATOR);
		final String validPrefix = StockControllerTest.TEST_BREEDER_IDENTIFIER + 1 + StockControllerTest.TEST_SEPARATOR;
		Mockito.doReturn(validPrefix).when(this.stockService)
				.calculateNextStockIDPrefix(StockControllerTest.TEST_BREEDER_IDENTIFIER, StockControllerTest.TEST_SEPARATOR);

		final Map<String, String> results = this.dut.retrieveNextStockIDPrefix(param);
		Assert.assertEquals(StockController.SUCCESS, results.get(StockController.IS_SUCCESS));
		Assert.assertEquals(StockControllerTest.TEST_BREEDER_IDENTIFIER + 1, results.get("prefix"));
	}

	@Test
	public void testCalculatePrefixMiddlewareExceptionThrown() throws MiddlewareException {
		final StockListGenerationSettings param =
				new StockListGenerationSettings(StockControllerTest.TEST_BREEDER_IDENTIFIER, StockControllerTest.TEST_SEPARATOR);
		Mockito.doThrow(MiddlewareException.class).when(this.stockService)
				.calculateNextStockIDPrefix(StockControllerTest.TEST_BREEDER_IDENTIFIER, StockControllerTest.TEST_SEPARATOR);

		final Map<String, String> results = this.dut.retrieveNextStockIDPrefix(param);
		Assert.assertEquals(StockController.FAILURE, results.get(StockController.IS_SUCCESS));

	}

	@Test
	public void testCalculatePrefixValidationError() throws MiddlewareException {
		final StockListGenerationSettings param = new StockListGenerationSettings("AB12", StockControllerTest.TEST_SEPARATOR);
		Mockito.doReturn("ABC").when(this.messageSource)
				.getMessage(Matchers.anyString(), Matchers.any(Object[].class), Matchers.any(Locale.class));
		final Map<String, String> results = this.dut.retrieveNextStockIDPrefix(param);
		Assert.assertEquals(StockController.FAILURE, results.get(StockController.IS_SUCCESS));
		Assert.assertNotNull(results.get(StockController.ERROR_MESSAGE));
		Assert.assertTrue(results.get(StockController.ERROR_MESSAGE).length() > 0);

	}

	@Test
	public void testGenerateStockListForAdvanceList() throws MiddlewareException {
		final StockListGenerationSettings param =
				new StockListGenerationSettings(StockControllerTest.TEST_BREEDER_IDENTIFIER, StockControllerTest.TEST_SEPARATOR);
		final String validPrefix = StockControllerTest.TEST_BREEDER_IDENTIFIER + 1 + StockControllerTest.TEST_SEPARATOR;
		Mockito.doReturn(validPrefix).when(this.stockService)
				.calculateNextStockIDPrefix(StockControllerTest.TEST_BREEDER_IDENTIFIER, StockControllerTest.TEST_SEPARATOR);
		Mockito.doReturn(StockControllerTest.TEST_GERMPLASM_LIST_DATA_LIST_ID).when(this.germplasmListManager)
				.retrieveDataListIDFromListDataProjectListID(StockControllerTest.TEST_LISTDATA_PROJECT_LIST_ID);

		final Pair<List<ListDataProject>, List<GermplasmListData>> testData = this.generateTestLists();
		final List<ListDataProject> dataProjectList = testData.getLeft();
		final List<GermplasmListData> germplasmListDatas = testData.getRight();

		Mockito.doReturn(germplasmListDatas).when(this.germplasmListManager)
				.getGermplasmListDataByListId(StockControllerTest.TEST_GERMPLASM_LIST_DATA_LIST_ID);
		Mockito.doReturn(dataProjectList).when(this.germplasmListManager)
				.retrieveSnapshotListData(StockControllerTest.TEST_LISTDATA_PROJECT_LIST_ID);

		Mockito.doReturn(StockControllerTest.TEST_CURRENT_USER_ID).when(this.dut).getCurrentIbdbUserId();

		final Map<String, String> resultMap = this.dut.generateStockList(param, StockControllerTest.TEST_LISTDATA_PROJECT_LIST_ID);

		final ArgumentCaptor<InventoryDetails> detailParam = ArgumentCaptor.forClass(InventoryDetails.class);
		final ArgumentCaptor<ListDataProject> listDataParam = ArgumentCaptor.forClass(ListDataProject.class);
		Mockito.verify(this.inventoryService, Mockito.atMost(StockControllerTest.GERMPLASM_DATA_COUNT))
				.addLotAndTransaction(detailParam.capture(), Matchers.any(GermplasmListData.class), listDataParam.capture());

		Assert.assertEquals(StockController.SUCCESS, resultMap.get(StockController.IS_SUCCESS));

		Assert.assertEquals(validPrefix + listDataParam.getValue().getEntryId(), detailParam.getValue().getInventoryID());
		Assert.assertEquals(listDataParam.getValue().getGermplasmId(), detailParam.getValue().getGid());
		Assert.assertEquals(StockControllerTest.TEST_GERMPLASM_LIST_DATA_LIST_ID, detailParam.getValue().getSourceId().intValue());

	}

	@Test
	public void testGenerateGermplasmMap() throws MiddlewareException {
		final Pair<List<ListDataProject>, List<GermplasmListData>> testData = this.generateTestLists();
		final List<ListDataProject> dataProjectList = testData.getLeft();
		final List<GermplasmListData> germplasmListDatas = testData.getRight();

		Mockito.doReturn(germplasmListDatas).when(this.germplasmListManager)
				.getGermplasmListDataByListId(StockControllerTest.TEST_GERMPLASM_LIST_DATA_LIST_ID);
		Mockito.doReturn(dataProjectList).when(this.germplasmListManager)
				.retrieveSnapshotListData(StockControllerTest.TEST_LISTDATA_PROJECT_LIST_ID);

		final Map<ListDataProject, GermplasmListData> germplasmMap =
				this.dut.generateGermplasmMap(StockControllerTest.TEST_GERMPLASM_LIST_DATA_LIST_ID,
						StockControllerTest.TEST_LISTDATA_PROJECT_LIST_ID);

		Assert.assertEquals(StockControllerTest.GERMPLASM_DATA_COUNT, germplasmMap.size());
		for (final Map.Entry<ListDataProject, GermplasmListData> entry : germplasmMap.entrySet()) {
			final ListDataProject ldp = entry.getKey();
			Assert.assertTrue(ldp.getEntryId().equals(entry.getValue().getEntryId()));
		}

	}

	protected Pair<List<ListDataProject>, List<GermplasmListData>> generateTestLists() {
		final List<ListDataProject> dataProjectList = new ArrayList<>();
		final List<GermplasmListData> germplasmListDatas = new ArrayList<>();

		for (int i = 0; i < StockControllerTest.GERMPLASM_DATA_COUNT; i++) {
			final ListDataProject ldp = new ListDataProject();
			ldp.setEntryId(i);
			ldp.setGermplasmId(i);

			dataProjectList.add(ldp);

			final GermplasmListData datum = new GermplasmListData();
			datum.setEntryId(i);
			datum.setGid(i);
			germplasmListDatas.add(datum);
		}

		return Pair.of(dataProjectList, germplasmListDatas);
	}

	@Test
	public void testGenerateStockTabIfNecessaryAvailable() throws MiddlewareException {
		Mockito.doReturn(true).when(this.inventoryDataManager)
				.transactionsExistForListProjectDataListID(StockControllerTest.TEST_LISTDATA_PROJECT_LIST_ID);

		final GermplasmList list = new GermplasmList();
		list.setName(StockControllerTest.TEST_LIST_NAME);
		list.setId(StockControllerTest.TEST_LISTDATA_PROJECT_LIST_ID);

		Mockito.doReturn(list).when(this.germplasmListManager).getGermplasmListById(StockControllerTest.TEST_LISTDATA_PROJECT_LIST_ID);

		final Model model = Mockito.mock(Model.class);
		final String returnVal = this.dut.generateStockTabIfNecessary(StockControllerTest.TEST_LISTDATA_PROJECT_LIST_ID, model);
		Mockito.verify(model).addAttribute("listName", StockControllerTest.TEST_LIST_NAME);
		Mockito.verify(model).addAttribute("listId", StockControllerTest.TEST_LISTDATA_PROJECT_LIST_ID);

		Assert.assertEquals("/NurseryManager/stockTab", returnVal);
	}

	@Test
	public void testGenerateStockTabIfNecessaryNotAvailable() throws MiddlewareException {
		Mockito.doReturn(false).when(this.inventoryDataManager)
				.transactionsExistForListProjectDataListID(StockControllerTest.TEST_LISTDATA_PROJECT_LIST_ID);

		final Model model = Mockito.mock(Model.class);
		final String returnVal = this.dut.generateStockTabIfNecessary(StockControllerTest.TEST_LISTDATA_PROJECT_LIST_ID, model);

		Assert.assertEquals("/NurseryManager/blank", returnVal);
	}

	@Test
	public void testImportList() throws MiddlewareException, FieldbookException, FileParsingException {
		final Integer listId = 5;
		final GermplasmListType germplasmListType = GermplasmListType.ADVANCED;
		final ImportStockForm form = new ImportStockForm();
		form.setStockListId(listId);

		final Map<String, Object> additionalParams = new HashMap<>();

		final MultipartFile file = new MockMultipartFile("FileName.xls", "OriginalFileName.xlsx", "application/octet-stream", new byte[0]);
		form.setFile(file);

		final GermplasmList germplasmList = new GermplasmList();
		germplasmList.setType(germplasmListType.name());

		final InventoryDetails inventoryDetails = new InventoryDetails();
		final List<InventoryDetails> inventoryDetailsList = new ArrayList<>();
		inventoryDetailsList.add(inventoryDetails);

		final ImportedInventoryList importedInventoryList = new ImportedInventoryList();
		importedInventoryList.setFilename(file.getName());
		importedInventoryList.setImportedInventoryDetails(inventoryDetailsList);

		Mockito.when(this.fieldbookMiddlewareService.getGermplasmListById(listId)).thenReturn(germplasmList);
		Mockito.when(this.importInventoryService.parseFile(form.getFile(), additionalParams)).thenReturn(importedInventoryList);
		Mockito.when(this.inventoryService.getInventoryListByListDataProjectListId(listId)).thenReturn(inventoryDetailsList);
		Mockito.when(this.importInventoryService
				.hasConflict(Mockito.anyListOf(InventoryDetails.class), Mockito.isA(ImportedInventoryList.class)))
				.thenReturn(Boolean.FALSE);

		Mockito.doNothing().when(this.importInventoryService)
				.validateInventoryDetails(inventoryDetailsList, importedInventoryList, germplasmListType);
		Mockito.doNothing().when(this.importInventoryService)
				.mergeInventoryDetails(inventoryDetailsList, importedInventoryList, germplasmListType);
		Mockito.doNothing().when(this.inventoryDataManager).updateInventory(listId, inventoryDetailsList);
		Mockito.doNothing().when(this.userSelection).setListId(listId);
		Mockito.doNothing().when(this.userSelection).setInventoryDetails(inventoryDetailsList);

		final String result = dut.importList(form);

		Assert.assertTrue("Incorrect List Id", result.contains("5"));
		Assert.assertTrue("Has Error", result.contains("false"));
		Assert.assertTrue("Incorrect List Type", result.contains("ADVANCED"));

	}
}
