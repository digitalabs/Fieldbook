package com.efficio.fieldbook.web.util.parsing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.generationcp.commons.parsing.FileParsingException;
import org.generationcp.commons.parsing.pojo.ImportedInventoryList;
import org.generationcp.commons.parsing.validation.BulkComplValidator;
import org.generationcp.commons.parsing.validation.CommaDelimitedValueValidator;
import org.generationcp.commons.parsing.validation.NonEmptyValidator;
import org.generationcp.commons.parsing.validation.ParseValidationMap;
import org.generationcp.commons.parsing.validation.ValueRangeValidator;
import org.generationcp.commons.parsing.validation.ValueTypeValidator;
import org.generationcp.commons.service.FileService;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.oms.Scale;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.OntologyService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.context.MessageSource;


/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 * Date: 2/26/2015
 * Time: 4:42 PM
 */

@RunWith(MockitoJUnitRunner.class)
public class InventoryImportParserTest {

	@Mock
	private FileService fileService;

	@Mock
	private MessageSource messageSource;

	@Mock
	private FieldbookService fieldbookMiddlewareService;

	@Mock
	private OntologyService ontologyService;
	
	@Mock
	private InventoryDataManager inventoryDataManager;

	@InjectMocks
	private InventoryImportParser parser;

	private InventoryImportParser moled;

	public static final String TEST_FILE_NAME = "Inventory_Import_Template_v1.xlsx";
	public static final String TEST_FILE_NAME_WITH_BULKING = "Inventory_Import_Template_Bulking.xlsx";
	public static final int DUMMY_INDEX = 1;
	private static final int TEST_LIST_ID = 1;

	private static final String STOCKID_PREFIX = "SID";

	private List<Location> testLocationList;
	private List<Scale> testScaleList;
	private List<String> testStockIds;
	
	private String[] headers;
	private Map<InventoryHeaderLabels,Integer> inventoryHeaderLabelsMap;
	
	@Before
	public void setUp() throws Exception {
		moled = spy(parser);
		testLocationList = createDummyLocationList();
		testScaleList = createDummyScaleList();
		testStockIds = createDummyStockIds();
		doReturn(testLocationList).when(fieldbookMiddlewareService).getAllLocations();
		doReturn(testScaleList).when(ontologyService).getAllInventoryScales();
		doReturn(testStockIds).when(inventoryDataManager).
			getStockIdsByListDataProjectListId(TEST_LIST_ID);
	}

	private Workbook createWorkbook(GermplasmListType germplasmListType) throws Exception {
		String filename = TEST_FILE_NAME;
		if(germplasmListType == GermplasmListType.CROSSES) {
			filename = TEST_FILE_NAME_WITH_BULKING;
		}
		File workbookFile = new File(ClassLoader.getSystemClassLoader().getResource(filename).toURI());
		
		assert workbookFile.exists();

		return WorkbookFactory.create(workbookFile);
	}

	private List<String> createDummyStockIds() {
		List<String> stockIds = new ArrayList<String>();
		for(int i=0;i<5;i++) {
			stockIds.add(STOCKID_PREFIX+"1-"+i);
		}
		return stockIds;
	}

	@Test
	public void testBuildAllowedLocationList() {
		moled.setLocations(testLocationList);
		List<String> allowedLocationsList = moled.buildAllowedLocationsList();
		assertEquals(testLocationList.size(), allowedLocationsList.size());

	}

	@Test
	public void testBuildAllowedScaleList() {
		moled.setScales(testScaleList);
		List<String> allowedScaleList = moled.buildAllowedScaleList();

		assertEquals(testScaleList.size(), allowedScaleList.size());
	}

	@Test
	public void testConvertToLocationMap() {
		Map<String, Location> locationMap = moled.convertToLocationMap(testLocationList);

		assertEquals(locationMap.size(), testLocationList.size());
		Collection<Location> values = locationMap.values();
		for (Location location : testLocationList) {
			assertTrue(values.contains(location));
		}
	}

	@Test
	public void testConvertToScaleMap() {
		Map<String, Scale> scaleMap = moled.convertToScaleMap(testScaleList);

		assertEquals(scaleMap.size(), testScaleList.size());
		Collection<Scale> values = scaleMap.values();
		for (Scale scale : testScaleList) {
			assertTrue(values.contains(scale));
		}
	}

	@Test
	public void testSetupParsingValidations() {
		generateHeaders(GermplasmListType.CROSSES);
		ParseValidationMap map = moled.setupIndividualColumnValidation();
		assertTrue(!map.getValidations(
				inventoryHeaderLabelsMap.get(InventoryHeaderLabels.LOCATION)).isEmpty());
		assertTrue(!map.getValidations(
				inventoryHeaderLabelsMap.get(InventoryHeaderLabels.SCALE)).isEmpty());
		assertTrue(!map.getValidations(
				inventoryHeaderLabelsMap.get(InventoryHeaderLabels.AMOUNT)).isEmpty());
		assertTrue(!map.getValidations(
				inventoryHeaderLabelsMap.get(InventoryHeaderLabels.ENTRY)).isEmpty());
		assertTrue(!map.getValidations(
				inventoryHeaderLabelsMap.get(InventoryHeaderLabels.GID)).isEmpty());
		assertTrue(!map.getValidations(
				inventoryHeaderLabelsMap.get(InventoryHeaderLabels.BULK_WITH)).isEmpty());
		assertTrue(!map.getValidations(
				inventoryHeaderLabelsMap.get(InventoryHeaderLabels.BULK_COMPL)).isEmpty());

		assertTrue(map.getValidations(
				inventoryHeaderLabelsMap.get(InventoryHeaderLabels.LOCATION)).get(0) instanceof ValueRangeValidator);
		assertTrue(map.getValidations(
				inventoryHeaderLabelsMap.get(InventoryHeaderLabels.SCALE)).get(0) instanceof ValueRangeValidator);
		assertTrue(map.getValidations(
				inventoryHeaderLabelsMap.get(InventoryHeaderLabels.AMOUNT)).get(0) instanceof ValueTypeValidator);
		assertTrue(map.getValidations(
				inventoryHeaderLabelsMap.get(InventoryHeaderLabels.ENTRY)).get(0) instanceof ValueTypeValidator);
		assertTrue(map.getValidations(
				inventoryHeaderLabelsMap.get(InventoryHeaderLabels.GID)).get(0) instanceof ValueTypeValidator);
		assertTrue(map.getValidations(
				inventoryHeaderLabelsMap.get(InventoryHeaderLabels.ENTRY)).get(1) instanceof NonEmptyValidator);
		assertTrue(map.getValidations(
				inventoryHeaderLabelsMap.get(InventoryHeaderLabels.GID)).get(1) instanceof NonEmptyValidator);
		assertTrue(map.getValidations(
				inventoryHeaderLabelsMap.get(InventoryHeaderLabels.BULK_WITH)).get(0) instanceof CommaDelimitedValueValidator);
		assertTrue(map.getValidations(
				inventoryHeaderLabelsMap.get(InventoryHeaderLabels.BULK_COMPL)).get(0) instanceof BulkComplValidator);
	}

	private void generateHeaders(GermplasmListType crosses) {
		inventoryHeaderLabelsMap = 
				InventoryHeaderLabels.headers(GermplasmListType.CROSSES);
		headers = InventoryHeaderLabels.getHeaderNames(inventoryHeaderLabelsMap);
		moled.setInventoryHeaderLabelsMap(inventoryHeaderLabelsMap);
		moled.setHeaders(headers);
	}

	@Test(expected = FileParsingException.class)
	public void testObjectConversionNotAllInventoryItemsPresent() throws Exception{
		GermplasmListType germplasmListType = GermplasmListType.CROSSES;
		generateHeaders(germplasmListType);
		InventoryImportParser.InventoryRowConverter rowConverter = createForTestingRowConverter(
				createWorkbook(germplasmListType));
		Map<Integer, String> testRowValue = new HashMap<>();
		testRowValue.put(
				inventoryHeaderLabelsMap.get(InventoryHeaderLabels.ENTRY), "1");
		testRowValue.put(
				inventoryHeaderLabelsMap.get(InventoryHeaderLabels.GID), "-10");
		testRowValue.put(
				inventoryHeaderLabelsMap.get(InventoryHeaderLabels.LOCATION), "DUMMY LOCATION");

		// only location has a value out of the three inventory related columns, an exception needs to be thrown
		rowConverter.convertToObject(testRowValue);
	}

	@Test
	public void testObjectConversionNoInventoryItemsPresent() throws Exception {
		GermplasmListType germplasmListType = GermplasmListType.ADVANCED;
		generateHeaders(germplasmListType);
		InventoryImportParser.InventoryRowConverter rowConverter = createForTestingRowConverter(
				createWorkbook(germplasmListType));
		Map<Integer, String> testRowValue = new HashMap<>();
		testRowValue.put(inventoryHeaderLabelsMap.get(InventoryHeaderLabels.ENTRY), "1");
		testRowValue.put(inventoryHeaderLabelsMap.get(InventoryHeaderLabels.GID), "-10");

		InventoryDetails details = rowConverter.convertToObject(testRowValue);
		assertNotNull(
				"Inventory details could not be properly created when all inventory related columns are blank",
				details);
		assertNull(details.getAmount());
		assertNull(details.getComment());
	}

	@Test
	public void testObjectConversionAllInventoryItemsPresent() throws Exception {
		GermplasmListType germplasmListType = GermplasmListType.CROSSES;
		generateHeaders(germplasmListType);
		InventoryImportParser.InventoryRowConverter rowConverter = 
				createForTestingRowConverter(createWorkbook(germplasmListType));
		Map<Integer, String> testRowValue = new HashMap<>();
		testRowValue.put(inventoryHeaderLabelsMap.get(InventoryHeaderLabels.ENTRY), "1");
		testRowValue.put(inventoryHeaderLabelsMap.get(InventoryHeaderLabels.GID), "-10");
		testRowValue.put(inventoryHeaderLabelsMap.get(InventoryHeaderLabels.LOCATION), "TEST1");
		testRowValue.put(inventoryHeaderLabelsMap.get(InventoryHeaderLabels.SCALE), "SCALE2");
		testRowValue.put(inventoryHeaderLabelsMap.get(InventoryHeaderLabels.AMOUNT), "15");

		InventoryDetails details = rowConverter.convertToObject(testRowValue);
		assertNotNull(
				"Inventory details could not be properly created when all inventory related columns are present",
				details);

		assertEquals(details.getAmount(), 15.0, 0);
		assertEquals("Inventory details not created with proper location abbr", "TEST1", details.getLocationAbbr());
		assertEquals("Inventory details not created with proper location id", new Integer(1), details.getLocationId());
		assertEquals("Inventory details not created with proper location name", "Test Location 1", details.getLocationName());
		assertEquals("Inventory details not created with proper scale id", new Integer(2), details.getScaleId());
		assertEquals("Inventory details not created with proper scale name", "SCALE2", details.getScaleName());
	}

	protected InventoryImportParser.InventoryRowConverter createForTestingRowConverter(Workbook workbook) {
		Map<String, Location> locationMap = moled.convertToLocationMap(testLocationList);
		Map<String, Scale> scaleMap = moled.convertToScaleMap(testScaleList);
		return new InventoryImportParser.InventoryRowConverter(workbook, DUMMY_INDEX,
				InventoryImportParser.INVENTORY_SHEET,
				inventoryHeaderLabelsMap.size(), inventoryHeaderLabelsMap, locationMap, scaleMap);
	}

	protected List<Location> createDummyLocationList() {
		List<Location> locationList = new ArrayList<>();

		Location location = new Location();
		location.setLocid(1);
		location.setLabbr("TEST1");
		location.setLname("Test Location 1");
		locationList.add(location);

		location = new Location();
		location.setLocid(2);
		location.setLabbr("TEST2");
		location.setLname("Test Location 2");
		locationList.add(location);


		return locationList;
	}

	protected List<Scale> createDummyScaleList() {
		List<Scale> scaleList = new ArrayList<>();

		Scale scale = new Scale(new Term(1, "SCALE1", "Test"));
		scale.setDisplayName("Test Scale 1");
		scaleList.add(scale);

		scale = new Scale(new Term(2, "SCALE2", "Test"));
		scale.setDisplayName("Test Display 2");
		scaleList.add(scale);

		return scaleList;
	}
	
	@Test
	public void testParseWorkbook_Crosses() throws Exception  {
		GermplasmListType germplasmListType = GermplasmListType.CROSSES;
		Map<String,Object> additionalParams = new HashMap<String,Object>();
		additionalParams.put(InventoryImportParser.HEADERS_MAP_PARAM_KEY, 
				InventoryHeaderLabels.headers(GermplasmListType.CROSSES));
		additionalParams.put(InventoryImportParser.LIST_ID_PARAM_KEY,TEST_LIST_ID);
		ImportedInventoryList importedInventoryList = moled.parseWorkbook(
				createWorkbook(germplasmListType), additionalParams);
		assertNotNull(importedInventoryList);
	}
}

